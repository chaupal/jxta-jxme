
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

public class LinkedList extends AbstractSequentialList
        implements List {
    private class ListItr
            implements ListIterator {

        public boolean hasNext() {
            return nextIndex != size;
        }

        public Object next() {
            checkForComodification();
            if (nextIndex == size) {
                throw new NoSuchElementException();
            } else {
                lastReturned = next;
                next = next.next;
                nextIndex++;
                return lastReturned.element;
            }
        }

        public boolean hasPrevious() {
            return nextIndex != 0;
        }

        public Object previous() {
            if (nextIndex == 0) {
                throw new NoSuchElementException();
            } else {
                lastReturned = next = next.previous;
                nextIndex--;
                checkForComodification();
                return lastReturned.element;
            }
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            LinkedList.this.remove(lastReturned);
            if (next == lastReturned)
                next = lastReturned.next;
            else
                nextIndex--;
            lastReturned = header;
            expectedModCount++;
        }

        public void set(Object obj) {
            if (lastReturned == header) {
                throw new IllegalStateException();
            } else {
                checkForComodification();
                lastReturned.element = obj;
                return;
            }
        }

        public void add(Object obj) {
            checkForComodification();
            lastReturned = header;
            addBefore(obj, next);
            nextIndex++;
            expectedModCount++;
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            else
                return;
        }

        private Entry lastReturned;
        private Entry next;
        private int nextIndex;
        private int expectedModCount;

        ListItr(int i) {
            lastReturned = header;
            expectedModCount = modCount;
            if (i < 0 || i > size)
                throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + size);
            if (i < size / 2) {
                next = header.next;
                for (nextIndex = 0; nextIndex < i; nextIndex++)
                    next = next.next;

                return;
            }
            next = header;
            for (nextIndex = size; nextIndex > i; nextIndex--)
                next = next.previous;

        }
    }

    private static class Entry {

        Object element;
        Entry next;
        Entry previous;

        Entry(Object obj, Entry entry1, Entry entry2) {
            element = obj;
            next = entry1;
            previous = entry2;
        }
    }


    public LinkedList() {
        header = new Entry(null, null, null);
        header.next = header.previous = header;
    }

    public LinkedList(Collection collection) {
        this();
        addAll(collection);
    }

    public Object getFirst() {
        if (size == 0)
            throw new NoSuchElementException();
        else
            return header.next.element;
    }

    public Object getLast() {
        if (size == 0)
            throw new NoSuchElementException();
        else
            return header.previous.element;
    }

    public Object removeFirst() {
        Object obj = header.next.element;
        remove(header.next);
        return obj;
    }

    public Object removeLast() {
        Object obj = header.previous.element;
        remove(header.previous);
        return obj;
    }

    public void addFirst(Object obj) {
        addBefore(obj, header.next);
    }

    public void addLast(Object obj) {
        addBefore(obj, header);
    }

    public boolean contains(Object obj) {
        return indexOf(obj) != -1;
    }

    public int size() {
        return size;
    }

    public boolean add(Object obj) {
        addBefore(obj, header);
        return true;
    }

    public boolean remove(Object obj) {
        if (obj == null) {
            for (Entry entry1 = header.next; entry1 != header; entry1 = entry1.next)
                if (entry1.element == null) {
                    remove(entry1);
                    return true;
                }

        } else {
            for (Entry entry2 = header.next; entry2 != header; entry2 = entry2.next)
                if (obj.equals(entry2.element)) {
                    remove(entry2);
                    return true;
                }

        }
        return false;
    }

    public boolean addAll(Collection collection) {
        return addAll(size, collection);
    }

    public boolean addAll(int i, Collection collection) {
        int j = collection.size();
        if (j == 0)
            return false;
        super.modCount++;
        Entry entry1 = i != size ? entry(i) : header;
        Entry entry2 = entry1.previous;
        Iterator iterator = collection.iterator();
        for (int k = 0; k < j; k++) {
            Entry entry3 = new Entry(iterator.next(), entry1, entry2);
            entry2.next = entry3;
            entry2 = entry3;
        }

        entry1.previous = entry2;
        size += j;
        return true;
    }

    public void clear() {
        super.modCount++;
        header.next = header.previous = header;
        size = 0;
    }

    public Object get(int i) {
        return entry(i).element;
    }

    public Object set(int i, Object obj) {
        Entry entry1 = entry(i);
        Object obj1 = entry1.element;
        entry1.element = obj;
        return obj1;
    }

    public void add(int i, Object obj) {
        addBefore(obj, i != size ? entry(i) : header);
    }

    public Object remove(int i) {
        Entry entry1 = entry(i);
        remove(entry1);
        return entry1.element;
    }

    private Entry entry(int i) {
        if (i < 0 || i >= size)
            throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + size);
        Entry entry1 = header;
        if (i < size / 2) {
            for (int j = 0; j <= i; j++)
                entry1 = entry1.next;

        } else {
            for (int k = size; k > i; k--)
                entry1 = entry1.previous;

        }
        return entry1;
    }

    public int indexOf(Object obj) {
        int i = 0;
        if (obj == null) {
            for (Entry entry1 = header.next; entry1 != header; entry1 = entry1.next) {
                if (entry1.element == null)
                    return i;
                i++;
            }

        } else {
            for (Entry entry2 = header.next; entry2 != header; entry2 = entry2.next) {
                if (obj.equals(entry2.element))
                    return i;
                i++;
            }

        }
        return -1;
    }

    public int lastIndexOf(Object obj) {
        int i = size;
        if (obj == null) {
            for (Entry entry1 = header.previous; entry1 != header; entry1 = entry1.previous) {
                i--;
                if (entry1.element == null)
                    return i;
            }

        } else {
            for (Entry entry2 = header.previous; entry2 != header; entry2 = entry2.previous) {
                i--;
                if (obj.equals(entry2.element))
                    return i;
            }

        }
        return -1;
    }

    public ListIterator listIterator(int i) {
        return new ListItr(i);
    }

    private Entry addBefore(Object obj, Entry entry1) {
        Entry entry2 = new Entry(obj, entry1, entry1.previous);
        entry2.previous.next = entry2;
        entry2.next.previous = entry2;
        size++;
        super.modCount++;
        return entry2;
    }

    private void remove(Entry entry1) {
        if (entry1 == header) {
            throw new NoSuchElementException();
        } else {
            entry1.previous.next = entry1.next;
            entry1.next.previous = entry1.previous;
            size--;
            super.modCount++;
            return;
        }
    }

//    public Object clone()
//    {
//        return new LinkedList(this);
//    }

    public Object[] toArray() {
        Object aobj[] = new Object[size];
        int i = 0;
        for (Entry entry1 = header.next; entry1 != header; entry1 = entry1.next)
            aobj[i++] = entry1.element;

        return aobj;
    }

    public Object[] toArray(Object aobj[]) {
        if (aobj.length < size)
            aobj = new Object[size];
//            aobj = (Object[])Array.newInstance(((Object) (aobj)).getClass().getComponentType(), size);
        int i = 0;
        for (Entry entry1 = header.next; entry1 != header; entry1 = entry1.next)
            aobj[i++] = entry1.element;

        if (aobj.length > size)
            aobj[size] = null;
        return aobj;
    }

    private transient Entry header;
    private transient int size;
}
