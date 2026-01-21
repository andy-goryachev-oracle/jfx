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
package com.sun.media.jfxmedia.locator;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class BInputStream extends InputStream {
    private final BufferedInputStream input;
    private final int signatureSize;
    private byte[] signature;
    private int index; // -1 when read past the signature block

    public BInputStream(InputStream in, int signatureSize) {
        this.input = (in instanceof BufferedInputStream b) ? b : new BufferedInputStream(in);
        this.signatureSize = signatureSize;
    }

    @Override
    public int available() throws IOException {
        int d = (index >= 0) ? signature.length - index : 0;
        return input.available() + d;
    }

    @Override
    public void close() throws IOException {
        index = -1;
        input.close();
    }

    @Override
    public int read() throws IOException {
        if (index >= 0) {
            if (signature == null) {
                signature = readSignature();
            }
            // check read count again since we could have read 0 bytes
            if (index >= 0) {
                int v = signature[index++];
                if (index >= signature.length) {
                    index = -1;
                }
                return v;
            }
        }
        return input.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (index >= 0) {
            if (signature == null) {
                signature = readSignature();
            }
            // check read count again since we could have read 0 bytes
            if (index >= 0) {
                int av = Math.min(len, signature.length - index);
                if (av == 0) {
                    index = -1;
                } else if (av > 0) {
                    System.arraycopy(signature, index, b, off, av);
                    index -= av;
                    if (index <= 0) {
                        index = -1;
                    }
                    return av;
                }
            }
         }
        return input.read(b, off, len);
   }

    public byte[] getSignature() throws IOException {
        if(signature == null) {
            signature = readSignature();
        }
        return signature;
    }

    private byte[] readSignature() throws IOException {
        byte[] b = new byte[signatureSize];
        int read = 0;
        do {
            int rd = input.read(b, read, b.length - read);
            if (rd < 0) {
                if (read == 0) {
                    // eof
                    index = -1;
                    return new byte[0];
                }
                break;
            }
            read += rd;
        } while (read < signatureSize);

        if (read < signatureSize) {
            byte[] smaller = new byte[read];
            System.arraycopy(b, 0, smaller, 0, read);
            return smaller;
        }
        return b;
    }
}
