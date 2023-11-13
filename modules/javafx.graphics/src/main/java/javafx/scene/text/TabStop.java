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

/**
 * This class encapsulates a single tab stop.
 * A tab stop is at a specified distance from the
 * left margin, aligns text in a specified way, and has a specified leader.
 * TabStops are immutable, and usually contained in {@link TabStopPolicy}.
 */
public class TabStop {
    public enum Alignment {
        CENTER,
        LEFT,
        RIGHT,
        DECIMAL
        // TODO BAR?
    }
    
    public enum Leader {
        /** Lead none */
        NONE,
        /** Lead dots */
        DOTS,
        /** Lead hyphens */
        HYPHENS,
        /** Lead underline */
        UNDERLINE,
        /** Lead thickline */
        THICK_LINE,
        /** Lead equals */
        EQUALS
    }
    
    private final double position;
    private final Alignment alignment;
    private final Leader leader;

    // TODO this might be a record
    public TabStop(double position, Alignment alignment, Leader leader) {
        this.position = position;
        this.alignment = alignment;
        this.leader = leader;
    }
    
    /**
     * Returns the position, in points, of the tab.
     * @return the position of the tab
     */
    public double getPosition() {
        return position;
    }
    
    /**
     * Returns the alignment of the tab.
     * @return the alignment of the tab
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * Returns the leader of the tab.
     * @return the leader of the tab
     */
    public Leader getLeader() {
        return leader;
    }
    
    // TODO equals, toString
}
