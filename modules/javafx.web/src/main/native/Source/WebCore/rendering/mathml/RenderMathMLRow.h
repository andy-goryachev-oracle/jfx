/*
 * Copyright (C) 2010 Alex Milowski (alex@milowski.com). All rights reserved.
 * Copyright (C) 2016 Igalia S.L.
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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#pragma once

#if ENABLE(MATHML)

#include "RenderMathMLBlock.h"

namespace WebCore {

class MathMLRowElement;

class RenderMathMLRow : public RenderMathMLBlock {
    WTF_MAKE_TZONE_OR_ISO_ALLOCATED(RenderMathMLRow);
    WTF_OVERRIDE_DELETE_FOR_CHECKED_PTR(RenderMathMLRow);
public:
    RenderMathMLRow(Type, MathMLRowElement&, RenderStyle&&);
    MathMLRowElement& element() const;
    virtual ~RenderMathMLRow();

protected:
    void layoutBlock(bool relayoutChildren, LayoutUnit pageLogicalHeight = 0_lu) override;
    std::optional<LayoutUnit> firstLineBaseline() const override;

    void stretchVerticalOperatorsAndLayoutChildren();
    void getContentBoundingBox(LayoutUnit& width, LayoutUnit& ascent, LayoutUnit& descent) const;
    void layoutRowItems(LayoutUnit width, LayoutUnit ascent);
    void shiftRowItems(LayoutUnit left, LayoutUnit top);
    LayoutUnit preferredLogicalWidthOfRowItems();
    void computePreferredLogicalWidths() override;

private:
    ASCIILiteral renderName() const override { return "RenderMathMLRow"_s; }
};

} // namespace WebCore

SPECIALIZE_TYPE_TRAITS_RENDER_OBJECT(RenderMathMLRow, isRenderMathMLRow())

#endif // ENABLE(MATHML)
