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

package com.sun.javafx.incubator.scene.control.rich;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import javafx.incubator.scene.control.rich.StyleResolver;
import javafx.incubator.scene.control.rich.TextPos;
import javafx.incubator.scene.control.rich.model.DataFormatHandler;
import javafx.incubator.scene.control.rich.model.StyleAttribute;
import javafx.incubator.scene.control.rich.model.StyleAttrs;
import javafx.incubator.scene.control.rich.model.StyledInput;
import javafx.incubator.scene.control.rich.model.StyledOutput;
import javafx.incubator.scene.control.rich.model.StyledSegment;
import javafx.incubator.scene.control.rich.model.StyledTextModel;
import javafx.scene.input.DataFormat;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * DataFormatHandler for use with attribute-based rich text models.
 * <p>
 * The handler uses a simple text-based format:<p>
 * (*) denotes an optional element.
 * <pre>
 * PARAGRAPH[]
 * 
 * PARAGRAPH: {
 *     PARAGRAPH_ATTRIBUTE[]*,
 *     TEXT_SEGMENT[],
 *     "\n"
 * }
 * 
 * PARAGRAPH_ATTRIBUTE: {
 *     "{!"
 *     <name>
 *     ATTRIBUTE_VALUE[]*
 *     "}"
 * }
 *  
 * ATTRIBUTE: {
 *     "{"
 *     <name>
 *     ATTRIBUTE_VALUE[]*
 *     "}"
 * }
 * 
 * ATTRIBUTE_VALUE: {
 *     |
 *     (value)
 * }
 * 
 * TEXT_SEGMENT: {
 *     ATTRIBUTE[]*
 *     (text string with escaped special characters)
 * }
 * </pre>
 * Attribute sequences are further deduplicated, using a single {number} token
 * which specifies the index into the list of unique sets of attributes.
 * Paragraph attribute sets are treated as separate from the segment attrubite sets.  
 * <p>
 * The following characters are escaped in text segments: {,%,}
 * The escape format is %XX where XX is a hexadecimal value.
 * <p>
 * Example:
 * <pre>
 * {!rtl}{c|ff00ff}text{b}bold\n
 * {!0}{1}line 2\n
 * </pre>
 */
public class RichTextFormatHandler2 extends DataFormatHandler {
    // String -> Handler
    // StyleAttribute -> Handler
    private final HashMap<Object,Handler> handlers = new HashMap<>(64);

    /**
     * Constructs a new instance.
     * @param format the data format
     */
    public RichTextFormatHandler2(DataFormat format) {
        super(format);
        
        addHandlerBoolean(
            StyleAttrs.BOLD,
            "b");
        addHandler(
            StyleAttrs.BACKGROUND,
            "bg",
            (v) -> toHexColor(v),
            (s) -> parseHexColor(s));
        addHandlerString(
            StyleAttrs.BULLET,
            "bullet");
        addHandlerString(
            StyleAttrs.FONT_FAMILY,
            "ff");
        addHandler(
            StyleAttrs.FIRST_LINE_INDENT,
            "firstIndent",
            (v) -> String.valueOf(v),
            (s) -> Double.parseDouble(s.get(0)));
        addHandler(
            StyleAttrs.FONT_SIZE,
            "fs",
            (v) -> String.valueOf(v),
            (s) -> Double.parseDouble(s.get(0)));
        addHandlerBoolean(
            StyleAttrs.ITALIC,
            "i");
        addHandler(
            StyleAttrs.LINE_SPACING,
            "lineSpacing",
            (v) -> String.valueOf(v),
            (s) -> Double.parseDouble(s.get(0)));
        addHandlerBoolean(
            StyleAttrs.RIGHT_TO_LEFT,
            "rtl");
        addHandler(
            StyleAttrs.SPACE_ABOVE,
            "spaceAbove",
            (v) -> String.valueOf(v),
            (s) -> Double.parseDouble(s.get(0)));
        addHandler(
            StyleAttrs.SPACE_BELOW,
            "spaceBelow",
            (v) -> String.valueOf(v),
            (s) -> Double.parseDouble(s.get(0)));
        addHandler(
            StyleAttrs.SPACE_LEFT,
            "spaceLeft",
            (v) -> String.valueOf(v),
            (s) -> Double.parseDouble(s.get(0)));
        addHandler(
            StyleAttrs.SPACE_RIGHT,
            "spaceRight",
            (v) -> String.valueOf(v),
            (s) -> Double.parseDouble(s.get(0)));
        addHandlerBoolean(
            StyleAttrs.STRIKE_THROUGH,
            "ss");
        addHandler(
            StyleAttrs.TEXT_ALIGNMENT,
            "alignment",
            (v) -> encodeAlignment(v),
            (s) -> decodeAlignment(s.get(0)));
        addHandler(
            StyleAttrs.TEXT_COLOR,
            "tc",
            (v) -> toHexColor(v),
            (s) -> parseHexColor(s));
        addHandlerBoolean(
            StyleAttrs.UNDERLINE,
            "u");
    }

    @Override
    public StyledInput createStyledInput(Object src) {
        String input = (String)src;
        return new RichStyledInput(input);
    }

    @Override
    public Object copy(StyledTextModel m, StyleResolver r, TextPos start, TextPos end) throws IOException {
        StringWriter wr = new StringWriter();
        StyledOutput so = createStyledOutput(r, wr);
        m.exportText(start, end, so);
        return wr.toString();
    }

    @Override
    public void save(StyledTextModel m, StyleResolver r, TextPos start, TextPos end, OutputStream out) throws IOException {
        Charset cs = Charset.forName("utf-8");
        Writer wr = new OutputStreamWriter(out, cs);
        StyledOutput so = createStyledOutput(r, wr);
        m.exportText(start, end, so);
    }

    public StyledOutput createStyledOutput(StyleResolver r, Writer wr) {
        Charset cs = Charset.forName("utf-8");
        boolean buffered = isBuffered(wr);
        if (buffered) {
            return new RichStyledOutput(r, wr);
        } else {
            wr = new BufferedWriter(wr);
            return new RichStyledOutput(r, wr);
        }
    }

    private static boolean isBuffered(Writer x) {
        return
            (x instanceof BufferedWriter) ||
            (x instanceof StringWriter);
    }

    protected static Color parseHexColor(List<String> ss) {
        try {
            String s = ss.get(0);
            double alpha;
            switch(s.length()) {
            case 8:
                // rrggbbaa
                alpha = parseByte(s, 6) / 255.0;
                break;
            case 6:
                // rrggbb
                alpha = 1.0;
                break;
            default:
                throw new IOException("unable to parse color: " + s);
            }
                
            int r = parseByte(s, 0);
            int g = parseByte(s, 2);
            int b = parseByte(s, 4);
            return Color.rgb(r, g, b, alpha);
        } catch(Exception e) {
            // FIX remove try-catch once exception handling is done
            log(e);
            return Color.RED;
        }
    }

    protected static int parseByte(String text, int start) throws IOException {
        int v = parseHexChar(text.charAt(start)) << 4;
        v += parseHexChar(text.charAt(start + 1));
        return v;
    }

    private static int parseHexChar(int ch) throws IOException {
        int c = ch - '0'; // 0...9
        if ((c >= 0) && (c <= 9)) {
            return c;
        }
        c = ch - 55; // handle A...F
        if ((c >= 10) && (c <= 15)) {
            return c;
        }
        c = ch - 97; // handle a...f
        if ((c >= 10) && (c <= 15)) {
            return c;
        }
        throw new IOException("not a hex char:" + ch);
    }

    protected static String toHexColor(Color c) {
        return
            toHex8(c.getRed()) +
            toHex8(c.getGreen()) +
            toHex8(c.getBlue()) +
            ((c.getOpacity() == 1.0) ? "" : toHex8(c.getOpacity()));
    }

    private static String toHex8(double x) {
        int v = (int)Math.round(255.0 * x);
        if (v < 0) {
            v = 0;
        } else if (v > 255) {
            v = 255;
        }
        return String.format("%02X", v);
    }

    /** attribute handler TODO own class? */
    static abstract class Handler<T> {
        
        public abstract String getId();
        
        public abstract StyleAttribute<T> getStyleAttribute();
        
        public abstract String write(T value);
        
        public abstract T read(List<String> ss);
    }

    protected <T> void addHandler(StyleAttribute<T> a, String id, Function<T,String> wr, Function<List<String>,T> rd) {
        Handler<T> h = new Handler<>() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public StyleAttribute<T> getStyleAttribute() {
                return a;
            }

            @Override
            public String write(T value) {
                return wr.apply(value);
            }

            @Override
            public T read(List<String> ss) {
                return rd.apply(ss);
            }
        };
        handlers.put(a, h);
        handlers.put(id, h);
    }
    
    protected void addHandlerBoolean(StyleAttribute<Boolean> a, String id) {
        addHandler
        (
            a, 
            id,
            (v) -> v ? null : "F", // TODO should not serialize FALSE
            (s) -> s.size() == 0 ? Boolean.TRUE : (s.get(0).equals("F") ? Boolean.FALSE : Boolean.TRUE)
        );
    }
    
    protected void addHandlerString(StyleAttribute<String> a, String id) {
        // TODO handler variant that accepts a single argument
        addHandler
        (
            a, 
            id,
            (v) -> v,
            (s) -> s.get(0)
        );
    }
    
    private static void log(Object x) {
        System.err.println(x); // TODO platform logger or disable
    }
    
    private String encodeAlignment(TextAlignment a) {
        switch (a) {
        case CENTER:
            return "C";
        case JUSTIFY:
            return "J";
        case RIGHT:
            return "R";
        case LEFT:
        default:
            return "L";
        }
    }

    private TextAlignment decodeAlignment(String s) {
        switch (s) {
        case "C":
            return TextAlignment.CENTER;
        case "J":
            return TextAlignment.JUSTIFY;
        default:
            // lambda does not support exceptions, FIX when public AttributeHandler is created
            // throw new IOException("failed parsing alignment (" + s + ")");
            // fall-through
        case "L":
            return TextAlignment.LEFT;
        case "R":
            return TextAlignment.RIGHT;
        }
    }

    /** exporter */
    private class RichStyledOutput implements StyledOutput {
        private final StyleResolver resolver;
        private final Writer wr;
        private HashMap<StyleAttrs, Integer> styles = new HashMap<>();

        public RichStyledOutput(StyleResolver resolver, Writer wr) {
            // TODO use caching resolver?
            this.resolver = resolver;
            this.wr = wr;
        }

        @Override
        public void append(StyledSegment seg) throws IOException {
            System.out.println(seg); // FIX
            switch (seg.getType()) {
            case INLINE_NODE:
                // TODO
                log("ignoring embedded node");
                break;
            case LINE_BREAK:
                wr.write("\n");
                break;
            case PARAGRAPH_ATTRIBUTES:
                {
                    StyleAttrs attrs = seg.getStyleAttrs(resolver);
                    emitAttributes(attrs, true);
                }
                break;
            case REGION:
                // TODO
                break;
            case TEXT:
                {
                    StyleAttrs attrs = seg.getStyleAttrs(resolver);
                    emitAttributes(attrs, false);
     
                    String text = seg.getText();
                    text = encode(text);
                    wr.write(text);
                }
                break;
            }
        }

        private void emitAttributes(StyleAttrs attrs, boolean forParagraph) throws IOException {
            if ((attrs != null) && (!attrs.isEmpty())) {
                Integer num = styles.get(attrs);
                if (num == null) {
                    // new style, gets numbered and added to the cache
                    int sz = styles.size();
                    styles.put(attrs, Integer.valueOf(sz));

                    ArrayList<StyleAttribute<?>> as = new ArrayList<>(attrs.getAttributes());
                    // sort by name to make serialized output stable
                    // the overhead is very low since this is done once per style
                    Collections.sort(as, new Comparator<StyleAttribute<?>>() {
                        @Override
                        public int compare(StyleAttribute<?> a, StyleAttribute<?> b) {
                            String sa = a.getName();
                            String sb = b.getName();
                            return sa.compareTo(sb);
                        }
                    });

                    for (StyleAttribute<?> a : as) {
                        Handler h = handlers.get(a);
                        try {
                            if (h != null) {
                                Object v = attrs.get(a);
                                wr.write('{');
                                if (forParagraph) {
                                    wr.write('!');
                                }
                                wr.write(h.getId());
                                String ss = h.write(v);
                                if (ss != null) {
                                    wr.write('|');
                                    wr.write(encode(ss));
                                }
                                wr.write('}');
                                continue;
                            }
                        } catch (Exception e) {
                            log(e);
                        }
                        // ignoring this attribute
                        log("failed to emit " + a + ", skipping");
                    }
                } else {
                    // cached style, emit the id
                    wr.write('{');
                    if (forParagraph) {
                        wr.write('!');
                    }
                    wr.write(String.valueOf(num));
                    wr.write('}');
                }
            }
        }

        private static String encode(String text) {
            if (text == null) {
                return "";
            }

            int ix = indexOfSpecialChar(text);
            if (ix < 0) {
                return text;
            }

            int len = text.length();
            StringBuilder sb = new StringBuilder(len + 32);
            if (ix > 0) {
                sb.append(text.substring(0, ix));
            }

            for (int i = ix; i < len; i++) {
                char c = text.charAt(i);
                if (isSpecialChar(c)) {
                    sb.append(String.format("%%%02X", (int)c));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private static int indexOfSpecialChar(String text) {
            int len = text.length();
            for (int i = 0; i < len; i++) {
                char c = text.charAt(i);
                if (isSpecialChar(c)) {
                    return i;
                }
            }
            return -1;
        }

        private static boolean isSpecialChar(char c) {
            switch (c) {
            case '{':
            case '}':
            case '%':
                return true;
            }
            return false;
        }

        @Override
        public void flush() throws IOException {
            wr.flush();
        }

        @Override
        public void close() throws IOException {
            wr.close();
        }
    }

    /** importer */
    private class RichStyledInput implements StyledInput {
        private final String text;
        private int index;
        private StringBuilder sb;
        private final ArrayList<StyleAttrs> styles = new ArrayList<>();
        private final ArrayList<String> parts = new ArrayList<>(4);

        public RichStyledInput(String text) {
            // TODO buffered input and line-by-line
            this.text = text;
        }

        @Override
        public StyledSegment nextSegment() {
            try {
                int c = charAt(0);
                switch (c) {
                case -1:
                    return null;
                case '\n':
                    index++;
                    return StyledSegment.LINE_BREAK;
                case '{':
                    StyleAttrs a = parseAttributes(true);
                    if (a != null) {
                        return StyledSegment.ofParagraphAttributes(a);
                    } else {
                        a = parseAttributes(false);
                        String text = decodeText();
                        return StyledSegment.of(text, a);
                    }
                }
                String text = decodeText();
                return StyledSegment.of(text);
            } catch (IOException e) {
                log(e);
                return null;
            }
        }

        @Override
        public void close() throws IOException {
        }

        private StyleAttrs parseAttributes(boolean forParagraph) throws IOException {
            StyleAttrs.Builder b = null;
            for (;;) {
                int c = charAt(0);
                if (c != '{') {
                    break;
                }
                c = charAt(1);
                if (forParagraph) {
                    if (c == '!') {
                        index++;
                    } else {
                        break;
                    }
                } else {
                    if (c == '!') {
                        throw err("unexpected paragraph attribute");
                    }
                }
                index++;
                
                int ix = text.indexOf('}', index);
                if(ix < 0) {
                    throw err("missing }");
                }
                String s = text.substring(index, ix);
                if(s.length() == 0) {
                    throw err("empty attribute name");
                }
                int n = parseStyleNumber(s);
                if(n < 0) {
                    RichUtils.split(parts, s, '|');
                    if (parts.size() == 0) {
                        throw err("missing attribute name");
                    }
                    // parse the attribute
                    String name = parts.remove(0);
                    Handler h = handlers.get(name);
                    if(h == null) {
                        // silently ignore the attribute
                        log("ignoring attribute: " + name);
                    } else {
                        Object v = h.read(parts);
                        StyleAttribute a = h.getStyleAttribute();
                        if (a.isParagraphAttribute() != forParagraph) {
                            throw err("paragraph type mismatch");
                        }
                        if(b == null) {
                            b = StyleAttrs.builder();
                        }
                        b.set(a, v);
                    }
                    index = ix + 1;
                } else {
                    index = ix + 1;
                    // get style from cache
                    return styles.get(n);
                }
            }
            if (b == null) {
                return null;
            }
            StyleAttrs attrs = b.build();
            styles.add(attrs);
            return attrs;
        }

        private int charAt(int delta) {
            int ix = index + delta;
            if(ix >= text.length()) {
                return -1;
            }
            return text.charAt(ix);
        }
        
        private String decodeText() throws IOException {
            int start = index;
            for(;;) {
                int c = charAt(0);
                switch(c) {
                case '\n':
                case '{':
                case -1:
                    return text.substring(start, index);
                case '%':
                    return decodeText(start, index);
                }
                index++;
            }
        }

        private String decodeText(int start, int ix) throws IOException {
            if (sb == null) {
                sb = new StringBuilder();
            }
            if (ix > start) {
                sb.append(text, start, ix);
            }
            for (;;) {
                int c = charAt(0);
                switch (c) {
                case '\n':
                case '{':
                case -1:
                    String s = sb.toString();
                    sb.setLength(0);
                    return s;
                case '%':
                    index++;
                    int ch = decodeHexByte();
                    sb.append((char)ch);
                    break;
                }
                index++;
            }
        }

        private int decodeHexByte() throws IOException {
            int ch = decodeHex(charAt(0)) << 4;
            index++;
            ch += decodeHex(charAt(0));
            return ch;
        }
        
        private static int decodeHex(int ch) throws IOException {
            int c = ch - '0'; // 0...9
            if((c >= 0) && (c <= 9)) {
                return c;
            }
            c = ch - 55; // handle A...F
            if((c >= 10) && (c <= 15)) {
                return c;
            }
            c = ch - 97; // handle a...f
            if((c >= 10) && (c <= 15)) {
                return c;
            }
            throw new IOException("not a hex char:" + ch);
        }

        private int parseStyleNumber(String s) throws IOException {
            if (Character.isDigit(s.charAt(0))) {
                int n;
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    throw err("invalid style number " + s);
                }
            }
            return -1;
        }

        private IOException err(String text) {
            // TODO specify line number once converted to stream
            return new IOException("malformed input: " + text); 
        }
    }
}
