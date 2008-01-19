
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

import java.util.Enumeration;

public class Vector extends AbstractList implements List {

    public Vector(int i, int j) {
        if (i < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + i);
        } else {
            elementData = new Object[i];
            capacityIncrement = j;
        }
    }

    public Vector(int i) {
        this(i, 0);
    }

    public Vector() {
        this(10);
    }

    public Vector(Collection collection) {
        this((collection.size() * 110) / 100);
        for (Iterator iterator = collection.iterator(); iterator.hasNext();)
            elementData[elementCount++] = iterator.next();

    }

    public synchronized void copyInto(Object aobj[]) {
        System.arraycopy(((Object) (elementData)), 0, ((Object) (aobj)), 0, elementCount);
    }

    public synchronized void trimToSize() {
        super.modCount++;
        int i = elementData.length;
        if (elementCount < i) {
            Object aobj[] = elementData;
            elementData = new Object[elementCount];
            System.arraycopy(((Object) (aobj)), 0, ((Object) (elementData)), 0, elementCount);
        }
    }

    public synchronized void ensureCapacity(int i) {
        super.modCount++;
        ensureCapacityHelper(i);
    }

    private void ensureCapacityHelper(int i) {
        int j = elementData.length;
        if (i > j) {
            Object aobj[] = elementData;
            int k = capacityIncrement <= 0 ? j * 2 : j + capacityIncrement;
            if (k < i)
                k = i;
            elementData = new Object[k];
            System.arraycopy(((Object) (aobj)), 0, ((Object) (elementData)), 0, elementCount);
        }
    }

    public synchronized void setSize(int i) {
        super.modCount++;
        if (i > elementCount) {
            ensureCapacityHelper(i);
        } else {
            for (int j = i; j < elementCount; j++)
                elementData[j] = null;

        }
        elementCount = i;
    }

    public int capacity() {
        return elementData.length;
    }

    public int size() {
        return elementCount;
    }

    public boolean isEmpty() {
        return elementCount == 0;
    }

    public Enumeration elements() {
        return new Enumeration() {

            public boolean hasMoreElements() {
                return count < elementCount;
            }

            public Object nextElement() {
                synchronized (Vector.this) {
                    if (count < elementCount) {
                        Object obj = elementData[count++];
                        return obj;
                    }
                }
                throw new NoSuchElementException("Vector java.util.Enumeration");
            }

            int count;

        };
    }

    public boolean contains(Object obj) {
        return indexOf(obj, 0) >= 0;
    }

    public int indexOf(Object obj) {
        return indexOf(obj, 0);
    }

    public synchronized int indexOf(Object obj, int i) {
        if (obj == null) {
            for (int j = i; j < elementCount; j++)
                if (elementData[j] == null)
                    return j;

        } else {
            for (int k = i; k < elementCount; k++)
                if (obj.equals(elementData[k]))
                    return k;

        }
        return -1;
    }

    public int lastIndexOf(Object obj) {
        return lastIndexOf(obj, elementCount - 1);
    }

    public synchronized int lastIndexOf(Object obj, int i) {
        if (obj == null) {
            for (int j = i; j >= 0; j--)
                if (elementData[j] == null)
                    return j;

        } else {
            for (int k = i; k >= 0; k--)
                if (obj.equals(elementData[k]))
                    return k;

        }
        return -1;
    }

    public synchronized Object elementAt(int i) {
        if (i >= elementCount)
            throw new ArrayIndexOutOfBoundsException(i + " >= " + elementCount);
        try {
            return elementData[i];
        } catch (ArrayIndexOutOfBoundsException _ex) {
            throw new ArrayIndexOutOfBoundsException(i + " < 0");
        }
    }

    public synchronized Object firstElement() {
        if (elementCount == 0)
            throw new NoSuchElementException();
        else
            return elementData[0];
    }

    public synchronized Object lastElement() {
        if (elementCount == 0)
            throw new NoSuchElementException();
        else
            return elementData[elementCount - 1];
    }

    public synchronized void setElementAt(Object obj, int i) {
        if (i >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(i + " >= " + elementCount);
        } else {
            elementData[i] = obj;
        }
    }

    public synchronized void removeElementAt(int i) {
        super.modCount++;
        if (i >= elementCount)
            throw new ArrayIndexOutOfBoundsException(i + " >= " + elementCount);
        if (i < 0)
            throw new ArrayIndexOutOfBoundsException(i);
        int j = elementCount - i - 1;
        if (j > 0)
            System.arraycopy(((Object) (elementData)), i + 1, ((Object) (elementData)), i, j);
        elementCount--;
        elementData[elementCount] = null;
    }

    public synchronized void insertElementAt(Object obj, int i) {
        super.modCount++;
        if (i >= elementCount + 1) {
            throw new ArrayIndexOutOfBoundsException(i + " > " + elementCount);
        } else {
            ensureCapacityHelper(elementCount + 1);
            System.arraycopy(((Object) (elementData)), i, ((Object) (elementData)), i + 1, elementCount - i);
            elementData[i] = obj;
            elementCount++;
            return;
        }
    }

    public synchronized void addElement(Object obj) {
        super.modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = obj;
    }

    public synchronized boolean removeElement(Object obj) {
        super.modCount++;
        int i = indexOf(obj);
        if (i >= 0) {
            removeElementAt(i);
            return true;
        } else {
            return false;
        }
    }

    public synchronized void removeAllElements() {
        for (int i = 0; i < elementCount; i++)
            elementData[i] = null;

        elementCount = 0;
    }

    public synchronized Object[]
    toArray() {
        Object aobj[] = new Object[elementCount];
        System.arraycopy(((Object) (elementData)), 0, ((Object) (aobj)), 0, elementCount);
        return aobj;
    }

    public synchronized Object[]
    toArray(Object aobj[]) {
        if (aobj.length < elementCount)
            aobj = new Object[elementCount];
        System.arraycopy(((Object) (elementData)), 0, ((Object) (aobj)), 0, elementCount);
        if (aobj.length > elementCount)
            aobj[elementCount] = null;
        return aobj;
    }

    public synchronized Object get(int i) {
        if (i >= elementCount)
            throw new ArrayIndexOutOfBoundsException(i);
        else
            return elementData[i];
    }

    public synchronized Object set(int i, Object obj) {
        if (i >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(i);
        } else {
            Object obj1 = elementData[i];
            elementData[i] = obj;
            return obj1;
        }
    }

    public synchronized boolean add(Object obj) {
        super.modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = obj;
        return true;
    }

    public boolean remove(Object obj) {
        return removeElement(obj);
    }

    public void add(int i, Object obj) {
        insertElementAt(obj, i);
    }

    public synchronized Object remove(int i) {
        super.modCount++;
        if (i >= elementCount)
            throw new ArrayIndexOutOfBoundsException(i);
        Object obj = elementData[i];
        int j = elementCount - i - 1;
        if (j > 0)
            System.arraycopy(((Object) (elementData)), i + 1, ((Object) (elementData)), i, j);
        elementData[--elementCount] = null;
        return obj;
    }

    public void clear() {
        removeAllElements();
    }

    public synchronized boolean containsAll(Collection collection) {
        return super.containsAll(collection);
    }

    public synchronized boolean addAll(Collection collection) {
        super.modCount++;
        int i = collection.size();
        ensureCapacityHelper(elementCount + i);
        Iterator iterator = collection.iterator();
        for (int j = 0; j < i; j++)
            elementData[elementCount++] = iterator.next();

        return i != 0;
    }

    public synchronized boolean removeAll(Collection collection) {
        return super.removeAll(collection);
    }

    public synchronized boolean retainAll(Collection collection) {
        return super.retainAll(collection);
    }

    public synchronized boolean addAll(int i, Collection collection) {
        super.modCount++;
        if (i < 0 || i > elementCount)
            throw new ArrayIndexOutOfBoundsException(i);
        int j = collection.size();
        ensureCapacityHelper(elementCount + j);
        int k = elementCount - i;
        if (k > 0)
            System.arraycopy(((Object) (elementData)), i, ((Object) (elementData)), i + j, k);
        Iterator iterator = collection.iterator();
        for (int l = 0; l < j; l++)
            elementData[i++] = iterator.next();

        elementCount += j;
        return j != 0;
    }

    public synchronized boolean equals(Object obj) {
        return super.equals(obj);
    }

    public synchronized int hashCode() {
        return super.hashCode();
    }

    public synchronized String toString() {
        return super.toString();
    }

    public List subList(int i, int j) {
        return Collections.synchronizedList(super.subList(i, j), this);
    }

    protected void removeRange(int i, int j) {
        super.modCount++;
        int k = elementCount - j;
        System.arraycopy(((Object) (elementData)), j, ((Object) (elementData)), i, k);
        for (int l = elementCount - (j - i); elementCount != l;)
            elementData[--elementCount] = null;

    }

    protected Object elementData[];
    protected int elementCount;
    protected int capacityIncrement;
    private static final long serialVersionUID = 0xd9977d5b803baf01L;
}
