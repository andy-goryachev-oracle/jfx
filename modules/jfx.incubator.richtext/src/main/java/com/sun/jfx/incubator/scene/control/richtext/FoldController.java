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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Subscription;
import com.sun.jfx.incubator.scene.control.richtext.util.RichUtils;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.ContentChange;
import jfx.incubator.scene.control.richtext.model.FoldRange;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;

/// Contains code folding logic and the visuals.
public class FoldController implements StyledTextModel.Listener {

    private static final PseudoClass COLLAPSED = PseudoClass.getPseudoClass("collapsed");
    private final VFlow vflow;
    private final Pane gutter;
    private CodeTextModel model;
    private Subscription sub;
    private ArrayList<FoldRange> collapsed;

    // TODO alternative: pass VFlow instance instead
    public FoldController(VFlow vflow) {
        this.vflow = vflow;
        gutter = new ClippedPane("fold-gutter");
        gutter.setManaged(false);
        // FIX 1em width does not work
        // TODO is this needed? or percentage of font size?
        // also, depends on the size of the toggle (both variants), unless the toggle is auto-sizing to parent
        // setPrefWidth(13);

        // listen to the model changes
        sub = vflow.control.modelProperty().subscribe((m) -> {
            if (model != null) {
                model.removeListener(this);
                model = null;
            }
            if (m instanceof CodeTextModel cm) {
                m.addListener(this);
                model = cm;
            }
        });
    }

    public void dispose() {
        if (sub != null) {
            sub.unsubscribe();
        }
    }

    public Pane getGutter() {
        return gutter;
    }

    public double getWidth() {
        return gutter.prefWidth(-1);
    }

    public FoldRange getCollapsedRange(int modelIndex) {
        if (collapsed != null) {
            // expecting a small number of folded ranges, so linear search should be ok
            for (FoldRange f : collapsed) {
                if (f.contains(modelIndex)) {
                    return f;
                }
            }
        }
        return null;
    }

    public void updateToggles(CellArrangement arrangement) {
        List<FoldRange> fs;
        if (model != null) {
            int start = arrangement.topIndex();
            int end = arrangement.bottomIndex();
            fs = model.getFoldRanges(start, end);
        } else {
            fs = null;
        }

        List<Node> children = gutter.getChildren();
        if ((fs == null) || fs.isEmpty()) {
            children.clear();
            return;
        }

        // update visible toggles
        int n = 0;
        for (FoldRange f: fs) {
            TextCell c = arrangement.getCell(f.getStart());
            if(c == null) {
                // can be inside a folded range already
                continue;
            }

            Node ch;
            if (n < children.size()) {
                ch = children.get(n);
            } else {
                ch = createToggle();
                ch.addEventHandler(MouseEvent.MOUSE_CLICKED, (ev) -> {
                    handleToggle(ch, f);
                });
                children.add(ch);
            }

            // FIX does not fill height, don't know why
            RichUtils.layoutInArea(ch, 0, c.getY(), getWidth(), c.getHeight());
            n++;
        }

        // remove overflow
        for (int i = children.size() - 1; i >= n; --i) {
            children.remove(i);
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

    @Override
    public void onContentChange(ContentChange ch) {
        if (!ch.isEdit()) {
            return;
        }
        // TODO
        IO.println(ch); // FIX
        // if edit is within the collapsed region -> expand!
        // if linesAdded + end.index - start.index = 0 - return (no lines updated)
        vflow.layoutChildren();
    }

    private void handleToggle(Node n, FoldRange f) {
        boolean folded = (getCollapsedRange(f.getStart()) != null);
        if (folded) {
            // expand
            collapsed.remove(f);
        } else {
            // collapse
            if (collapsed == null) {
                collapsed = new ArrayList<>();
            }
            collapsed.add(f);
        }
        // TODO maybe own class?
        ((Pane)n).getChildren().get(0).pseudoClassStateChanged(COLLAPSED, !folded);
        vflow.invalidateLayout();
        //vflow.layoutChildren();
    }
}
