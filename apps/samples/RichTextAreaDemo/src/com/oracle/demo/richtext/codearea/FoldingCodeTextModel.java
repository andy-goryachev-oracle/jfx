/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
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

package com.oracle.demo.richtext.codearea;

import java.util.ArrayList;
import java.util.List;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.FoldRange;
import jfx.incubator.scene.control.richtext.model.RichParagraph;

/**
 * CodeTextModel which provides a naive code folding based on curly braces.
 */
public class FoldingCodeTextModel extends CodeTextModel {

    public FoldingCodeTextModel() {
    }

    @Override
    public boolean isFoldingSupported() {
        return true;
    }

    static class Fold {
        public final int start;
        public int end = -1;
        public int extra;

        public Fold(int start) {
            this.start = start;
            this.end = -1;
        }
    }

    @Override
    public List<FoldRange> getFoldRanges(int start, int end) {
        ArrayList<Fold> folds = new ArrayList<>();
        for (int ix = start; ix <= end; ix++) {
            String s = getPlainText(ix);
            int len = s.length();
            for (int i = 0; i < len; i++) {
                Fold f;
                char c = s.charAt(i);
                switch (c) {
                case '{':
                    // start fold candidate
                    f = onSameLine(folds, ix);
                    if (f == null) {
                        folds.add(new Fold(ix));
                    } else {
                        f.extra++;
                    }
                    break;
                case '}':
                    // end fold
                    f = findLastOpen(folds);
                    if (f != null) {
                        if (f.extra > 0) {
                            f.extra--;
                            continue;
                        }
                    }
                    f.end = ix;
                    break;
                }
            }
        }
        ArrayList<FoldRange> r = new ArrayList<>(folds.size());
        for (Fold f : folds) {
            if ((f.extra == 0) && (f.end > 0)) {
                r.add(toFoldRange(f));
            }
        }
        return r;
    }

    private static Fold onSameLine(List<Fold> folds, int ix) {
        int i = folds.size() - 1;
        if (i >= 0) {
            Fold f = folds.get(i);
            if (f.start == ix) {
                return f;
            }
        }
        return null;
    }

    private static Fold findLastOpen(List<Fold> folds) {
        for (int i = folds.size() - 1; i >= 0; i--) {
            Fold f = folds.get(i);
            if (f.end < 0) {
                return f;
            }
        }
        return null;
    }

    private FoldRange toFoldRange(Fold f) {
        int start = f.start;
        int end = f.end;

        return new FoldRange() {
            @Override
            public int getStart() {
                return start;
            }

            @Override
            public int getEnd() {
                return end;
            }

            @Override
            public RichParagraph getPlaceholder() {
                // TODO
                return null;
            }

            @Override
            public String toString() {
                return "FoldRange(" + start + "," + end + ")";
            }
        };
    }
}
