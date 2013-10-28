/*
 * COPYRIGHT
 * Copyright (c) 2009, Matt Williams <matt@mattwilliamsnyc.com>
 *
 * LICENSE
 *
 * This source file is subject to the new BSD license bundled with this package
 * in the file, LICENSE. This license is also available through the web at:
 * {@link http://www.opensource.org/licenses/bsd-license.php}. If you did not
 * receive a copy of the license, and are unable to obtain it through the web,
 * please send an email to matt@mattwilliamsnyc.com, and I will send you a copy.
 */
package com.manzia.shopping.bestbuy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a node (element) in a document tree.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class Element {
    /**
     * Name of this element
     */
    private String name;

    /**
     * Value of this element
     */
    private Object value;

    /**
     * Attributes of this element as name/value pairs
     */
    private Map<String,String> attributes;

    /**
     * Children of this element
     */
    private List<Element> children;

    /**
     * Creates a new element with a given name and no value.
     * 
     * @param name Name of the element being created
     */
    public Element(String name) {
        this(name, null);
    }

    /**
     * Creates a new element with a given name and value.
     * 
     * @param name  Name of the element being created
     * @param value Value of the element being created
     */
    public Element(String name, Object value) {
        this.name  = name;
        this.value = value;
        children   = null;
    }

    /**
     * Adds a child element to this element.
     * 
     * @param  child Child element to be added
     * @return Provides a fluent interface.
     */
    public Element addChild(Element child) {
        if(null == children) {
            children = new ArrayList<Element>();
        }
        children.add(child);
        
        return this;
    }

    /**
     * Returns the value of an attribute by name; returns null if the attribute is not set.
     * 
     * @param  name Name of the attribute for which to retrieve a value
     * @return Value of the requested attribute
     */
    public String getAttribute(String name) {
        return null == attributes ? null : attributes.get(name);
    }

    /**
     * Returns all attributes associated with this element; returns null if no attributes are set.
     * 
     * @return Attributes associated with this element
     */
    public Map<String,String> getAttributes() {
        return attributes;
    }

    /**
     * Returns this element's children; returns null if this element has no children.
     * 
     * @return List of child elements
     */
    public List<Element> getChildren() {
        return children;
    }

    /**
     * Returns the name of this element.
     * 
     * @return Name of this element
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of this element.
     * 
     * @return Value of this element
     */
    public Object getValue() {
        return value;
    }

    /**
     * Indicates whether this element has any children.
     * 
     * @return Whether this element has any children
     */
    public boolean hasChildren() {
        return null != children && 0 != children.size();
    }

    /**
     * Removes a child element from this element's children.
     * 
     * @param  element Child element to be removed
     * @return Provides a fluent interface.
     * @throws RemixException Thrown if the specified Element is not a child of this Element
     */
    public Element removeChild(Element element) throws RemixException {
        if(null != children && children.contains(element)) {
            children.remove(element);
        } else {
            throw new RemixException("Child element not found.");
        }
        return this;
    }

    /**
     * Assigns an attribute to this Element.
     * 
     * @param  name  Name of the attribute to be assigned
     * @param  value Value of the attribute to be assigned
     * @return Provides a fluent interface.
     */
    public Element setAttribute(String name, String value) {
        if(null == attributes) {
            attributes = new HashMap<String,String>();
        }
        attributes.put(name, value);
        return this;
    }

    /**
     * Assigns a value to this Element.
     * 
     * @param  value Value to be assigned
     * @return Provides a fluent interface.
     */
    public Element setValue(Object value) {
        this.value = value;
        return this;
    }
}
