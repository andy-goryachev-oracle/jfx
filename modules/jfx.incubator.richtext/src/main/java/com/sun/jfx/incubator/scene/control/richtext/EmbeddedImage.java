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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import jfx.incubator.scene.control.richtext.model.StyleAttribute;

public class EmbeddedImage {

    public static final double FIT_WIDTH = -1.0;
    
    public static final StyleAttribute<EmbeddedImage> ATTRIBUTE = StyleAttribute.inlineNode("img", EmbeddedImage.class);
    public static final StringConverter<EmbeddedImage> CONVERTER = new Converter();

    private final byte[] bytes;
    private double width;

    // TODO height? keep aspect ratio?
    public EmbeddedImage(byte[] bytes, double width) {
        this.bytes = bytes;
        this.width = width;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public double getWidth() {
        return width;
    }

    public EmbeddedImageView getImageView() {
        Image im = new Image(new ByteArrayInputStream(bytes));
        return new EmbeddedImageView(this, im);
    }

    /// ImageView for EmbeddedImage
    public static class EmbeddedImageView extends ImageView {

        private final EmbeddedImage source;

        public EmbeddedImageView(EmbeddedImage src, Image im) {
            super(im);
            this.source = src;
        }
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
}
