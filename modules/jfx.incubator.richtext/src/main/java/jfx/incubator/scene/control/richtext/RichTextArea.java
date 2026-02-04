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
// This code borrows heavily from the following project, with permission from the author:
// https://github.com/andy-goryachev/FxEditor

package jfx.incubator.scene.control.richtext;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import jfx.incubator.scene.control.richtext.model.RichTextModel;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;
import jfx.incubator.scene.control.richtext.skin.RichTextAreaSkin;

/**
 * The RichTextArea control is designed for visualizing and editing rich text that can be styled in a variety of ways.
 *
 * <p>The RichTextArea control has a number of features, including:
 * <ul>
 * <li> {@link StyledTextModel paragraph-oriented model}, up to ~2 billion rows
 * <li> virtualized text cell flow
 * <li> support for text styling with an application stylesheet or {@link StyleAttributeMap inline attributes}
 * <li> support for multiple views connected to the same model
 * <li> {@link SelectionModel single selection}
 * <li> {@link jfx.incubator.scene.control.input.InputMap input map} which allows for easy behavior customization and extension
 * </ul>
 *
 * <h2>Creating a RichTextArea</h2>
 * <p>
 * The following example creates an editable control with the default {@link RichTextModel}:
 * <pre>{@code    RichTextArea textArea = new RichTextArea();
 * }</pre>
 * The methods
 * {@code appendText()}, {@code insertText()}, {@code replaceText()}, {@code applyStyle()},
 * {@code setStyle()}, or {@code clear()} can be used to modify text programmatically:
 * <pre>{@code    // create styles
 *   StyleAttributeMap heading = StyleAttributeMap.builder().setBold(true).setUnderline(true).setFontSize(18).build();
 *   StyleAttributeMap mono = StyleAttributeMap.builder().setFontFamily("Monospaced").build();
 *
 *   RichTextArea textArea = new RichTextArea();
 *   // build the content
 *   textArea.setUndoRedoEnabled(false);
 *   textArea.appendText("RichTextArea\n", heading);
 *   textArea.appendText("Example:\nText is ", StyleAttributeMap.EMPTY);
 *   textArea.appendText("monospaced.\n", mono);
 *   textArea.setUndoRedoEnabled(true);
 * }</pre>
 * Which results in the following visual representation:
 * <p>
 * <img src="doc-files/RichTextArea.png" alt="Image of the RichTextArea control">
 * </p>
 * <p>
 * A view-only information control requires a different model.  The following example illustrates how to
 * create a model that uses a stylesheet for styling:
 * <pre>{@code
 *     SimpleViewOnlyStyledModel m = new SimpleViewOnlyStyledModel();
 *     // add text segment using CSS style name (requires a stylesheet)
 *     m.addWithStyleNames("RichTextArea ", "HEADER");
 *     // add text segment using inline styles
 *     m.addWithInlineStyle("Demo", "-fx-font-size:200%; -fx-font-weight:bold;");
 *     // add newline
 *     m.nl();
 *
 *     RichTextArea textArea = new RichTextArea(m);
 * }</pre>
 *
 * <h2>Text Models</h2>
 * <p>
 * A number of standard models can be used with RichTextArea, each addressing a specific use case:
 * </p>
 * <table border=1>
 * <caption>Standard Models</caption>
 * <tr><th>Model Class</th><th>Description</th></tr>
 * <tr><td><pre>{@link StyledTextModel}</pre></td><td>Base class (abstract)</td></tr>
 * <tr><td><pre> ├─ {@link RichTextModel}</pre></td><td>Default model for RichTextArea</td></tr>
 * <tr><td><pre> ├─ {@link jfx.incubator.scene.control.richtext.model.BasicTextModel BasicTextModel}</pre></td><td>Unstyled text model</td></tr>
 * <tr><td><pre> │   └─ {@link jfx.incubator.scene.control.richtext.model.CodeTextModel CodeTextModel}</pre></td><td>Default model for CodeArea</td></tr>
 * <tr><td><pre> └─ {@link jfx.incubator.scene.control.richtext.model.StyledTextModelViewOnlyBase StyledTextModelViewOnlyBase}</pre></td><td>Base class for a view-only model (abstract)</td></tr>
 * <tr><td><pre>     └─ {@link jfx.incubator.scene.control.richtext.model.SimpleViewOnlyStyledModel SimpleViewOnlyStyledModel}</pre></td><td>In-memory view-only styled model</td></tr>
 * </table>
 *
 * <h2>Selection</h2>
 * <p>
 * The RichTextArea control maintains a single {@link #selectionProperty() contiguous selection segment}
 * as a part of the {@link SelectionModel}.  Additionally,
 * {@link #anchorPositionProperty()} and {@link #caretPositionProperty()} read-only properties
 * are derived from the {@link #selectionProperty()} for convenience.
 *
 * <h2>Customizing</h2>
 * The RichTextArea control offers some degree of customization that does not require subclassing:
 * <ul>
 * <li>customizing key bindings with the {@link jfx.incubator.scene.control.input.InputMap InputMap}
 * <li>setting {@link #leftDecoratorProperty() leftDecorator}
 * and {@link #rightDecoratorProperty() rightDecorator} properties
 * </ul>
 *
 * @since 24
 * @author Andy Goryachev
 * @see StyledTextModel
 */
public class RichTextArea extends AbstractStyledTextArea {

    private SimpleObjectProperty<StyleAttributeMap> insertStyles;

    /**
     * Creates the instance with the in-memory model {@link RichTextModel}.
     */
    public RichTextArea() {
        this(new RichTextModel());
    }

    /**
     * Creates the instance using the specified model.
     * <p>
     * Multiple RichTextArea instances can work off a single model.
     *
     * @param model the model
     */
    public RichTextArea(StyledTextModel model) {
        super(model);
    }

    // Properties


    /**
     * Specifies the styles to be in effect for the characters to be inserted via user input.
     * The value can be {@code null}, in which case the styles are determined by the model.
     *
     * @return the insert styles property
     * @defaultValue null
     * @since 26
     */
    public final ObjectProperty<StyleAttributeMap> insertStylesProperty() {
        if (insertStyles == null) {
            insertStyles = new SimpleObjectProperty<>(this, "insertStyles");
        }
        return insertStyles;
    }

    @Override
    public final StyleAttributeMap getInsertStyles() {
        if (insertStyles == null) {
            return null;
        }
        return insertStyles.get();
    }

    public final void setInsertStyles(StyleAttributeMap v) {
        insertStylesProperty().set(v);
    }

    // Non-public Methods

    @Override
    protected RichTextAreaSkin createDefaultSkin() {
        return new RichTextAreaSkin(this);
    }

    private RichTextAreaSkin richTextAreaSkin() {
        return (RichTextAreaSkin)getSkin();
    }
}
