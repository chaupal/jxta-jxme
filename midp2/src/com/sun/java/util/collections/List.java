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

public interface List extends Collection {

    public abstract int size();

    public abstract boolean isEmpty();

    public abstract boolean contains(Object obj);

    public abstract Iterator iterator();

    public abstract Object[] toArray();

    public abstract Object[] toArray(Object aobj[]);

    public abstract boolean add(Object obj);

    public abstract boolean remove(Object obj);

    public abstract boolean containsAll(Collection collection);

    public abstract boolean addAll(Collection collection);

    public abstract boolean addAll(int i, Collection collection);

    public abstract boolean removeAll(Collection collection);

    public abstract boolean retainAll(Collection collection);

    public abstract void clear();

    public abstract boolean equals(Object obj);

    public abstract int hashCode();

    public abstract Object get(int i);

    public abstract Object set(int i, Object obj);

    public abstract void add(int i, Object obj);

    public abstract Object remove(int i);

    public abstract int indexOf(Object obj);

    public abstract int lastIndexOf(Object obj);

    public abstract ListIterator listIterator();

    public abstract ListIterator listIterator(int i);

    public abstract List subList(int i, int j);
}
