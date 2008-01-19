
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
