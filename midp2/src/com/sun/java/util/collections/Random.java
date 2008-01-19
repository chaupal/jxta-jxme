
/*
 *
 *  Copyright (c) 2001-2008 Sun Microsystems, Inc.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Sun Microsystems, Inc. for Project JXTA."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA",
 *  nor may "JXTA" appear in their name, without prior written
 *  permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  =========================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 *
 *  $Id: $
 *
 *
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
