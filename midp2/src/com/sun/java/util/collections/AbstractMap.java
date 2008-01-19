
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

public abstract class AbstractMap implements Map {

    protected AbstractMap() {
    }

    public int size() {
        return entrySet().size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean containsValue(Object obj) {
        Iterator iterator = entrySet().iterator();
        if (obj == null)
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (entry.getValue() == null)
                    return true;
            }
        else
            while (iterator.hasNext()) {
                Map.Entry entry1 = (Map.Entry) iterator.next();
                if (obj.equals(entry1.getValue()))
                    return true;
            }
        return false;
    }

    public boolean containsKey(Object obj) {
        Iterator iterator = entrySet().iterator();
        if (obj == null)
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (entry.getKey() == null)
                    return true;
            }
        else
            while (iterator.hasNext()) {
                Map.Entry entry1 = (Map.Entry) iterator.next();
                if (obj.equals(entry1.getKey()))
                    return true;
            }
        return false;
    }

    public Object get(Object obj) {
        Iterator iterator = entrySet().iterator();
        if (obj == null)
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                if (entry.getKey() == null)
                    return entry.getValue();
            }
        else
            while (iterator.hasNext()) {
                Map.Entry entry1 = (Map.Entry) iterator.next();
                if (obj.equals(entry1.getKey()))
                    return entry1.getValue();
            }
        return null;
    }

    public Object put(Object obj, Object obj1) {
        throw new UnsupportedOperationException();
    }

    public Object remove(Object obj) {
        Iterator iterator = entrySet().iterator();
        Map.Entry entry = null;
        if (obj == null)
            while (entry == null && iterator.hasNext()) {
                Map.Entry entry1 = (Map.Entry) iterator.next();
                if (entry1.getKey() == null)
                    entry = entry1;
            }
        else
            while (entry == null && iterator.hasNext()) {
                Map.Entry entry2 = (Map.Entry) iterator.next();
                if (obj.equals(entry2.getKey()))
                    entry = entry2;
            }
        Object obj1 = null;
        if (entry != null) {
            obj1 = entry.getValue();
            iterator.remove();
        }
        return obj1;
    }

    public void putAll(Map map) {
        Map.Entry entry;
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); put(entry.getKey(), entry.getValue()))
            entry = (Map.Entry) iterator.next();

    }

    public void clear() {
        entrySet().clear();
    }

    public Set keySet() {
        if (keySet == null)
            keySet = new AbstractSet() {

                public Iterator iterator() {
                    return new Iterator() {

                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        public Object next() {
                            return ((Map.Entry) i.next()).getKey();
                        }

                        public void remove() {
                            i.remove();
                        }

                        private Iterator i;


                        {
                            i = entrySet().iterator();
                        }
                    };
                }

                public int size() {
                    return AbstractMap.this.size();
                }

                public boolean contains(Object obj) {
                    return containsKey(obj);
                }

            };
        return keySet;
    }

    public Collection values() {
        if (values == null)
            values = new AbstractCollection() {

                public Iterator iterator() {
                    return new Iterator() {

                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        public Object next() {
                            return ((Map.Entry) i.next()).getValue();
                        }

                        public void remove() {
                            i.remove();
                        }

                        private Iterator i;


                        {
                            i = entrySet().iterator();
                        }
                    };
                }

                public int size() {
                    return AbstractMap.this.size();
                }

                public boolean contains(Object obj) {
                    return containsValue(obj);
                }

            };
        return values;
    }

    public abstract Set entrySet();

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Map))
            return false;
        Map map = (Map) obj;
        if (map.size() != size())
            return false;
        for (Iterator iterator = entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
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

    public int hashCode() {
        int i = 0;
        for (Iterator iterator = entrySet().iterator(); iterator.hasNext();)
            i += iterator.next().hashCode();

        return i;
    }

    public String toString() {
        int i = size() - 1;
        StringBuffer stringbuffer = new StringBuffer();
        Iterator iterator = entrySet().iterator();
        stringbuffer.append("{");
        for (int j = 0; j <= i; j++) {
            Map.Entry entry = (Map.Entry) iterator.next();
            stringbuffer.append(entry.getKey() + "=" + entry.getValue());
            if (j < i)
                stringbuffer.append(", ");
        }

        stringbuffer.append("}");
        return stringbuffer.toString();
    }

    private transient Set keySet;
    private transient Collection values;
}
