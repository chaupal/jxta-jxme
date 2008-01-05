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
