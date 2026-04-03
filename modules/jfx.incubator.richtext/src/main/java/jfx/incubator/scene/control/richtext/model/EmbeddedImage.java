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

package jfx.incubator.scene.control.richtext.model;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import com.sun.jfx.incubator.scene.control.richtext.RequiresComplexLayout;
import com.sun.jfx.incubator.scene.control.richtext.VFlow;

/**
 * An attribute which allows to embed an image into the {@link RichTextModel}.
 * @since 27
 */
public final class EmbeddedImage {

    /** Limits the width of the inline to the wrapped text width. */
    public static final double FIT_WIDTH = -1.0;

    /**
     * The attribute descriptor.
     */
    public static final StyleAttribute<EmbeddedImage> ATTRIBUTE = StyleAttribute.inlineNode("img", EmbeddedImage.class);

    /**
     * The attribute String converter.
     */
    public static final StringConverter<EmbeddedImage> CONVERTER = new Converter();

    private final byte[] bytes;
    private final double width;
    private final double height;
    private final double targetWidth;

    /**
     * Constructor.
     *
     * @param bytes the image source
     * @param width the original image width
     * @param height the original image height
     * @param targetWidth target image width, or {link #FIT_WIDTH}
     */
    public EmbeddedImage(byte[] bytes, double width, double height, double targetWidth) {
        this.bytes = bytes;
        this.width = width;
        this.height = height;
        this.targetWidth = targetWidth;
    }

    private byte[] getBytes() {
        return bytes;
    }

    /**
     * Returns the original image width.
     * @return the image width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the original image height.
     * @return the image height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns the target image width specification: positive when specifying the final width,
     * or {@link #FIT_WIDTH} to make the image not to exceed the viewport width.
     * @return the image width
     */
    public double getTargetWidth() {
        return targetWidth;
    }

    @Override
    public String toString() {
        return "EmbeddedImage{target=" + targetWidth + "}";
    }

    @Override
    public boolean equals(Object x) {
        if (x == this) {
            return true;
        } else if (x instanceof EmbeddedImage im) {
            return
                (width == im.width) &&
                (height == im.height) &&
                (targetWidth == im.targetWidth) &&
                Arrays.equals(bytes, im.bytes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = EmbeddedImage.class.hashCode();
        h = 31 * h + Objects.hashCode(bytes);
        h = 31 * h + Double.hashCode(width);
        h = 31 * h + Double.hashCode(height);
        h = 31 * h + Double.hashCode(targetWidth);
        return h;
    }

    /**
     * Creates a copy of this {@code EmbeddedImage} with the specified target width.
     * @param target the new target width
     * @return the new instance
     */
    public EmbeddedImage setTargetWidth(double target) {
        return new EmbeddedImage(bytes, width, height, target);
    }

    /**
     * Creates the Node to be inserted into RichTextArea.
     * @return the node instance
     */
    public Node createNode() {
        Image im = new Image(new ByteArrayInputStream(bytes));
        if (targetWidth < 0) {
            return new Scaled(im);
        } else {
            return new Fixed(im);
        }
    }

    /// Embedded Image StringConverter.
    private static class Converter extends StringConverter<EmbeddedImage> {

        @Override
        public String toString(EmbeddedImage em) {
            byte[] b = em.getBytes();
            double w = em.getWidth();
            double h = em.getHeight();
            double tw = em.getTargetWidth();
            return
                "w," + w +
                ",h," + h +
                ",tw," + tw +
                ",b," + Base64.getEncoder().encodeToString(b);
        }

        @Override
        public EmbeddedImage fromString(String s) {
            String[] ss = s.split(",");
            byte[] b = null;
            double w = Double.NaN;
            double h = Double.NaN;
            double tw = FIT_WIDTH;
            for (int i = 0; i < ss.length;) {
                String k = ss[i++];
                String v = ss[i++];
                switch (k) {
                case "b":
                    b = Base64.getDecoder().decode(v);
                    break;
                case "h":
                    h = Double.parseDouble(v);
                    break;
                case "tw":
                    tw = Double.parseDouble(v);
                    break;
                case "w":
                    w = Double.parseDouble(v);
                    break;
                default:
                    throw new IllegalArgumentException("unknown field " + k);
                }
            }
            if ((b == null) || Double.isNaN(w) || Double.isNaN(h)) {
                // exception could include first N characters for debugging purposes
                throw new IllegalArgumentException("failed to parse EmbeddedImage");
            }
            return new EmbeddedImage(b, w, h, tw);
        }
    }

    /// Image Container that scales so as not to exceed the document width.
    private final class Scaled extends Label implements RequiresComplexLayout {

        private final ImageView view;

        public Scaled(Image im) {
            view = new ImageView(im);
            view.setSmooth(true);
            view.setPreserveRatio(true);
            
            setGraphic(view);
            setMaxWidth(Double.MAX_VALUE);
            setMinWidth(2);
            setMinHeight(2);
        }

        @Override
        public void updateVFlowContext(VFlow f) {
            double av = f.availableWidth();
            if (targetWidth < 0.0) {
                double fitWidth = ((av > 0.0) && (width > av)) ? av : width;
                view.setFitWidth(fitWidth);
            }
        }
    }

    /// Image Container with a fixed image.
    private final class Fixed extends Label {

        public Fixed(Image im) {
            ImageView view = new ImageView(im);
            view.setSmooth(true);
            view.setPreserveRatio(true);
            view.setFitWidth(targetWidth);

            setGraphic(view);
            setMaxWidth(USE_PREF_SIZE);
            setMinWidth(2);
            setMinHeight(2);
        }
    }
}
