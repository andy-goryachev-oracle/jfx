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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A simple Java syntax analyzer.
 * This is just a demo, as it has no link to the real compiler, does not understand Java language
 * and does not take into account version-specific language features.
 */
public class JavaSyntaxAnalyzer {
    public static class Line {
        private ArrayList<Segment> segments = new ArrayList<>();

        public void addSegment(Type type, String text) {
            segments.add(new Segment(type, text));
        }
        
        public List<Segment> getSegments() {
            return segments;
        }
        
        @Override
        public String toString() {
            return segments.toString();
        }
    }
    
    public static class Segment {
        private final Type type;
        private final String text;
        
        public Segment(Type type, String text) {
            this.type = type;
            this.text = text;
        }

        public Type getType() {
            return type;
        }

        public String getText() {
            return text;
        }
        
        @Override
        public String toString() {
            return type + ":[" + text + "]";
        }
    }
    
    public enum Type {
        COMMENT,
        KEYWORD,
        NUMBER,
        OTHER,
        STRING,
    }
    
    enum State {
        COMMENT_BLOCK,
        COMMENT_LINE,
        EOF,
        EOL,
        OTHER,
        WHITESPACE,
    }

    private static final Pattern PATTERNS = initPattern();
    private static final int EOF = -1;
    private final String text;
    private int pos;
    private int start;
    private boolean blockComment;
    private State state = State.OTHER;
    private int tokenLength;
    private ArrayList<Line> lines;
    private Line currentLine;

    public JavaSyntaxAnalyzer(String text) {
        this.text = text;
    }

    private static Pattern initPattern() {
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

        StringBuilder sb = new StringBuilder();
        // digits
        sb.append("(\\b\\d+\\b)");

        // keywords
        for (String k : keywords) {
            sb.append("|\\b(");
            sb.append(k);
            sb.append(")\\b");
        }
        return Pattern.compile(sb.toString());
    }

    private int peek() {
        if (pos < text.length()) {
            return text.charAt(pos);
        }
        return EOF;
    }

    private Type type(State s) {
        switch(s) {
        case COMMENT_BLOCK:
            return Type.COMMENT;
        case COMMENT_LINE:
            return Type.COMMENT;
        case EOF:
            return Type.OTHER;
        case EOL:
            return Type.OTHER;
        case OTHER:
            return Type.OTHER;
        case WHITESPACE:
            return Type.OTHER;
        default:
            throw new Error("?" + s);
        }
    }

    private void addSegment() {
        if (pos > start) {
            String s = text.substring(start, pos);
            Type type = type(state);
            
            if (currentLine == null) {
                currentLine = new Line();
            }
            currentLine.addSegment(type, s);
            
            start = pos;
            System.out.println("  " + type + ":[" + s + "]"); // FIX
        }
    }

    private void addNewLine() {
        if (currentLine == null) {
            currentLine = new Line();
        }
        lines.add(currentLine);
        currentLine = null;
        System.out.println("  <NL>"); // FIX
    }

    private boolean match(String pattern) {
        for (int i = 0; i < pattern.length(); i++) {
            if (charAt(pos + i) != pattern.charAt(i)) {
                return false;
            }
        }
        tokenLength = pattern.length();
        return true;
    }
    
    private int charAt(int ix) {
        if (ix < text.length()) {
            return text.charAt(ix);
        }
        return EOF;
    }

    public List<Line> analyze() {
        System.out.println("analyze"); // FIX
        lines = new ArrayList<>();
        start = 0;

        for (;;) {
            tokenLength = 0;
            int c = peek();
            
            switch(c) {
            case EOF:
                addSegment();
                if (currentLine != null) {
                    lines.add(currentLine);
                }
                if (lines.size() == 0) {
                    lines.add(new Line());
                }
                return lines;
            case '*':
                switch (state) {
                case COMMENT_BLOCK:
                    if (match("*/")) {
                        pos += tokenLength;
                        addSegment();
                        state = State.OTHER;
                        continue;
                    }
                }
                break;
            case '\n':
                addSegment();
                addNewLine();
                pos++;
                start = pos;
                switch (state) {
                case COMMENT_BLOCK:
                    break;
                default:
                    state = State.OTHER;
                    break;
                }
                continue;
            case '/':
                switch(state) {
                case COMMENT_BLOCK:
                case COMMENT_LINE:
                    break;
                default:
                    if (match("/*")) {
                        addSegment();
                        pos += tokenLength;
                        state = State.COMMENT_BLOCK;
                        continue;
                    } else if (match("//")) {
                        addSegment();
                        pos += tokenLength;
                        state = State.COMMENT_LINE;
                        continue;
                    }
                }
            }
            
            pos++;
        }
    }
}
