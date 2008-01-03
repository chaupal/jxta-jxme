/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jxta-jxme.dev.java.net/public/CDDL+GPL.html
 * or jxme/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jxme/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.java.util.collections;

public class Random {

    public Random() {
        this(System.currentTimeMillis());
    }

    public Random(long l) {
        haveNextNextGaussian = false;
        setSeed(l);
    }

    public synchronized void setSeed(long l) {
        seed = (l ^ 0x5deece66dL) & (1L << 48) - 1L;
        haveNextNextGaussian = false;
    }

    protected synchronized int next(int i) {
        long l = seed * 0x5deece66dL + 11L & (1L << 48) - 1L;
        seed = l;
        return (int) (l >>> 48 - i);
    }

    public void nextBytes(byte abyte0[]) {
        int i = abyte0.length;
        int j = 0;
        int k = 0;
        for (int l = 0; l < 4; l++) {
            if (j == i)
                return;
            k = l != 0 ? k >> 8 : next(32);
            abyte0[j++] = (byte) k;
        }

    }

    public int nextInt() {
        return next(32);
    }

    public int nextInt(int i) {
        if (i <= 0)
            throw new IllegalArgumentException("n must be positive");
        int j;
        int k;
        do {
            j = next(31);
            k = j % i;
        } while ((j - k) + (i - 1) < 0);
        return k;
    }

    public long nextLong() {
        return ((long) next(32) << 32) + (long) next(32);
    }

    public boolean nextBoolean() {
        return next(1) != 0;
    }

    public float nextFloat() {
        int i = next(24);
        return (float) i / 1.677722E+07F;
    }

    public double nextDouble() {
        long l = ((long) next(26) << 27) + (long) next(27);
        return (double) l / (double) (1L << 53);
    }

    static final long serialVersionUID = 0x363296344bf00a53L;
    private long seed;
    private static final long multiplier = 0x5deece66dL;
    private static final long addend = 11L;
    private static final long mask = (1L << 48) - 1L;
    private static final int BITS_PER_BYTE = 8;
    private static final int BYTES_PER_INT = 4;
    private double nextNextGaussian;
    private boolean haveNextNextGaussian;
}
