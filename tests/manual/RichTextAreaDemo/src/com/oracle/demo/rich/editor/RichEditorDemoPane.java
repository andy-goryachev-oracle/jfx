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

import javafx.geometry.Insets;
import javafx.incubator.scene.control.rich.RichTextArea;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import com.oracle.demo.rich.util.FX;

/**
 * Main Panel contains CodeArea, split panes for quick size adjustment, and an option pane.
 */
public class RichEditorDemoPane extends BorderPane {
    public final RichTextArea control;
    public final Actions actions;

    public RichEditorDemoPane() {
        FX.name(this, "RichEditorDemoPane");

        control = new RichTextArea();
        control.setContentPadding(new Insets(10));
        
        actions = new Actions(control);
        control.setContextMenu(createContextMenu());
        
        setTop(createToolBar());
        setCenter(control);
    }
    
    private ToolBar createToolBar() {
        ToolBar b = new ToolBar();
        FX.button(b, "B", "Bold Text", actions.bold);
        FX.button(b, "I", "Italicize Text", actions.italic);
        FX.button(b, "S", "Strike Through Text", actions.strikeThrough);
        FX.button(b, "U", "Underline Text", actions.underline);
        FX.space(b);
        FX.button(b, "W", "Wrap Text", actions.wrapText);
        return b;
    }

    private ContextMenu createContextMenu() {
        ContextMenu m = new ContextMenu();
        FX.item(m, "Undo", actions.undo);
        FX.item(m, "Redo", actions.redo);
        FX.separator(m);
        FX.item(m, "Cut", actions.cut);
        FX.item(m, "Copy", actions.copy);
        FX.item(m, "Paste", actions.paste);
        FX.item(m, "Paste and Retain Style", actions.pasteUnformatted);
        FX.separator(m);
        FX.item(m, "Select All", actions.selectAll);
        FX.separator(m);
        // TODO under "Style" submenu
        FX.item(m, "Bold", actions.bold);
        FX.item(m, "Italic", actions.italic);
        FX.item(m, "Strike Through", actions.strikeThrough);
        FX.item(m, "Underline", actions.underline);
        return m;
    }

//    protected void fontMenu(Menu menu, boolean selected, String family) {
//        MenuItem m = new MenuItem(family);
//        m.setDisable(!selected);
//        m.setOnAction((ev) -> apply(StyleAttrs.FONT_FAMILY, family));
//        menu.getItems().add(m);
//    }
//
//    protected void sizeMenu(Menu menu, boolean selected, double size) {
//        MenuItem m = new MenuItem(String.valueOf(size));
//        m.setDisable(!selected);
//        m.setOnAction((ev) -> apply(StyleAttrs.FONT_SIZE, size));
//        menu.getItems().add(m);
//    }
//    
//    protected void colorMenu(Menu menu, boolean selected, Color color) {
//        int w = 16;
//        int h = 16;
//        Canvas c = new Canvas(w, h);
//        GraphicsContext g = c.getGraphicsContext2D();
//        if(color != null) {
//            g.setFill(color);
//            g.fillRect(0, 0, w, h);
//        }
//        g.setStroke(Color.DARKGRAY);
//        g.strokeRect(0, 0, w, h);
//        
//        MenuItem m = new MenuItem(null, c);
//        m.setDisable(!selected);
//        m.setOnAction((ev) -> apply(StyleAttrs.TEXT_COLOR, color));
//        menu.getItems().add(m);
//    }
//
//    protected <X> void apply(StyleAttribute<X> attr, X val) {
//        TextPos ca = control.getCaretPosition();
//        TextPos an = control.getAnchorPosition();
//        StyleAttrs a = StyleAttrs.
//            builder().
//            set(attr, val).
//            build();
//        control.applyStyle(ca, an, a);
//    }
//
//    private <T> void applyStyle(StyleAttribute<T> a, T val) {
//        TextPos ca = control.getCaretPosition();
//        TextPos an = control.getAnchorPosition();
//        StyleAttrs m = StyleAttrs.of(a, val);
//        control.applyStyle(ca, an, m);
//    }
}
