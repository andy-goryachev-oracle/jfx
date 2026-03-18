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

package com.sun.jfx.incubator.scene.control.richtext;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import com.sun.jfx.incubator.scene.control.richtext.util.RichUtils;
import jfx.incubator.scene.control.richtext.model.StyleAttribute;

public class EmbeddedImage {

    /// Limits the width of the inline to the wrapped text width.
    public static final double FIT_WIDTH = -1.0;
    /// Limits the width of the inline to the wrapped text width, and prevents other elements from being placed
    /// in the same visual line.
    public static final double FULL_PARAGRAPH = -2.0;
    
    public static final StyleAttribute<EmbeddedImage> ATTRIBUTE = StyleAttribute.inlineNode("img", EmbeddedImage.class);
    public static final StringConverter<EmbeddedImage> CONVERTER = new Converter();

    private final byte[] bytes;
    private double width;

    // TODO height? keep aspect ratio?
    public EmbeddedImage(byte[] bytes, double width) {
        this.bytes = bytes;
        this.width = FIT_WIDTH; // FIX width;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public double getWidth() {
        return width;
    }

    public Node getNode() {
        Image im = new Image(new ByteArrayInputStream(bytes));
        //return new EmbeddedImageViewScaled(this, im);
        return new EmbeddedImageViewPref(this, im);
    }

    private static double availableWidth(VFlow f) {
        // TODO if wrapped?
        if (f != null) {
            Insets m = f.contentPadding();
            double w = f.getDocumentArea().getWidth() - m.getLeft() - m.getRight();
            if (w > 0.0) {
                return w;
            }
        }
        return -1.0;
    }

    /// Converter
    public static class Converter extends StringConverter<EmbeddedImage> {

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
    public final class EmbeddedImageViewPref extends Label {

        private final Image image;
        private final boolean useImageScale;
        private final boolean fullWidth;
        private DoubleBinding available;
        private BooleanBinding wrap;
        private ObjectBinding<VFlow> vflow;

        public EmbeddedImageViewPref(EmbeddedImage src, Image im) {
            this.image = im;

            ImageView v = new ImageView(im);
            v.setSmooth(true);
            v.setPreserveRatio(true);
            
            double width = src.getWidth();
            useImageScale = (width < 0.0);
            fullWidth = (width == FULL_PARAGRAPH);
            
            if(useImageScale) {
                // if use image scale, bind imageview width to prop1=(min(vflow.available, image.width))
                v.fitWidthProperty().bind(Bindings.createDoubleBinding(
                    this::computeImageWidth,
                    available()));
            }
            if(fullWidth) {
                // if full width, bind label width to prop2=(vflow.available)
                // TODO but only if wrapped!
                prefWidthProperty().bind(Bindings.createDoubleBinding(
                    this::computeContainerWidth,
                    available(),
                    wrap()));
            }
            
            // TODO set the scale instead!  see ImageCellPane

            setGraphic(v);
            setMaxWidth(Double.MAX_VALUE);
            setMinWidth(1);
            setMinHeight(1);
            setBackground(Background.fill(Color.LIGHTCORAL)); // FIX
            setPadding(new Insets(2)); // FIX
            
            widthProperty().addListener((_) -> {
                System.out.println("w=" + getWidth());
            });
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
                        // TODO move here?
                        return EmbeddedImage.availableWidth(f);
                    },
                    vflow()
                );
            }
            return available;
        }

        private BooleanBinding wrap() {
            if (wrap == null) {
                wrap = Bindings.createBooleanBinding(
                    () -> {
                        VFlow f = vflow().get();
                        return f == null ? false : f.isWrapText();
                    },
                    vflow()
                );
            }
            return wrap;
        }

        // depends on available()
        private double computeImageWidth() {
            double w = image.getWidth();
            double av = available().get();
            return (w > av) ? av : w;
        }

        // depends on available, wrap
        private double computeContainerWidth() {
            if (wrap().get()) {
                double av = available().get();
                return av;
            }
            return Region.USE_PREF_SIZE;
        }

        private VFlow getVFlow() {
            return RichUtils.getParentOfClass(VFlow.class, this);
        }

        private double computeScale() {
            if (width < 0.0) {
                //VFlow f = flow.get();
                // TODO can be optimized
                //double available = availableWidth(f);
                double available = availableWidth();
                IO.print("computeScale available=" + available);
                if (available > 0.0) {
                    // defaults to FIT_WIDTH
                    double w = image().getWidth();
                    if(w > available) {
                        return available / w;
                    }
                }
            }
            return 1.0;
        }

        private double availableWidth() {
            Parent p = getParent();
            if (p instanceof Region r) {
                // FIX TextFlow, returns 0
                return r.getWidth() - r.snappedLeftInset() - r.snappedRightInset();
            }
            return -1.0;
        }

        public EmbeddedImage getSource() {
            return EmbeddedImage.this;
        }

        private Image image() {
            return image;
            //return ((ImageView)getGraphic()).getImage();
        }

        /*
        @Override
        protected double computePrefWidth(double h) {
            double w = computePrefWidth();
            IO.println("computePrefWidth=" + w); // FIX
            if (w > 0.0) {
                return w;
            }
            return super.computePrefWidth(h);
        }

        /// returns preferred width or -1
        private double computePrefWidth() {
            if (width < 0.0) {
                //VFlow f = RichUtils.getParentOfClass(VFlow.class, this);
                double available = availableWidth();
                if (available <= 0.0) {
                    return -1.0;
                }
                // defaults to FIT_WIDTH
                double w = image().getWidth();
                return (w > available) ? available : w;
            }
            return -1.0;
        }
        */
    }

    /// Image Container with scaled image
    @Deprecated
    public final class EmbeddedImageViewScaled extends Label {

        private final Image image;
        private SimpleObjectProperty<VFlow> flow = new SimpleObjectProperty<>();

        public EmbeddedImageViewScaled(EmbeddedImage src, Image im) {
            this.image = im;

            flow.bind(Bindings.createObjectBinding(
                this::getVFlow,
                parentProperty(),
                widthProperty()));

            ImageView v = new ImageView(im);
            v.setSmooth(true);
            v.fitWidthProperty().bind(widthProperty());
            v.setPreserveRatio(true);
            v.scaleXProperty().bind(Bindings.createDoubleBinding(
                this::computeScale,
                flow));
            v.scaleYProperty().bind(v.scaleXProperty());
            // TODO set the scale instead!  see ImageCellPane

            setGraphic(v);
            setMaxWidth(USE_PREF_SIZE);
            setPrefWidth(77); //USE_COMPUTED_SIZE); FIX
            // TODO context menu: undo? size
        }

        private VFlow getVFlow() {
            return RichUtils.getParentOfClass(VFlow.class, this);
        }

        private double computeScale() {
            if (width < 0.0) {
                VFlow f = flow.get();
                // TODO can be optimized
                double available = availableWidth(f);
                if (available > 0.0) {
                    // defaults to FIT_WIDTH
                    double w = image().getWidth();
                    if(w > available) {
                        return available / w;
                    }
                }
            }
            return 1.0;
        }

        public EmbeddedImage getSource() {
            return EmbeddedImage.this;
        }

        private Image image() {
            return image;
        }
    }
}
