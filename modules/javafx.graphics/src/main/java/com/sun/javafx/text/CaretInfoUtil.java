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
package com.sun.javafx.text;

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.text.CaretInfo;

/**
 * Utility helps building the CaretInfo.
 */
public class CaretInfoUtil {
    // TODO pass adjancent text runs info, derive flip
    public static CaretInfo createDual(float lineX, float lineY, float lineX2, float lineHeight, boolean flip) {
        return new CaretInfo() {
            @Override
            public PathElement[] getShape() {
                double y2 = lineY + lineHeight / 2;
                double yh = lineY + lineHeight;
                double dx = lineHeight * 0.1;
                double dy = lineHeight * 0.1;
                if (flip) {
                    dx = -dx;
                }

                return new PathElement[] {
                    new MoveTo(lineX, y2),
                    new LineTo(lineX, lineY),
                    new LineTo(lineX - dx, lineY),
                    new LineTo(lineX, lineY + dy),
                    new MoveTo(lineX2, y2),
                    new LineTo(lineX2, yh),
                    new LineTo(lineX2 + dx, yh),
                    new LineTo(lineX2, yh - dy)
                };
            }

            @Override
            public boolean isDual() {
                return true;
            }
        };
    }

    /** original caret shape with no direction indicators */
    private static PathElement[] createLegacyDualCaret(float lineX, float lineY, float lineX2, float lineHeight) {
        return new PathElement[] {
            new MoveTo(lineX, lineY),
            new LineTo(lineX, lineY + lineHeight / 2),
            new MoveTo(lineX2, lineY + lineHeight / 2),
            new LineTo(lineX2, lineY + lineHeight)
        };
    }

    public static CaretInfo createSingle(float x, float y, float h) {
        return new CaretInfo() {
            @Override
            public PathElement[] getShape() {
                return new PathElement[] {
                    new MoveTo(x, y),
                    new LineTo(x, y + h)
                };
            }

            @Override
            public boolean isDual() {
                return false;
            }
        };
    }
}
