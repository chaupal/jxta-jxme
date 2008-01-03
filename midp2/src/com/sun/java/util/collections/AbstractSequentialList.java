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

public abstract class AbstractSequentialList extends AbstractList {

    protected AbstractSequentialList() {
    }

    public Object get(int i) {
        ListIterator listiterator = listIterator(i);
        try {
            return listiterator.next();
        }
        catch (NoSuchElementException _ex) {
            throw new IndexOutOfBoundsException("Index: " + i);
        }
    }

    public Object set(int i, Object obj) {
        ListIterator listiterator = listIterator(i);
        try {
            Object obj1 = listiterator.next();
            listiterator.set(obj);
            return obj1;
        }
        catch (NoSuchElementException _ex) {
            throw new IndexOutOfBoundsException("Index: " + i);
        }
    }

    public void add(int i, Object obj) {
        ListIterator listiterator = listIterator(i);
        listiterator.add(obj);
    }

    public Object remove(int i) {
        ListIterator listiterator = listIterator(i);
        Object obj;
        try {
            obj = listiterator.next();
        }
        catch (NoSuchElementException _ex) {
            throw new IndexOutOfBoundsException("Index: " + i);
        }
        listiterator.remove();
        return obj;
    }

    public boolean addAll(int i, Collection collection) {
        boolean flag = false;
        ListIterator listiterator = listIterator(i);
        for (Iterator iterator1 = collection.iterator(); iterator1.hasNext();) {
            listiterator.add(iterator1.next());
            listiterator.next();
            flag = true;
        }

        return flag;
    }

    public Iterator iterator() {
        return listIterator();
    }

    public abstract ListIterator listIterator(int i);
}
