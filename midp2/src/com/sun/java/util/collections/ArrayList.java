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
// FDM
//            aobj = (Object[])Array.newInstance(((Object) (aobj)).getClass().getComponentType(), size);
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
