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

public class Arrays {
    private static class ArrayList extends AbstractList {

        public int size() {
            return a.length;
        }

        public Object[] toArray() {
            return a;
        }

        public Object get(int i) {
            return a[i];
        }

        public Object set(int i, Object obj) {
            Object obj1 = a[i];
            a[i] = obj;
            return obj1;
        }

        private Object a[];

        ArrayList(Object aobj[]) {
            a = aobj;
        }
    }


    private Arrays() {
    }

    public static void sort(long al[]) {
        sort1(al, 0, al.length);
    }

    public static void sort(long al[], int i, int j) {
        rangeCheck(al.length, i, j);
        sort1(al, i, j - i);
    }

    public static void sort(int ai[]) {
        sort1(ai, 0, ai.length);
    }

    public static void sort(int ai[], int i, int j) {
        rangeCheck(ai.length, i, j);
        sort1(ai, i, j - i);
    }

    public static void sort(short aword0[]) {
        sort1(aword0, 0, aword0.length);
    }

    public static void sort(short aword0[], int i, int j) {
        rangeCheck(aword0.length, i, j);
        sort1(aword0, i, j - i);
    }

    public static void sort(char ac[]) {
        sort1(ac, 0, ac.length);
    }

    public static void sort(char ac[], int i, int j) {
        rangeCheck(ac.length, i, j);
        sort1(ac, i, j - i);
    }

    public static void sort(byte abyte0[]) {
        sort1(abyte0, 0, abyte0.length);
    }

    public static void sort(byte abyte0[], int i, int j) {
        rangeCheck(abyte0.length, i, j);
        sort1(abyte0, i, j - i);
    }

    public static void sort(double ad[]) {
        sort2(ad, 0, ad.length);
    }

    public static void sort(double ad[], int i, int j) {
        rangeCheck(ad.length, i, j);
        sort2(ad, i, j);
    }

    public static void sort(float af[]) {
        sort2(af, 0, af.length);
    }

    public static void sort(float af[], int i, int j) {
        rangeCheck(af.length, i, j);
        sort2(af, i, j);
    }

    private static void sort2(double ad[], int i, int j) {
        long l = Double.doubleToLongBits(-0D);
        int k = 0;
        int i1 = i;
        int j1;
        for (j1 = j; i1 < j1;)
            if (ad[i1] != ad[i1]) {
                ad[i1] = ad[--j1];
                ad[j1] = (0.0D / 0.0D);
            } else {
                if (ad[i1] == 0.0D && Double.doubleToLongBits(ad[i1]) == l) {
                    ad[i1] = 0.0D;
                    k++;
                }
                i1++;
            }

        sort1(ad, i, j1 - i);
        if (k != 0) {
            int k1;
            for (k1 = binarySearch(ad, 0.0D, i, j1 - 1); --k1 >= 0 && ad[k1] == 0.0D;)
                ;
            for (int l1 = 0; l1 < k; l1++)
                ad[++k1] = -0D;

        }
    }

    private static void sort2(float af[], int i, int j) {
        int k = Float.floatToIntBits(-0F);
        int l = 0;
        int i1 = i;
        int j1;
        for (j1 = j; i1 < j1;)
            if (af[i1] != af[i1]) {
                af[i1] = af[--j1];
                af[j1] = (0.0F / 0.0F);
            } else {
                if (af[i1] == 0.0F && Float.floatToIntBits(af[i1]) == k) {
                    af[i1] = 0.0F;
                    l++;
                }
                i1++;
            }

        sort1(af, i, j1 - i);
        if (l != 0) {
            int k1;
            for (k1 = binarySearch(af, 0.0F, i, j1 - 1); --k1 >= 0 && af[k1] == 0.0F;)
                ;
            for (int l1 = 0; l1 < l; l1++)
                af[++k1] = -0F;

        }
    }

    private static void sort1(long al[], int i, int j) {
        if (j < 7) {
            for (int k = i; k < j + i; k++) {
                for (int i1 = k; i1 > i && al[i1 - 1] > al[i1]; i1--)
                    swap(al, i1, i1 - 1);

            }

            return;
        }
        int l = i + j / 2;
        if (j > 7) {
            int j1 = i;
            int k1 = (i + j) - 1;
            if (j > 40) {
                int i2 = j / 8;
                j1 = med3(al, j1, j1 + i2, j1 + 2 * i2);
                l = med3(al, l - i2, l, l + i2);
                k1 = med3(al, k1 - 2 * i2, k1 - i2, k1);
            }
            l = med3(al, j1, l, k1);
        }
        long l1 = al[l];
        int j2 = i;
        int k2 = j2;
        int l2 = (i + j) - 1;
        int i3 = l2;
        do {
            while (k2 <= l2 && al[k2] <= l1) {
                if (al[k2] == l1)
                    swap(al, j2++, k2);
                k2++;
            }
            for (; l2 >= k2 && al[l2] >= l1; l2--)
                if (al[l2] == l1)
                    swap(al, l2, i3--);

            if (k2 > l2)
                break;
            swap(al, k2++, l2--);
        } while (true);
        int k3 = i + j;
        int j3 = Math.min(j2 - i, k2 - j2);
        vecswap(al, i, k2 - j3, j3);
        j3 = Math.min(i3 - l2, k3 - i3 - 1);
        vecswap(al, k2, k3 - j3, j3);
        if ((j3 = k2 - j2) > 1)
            sort1(al, i, j3);
        if ((j3 = i3 - l2) > 1)
            sort1(al, k3 - j3, j3);
    }

    private static void swap(long al[], int i, int j) {
        long l = al[i];
        al[i] = al[j];
        al[j] = l;
    }

    private static void vecswap(long al[], int i, int j, int k) {
        for (int l = 0; l < k;) {
            swap(al, i, j);
            l++;
            i++;
            j++;
        }

    }

    private static int med3(long al[], int i, int j, int k) {
        if (al[i] < al[j]) {
            if (al[j] < al[k])
                return j;
            if (al[i] < al[k])
                return k;
            else
                return i;
        }
        if (al[j] > al[k])
            return j;
        if (al[i] > al[k])
            return k;
        else
            return i;
    }

    private static void sort1(int ai[], int i, int j) {
        if (j < 7) {
            for (int k = i; k < j + i; k++) {
                for (int i1 = k; i1 > i && ai[i1 - 1] > ai[i1]; i1--)
                    swap(ai, i1, i1 - 1);

            }

            return;
        }
        int l = i + j / 2;
        if (j > 7) {
            int j1 = i;
            int l1 = (i + j) - 1;
            if (j > 40) {
                int j2 = j / 8;
                j1 = med3(ai, j1, j1 + j2, j1 + 2 * j2);
                l = med3(ai, l - j2, l, l + j2);
                l1 = med3(ai, l1 - 2 * j2, l1 - j2, l1);
            }
            l = med3(ai, j1, l, l1);
        }
        int k1 = ai[l];
        int i2 = i;
        int k2 = i2;
        int l2 = (i + j) - 1;
        int i3 = l2;
        do {
            while (k2 <= l2 && ai[k2] <= k1) {
                if (ai[k2] == k1)
                    swap(ai, i2++, k2);
                k2++;
            }
            for (; l2 >= k2 && ai[l2] >= k1; l2--)
                if (ai[l2] == k1)
                    swap(ai, l2, i3--);

            if (k2 > l2)
                break;
            swap(ai, k2++, l2--);
        } while (true);
        int k3 = i + j;
        int j3 = Math.min(i2 - i, k2 - i2);
        vecswap(ai, i, k2 - j3, j3);
        j3 = Math.min(i3 - l2, k3 - i3 - 1);
        vecswap(ai, k2, k3 - j3, j3);
        if ((j3 = k2 - i2) > 1)
            sort1(ai, i, j3);
        if ((j3 = i3 - l2) > 1)
            sort1(ai, k3 - j3, j3);
    }

    private static void swap(int ai[], int i, int j) {
        int k = ai[i];
        ai[i] = ai[j];
        ai[j] = k;
    }

    private static void vecswap(int ai[], int i, int j, int k) {
        for (int l = 0; l < k;) {
            swap(ai, i, j);
            l++;
            i++;
            j++;
        }

    }

    private static int med3(int ai[], int i, int j, int k) {
        if (ai[i] < ai[j]) {
            if (ai[j] < ai[k])
                return j;
            if (ai[i] < ai[k])
                return k;
            else
                return i;
        }
        if (ai[j] > ai[k])
            return j;
        if (ai[i] > ai[k])
            return k;
        else
            return i;
    }

    private static void sort1(short aword0[], int i, int j) {
        if (j < 7) {
            for (int k = i; k < j + i; k++) {
                for (int i1 = k; i1 > i && aword0[i1 - 1] > aword0[i1]; i1--)
                    swap(aword0, i1, i1 - 1);

            }

            return;
        }
        int l = i + j / 2;
        if (j > 7) {
            int j1 = i;
            int k1 = (i + j) - 1;
            if (j > 40) {
                int i2 = j / 8;
                j1 = med3(aword0, j1, j1 + i2, j1 + 2 * i2);
                l = med3(aword0, l - i2, l, l + i2);
                k1 = med3(aword0, k1 - 2 * i2, k1 - i2, k1);
            }
            l = med3(aword0, j1, l, k1);
        }
        short word0 = aword0[l];
        int l1 = i;
        int j2 = l1;
        int k2 = (i + j) - 1;
        int l2 = k2;
        do {
            while (j2 <= k2 && aword0[j2] <= word0) {
                if (aword0[j2] == word0)
                    swap(aword0, l1++, j2);
                j2++;
            }
            for (; k2 >= j2 && aword0[k2] >= word0; k2--)
                if (aword0[k2] == word0)
                    swap(aword0, k2, l2--);

            if (j2 > k2)
                break;
            swap(aword0, j2++, k2--);
        } while (true);
        int j3 = i + j;
        int i3 = Math.min(l1 - i, j2 - l1);
        vecswap(aword0, i, j2 - i3, i3);
        i3 = Math.min(l2 - k2, j3 - l2 - 1);
        vecswap(aword0, j2, j3 - i3, i3);
        if ((i3 = j2 - l1) > 1)
            sort1(aword0, i, i3);
        if ((i3 = l2 - k2) > 1)
            sort1(aword0, j3 - i3, i3);
    }

    private static void swap(short aword0[], int i, int j) {
        short word0 = aword0[i];
        aword0[i] = aword0[j];
        aword0[j] = word0;
    }

    private static void vecswap(short aword0[], int i, int j, int k) {
        for (int l = 0; l < k;) {
            swap(aword0, i, j);
            l++;
            i++;
            j++;
        }

    }

    private static int med3(short aword0[], int i, int j, int k) {
        if (aword0[i] < aword0[j]) {
            if (aword0[j] < aword0[k])
                return j;
            if (aword0[i] < aword0[k])
                return k;
            else
                return i;
        }
        if (aword0[j] > aword0[k])
            return j;
        if (aword0[i] > aword0[k])
            return k;
        else
            return i;
    }

    private static void sort1(char ac[], int i, int j) {
        if (j < 7) {
            for (int k = i; k < j + i; k++) {
                for (int i1 = k; i1 > i && ac[i1 - 1] > ac[i1]; i1--)
                    swap(ac, i1, i1 - 1);

            }

            return;
        }
        int l = i + j / 2;
        if (j > 7) {
            int j1 = i;
            int k1 = (i + j) - 1;
            if (j > 40) {
                int i2 = j / 8;
                j1 = med3(ac, j1, j1 + i2, j1 + 2 * i2);
                l = med3(ac, l - i2, l, l + i2);
                k1 = med3(ac, k1 - 2 * i2, k1 - i2, k1);
            }
            l = med3(ac, j1, l, k1);
        }
        char c = ac[l];
        int l1 = i;
        int j2 = l1;
        int k2 = (i + j) - 1;
        int l2 = k2;
        do {
            while (j2 <= k2 && ac[j2] <= c) {
                if (ac[j2] == c)
                    swap(ac, l1++, j2);
                j2++;
            }
            for (; k2 >= j2 && ac[k2] >= c; k2--)
                if (ac[k2] == c)
                    swap(ac, k2, l2--);

            if (j2 > k2)
                break;
            swap(ac, j2++, k2--);
        } while (true);
        int j3 = i + j;
        int i3 = Math.min(l1 - i, j2 - l1);
        vecswap(ac, i, j2 - i3, i3);
        i3 = Math.min(l2 - k2, j3 - l2 - 1);
        vecswap(ac, j2, j3 - i3, i3);
        if ((i3 = j2 - l1) > 1)
            sort1(ac, i, i3);
        if ((i3 = l2 - k2) > 1)
            sort1(ac, j3 - i3, i3);
    }

    private static void swap(char ac[], int i, int j) {
        char c = ac[i];
        ac[i] = ac[j];
        ac[j] = c;
    }

    private static void vecswap(char ac[], int i, int j, int k) {
        for (int l = 0; l < k;) {
            swap(ac, i, j);
            l++;
            i++;
            j++;
        }

    }

    private static int med3(char ac[], int i, int j, int k) {
        if (ac[i] < ac[j]) {
            if (ac[j] < ac[k])
                return j;
            if (ac[i] < ac[k])
                return k;
            else
                return i;
        }
        if (ac[j] > ac[k])
            return j;
        if (ac[i] > ac[k])
            return k;
        else
            return i;
    }

    private static void sort1(byte abyte0[], int i, int j) {
        if (j < 7) {
            for (int k = i; k < j + i; k++) {
                for (int i1 = k; i1 > i && abyte0[i1 - 1] > abyte0[i1]; i1--)
                    swap(abyte0, i1, i1 - 1);

            }

            return;
        }
        int l = i + j / 2;
        if (j > 7) {
            int j1 = i;
            int k1 = (i + j) - 1;
            if (j > 40) {
                int i2 = j / 8;
                j1 = med3(abyte0, j1, j1 + i2, j1 + 2 * i2);
                l = med3(abyte0, l - i2, l, l + i2);
                k1 = med3(abyte0, k1 - 2 * i2, k1 - i2, k1);
            }
            l = med3(abyte0, j1, l, k1);
        }
        byte byte0 = abyte0[l];
        int l1 = i;
        int j2 = l1;
        int k2 = (i + j) - 1;
        int l2 = k2;
        do {
            while (j2 <= k2 && abyte0[j2] <= byte0) {
                if (abyte0[j2] == byte0)
                    swap(abyte0, l1++, j2);
                j2++;
            }
            for (; k2 >= j2 && abyte0[k2] >= byte0; k2--)
                if (abyte0[k2] == byte0)
                    swap(abyte0, k2, l2--);

            if (j2 > k2)
                break;
            swap(abyte0, j2++, k2--);
        } while (true);
        int j3 = i + j;
        int i3 = Math.min(l1 - i, j2 - l1);
        vecswap(abyte0, i, j2 - i3, i3);
        i3 = Math.min(l2 - k2, j3 - l2 - 1);
        vecswap(abyte0, j2, j3 - i3, i3);
        if ((i3 = j2 - l1) > 1)
            sort1(abyte0, i, i3);
        if ((i3 = l2 - k2) > 1)
            sort1(abyte0, j3 - i3, i3);
    }

    private static void swap(byte abyte0[], int i, int j) {
        byte byte0 = abyte0[i];
        abyte0[i] = abyte0[j];
        abyte0[j] = byte0;
    }

    private static void vecswap(byte abyte0[], int i, int j, int k) {
        for (int l = 0; l < k;) {
            swap(abyte0, i, j);
            l++;
            i++;
            j++;
        }

    }

    private static int med3(byte abyte0[], int i, int j, int k) {
        if (abyte0[i] < abyte0[j]) {
            if (abyte0[j] < abyte0[k])
                return j;
            if (abyte0[i] < abyte0[k])
                return k;
            else
                return i;
        }
        if (abyte0[j] > abyte0[k])
            return j;
        if (abyte0[i] > abyte0[k])
            return k;
        else
            return i;
    }

    private static void sort1(double ad[], int i, int j) {
        if (j < 7) {
            for (int k = i; k < j + i; k++) {
                for (int i1 = k; i1 > i && ad[i1 - 1] > ad[i1]; i1--)
                    swap(ad, i1, i1 - 1);

            }

            return;
        }
        int l = i + j / 2;
        if (j > 7) {
            int j1 = i;
            int k1 = (i + j) - 1;
            if (j > 40) {
                int l1 = j / 8;
                j1 = med3(ad, j1, j1 + l1, j1 + 2 * l1);
                l = med3(ad, l - l1, l, l + l1);
                k1 = med3(ad, k1 - 2 * l1, k1 - l1, k1);
            }
            l = med3(ad, j1, l, k1);
        }
        double d = ad[l];
        int i2 = i;
        int j2 = i2;
        int k2 = (i + j) - 1;
        int l2 = k2;
        do {
            while (j2 <= k2 && ad[j2] <= d) {
                if (ad[j2] == d)
                    swap(ad, i2++, j2);
                j2++;
            }
            for (; k2 >= j2 && ad[k2] >= d; k2--)
                if (ad[k2] == d)
                    swap(ad, k2, l2--);

            if (j2 > k2)
                break;
            swap(ad, j2++, k2--);
        } while (true);
        int j3 = i + j;
        int i3 = Math.min(i2 - i, j2 - i2);
        vecswap(ad, i, j2 - i3, i3);
        i3 = Math.min(l2 - k2, j3 - l2 - 1);
        vecswap(ad, j2, j3 - i3, i3);
        if ((i3 = j2 - i2) > 1)
            sort1(ad, i, i3);
        if ((i3 = l2 - k2) > 1)
            sort1(ad, j3 - i3, i3);
    }

    private static void swap(double ad[], int i, int j) {
        double d = ad[i];
        ad[i] = ad[j];
        ad[j] = d;
    }

    private static void vecswap(double ad[], int i, int j, int k) {
        for (int l = 0; l < k;) {
            swap(ad, i, j);
            l++;
            i++;
            j++;
        }

    }

    private static int med3(double ad[], int i, int j, int k) {
        if (ad[i] < ad[j]) {
            if (ad[j] < ad[k])
                return j;
            if (ad[i] < ad[k])
                return k;
            else
                return i;
        }
        if (ad[j] > ad[k])
            return j;
        if (ad[i] > ad[k])
            return k;
        else
            return i;
    }

    private static void sort1(float af[], int i, int j) {
        if (j < 7) {
            for (int k = i; k < j + i; k++) {
                for (int i1 = k; i1 > i && af[i1 - 1] > af[i1]; i1--)
                    swap(af, i1, i1 - 1);

            }

            return;
        }
        int l = i + j / 2;
        if (j > 7) {
            int j1 = i;
            int k1 = (i + j) - 1;
            if (j > 40) {
                int i2 = j / 8;
                j1 = med3(af, j1, j1 + i2, j1 + 2 * i2);
                l = med3(af, l - i2, l, l + i2);
                k1 = med3(af, k1 - 2 * i2, k1 - i2, k1);
            }
            l = med3(af, j1, l, k1);
        }
        float f = af[l];
        int l1 = i;
        int j2 = l1;
        int k2 = (i + j) - 1;
        int l2 = k2;
        do {
            while (j2 <= k2 && af[j2] <= f) {
                if (af[j2] == f)
                    swap(af, l1++, j2);
                j2++;
            }
            for (; k2 >= j2 && af[k2] >= f; k2--)
                if (af[k2] == f)
                    swap(af, k2, l2--);

            if (j2 > k2)
                break;
            swap(af, j2++, k2--);
        } while (true);
        int j3 = i + j;
        int i3 = Math.min(l1 - i, j2 - l1);
        vecswap(af, i, j2 - i3, i3);
        i3 = Math.min(l2 - k2, j3 - l2 - 1);
        vecswap(af, j2, j3 - i3, i3);
        if ((i3 = j2 - l1) > 1)
            sort1(af, i, i3);
        if ((i3 = l2 - k2) > 1)
            sort1(af, j3 - i3, i3);
    }

    private static void swap(float af[], int i, int j) {
        float f = af[i];
        af[i] = af[j];
        af[j] = f;
    }

    private static void vecswap(float af[], int i, int j, int k) {
        for (int l = 0; l < k;) {
            swap(af, i, j);
            l++;
            i++;
            j++;
        }

    }

    private static int med3(float af[], int i, int j, int k) {
        if (af[i] < af[j]) {
            if (af[j] < af[k])
                return j;
            if (af[i] < af[k])
                return k;
            else
                return i;
        }
        if (af[j] > af[k])
            return j;
        if (af[i] > af[k])
            return k;
        else
            return i;
    }

    public static void sort(Object aobj[]) {
        Object aobj1[] = aobj;
        mergeSort(aobj1, aobj, 0, aobj.length);
    }

    public static void sort(Object aobj[], int i, int j) {
        rangeCheck(aobj.length, i, j);
        Object aobj1[] = aobj;
        mergeSort(aobj1, aobj, i, j);
    }

    private static void mergeSort(Object aobj[], Object aobj1[], int i, int j) {
        int k = j - i;
        if (k < 7) {
            for (int l = i; l < j; l++) {
                for (int j1 = l; j1 > i && ((Comparable) aobj1[j1 - 1]).compareTo((Comparable) aobj1[j1]) > 0; j1--)
                    swap(aobj1, j1, j1 - 1);

            }

            return;
        }
        int i1 = (i + j) / 2;
        mergeSort(aobj1, aobj, i, i1);
        mergeSort(aobj1, aobj, i1, j);
        if (((Comparable) aobj[i1 - 1]).compareTo((Comparable) aobj[i1]) <= 0) {
            System.arraycopy(((Object) (aobj)), i, ((Object) (aobj1)), i, k);
            return;
        }
        int k1 = i;
        int l1 = i;
        int i2 = i1;
        for (; k1 < j; k1++)
            if (i2 >= j || l1 < i1 && ((Comparable) aobj[l1]).compareTo(aobj[i2]) <= 0)
                aobj1[k1] = aobj[l1++];
            else
                aobj1[k1] = aobj[i2++];

    }

    private static void swap(Object aobj[], int i, int j) {
        Object obj = aobj[i];
        aobj[i] = aobj[j];
        aobj[j] = obj;
    }

    public static void sort(Object aobj[], Comparator comparator) {
        Object aobj1[] = aobj;
        mergeSort(aobj1, aobj, 0, aobj.length, comparator);
    }

    public static void sort(Object aobj[], int i, int j, Comparator comparator) {
        rangeCheck(aobj.length, i, j);
        Object aobj1[] = aobj;
        mergeSort(aobj1, aobj, i, j, comparator);
    }

    private static void mergeSort(Object aobj[], Object aobj1[], int i, int j, Comparator comparator) {
        int k = j - i;
        if (k < 7) {
            for (int l = i; l < j; l++) {
                for (int j1 = l; j1 > i && comparator.compare(aobj1[j1 - 1], aobj1[j1]) > 0; j1--)
                    swap(aobj1, j1, j1 - 1);

            }

            return;
        }
        int i1 = (i + j) / 2;
        mergeSort(aobj1, aobj, i, i1, comparator);
        mergeSort(aobj1, aobj, i1, j, comparator);
        if (comparator.compare(aobj[i1 - 1], aobj[i1]) <= 0) {
            System.arraycopy(((Object) (aobj)), i, ((Object) (aobj1)), i, k);
            return;
        }
        int k1 = i;
        int l1 = i;
        int i2 = i1;
        for (; k1 < j; k1++)
            if (i2 >= j || l1 < i1 && comparator.compare(aobj[l1], aobj[i2]) <= 0)
                aobj1[k1] = aobj[l1++];
            else
                aobj1[k1] = aobj[i2++];

    }

    private static void rangeCheck(int i, int j, int k) {
        if (j > k)
            throw new IllegalArgumentException("fromIndex(" + j + ") > toIndex(" + k + ")");
        if (j < 0)
            throw new ArrayIndexOutOfBoundsException(j);
        if (k > i)
            throw new ArrayIndexOutOfBoundsException(k);
        else
            return;
    }

    public static int binarySearch(long al[], long l) {
        int i = 0;
        for (int j = al.length - 1; i <= j;) {
            int k = (i + j) / 2;
            long l1 = al[k];
            if (l1 < l)
                i = k + 1;
            else if (l1 > l)
                j = k - 1;
            else
                return k;
        }

        return -(i + 1);
    }

    public static int binarySearch(int ai[], int i) {
        int j = 0;
        for (int k = ai.length - 1; j <= k;) {
            int l = (j + k) / 2;
            int i1 = ai[l];
            if (i1 < i)
                j = l + 1;
            else if (i1 > i)
                k = l - 1;
            else
                return l;
        }

        return -(j + 1);
    }

    public static int binarySearch(short aword0[], short word0) {
        int i = 0;
        for (int j = aword0.length - 1; i <= j;) {
            int k = (i + j) / 2;
            short word1 = aword0[k];
            if (word1 < word0)
                i = k + 1;
            else if (word1 > word0)
                j = k - 1;
            else
                return k;
        }

        return -(i + 1);
    }

    public static int binarySearch(char ac[], char c) {
        int i = 0;
        for (int j = ac.length - 1; i <= j;) {
            int k = (i + j) / 2;
            char c1 = ac[k];
            if (c1 < c)
                i = k + 1;
            else if (c1 > c)
                j = k - 1;
            else
                return k;
        }

        return -(i + 1);
    }

    public static int binarySearch(byte abyte0[], byte byte0) {
        int i = 0;
        for (int j = abyte0.length - 1; i <= j;) {
            int k = (i + j) / 2;
            byte byte1 = abyte0[k];
            if (byte1 < byte0)
                i = k + 1;
            else if (byte1 > byte0)
                j = k - 1;
            else
                return k;
        }

        return -(i + 1);
    }

    public static int binarySearch(double ad[], double d) {
        return binarySearch(ad, d, 0, ad.length - 1);
    }

    private static int binarySearch(double ad[], double d, int i, int j) {
        while (i <= j) {
            int k = (i + j) / 2;
            double d1 = ad[k];
            byte byte0;
            if (d1 < d)
                byte0 = -1;
            else if (d1 > d) {
                byte0 = 1;
            } else {
                long l = Double.doubleToLongBits(d1);
                long l1 = Double.doubleToLongBits(d);
                byte0 = l != l1 ? ((byte) (l >= l1 ? 1 : -1)) : 0;
            }
            if (byte0 < 0)
                i = k + 1;
            else if (byte0 > 0)
                j = k - 1;
            else
                return k;
        }
        return -(i + 1);
    }

    public static int binarySearch(float af[], float f) {
        return binarySearch(af, f, 0, af.length - 1);
    }

    private static int binarySearch(float af[], float f, int i, int j) {
        while (i <= j) {
            int k = (i + j) / 2;
            float f1 = af[k];
            byte byte0;
            if (f1 < f)
                byte0 = -1;
            else if (f1 > f) {
                byte0 = 1;
            } else {
                int l = Float.floatToIntBits(f1);
                int i1 = Float.floatToIntBits(f);
                byte0 = l != i1 ? ((byte) (l >= i1 ? 1 : -1)) : 0;
            }
            if (byte0 < 0)
                i = k + 1;
            else if (byte0 > 0)
                j = k - 1;
            else
                return k;
        }
        return -(i + 1);
    }

    public static int binarySearch(Object aobj[], Object obj) {
        int i = 0;
        for (int j = aobj.length - 1; i <= j;) {
            int k = (i + j) / 2;
            Object obj1 = aobj[k];
            int l = ((Comparable) obj1).compareTo(obj);
            if (l < 0)
                i = k + 1;
            else if (l > 0)
                j = k - 1;
            else
                return k;
        }

        return -(i + 1);
    }

    public static int binarySearch(Object aobj[], Object obj, Comparator comparator) {
        int i = 0;
        for (int j = aobj.length - 1; i <= j;) {
            int k = (i + j) / 2;
            Object obj1 = aobj[k];
            int l = comparator.compare(obj1, obj);
            if (l < 0)
                i = k + 1;
            else if (l > 0)
                j = k - 1;
            else
                return k;
        }

        return -(i + 1);
    }

    public static boolean equals(long al[], long al1[]) {
        if (al == al1)
            return true;
        if (al == null || al1 == null)
            return false;
        int i = al.length;
        if (al1.length != i)
            return false;
        for (int j = 0; j < i; j++)
            if (al[j] != al1[j])
                return false;

        return true;
    }

    public static boolean equals(int ai[], int ai1[]) {
        if (ai == ai1)
            return true;
        if (ai == null || ai1 == null)
            return false;
        int i = ai.length;
        if (ai1.length != i)
            return false;
        for (int j = 0; j < i; j++)
            if (ai[j] != ai1[j])
                return false;

        return true;
    }

    public static boolean equals(short aword0[], short aword1[]) {
        if (aword0 == aword1)
            return true;
        if (aword0 == null || aword1 == null)
            return false;
        int i = aword0.length;
        if (aword1.length != i)
            return false;
        for (int j = 0; j < i; j++)
            if (aword0[j] != aword1[j])
                return false;

        return true;
    }

    public static boolean equals(char ac[], char ac1[]) {
        if (ac == ac1)
            return true;
        if (ac == null || ac1 == null)
            return false;
        int i = ac.length;
        if (ac1.length != i)
            return false;
        for (int j = 0; j < i; j++)
            if (ac[j] != ac1[j])
                return false;

        return true;
    }

    public static boolean equals(byte abyte0[], byte abyte1[]) {
        if (abyte0 == abyte1)
            return true;
        if (abyte0 == null || abyte1 == null)
            return false;
        int i = abyte0.length;
        if (abyte1.length != i)
            return false;
        for (int j = 0; j < i; j++)
            if (abyte0[j] != abyte1[j])
                return false;

        return true;
    }

    public static boolean equals(boolean aflag[], boolean aflag1[]) {
        if (aflag == aflag1)
            return true;
        if (aflag == null || aflag1 == null)
            return false;
        int i = aflag.length;
        if (aflag1.length != i)
            return false;
        for (int j = 0; j < i; j++)
            if (aflag[j] != aflag1[j])
                return false;

        return true;
    }

    public static boolean equals(double ad[], double ad1[]) {
        if (ad == ad1)
            return true;
        if (ad == null || ad1 == null)
            return false;
        int i = ad.length;
        if (ad1.length != i)
            return false;
        for (int j = 0; j < i; j++)
            if (Double.doubleToLongBits(ad[j]) != Double.doubleToLongBits(ad1[j]))
                return false;

        return true;
    }

    public static boolean equals(float af[], float af1[]) {
        if (af == af1)
            return true;
        if (af == null || af1 == null)
            return false;
        int i = af.length;
        if (af1.length != i)
            return false;
        for (int j = 0; j < i; j++)
            if (Float.floatToIntBits(af[j]) != Float.floatToIntBits(af1[j]))
                return false;

        return true;
    }

    public static boolean equals(Object aobj[], Object aobj1[]) {
        if (aobj == aobj1)
            return true;
        if (aobj == null || aobj1 == null)
            return false;
        int i = aobj.length;
        if (aobj1.length != i)
            return false;
        for (int j = 0; j < i; j++) {
            Object obj = aobj[j];
            Object obj1 = aobj1[j];
            if (obj != null ? !obj.equals(obj1) : obj1 != null && true)
                return false;
        }

        return true;
    }

    public static void fill(long al[], long l) {
        fill(al, 0, al.length, l);
    }

    public static void fill(long al[], int i, int j, long l) {
        rangeCheck(al.length, i, j);
        for (int k = i; k < j; k++)
            al[k] = l;

    }

    public static void fill(int ai[], int i) {
        fill(ai, 0, ai.length, i);
    }

    public static void fill(int ai[], int i, int j, int k) {
        rangeCheck(ai.length, i, j);
        for (int l = i; l < j; l++)
            ai[l] = k;

    }

    public static void fill(short aword0[], short word0) {
        fill(aword0, 0, aword0.length, word0);
    }

    public static void fill(short aword0[], int i, int j, short word0) {
        rangeCheck(aword0.length, i, j);
        for (int k = i; k < j; k++)
            aword0[k] = word0;

    }

    public static void fill(char ac[], char c) {
        fill(ac, 0, ac.length, c);
    }

    public static void fill(char ac[], int i, int j, char c) {
        rangeCheck(ac.length, i, j);
        for (int k = i; k < j; k++)
            ac[k] = c;

    }

    public static void fill(byte abyte0[], byte byte0) {
        fill(abyte0, 0, abyte0.length, byte0);
    }

    public static void fill(byte abyte0[], int i, int j, byte byte0) {
        rangeCheck(abyte0.length, i, j);
        for (int k = i; k < j; k++)
            abyte0[k] = byte0;

    }

    public static void fill(boolean aflag[], boolean flag) {
        fill(aflag, 0, aflag.length, flag);
    }

    public static void fill(boolean aflag[], int i, int j, boolean flag) {
        rangeCheck(aflag.length, i, j);
        for (int k = i; k < j; k++)
            aflag[k] = flag;

    }

    public static void fill(double ad[], double d) {
        fill(ad, 0, ad.length, d);
    }

    public static void fill(double ad[], int i, int j, double d) {
        rangeCheck(ad.length, i, j);
        for (int k = i; k < j; k++)
            ad[k] = d;

    }

    public static void fill(float af[], float f) {
        fill(af, 0, af.length, f);
    }

    public static void fill(float af[], int i, int j, float f) {
        rangeCheck(af.length, i, j);
        for (int k = i; k < j; k++)
            af[k] = f;

    }

    public static void fill(Object aobj[], Object obj) {
        fill(aobj, 0, aobj.length, obj);
    }

    public static void fill(Object aobj[], int i, int j, Object obj) {
        rangeCheck(aobj.length, i, j);
        for (int k = i; k < j; k++)
            aobj[k] = obj;

    }

    public static List asList(Object aobj[]) {
        return new ArrayList(aobj);
    }
}
