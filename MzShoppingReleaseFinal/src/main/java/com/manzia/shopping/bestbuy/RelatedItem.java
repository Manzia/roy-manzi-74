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

/**
 * Related item data returned with a {@link Product} representation.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class RelatedItem extends Entity {
    /**
     * Creates a new RelatedItem entity from a document representation.
     * 
     * @param element Root element of a RelatedItem representation
     */
    public RelatedItem(Element element) {
        super(element);
    }

    /**
     * Returns the "SKU #" attribute associated with this item.
     * 
     * @return Item SKU #
     */
    public String getSku() {
        return (String) getField("sku");
    }

    /**
     * Returns the "active" attribute associated with this item.
     * 
     * @return Is this item active?
     */
    public boolean isActive() {
        return Boolean.valueOf((String) getField("title"));
    }

    /**
     * Returns the "title" attribute associated with this item.
     * 
     * @return Item title
     */
    public String getTitle() {
        return (String) getField("title");
    }

    /**
     * Returns the "type" attribute associated with this item.
     * 
     * @return Item type
     */
    public String getType() {
        return (String) getField("type");
    }
}
