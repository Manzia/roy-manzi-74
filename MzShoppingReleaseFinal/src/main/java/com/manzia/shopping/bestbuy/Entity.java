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

import java.util.HashMap;
import java.util.Map;

/**
 * Generic entity consisting of uniquely named data fields.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class Entity {
    /**
     * Entity fields mapped as key/value pairs
     */
    protected Map<String, Object> fields;

    /**
     * Creates a new entity.
     */
    public Entity() {
        fields = new HashMap<String, Object>();
    }

    /**
     * Creates a new entity from a document tree.
     * 
     * Child elements must be uniquely named; duplicate elements should be wrapped in a 
     * uniquely named grouping element.
     * 
     * @param root Root element of the document tree containing entity fields
     */
    public Entity(Element root) {
        this();
        if(null != root && root.hasChildren()) {
            for(Element child : root.getChildren()) {
                if(child.hasChildren()) {
                    setField(child.getName(), child);
                } else {
                    setField(child.getName(), child.getValue());
                }
            }
        }
    }

    /**
     * Returns a field associated with this Entity.
     * 
     * @param  key Name of the field for which to retrieve a value
     * @return Value of the requested entity field
     */
    public Object getField(String key) {
        return fields.get(key);
    }

    /**
     * Assigns a new field value to this Entity.
     * 
     * @param  key   Field name identifier
     * @param  value Data assigned to this field
     * @return Provides a fluent interface.
     */
    protected Entity setField(String key, Object value) {
        fields.put(key, value);
        return this;
    }
}
