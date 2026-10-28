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

/**
 * Describes the range of paragraphs in the model which can be collapsed (folded) in the view.
 *
 * @since 999 TODO
 */
public interface FoldRange {

    /**
     * Fold range start paragraph index.
     * @return the start index
     */
    public int getStart();

    /**
     * Fold range end paragraph index.
     * TODO last index of the folded range?
     * @return the end index
     */
    public int getEnd();

    /**
     * Provides the content to be displayed in place of the folded range.
     * A {@code null} value will be shown as a blank line.
     * @return the placeholder paragraph
     */
    public RichParagraph getPlaceholder();
}
