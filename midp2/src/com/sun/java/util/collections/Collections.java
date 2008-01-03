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

public class Collections {
    static class UnmodifiableCollection
            implements Collection {

        public int size() {
            return c.size();
        }

        public boolean isEmpty() {
            return c.isEmpty();
        }

        public boolean contains(Object obj) {
            return c.contains(obj);
        }

        public Object[] toArray() {
            return c.toArray();
        }

        public Object[] toArray(Object aobj[]) {
            return c.toArray(aobj);
        }

        public Iterator iterator() {
            return new Iterator() {

                public boolean hasNext() {
                    return i.hasNext();
                }

                public Object next() {
                    return i.next();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                Iterator i;


                {
                    i = c.iterator();
                }
            };
        }

        public boolean add(Object obj) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object obj) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection collection) {
            return c.containsAll(collection);
        }

        public boolean addAll(Collection collection) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection collection) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        Collection c;

        UnmodifiableCollection(Collection collection) {
            c = collection;
        }
    }

    static class UnmodifiableSet extends UnmodifiableCollection
            implements Set {

        public boolean equals(Object obj) {
            return super.c.equals(obj);
        }

        public int hashCode() {
            return super.c.hashCode();
        }

        UnmodifiableSet(Set set) {
            super(set);
        }
    }

    static class UnmodifiableSortedSet extends UnmodifiableSet
            implements SortedSet {

        public Comparator comparator() {
            return ss.comparator();
        }

        public SortedSet subSet(Object obj, Object obj1) {
            return new UnmodifiableSortedSet(ss.subSet(obj, obj1));
        }

        public SortedSet headSet(Object obj) {
            return new UnmodifiableSortedSet(ss.headSet(obj));
        }

        public SortedSet tailSet(Object obj) {
            return new UnmodifiableSortedSet(ss.tailSet(obj));
        }

        public Object first() {
            return ss.first();
        }

        public Object last() {
            return ss.last();
        }

        private SortedSet ss;

        UnmodifiableSortedSet(SortedSet sortedset) {
            super(sortedset);
            ss = sortedset;
        }
    }

    static class UnmodifiableList extends UnmodifiableCollection
            implements List {

        public boolean equals(Object obj) {
            return list.equals(obj);
        }

        public int hashCode() {
            return list.hashCode();
        }

        public Object get(int i) {
            return list.get(i);
        }

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
            return list.indexOf(obj);
        }

        public int lastIndexOf(Object obj) {
            return list.lastIndexOf(obj);
        }

        public boolean addAll(int i, Collection collection) {
            throw new UnsupportedOperationException();
        }

        public ListIterator listIterator() {
            return listIterator(0);
        }

        public ListIterator listIterator(final int final_j) {
            return new ListIterator() {

                public boolean hasNext() {
                    return i.hasNext();
                }

                public Object next() {
                    return i.next();
                }

                public boolean hasPrevious() {
                    return i.hasPrevious();
                }

                public Object previous() {
                    return i.previous();
                }

                public int nextIndex() {
                    return i.nextIndex();
                }

                public int previousIndex() {
                    return i.previousIndex();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public void set(Object obj) {
                    throw new UnsupportedOperationException();
                }

                public void add(Object obj) {
                    throw new UnsupportedOperationException();
                }

                ListIterator i;


                {
                    i = list.listIterator(final_j);
                }
            };
        }

        public List subList(int i, int j) {
            return new UnmodifiableList(list.subList(i, j));
        }

        private List list;


        UnmodifiableList(List list1) {
            super(list1);
            list = list1;
        }
    }

    private static class UnmodifiableMap
            implements Map {
        static class UnmodifiableEntrySet extends UnmodifiableSet {
            private static class UnmodifiableEntry
                    implements Map.Entry {

                public Object getKey() {
                    return e.getKey();
                }

                public Object getValue() {
                    return e.getValue();
                }

                public Object setValue(Object obj) {
                    throw new UnsupportedOperationException();
                }

                public int hashCode() {
                    return e.hashCode();
                }

                public boolean equals(Object obj) {
                    if (!(obj instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry) obj;
                    return Collections.eq(e.getKey(), entry.getKey()) && Collections.eq(e.getValue(), entry.getValue());
                }

                public String toString() {
                    return e.toString();
                }

                private Map.Entry e;

                UnmodifiableEntry(Map.Entry entry) {
                    e = entry;
                }
            }


            public Iterator iterator() {
                return new Iterator() {

                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    public Object next() {
                        return new UnmodifiableEntry((Map.Entry) i.next());
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                    Iterator i;


                    {
                        i = c.iterator();
                    }
                };
            }

            public Object[] toArray() {
                Object aobj[] = super.c.toArray();
                for (int i = 0; i < aobj.length; i++)
                    aobj[i] = new UnmodifiableEntry((Map.Entry) aobj[i]);

                return aobj;
            }

            public Object[] toArray(Object aobj[]) {
                Object aobj1[] = super.c.toArray(aobj.length != 0 ? new Object[0] : aobj);
                for (int i = 0; i < aobj1.length; i++)
                    aobj1[i] = new UnmodifiableEntry((Map.Entry) aobj1[i]);

                if (aobj1.length > aobj.length)
                    return aobj1;
                System.arraycopy(((Object) (aobj1)), 0, ((Object) (aobj)), 0, aobj1.length);
                if (aobj.length > aobj1.length)
                    aobj[aobj1.length] = null;
                return aobj;
            }

            public boolean contains(Object obj) {
                if (!(obj instanceof Map.Entry))
                    return false;
                else
                    return super.c.contains(new UnmodifiableEntry((Map.Entry) obj));
            }

            public boolean containsAll(Collection collection) {
                for (Iterator iterator1 = collection.iterator(); iterator1.hasNext();)
                    if (!contains(iterator1.next()))
                        return false;

                return true;
            }

            public boolean equals(Object obj) {
                if (obj == this)
                    return true;
                if (!(obj instanceof Set))
                    return false;
                Set set = (Set) obj;
                if (set.size() != super.c.size())
                    return false;
                else
                    return containsAll(set);
            }

            UnmodifiableEntrySet(Set set) {
                super(set);
            }
        }


        public int size() {
            return m.size();
        }

        public boolean isEmpty() {
            return m.isEmpty();
        }

        public boolean containsKey(Object obj) {
            return m.containsKey(obj);
        }

        public boolean containsValue(Object obj) {
            return m.containsValue(obj);
        }

        public Object get(Object obj) {
            return m.get(obj);
        }

        public Object put(Object obj, Object obj1) {
            throw new UnsupportedOperationException();
        }

        public Object remove(Object obj) {
            throw new UnsupportedOperationException();
        }

        public void putAll(Map map) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public Set keySet() {
            if (keySet == null)
                keySet = Collections.unmodifiableSet(m.keySet());
            return keySet;
        }

        public Set entrySet() {
            if (entrySet == null)
                entrySet = new UnmodifiableEntrySet(m.entrySet());
            return entrySet;
        }

        public Collection values() {
            if (values == null)
                values = Collections.unmodifiableCollection(m.values());
            return values;
        }

        public boolean equals(Object obj) {
            return m.equals(obj);
        }

        public int hashCode() {
            return m.hashCode();
        }

        private final Map m;
        private transient Set keySet;
        private transient Set entrySet;
        private transient Collection values;

        UnmodifiableMap(Map map) {
            m = map;
        }
    }

    static class UnmodifiableSortedMap extends UnmodifiableMap
            implements SortedMap {

        public Comparator comparator() {
            return sm.comparator();
        }

        public SortedMap subMap(Object obj, Object obj1) {
            return new UnmodifiableSortedMap(sm.subMap(obj, obj1));
        }

        public SortedMap headMap(Object obj) {
            return new UnmodifiableSortedMap(sm.headMap(obj));
        }

        public SortedMap tailMap(Object obj) {
            return new UnmodifiableSortedMap(sm.tailMap(obj));
        }

        public Object firstKey() {
            return sm.firstKey();
        }

        public Object lastKey() {
            return sm.lastKey();
        }

        private SortedMap sm;

        UnmodifiableSortedMap(SortedMap sortedmap) {
            super(sortedmap);
            sm = sortedmap;
        }
    }

    static class SynchronizedCollection
            implements Collection {

        public int size() {
            synchronized (mutex) {
                int i = c.size();
                return i;
            }
        }

        public boolean isEmpty() {
            synchronized (mutex) {
                boolean flag = c.isEmpty();
                return flag;
            }
        }

        public boolean contains(Object obj) {
            synchronized (mutex) {
                boolean flag = c.contains(obj);
                return flag;
            }
        }

        public Object[] toArray() {
            synchronized (mutex) {
                Object aobj[] = c.toArray();
                return aobj;
            }
        }

        public Object[] toArray(Object aobj[]) {
            synchronized (mutex) {
                Object aobj1[] = c.toArray(aobj);
                return aobj1;
            }
        }

        public Iterator iterator() {
            return c.iterator();
        }

        public boolean add(Object obj) {
            synchronized (mutex) {
                boolean flag = c.add(obj);
                return flag;
            }
        }

        public boolean remove(Object obj) {
            synchronized (mutex) {
                boolean flag = c.remove(obj);
                return flag;
            }
        }

        public boolean containsAll(Collection collection) {
            synchronized (mutex) {
                boolean flag = c.containsAll(collection);
                return flag;
            }
        }

        public boolean addAll(Collection collection) {
            synchronized (mutex) {
                boolean flag = c.addAll(collection);
                return flag;
            }
        }

        public boolean removeAll(Collection collection) {
            synchronized (mutex) {
                boolean flag = c.removeAll(collection);
                return flag;
            }
        }

        public boolean retainAll(Collection collection) {
            synchronized (mutex) {
                boolean flag = c.retainAll(collection);
                return flag;
            }
        }

        public void clear() {
            synchronized (mutex) {
                c.clear();
            }
        }

        Collection c;
        Object mutex;

        SynchronizedCollection(Collection collection) {
            c = collection;
            mutex = this;
        }

        SynchronizedCollection(Collection collection, Object obj) {
            c = collection;
            mutex = obj;
        }
    }

    static class SynchronizedSet extends SynchronizedCollection
            implements Set {

        public boolean equals(Object obj) {
            synchronized (super.mutex) {
                boolean flag = super.c.equals(obj);
                return flag;
            }
        }

        public int hashCode() {
            synchronized (super.mutex) {
                int i = super.c.hashCode();
                return i;
            }
        }

        SynchronizedSet(Set set) {
            super(set);
        }

        SynchronizedSet(Set set, Object obj) {
            super(set, obj);
        }
    }

    static class SynchronizedSortedSet extends SynchronizedSet
            implements SortedSet {

        public Comparator comparator() {
            synchronized (super.mutex) {
                Comparator comparator1 = ss.comparator();
                return comparator1;
            }
        }

        public SortedSet subSet(Object obj, Object obj1) {
            synchronized (super.mutex) {
                SynchronizedSortedSet synchronizedsortedset = new SynchronizedSortedSet(ss.subSet(obj, obj1), super.mutex);
                return synchronizedsortedset;
            }
        }

        public SortedSet headSet(Object obj) {
            synchronized (super.mutex) {
                SynchronizedSortedSet synchronizedsortedset = new SynchronizedSortedSet(ss.headSet(obj), super.mutex);
                return synchronizedsortedset;
            }
        }

        public SortedSet tailSet(Object obj) {
            synchronized (super.mutex) {
                SynchronizedSortedSet synchronizedsortedset = new SynchronizedSortedSet(ss.tailSet(obj), super.mutex);
                return synchronizedsortedset;
            }
        }

        public Object first() {
            synchronized (super.mutex) {
                Object obj = ss.first();
                return obj;
            }
        }

        public Object last() {
            synchronized (super.mutex) {
                Object obj = ss.last();
                return obj;
            }
        }

        private SortedSet ss;

        SynchronizedSortedSet(SortedSet sortedset) {
            super(sortedset);
            ss = sortedset;
        }

        SynchronizedSortedSet(SortedSet sortedset, Object obj) {
            super(sortedset, obj);
            ss = sortedset;
        }
    }

    static class SynchronizedList extends SynchronizedCollection
            implements List {

        public boolean equals(Object obj) {
            synchronized (super.mutex) {
                boolean flag = list.equals(obj);
                return flag;
            }
        }

        public int hashCode() {
            synchronized (super.mutex) {
                int i = list.hashCode();
                return i;
            }
        }

        public Object get(int i) {
            synchronized (super.mutex) {
                Object obj = list.get(i);
                return obj;
            }
        }

        public Object set(int i, Object obj) {
            synchronized (super.mutex) {
                Object obj1 = list.set(i, obj);
                return obj1;
            }
        }

        public void add(int i, Object obj) {
            synchronized (super.mutex) {
                list.add(i, obj);
            }
        }

        public Object remove(int i) {
            synchronized (super.mutex) {
                Object obj = list.remove(i);
                return obj;
            }
        }

        public int indexOf(Object obj) {
            synchronized (super.mutex) {
                int i = list.indexOf(obj);
                return i;
            }
        }

        public int lastIndexOf(Object obj) {
            synchronized (super.mutex) {
                int i = list.lastIndexOf(obj);
                return i;
            }
        }

        public boolean addAll(int i, Collection collection) {
            synchronized (super.mutex) {
                boolean flag = list.addAll(i, collection);
                return flag;
            }
        }

        public ListIterator listIterator() {
            return list.listIterator();
        }

        public ListIterator listIterator(int i) {
            return list.listIterator(i);
        }

        public List subList(int i, int j) {
            synchronized (super.mutex) {
                SynchronizedList synchronizedlist = new SynchronizedList(list.subList(i, j), super.mutex);
                return synchronizedlist;
            }
        }

        private List list;

        SynchronizedList(List list1) {
            super(list1);
            list = list1;
        }

        SynchronizedList(List list1, Object obj) {
            super(list1, obj);
            list = list1;
        }
    }

    private static class SynchronizedMap
            implements Map {

        public int size() {
            synchronized (mutex) {
                int i = m.size();
                return i;
            }
        }

        public boolean isEmpty() {
            synchronized (mutex) {
                boolean flag = m.isEmpty();
                return flag;
            }
        }

        public boolean containsKey(Object obj) {
            synchronized (mutex) {
                boolean flag = m.containsKey(obj);
                return flag;
            }
        }

        public boolean containsValue(Object obj) {
            synchronized (mutex) {
                boolean flag = m.containsValue(obj);
                return flag;
            }
        }

        public Object get(Object obj) {
            synchronized (mutex) {
                Object obj1 = m.get(obj);
                return obj1;
            }
        }

        public Object put(Object obj, Object obj1) {
            synchronized (mutex) {
                Object obj2 = m.put(obj, obj1);
                return obj2;
            }
        }

        public Object remove(Object obj) {
            synchronized (mutex) {
                Object obj1 = m.remove(obj);
                return obj1;
            }
        }

        public void putAll(Map map) {
            synchronized (mutex) {
                m.putAll(map);
            }
        }

        public void clear() {
            synchronized (mutex) {
                m.clear();
            }
        }

        public Set keySet() {
            synchronized (mutex) {
                if (keySet == null)
                    keySet = new SynchronizedSet(m.keySet(), this);
                Set set = keySet;
                return set;
            }
        }

        public Set entrySet() {
            synchronized (mutex) {
                if (entrySet == null)
                    entrySet = new SynchronizedSet(m.entrySet(), this);
                Set set = entrySet;
                return set;
            }
        }

        public Collection values() {
            synchronized (mutex) {
                if (values == null)
                    values = new SynchronizedCollection(m.values(), this);
                Collection collection = values;
                return collection;
            }
        }

        public boolean equals(Object obj) {
            synchronized (mutex) {
                boolean flag = m.equals(obj);
                return flag;
            }
        }

        public int hashCode() {
            synchronized (mutex) {
                int i = m.hashCode();
                return i;
            }
        }

        private Map m;
        Object mutex;
        private transient Set keySet;
        private transient Set entrySet;
        private transient Collection values;

        SynchronizedMap(Map map) {
            m = map;
            mutex = this;
        }

        SynchronizedMap(Map map, Object obj) {
            m = map;
            mutex = obj;
        }
    }

    static class SynchronizedSortedMap extends SynchronizedMap
            implements SortedMap {

        public Comparator comparator() {
            synchronized (super.mutex) {
                Comparator comparator1 = sm.comparator();
                return comparator1;
            }
        }

        public SortedMap subMap(Object obj, Object obj1) {
            synchronized (super.mutex) {
                SynchronizedSortedMap synchronizedsortedmap = new SynchronizedSortedMap(sm.subMap(obj, obj1), super.mutex);
                return synchronizedsortedmap;
            }
        }

        public SortedMap headMap(Object obj) {
            synchronized (super.mutex) {
                SynchronizedSortedMap synchronizedsortedmap = new SynchronizedSortedMap(sm.headMap(obj), super.mutex);
                return synchronizedsortedmap;
            }
        }

        public SortedMap tailMap(Object obj) {
            synchronized (super.mutex) {
                SynchronizedSortedMap synchronizedsortedmap = new SynchronizedSortedMap(sm.tailMap(obj), super.mutex);
                return synchronizedsortedmap;
            }
        }

        public Object firstKey() {
            synchronized (super.mutex) {
                Object obj = sm.firstKey();
                return obj;
            }
        }

        public Object lastKey() {
            synchronized (super.mutex) {
                Object obj = sm.lastKey();
                return obj;
            }
        }

        private SortedMap sm;

        SynchronizedSortedMap(SortedMap sortedmap) {
            super(sortedmap);
            sm = sortedmap;
        }

        SynchronizedSortedMap(SortedMap sortedmap, Object obj) {
            super(sortedmap, obj);
            sm = sortedmap;
        }
    }

    private static class ReverseComparator
            implements Comparator {

        public int compare(Object obj, Object obj1) {
            Comparable comparable = (Comparable) obj;
            Comparable comparable1 = (Comparable) obj1;
            return -comparable.compareTo(comparable1);
        }

        ReverseComparator() {
        }
    }


    private Collections() {
    }

    public static void sort(List list) {
        Object aobj[] = list.toArray();
        Arrays.sort(aobj);
        ListIterator listiterator = list.listIterator();
        for (int i = 0; i < aobj.length; i++) {
            listiterator.next();
            listiterator.set(aobj[i]);
        }

    }

    public static void sort(List list, Comparator comparator) {
        Object aobj[] = list.toArray();
        Arrays.sort(aobj, comparator);
        ListIterator listiterator = list.listIterator();
        for (int i = 0; i < aobj.length; i++) {
            listiterator.next();
            listiterator.set(aobj[i]);
        }

    }

    public static int binarySearch(List list, Object obj) {
        if (list instanceof AbstractSequentialList) {
            ListIterator listiterator;
            for (listiterator = list.listIterator(); listiterator.hasNext();) {
                int j = ((Comparable) listiterator.next()).compareTo(obj);
                if (j == 0)
                    return listiterator.previousIndex();
                if (j > 0)
                    return -listiterator.nextIndex();
            }

            return -listiterator.nextIndex() - 1;
        }
        int i = 0;
        for (int k = list.size() - 1; i <= k;) {
            int l = (i + k) / 2;
            Object obj1 = list.get(l);
            int i1 = ((Comparable) obj1).compareTo(obj);
            if (i1 < 0)
                i = l + 1;
            else if (i1 > 0)
                k = l - 1;
            else
                return l;
        }

        return -(i + 1);
    }

    public static int binarySearch(List list, Object obj, Comparator comparator) {
        if (list instanceof AbstractSequentialList) {
            ListIterator listiterator;
            for (listiterator = list.listIterator(); listiterator.hasNext();) {
                int j = comparator.compare(listiterator.next(), obj);
                if (j == 0)
                    return listiterator.previousIndex();
                if (j > 0)
                    return -listiterator.nextIndex();
            }

            return -listiterator.nextIndex() - 1;
        }
        int i = 0;
        for (int k = list.size() - 1; i <= k;) {
            int l = (i + k) / 2;
            Object obj1 = list.get(l);
            int i1 = comparator.compare(obj1, obj);
            if (i1 < 0)
                i = l + 1;
            else if (i1 > 0)
                k = l - 1;
            else
                return l;
        }

        return -(i + 1);
    }

    public static void reverse(List list) {
        ListIterator listiterator = list.listIterator();
        ListIterator listiterator1 = list.listIterator(list.size());
        int i = 0;
        for (int j = list.size() / 2; i < j; i++) {
            Object obj = listiterator.next();
            listiterator.set(listiterator1.previous());
            listiterator1.set(obj);
        }

    }

    public static void shuffle(List list) {
        shuffle(list, r);
    }

    public static void shuffle(List list, Random random) {
        for (int i = list.size(); i > 1; i--)
            swap(list, i - 1, random.nextInt(i));

    }

    private static void swap(List list, int i, int j) {
        Object obj = list.get(i);
        list.set(i, list.get(j));
        list.set(j, obj);
    }

    public static void fill(List list, Object obj) {
        for (ListIterator listiterator = list.listIterator(); listiterator.hasNext(); listiterator.set(obj))
            listiterator.next();

    }

    public static void copy(List list, List list1) {
        try {
            ListIterator listiterator = list.listIterator();
            for (ListIterator listiterator1 = list1.listIterator(); listiterator1.hasNext(); listiterator.set(listiterator1.next()))
                listiterator.next();

            return;
        } catch (NoSuchElementException _ex) {
            throw new IndexOutOfBoundsException("Source does not fit in dest.");
        }
    }

    public static Object min(Collection collection) {
        Iterator iterator = collection.iterator();
        Comparable comparable = (Comparable) iterator.next();
        while (iterator.hasNext()) {
            Comparable comparable1 = (Comparable) iterator.next();
            if (comparable1.compareTo(comparable) < 0)
                comparable = comparable1;
        }
        return comparable;
    }

    public static Object min(Collection collection, Comparator comparator) {
        Iterator iterator = collection.iterator();
        Object obj = iterator.next();
        while (iterator.hasNext()) {
            Object obj1 = iterator.next();
            if (comparator.compare(obj1, obj) < 0)
                obj = obj1;
        }
        return obj;
    }

    public static Object max(Collection collection) {
        Iterator iterator = collection.iterator();
        Comparable comparable = (Comparable) iterator.next();
        while (iterator.hasNext()) {
            Comparable comparable1 = (Comparable) iterator.next();
            if (comparable1.compareTo(comparable) > 0)
                comparable = comparable1;
        }
        return comparable;
    }

    public static Object max(Collection collection, Comparator comparator) {
        Iterator iterator = collection.iterator();
        Object obj = iterator.next();
        while (iterator.hasNext()) {
            Object obj1 = iterator.next();
            if (comparator.compare(obj1, obj) > 0)
                obj = obj1;
        }
        return obj;
    }

    public static Collection unmodifiableCollection(Collection collection) {
        return new UnmodifiableCollection(collection);
    }

    public static Set unmodifiableSet(Set set) {
        return new UnmodifiableSet(set);
    }

    public static SortedSet unmodifiableSortedSet(SortedSet sortedset) {
        return new UnmodifiableSortedSet(sortedset);
    }

    public static List unmodifiableList(List list) {
        return new UnmodifiableList(list);
    }

    public static Map unmodifiableMap(Map map) {
        return new UnmodifiableMap(map);
    }

    public static SortedMap unmodifiableSortedMap(SortedMap sortedmap) {
        return new UnmodifiableSortedMap(sortedmap);
    }

    public static Collection synchronizedCollection(Collection collection) {
        return new SynchronizedCollection(collection);
    }

    static Collection synchronizedCollection(Collection collection, Object obj) {
        return new SynchronizedCollection(collection, obj);
    }

    public static Set synchronizedSet(Set set) {
        return new SynchronizedSet(set);
    }

    static Set synchronizedSet(Set set, Object obj) {
        return new SynchronizedSet(set, obj);
    }

    public static SortedSet synchronizedSortedSet(SortedSet sortedset) {
        return new SynchronizedSortedSet(sortedset);
    }

    public static List synchronizedList(List list) {
        return new SynchronizedList(list);
    }

    static List synchronizedList(List list, Object obj) {
        return new SynchronizedList(list, obj);
    }

    public static Map synchronizedMap(Map map) {
        return new SynchronizedMap(map);
    }

    public static SortedMap synchronizedSortedMap(SortedMap sortedmap) {
        return new SynchronizedSortedMap(sortedmap);
    }

    public static Set singleton(final Object o) {
        return new AbstractSet() {

            public Iterator iterator() {
                return new Iterator() {

                    public boolean hasNext() {
                        return hasNext;
                    }

                    public Object next() {
                        if (hasNext) {
                            hasNext = false;
                            return o;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                    private boolean hasNext;


                    {
                        hasNext = true;
                    }
                };
            }

            public int size() {
                return 1;
            }

            public boolean contains(Object obj) {
                return Collections.eq(obj, o);
            }

        };
    }

    public static List nCopies(final int n, final Object o) {
        if (n < 0)
            throw new IllegalArgumentException("List length = " + n);
        else
            return new AbstractList() {

                public int size() {
                    return n;
                }

                public boolean contains(Object obj) {
                    return n != 0 && Collections.eq(obj, o);
                }

                public Object get(int i) {
                    if (i < 0 || i >= n)
                        throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + n);
                    else
                        return o;
                }

            };
    }

    public static Comparator reverseOrder() {
        return REVERSE_ORDER;
    }

    public static Enumeration enumeration(final Collection final_collection) {
        return new Enumeration() {

            public boolean hasMoreElements() {
                return i.hasNext();
            }

            public Object nextElement() {
                return i.next();
            }

            Iterator i;


            {
                i = final_collection.iterator();
            }
        };
    }

    private static boolean eq(Object obj, Object obj1) {
        if (obj == null)
            return obj1 == null;
        else
            return obj.equals(obj1);
    }

    private static Random r = new Random();
    public static final Set EMPTY_SET = new AbstractSet() {

        public Iterator iterator() {
            return new Iterator() {

                public boolean hasNext() {
                    return false;
                }

                public Object next() {
                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
        }

        public int size() {
            return 0;
        }

        public boolean contains(Object obj) {
            return false;
        }

    };
    public static final List EMPTY_LIST = new AbstractList() {

        public int size() {
            return 0;
        }

        public boolean contains(Object obj) {
            return false;
        }

        public Object get(int i) {
            throw new IndexOutOfBoundsException("Index: " + i);
        }

    };
    private static final Comparator REVERSE_ORDER = new ReverseComparator();


}
