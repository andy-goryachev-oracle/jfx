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
package com.oracle.demo.rich.codearea;

import java.nio.charset.Charset;
import java.util.Base64;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.incubator.scene.control.rich.CodeArea;
import javafx.incubator.scene.control.rich.CodeTextModel;
import javafx.incubator.scene.control.rich.TextPos;
import javafx.incubator.scene.control.rich.model.StyleAttribute;
import javafx.incubator.scene.control.rich.model.StyleAttrs;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.util.StringConverter;
import com.oracle.demo.rich.rta.FontSelector;
import com.oracle.demo.rich.rta.ROptionPane;
import com.oracle.demo.rich.util.FX;

/**
 * Main Panel contains CodeArea, split panes for quick size adjustment, and an option pane.
 */
public class CodeAreaDemoPane extends BorderPane {
    public final ROptionPane op;
    public final CodeArea control;

    public CodeAreaDemoPane(CodeTextModel m) {
        FX.name(this, "CodeAreaDemoPane");
        control = new CodeArea(m);

        SplitPane hsplit = new SplitPane(control, pane());
        FX.name(hsplit, "hsplit");
        hsplit.setBorder(null);
        hsplit.setDividerPositions(0.9);
        hsplit.setOrientation(Orientation.HORIZONTAL);

        SplitPane vsplit = new SplitPane(hsplit, pane());
        FX.name(vsplit, "vsplit");
        vsplit.setBorder(null);
        vsplit.setDividerPositions(0.9);
        vsplit.setOrientation(Orientation.VERTICAL);

        FontSelector fontSelector = new FontSelector("font", control::setFont);

        CheckBox editable = new CheckBox("editable");
        FX.name(editable, "editable");
        editable.selectedProperty().bindBidirectional(control.editableProperty());

        CheckBox wrapText = new CheckBox("wrap text");
        FX.name(wrapText, "wrapText");
        wrapText.selectedProperty().bindBidirectional(control.wrapTextProperty());

        CheckBox displayCaret = new CheckBox("display caret");
        FX.name(displayCaret, "displayCaret");
        displayCaret.selectedProperty().bindBidirectional(control.displayCaretProperty());

        CheckBox fatCaret = new CheckBox("fat caret");
        FX.name(fatCaret, "fatCaret");
        fatCaret.selectedProperty().addListener((s, p, on) -> {
            Node n = control.lookup(".caret");
            if (n != null) {
                if (on) {
                    n.setStyle(
                        "-fx-stroke-width:2; -fx-stroke:red; -fx-effect:dropshadow(gaussian,rgba(0,0,0,.5),5,0,1,1);");
                } else {
                    n.setStyle(null);
                }
            }
        });

        CheckBox highlightCurrentLine = new CheckBox("highlight current line");
        FX.name(highlightCurrentLine, "highlightCurrentLine");
        highlightCurrentLine.selectedProperty().bindBidirectional(control.highlightCurrentParagraphProperty());

        ComboBox<Integer> tabSize = new ComboBox<>();
        FX.name(tabSize, "tabSize");
        tabSize.getItems().setAll(1, 2, 3, 4, 8, 16);
        tabSize.getSelectionModel().selectedItemProperty().addListener((s, p, v) -> {
            control.setTabSize(v);
        });

        CheckBox customPopup = new CheckBox("custom popup menu");
        FX.name(customPopup, "customPopup");
        customPopup.selectedProperty().addListener((s, p, v) -> {
            setCustomPopup(v);
        });

        ComboBox<Insets> contentPadding = new ComboBox<>();
        FX.name(contentPadding, "contentPadding");
        contentPadding.setConverter(new StringConverter<Insets>() {
            @Override
            public String toString(Insets x) {
                if (x == null) {
                    return "null";
                }
                return String.format(
                    "T%d, B%d, L%d, R%d",
                    (int)x.getTop(),
                    (int)x.getBottom(),
                    (int)x.getLeft(),
                    (int)x.getRight()
                );
            }

            @Override
            public Insets fromString(String s) {
                return null;
            }
        });
        contentPadding.getItems().setAll(
            null,
            new Insets(1),
            new Insets(2),
            new Insets(10),
            new Insets(22.22),
            new Insets(50),
            new Insets(100),
            new Insets(5, 10, 15, 20)
        );
        contentPadding.getSelectionModel().selectedItemProperty().addListener((s, p, v) -> {
            control.setContentPadding(v);
        });

        ComboBox<Double> lineSpacing = new ComboBox<>();
        FX.name(lineSpacing, "lineSpacing");
        lineSpacing.getItems().setAll(
            0.0,
            5.0,
            31.415
        );
        lineSpacing.getSelectionModel().selectedItemProperty().addListener((s, p, v) -> {
            setLineSpacing(v);
        });

        CheckBox lineNumbers = new CheckBox("line numbers");
        FX.name(lineNumbers, "lineNumbers");
        lineNumbers.selectedProperty().bindBidirectional(control.lineNumbersEnabledProperty());

        CheckBox syntax = new CheckBox("syntax highlighter");
        FX.name(syntax, "syntax");
        syntax.selectedProperty().addListener((x) -> {
            boolean on = syntax.isSelected();
            control.setSyntaxDecorator(on ? new DemoSyntaxDecorator() : null);
        });

        op = new ROptionPane();
        op.option(editable);
        op.label("Font:");
        op.option(fontSelector.fontNode());
        op.label("Size:");
        op.option(fontSelector.sizeNode());
        op.option(wrapText);
        op.option(displayCaret);
        op.option(fatCaret);
        op.option(highlightCurrentLine);
        op.option(lineNumbers);
        op.label("Tab Size:");
        op.option(tabSize);
        op.option(customPopup);
        op.label("Content Padding:");
        op.option(contentPadding);
        op.label("Line Spacing:");
        op.option(lineSpacing);
        op.option(syntax);

        setCenter(vsplit);
        setRight(op);

        contentPadding.getSelectionModel().selectFirst();
        lineSpacing.getSelectionModel().selectFirst();
    }

    protected static Pane pane() {
        Pane p = new Pane();
        SplitPane.setResizableWithParent(p, false);
        p.setStyle("-fx-background-color:#dddddd;");
        return p;
    }

    public Button addButton(String name, Runnable action) {
        Button b = new Button(name);
        b.setOnAction((ev) -> {
            action.run();
        });

        toolbar().add(b);
        return b;
    }

    public TBar toolbar() {
        if (getTop() instanceof TBar) {
            return (TBar)getTop();
        }

        TBar t = new TBar();
        setTop(t);
        return t;
    }

    public Window getWindow() {
        Scene s = getScene();
        if (s != null) {
            return s.getWindow();
        }
        return null;
    }

    public void setOptions(Node n) {
        setRight(n);
    }

    protected String generateStylesheet(boolean fat) {
        String s = ".rich-text-area .caret { -fx-stroke-width:" + (fat ? 2 : 1) + "; }";
        return "data:text/css;base64," + Base64.getEncoder().encodeToString(s.getBytes(Charset.forName("utf-8")));
    }

    protected void setCustomPopup(boolean on) {
        if (on) {
            ContextMenu m = new ContextMenu();
            m.getItems().add(new MenuItem("Dummy")); // otherwise no popup is shown
            m.addEventFilter(Menu.ON_SHOWING, (ev) -> {
                m.getItems().clear();
                populatePopupMenu(m.getItems());
            });
            control.setContextMenu(m);
        } else {
            control.setContextMenu(null);
        }
    }

    protected void populatePopupMenu(ObservableList<MenuItem> items) {
        boolean sel = control.hasNonEmptySelection();
        boolean paste = true; // would be easier with Actions (findFormatForPaste() != null);
        boolean styled = true; //(control.getModel() instanceof EditableRichTextModel);

        items.add(new MenuItem("★ Custom Context Menu"));

        items.add(new SeparatorMenuItem());

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

        if (styled) {
            StyleAttrs a = control.getActiveStyleAttrs();
            System.err.println(a); // FIX

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
            sizeMenu(m2, sel, 300);
            sizeMenu(m2, sel, 200);
            sizeMenu(m2, sel, 150);
            sizeMenu(m2, sel, 125);
            sizeMenu(m2, sel, 100);
            sizeMenu(m2, sel, 75);
            sizeMenu(m2, sel, 60);
            sizeMenu(m2, sel, 50);

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
        }

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
        if (color != null) {
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
        StyleAttrs a = StyleAttrs.builder().set(attr, val).build();
        control.applyStyle(ca, an, a);
    }

    protected void setLineSpacing(double x) {
        control.setDefaultAttribute(StyleAttrs.LINE_SPACING, x);
    }

    private <T> void applyStyle(StyleAttribute<T> a, T val) {
        TextPos ca = control.getCaretPosition();
        TextPos an = control.getAnchorPosition();
        StyleAttrs m = StyleAttrs.of(a, val);
        control.applyStyle(ca, an, m);
    }

    //

    public static class TBar extends HBox {
        public TBar() {
            setFillHeight(true);
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(2);
        }

        public <T extends Node> T add(T n) {
            getChildren().add(n);
            return n;
        }

        public void addAll(Node... nodes) {
            for (Node n : nodes) {
                add(n);
            }
        }
    }
}
