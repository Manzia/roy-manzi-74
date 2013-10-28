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
 * Representation of a Best Buy store.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class Store extends Entity {
    /**
     * List of available products (only included with a "store availability" response)
     */
    private List<Product> products;

    /**
     * Creates a new Store containing no data.
     */
    public Store() {
        super();
    }

    /**
     * Creates a new Store from a document representation.
     * 
     * @param element Root element of a store representation
     */
    public Store(Element element) {
        super(element);
    }

    /**
     * Returns the "storeId" property associated with this store.
     * 
     * @return Store ID
     */
    public String getStoreId() {
        return (String) getField("storeId");
    }

    /**
     * Returns the "name" property associated with this store.
     * 
     * @return Store name
     */
    public String getName() {
        return (String) getField("name");
    }

    /**
     * Returns the "address" property associated with this store.
     * 
     * @return Address (store location)
     */
    public String getAddress() {
        return (String) getField("address");
    }

    /**
     * Returns the "city" property associated with this store.
     * 
     * @return City (store location)
     */
    public String getCity() {
        return (String) getField("city");
    }

    /**
     * Returns the "region" property associated with this store.
     * 
     * @return Region (store location)
     */
    public String getRegion() {
        return (String) getField("region");
    }

    /**
     * Returns the "postalCode" property associated with this store.
     * 
     * @return Postal code (store location)
     */
    public String getPostalCode() {
        return (String) getField("postalCode");
    }

    /**
     * Returns the "fullPostalCode" property associated with this store.
     * 
     * @return Full postal code (store location)
     */
    public String getFullPostalCode() {
        return (String) getField("fullPostalCode");
    }

    /**
     * Returns the "country" property associated with this store.
     * 
     * @return Country (store location)
     */
    public String getCountry() {
        return (String) getField("country");
    }

    /**
     * Returns the "lat" property associated with this store.
     * 
     * @return Latitude (store location)
     */
    public String getLat() {
        return (String) getField("lat");
    }

    /**
     * Returns the "lng" property associated with this store.
     * 
     * @return Longitude (store location)
     */
    public String getLng() {
        return (String) getField("lng");
    }

    /**
     * Returns the "phone" property associated with this store.
     * 
     * @return Telephone number
     */
    public String getPhone() {
        return (String) getField("phone");
    }

    /**
     * Returns the "hours" property associated with this store.
     * 
     * @return Hours of operation
     */
    public String getHours() {
        return (String) getField("hours");
    }

    /**
     * Returns the "distance" property associated with this store.
     * 
     * @return Distance to this store in miles (returned when filtering by location)
     */
    public float getDistance() {
        return Float.valueOf((String) getField("distance"));
    }

    /**
     * Returns the "products" property associated with this store (only included in "store availabilty" calls).
     * 
     * @return List of available products at this store location
     */
    public List<Product> getProducts() {
        if(null == products) {
            products = new ArrayList<Product>();
            Object field = getField("products");
            if(null != field && field instanceof Element && ((Element) field).hasChildren()) {
                for(Element child : ((Element) field).getChildren()) {
                    products.add(new Product(child));
                }
            }
        }
        return products;
    }
}
