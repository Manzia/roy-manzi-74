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

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Response to a Remix API call to the "stores" collection resource.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class StoresResponse extends CollectionResponse {
    /**
     * List of {@link Store stores} returned with this response 
     */
    private List<Store> stores;

    /**
     * Creates a new StoresResponse from an HTTP connection.
     * 
     * @param  connection HTTP connection used to retrieve this response
     * @throws RemixException Error parsing HTTP response
     */
    public StoresResponse(HttpURLConnection connection) throws RemixException {
        super(connection);
    }

    /**
     * Returns a list of {@link Store stores} returned with this response.
     * 
     * @return List of {@link Store stores} returned with this response. 
     */
    public List<Store> list() {
        if(null == stores) {
            stores = new ArrayList<Store>();
            Element doc = getDocumentRoot();
            if(null != doc && doc.hasChildren()) {
                for(Element store : doc.getChildren()) {
                    stores.add(new Store(store));
                }
            }       
        }
        return stores;
    }
}
