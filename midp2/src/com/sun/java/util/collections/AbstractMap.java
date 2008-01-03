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
