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
 * Offer data returned with a {@link Product} representation.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class Offer extends Entity {
    /**
     * Creates a new Offer entity from a document representation.
     * 
     * @param element Root element of an Offer representation
     */
    public Offer(Element element) {
        super(element);
    }

    /**
     * Returns the "id" attribute associated with this offer.
     * 
     * @return Offer ID
     */
    public String getId() {
        return (String) getField("id");
    }

    /**
     * Returns the "text" attribute associated with this offer.
     *  
     * @return Offer text
     */
    public String getText() {
        return (String) getField("text");
    }

    /**
     * Returns the "url" attribute associated with this offer.
     *  
     * @return Offer URL
     */
    public String getUrl() {
        return (String) getField("url");
    }
}
