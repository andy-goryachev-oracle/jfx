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

package test.com.oracle.demo.rich.codearea;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.oracle.demo.rich.codearea.JavaSyntaxAnalyzer;

public class TestJavaSyntaxDecorator {
    private static final JavaSyntaxAnalyzer.Type H = JavaSyntaxAnalyzer.Type.CHARACTER;
    private static final JavaSyntaxAnalyzer.Type C = JavaSyntaxAnalyzer.Type.COMMENT;
    private static final JavaSyntaxAnalyzer.Type K = JavaSyntaxAnalyzer.Type.KEYWORD;
    private static final JavaSyntaxAnalyzer.Type N = JavaSyntaxAnalyzer.Type.NUMBER;
    private static final JavaSyntaxAnalyzer.Type O = JavaSyntaxAnalyzer.Type.OTHER;
    private static final JavaSyntaxAnalyzer.Type S = JavaSyntaxAnalyzer.Type.STRING;
    private static final Object NL = new Object();

    @Test
    public void tests() {
        // TODO

        // strings
        t(O, " ", S, "\"\\\"/*\\\"\"", NL);
        t(S, "\"\\\"\\\"\\\"\"", O, " {", NL);
        t(S, "\"abc\"", NL, O, "s = ", S, "\"\"");
        
        // comments
        t(O, " ", C, "/* yo", NL, C, "yo yo", NL, C, " */", O, " ");
        t(O, " ", C, "// yo yo", NL, K, "int", O, " c;");
        t(C, "/* // yo", NL, C, "// */", O, " ");
        
        // chars
        t(H, "'\\b'");
        t(H, "'\\b'", NL);
        t(H, "'\\u0000'", NL, H, "'\\uFf9a'", NL );
        t(H, "'a'", NL, H, "'\\b'", NL, H, "'\\f'", NL, H, "'\\n'", NL, H, "'\\r'", NL);
        t(H, "'\\''", NL, H, "'\\\"'", NL, H, "'\\\\'", NL );
        // keywords
        t(K, "package", O, " java.com;", NL);
        t(K, "import", O, " java.util.ArrayList;", NL);
        t(K, "import", O, " java.util.ArrayList;", NL, K, "import", O, " java.util.ArrayList;", NL);
        // misc
        t(K, "if", O, "(", S, "\"/*\"", O, " == null) {", NL);
        t(C, "// test", NL, O, "--", NL);
    }

    private void t(Object... items) {
        StringBuilder sb = new StringBuilder();
        ArrayList<JavaSyntaxAnalyzer.Line> expected = new ArrayList<>();
        JavaSyntaxAnalyzer.Line line = null;

        for (int i = 0; i < items.length; ) {
            Object x = items[i++];
            if (x == NL) {
                sb.append("\n");
                if (line == null) {
                    line = new JavaSyntaxAnalyzer.Line();
                }
                expected.add(line);
                line = null;
            } else {
                JavaSyntaxAnalyzer.Type t = (JavaSyntaxAnalyzer.Type)x;
                String text = (String)items[i++];
                if (line == null) {
                    line = new JavaSyntaxAnalyzer.Line();
                }
                line.addSegment(t, text);
                sb.append(text);
            }
        }

        if (line != null) {
            expected.add(line);
        }

        String input = sb.toString();
        List<JavaSyntaxAnalyzer.Line> res = new JavaSyntaxAnalyzer(input).analyze();
        Assertions.assertArrayEquals(expected.toArray(), res.toArray());
    }
}
