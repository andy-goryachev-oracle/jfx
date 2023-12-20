/*
 * Copyright (c) 2022, 2023, Oracle and/or its affiliates. All rights reserved.
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
// This code borrows heavily from the following project, with permission from the author:
// https://github.com/andy-goryachev/FxEditor

package javafx.incubator.scene.control.rich;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.InsetsConverter;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.incubator.scene.control.behavior.FunctionTag;
import javafx.incubator.scene.control.behavior.InputMap;
import javafx.incubator.scene.control.rich.model.EditableRichTextModel;
import javafx.incubator.scene.control.rich.model.StyleAttribute;
import javafx.incubator.scene.control.rich.model.StyleAttrs;
import javafx.incubator.scene.control.rich.model.StyledInput;
import javafx.incubator.scene.control.rich.model.StyledTextModel;
import javafx.incubator.scene.control.rich.skin.RichTextAreaSkin;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.input.DataFormat;
import javafx.scene.text.Font;
import javafx.util.Duration;
import com.sun.javafx.incubator.scene.control.rich.CssStyles;
import com.sun.javafx.incubator.scene.control.rich.Params;
import com.sun.javafx.incubator.scene.control.rich.RichTextAreaSkinHelper;
import com.sun.javafx.incubator.scene.control.rich.RichUtils;
import com.sun.javafx.incubator.scene.control.rich.VFlow;

/**
 * Text input component that allows a user to enter multiple lines of rich text.
 */
public class RichTextArea extends Control {
    /** Deletes the previous symbol */
    public static final FunctionTag BACKSPACE = new FunctionTag();
    /** Copies selected text to the clipboard */
    public static final FunctionTag COPY = new FunctionTag();
    /** Cuts selected text and places it to the clipboard */
    public static final FunctionTag CUT = new FunctionTag();
    /** Deletes symbol at the caret */
    public static final FunctionTag DELETE = new FunctionTag();
    /** Deletes paragraph at the caret, or selected paragraphs */
    public static final FunctionTag DELETE_PARAGRAPH = new FunctionTag();
    /** Inserts a single line break */
    public static final FunctionTag INSERT_LINE_BREAK = new FunctionTag();
    /** Inserts a TAB symbol */
    public static final FunctionTag INSERT_TAB = new FunctionTag();
    /** Moves the caret to end of the document */
    public static final FunctionTag MOVE_DOCUMENT_END = new FunctionTag();
    /** Moves the caret to beginning of the document */
    public static final FunctionTag MOVE_DOCUMENT_START = new FunctionTag();
    /** Moves the caret one visual text line down */
    public static final FunctionTag MOVE_DOWN = new FunctionTag();
    /** Moves the caret one symbol to the left */
    public static final FunctionTag MOVE_LEFT = new FunctionTag();
    /** Moves the caret to the end of the current paragraph */
    public static final FunctionTag MOVE_PARAGRAPH_END = new FunctionTag();
    /** Moves the caret to the beginning of the current paragraph */
    public static final FunctionTag MOVE_PARAGRAPH_START = new FunctionTag();
    /** Moves the caret one symbol to the right */
    public static final FunctionTag MOVE_RIGHT = new FunctionTag();
    /** Moves the caret one visual text line up */
    public static final FunctionTag MOVE_UP = new FunctionTag();
    /** Moves the caret one word left (previous word if LTR, next word if RTL) */
    public static final FunctionTag MOVE_WORD_LEFT = new FunctionTag();
    /** Moves the caret to the next word */
    public static final FunctionTag MOVE_WORD_NEXT = new FunctionTag();
    /** Moves the caret to the end of next word */
    public static final FunctionTag MOVE_WORD_NEXT_END = new FunctionTag();
    /** Moves the caret to the previous word */
    public static final FunctionTag MOVE_WORD_PREVIOUS = new FunctionTag();
    /** Moves the caret one word right (next word if LTR, previous word if RTL) */
    public static final FunctionTag MOVE_WORD_RIGHT = new FunctionTag();
    /** Moves the caret one page down */
    public static final FunctionTag PAGE_DOWN = new FunctionTag();
    /** Moves the caret one page up */
    public static final FunctionTag PAGE_UP = new FunctionTag();
    /** Inserts rich text from the clipboard */
    public static final FunctionTag PASTE = new FunctionTag();
    /** Inserts plain text from the clipboard */
    public static final FunctionTag PASTE_PLAIN_TEXT = new FunctionTag();
    /** Reverts the last undo operation */
    public static final FunctionTag REDO = new FunctionTag();
    /** Selects all text in the document */
    public static final FunctionTag SELECT_ALL = new FunctionTag();
    /** Selects text (or extends selection) from the current caret position to the end of document */
    public static final FunctionTag SELECT_DOCUMENT_END = new FunctionTag();
    /** Selects text (or extends selection) from the current caret position to the start of document */
    public static final FunctionTag SELECT_DOCUMENT_START = new FunctionTag();
    /** Selects text (or extends selection) from the current caret position one visual text line down */
    public static final FunctionTag SELECT_DOWN = new FunctionTag();
    /** Selects text (or extends selection) from the current position to one symbol to the left */
    public static final FunctionTag SELECT_LEFT = new FunctionTag();
    /** Selects text (or extends selection) from the current position to one page down */
    public static final FunctionTag SELECT_PAGE_DOWN = new FunctionTag();
    /** Selects text (or extends selection) from the current position to one page up */
    public static final FunctionTag SELECT_PAGE_UP = new FunctionTag();
    /** Selects text (or extends selection) of the current paragraph */
    public static final FunctionTag SELECT_PARAGRAPH = new FunctionTag();
    /** Selects text (or extends selection) from the current position to one symbol to the right */
    public static final FunctionTag SELECT_RIGHT = new FunctionTag();
    /** Selects text (or extends selection) from the current caret position one visual text line up */
    public static final FunctionTag SELECT_UP = new FunctionTag();
    /** Selects word at the caret position */
    public static final FunctionTag SELECT_WORD = new FunctionTag();
    /** Extends selection to the previous word (LTR) or next word (RTL) */
    public static final FunctionTag SELECT_WORD_LEFT = new FunctionTag();
    /** Extends selection to the next word */
    public static final FunctionTag SELECT_WORD_NEXT = new FunctionTag();
    /** Extends selection to the end of next word */
    public static final FunctionTag SELECT_WORD_NEXT_END = new FunctionTag();
    /** Extends selection to the previous word */
    public static final FunctionTag SELECT_WORD_PREVIOUS = new FunctionTag();
    /** Extends selection to the next word (LTR) or previous word (RTL) */
    public static final FunctionTag SELECT_WORD_RIGHT = new FunctionTag();
    /** Undoes the last edit operation */
    public static final FunctionTag UNDO = new FunctionTag();

    private final ConfigurationParameters config;
    private final ObjectProperty<StyledTextModel> model = new SimpleObjectProperty<>(this, "model");
    private final SimpleBooleanProperty displayCaretProperty = new SimpleBooleanProperty(this, "displayCaret", true);
    private final SimpleObjectProperty<StyleAttrs> defaultAttributes;
    private final SimpleObjectProperty<StyleAttrs> defaultTextCellAttributes;
    private SimpleBooleanProperty editableProperty;
    private StyleableObjectProperty<Font> font;
    private final ReadOnlyObjectWrapper<Duration> caretBlinkPeriod;
    private final SelectionModel selectionModel = new SingleSelectionModel();
    private ReadOnlyIntegerWrapper tabSizeProperty;
    private ObjectProperty<SideDecorator> leftDecorator;
    private ObjectProperty<SideDecorator> rightDecorator;
    private ObjectProperty<Insets> contentPadding;
    private BooleanProperty highlightCurrentParagraph;
    private BooleanProperty useContentWidth;
    private BooleanProperty useContentHeight;
    private static HashMap<StyleAttribute,StyleAttributeHandler> parStyleHandlerMap = new HashMap<>();
    private static HashMap<StyleAttribute,StyleAttributeHandler> segStyleHandlerMap = new HashMap<>();
    static { initStyleHandlers(); }

    /**
     * Creates an editable instance with default configuration parameters,
     * using an in-memory model {@link EditableRichTextModel}.
     */
    public RichTextArea() {
        this(new EditableRichTextModel());
    }

    /**
     * Creates an instance with default configuration parameters, using the specified model.
     * @param model styled text model
     */
    public RichTextArea(StyledTextModel model) {
        this(ConfigurationParameters.defaultConfig(), model);
    }

    /**
     * Creates an instance with the specified configuration parameters and model.
     * @param c configuration parameters
     * @param m styled text model
     */
    public RichTextArea(ConfigurationParameters c, StyledTextModel m) {
        this.config = c;
        
        caretBlinkPeriod = new ReadOnlyObjectWrapper<>(this, "caretBlinkPeriod", Duration.millis(Params.DEFAULT_CARET_BLINK_PERIOD));

        defaultTextCellAttributes = new SimpleObjectProperty<>(this, "defaultTextCellAttributes");

        defaultAttributes = new SimpleObjectProperty<>(this, "defaultParagraphAttributes");

        setFocusTraversable(true);
        getStyleClass().add("rich-text-area");
        setAccessibleRole(AccessibleRole.TEXT_AREA);

        if (m != null) {
            setModel(m);
        }
    }

    @Override
    protected RichTextAreaSkin createDefaultSkin() {
        return new RichTextAreaSkin(this, config);
    }

    /**
     * Determines the {@link StyledTextModel} to use with this RichTextArea.
     * The model can be null.
     * @return the model property
     */
    public final ObjectProperty<StyledTextModel> modelProperty() {
        return model;
    }

    public final void setModel(StyledTextModel m) {
        modelProperty().set(m);
    }

    public final StyledTextModel getModel() {
        return model.get();
    }

    /**
     * Indicates whether text should be wrapped in this RichTextArea.
     * If a run of text exceeds the width of the {@code RichTextArea},
     * then this variable indicates whether the text should wrap onto
     * another line.
     * Setting this property to {@code true} has a side effect of hiding the horizontal scroll bar.
     * @defaultValue false
     */
    private StyleableBooleanProperty wrapText = new StyleableBooleanProperty(false) {
        @Override
        public Object getBean() {
            return RichTextArea.this;
        }

        @Override
        public String getName() {
            return "wrapText";
        }

        @Override
        public CssMetaData<RichTextArea, Boolean> getCssMetaData() {
            return StyleableProperties.WRAP_TEXT;
        }
    };

    public final BooleanProperty wrapTextProperty() {
        return wrapText;
    }

    public final boolean isWrapText() {
        return wrapText.getValue();
    }

    public final void setWrapText(boolean value) {
        wrapText.setValue(value);
    }

    /**
     * This property controls whether caret will be displayed or not.
     * TODO StyleableProperty ?
     * @return the display caret property
     */
    public final BooleanProperty displayCaretProperty() {
        return displayCaretProperty;
    }

    public final void setDisplayCaret(boolean on) {
        displayCaretProperty.set(on);
    }

    public final boolean isDisplayCaret() {
        return displayCaretProperty.get();
    }

    /**
     * Indicates whether this RichTextArea can be edited by the user.
     * @return the editable property
     */
    public final BooleanProperty editableProperty() {
        if (editableProperty == null) {
            editableProperty = new SimpleBooleanProperty(this, "editable", true);
        }
        return editableProperty;
    }
    
    public final boolean isEditable() {
        if (editableProperty == null) {
            return true;
        }
        return editableProperty().get();
    }

    public final void setEditable(boolean on) {
        editableProperty().set(on);
    }

    /**
     * Indicates whether the current paragraph will be visually highlighted.
     * TODO StyleableProperty ?
     * @return the highlight current paragraph property
     */
    public final BooleanProperty highlightCurrentParagraphProperty() {
        if (highlightCurrentParagraph == null) {
            highlightCurrentParagraph = new SimpleBooleanProperty(this, "highlightCurrentParagraph", true);
        }
        return highlightCurrentParagraph;
    }

    public final boolean isHighlightCurrentParagraph() {
        if (highlightCurrentParagraph == null) {
            return true;
        }
        return highlightCurrentParagraph.get();
    }
    
    public final void setHighlightCurrentParagraph(boolean on) {
        highlightCurrentParagraphProperty().set(on);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        switch (attribute) {
        // TODO possibly large text - could we send just what is displayed?
//        case TEXT: {
//            String accText = getAccessibleText();
//            if (accText != null && !accText.isEmpty())
//                return accText;
//
//            String text = getText();
//            if (text == null || text.isEmpty()) {
//                text = getPromptText();
//            }
//            return text;
//        }
        case EDITABLE:
            return isEditable();
//        case SELECTION_START:
//            return getSelection().getStart();
//        case SELECTION_END:
//            return getSelection().getEnd();
//        case CARET_OFFSET:
//            return getCaretPosition();
//        case FONT:
//            return getFont();
        default:
            return super.queryAccessibleAttribute(attribute, parameters);
        }
    }

    // TODO lazy initialization is not necessary
    private static class StyleableProperties {
        private static final CssMetaData<RichTextArea, Insets> CONTENT_PADDING =
            new CssMetaData<>("-fx-content-padding", InsetsConverter.getInstance(), Params.CONTENT_PADDING)
        {
            @Override
            public boolean isSettable(RichTextArea t) {
                return t.contentPadding == null || !t.contentPadding.isBound();
            }

            @Override
            public StyleableProperty<Insets> getStyleableProperty(RichTextArea t) {
                return (StyleableProperty<Insets>)t.contentPaddingProperty();
            }
        };

        private static final CssMetaData<RichTextArea,Boolean> WRAP_TEXT =
            new CssMetaData<>("-fx-wrap-text", StyleConverter.getBooleanConverter(), false)
        {
                @Override
                public boolean isSettable(RichTextArea t) {
                    return !t.wrapText.isBound();
                }
    
                @Override
                public StyleableProperty<Boolean> getStyleableProperty(RichTextArea t) {
                    return (StyleableProperty<Boolean>)t.wrapTextProperty();
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = RichUtils.combine(
            Control.getClassCssMetaData(),
            CONTENT_PADDING,
            WRAP_TEXT
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
        return getClassCssMetaData();
    }
    
    private VFlow vflow() {
        return RichTextAreaSkinHelper.getVFlow(this);
    }

    /**
     * Finds a text position corresponding to the specified screen coordinates.
     * @param screenX screen x coordinate
     * @param screenY screen y coordinate
     * @return the TextPosition
     */
    // TODO or should it be local to control?
    public TextPos getTextPosition(double screenX, double screenY) {
        Point2D local = vflow().getContentPane().screenToLocal(screenX, screenY);
        return vflow().getTextPosLocal(local.getX(), local.getY());
    }

    /**
     * Determines the caret blink rate.
     * @return the caret blunk period property
     */
    public final ReadOnlyObjectProperty<Duration> caretBlinkPeriodProperty() {
        return caretBlinkPeriod.getReadOnlyProperty();
    }

    public final void setCaretBlinkPeriod(Duration period) {
        if (period == null) {
            throw new NullPointerException("caret blink period cannot be null");
        }
        caretBlinkPeriod.set(period);
    }

    public final Duration getCaretBlinkPeriod() {
        return caretBlinkPeriod.get();
    }

    /**
     * Moves the caret and anchor to the new position, unless {@code extendSelection} is true, in which case
     * extend selection from the existing anchor to the newly set caret position.
     * @param p text position
     * @param extendSelection specifies whether to clear (false) or extend (true) any existing selection
     */
    public void moveCaret(TextPos p, boolean extendSelection) {
        if (extendSelection) {
            extendSelection(p);
        } else {
            select(p, p);
        }
    }

    /**
     * Tracks the caret position within the document.  The value can be null.
     * <p>
     * Important note: setting a {@link SelectionSegment} causes an update to both anchor and caret properties.
     * Typically, they both should be either null (corresponding to a null selection segment) or non-null.
     * However, it is possible to read one null value and one non-null value in a listener.  To lessen the impact,
     * the caretProperty is updated last, so any listener monitoring the caret property would read the right anchor
     * value.  A listener monitoring the anchorProperty might see erroneous value for the caret, so keep that in mind.
     *
     * @return the caret position property
     */
    public final ReadOnlyProperty<TextPos> caretPositionProperty() {
        return selectionModel.caretPositionProperty();
    }
    
    public final TextPos getCaretPosition() {
        return caretPositionProperty().getValue();
    }

    public final TextPos getAnchorPosition() {
        return anchorPositionProperty().getValue();
    }

    /**
     * Tracks the selection anchor position within the document.  The value can be null.
     * <p>
     * Important note: setting a {@link SelectionSegment} causes an update to both anchor and caret properties.
     * Typically, they both should be either null (corresponding to a null selection segment) or non-null.
     * However, it is possible to read one null value and one non-null value in a listener.  To lessen the impact,
     * the caretProperty is updated last, so any listener monitoring the caret property would read the right anchor
     * value.  A listener monitoring the anchorProperty might see erroneous value for the caret, so keep that in mind.
     *
     * @return the anchor position property
     */
    public final ReadOnlyProperty<TextPos> anchorPositionProperty() {
        return selectionModel.anchorPositionProperty();
    }

    /**
     * Tracks the current selection segment position.
     * The value can be null.
     * @return the selection segment property
     */
    public final ReadOnlyProperty<SelectionSegment> selectionSegmentProperty() {
        return selectionModel.selectionSegmentProperty();
    }

    /**
     * Clears existing selection, if any.
     */
    public void clearSelection() {
        selectionModel.clear();
    }

    /**
     * Moves the caret to the specified position, clearing the selection.
     * @param pos the text position
     */
    public void setCaret(TextPos pos) {
        StyledTextModel model = getModel();
        if (model != null) {
            Marker m = model.getMarker(pos);
            selectionModel.setSelection(m, m);
        }
    }

    /**
     * Selects the specified range and places the caret at the new position.
     * @param anchor the new selection anchor position
     * @param caret the new caret position
     */
    public void select(TextPos anchor, TextPos caret) {
        StyledTextModel model = getModel();
        if (model != null) {
            Marker ma = model.getMarker(anchor);
            Marker mc = model.getMarker(caret);
            selectionModel.setSelection(ma, mc);
        }
    }
    
    /**
     * Extends selection from the existing anchor to the new position.
     * @param pos the text position
     */
    public void extendSelection(TextPos pos) {
        StyledTextModel model = getModel();
        if (model != null) {
            Marker m = model.getMarker(pos);
            selectionModel.extendSelection(m);
        }
    }

    /**
     * Returns the number of paragraphs in the model.  If model is null, returns 0.
     * @return the paragraph count
     */
    public int getParagraphCount() {
        StyledTextModel m = getModel();
        return (m == null) ? 0 : m.size();
    }

    /**
     * Returns the plain text at the specified paragraph index.
     * @param modelIndex paragraph index
     * @return plain text string, or null
     * @throws IllegalArgumentException if the modelIndex is outside of the range supported by the model
     */
    public String getPlainText(int modelIndex) {
        if ((modelIndex < 0) || (modelIndex >= getParagraphCount())) {
            throw new IllegalArgumentException("No paragraph at index=" + modelIndex);
        }
        return getModel().getPlainText(modelIndex);
    }

    private RichTextAreaSkin richTextAreaSkin() {
        return (RichTextAreaSkin)getSkin();
    }

    private StyleResolver resolver() {
        RichTextAreaSkin skin = richTextAreaSkin();
        if (skin != null) {
            return skin.getStyleResolver();
        }
        return null;
    }

    /**
     * Determines whether the preferred width is the same as the content width.
     * When set to true, the horizontal scroll bar is disabled.
     *
     * @defaultValue false
     * @return the use content width property
     */
    public final BooleanProperty useContentWidthProperty() {
        if (useContentWidth == null) {
            useContentWidth = new SimpleBooleanProperty();
        }
        return useContentWidth;
    }

    public final boolean isUseContentWidth() {
        return useContentWidth == null ? false : useContentWidth.get();
    }

    public final void setUseContentWidth(boolean on) {
        useContentWidthProperty().set(true);
    }
    
    /**
     * Determines whether the preferred height is the same as the content height.
     * When set to true, the vertical scroll bar is disabled.
     *
     * @defaultValue false
     * @return the use content height property
     */
    public final BooleanProperty useContentHeightProperty() {
        if (useContentHeight == null) {
            useContentHeight = new SimpleBooleanProperty();
        }
        return useContentHeight;
    }

    public final boolean isUseContentHeight() {
        return useContentHeight == null ? false : useContentHeight.get();
    }

    public final void setUseContentHeight(boolean on) {
        useContentHeightProperty().set(true);
    }

    /**
     * When selection exists, deletes selected text.  Otherwise, deletes the symbol before the caret.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void backspace() {
        execute(BACKSPACE);
    }
    
    /**
     * When selection exists, copies the selected rich text to the clipboard in all formats supported
     * by the model.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void copy() {
        execute(COPY);
    }

    /**
     * Copies the text in the specified format when selection exists and when the export in this format
     * is supported by the model, and the skin must be installed; otherwise, this method is a no-op.
     * @param format the data format to use
     */
    public void copy(DataFormat format) {
        RichTextAreaSkin skin = richTextAreaSkin();
        if (skin != null) {
            skin.copy(format);
        }
    }

    /**
     * When selection exists, removes the selected rich text and places it into the clipboard.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void cut() {
        execute(CUT);
    }

    /**
     * When selection exists, deletes selected text.  Otherwise, deletes the symbol at the caret.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void delete() {
        execute(DELETE);
    }

    /**
     * Inserts a line break at the caret.  If selection exists, first deletes the selected text.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void insertLineBreak() {
        execute(INSERT_LINE_BREAK);
    }
    
    /**
     * Inserts a tab symbol at the caret.  If selection exists, first deletes the selected text.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void insertTab() {
        execute(INSERT_TAB);
    }
    
    /**
     * Moves the caret to after the last character of the text, also clearing the selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveDocumentEnd() {
        execute(MOVE_DOCUMENT_END);
    }
    
    /**
     * Moves the caret to before the first character of the text.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveDocumentStart() {
        execute(MOVE_DOCUMENT_START);
    }
    
    /**
     * Moves the caret down.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveDown() {
        execute(MOVE_DOWN);
    }
    
    /**
     * Moves the caret left.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveLeft() {
        execute(MOVE_LEFT);
    }
    
    /**
     * Moves the caret to the end of the current paragraph.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveParagraphEnd() {
        execute(MOVE_PARAGRAPH_END);
    }
    
    /**
     * Moves the caret to the start of the current paragraph.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveLineStart() {
        execute(MOVE_PARAGRAPH_START);
    }
    
    /**
     * Moves the caret to the next symbol.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveRight() {
        execute(MOVE_RIGHT);
    }
    
    /**
     * Moves the caret up.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveUp() {
        execute(MOVE_UP);
    }
    
    /**
     * Moves the caret to the beginning of previous word in a left-to-right setting,
     * or the beginning of the next word in a right-to-left setting.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveLeftWord() {
        execute(MOVE_WORD_PREVIOUS);
    }

    /**
     * Moves the caret to the beginning of next word in a left-to-right setting,
     * or the beginning of the previous word in a right-to-left setting.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveRightWord() {
        execute(MOVE_WORD_NEXT);
    }
    
    /**
     * Moves the caret to the beginning of previous word.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void movePreviousWord() {
        execute(MOVE_WORD_PREVIOUS);
    }

    /**
     * Moves the caret to the beginning of next word.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveNextWord() {
        execute(MOVE_WORD_NEXT);
    }

    /**
     * Moves the caret to the end of the next word.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void moveEndOfNextWord() {
        execute(MOVE_WORD_NEXT_END);
    }
    
    /**
     * Move caret one page down.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void pageDown() {
        execute(PAGE_DOWN);
    }
    
    /**
     * Move caret one page up.
     * This method has a side effect of clearing an existing selection.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void pageUp() {
        execute(PAGE_UP);
    }

    /**
     * Pastes the clipboard content at the caret, or, if selection exists, replacing the selected text.
     * The model decides which format to use.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void paste() {
        execute(PASTE);
    }

    /**
     * Pastes the clipboard content at the caret, or, if selection exists, replacing the selected text.
     * The format must be supported by the model, and the skin must be installed,
     * otherwise this method has no effect.
     * @param format the data format to use
     */
    public void paste(DataFormat format) {
        RichTextAreaSkin skin = richTextAreaSkin();
        if (skin != null) {
            skin.paste(format);
        }
    }

    /**
     * Pastes the plain text clipboard content at the caret, or, if selection exists, replacing the selected text.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void pastePlainText() {
        execute(PASTE_PLAIN_TEXT);
    }
    
    /**
     * If possible, redoes the last undone modification. If {@link #isRedoable()} returns
     * false, then calling this method has no effect.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void redo() {
        execute(REDO);
    }
    
    /**
     * Selects all the text in the document.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectAll() {
        execute(SELECT_ALL);
    }

    /**
     * Selects from the anchor position to the document start.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectDocumentStart() {
        execute(SELECT_DOCUMENT_START);
    }

    /**
     * Selects from the anchor position to the document end.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectDocumentEnd() {
        execute(SELECT_DOCUMENT_END);
    }
    
    /**
     * Moves the caret down and extends selection to the new position.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectDown() {
        execute(SELECT_DOWN);
    }
    
    /**
     * Moves the caret left and extends selection to the new position.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectLeft() {
        execute(SELECT_LEFT);
    }
    
    /**
     * Moves the caret one page down and extends selection to the new position.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectPageDown() {
        execute(SELECT_PAGE_DOWN);
    }
    
    /**
     * Moves the caret one page up and extends selection to the new position.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectPageUp() {
        execute(SELECT_PAGE_UP);
    }
    
    /**
     * Selects the paragraph at the caret.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectParagraph() {
        execute(SELECT_PARAGRAPH);
    }
    
    /**
     * Moves the caret right and extends selection to the new position.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectRight() {
        execute(SELECT_RIGHT);
    }
    
    /**
     * Moves the caret up and extends selection to the new position.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectUp() {
        execute(SELECT_UP);
    }

    /**
     * Selects a word at the caret.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectWord() {
        execute(SELECT_WORD);
    }
    
    /**
     * Moves the caret to the beginning of previous word in a left-to-right setting,
     * or to the beginning of the next word in a right-to-left setting.
     * This does not cause
     * the selection to be cleared. Rather, the anchor stays put and the caretPosition is
     * moved to the beginning of next word.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectLeftWord() {
        execute(SELECT_WORD_LEFT);
    }
    
    /**
     * Moves the caret to the beginning of next word in a left-to-right setting,
     * or to the beginning of the previous word in a right-to-left setting.
     * This does not cause
     * the selection to be cleared. Rather, the anchor stays put and the caretPosition is
     * moved to the beginning of next word.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectRightWord() {
        execute(SELECT_WORD_RIGHT);
    }
    
    /**
     * Moves the caret to the beginning of next word. This does not cause
     * the selection to be cleared. Rather, the anchor stays put and the caretPosition is
     * moved to the beginning of next word.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectNextWord() {
        execute(SELECT_WORD_NEXT);
    }
    
    /**
     * Moves the caret to the beginning of previous word. This does not cause
     * the selection to be cleared. Rather, the anchor stays put and the caretPosition is
     * moved to the beginning of previous word.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectPreviousWord() {
        execute(SELECT_WORD_PREVIOUS);
    }

    /**
     * Moves the caret to the end of the next word. This does not cause
     * the selection to be cleared.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void selectEndOfNextWord() {
        execute(SELECT_WORD_NEXT_END);
    }

    /**
     * If possible, undoes the last modification. If {@link #isUndoable()} returns
     * false, then calling this method has no effect.
     * <p>This action can be changed by remapping the default behavior, @see {@link #getInputMap()}.
     */
    public void undo() {
        execute(UNDO);
    }
    
    /**
     * Determines whether the last edit operation is undoable.
     * @return true if undoable
     */
    public boolean isUndoable() {
        StyledTextModel m = getModel();
        return m == null ? false : m.isUndoable();
    }

    /**
     * Determines whether the last edit operation is redoable.
     * @return true if redoable
     */
    public boolean isRedoable() {
        StyledTextModel m = getModel();
        return m == null ? false : m.isRedoable();
    }

    /**
     * Returns true if a non-empty selection exists.
     * @return true if selection exists
     */
    public boolean hasNonEmptySelection() {
        TextPos ca = getCaretPosition();
        if (ca != null) {
            TextPos an = getAnchorPosition();
            if (an != null) {
                return !ca.isSameInsertionIndex(an);
            }
        }
        return false;
    }

    /**
     * Applies the specified style to the selected range.  The specified attributes will be merged, overriding
     * the existing ones.
     * When applying the paragraph attributes, the affected range might go beyond the range specified by
     * {@code start} and {@code end}.
     * @param start the start of text range
     * @param end the end of text range
     * @param attrs the style attributes to apply
     */
    public void applyStyle(TextPos start, TextPos end, StyleAttrs attrs) {
        if (canEdit()) {
            StyledTextModel m = getModel();
            m.applyStyle(start, end, attrs);
        }
    }

    /**
     * Sets the specified style to the selected range.
     * All the existing attributes in the selected range will be cleared.
     * When setting the paragraph attributes, the affected range
     * might be wider than one specified.
     * @param start the start of text range
     * @param end the end of text range
     * @param attrs the style attributes to set
     */
    public void setStyle(TextPos start, TextPos end, StyleAttrs attrs) {
        if (canEdit()) {
            StyledTextModel m = getModel();
            m.setStyle(start, end, attrs);
        }
    }

    /**
     * Returns true if this control's {@link #isEditable()} returns true and the model's
     * {@link StyledTextModel#isEditable()} also returns true.
     * @return true if model is not null and is editable
     */
    protected boolean canEdit() {
        if (isEditable()) {
            StyledTextModel m = getModel();
            if (m != null) {
                return m.isEditable();
            }
        }
        return false;
    }

    private StyleAttrs getModelStyleAttrs(StyleResolver r) {
        StyledTextModel m = getModel();
        if (m != null) {
            TextPos pos = getCaretPosition();
            if (pos != null) {
                if (hasNonEmptySelection()) {
                    TextPos an = getAnchorPosition();
                    if (pos.compareTo(an) > 0) {
                        pos = an;
                    }
                } else if (!TextPos.ZERO.equals(pos)) {
                    int ix = pos.offset() - 1;
                    if (ix < 0) {
                        // FIX find previous symbol
                        ix = 0;
                    }
                    pos = new TextPos(pos.index(), ix);
                }
                return m.getStyleAttrs(r, pos);
            }
        }
        return StyleAttrs.EMPTY;
    }

    /**
     * Returns {@code StyleAttrs} which contains character and paragraph attributes.
     * <br>
     * When selection exists, returns the attributes at the first selected character.
     * <br>
     * When no selection exists, returns the attributes at the character which immediately precedes the caret.
     * When at the beginning of the document, returns the attributes of the first character.
     * If the model uses CSS styles, this method resolves individual attributes (bold, font size, etc.)
     * according to the stylesheet for this instance of {@code RichTextArea}.
     *
     * @return the non-null {@code StyleAttrs} instance
     */
    // FIX add paragraph attributes
    public StyleAttrs getActiveStyleAttrs() {
        StyleResolver r = resolver();
        StyleAttrs a = getModelStyleAttrs(r);

        StyleAttrs pa = getDefaultAttributes();
        if ((pa == null) || pa.isEmpty()) {
            return a;
        }
        return pa.combine(a);
    }

    /**
     * Returns a TextPos corresponding to the end of the document.
     *
     * @return the text position
     */
    public TextPos getEndTextPos() {
        StyledTextModel m = getModel();
        return (m == null) ? TextPos.ZERO : m.getDocumentEnd();
    }

    /**
     * Returns a TextPos corresponding to the end of paragraph.
     *
     * @param index paragraph index
     * @return text position
     */
    public TextPos getEndOfParagraph(int index) {
        StyledTextModel m = getModel();
        return (m == null) ? TextPos.ZERO : m.getEndOfParagraphTextPos(index);
    }

    /**
     * Specifies the left-side paragraph decorator.
     * The value can be null.
     * @return the left decorator property
     */
    public final ObjectProperty<SideDecorator> leftDecoratorProperty() {
        if (leftDecorator == null) {
            leftDecorator = new SimpleObjectProperty<>(this, "leftDecorator");
        }
        return leftDecorator;
    }

    public final SideDecorator getLeftDecorator() {
        if (leftDecorator == null) {
            return null;
        }
        return leftDecorator.get();
    }
    
    public final void setLeftDecorator(SideDecorator d) {
        leftDecoratorProperty().set(d);
    }

    /**
     * Specifies the right-side paragraph decorator.
     * The value can be null.
     * @return the right decorator property
     */
    public final ObjectProperty<SideDecorator> rightDecoratorProperty() {
        if (rightDecorator == null) {
            rightDecorator = new SimpleObjectProperty<>(this, "rightDecorator");
        }
        return rightDecorator;
    }

    public final SideDecorator getRightDecorator() {
        if (rightDecorator == null) {
            return null;
        }
        return rightDecorator.get();
    }
    
    public final void setRightDecorator(SideDecorator d) {
        rightDecoratorProperty().set(d);
    }

    /**
     * Specifies the padding for the RichTextArea content.
     * The value can be null.
     * @return the content padding property
     */
    public final ObjectProperty<Insets> contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = new SimpleStyleableObjectProperty<Insets>(
                StyleableProperties.CONTENT_PADDING,
                this,
                "contentPadding",
                Params.CONTENT_PADDING
            );
        }
        return contentPadding;
    }

    public final void setContentPadding(Insets value) {
        contentPaddingProperty().set(value);
    }

    public final Insets getContentPadding() {
        if(contentPadding == null) {
            return Params.CONTENT_PADDING;
        }
        return contentPadding.get();
    }

    /**
     * Specifies the default attributes.
     * The value can be null.
     * @return the default attributes property
     */
    // TODO this might be a mistake.  Instead, the default attrbiutes should be in the model?
    // the use case is the default font.
    public final ObjectProperty<StyleAttrs> defaultAttributesProperty() {
        return defaultAttributes;
    }

    public final void setDefaultAttributes(StyleAttrs a) {
        defaultAttributes.set(a);
    }

    public final StyleAttrs getDefaultAttributes() {
        return defaultAttributes.get();
    }

    /**
     * Sets a single default attribute by updating the {@code defaultAttributesProperty}.
     * @param <T> the attribute type
     * @param attr the attribute
     * @param value the attribute value
     */
    public final <T> void setDefaultAttribute(StyleAttribute<T> attr, T value) {
        StyleAttrs old = getDefaultAttributes();
        StyleAttrs a = StyleAttrs.builder().
            merge(old).
            set(attr, value).
            build();
        setDefaultAttributes(a);
    }

    // TODO to be moved to Control JDK-8314968
    private final InputMap<RichTextArea> inputMap = new InputMap<>(this);

    // TODO to be moved to Control JDK-8314968
    public InputMap<RichTextArea> getInputMap() {
        return inputMap;
    }

    // TODO to be moved to Control JDK-8314968
    protected final void execute(FunctionTag tag) {
        Runnable f = getInputMap().getFunction(tag);
        if (f != null) {
            f.run();
        }
    }

    /**
     * Sets a paragraph style attribute handler.
     * @param <C>
     * @param <T>
     * @param a
     * @param p
     */
    protected static <C extends RichTextArea, T> void setParHandler(StyleAttribute<T> a, StyleAttributeHandler<C, T> p) {
        parStyleHandlerMap.put(a, p);
    }
    
    /**
     * Sets a text segment style attribute handler.
     * @param <C>
     * @param <T>
     * @param a
     * @param p
     */
    protected static <C extends RichTextArea, T> void setSegHandler(StyleAttribute<T> a, StyleAttributeHandler<C, T> p) {
        segStyleHandlerMap.put(a, p);
    }

    /**
     * TODO hide behind an accessor
     *
     * @param <T>
     * @param forParagraph
     * @param cx
     * @param a
     * @param value
     */
    public <T> void processAttribute(boolean forParagraph, CellContext cx, StyleAttribute<T> a, T value) {
        StyleAttributeHandler h = (forParagraph ? parStyleHandlerMap : segStyleHandlerMap).get(a);
        if (h != null) {
            h.apply(this, cx, value);
        }
    }

    private static void initStyleHandlers() {
        setParHandler(StyleAttrs.BACKGROUND, (c, cx, v) -> {
            String color = RichUtils.toCssColor(v);
            cx.addStyle("-fx-background-color:" + color + ";");
        });

        setSegHandler(StyleAttrs.BOLD, (c, cx, v) -> {
            cx.addStyle(v ? "-fx-font-weight:bold;" : "-fx-font-weight:normal;");
        });

        setSegHandler(CssStyles.CSS, (c, cx, v) -> {
            String st = v.style();
            if (st != null) {
                cx.addStyle(st);
            }
            String[] names = v.names();
            if (names != null) {
                cx.getNode().getStyleClass().addAll(names);
            }
        });

        setSegHandler(StyleAttrs.FONT_FAMILY, (cc, c, v) -> {
            c.addStyle("-fx-font-family:'" + v + "';");
        });

        setSegHandler(StyleAttrs.FONT_SIZE, (cc, c, v) -> {
            c.addStyle("-fx-font-size:" + v + ";");
        });

        setSegHandler(StyleAttrs.ITALIC, (cc, c, v) -> {
            if (v) {
                c.addStyle("-fx-font-style:italic;");
            }
        });

        setParHandler(StyleAttrs.LINE_SPACING, (cc, c, v) -> {
            c.addStyle("-fx-line-spacing:" + v + ";");
        });
        
        setParHandler(StyleAttrs.RIGHT_TO_LEFT, (cc, cx, v) -> {
            if (cc.isWrapText()) {
                // node orientation property is not styleable (yet?)
                cx.getNode().setNodeOrientation(v ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
            }
        });

        // this is a special case: 4 attributes merged into one -fx style
        // unfortunately, this might create multiple copies of the same style string
        StyleAttributeHandler<RichTextArea, Double> spaceHandler = (cc, c, v) -> {
            StyleAttrs a = c.getAttributes();
            double top = a.getDouble(StyleAttrs.SPACE_ABOVE, 0);
            double right = a.getDouble(StyleAttrs.SPACE_RIGHT, 0);
            double bottom = a.getDouble(StyleAttrs.SPACE_BELOW, 0);
            double left = a.getDouble(StyleAttrs.SPACE_LEFT, 0);
            c.addStyle("-fx-padding:" + top + ' ' + right + ' ' + bottom + ' ' + left + ";");
        };
        setParHandler(StyleAttrs.SPACE_ABOVE, spaceHandler);
        setParHandler(StyleAttrs.SPACE_RIGHT, spaceHandler);
        setParHandler(StyleAttrs.SPACE_BELOW, spaceHandler);
        setParHandler(StyleAttrs.SPACE_LEFT, spaceHandler);

        setSegHandler(StyleAttrs.STRIKE_THROUGH, (cc, c, v) -> {
            if (v) {
                c.addStyle("-fx-strikethrough:true;");
            }
        });

        setParHandler(StyleAttrs.TEXT_ALIGNMENT, (c, cx, v) -> {
            if (c.isWrapText()) {
                String alignment = RichUtils.toCss(v);
                cx.addStyle("-fx-text-alignment:" + alignment + ";");
            }
        });

        setSegHandler(StyleAttrs.TEXT_COLOR, (c, cx, v) -> {
            String color = RichUtils.toCssColor(v);
            cx.addStyle("-fx-fill:" + color + ";");
        });

        setSegHandler(StyleAttrs.UNDERLINE, (cc, cx, v) -> {
            if (v) {
                cx.addStyle("-fx-underline:true;");
            }
        });
    }

    /**
     * TODO
     * @param text
     * @param attrs
     * @return
     */
    public TextPos appendText(String text, StyleAttrs attrs) {
        TextPos p = getEndTextPos();
        return insertText(p, text, attrs);
    }

    /**
     * TODO
     * @param in
     * @return
     */
    public TextPos appendText(StyledInput in) {
        TextPos p = getEndTextPos();
        return insertText(p, in);
    }

    /**
     * TODO
     * @param pos
     * @param text
     * @param attrs
     * @return
     */
    public TextPos insertText(TextPos pos, String text, StyleAttrs attrs) {
        StyledInput in = StyledInput.of(text, attrs);
        return insertText(pos, in);
    }
    
    /**
     * TODO
     * @param pos
     * @param in
     * @return
     */
    public TextPos insertText(TextPos pos, StyledInput in) {
        if (canEdit()) {
            StyledTextModel m = getModel();
            m.clearUndoRedo();
            return m.replace(vflow(), pos, pos, in, false);
        }
        return null;
    }

    /**
     * Replaces the specified range with the new text.
     * @param start the start text position
     * @param end the end text position
     * @param in the input stream
     * @param createUndo when true, creates an undo-redo entry
     * @return the new caret position at the end of inserted text, or null if the change cannot be made
     */
    // TODO styled segment?  StyledInput?
    // TODO is create undo needed?
    public TextPos replaceText(TextPos start, TextPos end, StyledInput in, boolean createUndo) {
        if (canEdit()) {
            StyledTextModel m = getModel();
            return m.replace(vflow(), start, end, in, createUndo);
        }
        return null;
    }
    
    /**
     * Clears the undo-redo stack of the underlying model.
     * This method does nothing if the model is null.
     */
    public void clearUndoRedo() {
        StyledTextModel m = getModel();
        if (m != null) {
            m.clearUndoRedo();
        }
    }

    /**
     * Writes the content the output stream using the model's highest priority {@code DataFormat}.
     * This method does not close the output stream.
     * @param out the output stream
     * @throws IOException if an I/O error occurs
     */
    public void save(OutputStream out) throws IOException {
        DataFormat f = bestDataFormat(true);
        if (f != null) {
            save(f, out);
        }
    }

    /**
     * Writes the content the output stream using the specified {@code DataFormat}.
     * This method does not close the output stream.
     * @param f the data format
     * @param out the output stream
     * @throws IOException if an I/O error occurs
     */
    public void save(DataFormat f, OutputStream out) throws IOException {
        StyledTextModel m = getModel();
        if (m != null) {
            StyleResolver r = resolver();
            m.save(r, f, out);
        }
    }

    /**
     * Loads the content using the model's highest priority {@code DataFormat}.
     * This method does not close the input stream.
     * @param in the input stream
     * @throws IOException if an I/O error occurs
     */
    public void load(InputStream in) throws IOException {
        DataFormat f = bestDataFormat(false);
        if (f != null) {
            load(f, in);
        }
    }

    /**
     * Loads the content using the specified {@code DataFormat}.
     * This method does not close the input stream.
     * @param f the data format
     * @param in the input stream
     * @throws IOException if an I/O error occurs
     */
    public void load(DataFormat f, InputStream in) throws IOException {
        StyledTextModel m = getModel();
        if (m != null) {
            StyleResolver r = resolver();
            m.load(r, f, in);
            select(TextPos.ZERO, TextPos.ZERO);
        }
    }

    private DataFormat bestDataFormat(boolean forExport) {
        StyledTextModel m = getModel();
        if (m != null) {
            DataFormat[] fs = m.getSupportedDataFormats(forExport);
            if (fs.length > 0) {
                return fs[0];
            }
        }
        return null;
    }
}
