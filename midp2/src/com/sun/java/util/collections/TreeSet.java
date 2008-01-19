
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

public class TreeSet extends AbstractSet implements SortedSet {

    private TreeSet(SortedMap sortedmap) {
        m = sortedmap;
        keySet = sortedmap.keySet();
    }

    public TreeSet() {
        this(((SortedMap) (new TreeMap())));
    }

    public TreeSet(Comparator comparator1) {
        this(((SortedMap) (new TreeMap(comparator1))));
    }

    public TreeSet(Collection collection) {
        this();
        addAll(collection);
    }

    public TreeSet(SortedSet sortedset) {
        this(sortedset.comparator());
        addAll(sortedset);
    }

    public Iterator iterator() {
        return keySet.iterator();
    }

    public int size() {
        return m.size();
    }

    public boolean isEmpty() {
        return m.isEmpty();
    }

    public boolean contains(Object obj) {
        return m.containsKey(obj);
    }

    public boolean add(Object obj) {
        return m.put(obj, PRESENT) == null;
    }

    public boolean remove(Object obj) {
        return m.remove(obj) == PRESENT;
    }

    public void clear() {
        m.clear();
    }

    public boolean addAll(Collection collection) {
        if (m.size() == 0 && collection.size() > 0 && (collection instanceof SortedSet) && (m instanceof TreeMap)) {
            SortedSet sortedset = (SortedSet) collection;
            TreeMap treemap = (TreeMap) m;
            Comparator comparator1 = sortedset.comparator();
            Comparator comparator2 = treemap.comparator();
            if (comparator1 == comparator2 || comparator1 != null && comparator1.equals(comparator2)) {
                treemap.addAllForTreeSet(sortedset, PRESENT);
                return true;
            }
        }
        return super.addAll(collection);
    }

    public SortedSet subSet(Object obj, Object obj1) {
        return new TreeSet(m.subMap(obj, obj1));
    }

    public SortedSet headSet(Object obj) {
        return new TreeSet(m.headMap(obj));
    }

    public SortedSet tailSet(Object obj) {
        return new TreeSet(m.tailMap(obj));
    }

    public Comparator comparator() {
        return m.comparator();
    }

    public Object first() {
        return m.firstKey();
    }

    public Object last() {
        return m.lastKey();
    }

    private transient SortedMap m;
    private transient Set keySet;
    private static final Object PRESENT = new Object();

}
