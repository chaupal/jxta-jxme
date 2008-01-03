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

// FDM
//    public Object clone() {
//        return new TreeSet(this);
//    }

    private transient SortedMap m;
    private transient Set keySet;
    private static final Object PRESENT = new Object();

}
