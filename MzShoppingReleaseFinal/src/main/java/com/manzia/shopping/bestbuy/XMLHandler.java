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

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;



/**
 * Parses an XML document stream into a tree of generic {@link Element}s.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class XMLHandler extends DefaultHandler {

    /**
     * Element tree parsed from an XML document
     */
    private Element documentRoot;

    /** 
     * Captures character data inside an element
     */
    private StringBuffer buffer;

    /**
     * Used to hold elements during DFS traversal of an XML document
     */
    private Stack<Element> stack;

    /**
     * Instantiates a new handler to parse a Remix API XML response
     */
    public XMLHandler() {
        buffer = new StringBuffer();
        stack  = new Stack<Element>();
    }

    /**
     * Retrieve the root element of the document tree parsed by this handler.
     * 
     * @return Document root element
     */
    public Element getDocumentRoot() {
        return documentRoot;
    }

    /**
     * Receive notification of character data inside an element.
     */
    public void characters(char ch[], int start, int length) {
        buffer.append(ch, start, length);
    }

    /**
     * Receive notification of the start of an element.
     */
    public void startElement( String uri, String localName, String qName, Attributes attribs ) {
        Element e = new Element(localName);
        for(int i = 0; i < attribs.getLength(); ++i) {
            e.setAttribute(attribs.getQName(i), attribs.getValue(i));
        }
        stack.push(e);
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String name, String qName) {
        Element current = stack.pop();
        if(0 < buffer.length()) {
            current.setValue(buffer.toString().replace("\n","").trim());
            buffer = new StringBuffer();
        }
        if(stack.size() > 0) {
            stack.peek().addChild(current);
        } else {
            documentRoot = current;
        }
    }
}
