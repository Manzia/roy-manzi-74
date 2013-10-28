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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Client for submitting requests to Best Buy's Remix API.
 * 
 * @link    http://remix.bestbuy.com/
 * @author  Modified BY: Roy Manzi Tumubweinee. Manzia Corporation.
 * @version $Id$
 */
public class Remix {
    /**
     * Entry point for all API calls
     */
    public static final String ENTRY_POINT = "http://api.remix.bestbuy.com/v1/";

    /**
     * Resource URI
     */
    public static final String
        PATH_STORES   = "stores",
        PATH_STORE    = "stores/%s.xml",
        PATH_PRODUCTS = "products",
        PATH_PRODUCT  = "products/%s.xml";

    /**
     * Default User-Agent string sent with each API request
     */
    public static final String USER_AGENT = "Manzia Corp Java client";

    /**
     * Client library version string included with User-Agent header
     */
    public static final String VERSION = "1.0.0";

    /**
     * Developer key used to access to the Remix API 
     */
    private String apiKey;

    /**
     * Request headers to be sent with an API call
     */
    private Map<String,String> headers = new HashMap<String,String>();

    /**
     * Creates a new Remix API client.
     * 
     * API keys may be obtained after registering for a developer account with Best Buy.
     * 
     * @param apiKey Identifier used to authenticate API requests
     */
    public Remix(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Clears HTTP request headers.
     * 
     * @return Provides a fluent interface.
     */
    public Remix clearHeaders() {
        headers.clear();
        return this;
    }

    /**
     * Makes an API call to the "product" resource, targeted by SKU #.
     * 
     * @param  sku SKU # used to identify the product resource being retrieved
     * @return API response
     * @throws RemixException 
     */
    public ProductResponse getProduct(int sku) throws RemixException {
        return getProduct(String.valueOf(sku), null);
    }

    /**
     * Makes an API call to the "product" resource, targeted by SKU #.
     * 
     * @param  sku SKU # used to identify the product resource being retrieved
     * @return API response
     * @throws RemixException
     */
    public ProductResponse getProduct(String sku) throws RemixException {
        return getProduct(sku, null);
    }

    /**
     * Makes an API call to the "product" resource, targeted by SKU #.
     * 
     * @param  sku    SKU # used to identify the product resource being retrieved
     * @param  params  Query string parameters
     * @return API response
     * @throws RemixException
     */
    public ProductResponse getProduct(String sku, Map<String,String> params) throws RemixException {
        String uri = new Formatter().format(PATH_PRODUCT, sku).toString();
        return new ProductResponse(query("GET", uri, params));
    }

    /**
     * Makes an API call to the "products" collection resource.
     * 
     * @return API response
     * @throws RemixException 
     */
    public ProductsResponse getProducts() throws RemixException {
        return getProducts(null, null);
    }

    /**
     * Makes an API call to the "products" collection resource.
     * 
     * @param  filters List of filters to be applied to the products collection
     * @return API response
     * @throws RemixException 
     */
    public ProductsResponse getProducts(List<String> filters) throws RemixException {
        return getProducts(filters, null);
    }

    /**
     * Makes an API call to the "products" collection resource.
     * 
     * @param  params Query string parameters
     * @return API response
     * @throws RemixException 
     */
    public ProductsResponse getProducts(Map<String,String> params) throws RemixException {
        return getProducts(null, params);
    }

    /**
     * Makes an API call to the "products" collection resource.
     * 
     * @param  filters List of filters to be applied to the products collection
     * @param  params  Query string parameters
     * @return API response
     * @throws RemixException 
     */
    public ProductsResponse getProducts(List<String> filters, Map<String,String> params) throws RemixException {
        String uri = PATH_PRODUCTS + RemixUtil.buildFilterString(filters);
        return new ProductsResponse(query("GET", uri, params));
    }

    /**
     * Makes an API call to the "store" resource, targeted by Store ID.
     * 
     * @param  storeId Identifier for the store resource being retrieved
     * @return API response
     * @throws RemixException 
     */
    public StoreResponse getStore(int storeId) throws RemixException {
        return getStore(String.valueOf(storeId), null);
    }

    /**
     * Makes an API call to the "store" resource, targeted by Store ID.
     * 
     * @param  storeId Identifier for the store resource being retrieved
     * @return API response
     * @throws RemixException
     */
    public StoreResponse getStore(String storeId) throws RemixException {
        return getStore(storeId, null);
    }

    /**
     * Makes an API call to the "store" resource, targeted by Store ID.
     * 
     * @param  storeId Identifier for the store resource being retrieved
     * @param  params  Query string parameters
     * @return API response
     * @throws RemixException
     */
    public StoreResponse getStore(String storeId, Map<String,String> params) throws RemixException {
        String uri = new Formatter().format(PATH_STORE, storeId).toString();
        return new StoreResponse(query("GET", uri, params));
    }

    /**
     * Makes an API call to the "stores" collection resource.
     * 
     * @return API response
     * @throws RemixException 
     */
    public StoresResponse getStores() throws RemixException {
        return getStores(null, null);
    }

    /**
     * Makes an API call to the "stores" collection resource.
     * 
     * @param  filters List of filters to be applied to the stores collection
     * @return API response
     * @throws RemixException 
     */
    public StoresResponse getStores(List<String> filters) throws RemixException {
        return getStores(filters, null);
    }

    /**
     * Makes an API call to the "stores" collection resource.
     * 
     * @param  params Query string parameters
     * @return API response
     * @throws RemixException 
     */
    public StoresResponse getStores(Map<String,String> params) throws RemixException {
        return getStores(null, params);
    }

    /**
     * Makes an API call to the "stores" collection resource.
     * 
     * @param  filters List of filters to be applied to the stores collection
     * @param  params  Query string parameters
     * @return API response
     * @throws RemixException 
     */
    public StoresResponse getStores(List<String> filters, Map<String,String> params) throws RemixException {
        String uri = PATH_STORES + RemixUtil.buildFilterString(filters);
        return new StoresResponse(query("GET", uri, params));
    }

    /**
     * Makes an API call to the "store availability" collection resource.
     * 
     * @param  storeFilters   List of filters to be applied to the stores resource
     * @param  productFilters List of filters to be applied to the products resource 
     * @return API response
     * @throws RemixException 
     */
    public StoresResponse getStoreAvailability(
            List<String> storeFilters, List<String> productFilters
    ) throws RemixException {
        return getStoreAvailability(storeFilters, productFilters, null);
    }

    /**
     * Makes an API call to the "store availability" collection resource.
     * 
     * @param  storeFilters   List of filters to be applied to the stores resource
     * @param  productFilters List of filters to be applied to the products resource 
     * @param  params         Query string parameters
     * @return API response
     * @throws RemixException 
     */
    public StoresResponse getStoreAvailability(
            List<String> storeFilters, List<String> productFilters, Map<String,String> params
    ) throws RemixException {
        String uri = PATH_STORES   + RemixUtil.buildFilterString(storeFilters) + "+" +
                     PATH_PRODUCTS + RemixUtil.buildFilterString(productFilters);
        return new StoresResponse(query("GET", uri, params));
    }

    /**
     * Assigns an HTTP header to be sent with API calls.
     * 
     * @param  name  Name of the request header being set
     * @param  value Value of the request header being set
     * @return Provides a fluent interface.
     */
    public Remix setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Assigns a set of HTTP headers to be sent with API calls.
     *  
     * @param  headers Parameters to be assigned
     * @return Provides a fluent interface.
     */
    public Remix setHeaders(Map<String, String> headers) {
        for(String key : headers.keySet()) {
            setHeader(key, headers.get(key));
        }
        return this;
    }

    /**
     * Makes an HTTP request and returns the {@link HTTPURLConnection connection}.
     * 
     * @param  method HTTP method for this request
     * @param  uri    Resource URI targeted by this request
     * @return HTTP Connection
     * @throws RemixException
     */
    private HttpURLConnection query(String method, String uri, Map<String,String> params) throws RemixException {
        HttpURLConnection connection;
        try {
            if(null == params) {
                params = new HashMap<String,String>();
            }
            params.put("apiKey", apiKey);
            URL url = new URL(ENTRY_POINT + uri + RemixUtil.buildQueryString(params));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.addRequestProperty("User-Agent", USER_AGENT + " v" + VERSION);
            for(String key : headers.keySet()) {
                connection.addRequestProperty(key, headers.get(key));
            }
        } catch (MalformedURLException e) {
            throw new RemixException("Invalid URL", e);
        } catch (ProtocolException e) {
            throw new RemixException("Invalid request protocol", e);
        } catch (IOException e) {
            throw new RemixException("IO Error: Please try again", e);
        }
        return connection;
    }
}
