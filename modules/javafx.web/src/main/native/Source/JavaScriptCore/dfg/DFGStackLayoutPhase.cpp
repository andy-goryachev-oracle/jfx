/*
 * Copyright (C) 2013-2019 Apple Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY APPLE INC. ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL APPLE INC. OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "config.h"
#include "DFGStackLayoutPhase.h"

#if ENABLE(DFG_JIT)

#include "DFGGraph.h"
#include "DFGPhase.h"
#include "DFGValueSource.h"
#include "JSCJSValueInlines.h"

namespace JSC { namespace DFG {

class StackLayoutPhase : public Phase {
    static constexpr bool verbose = false;

public:
    StackLayoutPhase(Graph& graph)
        : Phase(graph, "stack layout"_s)
    {
    }

    bool run()
    {
        // This enumerates the locals that we actually care about and packs them. So for example
        // if we use local 1, 3, 4, 5, 7, then we remap them: 1->0, 3->1, 4->2, 5->3, 7->4. We
        // treat a variable as being "used" if there exists an access to it (SetLocal, GetLocal,
        // Flush, PhantomLocal).

        Operands<bool> usedOperands(0, graph().m_localVars, graph().m_tmps, false);

        // Collect those variables that are used from IR.
        bool hasNodesThatNeedFixup = false;
        for (BlockIndex blockIndex = m_graph.numBlocks(); blockIndex--;) {
            BasicBlock* block = m_graph.block(blockIndex);
            if (!block)
                continue;
            for (unsigned nodeIndex = block->size(); nodeIndex--;) {
                Node* node = block->at(nodeIndex);
                switch (node->op()) {
                case GetLocal:
                case SetLocal:
                case Flush:
                case PhantomLocal: {
                    VariableAccessData* variable = node->variableAccessData();
                    if (variable->operand().isArgument())
                        break;
                    usedOperands.setOperand(variable->operand(), true);
                    break;
                }

                case LoadVarargs:
                case ForwardVarargs: {
                    LoadVarargsData* data = node->loadVarargsData();
                    usedOperands.setOperand(data->count, true);
                    if (data->start.isLocal()) {
                        // This part really relies on the contiguity of stack layout
                        // assignments.
                        ASSERT(VirtualRegister(data->start.offset() + data->limit - 1).isLocal());
                        for (unsigned i = data->limit; i--;)
                            usedOperands.setOperand(VirtualRegister(data->start.offset() + i), true);
                    } // the else case shouldn't happen.
                    hasNodesThatNeedFixup = true;
                    break;
                }

                case PutStack:
                case GetStack: {
                    StackAccessData* stack = node->stackAccessData();
                    if (stack->operand.isArgument())
                        break;
                    usedOperands.setOperand(stack->operand, true);
                    break;
                }

                default:
                    break;
                }
            }
        }

        for (InlineCallFrameSet::iterator iter = m_graph.m_plan.inlineCallFrames()->begin(); !!iter; ++iter) {
            InlineCallFrame* inlineCallFrame = *iter;

            if (inlineCallFrame->isVarargs()) {
                usedOperands.setOperand(VirtualRegister(
                    CallFrameSlot::argumentCountIncludingThis + inlineCallFrame->stackOffset), true);
            }

            for (unsigned argument = inlineCallFrame->m_argumentsWithFixup.size(); argument--;) {
                usedOperands.setOperand(VirtualRegister(
                    virtualRegisterForArgumentIncludingThis(argument).offset() +
                    inlineCallFrame->stackOffset), true);
            }
        }

        Vector<unsigned> allocation(usedOperands.size());
        m_graph.m_nextMachineLocal = CodeBlock::calleeSaveSpaceAsVirtualRegisters(RegisterAtOffsetList::dfgCalleeSaveRegisters());
        for (unsigned i = 0; i < usedOperands.size(); ++i) {
            if (!usedOperands.getForOperandIndex(i)) {
                allocation[i] = UINT_MAX;
                continue;
            }

            allocation[i] = m_graph.m_nextMachineLocal++;
        }

        for (unsigned i = m_graph.m_variableAccessData.size(); i--;) {
            VariableAccessData* variable = &m_graph.m_variableAccessData[i];
            if (!variable->isRoot())
                continue;

            if (variable->operand().isArgument()) {
                variable->machineLocal() = variable->operand().virtualRegister();
                continue;
            }

            Operand operand = variable->operand();
            size_t index = usedOperands.operandIndex(operand);
            if (index >= allocation.size())
                continue;

            if (allocation[index] == UINT_MAX)
                continue;

            variable->machineLocal() = assign(usedOperands, allocation, variable->operand());
        }

        for (StackAccessData* data : m_graph.m_stackAccessData) {
            if (data->operand.isArgument()) {
                data->machineLocal = data->operand.virtualRegister();
                continue;
            }

            if (data->operand.isLocal()) {
                if (static_cast<size_t>(data->operand.toLocal()) >= allocation.size())
                    continue;
                if (allocation[data->operand.toLocal()] == UINT_MAX)
                    continue;
            }

            data->machineLocal = assign(usedOperands, allocation, data->operand);
        }

        if (!m_graph.needsScopeRegister())
            codeBlock()->setScopeRegister(VirtualRegister());
        else
            codeBlock()->setScopeRegister(assign(usedOperands, allocation, codeBlock()->scopeRegister()));

        for (unsigned i = m_graph.m_inlineVariableData.size(); i--;) {
            InlineVariableData data = m_graph.m_inlineVariableData[i];
            InlineCallFrame* inlineCallFrame = data.inlineCallFrame;

            if (inlineCallFrame->isVarargs())
                inlineCallFrame->argumentCountRegister = assign(usedOperands, allocation, VirtualRegister(inlineCallFrame->stackOffset + CallFrameSlot::argumentCountIncludingThis));

            for (unsigned argument = inlineCallFrame->m_argumentsWithFixup.size(); argument--;) {
                ArgumentPosition& position = m_graph.m_argumentPositions[
                    data.argumentPositionStart + argument];
                VariableAccessData* variable = position.someVariable();
                ValueSource source;
                if (!variable)
                    source = ValueSource(SourceIsDead);
                else {
                    source = ValueSource::forFlushFormat(
                        variable->machineLocal(), variable->flushFormat());
                }
                inlineCallFrame->m_argumentsWithFixup[argument] = source.valueRecovery();
            }

            RELEASE_ASSERT(inlineCallFrame->isClosureCall == !!data.calleeVariable);
            if (inlineCallFrame->isClosureCall) {
                VariableAccessData* variable = data.calleeVariable->find();
                ValueSource source = ValueSource::forFlushFormat(
                    variable->machineLocal(),
                    variable->flushFormat());
                inlineCallFrame->calleeRecovery = source.valueRecovery();
            } else
                RELEASE_ASSERT(inlineCallFrame->calleeRecovery.isConstant());
        }

        // Fix Varargs' variable references.
        if (hasNodesThatNeedFixup) {
            for (BlockIndex blockIndex = m_graph.numBlocks(); blockIndex--;) {
                BasicBlock* block = m_graph.block(blockIndex);
                if (!block)
                    continue;
                for (unsigned nodeIndex = block->size(); nodeIndex--;) {
                    Node* node = block->at(nodeIndex);
                    switch (node->op()) {
                    case LoadVarargs:
                    case ForwardVarargs: {
                        LoadVarargsData* data = node->loadVarargsData();
                        data->machineCount = assign(usedOperands, allocation, data->count);
                        data->machineStart = assign(usedOperands, allocation, data->start);
                        break;
                    }

                    default:
                        break;
                    }
                }
            }
        }

        return true;
    }

private:
    VirtualRegister assign(const Operands<bool>& usedOperands, const Vector<unsigned>& allocation, Operand operand)
    {
        if (operand.isArgument())
            return operand.virtualRegister();

        size_t operandIndex = usedOperands.operandIndex(operand);
        unsigned myAllocation = allocation[operandIndex];
        if (myAllocation == UINT_MAX)
            return VirtualRegister();
        return virtualRegisterForLocal(myAllocation);
    }
};

bool performStackLayout(Graph& graph)
{
    return runPhase<StackLayoutPhase>(graph);
}

} } // namespace JSC::DFG

#endif // ENABLE(DFG_JIT)

