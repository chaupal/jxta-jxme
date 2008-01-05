/*
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
 */
package net.jxta.util;

import com.sun.java.util.collections.Arrays;
import com.sun.java.util.collections.HashSet;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.Set;


public final class SimpleSelector extends AbstractSimpleSelectable {

    /*
     *  A simpleSelector It is not very usuable by multiple threads at once, unless they all do the same thing. Items are removed
     *  from the current batch when select returns it. As a result, any state change occuring in-between two calls to select is
     *  guaranteed to be reported by the next call. However, the new state may be observed before the next select call to select
     *  returns the corresponding event, which may then be viewed as redundant. So events are reported too much rather than too
     *  little.
     */
    /**
     * The small set of items that changed since the last batch was returned by
     * select.
     */
    private Set currentBatch = new HashSet(2);

    /**
     * Let it be newed for now. We need to find a place for a factory.
     */
    public SimpleSelector() {
    }

    /**
     * This is invoked by registered items when their state changes. Records
     * changes and performs notifyChange(), which will cause notification of
     * cascaded selectors, if any. It is thus possible to register selectors
     * with a selector and come to a particular one only when it has something
     * to report.
     *
     * @param item Description of the Parameter
     */
    public final void itemChanged(SimpleSelectable item) {
        synchronized (currentBatch) {
            currentBatch.add(item.getIdentityReference());
            currentBatch.notifyAll();
        }
        notifyChange();
    }

    /**
     * This blocks unless and until at least one of the selected items reports
     * to have changed. Then the list of such items is returned. More than one
     * item may be added to the list by the time it is returned to the invoker.
     * The name is deliberately not "select", so that we easily catch spots
     * where retrofitting is required when moving to NIO (if ever). Note: the
     * result cannot be a set, otherwise we would be prevented from returning
     * objects that overload hashCode/equals. It is thus a List. However, an
     * item will never be found twice in that list. The invoker should <b>never
     * </b> assume that all items in the result have indeed changed in any
     * expected manner or even changed at all. The simple action of registering
     * a selector may and usually does cause the selectable object to report a
     * change. In some cases a selectable object may just be unable to prove
     * that it has not changed, and thus report a change. It is up to the
     * invoker to inspect the relevant item's state every time.
     *
     * @return Description of the Return Value
     * @throws InterruptedException Description of the Exception
     */
    public List select() throws InterruptedException {

        Object[] result = null;
        int resLen = 0;

        synchronized (currentBatch) {
            while ((resLen = currentBatch.size()) == 0) {
                currentBatch.wait();
            }
            result = new Object[resLen];
            currentBatch.toArray(result);
            currentBatch.clear();
        }

        // Now we have to retrieve the real objects behind the identity references.
        // Costly, but unavoidable.
        int i = resLen;
        while (i-- > 0) {
            result[i] = ((IdentityReference) result[i]).getObject();
        }

        return Arrays.asList(result);
    }
}
