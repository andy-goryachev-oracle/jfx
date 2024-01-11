/*
 * Copyright (c) 2023, 2024, Oracle and/or its affiliates. All rights reserved.
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
package javafx.incubator.scene.control.rich.code;

import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.incubator.scene.control.rich.RichTextArea;
import javafx.incubator.scene.control.rich.StyleHandlerRegistry;
import javafx.incubator.scene.control.rich.TextPos;
import javafx.incubator.scene.control.rich.model.StyleAttribute;
import javafx.incubator.scene.control.rich.model.StyledTextModel;
import javafx.incubator.scene.control.rich.skin.LineNumberDecorator;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.text.Font;
import com.sun.javafx.incubator.scene.control.rich.util.RichUtils;

/**
 * CodeArea is a text component which supports styling (a.k.a. "syntax highlighting") of monospaced text.
 */
// TODO lineSpacing property
public class CodeArea extends RichTextArea {
    static final StyleAttribute<Font> FONT = new StyleAttribute<>("CodeArea.FONT", Font.class, false);
    static final StyleAttribute<Integer> TAB_SIZE = new StyleAttribute<>("CodeArea.TAB_SIZE", Integer.class, true);
    private static final int DEFAULT_TAB_SIZE = 8;
    private BooleanProperty lineNumbers;
    private StyleableIntegerProperty tabSize;
    private StyleableObjectProperty<Font> font;
    private String fontStyle;
    protected static final StyleHandlerRegistry styleHandlerRegistry = initStyleHandlerRegistry();

    public CodeArea(CodeTextModel m) {
        super(m);
        modelProperty().addListener((s, prev, newValue) -> {
            // TODO is there a better way?
            // perhaps even block any change of (already set CodeModel)
            if (newValue != null) {
                if (!(newValue instanceof CodeTextModel)) {
                    setModel(prev);
                    throw new IllegalArgumentException("model must be of type " + CodeTextModel.class);
                }
            }
        });
        // set default font
        Font f = Font.getDefault();
        setFont(Font.font("monospace", f.getSize()));
    }

    public CodeArea() {
        this(new CodeTextModel());
    }

    private CodeTextModel codeModel() {
        return (CodeTextModel)getModel();
    }

    private void updateFont(Font f) {
        setDefaultAttribute(FONT, f);
    }

    private void updateTabSize(int sz) {
        setDefaultAttribute(TAB_SIZE, sz);
    }

    /**
     * This convenience method sets the decorator property in the model.
     *
     * @param d the syntax decorator
     * @see CodeTextModel#setDecorator(SyntaxDecorator)
     */
    public void setSyntaxDecorator(SyntaxDecorator d) {
        CodeTextModel m = codeModel();
        if (m != null) {
            m.setDecorator(d);
        }
    }

    /**
     * This convenience method returns the syntax decorator value in the model,
     * or null if the said model is null.
     * @return the syntax devocrator value, or null
     */
    public SyntaxDecorator getSyntaxDecorator() {
        CodeTextModel m = codeModel();
        return (m == null) ? null : m.getDecorator();
    }

    /**
     * Determines whether to show line numbers.
     * @return the line numbers enabled property
     */
    // TODO should there be a way to customize the line number component? createLineNumberDecorator() ?
    // TODO should this be a styleable property?
    public final BooleanProperty lineNumbersEnabledProperty() {
        if (lineNumbers == null) {
            lineNumbers = new SimpleBooleanProperty() {
                @Override
                protected void invalidated() {
                    LineNumberDecorator d;
                    if (get()) {
                        // TODO create line number decorator method?
                        d = new LineNumberDecorator() {
                            @Override
                            public Node getNode(int ix, boolean forMeasurement) {
                                Node n = super.getNode(ix, forMeasurement);
                                if (n instanceof Labeled t) {
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

    public final boolean isLineNumbersEnabled() {
        return lineNumbers == null ? false : lineNumbers.get();
    }

    public final void setLineNumbersEnabled(boolean on) {
        lineNumbersEnabledProperty().set(on);
    }

    /**
     * The size of a tab stop in spaces.
     * Values less than 1 are treated as 1.
     * @defaultValue 8
     */
    public final IntegerProperty tabSizeProperty() {
        if (tabSize == null) {
            tabSize = new StyleableIntegerProperty(DEFAULT_TAB_SIZE) {
                @Override
                public Object getBean() {
                    return CodeArea.this;
                }

                @Override
                public String getName() {
                    return "tabSize";
                }

                @Override
                public CssMetaData getCssMetaData() {
                    return StyleableProperties.TAB_SIZE;
                }

                @Override
                protected void invalidated() {
                    updateTabSize(get());
                    requestLayout();
                }
            };
        }
        return tabSize;
    }

    public final int getTabSize() {
        return tabSize == null ? DEFAULT_TAB_SIZE : tabSize.get();
    }

    public final void setTabSize(int spaces) {
        tabSizeProperty().set(spaces);
    }

    /**
     * The default font to use for text in the {@code CodeArea}.
     * @return the font property
     * @defaultValue the value supplied by {@link Font#getDefault()}
     */
    public final ObjectProperty<Font> fontProperty() {
        if (font == null) {
            font = new StyleableObjectProperty<Font>(Font.getDefault())
            {
                private boolean fontSetByCss;

                @Override
                public void applyStyle(StyleOrigin newOrigin, Font value) {
                    // TODO perhaps this is not needed
                    // RT-20727 JDK-8127428
                    // if CSS is setting the font, then make sure invalidate doesn't call NodeHelper.reapplyCSS
                    try {
                        // super.applyStyle calls set which might throw if value is bound.
                        // Have to make sure fontSetByCss is reset.
                        fontSetByCss = true;
                        super.applyStyle(newOrigin, value);
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        fontSetByCss = false;
                    }
                }

                @Override
                public void set(Font value) {
                    Font old = get();
                    if (value == null ? old == null : value.equals(old)) {
                        return;
                    }
                    super.set(value);
                }

                @Override
                protected void invalidated() {
                    updateFont(get());
                    /** FIX reapplyCSS should be public
                    // RT-20727 JDK-8127428
                    // if font is changed by calling setFont, then
                    // css might need to be reapplied since font size affects
                    // calculated values for styles with relative values
                    if (fontSetByCss == false) {
                        NodeHelper.reapplyCSS(RichTextArea.this);
                    }
                    */
                    // don't know whether this is ok
                    requestLayout();
                }

                @Override
                public CssMetaData<CodeArea, Font> getCssMetaData() {
                    return StyleableProperties.FONT;
                }

                @Override
                public Object getBean() {
                    return CodeArea.this;
                }

                @Override
                public String getName() {
                    return "font";
                }
            };
        }
        return font;
    }

    public final void setFont(Font value) {
        fontProperty().setValue(value);
    }

    public final Font getFont() {
        return font == null ? Font.getDefault() : font.getValue();
    }

    // TODO lazy initialization is not necessary
    private static class StyleableProperties {
        private static final FontCssMetaData<CodeArea> FONT =
            new FontCssMetaData<>("-fx-font", Font.getDefault())
        {
            @Override
            public boolean isSettable(CodeArea n) {
                return n.font == null || !n.font.isBound();
            }

            @Override
            public StyleableProperty<Font> getStyleableProperty(CodeArea n) {
                return (StyleableProperty<Font>)(WritableValue<Font>)n.fontProperty();
            }
        };

        private static final CssMetaData<CodeArea, Number> TAB_SIZE =
            new CssMetaData<>("-fx-tab-size", SizeConverter.getInstance(), DEFAULT_TAB_SIZE)
        {
            @Override
            public boolean isSettable(CodeArea n) {
                return n.tabSize == null || !n.tabSize.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(CodeArea n) {
                return (StyleableProperty<Number>)n.tabSizeProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = RichUtils.combine(
            RichTextArea.getClassCssMetaData(),
            FONT,
            TAB_SIZE
        );
    }

    /**
     * Gets the {@code CssMetaData} associated with this class, which may include the
     * {@code CssMetaData} of its superclasses.
     * @return the {@code CssMetaData}
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
    
    @Override
    public StyleHandlerRegistry getStyleHandlerRegistry() {
        return styleHandlerRegistry;
    }

    private static StyleHandlerRegistry initStyleHandlerRegistry() {
        StyleHandlerRegistry.Builder b = StyleHandlerRegistry.builder(RichTextArea.styleHandlerRegistry);

        // this paragraph attribute affects each segment
        b.setSegHandler(CodeArea.FONT, (c, cx, v) -> {
            String family = v.getFamily();
            double size = v.getSize();
            cx.addStyle("-fx-font-family:'" + family + "';");
            cx.addStyle("-fx-font-size:" + size + ";");
        });

        b.setParHandler(CodeArea.TAB_SIZE, (c, cx, v) -> {
            if (v > 0) {
                cx.addStyle("-fx-tab-size:" + v + ";");
            }
        });

        return b.build();
    }

    /**
     * Returns plain text.
     * @return plain text
     */
    public String getText() {
        // TODO or use save(DataFormat, Writer) ?
        StyledTextModel m = getModel();
        StringBuilder sb = new StringBuilder(4096);
        int sz = m.size();
        for(int i=0; i<sz; i++) {
            String s = m.getPlainText(i);
            sb.append(s);
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Replaces text in this CodeArea.  The caret gets reset to the start of the document.
     * @param text the text string
     */
    public void setText(String text) {
        TextPos end = getEndTextPos();
        getModel().replace(null, TextPos.ZERO, end, text, true);
    }
}
