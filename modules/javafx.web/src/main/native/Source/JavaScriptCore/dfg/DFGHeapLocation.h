/*
 * Copyright (C) 2014-2019 Apple Inc. All rights reserved.
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

#pragma once

#if ENABLE(DFG_JIT)

#include "DFGAbstractHeap.h"
#include "DFGLazyNode.h"
#include "DFGNode.h"

namespace JSC { namespace DFG {

enum LocationKind {
    InvalidLocationKind,

    ArrayLengthLoc,
    ArrayMaskLoc,
    VectorLengthLoc,
    ButterflyLoc,
    CheckTypeInfoFlagsLoc,
    OverridesHasInstanceLoc,
    ClosureVariableLoc,
    DirectArgumentsLoc,
    GetterLoc,
    GlobalVariableLoc,
    EnumeratorNextUpdateIndexAndModeLoc,
    HasIndexedPropertyLoc,
    IndexedPropertyDoubleLoc,
    IndexedPropertyDoubleSaneChainLoc,
    IndexedPropertyDoubleOutOfBoundsSaneChainLoc,
    IndexedPropertyDoubleOrOtherOutOfBoundsSaneChainLoc,
    IndexedPropertyInt32Loc,
    IndexedPropertyInt32OutOfBoundsSaneChainLoc,
    IndexedPropertyInt52Loc,
    IndexedPropertyInt52OutOfBoundsSaneChainLoc,
    IndexedPropertyJSLoc,
    IndexedPropertyJSOutOfBoundsSaneChainLoc,
    IndexedPropertyStorageLoc,
    InvalidationPointLoc,
    IsCallableLoc,
    IsConstructorLoc,
    TypeOfIsObjectLoc,
    TypeOfIsFunctionLoc,
    NamedPropertyLoc,
    RegExpObjectLastIndexLoc,
    SetterLoc,
    StructureLoc,
    TypedArrayByteOffsetLoc,
    TypedArrayByteOffsetInt52Loc,
    TypedArrayLengthInt52Loc,
    PrototypeLoc,
    StackLoc,
    StackPayloadLoc,
    GlobalProxyTargetLoc,
    DateFieldLoc,
    MapBucketLoc,
    MapBucketHeadLoc,
    MapBucketValueLoc,
    MapBucketKeyLoc,
    MapBucketNextLoc,
    MapIteratorNextLoc,
    MapIteratorKeyLoc,
    MapIteratorValueLoc,
    MapStorageLoc,
    MapIterationNextLoc,
    MapIterationEntryLoc,
    MapIterationEntryKeyLoc,
    MapIterationEntryValueLoc,
    MapEntryKeyLoc,
    MapEntryValueLoc,
    LoadMapValueLoc,
    WeakMapGetLoc,
    InternalFieldObjectLoc,
    DOMStateLoc,
};

class HeapLocation {
public:
    HeapLocation(
        LocationKind kind = InvalidLocationKind,
        AbstractHeap heap = AbstractHeap(),
        Node* base = nullptr,
        LazyNode index = LazyNode(),
        Node* descriptor = nullptr,
        void* extraState = nullptr)
        : m_kind(kind)
        , m_heap(heap)
        , m_base(base)
        , m_index(index)
        , m_descriptor(descriptor)
        , m_extraState(extraState)
    {
        ASSERT((kind == InvalidLocationKind) == !heap);
        ASSERT(!!m_heap || !m_base);
        ASSERT(m_base || (!m_index && !m_descriptor && !m_extraState));
    }

    HeapLocation(LocationKind kind, AbstractHeap heap, Node* base, Node* index, Node* descriptor = nullptr)
        : HeapLocation(kind, heap, base, LazyNode(index), descriptor)
    {
    }

    HeapLocation(LocationKind kind, AbstractHeap heap, Edge base, Edge index = Edge(), Edge descriptor = Edge())
        : HeapLocation(kind, heap, base.node(), index.node(), descriptor.node())
    {
    }

    HeapLocation(LocationKind kind, AbstractHeap heap, Edge base, void* extraState)
        : HeapLocation(kind, heap, base.node(), nullptr, nullptr, extraState)
    {
    }

    HeapLocation(WTF::HashTableDeletedValueType)
        : m_kind(InvalidLocationKind)
        , m_heap(WTF::HashTableDeletedValue)
        , m_base(nullptr)
        , m_index(nullptr)
        , m_descriptor(nullptr)
    {
    }

    bool operator!() const { return !m_heap; }

    LocationKind kind() const { return m_kind; }
    AbstractHeap heap() const { return m_heap; }
    Node* base() const { return m_base; }
    LazyNode index() const { return m_index; }
    void* extraState() const { return m_extraState; }

    unsigned hash() const
    {
        return m_kind
            + m_heap.hash()
            + m_index.hash()
            + static_cast<unsigned>(bitwise_cast<uintptr_t>(m_base))
            + static_cast<unsigned>(bitwise_cast<uintptr_t>(m_descriptor))
            + static_cast<unsigned>(bitwise_cast<uintptr_t>(m_extraState));
    }

    friend bool operator==(const HeapLocation&, const HeapLocation&) = default;

    bool isHashTableDeletedValue() const
    {
        return m_heap.isHashTableDeletedValue();
    }

    void dump(PrintStream& out) const;

private:
    LocationKind m_kind;
    AbstractHeap m_heap;
    Node* m_base;
    LazyNode m_index;
    Node* m_descriptor;
    void* m_extraState { nullptr };
};

struct HeapLocationHash {
    static unsigned hash(const HeapLocation& key) { return key.hash(); }
    static bool equal(const HeapLocation& a, const HeapLocation& b) { return a == b; }
    static constexpr bool safeToCompareToEmptyOrDeleted = true;
};


inline LocationKind indexedPropertyLocForResultType(NodeFlags canonicalResultRepresentation)
{
    if (!canonicalResultRepresentation)
        return IndexedPropertyJSLoc;

    ASSERT((canonicalResultRepresentation & NodeResultMask) == canonicalResultRepresentation);
    switch (canonicalResultRepresentation) {
    case NodeResultDouble:
        return IndexedPropertyDoubleLoc;
    case NodeResultInt52:
        return IndexedPropertyInt52Loc;
    case NodeResultInt32:
        return IndexedPropertyInt32Loc;
    case NodeResultJS:
        return IndexedPropertyJSLoc;
    case NodeResultStorage:
        RELEASE_ASSERT_NOT_REACHED();
    default:
        break;
    }
    RELEASE_ASSERT_NOT_REACHED();
}

inline LocationKind indexedPropertyLocToOutOfBoundsSaneChain(LocationKind location)
{
    switch (location) {
    case IndexedPropertyInt32Loc:
        return IndexedPropertyInt32OutOfBoundsSaneChainLoc;
    case IndexedPropertyInt52Loc:
        return IndexedPropertyInt52OutOfBoundsSaneChainLoc;
    case IndexedPropertyDoubleLoc:
        return IndexedPropertyDoubleOutOfBoundsSaneChainLoc;
    case IndexedPropertyJSLoc:
        return IndexedPropertyJSOutOfBoundsSaneChainLoc;
    default:
        RELEASE_ASSERT_NOT_REACHED();
    }
}
} } // namespace JSC::DFG

namespace WTF {


template<typename T> struct DefaultHash;
template<> struct DefaultHash<JSC::DFG::HeapLocation> : JSC::DFG::HeapLocationHash { };

template<typename T> struct HashTraits;
template<> struct HashTraits<JSC::DFG::HeapLocation> : SimpleClassHashTraits<JSC::DFG::HeapLocation> {
    static constexpr bool emptyValueIsZero = false;
};

} // namespace WTF

#endif // ENABLE(DFG_JIT)
