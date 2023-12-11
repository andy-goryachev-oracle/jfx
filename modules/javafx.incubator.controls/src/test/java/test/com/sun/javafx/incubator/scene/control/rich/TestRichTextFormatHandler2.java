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

package test.com.sun.javafx.incubator.scene.control.rich;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javafx.incubator.scene.control.rich.model.StyleAttribute;
import javafx.incubator.scene.control.rich.model.StyleAttrs;
import javafx.incubator.scene.control.rich.model.StyledInput;
import javafx.incubator.scene.control.rich.model.StyledOutput;
import javafx.incubator.scene.control.rich.model.StyledSegment;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.sun.javafx.incubator.scene.control.rich.RichTextFormatHandler2;

/**
 * Tests RichTextFormatHandler2.
 */
public class TestRichTextFormatHandler2 {
    private static final boolean DEBUG = true;

    @Test
    public void testRoundTrip() throws IOException {
        Object[] ss = {
            List.of(
                s("background", a(StyleAttrs.BACKGROUND, Color.RED)), nl(),
                s("bold", StyleAttrs.BOLD), nl(),
                s("italic/color/underline", StyleAttrs.ITALIC, a(StyleAttrs.TEXT_COLOR, Color.RED), StyleAttrs.UNDERLINE), nl()
            )
        };

        RichTextFormatHandler2 handler = new RichTextFormatHandler2(null);

        for (Object x : ss) {
            testRoundTrip(handler, (List<StyledSegment>)x);
        }
    }

    private static StyledSegment s(String text, Object... items) {
        StyleAttrs.Builder b = StyleAttrs.builder();
        for (Object x : items) {
            if (x instanceof StyleAttribute a) {
                b.set(a, Boolean.TRUE);
            } else if (x instanceof StyleAttrs a) {
                b.merge(a);
            } else {
                throw new Error("?" + x);
            }
        }
        StyleAttrs a = b.build();
        return StyledSegment.of(text, a);
    }

    private static <T> StyleAttrs a(StyleAttribute<T> a, T value) {
        return StyleAttrs.builder().set(a, value).build();
    }

    private static StyledSegment nl() {
        return StyledSegment.LINE_BREAK;
    }

    private void testRoundTrip(RichTextFormatHandler2 handler, List<StyledSegment> input) throws IOException {
        // export to string
        int ct = 0;
        StringWriter wr = new StringWriter();
        StyledOutput out = handler.createStyledOutput(null, wr);
        for (StyledSegment s : input) {
            if (DEBUG) {
                System.out.println(s);
            }
            out.append(s);
            ct++;
        }
        out.flush();
        String exported = wr.toString();
        if (DEBUG) {
            System.out.println("exported " + ct + " segments=" + exported);
        }

        // import from string
        ArrayList<StyledSegment> segments = new ArrayList<>();
        StyledInput in = handler.createStyledInput(exported);
        StyledSegment seg;
        while ((seg = in.nextSegment()) != null) {
            if (DEBUG) {
                System.out.println(seg);
            }
            segments.add(seg);
        }

        // check segments for equality
        Assertions.assertEquals(input.size(), segments.size());
        for (int i = 0; i < input.size(); i++) {
            StyledSegment is = input.get(i);
            StyledSegment rs = segments.get(i);
            Assertions.assertEquals(is.getType(), rs.getType());
            Assertions.assertEquals(is.getText(), rs.getText());
            Assertions.assertEquals(is.getStyleAttrs(null), rs.getStyleAttrs(null));
        }

        // export to a string again
        wr = new StringWriter();
        out = handler.createStyledOutput(null, wr);
        for (StyledSegment s : segments) {
            out.append(s);
        }
        out.flush();
        String result = wr.toString();
        if (DEBUG) {
            System.out.println("result=" + result);
        }

        // relying on stable order of attributes
        Assertions.assertEquals(exported, result);
    }

    private void testRoundTrip_DELETE(RichTextFormatHandler2 handler, String text) throws IOException {
        ArrayList<StyledSegment> segments = new ArrayList<>();

        StyledInput in = handler.createStyledInput(text);
        StyledSegment seg;
        while ((seg = in.nextSegment()) != null) {
            segments.add(seg);
            if (DEBUG) {
                System.out.println(seg);
            }
        }

        StringWriter wr = new StringWriter();
        StyledOutput out = handler.createStyledOutput(null, wr);
        for (StyledSegment s : segments) {
            out.append(s);
        }
        out.flush();

        String result = wr.toString();
        Assertions.assertEquals(text, result);
    }
}
