/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.demo.richtext.common;

import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.model.StyleAttribute;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;

/**
 * Standard Styles.
 */
public class Styles {
    // TODO perhaps we should specifically set fonts to be used,
    // and couple that with the app stylesheet
    public static final StyleAttributeMap TITLE = mkStyle("System", 24, true);
    public static final StyleAttributeMap HEADING = mkStyle("System", 18, true);
    public static final StyleAttributeMap SUBHEADING = mkStyle("System", 14, true);
    public static final StyleAttributeMap BODY = mkStyle("System", 12, false);
    public static final StyleAttributeMap MONOSPACED = mkStyle("Monospace", 12, false);

    private static final StyleAttribute<?>[] STD_ATTRS = {
        StyleAttributeMap.BOLD,
        StyleAttributeMap.FONT_FAMILY,
        StyleAttributeMap.FONT_SIZE,
        StyleAttributeMap.TEXT_COLOR,
    };

    private static StyleAttributeMap mkStyle(String font, double size, boolean bold) {
        return StyleAttributeMap.builder().
            setFontFamily(font).
            setFontSize(size).
            setBold(bold).
            setTextColor(Color.BLACK).
            build();
    }

    public static StyleAttributeMap getStyleAttributeMap(TextStyle st) {
        switch (st) {
        case BODY:
            return BODY;
        case HEADING:
            return HEADING;
        case MONOSPACED:
            return MONOSPACED;
        case TITLE:
            return TITLE;
        case SUBHEADING:
            return SUBHEADING;
        default:
            return BODY;
        }
    }

    public static TextStyle guessTextStyle(StyleAttributeMap attrs) {
        if (attrs != null) {
            if (attrs.isEmpty()) {
                return TextStyle.BODY;
            }
            for (TextStyle st : TextStyle.values()) {
                StyleAttributeMap a = getStyleAttributeMap(st);
                if (match(attrs, a, STD_ATTRS)) {
                    return st;
                }
            }
        }
        return null;
    }

    private static boolean match(StyleAttributeMap attrs, StyleAttributeMap builtin, StyleAttribute<?>[] keys) {
        for (StyleAttribute<?> k : keys) {
            Object v1 = attrs.get(k);
            Object v2 = builtin.get(k);
            if (k.getType() == Boolean.class) {
                if (getBoolean(v1) != getBoolean(v2)) {
                    return false;
                }
            } else {
                if (!eq(v1, v2)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean getBoolean(Object x) {
        return Boolean.TRUE.equals(x);
    }

    private static boolean eq(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
