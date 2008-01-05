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

import java.io.IOException;

public class TreeMap extends AbstractMap
        implements SortedMap {
    private class SubMap extends AbstractMap
            implements SortedMap {
        private class EntrySetView extends AbstractSet {

            public int size() {
                if (size == -1 || sizeModCount != modCount) {
                    size = 0;
                    sizeModCount = modCount;
                    for (com.sun.java.util.collections.Iterator iterator1 = iterator(); iterator1.hasNext(); iterator1.next())
                        size++;

                }
                return size;
            }

            public boolean isEmpty() {
                return !iterator().hasNext();
            }

            public boolean contains(Object obj) {
                if (!(obj instanceof Map.Entry))
                    return false;
                Map.Entry entry = (Map.Entry) obj;
                Object obj1 = entry.getKey();
                if (!inRange(obj1))
                    return false;
                TreeMap.Entry entry1 = getEntry(obj1);
                return entry1 != null && TreeMap.valEquals(entry1.getValue(), entry.getValue());
            }

            public boolean remove(Object obj) {
                if (!(obj instanceof Map.Entry))
                    return false;
                Map.Entry entry = (Map.Entry) obj;
                Object obj1 = entry.getKey();
                if (!inRange(obj1))
                    return false;
                TreeMap.Entry entry1 = getEntry(obj1);
                if (entry1 != null && TreeMap.valEquals(entry1.getValue(), entry.getValue())) {
                    deleteEntry(entry1);
                    return true;
                } else {
                    return false;
                }
            }

            public com.sun.java.util.collections.Iterator iterator() {
                return new Iterator(fromStart ? firstEntry() : getCeilEntry(fromKey), toEnd ? null : getCeilEntry(toKey));
            }

            private transient int size;
            private transient int sizeModCount;

            EntrySetView() {
                size = -1;
            }
        }


        public boolean isEmpty() {
            return entrySet.isEmpty();
        }

        public boolean containsKey(Object obj) {
            return inRange(obj) && TreeMap.this.containsKey(obj);
        }

        public Object get(Object obj) {
            if (!inRange(obj))
                return null;
            else
                return TreeMap.this.get(obj);
        }

        public Object put(Object obj, Object obj1) {
            if (!inRange(obj))
                throw new IllegalArgumentException("key out of range");
            else
                return TreeMap.this.put(obj, obj1);
        }

        public Comparator comparator() {
            return TreeMap.this.comparator;
        }

        public Object firstKey() {
            return TreeMap.key(fromStart ? firstEntry() : getCeilEntry(fromKey));
        }

        public Object lastKey() {
            return TreeMap.key(toEnd ? lastEntry() : getPrecedingEntry(toKey));
        }

        public Set entrySet() {
            return entrySet;
        }

        public SortedMap subMap(Object obj, Object obj1) {
            if (!inRange(obj))
                throw new IllegalArgumentException("fromKey out of range");
            if (!inRange2(obj1))
                throw new IllegalArgumentException("toKey out of range");
            else
                return new SubMap(obj, obj1);
        }

        public SortedMap headMap(Object obj) {
            if (!inRange2(obj))
                throw new IllegalArgumentException("toKey out of range");
            else
                return new SubMap(fromStart, fromKey, false, obj);
        }

        public SortedMap tailMap(Object obj) {
            if (!inRange(obj))
                throw new IllegalArgumentException("fromKey out of range");
            else
                return new SubMap(false, obj, toEnd, toKey);
        }

        private boolean inRange(Object obj) {
            return (fromStart || compare(obj, fromKey) >= 0) && (toEnd || compare(obj, toKey) < 0);
        }

        private boolean inRange2(Object obj) {
            return (fromStart || compare(obj, fromKey) >= 0) && (toEnd || compare(obj, toKey) <= 0);
        }

        private boolean fromStart;
        private boolean toEnd;
        private Object fromKey;
        private Object toKey;
        private transient Set entrySet;


        SubMap(Object obj, Object obj1) {
            fromStart = false;
            toEnd = false;
            entrySet = new EntrySetView();
            if (compare(obj, obj1) > 0) {
                throw new IllegalArgumentException("fromKey > toKey");
            } else {
                fromKey = obj;
                toKey = obj1;
                return;
            }
        }

        SubMap(Object obj, boolean flag) {
            fromStart = false;
            toEnd = false;
            entrySet = new EntrySetView();
            if (flag) {
                fromStart = true;
                toKey = obj;
                return;
            } else {
                toEnd = true;
                fromKey = obj;
                return;
            }
        }

        SubMap(boolean flag, Object obj, boolean flag1, Object obj1) {
            fromStart = false;
            toEnd = false;
            entrySet = new EntrySetView();
            fromStart = flag;
            fromKey = obj;
            toEnd = flag1;
            toKey = obj1;
        }
    }

    private class Iterator
            implements com.sun.java.util.collections.Iterator {

        public boolean hasNext() {
            return next != firstExcluded;
        }

        public Object next() {
            if (next == firstExcluded)
                throw new NoSuchElementException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            lastReturned = next;
            next = successor(next);
            if (type == 0)
                return lastReturned.key;
            if (type == 1)
                return lastReturned.value;
            else
                return lastReturned;
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                deleteEntry(lastReturned);
                expectedModCount++;
                lastReturned = null;
                return;
            }
        }

        private int type;
        private int expectedModCount;
        private TreeMap.Entry lastReturned;
        private TreeMap.Entry next;
        private TreeMap.Entry firstExcluded;

        Iterator(int i) {
            expectedModCount = modCount;
            type = i;
            next = firstEntry();
        }

        Iterator(TreeMap.Entry entry, TreeMap.Entry entry1) {
            expectedModCount = modCount;
            type = 2;
            next = entry;
            firstExcluded = entry1;
        }
    }

    static class Entry
            implements Map.Entry {

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object obj) {
            Object obj1 = value;
            value = obj;
            return obj1;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Map.Entry))
                return false;
            Map.Entry entry = (Map.Entry) obj;
            return TreeMap.valEquals(key, entry.getKey()) && TreeMap.valEquals(value, entry.getValue());
        }

        public int hashCode() {
            int i = key != null ? key.hashCode() : 0;
            int j = value != null ? value.hashCode() : 0;
            return i ^ j;
        }

        public String toString() {
            return key + "=" + value;
        }

        Object key;
        Object value;
        Entry left;
        Entry right;
        Entry parent;
        boolean color;

        Entry(Object obj, Object obj1, Entry entry) {
            color = true;
            key = obj;
            value = obj1;
            parent = entry;
        }
    }


    private void incrementSize() {
        modCount++;
        size++;
    }

    private void decrementSize() {
        modCount++;
        size--;
    }

    public TreeMap() {
    }

    public TreeMap(Comparator comparator1) {
        comparator = comparator1;
    }

    public TreeMap(Map map) {
        putAll(map);
    }

    public TreeMap(SortedMap sortedmap) {
        comparator = sortedmap.comparator();
        try {
            buildFromSorted(sortedmap.size(), sortedmap.entrySet().iterator(), null);
            return;
        } catch (IOException _ex) {
            return;
        }
        catch (ClassNotFoundException _ex) {
            return;
        }
    }

    public int size() {
        return size;
    }

    public boolean containsKey(Object obj) {
        return getEntry(obj) != null;
    }

    public boolean containsValue(Object obj) {
        if (obj == null)
            return valueSearchNull(root);
        else
            return valueSearchNonNull(root, obj);
    }

    private boolean valueSearchNull(Entry entry) {
        if (entry.value == null)
            return true;
        return entry.left != null && valueSearchNull(entry.left) || entry.right != null && valueSearchNull(entry.right);
    }

    private boolean valueSearchNonNull(Entry entry, Object obj) {
        if (obj.equals(entry.value))
            return true;
        return entry.left != null && valueSearchNonNull(entry.left, obj) || entry.right != null && valueSearchNonNull(entry.right, obj);
    }

    public Object get(Object obj) {
        Entry entry = getEntry(obj);
        if (entry == null)
            return null;
        else
            return entry.value;
    }

    public Comparator comparator() {
        return comparator;
    }

    public Object firstKey() {
        return key(firstEntry());
    }

    public Object lastKey() {
        return key(lastEntry());
    }

    public void putAll(Map map) {
        int i = map.size();
        if (size == 0 && i != 0 && (map instanceof SortedMap)) {
            Comparator comparator1 = ((SortedMap) map).comparator();
            if (comparator1 == comparator || comparator1 != null && comparator1.equals(comparator)) {
                modCount++;
                try {
                    buildFromSorted(i, map.entrySet().iterator(), null);
                    return;
                } catch (IOException _ex) {
                    return;
                }
                catch (ClassNotFoundException _ex) {
                    return;
                }
            }
        }
        super.putAll(map);
    }

    private Entry getEntry(Object obj) {
        for (Entry entry = root; entry != null;) {
            int i = compare(obj, entry.key);
            if (i == 0)
                return entry;
            if (i < 0)
                entry = entry.left;
            else
                entry = entry.right;
        }

        return null;
    }

    private Entry getCeilEntry(Object obj) {
        Entry entry = root;
        if (entry == null)
            return null;
        do {
            int i = compare(obj, entry.key);
            if (i == 0)
                return entry;
            if (i < 0) {
                if (entry.left != null)
                    entry = entry.left;
                else
                    return entry;
                continue;
            }
            if (entry.right == null)
                break;
            entry = entry.right;
        } while (true);
        Entry entry1 = entry.parent;
        for (Entry entry2 = entry; entry1 != null && entry2 == entry1.right; entry1 = entry1.parent)
            entry2 = entry1;

        return entry1;
    }

    private Entry getPrecedingEntry(Object obj) {
        Entry entry = root;
        if (entry == null)
            return null;
        do {
            int i = compare(obj, entry.key);
            if (i > 0) {
                if (entry.right != null)
                    entry = entry.right;
                else
                    return entry;
                continue;
            }
            if (entry.left == null)
                break;
            entry = entry.left;
        } while (true);
        Entry entry1 = entry.parent;
        for (Entry entry2 = entry; entry1 != null && entry2 == entry1.left; entry1 = entry1.parent)
            entry2 = entry1;

        return entry1;
    }

    private static Object key(Entry entry) {
        if (entry == null)
            throw new NoSuchElementException();
        else
            return entry.key;
    }

    public Object put(Object obj, Object obj1) {
        Entry entry = root;
        if (entry == null) {
            incrementSize();
            root = new Entry(obj, obj1, null);
            return null;
        }
        do {
            int i = compare(obj, entry.key);
            if (i == 0)
                return entry.setValue(obj1);
            if (i < 0) {
                if (entry.left != null) {
                    entry = entry.left;
                } else {
                    incrementSize();
                    entry.left = new Entry(obj, obj1, entry);
                    fixAfterInsertion(entry.left);
                    return null;
                }
            } else if (entry.right != null) {
                entry = entry.right;
            } else {
                incrementSize();
                entry.right = new Entry(obj, obj1, entry);
                fixAfterInsertion(entry.right);
                return null;
            }
        } while (true);
    }

    public Object remove(Object obj) {
        Entry entry = getEntry(obj);
        if (entry == null) {
            return null;
        } else {
            Object obj1 = entry.value;
            deleteEntry(entry);
            return obj1;
        }
    }

    public void clear() {
        modCount++;
        size = 0;
        root = null;
    }

    public Set keySet() {
        if (keySet == null)
            keySet = new AbstractSet() {

                public com.sun.java.util.collections.Iterator iterator() {
                    return new Iterator(0);
                }

                public int size() {
                    return TreeMap.this.size();
                }

                public boolean contains(Object obj) {
                    return containsKey(obj);
                }

                public boolean remove(Object obj) {
                    return TreeMap.this.remove(obj) != null;
                }

                public void clear() {
                    TreeMap.this.clear();
                }

            };
        return keySet;
    }

    public Collection values() {
        if (values == null)
            values = new AbstractCollection() {

                public com.sun.java.util.collections.Iterator iterator() {
                    return new Iterator(1);
                }

                public int size() {
                    return TreeMap.this.size();
                }

                public boolean contains(Object obj) {
                    for (TreeMap.Entry entry = firstEntry(); entry != null; entry = successor(entry))
                        if (TreeMap.valEquals(entry.getValue(), obj))
                            return true;

                    return false;
                }

                public boolean remove(Object obj) {
                    for (TreeMap.Entry entry = firstEntry(); entry != null; entry = successor(entry))
                        if (TreeMap.valEquals(entry.getValue(), obj)) {
                            deleteEntry(entry);
                            return true;
                        }

                    return false;
                }

                public void clear() {
                    TreeMap.this.clear();
                }

            };
        return values;
    }

    public Set entrySet() {
        if (entrySet == null)
            entrySet = new AbstractSet() {

                public com.sun.java.util.collections.Iterator iterator() {
                    return new Iterator(2);
                }

                public boolean contains(Object obj) {
                    if (!(obj instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry) obj;
                    Object obj1 = entry.getValue();
                    TreeMap.Entry entry1 = getEntry(entry.getKey());
                    return entry1 != null && TreeMap.valEquals(entry1.getValue(), obj1);
                }

                public boolean remove(Object obj) {
                    if (!(obj instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry) obj;
                    Object obj1 = entry.getValue();
                    TreeMap.Entry entry1 = getEntry(entry.getKey());
                    if (entry1 != null && TreeMap.valEquals(entry1.getValue(), obj1)) {
                        deleteEntry(entry1);
                        return true;
                    } else {
                        return false;
                    }
                }

                public int size() {
                    return TreeMap.this.size();
                }

                public void clear() {
                    TreeMap.this.clear();
                }

            };
        return entrySet;
    }

    public SortedMap subMap(Object obj, Object obj1) {
        return new SubMap(obj, obj1);
    }

    public SortedMap headMap(Object obj) {
        return new SubMap(obj, true);
    }

    public SortedMap tailMap(Object obj) {
        return new SubMap(obj, false);
    }

    private int compare(Object obj, Object obj1) {
        if (comparator == null)
            return ((Comparable) obj).compareTo(obj1);
        else
            return comparator.compare(obj, obj1);
    }

    private static boolean valEquals(Object obj, Object obj1) {
        if (obj == null)
            return obj1 == null;
        else
            return obj.equals(obj1);
    }

    private Entry firstEntry() {
        Entry entry = root;
        if (entry != null)
            for (; entry.left != null; entry = entry.left)
                ;
        return entry;
    }

    private Entry lastEntry() {
        Entry entry = root;
        if (entry != null)
            for (; entry.right != null; entry = entry.right)
                ;
        return entry;
    }

    private Entry successor(Entry entry) {
        if (entry == null)
            return null;
        if (entry.right != null) {
            Entry entry1;
            for (entry1 = entry.right; entry1.left != null; entry1 = entry1.left)
                ;
            return entry1;
        }
        Entry entry2 = entry.parent;
        for (Entry entry3 = entry; entry2 != null && entry3 == entry2.right; entry2 = entry2.parent)
            entry3 = entry2;

        return entry2;
    }

    private static boolean colorOf(Entry entry) {
        if (entry == null)
            return true;
        else
            return entry.color;
    }

    private static Entry parentOf(Entry entry) {
        if (entry == null)
            return null;
        else
            return entry.parent;
    }

    private static void setColor(Entry entry, boolean flag) {
        if (entry != null)
            entry.color = flag;
    }

    private static Entry leftOf(Entry entry) {
        if (entry == null)
            return null;
        else
            return entry.left;
    }

    private static Entry rightOf(Entry entry) {
        if (entry == null)
            return null;
        else
            return entry.right;
    }

    private void rotateLeft(Entry entry) {
        Entry entry1 = entry.right;
        entry.right = entry1.left;
        if (entry1.left != null)
            entry1.left.parent = entry;
        entry1.parent = entry.parent;
        if (entry.parent == null)
            root = entry1;
        else if (entry.parent.left == entry)
            entry.parent.left = entry1;
        else
            entry.parent.right = entry1;
        entry1.left = entry;
        entry.parent = entry1;
    }

    private void rotateRight(Entry entry) {
        Entry entry1 = entry.left;
        entry.left = entry1.right;
        if (entry1.right != null)
            entry1.right.parent = entry;
        entry1.parent = entry.parent;
        if (entry.parent == null)
            root = entry1;
        else if (entry.parent.right == entry)
            entry.parent.right = entry1;
        else
            entry.parent.left = entry1;
        entry1.right = entry;
        entry.parent = entry1;
    }

    private void fixAfterInsertion(Entry entry) {
        for (entry.color = false; entry != null && entry != root && !entry.parent.color;)
            if (parentOf(entry) == leftOf(parentOf(parentOf(entry)))) {
                Entry entry1 = rightOf(parentOf(parentOf(entry)));
                if (!colorOf(entry1)) {
                    setColor(parentOf(entry), true);
                    setColor(entry1, true);
                    setColor(parentOf(parentOf(entry)), false);
                    entry = parentOf(parentOf(entry));
                } else {
                    if (entry == rightOf(parentOf(entry))) {
                        entry = parentOf(entry);
                        rotateLeft(entry);
                    }
                    setColor(parentOf(entry), true);
                    setColor(parentOf(parentOf(entry)), false);
                    if (parentOf(parentOf(entry)) != null)
                        rotateRight(parentOf(parentOf(entry)));
                }
            } else {
                Entry entry2 = leftOf(parentOf(parentOf(entry)));
                if (!colorOf(entry2)) {
                    setColor(parentOf(entry), true);
                    setColor(entry2, true);
                    setColor(parentOf(parentOf(entry)), false);
                    entry = parentOf(parentOf(entry));
                } else {
                    if (entry == leftOf(parentOf(entry))) {
                        entry = parentOf(entry);
                        rotateRight(entry);
                    }
                    setColor(parentOf(entry), true);
                    setColor(parentOf(parentOf(entry)), false);
                    if (parentOf(parentOf(entry)) != null)
                        rotateLeft(parentOf(parentOf(entry)));
                }
            }

        root.color = true;
    }

    private void deleteEntry(Entry entry) {
        decrementSize();
        if (entry.left != null && entry.right != null) {
            Entry entry1 = successor(entry);
            swapPosition(entry1, entry);
        }
        Entry entry2 = entry.left == null ? entry.right : entry.left;
        if (entry2 != null) {
            entry2.parent = entry.parent;
            if (entry.parent == null)
                root = entry2;
            else if (entry == entry.parent.left)
                entry.parent.left = entry2;
            else
                entry.parent.right = entry2;
            entry.left = entry.right = entry.parent = null;
            if (entry.color) {
                fixAfterDeletion(entry2);
                return;
            }
        } else {
            if (entry.parent == null) {
                root = null;
                return;
            }
            if (entry.color)
                fixAfterDeletion(entry);
            if (entry.parent != null) {
                if (entry == entry.parent.left)
                    entry.parent.left = null;
                else if (entry == entry.parent.right)
                    entry.parent.right = null;
                entry.parent = null;
            }
        }
    }

    private void fixAfterDeletion(Entry entry) {
        while (entry != root && colorOf(entry))
            if (entry == leftOf(parentOf(entry))) {
                Entry entry1 = rightOf(parentOf(entry));
                if (!colorOf(entry1)) {
                    setColor(entry1, true);
                    setColor(parentOf(entry), false);
                    rotateLeft(parentOf(entry));
                    entry1 = rightOf(parentOf(entry));
                }
                if (colorOf(leftOf(entry1)) && colorOf(rightOf(entry1))) {
                    setColor(entry1, false);
                    entry = parentOf(entry);
                } else {
                    if (colorOf(rightOf(entry1))) {
                        setColor(leftOf(entry1), true);
                        setColor(entry1, false);
                        rotateRight(entry1);
                        entry1 = rightOf(parentOf(entry));
                    }
                    setColor(entry1, colorOf(parentOf(entry)));
                    setColor(parentOf(entry), true);
                    setColor(rightOf(entry1), true);
                    rotateLeft(parentOf(entry));
                    entry = root;
                }
            } else {
                Entry entry2 = leftOf(parentOf(entry));
                if (!colorOf(entry2)) {
                    setColor(entry2, true);
                    setColor(parentOf(entry), false);
                    rotateRight(parentOf(entry));
                    entry2 = leftOf(parentOf(entry));
                }
                if (colorOf(rightOf(entry2)) && colorOf(leftOf(entry2))) {
                    setColor(entry2, false);
                    entry = parentOf(entry);
                } else {
                    if (colorOf(leftOf(entry2))) {
                        setColor(rightOf(entry2), true);
                        setColor(entry2, false);
                        rotateLeft(entry2);
                        entry2 = leftOf(parentOf(entry));
                    }
                    setColor(entry2, colorOf(parentOf(entry)));
                    setColor(parentOf(entry), true);
                    setColor(leftOf(entry2), true);
                    rotateRight(parentOf(entry));
                    entry = root;
                }
            }
        setColor(entry, true);
    }

    private void swapPosition(Entry entry, Entry entry1) {
        Entry entry2 = entry.parent;
        Entry entry3 = entry.left;
        Entry entry4 = entry.right;
        Entry entry5 = entry1.parent;
        Entry entry6 = entry1.left;
        Entry entry7 = entry1.right;
        boolean flag = entry2 != null && entry == entry2.left;
        boolean flag1 = entry5 != null && entry1 == entry5.left;
        if (entry == entry5) {
            entry.parent = entry1;
            if (flag1) {
                entry1.left = entry;
                entry1.right = entry4;
            } else {
                entry1.right = entry;
                entry1.left = entry3;
            }
        } else {
            entry.parent = entry5;
            if (entry5 != null)
                if (flag1)
                    entry5.left = entry;
                else
                    entry5.right = entry;
            entry1.left = entry3;
            entry1.right = entry4;
        }
        if (entry1 == entry2) {
            entry1.parent = entry;
            if (flag) {
                entry.left = entry1;
                entry.right = entry7;
            } else {
                entry.right = entry1;
                entry.left = entry6;
            }
        } else {
            entry1.parent = entry2;
            if (entry2 != null)
                if (flag)
                    entry2.left = entry1;
                else
                    entry2.right = entry1;
            entry.left = entry6;
            entry.right = entry7;
        }
        if (entry.left != null)
            entry.left.parent = entry;
        if (entry.right != null)
            entry.right.parent = entry;
        if (entry1.left != null)
            entry1.left.parent = entry1;
        if (entry1.right != null)
            entry1.right.parent = entry1;
        boolean flag2 = entry.color;
        entry.color = entry1.color;
        entry1.color = flag2;
        if (root == entry) {
            root = entry1;
            return;
        }
        if (root == entry1)
            root = entry;
    }

    void addAllForTreeSet(SortedSet sortedset, Object obj) {
        try {
            buildFromSorted(sortedset.size(), sortedset.iterator(), obj);
            return;
        } catch (IOException _ex) {
            return;
        }
        catch (ClassNotFoundException _ex) {
            return;
        }
    }

    private void buildFromSorted(int i, com.sun.java.util.collections.Iterator iterator, Object obj)
            throws IOException, ClassNotFoundException {
        size = i;
        root = buildFromSorted(0, 0, i - 1, computeRedLevel(i), iterator, obj);
    }

    private static Entry buildFromSorted(int i, int j, int k, int l, com.sun.java.util.collections.Iterator iterator, Object obj)
            throws IOException, ClassNotFoundException {
        if (k < j)
            return null;
        int i1 = (j + k) / 2;
        Entry entry = null;
        if (j < i1)
            entry = buildFromSorted(i + 1, j, i1 - 1, l, iterator, obj);
        Object obj1 = null;
        Object obj2 = null;
        if (iterator != null) {
            if (obj == null) {
                Map.Entry entry1 = (Map.Entry) iterator.next();
                obj1 = entry1.getKey();
                obj2 = entry1.getValue();
            } else {
                obj1 = iterator.next();
                obj2 = obj;
            }
        }
        Entry entry2 = new Entry(obj1, obj2, null);
        if (i == l)
            entry2.color = false;
        if (entry != null) {
            entry2.left = entry;
            entry.parent = entry2;
        }
        if (i1 < k) {
            Entry entry3 = buildFromSorted(i + 1, i1 + 1, k, l, iterator, obj);
            entry2.right = entry3;
            entry3.parent = entry2;
        }
        return entry2;
    }

    private static int computeRedLevel(int i) {
        int j = 0;
        for (int k = i - 1; k >= 0; k = k / 2 - 1)
            j++;

        return j;
    }

    private Comparator comparator;
    private transient Entry root;
    private transient int size;
    private transient int modCount;
    private transient Set keySet;
    private transient Set entrySet;
    private transient Collection values;
    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;
    private static final boolean RED = false;
    private static final boolean BLACK = true;


}
