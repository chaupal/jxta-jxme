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
