/*
 * Copyright (c) 2024, 2026, Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.scene.text.LayoutInfo;
import javafx.scene.text.TextLineInfo;

/**
 * Represents the text geometry as a sequence of bounding rectangles
 * in the TextFlow coordinates.
 */
public final class RangeInfo {
    private final List<TextLineInfo> lines;
    private final double ymin;
    private final double ymax;

    private RangeInfo(List<TextLineInfo> lines, double ymin, double ymax) {
        this.lines = lines;
        this.ymin = ymin;
        this.ymax = ymax;
    }

    public static RangeInfo of(double width, double height) {
        return new RangeInfo(null, 0.0, height);
    }

    public static RangeInfo of(LayoutInfo la, double lineSpacing) {
        Rectangle2D r = la.getLogicalBounds(false);
        double ymin = r.getMinY();
        double ymax = r.getMaxY();
        List<TextLineInfo> lines = la.getTextLines(false);
        return new RangeInfo(lines, ymin, ymax);
    }

    public double getFirstLineMidY() {
        int sz = lines.size();
        if (sz > 0) {
            TextLineInfo t = lines.get(0);
            return midPoint(t);
        }
        return midPoint();
    }

    public double getLastLineMidY() {
        int sz = lines.size();
        if (sz > 0) {
            TextLineInfo t = lines.get(sz - 1);
            return midPoint(t);
        }
        return midPoint();
    }

    private double midPoint() {
        return (ymax + ymin) / 2.0;
    }

    private static double midPoint(TextLineInfo t) {
        Rectangle2D r = t.bounds();
        return (r.getMinY() + r.getMaxY()) / 2.0;
    }

    public boolean isOutsideTextRangeY(double y) {
        return (y < ymin) || (y >= ymax);
    }

    public double findHitMidpoint(double y) {
        if (lines != null) {
            for (TextLineInfo t : lines) {
                Rectangle2D r = t.bounds();
                if ((y >= r.getMinY()) && (y < r.getMaxY())) {
                    return midPoint(t);
                }
            }
        }
        return midPoint();
    }
}
