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
import java.util.Base64;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import com.sun.jfx.incubator.scene.control.richtext.VFlow;
import com.sun.jfx.incubator.scene.control.richtext.util.RichUtils;

/**
 * An attribute which allows to embed an image into the {@link RichTextModel}.
 */
public final class EmbeddedImage {

    /** Limits the width of the inline to the wrapped text width. */
    public static final double FIT_WIDTH = -1.0;

    // TODO remove? has issues.
    // Limits the width of the inline to the wrapped text width, and prevents other elements from being placed
    // in the same visual line.
    private static final double FULL_PARAGRAPH = -2.0;

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

    /**
     * Constructor.
     *
     * @param bytes the image source
     * @param width the width of an image, also accepts FIT_WIDTH
     */
    // TODO height? keep aspect ratio?
    public EmbeddedImage(byte[] bytes, double width) {
        this.bytes = bytes;
        this.width = width;
    }

    private byte[] getBytes() {
        return bytes;
    }

    /**
     * Returns the image width specification, including {@link #FIT_WIDTH}.
     * @return the image width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Creates the Node to be inserted into RichTextArea.
     * @return the node instance
     */
    public Node createNode() {
        Image im = new Image(new ByteArrayInputStream(bytes));
        if (width < 0) {
            return new Flex(im);
        } else {
            return new Scaled(im);
        }
    }

    /// Converter
    private static class Converter extends StringConverter<EmbeddedImage> {

        @Override
        public String toString(EmbeddedImage em) {
            byte[] b = em.getBytes();
            double w = em.getWidth();
            return "w," + w + ",b," + Base64.getEncoder().encodeToString(b);
        }

        @Override
        public EmbeddedImage fromString(String s) {
            String[] ss = s.split(",");
            byte[] b = null;
            double w = Double.NaN;
            for (int i = 0; i < ss.length;) {
                String k = ss[i++];
                String v = ss[i++];
                switch (k) {
                case "w":
                    w = Double.parseDouble(v);
                    break;
                case "b":
                    b = Base64.getDecoder().decode(v);
                    break;
                default:
                    throw new IllegalArgumentException("unknown field " + k);
                }
            }
            if ((b == null) || Double.isNaN(w)) {
                // exception could include first N characters for debugging purposes
                throw new IllegalArgumentException("failed to parse EmbeddedImage");
            }
            return new EmbeddedImage(b, w);
        }
    }

    /// Image Container
    /// Label[[ImageView]..space..]
    private final class Flex extends Label {

        private final Image image;
        private final boolean useImageScale;
        private final boolean fullWidth;
        private DoubleBinding available;
        private BooleanBinding wrap;
        private ObjectBinding<VFlow> vflow;

        public Flex(Image im) {
            this.image = im;

            ImageView v = new ImageView(im);
            v.setSmooth(true);
            v.setPreserveRatio(true);
            
            useImageScale = (width < 0.0);
            fullWidth = (width == FULL_PARAGRAPH);
            
            if (useImageScale) {
                // if use image scale, bind imageview width to prop1=(min(vflow.available, image.width))
                v.fitWidthProperty().bind(Bindings.createDoubleBinding(
                    () -> {
                        double w = image.getWidth();
                        double av = available().get();
                        if ((av > 0.0) && (w > av)) {
                            return av;
                        }
                        return w;
                    },
                    available()));
            }
            if (fullWidth) {
                // if full width, bind label width to prop2=(vflow.available)
                prefWidthProperty().bind(Bindings.createDoubleBinding(
                    () -> {
                        if (wrap().get()) {
                            double av = available().get();
                            if (av > 0.0) {
                                return av;
                            }
                        }
                        return Region.USE_PREF_SIZE;
                    },
                    available(),
                    wrap()));
            }
            
            setGraphic(v);
            setMaxWidth(Double.MAX_VALUE);
            setMinWidth(2);
            setMinHeight(2);
            
            // debug FIX z!
            {
                setBackground(Background.fill(Color.LIGHTCORAL)); // FIX
                setPadding(new Insets(2)); // FIX
                widthProperty().addListener((_) -> {
                    IO.println("EI.w=" + getWidth());
                });
            }
        }

        private ObjectBinding<VFlow> vflow() {
            if (vflow == null) {
                vflow = Bindings.createObjectBinding(
                    () -> {
                        return getScene() == null ? null : RichUtils.getParentOfClass(VFlow.class, this);
                    },
                    sceneProperty()
                );
            }
            return vflow;
        }

        private DoubleBinding available() {
            if (available == null) {
                available = Bindings.createDoubleBinding(
                    () -> {
                        VFlow f = vflow().get();
                        if (f != null) {
                            // FIX if !wrap
                            Insets m = f.contentPadding();
                            double w = f.getDocumentArea().getWidth() - m.getLeft() - m.getRight();
                            if (w > 0.0) {
                                return w;
                            }
                        }
                        return -1.0;
                    },
                    vflow()
                    // TODO vflow.wrap property!
                );
            }
            return available;
        }

        // TODO maybe fold it into available()
        @Deprecated
        private BooleanBinding wrap() {
            if (wrap == null) {
                wrap = Bindings.createBooleanBinding(
                    () -> {
                        VFlow f = vflow().get();
                        return f == null ? false : f.isWrapText();
                    },
                    // FIX this does not track runtime change of RTA.wrapText property!
                    vflow()
                );
            }
            return wrap;
        }
    }

    /// Image Container with scaled image
    private final class Scaled extends Label {

        private final Image image;

        public Scaled(Image im) {
            this.image = im;

            ImageView v = new ImageView(im);
            v.setSmooth(true);
            v.setFitWidth(width);
            v.setPreserveRatio(true);

            setGraphic(v);
            setMaxWidth(USE_PREF_SIZE);
            setMinWidth(2);
            setMinHeight(2);
        }
    }
}
