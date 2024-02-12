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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple Java syntax analyzer.
 * This is just a demo, as it has no link to the real compiler, does not understand Java language
 * and does not take into account version-specific language features.
 */
public class JavaSyntaxAnalyzer {
    private boolean DEBUG = !false;

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
        
        @Override
        public boolean equals(Object x) {
            if (x == this) {
                return true;
            } else if (x instanceof Line n) {
                return segments.equals(n.segments);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return 0; // we only need equals, don't put a hash table!
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
        
        @Override
        public boolean equals(Object x) {
            if (x == this) {
                return true;
            } else if (x instanceof Segment s) {
                return
                    (type == s.type) &&
                    (text.equals(s.text));
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return 0; // we only need equals, don't put a hash table!
        }
    }
    
    public enum Type {
        CHARACTER,
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
        KEYWORD,
        OTHER,
        STRING,
        TEXT_BLOCK,
        WHITESPACE,
    }

    private static final int EOF = -1;
    private static Pattern KEYWORDS;
    private static Set<Integer> FIRST;
    private static Pattern CHARS;
    static { init(); }

    private final String text;
    private final Matcher keywordMatcher;
    private final Matcher charsMatcher;
    private int pos;
    private int start;
    private boolean blockComment;
    private State state = State.OTHER;
    private int tokenLength;
    private ArrayList<Line> lines;
    private Line currentLine;

    public JavaSyntaxAnalyzer(String text) {
        this.text = text;
        this.keywordMatcher = KEYWORDS.matcher(text);
        this.charsMatcher = CHARS.matcher(text);
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
        FIRST = chars;

        String charsPattern =
            "(\\G\\\\[bfnrt'\"\\\\]')|" +  // \b' + \f' + \n' + \r' + \t' + \'' + \"' +  \\'
            "(\\G[^\\\\u]')|" + // any char followed by ', except u and \
            "(\\G\\\\u[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]')" // unicode escapes
            ;
        CHARS = Pattern.compile(charsPattern);
    }
    
    // returns the length of java keyword, 0 if not a java keyword
    private int matchJavaKeyword() {
        int c = charAt(0);
        if (FIRST.contains(c)) {
            if (keywordMatcher.find(pos)) {
                int start = keywordMatcher.start();
                int end = keywordMatcher.end();
                return (end - start);
            }
        }
        return 0;
    }

    // returns the length of the character, or 0 if not a character
    private int matchCharacter() {
        if(charsMatcher.find(pos + 1)) {
            int start = charsMatcher.start();
            int end = charsMatcher.end();
            return (end - start);
        }
        return 0;
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
        case KEYWORD:
            return Type.KEYWORD;
        case OTHER:
            return Type.OTHER;
        case STRING:
            return Type.STRING;
        case TEXT_BLOCK:
            return Type.STRING;
        case WHITESPACE:
            return Type.OTHER;
        default:
            throw new Error("?" + s);
        }
    }

    private void addSegment() {
        Type type = type(state);
        addSegment(type);
    }

    private void addSegment(Type type) {
        if (pos > start) {
            String s = text.substring(start, pos);
            
            if (currentLine == null) {
                currentLine = new Line();
            }
            currentLine.addSegment(type, s);
            
            start = pos;
            if(DEBUG) System.out.println("  " + type + ":[" + s + "]"); // FIX
        }
    }

    private void addNewLine() {
        if (currentLine == null) {
            currentLine = new Line();
        }
        lines.add(currentLine);
        currentLine = null;
        if(DEBUG) System.out.println("  <NL>"); // FIX
    }

    private boolean match(String pattern) {
        for (int i = 0; i < pattern.length(); i++) {
            if (charAt(i) != pattern.charAt(i)) {
                return false;
            }
        }
        tokenLength = pattern.length();
        return true;
    }

    // relative to 'pos'
    private int charAt(int ix) {
        ix += pos;
        if ((ix >= 0) && (ix < text.length())) {
            return text.charAt(ix);
        }
        return EOF;
    }
    
    public List<Line> analyze() {
        if(DEBUG) System.out.println("analyze"); // FIX
        lines = new ArrayList<>();
        start = 0;

        for (;;) {
            tokenLength = 0;
            int c = peek();
            
            switch (c) {
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
                case STRING:
                case TEXT_BLOCK:
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
                break;
            case '"':
                switch(state) {
                case COMMENT_BLOCK:
                case COMMENT_LINE:
                    break;
                case STRING:
                    pos++;
                    addSegment();
                    state = State.OTHER;
                    continue;
                default:
                    if(match("\"\"\"")) {
                        addSegment();
                        pos += tokenLength;
                        state = State.TEXT_BLOCK;
                        continue;
                    } else {
                        addSegment();
                        state = State.STRING;
                    }
                    break;
                }
                break;
            case '\'':
                switch(state) {
                case COMMENT_BLOCK:
                case COMMENT_LINE:
                case STRING:
                    break;
                default:
                    switch (charAt(1)) {
                    case '\n':
                        pos++;
                        continue;
                    }
                    tokenLength = matchCharacter();
                    if (tokenLength > 0) {
                        addSegment();
                        pos += (tokenLength + 1);
                        addSegment(Type.CHARACTER);
                        state = State.OTHER;
                        continue;
                    }
                    break;
                }
                break;
            case '\\':
                switch (state) {
                case STRING:
                    switch (charAt(1)) {
                    case '\n':
                        break;
                        // FIX breaks around "\"/*...
                    default:
                        pos++;
                        continue;
                    }
                    break;
                }
                break;
            default:
                switch (state) {
                case OTHER:
                    tokenLength = matchJavaKeyword();
                    if (tokenLength > 0) {
                        addSegment();
                        pos += tokenLength;
                        state = State.KEYWORD;
                        addSegment();
                        state = State.OTHER;
                        continue;
                    }
                    break;
                default:
                    int x = 5; // FIX
                    break;
                }
                break;
            }
            
            pos++;
        }
    }
    
    // TODO text blocks
    // TODO ints, longs, doubles
}
