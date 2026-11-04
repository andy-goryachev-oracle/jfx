/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jfx.incubator.scene.control.richtext.skin;

import java.util.List;
import javafx.scene.Node;
import com.sun.jfx.incubator.scene.control.richtext.CellArrangement;
import com.sun.jfx.incubator.scene.control.richtext.ClippedPane;
import com.sun.jfx.incubator.scene.control.richtext.TextCell;
import com.sun.jfx.incubator.scene.control.richtext.util.RichUtils;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.FoldRange;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;

/**
 * The vertical bar which contains the folding toggles,
 * shown to the left of the content area when code folding is enabled.
 */
public class FoldGutter extends ClippedPane {

    // TODO pass the model property?
    public FoldGutter() {
        super("fold-gutter");
        setManaged(false);
        // TODO is this needed? or percentage of font size?
        // also, depends on the size of the toggle (both variants), unless the toggle is auto-sizing to parent
        //setPrefWidth(13);
    }

    // FIX CellArrangement is not public!
    public void update(StyledTextModel model, CellArrangement a) {
        List<FoldRange> fs;
        if (model instanceof CodeTextModel m) {
            int start = a.getOriginIndex();
            int end = start + a.getVisibleCellCount();
            fs = m.getFoldRanges(start, end);
        } else {
            fs = null;
        }

        if ((fs == null) || fs.isEmpty()) {
            getChildren().clear();
            return;
        }

        // update visible toggles
        List<Node> cs = getChildren();
        int i = 0;
        for ( ; i < fs.size(); i++) {
            FoldRange f = fs.get(i);
            FoldToggle t;
            if (i < cs.size()) {
                t = (FoldToggle)cs.get(i);
            } else {
                t = new FoldToggle();
                // TODO click handler - toggle
                cs.add(t);
            }
            TextCell c = a.getCell(f.getStart());
            RichUtils.layoutInArea(t, 0, c.getY(), getWidth(), c.getHeight());
        }
        while (i < cs.size()) {
            // FIX suboptimal: unnecessary memmove.  delete the tail before iterating
            cs.remove(i);
        }
    }
}
