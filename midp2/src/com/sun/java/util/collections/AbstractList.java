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
