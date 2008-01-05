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
package net.jxta.platform;

import net.jxta.id.ID;

public abstract class ModuleClassID extends ID {

    /**
     * Returns true if this ModuleClassID is of the same base class than the
     * given class.
     * Note: This method is NOT named "isOfClass" because a ModuleClassID
     * may have two UUID; one that denotes a "base" class proper,
     * and an optional second one that denotes a "Role", or subclass.
     * Compatibility between ClassIDs is based on the "base" portion, hence the
     * "isOfSame" naming. This routine can be used for comparison with a base class
     * since a base class is just a class which role portion happens to be zero.
     *
     * @param id Module class id to compare with
     * @return boolean true if equals
     */

    public abstract boolean isOfSameBaseClass(ModuleClassID id);

    /**
     * Returns true if this ModuleClassID is of the same class than the
     * the given ModuleSpecID.
     *
     * @param id Module spec id to compare with
     * @return boolean true if equals
     */

    public abstract boolean isOfSameBaseClass(ModuleSpecID id);

    /**
     * Return a ModuleClassID of the same base class but with the role portion
     * set to zero. aka "the base class".
     *
     * @return ModuleClassID the base class.
     */
    public abstract ModuleClassID getBaseClass();
}
