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
package net.jxta.endpoint;

import com.sun.java.util.collections.*;
import net.jxta.document.MimeMediaType;
import net.jxta.id.UUID.UUID;
import net.jxta.id.UUID.UUIDFactory;
import net.jxta.util.AbstractSimpleSelectable;
import net.jxta.util.SimpleSelectable;
import net.jxta.util.java.io.StringWriter;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.io.IOException;
import java.util.NoSuchElementException;

public class Message extends AbstractSimpleSelectable {
    private static final transient Logger LOG = Logger.getInstance(Message.class.getName());

    /**
     * Magic value for this format of serialization version.
     */
    private static final long serialVersionUID = 3418026921074097757L;

    /**
     * If true, then modification logging be activated. This is a very expensive
     * option as it causes a stack crawl to be captured for every message
     * modification.
     * <p/>
     * <p/>To enable modification tracking, set to <code>true</code> and
     * recompile.
     */
    protected static final boolean LOG_MODIFICATIONS = false;

    /**
     * If true, then a tracking element is added to the message. This provides
     * the ability to follow messages throughout the network. If a message has
     * a tracking element then it will be used in the toString representation.
     * <p/>
     * <p/>To enable addition of a tracking element, set to <code>true</code>
     * and recompile.
     */
    protected static final boolean GLOBAL_TRACKING_ELEMENT = false;

    /**
     * Incremented for each standalone message instance.
     */
    private static transient volatile int messagenumber = 1;

    /**
     * This string identifies the namespace which is assumed when calls are
     * made that don't include a namespace specification.
     */
    protected final String defaultNamespace;

    /**
     * the namespaces in this message and the elements in each.
     * <p/>
     * <ul>
     * <li>keys are {@link String}</li>
     * <li>values are {@link java.util.List}
     * <ul>
     * <li>values are {@link MessageElement}</li>
     * </ul>
     * </li>
     * </ul>
     */
    protected transient Map namespaces = new HashMap();

    /**
     * List of the elements.
     * <p/>
     * <ul>
     * <li>values are {@link net.jxta.endpoint.Message.element}</li>
     * </ul>
     */
    protected transient List elements = new ArrayList();

    /**
     * Message properties HashMap
     * <p/>
     * <ul>
     * <li>keys are {@link Object}</li>
     * <li>values are {@link Object}</li>
     * </ul>
     */
    protected transient Map properties = Collections.synchronizedMap(new HashMap());

    /**
     * A list of {@link Integer} which details the lineage (history
     * of cloning) that produced this message. This message's number is index
     * 0, all of the ancestors are in order at higher indexes.
     */
    protected transient List lineage = new ArrayList();

    /**
     * Modification count of this message. Can be used to detect message being
     * modified when message is shared.
     */
    protected transient volatile int modCount = 0;

    /**
     * cached aggregate size of all the memeber elements. Used by
     * {@link #getByteLength()}
     */
    protected transient long cachedByteLength = 0;

    /**
     * modcount at the time the message length was last calculated. Used by
     * {@link #getByteLength()}
     */
    protected transient int cachedByteLengthModCount = -1;


    /**
     * If true then the message is modifiable. This is primarily intended as a
     * diagnostic tool for detecting concurrent modification.
     *
     * @deprecated You really should not depend on this feature.
     */
    public boolean modifiable = true;


    /**
     * The history of modifications this message.
     * <p/>
     * <p><ul>
     * <li>
     * Values are {@link Throwable} with the description field
     * formatted as <code>timeInAbsoluteMillis : threadName</code>.
     * </li>
     * </ul>
     */
    protected transient List modHistory;

    /**
     * A ListIterator for MessageElements which also provides the ability to
     * determine the namespace of the current message element. Message Elements
     * are iterated in the order in which they were added to the Message.
     * <p/>
     * <p/>This ListIterator returned is not synchronized with the message. If
     * you modify the state of the Message, the iterator will throw
     * ConcurrentModificationException when <code>next()</code> or
     * <code>previous()</code> is called.
     */
    public class ElementIterator implements ListIterator {

        /**
         * The elements being iterated.
         */
        ListIterator list;

        /**
         * The current element
         */
        element current = null;

        /**
         * The modCount at the time when the iterator was created.
         */
        transient int origModCount;

        /**
         * Intialize the iterator from a list iterator. The list iterator must
         * be an iterator of {@link element}.
         *
         * @param list The ListIterator we are managing.
         */
        ElementIterator(ListIterator list) {
            origModCount = Message.this.getMessageModCount();
            this.list = list;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            if (origModCount != Message.this.getMessageModCount()) {
                RuntimeException failure = new ConcurrentModificationException(Message.this + " concurrently modified. Iterator was made at mod " + origModCount);
                if (LOG.isEnabledFor(Priority.ERROR)) {
                    LOG.error(Message.this + " concurrently modified\n" + getMessageModHistory(), failure);
                }

                throw failure;
            }

            return list.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        public Object next() {
            if (origModCount != Message.this.getMessageModCount()) {
                RuntimeException failure = new ConcurrentModificationException(Message.this + " concurrently modified. Iterator was made at mod " + origModCount);
                if (LOG.isEnabledFor(Priority.ERROR)) {
                    LOG.error(Message.this + " concurrently modified\n" + getMessageModHistory(), failure);
                }
                throw failure;
            }

            current = (element) list.next();
            return current.element;
        }

        /**
         * {@inheritDoc}
         */
        public int nextIndex() {
            return list.nextIndex();
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasPrevious() {
            if (origModCount != Message.this.getMessageModCount()) {
                RuntimeException failure = new ConcurrentModificationException(Message.this + " concurrently modified. Iterator was made at mod " + origModCount);
                if (LOG.isEnabledFor(Priority.ERROR)) {
                    LOG.error(Message.this + " concurrently modified\n" + getMessageModHistory(), failure);
                }

                throw failure;
            }

            return list.hasPrevious();
        }

        /**
         * {@inheritDoc}
         */
        public Object previous() {
            if (origModCount != Message.this.getMessageModCount()) {
                RuntimeException failure = new ConcurrentModificationException(Message.this + " concurrently modified. Iterator was made at mod " + origModCount);
                if (LOG.isEnabledFor(Priority.ERROR)) {
                    LOG.error(Message.this + " concurrently modified\n" + getMessageModHistory(), failure);
                }

                throw failure;
            }

            current = (element) list.previous();
            return current.element;
        }

        /**
         * {@inheritDoc}
         */
        public int previousIndex() {
            return list.previousIndex();
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Not provided because the namespace cannot be specified.
         */
        public void add(Object obj) {
            throw new UnsupportedOperationException("add() not supported");
        }

        /**
         * {@inheritDoc}
         */
        public void remove() {
            if (origModCount != Message.this.getMessageModCount()) {
                RuntimeException failure = new ConcurrentModificationException(Message.this + " concurrently modified. Iterator was made at mod " + origModCount);
                if (LOG.isEnabledFor(Priority.ERROR)) {
                    LOG.error(Message.this + " concurrently modified\n" + getMessageModHistory(), failure);
                }

                throw failure;
            }

            if (null == current) {
                throw new IllegalStateException("no current element, call next() or previous()");
            }

            ListIterator elsPosition = Message.this.elements.listIterator();
            ListIterator nsPosition = ((List) (namespaces.get(current.namespace))).listIterator();

            int currentPrevious = list.previousIndex();

            // restart this iterator
            while (list.previousIndex() >= 0) {
                list.previous();
            }

            // readvance to the current position, but track in ns list and master list
            while (list.previousIndex() < currentPrevious) {
                element anElement = (element) list.next();

                try {
                    // advance to the same element in the master list.
                    element anElsElement;

                    do {
                        anElsElement = (element) elsPosition.next();
                    } while (anElement != anElsElement);

                    // advance to the same element in the ns list.
                    MessageElement anNsElement;

                    if (current.namespace.equals(anElement.namespace)) {
                        do {
                            anNsElement = (MessageElement) nsPosition.next();
                        } while (anElement.element != anNsElement);
                    }
                } catch (NoSuchElementException ranOut) {
                    RuntimeException failure = new ConcurrentModificationException(Message.this + " concurrently modified. Iterator was made at mod " + origModCount);
                    if (LOG.isEnabledFor(Priority.ERROR)) {
                        LOG.error(Message.this + " concurrently modified\n" + getMessageModHistory(), failure);
                    }

                    throw failure;
                }
            }

            elsPosition.remove();
            nsPosition.remove();
            list.remove();
            origModCount = Message.this.incMessageModCount();
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("Removed " + current.namespace + "::" + current.element.getElementName() + "/" + current.element.getClass().getName() + "@" + current.element.hashCode() + " from " + Message.this);
            }
            current = null;
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Replacement MessageElement will be in the same name space as the replaced element.
         */
        public void set(Object obj) {
            if (origModCount != Message.this.getMessageModCount()) {
                RuntimeException failure = new ConcurrentModificationException(Message.this + " concurrently modified. Iterator was made at mod " + origModCount);
                if (LOG.isEnabledFor(Priority.ERROR)) {
                    LOG.error(Message.this + " concurrently modified\n" + getMessageModHistory(), failure);
                }
                throw failure;
            }

            if (!(obj instanceof MessageElement)) {
                throw new IllegalStateException("replacement must be a MessageElement");
            }

            if (null == current) {
                throw new IllegalStateException("no current element, call next() or previous()");
            }

            ListIterator elsPosition = Message.this.elements.listIterator();
            ListIterator nsPosition = ((List) (namespaces.get(current.namespace))).listIterator();

            int currentPrevious = list.previousIndex();

            // restart this iterator
            while (list.previousIndex() >= 0) {
                list.previous();
            }

            // readvance to the current position, but track in ns list and master list
            while (list.previousIndex() < currentPrevious) {
                element anElement = (element) list.next();

                try {
                    // advance to the same element in the master list.
                    element anElsElement;

                    do {
                        anElsElement = (element) elsPosition.next();
                    } while (anElement != anElsElement);

                    // advance to the same element in the ns list.
                    MessageElement anNsElement;

                    if (current.namespace.equals(anElement.namespace)) {
                        do {
                            anNsElement = (MessageElement) nsPosition.next();
                        } while (anElement.element != anNsElement);
                    }
                } catch (NoSuchElementException ranOut) {
                    RuntimeException failure = new ConcurrentModificationException(Message.this + " concurrently modified. Iterator was made at mod " + origModCount);
                    if (LOG.isEnabledFor(Priority.ERROR)) {
                        LOG.error(Message.this + " concurrently modified\n" + getMessageModHistory(), failure);
                    }

                    throw failure;
                }
            }

            Message.element newCurrent = new Message.element(current.namespace, (MessageElement) obj);
            elsPosition.set(newCurrent);
            nsPosition.set(obj);
            list.set(newCurrent);
            origModCount = Message.this.incMessageModCount();
            if (LOG.isEnabledFor(Priority.DEBUG)) {
                LOG.debug("Replaced " + current.namespace + "::" + current.element.getElementName() + "/" + current.element.getClass().getName() + "@" + current.element.hashCode() +
                        " with " + newCurrent.namespace + "::" + newCurrent.element.getElementName() + "/" + newCurrent.element.getClass().getName() + "@" + newCurrent.element.hashCode() + " in " + Message.this);
            }
            current = newCurrent;
        }

        /**
         * return the namespace of the current element.
         *
         * @return String containing the name space of the current element.
         */
        public String getNamespace() {
            if (null == current) {
                throw new IllegalStateException("no current element, call next() or previous()");
            }

            return current.namespace;
        }
    }

    /**
     * holds an element and its namespace
     */
    protected static class element {
        String namespace;

        MessageElement element;

        element(String namespace, MessageElement element) {
            this.namespace = namespace;
            this.element = element;
        }
    }

    /**
     * Returns the next message number in sequence.
     *
     * @return the next message number in sequence.
     */
    protected static int getNextMessageNumber() {
        synchronized (Message.class) {
            return messagenumber++;
        }
    }

    /**
     * Standard Constructor for messages. The default namespace will be the
     * empty string ("")
     */
    public Message() {
        this("");

        if (GLOBAL_TRACKING_ELEMENT) {
            UUID tracking = UUIDFactory.newSeqUUID();

            MessageElement trackingElement = new StringMessageElement("Tracking UUID", tracking.toString(), null);

            addMessageElement("jxta", trackingElement);
        }
    }

    /**
     * Standard Constructor for messages.
     *
     * @param defaultNamespace the namespace which is assumed when calls are
     *                         made that don't include a namespace specification.
     */
    protected Message(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;

        lineage.add(new Integer(getNextMessageNumber()));

        if (LOG_MODIFICATIONS) {
            modHistory = new ArrayList();
            incMessageModCount();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p/> Duplicates the Message. The returned duplicate is a real copy. It may
     * be freely modified without causing change to the originally cloned
     * message.
     *
     * @return Message a Message that is a copy of the original message
     */
    public Object clone() {
        Message clone = new Message(getDefaultNamespace());

        clone.lineage.addAll(lineage);
        clone.elements.addAll(elements);

        Iterator eachNamespace = namespaces.keySet().iterator();

        while (eachNamespace.hasNext()) {
            String aNamespace = (String) eachNamespace.next();

            List namespaceElements = (List) namespaces.get(aNamespace);

            List newNamespaceElements = new ArrayList(namespaceElements.size());
            newNamespaceElements.addAll(namespaceElements);
            clone.namespaces.put(aNamespace, newNamespaceElements);
        }
        if (LOG.isEnabledFor(Priority.DEBUG)) {
            LOG.debug("Created clone " + clone + " of " + this);
        }

        return clone;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Compare this Message against another. Returns true if all of the
     * elements are identical and in the same order. Message properties
     * (setProperty/getProperty) are not considered in the calculation.
     *
     * @param target The  to compare against.
     * @return boolean true if the elements are identical.
     */
    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }

        if (target instanceof Message) {
            Message likeMe = (Message) target;

            ElementIterator myElements = getMessageElements();
            ElementIterator itsElements = likeMe.getMessageElements();

            while (myElements.hasNext()) {
                if (!itsElements.hasNext()) {
                    return false; // it has fewer than i do.
                }

                MessageElement mine = (MessageElement) myElements.next();
                MessageElement its = (MessageElement) itsElements.next();

                if (!myElements.getNamespace().equals(itsElements.getNamespace())) {
                    return false; // elements not in the same namespace
                }

                if (!mine.equals(its)) {
                    return false;       // content didnt match
                }
            }

            return (!itsElements.hasNext()); // ran out at the same time?
        }

        return false; // not a message
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int result = 0;
        Iterator eachElement = getMessageElements();

        while (eachElement.hasNext()) {
            MessageElement anElement = (MessageElement) eachElement.next();

            result += anElement.hashCode();
            result *= 6037; // a prime
        }

        if (0 == result) {
            result = 1;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p/> Displays the lineage of the message including its own message number.
     * This is useful primarily for debugging purposes.
     */
    public String toString() {
        StringBuffer toString = new StringBuffer(128);

        toString.append(getClass().getName());
        toString.append('@');
        toString.append(super.hashCode());
        toString.append('(');
        toString.append(modCount);
        toString.append("){");

        Iterator allLineage = getMessageLineage();
        while (allLineage.hasNext()) {
            toString.append(allLineage.next().toString());
            if (allLineage.hasNext()) {
                toString.append(',');
            }
        }

        toString.append('}');

        if (GLOBAL_TRACKING_ELEMENT) {
            toString.append("[");
            Iterator eachUUID = getMessageElements("jxta", "Tracking UUID");

            while (eachUUID.hasNext()) {
                toString.append("[");
                toString.append(eachUUID.next().toString());
                toString.append("]");
                if (eachUUID.hasNext()) {
                    toString.append(',');
                }
            }
            toString.append("]");
        }

        return toString.toString();
    }

    /**
     * Return the default Namespace of this message.
     *
     * @return The default namespace for this message.
     */
    protected String getDefaultNamespace() {
        return defaultNamespace;
    }

    /**
     * Add a MessageElement into the message. The MessageElement is stored in
     * the default namespace.
     *
     * @param add the Element to add to the message.
     */
    public void addMessageElement(MessageElement add) {

        addMessageElement(null, add);
    }

    /**
     * Add a MessageElement into the message using the specified namespace.
     *
     * @param namespace contains the namespace of the element to add. You can
     *                  specify null as a shorthand for the default namespace.
     * @param add       the MessageElement to add to the message.
     */
    public void addMessageElement(String namespace, MessageElement add) {
        if (null == namespace) {
            namespace = getDefaultNamespace();
        }

        if (null == add) {
            throw new IllegalArgumentException("Message Element must be non-null");
        }

        elements.add(new element(namespace, add));

        List namespaceElements = (List) namespaces.get(namespace);
        if (null == namespaceElements) {
            namespaceElements = new ArrayList();
            namespaces.put(namespace, namespaceElements);
        }

        namespaceElements.add(add);
        incMessageModCount();
        if (LOG.isEnabledFor(Priority.DEBUG)) {
            LOG.debug("Added " + namespace + "::" + add.getElementName() + "/" + add.getClass().getName() + "@" + add.hashCode() + " to " + this);
        }
    }

    /**
     * Replace a {@link MessageElement} in the message. This method will remove
     * all MessageElement instances in the default namespace which match the
     * specified name (if any) and then insert the replacement element. The
     * existing version of the element is returned, if more than one matching
     * element was removed, a random matching element is returned.
     * <p/>
     * <p/>For greatest control over element replacement, use the
     * {@link java.util.ListIterator#set(Object)} method as returned
     * by {@link #getMessageElements()},
     * {@link #getMessageElements(String)} or
     * {@link #getMessageElementsOfNamespace(String)}
     *
     * @param replacement the Element to be inserted into to the message.
     * @return One of the elements which was replaced or null if no existing
     *         matching item was located.
     */
    public MessageElement replaceMessageElement(MessageElement replacement) {
        return replaceMessageElement(null, replacement);
    }

    /**
     * Replace a {@link MessageElement} in the message using the specified
     * namespace. This method will remove all MessageElement instances which
     * match the specified name (if any) and then insert the replacement
     * element. The existing version of the element is returned, if more than
     * one matching element was removed, a random matching element is returned.
     * <p/>
     * <p/>For greatest control over element replacement, use the
     * {@link java.util.ListIterator#set(Object)} method as returned
     * by {@link #getMessageElements()},
     * {@link #getMessageElements(String)} or
     * {@link #getMessageElementsOfNamespace(String)}
     *
     * @param namespace   contains the namespace of the element to be replaced.
     *                    You can specify null as a shorthand for the default namespace.
     * @param replacement the Element to be inserted into to the message.
     * @return One of the elements which was replaced or null if no existing
     *         matching item was located.
     */
    public MessageElement replaceMessageElement(String namespace, MessageElement replacement) {
        if (null == namespace) {
            namespace = getDefaultNamespace();
        }

        if (null == replacement) {
            throw new IllegalArgumentException("Message Element must be non-null");
        }

        MessageElement removed = null;
        Iterator allMatching = getMessageElements(namespace, replacement.getElementName());

        while (allMatching.hasNext()) {
            MessageElement anElement = (MessageElement) allMatching.next();
            allMatching.remove();
            removed = anElement;
        }

        addMessageElement(namespace, replacement); // updates mod count

        return removed;
    }

    /**
     * Returns an iterator of the namespaces present in this message. All of the
     * elements will be Strings.
     *
     * @return iterator of strings of the namespaces of this message.
     */
    public Iterator getMessageNamespaces() {
        return Collections.unmodifiableMap(namespaces).keySet().iterator();
    }

    /**
     * Retrieve a element by name from the message without regard to
     * namespace. If there is more than one element with this name, a random
     * element will be returned.
     *
     * @param name The name of the element to attept to retrieve.
     * @return Element the element or null if no matching element could be
     *         found.
     */
    public MessageElement getMessageElement(String name) {
        Iterator eachElement = elements.listIterator();

        while (eachElement.hasNext()) {
            element anElement = (element) eachElement.next();

            if (name.equals(anElement.element.getElementName())) {
                return anElement.element;
            }
        }

        return null;
    }

    /**
     * Retrieve a element by name in the specified namespace from the message.
     * If there is more than one element with this name, a random
     * element will be returned.
     *
     * @param namespace contains the namespace of the element to get. You can
     *                  specify null as a shorthand for the default namespace.
     * @param name      contains the name of the element to get
     * @return Element the element.
     */
    public MessageElement getMessageElement(String namespace, String name) {
        if (null == namespace) {
            namespace = getDefaultNamespace();
        }

        List namespaceElements = (List) namespaces.get(namespace);

        // no namespace means no element.
        if (null == namespaceElements) {
            return null;
        }

        Iterator eachElement = namespaceElements.listIterator();

        while (eachElement.hasNext()) {
            MessageElement anElement = (MessageElement) eachElement.next();

            if (name.equals(anElement.getElementName())) {
                return anElement;
            }
        }

        return null;
    }

    /**
     * Returns a list iterator of all of the elements contained in this message.
     * Elements from all namespaces are returned.
     * <p/>
     * <p/>The iterator returned is not synchronized with the message and will
     * throw {@link java.util.ConcurrentModificationException} if the
     * message is modified.
     *
     * @return Enumeration of Elements.
     */
    public ElementIterator getMessageElements() {
        Vector theMsgElements = new Vector(elements);

        return new ElementIterator(theMsgElements.listIterator());
    }

    /**
     * Returns a list iterator  of all of the elements contained in this
     * message who's name matches the specified name. Elements from all
     * namespaces are returned. Message Elements are iterated in the order in
     * which they were added to the Message.
     * <p/>
     * <p/>The iterator returned is not synchronized with the message and will
     * throw {@link java.util.ConcurrentModificationException} if the
     * message is modified.
     *
     * @param name the name of the elements to match against
     * @return iterator of the elements matching the specified name, if any.
     */
    public ElementIterator getMessageElements(String name) {
        List theMsgElements = new ArrayList(elements.size());

        Iterator eachElement = elements.iterator();

        while (eachElement.hasNext()) {
            element anElement = (element) eachElement.next();

            if (name.equals(anElement.element.getElementName())) {
                theMsgElements.add(anElement);
            }
        }

        return new ElementIterator(theMsgElements.listIterator());
    }

    /**
     * Returns an list iterator  of all of the elements contained in this message
     * which match the specified namespace. Message Elements are iterated in
     * the order in which they were added to the Message.
     * <p/>
     * <p/>This ListIterator returned is not synchronized with the message. If
     * you modify the state of the Message, the iterator will throw
     * ConcurrentModificationException when <code>next()</code> or
     * <code>previous()</code> is called.
     * <p/>
     * <p/>The iterator returned is not synchronized with the message and will
     * throw {@link java.util.ConcurrentModificationException} if the
     * message is modified.
     *
     * @param namespace contains the namespace which must be matched in the
     *                  elements returned. You can specify null as a shorthand for the default
     *                  namespace.
     * @return Enumeration of Elements.
     */
    public ElementIterator getMessageElementsOfNamespace(String namespace) {
        List theMsgElements = new ArrayList(elements.size());

        if (null == namespace) {
            namespace = getDefaultNamespace();
        }

        Iterator eachElement = elements.iterator();

        while (eachElement.hasNext()) {
            element anElement = (element) eachElement.next();

            if (namespace.equals(anElement.namespace)) {
                theMsgElements.add(anElement);
            }
        }

        return new ElementIterator(theMsgElements.listIterator());
    }

    /**
     * Returns a list iterator  of all of the elements contained in the
     * specified namespace who's name matches the specified name in the order
     * in which they were added to the Message.
     * <p/>
     * <p/>The iterator returned is not synchronized with the message and will
     * throw {@link java.util.ConcurrentModificationException} if the
     * message is modified.
     *
     * @param namespace contains the namespace which must be matched in the
     *                  elements returned. You can specify null as a shorthand for the default
     *                  namespace.
     * @param name      contains the name of the elements to get
     * @return Enumeration of Elements.
     */
    public ElementIterator getMessageElements(String namespace, String name) {
        List theMsgElements = new ArrayList(elements.size());

        if (null == namespace) {
            namespace = getDefaultNamespace();
        }

        Iterator eachElement = elements.iterator();

        while (eachElement.hasNext()) {
            element anElement = (element) eachElement.next();

            if (namespace.equals(anElement.namespace) && name.equals(anElement.element.getElementName())) {
                theMsgElements.add(anElement);
            }
        }

        return new ElementIterator(theMsgElements.listIterator());
    }

    /**
     * Returns a list iterator of all of the elements contained in this message
     * whose mime-type matchs the given in the order they were added to the
     * message. Elements from all namespaces are returned.
     * <p/>
     * <p/>The iterator returned is not synchronized with the message and will
     * throw {@link java.util.ConcurrentModificationException} if the
     * message is modified.
     *
     * @param type contains the type of the elements to get
     * @return Enumeration of Elements.
     */
    public ElementIterator getMessageElements(MimeMediaType type) {
        ArrayList theMsgElements = new ArrayList(elements.size());
//        Vector theMsgElements = new Vector(elements.size());

        ListIterator eachElement = elements.listIterator();

        while (eachElement.hasNext()) {
            element anElement = (element) eachElement.next();

            if (type.equals(anElement.element.getMimeType())) {
                theMsgElements.add(anElement.element);
            }
        }

        return new ElementIterator(theMsgElements.listIterator());
    }

    /**
     * Returns a list iterator of all of the elements contained in this message
     * whose type matches the given in the order they were added to the message.
     * <p/>
     * <p/>The iterator returned is not synchronized with the message and will
     * throw {@link java.util.ConcurrentModificationException} if the
     * message is modified.
     *
     * @param namespace contains the namespace which must be matched in the
     *                  elements returned. You can specify null as a shorthand for the default
     *                  namespace.
     * @param type      contains the type of the elements to get
     * @return Enumeration of Elements.
     */
    public ElementIterator getMessageElements(String namespace, MimeMediaType type) {
        List theMsgElements = new ArrayList(elements.size());

        if (null == namespace) {
            namespace = getDefaultNamespace();
        }

        Iterator eachElement = elements.iterator();

        while (eachElement.hasNext()) {
            element anElement = (element) eachElement.next();

            if (namespace.equals(anElement.namespace) && type.equals(anElement.element.getMimeType())) {
                theMsgElements.add(anElement);
            }
        }

        return new ElementIterator(theMsgElements.listIterator());
    }

    /**
     * Remove an the first occurance of the provided MessageElement from the
     * message.
     *
     * @param remove the Element to remove from the message.
     * @return boolean returns true if the element was removed, otherwise false.
     */
    public boolean removeMessageElement(MessageElement remove) {
        Iterator eachElement = getMessageElements();

        while (eachElement.hasNext()) {
            MessageElement anElement = (MessageElement) eachElement.next();

            if (remove == anElement) {
                eachElement.remove();
                return true;
            }
        }

        return false;
    }

    /**
     * Remove the first occurance of the provided MessageElement within the
     * specified namespace from the message.  You can specify null as a
     * shorthand for the default namespace.
     *
     * @param namespace the namespace from which the element is to be removed.
     * @param remove    the Element to remove from the message.
     * @return boolean returns true if the element was removed, otherwise false.
     */
    public boolean removeMessageElement(String namespace, MessageElement remove) {
        Iterator eachElement = getMessageElementsOfNamespace(namespace);

        while (eachElement.hasNext()) {
            MessageElement anElement = (MessageElement) eachElement.next();

            if (remove == anElement) {
                eachElement.remove();
                return true;
            }
        }

        return false;
    }

    /**
     * Removes all of the elements in all namespaces from the message. Also
     * clears any properties set for this message.
     */
    public void clear() {
        elements.clear();
        namespaces.clear();
        properties.clear();
        // a cleared message has no ancestors
        lineage.retainAll(Collections.singleton(lineage.get(0)));
        incMessageModCount();
        if (LOG.isEnabledFor(Priority.DEBUG)) {
            LOG.debug("Cleared " + this);
        }
    }

    /**
     * Returns the aggregate size of all the memeber elements.
     *
     * @return the sum of all element sizes in bytes.
     */
    public synchronized long getByteLength() {
        if (modCount != cachedByteLengthModCount) {
            cachedByteLength = 0;
            Iterator eachElement = getMessageElements();

            while (eachElement.hasNext()) {
                MessageElement anElement = (MessageElement) eachElement.next();

                cachedByteLength += anElement.getByteLength();
            }

            cachedByteLengthModCount = modCount;
        }

        return cachedByteLength;
    }

    /**
     * Returns the modification count of this message. This ever ascending
     * number can be used to determine if the message has been modified by
     * another thread or for use in caching of parts of the message structure.
     *
     * @return the modification count of this message.
     */
    public int getMessageModCount() {
        return modCount;
    }

    /**
     * Returns the modification count of this message. This ever ascending
     * number can be used to determine if the message has been modified by
     * another thread or for use in caching of parts of the message structure.
     *
     * @return the modification count of this message.
     */
    protected synchronized int incMessageModCount() {
        modCount++;

        if (LOG_MODIFICATIONS) {
            modHistory.add(new Throwable(Long.toString(System.currentTimeMillis()) + " : " + Thread.currentThread().getName()));
        }

        if (!modifiable) {
            IllegalStateException failure = new IllegalStateException("Unmodifiable message should not have been modified");
            if (LOG.isEnabledFor(Priority.ERROR)) {
                LOG.error(failure, failure);
            }
            throw failure;
        }
        if (LOG.isEnabledFor(Priority.DEBUG)) {
            LOG.debug("Modification to " + this);
        }

        return modCount;
    }

    /**
     * Returns a string containing the modification history for this message
     *
     * @return a string containing the
     */
    public synchronized String getMessageModHistory() {

        if (LOG_MODIFICATIONS) {
            StringBuffer modHistoryStr = new StringBuffer("Message Modification History for ");

            modHistoryStr.append(toString());
            modHistoryStr.append("\n\n");

            for (int eachMod = modHistory.size() - 1; eachMod >= 0; eachMod--) {
                StringWriter aStackStr = new StringWriter();
                Throwable aStack = (Throwable) modHistory.get(eachMod);
                modHistoryStr.append("Modification #");
                modHistoryStr.append(eachMod + 1);
                modHistoryStr.append(":\n\n");
                modHistoryStr.append(aStack.getMessage());
                modHistoryStr.append("\n");
            }

            return modHistoryStr.toString();
        } else {
            return "Modification history tracking is disabled";
        }
    }

    /**
     * Returns the message number of this message. Message Numbers are intended
     * to assist with debugging and the management of message cloning.
     * <p/>
     * <p/>Each message is assigned a unique number upon creation. Message
     * Numbers are monotonically increasing for each message created.
     * <p/>
     * <p/>Message Numbers are transient, ie. if the message object is
     * serialized then the message number after deserialization will be
     * probably be a different value. Message numbers should not be used to
     * record permanent relationships between messages.
     *
     * @return int this message's message number.
     */
    public int getMessageNumber() {
        return (((Integer) lineage.get(0)).intValue());
    }

    /**
     * Returns an iterator which describes the lineage of this message. Each
     * entry is an {@link Integer} Message Number. The first entry is
     * this message's number, following entries are the ancestors of this
     * message.
     *
     * @return an Iterator of {@link Integer}. Each entry is a
     *         message number.
     */
    public Iterator getMessageLineage() {
        return lineage.iterator();
    }

    /**
     * Associate a transient property with this message. if there was a
     * previous value for the key provided then it is returned. This feature is
     * useful for managing the state of messages during processing and for
     * caching. <strong>Message Properties are not transmitted as part of the
     * Message when the message is serialized!</strong>
     * <p/>
     * <p/>The setting of particular keys may be controlled by a Java Security
     * Manager. Keys of type 'java.lang.Class' are checked against the caller of
     * this method. Only callers which are instances of the key class may modify
     * the property. This check is not possible through reflection. All other
     * types of keys are unchecked.
     *
     * @param key   the property key
     * @param value the value for the property
     * @return previous value for the property or null if no previous
     */
    public Object setMessageProperty(Object key, Object value) {

        /*
            if(key instanceof java.lang.Class) {
              Class keyClass = (Class) key;
              SecurityManager secure =  new SecurityManager() {
                public boolean checkCallerOfClass(Class toCheck) {
                  Class [] context = getClassContext();

                  return toCheck.isAssignableFrom(context[2]);
                }
              };

              if(!secure.checkCallerOfClass(keyClass)) {
                throw new SecurityException("You can't set that key from this context.");
              }
            }
         */

        Object res = properties.put(key, value);

        // Any property addition (including redundant) is notified. Removals are too, since
        // removal is done by assigning null.

        // Exception: when removing what was not there: no notification.

        if (!(res == null && value == null)) {
            notifyChange();
        }

        return res;
    }

    /**
     * Retrieves a transient property from the set for this message.
     *
     * @param key the property key.
     * @return value for the property or null if no property for this key.
     */
    public Object getMessageProperty(Object key) {

        return properties.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public void itemChanged(SimpleSelectable o) {
        // For now, messages are not themselves registered with anything.
        // Therefore itemChanged does not do a thing.
    }
}
