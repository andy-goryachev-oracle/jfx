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

package com.oracle.demo.rich.codearea;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches java keywords.
 */
public class JavaKeywords {
    private static Pattern KEYWORDS;
    private static Set<Integer> CHARS;
    private final String text;
    private final Matcher matcher;
    static { init(); }

    public JavaKeywords(String text) {
        this.text = text;
        this.matcher = KEYWORDS.matcher(text);
    }

    private static void init() {
        String[] keywords = {
            "abstract",
            "assert",
            "boolean",
            "break",
            "byte",
            "case",
            "catch",
            "char",
            "class",
            "const",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "enum",
            "extends",
            "final",
            "finally",
            "float",
            "for",
            "goto",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "long",
            "native",
            "new",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "short",
            "static",
            "strictfpv",
            "super",
            "switch",
            "synchronized",
            "this",
            "throw",
            "throws",
            "transient",
            "try",
            "void",
            "volatile",
            "while"
        };

        HashSet<Integer> chars = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        boolean sep = false;
        for (String k : keywords) {
            if (sep) {
                sb.append("|");
            } else {
                sep = true;
            }
            sb.append("\\G"); // match at start of the input in match(pos);
            //sb.append("\\b("); // word boundary + capturing group
            sb.append("("); // capturing group
            sb.append(k);
            sb.append(")\\b"); // capturing group + word boundary

            chars.add(Integer.valueOf(k.charAt(0)));
        }

        KEYWORDS = Pattern.compile(sb.toString());
        CHARS = chars;
    }

    /**
     * Matches java keyword at the specified index.
     * @param index the offset into text
     * @return the length of java keyword, 0 if not a java keyword
     */
    public int matchJavaKeyword(int index) {
        int c = text.charAt(index);
        if (CHARS.contains(c)) {
            if (matcher.find(index)) {
                int start = matcher.start();
                int end = matcher.end();
                return (end - start);
            }
        }
        return 0;
    }
}
