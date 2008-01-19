
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

public class ArrayList extends AbstractList implements List {

    public ArrayList(int i) {
        elementData = new Object[i];
    }

    public ArrayList() {
        this(10);
    }

    public ArrayList(Collection collection) {
        this((collection.size() * 110) / 100);
        for (Iterator iterator = collection.iterator(); iterator.hasNext();)
            elementData[size++] = iterator.next();

    }

    public void trimToSize() {
        super.modCount++;
        int i = elementData.length;
        if (size < i) {
            Object aobj[] = elementData;
            elementData = new Object[size];
            System.arraycopy(((Object) (aobj)), 0, ((Object) (elementData)), 0, size);
        }
    }

    public void ensureCapacity(int i) {
        super.modCount++;
        int j = elementData.length;
        if (i > j) {
            Object aobj[] = elementData;
            int k = (j * 3) / 2 + 1;
            if (k < i)
                k = i;
            elementData = new Object[k];
            System.arraycopy(((Object) (aobj)), 0, ((Object) (elementData)), 0, size);
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object obj) {
        return indexOf(obj) >= 0;
    }

    public int indexOf(Object obj) {
        if (obj == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i] == null)
                    return i;

        } else {
            for (int j = 0; j < size; j++)
                if (obj.equals(elementData[j]))
                    return j;

        }
        return -1;
    }

    public int lastIndexOf(Object obj) {
        if (obj == null) {
            for (int i = size - 1; i >= 0; i--)
                if (elementData[i] == null)
                    return i;

        } else {
            for (int j = size - 1; j >= 0; j--)
                if (obj.equals(elementData[j]))
                    return j;

        }
        return -1;
    }

    public Object[] toArray() {
        Object aobj[] = new Object[size];
        System.arraycopy(((Object) (elementData)), 0, ((Object) (aobj)), 0, size);
        return aobj;
    }

    public Object[] toArray(Object aobj[]) {
        if (aobj.length < size)
            aobj = new Object[size];
        System.arraycopy(((Object) (elementData)), 0, ((Object) (aobj)), 0, size);
        if (aobj.length > size)
            aobj[size] = null;
        return aobj;
    }

    public Object get(int i) {
        RangeCheck(i);
        return elementData[i];
    }

    public Object set(int i, Object obj) {
        RangeCheck(i);
        Object obj1 = elementData[i];
        elementData[i] = obj;
        return obj1;
    }

    public boolean add(Object obj) {
        ensureCapacity(size + 1);
        elementData[size++] = obj;
        return true;
    }

    public void add(int i, Object obj) {
        if (i > size || i < 0) {
            throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + size);
        } else {
            ensureCapacity(size + 1);
            System.arraycopy(((Object) (elementData)), i, ((Object) (elementData)), i + 1, size - i);
            elementData[i] = obj;
            size++;
            return;
        }
    }

    public Object remove(int i) {
        RangeCheck(i);
        super.modCount++;
        Object obj = elementData[i];
        int j = size - i - 1;
        if (j > 0)
            System.arraycopy(((Object) (elementData)), i + 1, ((Object) (elementData)), i, j);
        elementData[--size] = null;
        return obj;
    }

    public void clear() {
        super.modCount++;
        for (int i = 0; i < size; i++)
            elementData[i] = null;

        size = 0;
    }

    public boolean addAll(Collection collection) {
        super.modCount++;
        int i = collection.size();
        ensureCapacity(size + i);
        Iterator iterator = collection.iterator();
        for (int j = 0; j < i; j++)
            elementData[size++] = iterator.next();

        return i != 0;
    }

    public boolean addAll(int i, Collection collection) {
        if (i > size || i < 0)
            throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + size);
        int j = collection.size();
        ensureCapacity(size + j);
        int k = size - i;
        if (k > 0)
            System.arraycopy(((Object) (elementData)), i, ((Object) (elementData)), i + j, k);
        Iterator iterator = collection.iterator();
        for (int l = 0; l < j; l++)
            elementData[i++] = iterator.next();

        size += j;
        return j != 0;
    }

    protected void removeRange(int i, int j) {
        super.modCount++;
        int k = size - j;
        System.arraycopy(((Object) (elementData)), j, ((Object) (elementData)), i, k);
        for (int l = size - (j - i); size != l;)
            elementData[--size] = null;

    }

    private void RangeCheck(int i) {
        if (i >= size || i < 0)
            throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + size);
        else
            return;
    }

    private transient Object elementData[];
    private int size;
}
