
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

public abstract class AbstractList extends AbstractCollection implements List {
    private class Itr implements Iterator {

        public boolean hasNext() {
            return cursor != size();
        }

        public Object next() {
            try {
                Object obj = get(cursor);
                checkForComodification();
                lastRet = cursor++;
                return obj;
            }
            catch (IndexOutOfBoundsException _ex) {
                checkForComodification();
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (lastRet == -1)
                throw new IllegalStateException();
            try {
                AbstractList.this.remove(lastRet);
                if (lastRet < cursor)
                    cursor--;
                lastRet = -1;
                int i = modCount;
                if (i - expectedModCount > 1) {
                    throw new ConcurrentModificationException();
                } else {
                    expectedModCount = i;
                    return;
                }
            }
            catch (IndexOutOfBoundsException _ex) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            else
                return;
        }

        int cursor;
        int lastRet;
        int expectedModCount;

        Itr() {
            lastRet = -1;
            expectedModCount = modCount;
        }
    }

    private class ListItr extends Itr
            implements ListIterator {

        public boolean hasPrevious() {
            return super.cursor != 0;
        }

        public Object previous() {
            try {
                Object obj = get(--super.cursor);
                checkForComodification();
                super.lastRet = super.cursor;
                return obj;
            }
            catch (IndexOutOfBoundsException _ex) {
                checkForComodification();
            }
            throw new NoSuchElementException();
        }

        public int nextIndex() {
            return super.cursor;
        }

        public int previousIndex() {
            return super.cursor - 1;
        }

        public void set(Object obj) {
            if (super.lastRet == -1)
                throw new IllegalStateException();
            try {
                AbstractList.this.set(super.lastRet, obj);
                int i = modCount;
                if (i - super.expectedModCount > 1) {
                    throw new ConcurrentModificationException();
                } else {
                    super.expectedModCount = i;
                    return;
                }
            }
            catch (IndexOutOfBoundsException _ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(Object obj) {
            try {
                AbstractList.this.add(super.cursor++, obj);
                super.lastRet = -1;
                int i = modCount;
                if (i - super.expectedModCount > 1) {
                    throw new ConcurrentModificationException();
                } else {
                    super.expectedModCount = i;
                    return;
                }
            }
            catch (IndexOutOfBoundsException _ex) {
                throw new ConcurrentModificationException();
            }
        }

        ListItr(int i) {
            super.cursor = i;
        }
    }


    protected AbstractList() {
    }

    public boolean add(Object obj) {
        add(size(), obj);
        return true;
    }

    public abstract Object get(int i);

    public Object set(int i, Object obj) {
        throw new UnsupportedOperationException();
    }

    public void add(int i, Object obj) {
        throw new UnsupportedOperationException();
    }

    public Object remove(int i) {
        throw new UnsupportedOperationException();
    }

    public int indexOf(Object obj) {
        ListIterator listiterator = listIterator();
        if (obj == null)
            while (listiterator.hasNext())
                if (listiterator.next() == null)
                    return listiterator.previousIndex();
                else
                    while (listiterator.hasNext())
                        if (obj.equals(listiterator.next()))
                            return listiterator.previousIndex();
        return -1;
    }

    public int lastIndexOf(Object obj) {
        ListIterator listiterator = listIterator(size());
        if (obj == null)
            while (listiterator.hasPrevious())
                if (listiterator.previous() == null)
                    return listiterator.nextIndex();
                else
                    while (listiterator.hasPrevious())
                        if (obj.equals(listiterator.previous()))
                            return listiterator.nextIndex();
        return -1;
    }

    public void clear() {
        removeRange(0, size());
    }

    public boolean addAll(int i, Collection collection) {
        boolean flag = false;
        for (Iterator iterator1 = collection.iterator(); iterator1.hasNext();) {
            add(i++, iterator1.next());
            flag = true;
        }

        return flag;
    }

    public Iterator iterator() {
        return new Itr();
    }

    public ListIterator listIterator() {
        return listIterator(0);
    }

    public ListIterator listIterator(int i) {
        if (i < 0 || i > size())
            throw new IndexOutOfBoundsException("Index: " + i);
        else
            return new ListItr(i);
    }

    public List subList(int i, int j) {
        return new SubList(this, i, j);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof List))
            return false;
        ListIterator listiterator = listIterator();
        ListIterator listiterator1;
        for (listiterator1 = ((List) obj).listIterator(); listiterator.hasNext() && listiterator1.hasNext();) {
            Object obj1 = listiterator.next();
            Object obj2 = listiterator1.next();
            if (obj1 != null ? !obj1.equals(obj2) : obj2 != null && true)
                return false;
        }

        return !listiterator.hasNext() && !listiterator1.hasNext();
    }

    public int hashCode() {
        int i = 1;
        for (Iterator iterator1 = iterator(); iterator1.hasNext();) {
            Object obj = iterator1.next();
            i = 31 * i + (obj != null ? obj.hashCode() : 0);
        }

        return i;
    }

    protected void removeRange(int i, int j) {
        ListIterator listiterator = listIterator(i);
        int k = 0;
        for (int l = j - i; k < l; k++) {
            listiterator.next();
            listiterator.remove();
        }

    }

    protected transient int modCount;
}
