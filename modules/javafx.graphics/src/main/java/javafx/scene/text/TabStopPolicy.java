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
package javafx.scene.text;

import java.util.List;

/**
 * Tab Stop Policy.
 *
 * TODO
 * @since 999
 */
public final class TabStopPolicy {

    private final List<TabStop> tabStops;
    private final double firstLineIndent;
    private final double defaultStops;

    /**
     * Creates an immutable {@code TabStop} instance.
     *
     * @param tabStops the tab stops (a copy will be made)
     * @param firstLineIndent the first line indent, in points
     * @param defaultStops the default stops, in points
     */
    public TabStopPolicy(List<TabStop> tabStops, double firstLineIndent, double defaultStops) {
        this.tabStops = List.copyOf(tabStops);
        this.firstLineIndent = firstLineIndent;
        this.defaultStops = defaultStops;
    }

    /**
     * Specifies the list of tab stops.
     *
     * @return the non-null list of tab stops 
     */
    public List<TabStop> tabStops() {
        return tabStops;
    }
    
    /**
     * First line indent, in points, a positive value.  Negative or 0 values are treated as no first line indent.
     *
     * TODO
     * It is unclear whether the TextLayout should support negative values as it might impact the size and
     * the preferred size of the layout.
     *
     * @return the first line indent, in points
     */
    public double firstLineIndent() {
        return firstLineIndent;
    }

    /**
     * Provides default tab stops (beyond the last tab stop specified by {@code #tabStops()}, as a distance
     * in points from the last tab stop position.
     *
     * TODO
     * It is unclear how to specify NONE value (negative perhaps?).  MS Word does not allow for NONE.
     * @return the default tab stops, in points.
     */
    public double defaultStops() {
        return defaultStops;
    }
    
    // TODO hashCode, equals, toString
}
