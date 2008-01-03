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

public abstract class AbstractCollection implements Collection {

    protected AbstractCollection() {
    }

    public abstract Iterator iterator();

    public abstract int size();

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(Object obj) {
        Iterator iterator1 = iterator();
        if (obj == null) {
            while (iterator1.hasNext())
                if (iterator1.next() == null)
                    return true;
        } else {
            while (iterator1.hasNext())
                if (obj.equals(iterator1.next()))
                    return true;
        }

        return false;
    }

    public Object[] toArray() {
        Object aobj[] = new Object[size()];
        Iterator iterator1 = iterator();
        for (int i = 0; iterator1.hasNext(); i++)
            aobj[i] = iterator1.next();

        return aobj;
    }

    public Object[] toArray(Object aobj[]) {
        int i = size();
        if (aobj.length < i)
            aobj = new Object[i];
        Iterator iterator1 = iterator();
        for (int j = 0; j < i; j++)
            aobj[j] = iterator1.next();

        if (aobj.length > i)
            aobj[i] = null;
        return aobj;
    }

    public boolean add(Object obj) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object obj) {
        Iterator iterator1 = iterator();
        if (obj == null) {
            while (iterator1.hasNext()) {
                if (iterator1.next() == null) {
                    iterator1.remove();
                    return true;
                }
            }
        } else {
            while (iterator1.hasNext()) {
                if (obj.equals(iterator1.next())) {
                    iterator1.remove();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsAll(Collection collection) {
        for (Iterator iterator1 = collection.iterator(); iterator1.hasNext();) {
            if (!contains(iterator1.next()))
                return false;
        }
        return true;
    }

    public boolean addAll(Collection collection) {
        boolean flag = false;
        for (Iterator iterator1 = collection.iterator(); iterator1.hasNext();) {
            if (add(iterator1.next()))
                flag = true;
        }
        return flag;
    }

    public boolean removeAll(Collection collection) {
        boolean flag = false;
        for (Iterator iterator1 = iterator(); iterator1.hasNext();) {
            if (collection.contains(iterator1.next())) {
                iterator1.remove();
                flag = true;
            }
        }
        return flag;
    }

    public boolean retainAll(Collection collection) {
        boolean flag = false;
        for (Iterator iterator1 = iterator(); iterator1.hasNext();) {
            if (!collection.contains(iterator1.next())) {
                iterator1.remove();
                flag = true;
            }
        }
        return flag;
    }

    public void clear() {
        for (Iterator iterator1 = iterator(); iterator1.hasNext(); iterator1.remove())
            iterator1.next();

    }

    public String toString() {
        StringBuffer stringbuffer = new StringBuffer();
        Iterator iterator1 = iterator();
        stringbuffer.append("[");
        int i = size() - 1;
        for (int j = 0; j <= i; j++) {
            stringbuffer.append(String.valueOf(iterator1.next()));
            if (j < i)
                stringbuffer.append(", ");
        }

        stringbuffer.append("]");
        return stringbuffer.toString();
    }
}
