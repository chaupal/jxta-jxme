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

class SubList extends AbstractList {

    SubList(AbstractList abstractlist, int i, int j) {
        if (i < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + i);
        if (j > abstractlist.size())
            throw new IndexOutOfBoundsException("toIndex = " + j);
        if (i > j) {
            throw new IllegalArgumentException("fromIndex(" + i + ") > toIndex(" + j + ")");
        } else {
            l = abstractlist;
            offset = i;
            size = j - i;
            expectedModCount = l.modCount;
            return;
        }
    }

    public Object set(int i, Object obj) {
        rangeCheck(i);
        checkForComodification();
        return l.set(i + offset, obj);
    }

    public Object get(int i) {
        rangeCheck(i);
        checkForComodification();
        return l.get(i + offset);
    }

    public int size() {
        checkForComodification();
        return size;
    }

    public void add(int i, Object obj) {
        if (i < 0 || i > size) {
            throw new IndexOutOfBoundsException();
        } else {
            checkForComodification();
            l.add(i + offset, obj);
            expectedModCount = l.modCount;
            size++;
            super.modCount++;
            return;
        }
    }

    public Object remove(int i) {
        rangeCheck(i);
        checkForComodification();
        Object obj = l.remove(i + offset);
        expectedModCount = l.modCount;
        size--;
        super.modCount++;
        return obj;
    }

    protected void removeRange(int i, int j) {
        checkForComodification();
        l.removeRange(i + offset, j + offset);
        expectedModCount = l.modCount;
        size -= j - i;
        super.modCount++;
    }

    public boolean addAll(Collection collection) {
        return addAll(size, collection);
    }

    public boolean addAll(int i, Collection collection) {
        if (i < 0 || i > size)
            throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + size);
        int j = collection.size();
        if (j == 0) {
            return false;
        } else {
            checkForComodification();
            l.addAll(offset + i, collection);
            expectedModCount = l.modCount;
            size += j;
            super.modCount++;
            return true;
        }
    }

    public Iterator iterator() {
        return listIterator();
    }

    public ListIterator listIterator(final int final_j) {
        checkForComodification();
        if (final_j < 0 || final_j > size)
            throw new IndexOutOfBoundsException("Index: " + final_j + ", Size: " + size);
        else
            return new ListIterator() {

                public boolean hasNext() {
                    return nextIndex() < size;
                }

                public Object next() {
                    if (hasNext())
                        return i.next();
                    else
                        throw new NoSuchElementException();
                }

                public boolean hasPrevious() {
                    return previousIndex() >= 0;
                }

                public Object previous() {
                    if (hasPrevious())
                        return i.previous();
                    else
                        throw new NoSuchElementException();
                }

                public int nextIndex() {
                    return i.nextIndex() - offset;
                }

                public int previousIndex() {
                    return i.previousIndex() - offset;
                }

                public void remove() {
                    i.remove();
                    expectedModCount = l.modCount;
                    size--;
                    modCount++;
                }

                public void set(Object obj) {
                    i.set(obj);
                }

                public void add(Object obj) {
                    i.add(obj);
                    expectedModCount = l.modCount;
                    size++;
                    modCount++;
                }

                private ListIterator i;


                {
                    i = l.listIterator(final_j + offset);
                }
            };
    }

    public List subList(int i, int j) {
        return new SubList(this, i, j);
    }

    private void rangeCheck(int i) {
        if (i < 0 || i >= size)
            throw new IndexOutOfBoundsException("Index: " + i + ",Size: " + size);
        else
            return;
    }

    private void checkForComodification() {
        if (l.modCount != expectedModCount)
            throw new ConcurrentModificationException();
        else
            return;
    }

    private AbstractList l;
    private int offset;
    private int size;
    private int expectedModCount;


}
