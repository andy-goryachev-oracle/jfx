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
import java.util.HashMap;
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
// TODO problem: no paragraph attr - {} is required.  or {!} {!0}
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
            StyleAttrs.FONT_SIZE,
            "fs",
            (v) -> new String[] { String.valueOf(v) },
            (s) -> Double.parseDouble(s[0]));
        addHandlerBoolean(
            StyleAttrs.ITALIC,
            "i");
        addHandler(
            StyleAttrs.LINE_SPACING,
            "lineSpacing",
            (v) -> new String[] { String.valueOf(v) },
            (s) -> Double.parseDouble(s[0]));
        addHandlerBoolean(
            StyleAttrs.RIGHT_TO_LEFT,
            "rtl");
        addHandler(
            StyleAttrs.SPACE_ABOVE,
            "spaceAbove",
            (v) -> new String[] { String.valueOf(v) },
            (s) -> Double.parseDouble(s[0]));
        addHandler(
            StyleAttrs.SPACE_BELOW,
            "spaceBelow",
            (v) -> new String[] { String.valueOf(v) },
            (s) -> Double.parseDouble(s[0]));
        addHandler(
            StyleAttrs.SPACE_LEFT,
            "spaceLeft",
            (v) -> new String[] { String.valueOf(v) },
            (s) -> Double.parseDouble(s[0]));
        addHandler(
            StyleAttrs.SPACE_RIGHT,
            "spaceRight",
            (v) -> new String[] { String.valueOf(v) },
            (s) -> Double.parseDouble(s[0]));
        addHandlerBoolean(
            StyleAttrs.STRIKE_THROUGH,
            "ss");
        addHandler(
            StyleAttrs.TEXT_ALIGNMENT,
            "alignment",
            (v) -> new String[] { encodeAlignment(v) },
            (s) -> decodeAlignment(s[0]));
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

    private StyledOutput createStyledOutput(StyleResolver r, Writer wr) {
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

    protected static Color parseHexColor(String[] ss) {
        String s = ss[0];
        switch(s.length()) {
        case 6:
            // rrggbb
        case 8:
            // rrggbbaa
        default:
            // TODO exception
            return Color.RED;
        }
    }
    
    protected static String[] toHexColor(Color c) {
        return new String[] {
            toHex8(c.getRed()) +
            toHex8(c.getGreen()) +
            toHex8(c.getBlue()) +
            ((c.getOpacity() == 1.0) ? "" : toHex8(c.getOpacity()))
        };
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
        
        public abstract String[] write(T value);
        
        public abstract T read(String[] ss);
    }

    protected <T> void addHandler(StyleAttribute<T> a, String id, Function<T,String[]> wr, Function<String[],T> rd) {
        Handler<T> h = new Handler<>() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String[] write(T value) {
                return wr.apply(value);
            }

            @Override
            public T read(String[] ss) {
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
            (v) -> v ? null : new String[] { "F" },
            null // TODO
        );
    }
    
    protected void addHandlerString(StyleAttribute<String> a, String id) {
        // TODO handler variant that accepts a single argument
        addHandler
        (
            a, 
            id,
            (v) -> new String[] { v },
            (s) -> s[0]
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
            this.resolver = resolver;
            this.wr = wr;
        }

        @Override
        public void append(StyledSegment seg) throws IOException {
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
                    // TODO use caching resolver?
                    StyleAttrs attrs = seg.getStyleAttrs(resolver);
                    emitAttributes(attrs, true);
                }
                break;
            case REGION:
                // TODO
                break;
            case TEXT:
                {
                    // TODO use caching resolver?
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

                    for (StyleAttribute<?> a : attrs.getAttributes()) {
                        Handler h = handlers.get(a);
                        try {
                            if (h != null) {
                                Object v = attrs.get(a);
                                wr.write('{');
                                if (forParagraph) {
                                    wr.write('!');
                                }
                                wr.write(h.getId());
                                String[] ss = h.write(v);
                                if (ss != null) {
                                    for (String s : ss) {
                                        wr.write('|');
                                        wr.write(encode(s));
                                    }
                                }
                                wr.write('}');
                                continue;
                            }
                        } catch (Exception e) {
                            log(e);
                        }
                        // ignoring this attribute
                        log("failed to write " + a);
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
    private static class RichStyledInput implements StyledInput {
        private final String text;
        private int index;
        private StringBuilder sb;
        private final ArrayList<StyleAttrs> attrs = new ArrayList<>();

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
                    index++;
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

        private StyleAttrs parseAttributes(boolean forParagraph) {
            // TODO
            return null;
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

        private int decodeInt() throws IOException {
            int v = 0;
            int ct = 0;
            for(;;) {
                int c = charAt(0);
                int d = Character.digit(c, 10);
                if(d < 0) {
                    if(ct == 0) {
                        throw new IOException("missing number index=" + index);
                    }
                    return v;
                } else {
                    v = v * 10 + d;
                    ct++;
                }
                index++;
            }
        }

        // FIX remove
        @Deprecated
        private double decodeDouble() throws IOException {
            String payload = decodePayload();
            try {
                return Double.parseDouble(payload);
            } catch(NumberFormatException e) {
                throw new IOException("expecting double: " + payload, e);
            }
        }

        // FIX remove
        @Deprecated
        private String decodePayload() throws IOException {
            int start = index;
            int i = 0;
            for(;;) {
                int c = charAt(i);
                switch(c) {
                case -1:
                    throw new IOException("unexpected end of token");
                case '`':
                    index = start + i;
                    return text.substring(start, index);
                }
                i++;
            }
        }

        @Override
        public void close() throws IOException {
        }
    }
}
