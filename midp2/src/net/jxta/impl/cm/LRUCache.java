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
package net.jxta.impl.cm;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Iterator;
import com.sun.java.util.collections.List;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class implements a Generic LRU Cache
 *
 * @author Ignacio J. Ortega
 * @author Mohamed Abdelaziz
 */

public class LRUCache {

    private transient int cacheSize;
    private transient int currentSize;
    private transient CacheNode first;
    private transient CacheNode last;
    private transient Hashtable nodes;


    /**
     * Constructor for the LRUCache object
     *
     * @param size Description of the Parameter
     */
    public LRUCache(int size) {
        currentSize = 0;
        cacheSize = size;
        nodes = new Hashtable(size);
    }

    /**
     * clear the cache
     */
    public void clear() {
        first = null;
        last = null;
    }

    /**
     * returns the number of elements currently in cache
     *
     * @retrun the number of elements in cache
     */
    public int size() {
        return currentSize;
    }

    /**
     * retrieve an object from cache
     *
     * @param key key
     * @return object
     */
    public Object get(Object key) {
        CacheNode node = (CacheNode) nodes.get(key);
        if (node != null) {
            moveToHead(node);
            return node.value;
        }
        return null;
    }

    public boolean contains(Object key) {
        return nodes.contains(key);
    }

    protected Iterator iterator(int size) {
        List list = new ArrayList();
        Enumeration e = nodes.elements();
        while (e.hasMoreElements()) {
            list.add(((CacheNode) e.nextElement()).value);
            if (list.size() >= size) {
                break;
            }
        }
        return list.iterator();
    }

    private void moveToHead(CacheNode node) {
        if (node == first) {
            return;
        }
        if (node.prev != null) {
            node.prev.next = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        }
        if (last == node) {
            last = node.prev;
        }
        if (first != null) {
            node.next = first;
            first.prev = node;
        }
        first = node;
        node.prev = null;
        if (last == null) {
            last = first;
        }
    }

    /**
     * puts an object into cache
     *
     * @param key   key to store value by
     * @param value object to insert
     */
    public void put(Object key, Object value) {
        CacheNode node = (CacheNode) nodes.get(key);
        if (node == null) {
            if (currentSize >= cacheSize) {
                if (last != null) {
                    nodes.remove(last.key);
                }
                removeLast();
            } else {
                currentSize++;
            }
            node = new CacheNode();
        }
        node.value = value;
        node.key = key;
        moveToHead(node);
        nodes.put(key, node);
    }

    /**
     * remove an object from cache
     *
     * @param key key
     * @return Object removed
     */
    public Object remove(Object key) {
        CacheNode node = (CacheNode) nodes.get(key);
        if (node != null) {
            if (node.prev != null) {
                node.prev.next = node.next;
            }
            if (node.next != null) {
                node.next.prev = node.prev;
            }
            if (last == node) {
                last = node.prev;
            }
            if (first == node) {
                first = node.next;
            }
        }
        return node;
    }

    /**
     * removes the last enry from cache
     */
    private void removeLast() {
        if (last != null) {
            if (last.prev != null) {
                last.prev.next = null;
            } else {
                first = null;
            }
            last = last.prev;
        }
    }

    /**
     * cache node object wrapper
     */
    protected class CacheNode {
        Object key;
        CacheNode next;

        CacheNode prev;
        Object value;

        /**
         * Constructor for the CacheNode object
         */
        CacheNode() {
        }
    }
}
