
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

public class HashMap extends AbstractMap implements Map {
    private static class Entry implements Map.Entry {

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
            return (key != null ? key.equals(entry.getKey()) : entry.getKey() == null) && (value != null ? value.equals(entry.getValue()) : entry.getValue() == null || false);
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

        Entry(int i, Object obj, Object obj1, Entry entry) {
            hash = i;
            key = obj;
            value = obj1;
            next = entry;
        }
    }

    private class HashIterator
            implements Iterator {

        public boolean hasNext() {
            for (; entry == null && index > 0; entry = table[--index])
                ;
            return entry != null;
        }

        public Object next() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            for (; entry == null && index > 0; entry = table[--index])
                ;
            if (entry != null) {
                HashMap.Entry entry1 = lastReturned = entry;
                entry = entry1.next;
                if (type == 0)
                    return entry1.key;
                if (type == 1)
                    return entry1.value;
                else
                    return entry1;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            HashMap.Entry aentry[] = HashMap.this.table;
            int i = (lastReturned.hash & 0x7fffffff) % aentry.length;
            HashMap.Entry entry1 = aentry[i];
            HashMap.Entry entry2 = null;
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

        HashMap.Entry table[];
        int index;
        HashMap.Entry entry;
        HashMap.Entry lastReturned;
        int type;
        private int expectedModCount;

        HashIterator(int i) {
            table = HashMap.this.table;
            index = table.length;
            expectedModCount = modCount;
            type = i;
        }
    }


    public HashMap(int i, float f) {
        if (i < 0)
            throw new IllegalArgumentException("Illegal Initial Capacity: " + i);
        if (f <= 0.0F)
            throw new IllegalArgumentException("Illegal Load factor: " + f);
        if (i == 0)
            i = 1;
        loadFactor = f;
        table = new Entry[i];
        threshold = (int) ((float) i * f);
    }

    public HashMap(int i) {
        this(i, 0.75F);
    }

    public HashMap() {
        this(101, 0.75F);
    }

    public HashMap(Map map) {
        this(Math.max(2 * map.size(), 11), 0.75F);
        putAll(map);
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean containsValue(Object obj) {
        Entry aentry[] = table;
        if (obj == null) {
            for (int i = aentry.length; i-- > 0;) {
                for (Entry entry = aentry[i]; entry != null; entry = entry.next)
                    if (entry.value == null)
                        return true;

            }

        } else {
            for (int j = aentry.length; j-- > 0;) {
                for (Entry entry1 = aentry[j]; entry1 != null; entry1 = entry1.next)
                    if (obj.equals(entry1.value))
                        return true;

            }

        }
        return false;
    }

    public boolean containsKey(Object obj) {
        Entry aentry[] = table;
        if (obj != null) {
            int i = obj.hashCode();
            int j = (i & 0x7fffffff) % aentry.length;
            for (Entry entry1 = aentry[j]; entry1 != null; entry1 = entry1.next)
                if (entry1.hash == i && obj.equals(entry1.key))
                    return true;

        } else {
            for (Entry entry = aentry[0]; entry != null; entry = entry.next)
                if (entry.key == null)
                    return true;

        }
        return false;
    }

    public Object get(Object obj) {
        Entry aentry[] = table;
        if (obj != null) {
            int i = obj.hashCode();
            int j = (i & 0x7fffffff) % aentry.length;
            for (Entry entry1 = aentry[j]; entry1 != null; entry1 = entry1.next)
                if (entry1.hash == i && obj.equals(entry1.key))
                    return entry1.value;

        } else {
            for (Entry entry = aentry[0]; entry != null; entry = entry.next)
                if (entry.key == null)
                    return entry.value;

        }
        return null;
    }

    private void rehash() {
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

    public Object put(Object obj, Object obj1) {
        Entry aentry[] = table;
        int i = 0;
        int j = 0;
        if (obj != null) {
            i = obj.hashCode();
            j = (i & 0x7fffffff) % aentry.length;
            for (Entry entry = aentry[j]; entry != null; entry = entry.next)
                if (entry.hash == i && obj.equals(entry.key)) {
                    Object obj2 = entry.value;
                    entry.value = obj1;
                    return obj2;
                }

        } else {
            for (Entry entry1 = aentry[0]; entry1 != null; entry1 = entry1.next)
                if (entry1.key == null) {
                    Object obj3 = entry1.value;
                    entry1.value = obj1;
                    return obj3;
                }

        }
        modCount++;
        if (count >= threshold) {
            rehash();
            aentry = table;
            j = (i & 0x7fffffff) % aentry.length;
        }
        Entry entry2 = new Entry(i, obj, obj1, aentry[j]);
        aentry[j] = entry2;
        count++;
        return null;
    }

    public Object remove(Object obj) {
        Entry aentry[] = table;
        if (obj != null) {
            int i = obj.hashCode();
            int j = (i & 0x7fffffff) % aentry.length;
            Entry entry2 = aentry[j];
            Entry entry3 = null;
            for (; entry2 != null; entry2 = entry2.next) {
                if (entry2.hash == i && obj.equals(entry2.key)) {
                    modCount++;
                    if (entry3 != null)
                        entry3.next = entry2.next;
                    else
                        aentry[j] = entry2.next;
                    count--;
                    Object obj2 = entry2.value;
                    entry2.value = null;
                    return obj2;
                }
                entry3 = entry2;
            }

        } else {
            Entry entry = aentry[0];
            Entry entry1 = null;
            for (; entry != null; entry = entry.next) {
                if (entry.key == null) {
                    modCount++;
                    if (entry1 != null)
                        entry1.next = entry.next;
                    else
                        aentry[0] = entry.next;
                    count--;
                    Object obj1 = entry.value;
                    entry.value = null;
                    return obj1;
                }
                entry1 = entry;
            }

        }
        return null;
    }

    public void putAll(Map map) {
        Map.Entry entry;
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); put(entry.getKey(), entry.getValue()))
            entry = (Map.Entry) iterator.next();

    }

    public void clear() {
        Entry aentry[] = table;
        modCount++;
        for (int i = aentry.length; --i >= 0;)
            aentry[i] = null;

        count = 0;
    }

    public Set keySet() {
        if (keySet == null)
            keySet = new AbstractSet() {

                public Iterator iterator() {
                    return new HashIterator(0);
                }

                public int size() {
                    return count;
                }

                public boolean contains(Object obj) {
                    return containsKey(obj);
                }

                public boolean remove(Object obj) {
                    return HashMap.this.remove(obj) != null;
                }

                public void clear() {
                    HashMap.this.clear();
                }

            };
        return keySet;
    }

    public Collection values() {
        if (values == null)
            values = new AbstractCollection() {

                public Iterator iterator() {
                    return new HashIterator(1);
                }

                public int size() {
                    return count;
                }

                public boolean contains(Object obj) {
                    return containsValue(obj);
                }

                public void clear() {
                    HashMap.this.clear();
                }

            };
        return values;
    }

    public Set entrySet() {
        if (entrySet == null)
            entrySet = new AbstractSet() {

                public Iterator iterator() {
                    return new HashIterator(2);
                }

                public boolean contains(Object obj) {
                    if (!(obj instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry) obj;
                    Object obj1 = entry.getKey();
                    HashMap.Entry aentry[] = table;
                    int i = obj1 != null ? obj1.hashCode() : 0;
                    int j = (i & 0x7fffffff) % aentry.length;
                    for (HashMap.Entry entry1 = aentry[j]; entry1 != null; entry1 = entry1.next)
                        if (entry1.hash == i && entry1.equals(entry))
                            return true;

                    return false;
                }

                public boolean remove(Object obj) {
                    if (!(obj instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry) obj;
                    Object obj1 = entry.getKey();
                    HashMap.Entry aentry[] = table;
                    int i = obj1 != null ? obj1.hashCode() : 0;
                    int j = (i & 0x7fffffff) % aentry.length;
                    HashMap.Entry entry1 = aentry[j];
                    HashMap.Entry entry2 = null;
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
                    HashMap.this.clear();
                }

            };
        return entrySet;
    }

    int capacity() {
        return table.length;
    }

    float loadFactor() {
        return loadFactor;
    }

    private transient Entry table[];
    private transient int count;
    private int threshold;
    private float loadFactor;
    private transient int modCount;
    private transient Set keySet;
    private transient Set entrySet;
    private transient Collection values;
    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;
}
