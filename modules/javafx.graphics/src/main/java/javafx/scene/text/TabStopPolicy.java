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
 */
public interface TabStopPolicy {
    /**
     * Determines whether this policy specifies a fixed tab size in terms of the width or the digit 0
     * (any positive value), or provides the tab stops relative to document leading edge.
     *
     * @return the tab size
     */
    public int tabSize();

    /**
     * @return the non-null list of tab stops 
     */
    public List<TabStop> tabStops();
    
    /**
     * First line indent, a positive value or 0.
     * This value is ignored when {@link #tabSize()} returns a non-zero value.
     *
     * TODO
     * It is unclear whether the TextLayout should support negative values as it might impact the size and
     * the preferred size of the layout.
     *
     * @return the first line indent
     */
    public double firstLineIndent();

    /**
     * Provides default tab stops (beyond the last tab stop specified by {@code #tabStops()}.
     * This value is ignored when {@link #tabSize()} returns a non-zero value.
     *
     * TODO
     * It is unclear how to specify NONE value (negative perhaps?).  MS Word does not allow for NONE.
     * @return the default tab stops, in points.
     */
    public double defaultStops();
    
    // TODO: factory method to create a simple fixed tab size policy
}
