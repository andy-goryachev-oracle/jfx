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
package com.oracle.controls.codearea;

import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.incubator.scene.control.rich.RichTextArea;
import javafx.incubator.scene.control.rich.skin.LineNumberDecorator;
import javafx.scene.text.Font;

/**
 * CodeArea is a text component which supports styling (a.k.a. "syntax highlighting") of monospaced text.
 */
public class CodeArea extends RichTextArea {
    private BooleanProperty lineNumbers;
    private String fontStyle;

    public CodeArea(CodeModel m) {
        super(m);
        modelProperty().addListener((s, prev, newValue) -> {
            // TODO perhaps even block any change of (already set CodeModel)
            if (newValue != null) {
                if (!(newValue instanceof CodeModel)) {
                    setModel(prev);
                    throw new IllegalArgumentException("model must be of type " + CodeModel.class);
                }
            }
        });
        // set default font
        Font f = Font.getDefault();
        setFont(Font.font("monospace", f.getSize()));
    }

    public CodeArea() {
        this(new CodeModel());
    }

//    private static class StyleableProperties {
//        private static final FontCssMetaData<CodeArea> FONT = new FontCssMetaData<>("-fx-font", Font.getDefault()) {
//
//            @Override
//            public boolean isSettable(CodeArea n) {
//                return n.font == null || !n.font.isBound();
//            }
//
//            @Override
//            public StyleableProperty<Font> getStyleableProperty(CodeArea n) {
//                return (StyleableProperty<Font>)(WritableValue<Font>)n.fontProperty();
//            }
//        };
//
//        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES =
//            Util.initStyleables(RichTextArea.getClassCssMetaData(), FONT);
//    }

    /**
     * Gets the {@code CssMetaData} associated with this class, which may include the
     * {@code CssMetaData} of its superclasses.
     * @return the {@code CssMetaData}
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        //return StyleableProperties.STYLEABLES;
        return RichTextArea.getClassCssMetaData();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    private CodeModel codeModel() {
        return (CodeModel)getModel();
    }

    // TODO another school of thought suggests to move the highlighter property here.
    public void setSyntaxHighlighter(SyntaxDecorator d) {
        var m = codeModel();
        if (m != null) {
            m.setDecorator(d);
        }
    }

    public SyntaxDecorator getSyntaxDecorator() {
        var m = codeModel();
        return (m == null) ? null : m.getDecorator();
    }

    public BooleanProperty lineNumbersEnabledProperty() {
        if (lineNumbers == null) {
            lineNumbers = new SimpleBooleanProperty() {
                @Override
                protected void invalidated() {
                    LineNumberDecorator d;
                    if(get()) {
                        d = new LineNumberDecorator() {
                            @Override
                            public Node getNode(int ix, boolean forMeasurement) {
                                Node n = super.getNode(ix, forMeasurement);
                                if(n instanceof Labeled t) {
                                    t.fontProperty().bind(fontProperty());
                                }
                                return n;
                            }
                        };
                    } else {
                        d = null;
                    }
                    setLeftDecorator(d);
                }
            };
        }
        return lineNumbers;
    }

    public boolean isLineNumbersEnabled() {
        return lineNumbers == null ? false : lineNumbers.get();
    }

    public void setLineNumbersEnabled(boolean on) {
        lineNumbersEnabledProperty().set(on);
    }
}
