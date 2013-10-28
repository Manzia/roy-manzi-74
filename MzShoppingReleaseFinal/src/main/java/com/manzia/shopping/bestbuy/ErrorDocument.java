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
import java.util.List;

/**
 * Error document returned in response to an API call.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class ErrorDocument extends Entity {
    /**
     * HTTP status code associated with this error
     */
    private int statusCode;

    /**
     * Creates a new error document with a specified status code and document tree.
     * 
     * @param statusCode HTTP status associated with this error
     * @param element    Document tree containing error data
     */
    public ErrorDocument(int statusCode, Element element) {
        super(element);
    }

    /**
     * Returns the HTTP status code associated with this error.
     * 
     * @return HTTP status code associated with this error
     */
    public int getCode() {
        return statusCode;
    }

    /**
     * Returns status text associated with this error.
     *  
     * @return Status text associated with this error
     */
    public String getStatus() {
        Object status = getField("status");
        return null == status ? "403 Forbidden" : (String) status;
    }

    /**
     * Returns message text associated with this error.
     * 
     * @return Message text associated with this error
     */
    public String getMessage() {
        Object message = getField("message");
        return null == message ? "Developer inactive" : (String) message;
    }

    /**
     * Returns a list of examples returned with this error document suggesting possible solutions.
     * 
     * @return List of example request URIs suggested as possible solutions for this error
     */
    public List<String> getExamples() {
        List<String> examples = new ArrayList<String>();
        Object field = getField("examples");
        if(field instanceof Element && ((Element) field).hasChildren()) {
            for(Element example : ((Element) field).getChildren()) {
                examples.add((String) example.getValue());
            }
        }
        return examples;
    }
}
