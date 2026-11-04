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

package com.sun.jfx.incubator.scene.control.richtext;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import com.sun.jfx.incubator.scene.control.richtext.util.RichUtils;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.FoldRange;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;

/// Contains code folding logic and the visuals.
public class FoldController {

    private final Pane foldGutter;

    public FoldController(ObjectProperty<StyledTextModel> p) {
        foldGutter = new ClippedPane("fold-gutter");
        foldGutter.setManaged(false);
        // FIX 1em width does not work
        // TODO is this needed? or percentage of font size?
        // also, depends on the size of the toggle (both variants), unless the toggle is auto-sizing to parent
        //setPrefWidth(13);

        // TODO listen to the model
    }

    public void dispose() {
        // TODO
    }

    public Pane getGutter() {
        return foldGutter;
    }

    public double getWidth() {
        return foldGutter.prefWidth(-1);
    }

    public void updateToggles(StyledTextModel model, CellArrangement arrangement) {
        List<FoldRange> fs;
        if (model instanceof CodeTextModel m) {
            int start = arrangement.topIndex();
            int end = arrangement.bottomIndex();
            fs = m.getFoldRanges(start, end);
        } else {
            fs = null;
        }

        List<Node> children = foldGutter.getChildren();
        if ((fs == null) || fs.isEmpty()) {
            children.clear();
            return;
        }

        // remove overflow
        int ct = children.size();
        for (int i = ct - 1; i >= fs.size(); i--) {
            children.remove(i);
        }

        // update visible toggles
        int i = 0;
        for ( ; i < fs.size(); i++) {
            FoldRange f = fs.get(i);
            Node ch;
            if (i < ct) {
                ch = children.get(i);
            } else {
                ch = createToggle();
                ch.addEventHandler(MouseEvent.MOUSE_CLICKED, (ev) -> {
                    handleToggle(f);
                });
                children.add(ch);
            }
            TextCell c = arrangement.getCell(f.getStart());
            // FIX does not fill height, don't know why
            RichUtils.layoutInArea(ch, 0, c.getY(), getWidth(), c.getHeight());
        }
    }

    private Node createToggle() {
        Pane t = new Pane();
        t.setMouseTransparent(true);
        t.getStyleClass().add("fold-toggle");

        Pane ch = new Pane(t);
        ch.setMaxHeight(Double.MAX_VALUE);
        ch.getStyleClass().add("fold-toggle-outer");
        return ch;
    }

    private void handleToggle(FoldRange f) {
        IO.println(f); // FIX
    }
}
