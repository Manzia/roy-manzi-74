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
 * Exception class used to wrap library exceptions.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class RemixException extends Exception {
    private static final long serialVersionUID = 1L;

    public RemixException(String s) {
        super(s);
    }

    public RemixException(Exception e) {
        super(e);
    }

    public RemixException(String s, Exception e) {
        super(s,e);
    }
}
