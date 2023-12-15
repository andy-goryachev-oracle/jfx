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
package com.oracle.tools.demo.editor;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.incubator.scene.control.rich.RichTextArea;
import javafx.incubator.scene.control.rich.TextPos;
import javafx.incubator.scene.control.rich.model.StyleAttribute;
import javafx.incubator.scene.control.rich.model.StyleAttrs;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import com.oracle.tools.demo.rich.FX;

/**
 * Main Panel contains CodeArea, split panes for quick size adjustment, and an option pane.
 */
public class RichEditorDemoPane extends BorderPane { 
    public final RichTextArea control;

    public RichEditorDemoPane() {
        FX.name(this, "RichEditorDemoPane");

        control = new RichTextArea();
        control.setContentPadding(new Insets(10));

        setTop(createToolBar());
        setCenter(control);
        
        setCustomPopup(); // TODO
    }
    
    private ToolBar createToolBar() {
        ToolBar b = new ToolBar();
        return b;
    }
    
    protected void setCustomPopup() {
        ContextMenu m = new ContextMenu();
        m.getItems().add(new MenuItem("Dummy")); // otherwise no popup is shown
        m.addEventFilter(Menu.ON_SHOWING, (ev) -> {
            m.getItems().clear();
            populatePopupMenu(m.getItems());
        });
        control.setContextMenu(m);
    }

    protected void populatePopupMenu(ObservableList<MenuItem> items) {
        boolean sel = control.hasNonEmptySelection();
        boolean paste = true; // would be easier with Actions (findFormatForPaste() != null);

        MenuItem m;
        items.add(m = new MenuItem("Undo"));
        m.setOnAction((ev) -> control.undo());
        m.setDisable(!control.isUndoable());

        items.add(m = new MenuItem("Redo"));
        m.setOnAction((ev) -> control.redo());
        m.setDisable(!control.isRedoable());

        items.add(new SeparatorMenuItem());

        items.add(m = new MenuItem("Cut"));
        m.setOnAction((ev) -> control.cut());
        m.setDisable(!sel);

        items.add(m = new MenuItem("Copy"));
        m.setOnAction((ev) -> control.copy());
        m.setDisable(!sel);

        items.add(m = new MenuItem("Paste"));
        m.setOnAction((ev) -> control.paste());
        m.setDisable(!paste);

        items.add(m = new MenuItem("Paste and Match Style"));
        m.setOnAction((ev) -> control.pastePlainText());
        m.setDisable(!paste);

        StyleAttrs a = control.getActiveStyleAttrs();

        items.add(new SeparatorMenuItem());

        items.add(m = new MenuItem("Bold"));
        m.setOnAction((ev) -> apply(StyleAttrs.BOLD, !a.getBoolean(StyleAttrs.BOLD)));
        m.setDisable(!sel);

        items.add(m = new MenuItem("Italic"));
        m.setOnAction((ev) -> apply(StyleAttrs.ITALIC, !a.getBoolean(StyleAttrs.ITALIC)));
        m.setDisable(!sel);

        items.add(m = new MenuItem("Strike Through"));
        m.setOnAction((ev) -> apply(StyleAttrs.STRIKE_THROUGH, !a.getBoolean(StyleAttrs.STRIKE_THROUGH)));
        m.setDisable(!sel);

        items.add(m = new MenuItem("Underline"));
        m.setOnAction((ev) -> apply(StyleAttrs.UNDERLINE, !a.getBoolean(StyleAttrs.UNDERLINE)));
        m.setDisable(!sel);

        Menu m2;
        CheckMenuItem cm;
        items.add(m2 = new Menu("Text Color"));
        colorMenu(m2, sel, Color.GREEN);
        colorMenu(m2, sel, Color.RED);
        colorMenu(m2, sel, Color.BLUE);
        colorMenu(m2, sel, null);

        items.add(m2 = new Menu("Text Size"));
        sizeMenu(m2, sel, 72);
        sizeMenu(m2, sel, 48);
        sizeMenu(m2, sel, 36);
        sizeMenu(m2, sel, 24);
        sizeMenu(m2, sel, 20);
        sizeMenu(m2, sel, 18);
        sizeMenu(m2, sel, 16);
        sizeMenu(m2, sel, 14);
        sizeMenu(m2, sel, 12);
        sizeMenu(m2, sel, 11);
        sizeMenu(m2, sel, 10);
        sizeMenu(m2, sel, 9);
        sizeMenu(m2, sel, 8);
        sizeMenu(m2, sel, 7);
        sizeMenu(m2, sel, 6);

        items.add(m2 = new Menu("Font Family"));
        fontMenu(m2, sel, "System");
        fontMenu(m2, sel, "Serif");
        fontMenu(m2, sel, "Sans-serif");
        fontMenu(m2, sel, "Cursive");
        fontMenu(m2, sel, "Fantasy");
        fontMenu(m2, sel, "Monospaced");
        m2.getItems().add(new SeparatorMenuItem());
        fontMenu(m2, sel, "Arial");
        fontMenu(m2, sel, "Courier New");
        fontMenu(m2, sel, "Times New Roman");
        fontMenu(m2, sel, "null");
        
        items.add(cm = new CheckMenuItem("Toggle Unsupported Attribute"));
        cm.setSelected(a.getBoolean(StyleAttrs.RIGHT_TO_LEFT));
        cm.setOnAction((ev) -> applyStyle(StyleAttrs.RIGHT_TO_LEFT, !a.getBoolean(StyleAttrs.RIGHT_TO_LEFT)));

        items.add(new SeparatorMenuItem());

        items.add(m = new MenuItem("Select All"));
        m.setOnAction((ev) -> control.selectAll());
    }

    protected void fontMenu(Menu menu, boolean selected, String family) {
        MenuItem m = new MenuItem(family);
        m.setDisable(!selected);
        m.setOnAction((ev) -> apply(StyleAttrs.FONT_FAMILY, family));
        menu.getItems().add(m);
    }

    protected void sizeMenu(Menu menu, boolean selected, double size) {
        MenuItem m = new MenuItem(String.valueOf(size));
        m.setDisable(!selected);
        m.setOnAction((ev) -> apply(StyleAttrs.FONT_SIZE, size));
        menu.getItems().add(m);
    }
    
    protected void colorMenu(Menu menu, boolean selected, Color color) {
        int w = 16;
        int h = 16;
        Canvas c = new Canvas(w, h);
        GraphicsContext g = c.getGraphicsContext2D();
        if(color != null) {
            g.setFill(color);
            g.fillRect(0, 0, w, h);
        }
        g.setStroke(Color.DARKGRAY);
        g.strokeRect(0, 0, w, h);
        
        MenuItem m = new MenuItem(null, c);
        m.setDisable(!selected);
        m.setOnAction((ev) -> apply(StyleAttrs.TEXT_COLOR, color));
        menu.getItems().add(m);
    }

    protected <X> void apply(StyleAttribute<X> attr, X val) {
        TextPos ca = control.getCaretPosition();
        TextPos an = control.getAnchorPosition();
        StyleAttrs a = StyleAttrs.
            builder().
            set(attr, val).
            build();
        control.applyStyle(ca, an, a);
    }

    private <T> void applyStyle(StyleAttribute<T> a, T val) {
        TextPos ca = control.getCaretPosition();
        TextPos an = control.getAnchorPosition();
        StyleAttrs m = StyleAttrs.of(a, val);
        control.applyStyle(ca, an, m);
    }
}
