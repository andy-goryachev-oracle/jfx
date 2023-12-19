/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.demo.rich.editor;

import javafx.incubator.scene.control.rich.RichTextArea;
import javafx.incubator.scene.control.rich.TextPos;
import javafx.incubator.scene.control.rich.model.StyleAttribute;
import javafx.incubator.scene.control.rich.model.StyleAttrs;
import javafx.incubator.scene.control.rich.model.StyledTextModel;
import com.oracle.demo.rich.util.FxAction;

/**
 * This is a bit of hack.  JavaFX has no actions (yet), so here we are using FxActions from
 * https://github.com/andy-goryachev/AppFramework (with permission from the author).
 * Ideally, these actions should be created upon demand and managed by the control, because
 * control knows when the enabled state of each action changes.
 * <p>
 * This class adds a listener to the model and updates the states of all the actions.
 * (The model does not change in this application).
 */
public class Actions {
    public final FxAction undo;
    public final FxAction redo;
    public final FxAction cut;
    public final FxAction copy;
    public final FxAction paste;
    public final FxAction pasteUnformatted;
    public final FxAction selectAll;
    public final FxAction bold;
    public final FxAction italic;
    public final FxAction underline;
    public final FxAction strikeThrough;
    public final FxAction wrapText;
    private final RichTextArea control;

    public Actions(RichTextArea control) {
        this.control = control;

        undo = new FxAction(control::undo);
        redo = new FxAction(control::redo);
        cut = new FxAction(control::cut);
        copy = new FxAction(control::copy);
        paste = new FxAction(control::paste);
        pasteUnformatted = new FxAction(control::pastePlainText);
        selectAll = new FxAction(control::selectAll);
        bold = new FxAction(() -> toggle(StyleAttrs.BOLD));
        italic = new FxAction(() -> toggle(StyleAttrs.ITALIC));
        underline = new FxAction(() -> toggle(StyleAttrs.UNDERLINE));
        strikeThrough = new FxAction(() -> toggle(StyleAttrs.STRIKE_THROUGH));

        wrapText = new FxAction();
        wrapText.selectedProperty().bindBidirectional(control.wrapTextProperty());
        
        control.getModel().addChangeListener(new StyledTextModel.ChangeListener() {
            @Override
            public void eventTextUpdated(TextPos start, TextPos end, int top, int added, int bottom) {
                handleEdit();
            }

            @Override
            public void eventStyleUpdated(TextPos start, TextPos end) {
            }
        });

        control.caretPositionProperty().addListener((x) -> {
            handleCaret();
        });

        handleEdit();
        handleCaret();
    }

    private void handleEdit() {
        undo.setEnabled(control.isUndoable());
        redo.setEnabled(control.isRedoable());
    }

    private void handleCaret() {
        boolean sel = control.hasNonEmptySelection();
        StyleAttrs a = control.getActiveStyleAttrs();

        cut.setEnabled(sel);
        copy.setEnabled(sel);

        bold.setSelected(a.getBoolean(StyleAttrs.BOLD), false);
        italic.setSelected(a.getBoolean(StyleAttrs.ITALIC), false);
        underline.setSelected(a.getBoolean(StyleAttrs.UNDERLINE), false);
        strikeThrough.setSelected(a.getBoolean(StyleAttrs.STRIKE_THROUGH), false);
    }

    private void toggle(StyleAttribute<Boolean> attr) {
        TextPos start = control.getAnchorPosition();
        TextPos end = control.getCaretPosition();
        if(start == null) {
            return;
        } else if(start.equals(end)) {
            // apply to the whole paragraph
            int ix = start.index();
            start = new TextPos(ix, 0);
            end = control.getEndOfParagraph(ix);
        }

        StyleAttrs a = control.getActiveStyleAttrs();
        boolean on = !a.getBoolean(attr);
        a = StyleAttrs.builder().set(attr, on).build();
        control.applyStyle(start, end, a);
    }
    
    private <T> void apply(StyleAttribute<T> attr, T value) {
        TextPos start = control.getAnchorPosition();
        TextPos end = control.getCaretPosition();
        if(start == null) {
            return;
        } else if(start.equals(end)) {
            // apply to the whole paragraph
            int ix = start.index();
            start = new TextPos(ix, 0);
            end = control.getEndOfParagraph(ix);
        }

        StyleAttrs a = control.getActiveStyleAttrs();
        a = StyleAttrs.builder().set(attr, value).build();
        control.applyStyle(start, end, a);
    }

    // TODO need to bind selected item in the combo
    public void setFontSize(Integer size) {
        apply(StyleAttrs.FONT_SIZE, size.doubleValue());
    }
    
    // TODO need to bind selected item in the combo
    public void setFontName(String name) {
        apply(StyleAttrs.FONT_FAMILY, name);
    }
}
