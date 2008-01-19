
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

import java.util.Enumeration;

public class Hashtable implements Map {
    private class KeySet extends AbstractSet {

        public Iterator iterator() {
            return new Enumerator(0, true);
        }

        public int size() {
            return count;
        }

        public boolean contains(Object obj) {
            return containsKey(obj);
        }

        public boolean remove(Object obj) {
            return Hashtable.this.remove(obj) != null;
        }

        public void clear() {
            Hashtable.this.clear();
        }

        KeySet() {
        }
    }

    private class EntrySet extends AbstractSet {

        public Iterator iterator() {
            return new Enumerator(2, true);
        }

        public boolean contains(Object obj) {
            if (!(obj instanceof Map.Entry))
                return false;
            Map.Entry entry = (Map.Entry) obj;
            Object obj1 = entry.getKey();
            Entry aentry[] = table;
            int i = obj1.hashCode();
            int j = (i & 0x7fffffff) % aentry.length;
            for (Entry entry1 = aentry[j]; entry1 != null; entry1 = entry1.next)
                if (entry1.hash == i && entry1.equals(entry))
                    return true;

            return false;
        }

        public boolean remove(Object obj) {
            if (!(obj instanceof Map.Entry))
                return false;
            Map.Entry entry = (Map.Entry) obj;
            Object obj1 = entry.getKey();
            Entry aentry[] = table;
            int i = obj1.hashCode();
            int j = (i & 0x7fffffff) % aentry.length;
            Entry entry1 = aentry[j];
            Entry entry2 = null;
            for (; entry1 != null; entry1 = entry1.next) {
                if (entry1.hash == i && entry1.equals(entry)) {
                    modCount++;
                    if (entry2 != null)
                        entry2.next = entry1.next;
                    else
                        aentry[j] = entry1.next;
                    count--;
                    entry1.value = null;
                    return true;
                }
                entry2 = entry1;
            }

            return false;
        }

        public int size() {
            return count;
        }

        public void clear() {
            Hashtable.this.clear();
        }

        EntrySet() {
        }
    }

    private class ValueCollection extends AbstractCollection {

        public Iterator iterator() {
            return new Enumerator(1, true);
        }

        public int size() {
            return count;
        }

        public boolean contains(Object obj) {
            return containsValue(obj);
        }

        public void clear() {
            Hashtable.this.clear();
        }

        ValueCollection() {
        }
    }

    private static class Entry
            implements Map.Entry {

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object obj) {
            if (obj == null) {
                throw new NullPointerException();
            } else {
                Object obj1 = value;
                value = obj;
                return obj1;
            }
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Map.Entry))
                return false;
            Map.Entry entry = (Map.Entry) obj;
            return (key != null ? key.equals(entry.getKey()) : entry.getKey() == null || false) && (value != null ? value.equals(entry.getValue()) : entry.getValue() == null || false);
        }

        public int hashCode() {
            return hash ^ (value != null ? value.hashCode() : 0);
        }

        public String toString() {
            return key.toString() + "=" + value.toString();
        }

        int hash;
        Object key;
        Object value;
        Entry next;

        protected Entry(int i, Object obj, Object obj1, Entry entry) {
            hash = i;
            key = obj;
            value = obj1;
            next = entry;
        }
    }

    private class Enumerator
            implements Enumeration, Iterator {

        public boolean hasMoreElements() {
            for (; entry == null && index > 0; entry = table[--index]) ;
            return entry != null;
        }

        public Object nextElement() {
            for (; entry == null && index > 0; entry = table[--index]) ;
            if (entry != null) {
                Entry entry1 = lastReturned = entry;
                entry = entry1.next;
                if (type == 0)
                    return entry1.key;
                if (type == 1)
                    return entry1.value;
                else
                    return entry1;
            } else {
                throw new NoSuchElementException("Hashtable Enumerator");
            }
        }

        public boolean hasNext() {
            return hasMoreElements();
        }

        public Object next() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            else
                return nextElement();
        }

        public void remove() {
            if (!iterator)
                throw new UnsupportedOperationException();
            if (lastReturned == null)
                throw new IllegalStateException("Hashtable Enumerator");
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            synchronized (Hashtable.this) {
                Entry aentry[] = Hashtable.this.table;
                int i = (lastReturned.hash & 0x7fffffff) % aentry.length;
                Entry entry1 = aentry[i];
                Entry entry2 = null;
                for (; entry1 != null; entry1 = entry1.next) {
                    if (entry1 == lastReturned) {
                        modCount++;
                        expectedModCount++;
                        if (entry2 == null)
                            aentry[i] = entry1.next;
                        else
                            entry2.next = entry1.next;
                        count--;
                        lastReturned = null;
                        return;
                    }
                    entry2 = entry1;
                }

                throw new ConcurrentModificationException();
            }
        }

        Entry table[];
        int index;
        Entry entry;
        Entry lastReturned;
        int type;
        boolean iterator;
        private int expectedModCount;

        Enumerator(int i, boolean flag) {
            table = Hashtable.this.table;
            index = table.length;
            expectedModCount = modCount;
            type = i;
            iterator = flag;
        }
    }


    public Hashtable(int i, float f) {
        if (i < 0)
            throw new IllegalArgumentException("Illegal Capacity: " + i);
        if (f <= 0.0F)
            throw new IllegalArgumentException("Illegal Load: " + f);
        if (i == 0)
            i = 1;
        loadFactor = f;
        table = new Entry[i];
        threshold = (int) ((float) i * f);
    }

    public Hashtable(int i) {
        this(i, 0.75F);
    }

    public Hashtable() {
        this(101, 0.75F);
    }

    public Hashtable(Map map) {
        this(Math.max(2 * map.size(), 11), 0.75F);
        putAll(map);
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public synchronized Enumeration keys() {
        return new Enumerator(0, false);
    }

    public synchronized Enumeration elements() {
        return new Enumerator(1, false);
    }

    public synchronized boolean contains(Object obj) {
        if (obj == null)
            throw new NullPointerException();
        Entry aentry[] = table;
        for (int i = aentry.length; i-- > 0;) {
            for (Entry entry = aentry[i]; entry != null; entry = entry.next)
                if (entry.value.equals(obj))
                    return true;

        }

        return false;
    }

    public boolean containsValue(Object obj) {
        return contains(obj);
    }

    public synchronized boolean containsKey(Object obj) {
        Entry aentry[] = table;
        int i = obj.hashCode();
        int j = (i & 0x7fffffff) % aentry.length;
        for (Entry entry = aentry[j]; entry != null; entry = entry.next)
            if (entry.hash == i && entry.key.equals(obj))
                return true;

        return false;
    }

    public synchronized Object get(Object obj) {
        Entry aentry[] = table;
        int i = obj.hashCode();
        int j = (i & 0x7fffffff) % aentry.length;
        for (Entry entry = aentry[j]; entry != null; entry = entry.next)
            if (entry.hash == i && entry.key.equals(obj))
                return entry.value;

        return null;
    }

    protected void rehash() {
        int i = table.length;
        Entry aentry[] = table;
        int j = i * 2 + 1;
        Entry aentry1[] = new Entry[j];
        modCount++;
        threshold = (int) ((float) j * loadFactor);
        table = aentry1;
        for (int k = i; k-- > 0;) {
            for (Entry entry = aentry[k]; entry != null;) {
                Entry entry1 = entry;
                entry = entry.next;
                int l = (entry1.hash & 0x7fffffff) % j;
                entry1.next = aentry1[l];
                aentry1[l] = entry1;
            }

        }

    }

    public synchronized Object put(Object obj, Object obj1) {
        if (obj1 == null)
            throw new NullPointerException();
        Entry aentry[] = table;
        int i = obj.hashCode();
        int j = (i & 0x7fffffff) % aentry.length;
        for (Entry entry = aentry[j]; entry != null; entry = entry.next)
            if (entry.hash == i && entry.key.equals(obj)) {
                Object obj2 = entry.value;
                entry.value = obj1;
                return obj2;
            }

        modCount++;
        if (count >= threshold) {
            rehash();
            aentry = table;
            j = (i & 0x7fffffff) % aentry.length;
        }
        Entry entry1 = new Entry(i, obj, obj1, aentry[j]);
        aentry[j] = entry1;
        count++;
        return null;
    }

    public synchronized Object remove(Object obj) {
        Entry aentry[] = table;
        int i = obj.hashCode();
        int j = (i & 0x7fffffff) % aentry.length;
        Entry entry = aentry[j];
        Entry entry1 = null;
        for (; entry != null; entry = entry.next) {
            if (entry.hash == i && entry.key.equals(obj)) {
                modCount++;
                if (entry1 != null)
                    entry1.next = entry.next;
                else
                    aentry[j] = entry.next;
                count--;
                Object obj1 = entry.value;
                entry.value = null;
                return obj1;
            }
            entry1 = entry;
        }

        return null;
    }

    public synchronized void putAll(Map map) {
        Map.Entry entry;
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); put(entry.getKey(), entry.getValue()))
            entry = (Map.Entry) iterator.next();

    }

    public synchronized void clear() {
        Entry aentry[] = table;
        modCount++;
        for (int i = aentry.length; --i >= 0;)
            aentry[i] = null;

        count = 0;
    }

    public synchronized String toString() {
        int i = size() - 1;
        StringBuffer stringbuffer = new StringBuffer();
        Iterator iterator = entrySet().iterator();
        stringbuffer.append("{");
        for (int j = 0; j <= i; j++) {
            Entry entry = (Entry) iterator.next();
            stringbuffer.append(entry.key + "=" + entry.value);
            if (j < i)
                stringbuffer.append(", ");
        }

        stringbuffer.append("}");
        return stringbuffer.toString();
    }

    public Set keySet() {
        if (keySet == null)
            keySet = Collections.synchronizedSet(new KeySet(), this);
        return keySet;
    }

    public Set entrySet() {
        if (entrySet == null)
            entrySet = Collections.synchronizedSet(new EntrySet(), this);
        return entrySet;
    }

    public Collection values() {
        if (values == null)
            values = Collections.synchronizedCollection(new ValueCollection(), this);
        return values;
    }

    public synchronized boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Map))
            return false;
        Map map = (Map) obj;
        if (map.size() != size())
            return false;
        for (Iterator iterator = entrySet().iterator(); iterator.hasNext();) {
            Entry entry = (Entry) iterator.next();
            Object obj1 = entry.getKey();
            Object obj2 = entry.getValue();
            if (obj2 == null) {
                if (map.get(obj1) != null || !map.containsKey(obj1))
                    return false;
            } else if (!obj2.equals(map.get(obj1)))
                return false;
        }

        return true;
    }

    public synchronized int hashCode() {
        int i = 0;
        for (Iterator iterator = entrySet().iterator(); iterator.hasNext();)
            i += iterator.next().hashCode();

        return i;
    }

    private transient Entry table[];
    private transient int count;
    private int threshold;
    private float loadFactor;
    private transient int modCount;
    private static final long serialVersionUID = 0x13bb0f25214ae4b8L;
    private transient Set keySet;
    private transient Set entrySet;
    private transient Collection values;
    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;
}
