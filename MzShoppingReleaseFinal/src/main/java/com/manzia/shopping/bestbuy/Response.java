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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Base class for all responses to a Remix API call.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 * 
 * Modified: 09/27/2012 by Roy Manzi Tumubweinee
 * Manzia Corporation
 */
public class Response {
    /**
     * Root element of the response document tree
     */
    private Element documentRoot;

    /**
     * Error document constructed from an error response
     */
    private ErrorDocument error;

    /**
     * HTTP headers returned with this response
     */
    private Map<String,List<String>> httpHeaders;

    /**
     * HTTP status code associated with this response
     */
    private int responseCode;

    /**
     * Creates a new Response from an HTTP connection.
     * 
     * @param  connection HTTP connection associated with a Remix API call
     * @throws RemixException Thrown if an error occurs during the API request/response
     */
    public Response(HttpURLConnection connection) throws RemixException {
        try {
            XMLReader  reader  = RemixUtil.createXMLReader();
            XMLHandler handler = new XMLHandler();
            reader.setContentHandler(handler);
            reader.setErrorHandler(handler);
            responseCode = connection.getResponseCode();
            httpHeaders  = connection.getHeaderFields();
            InputStream productStream = isError() ? connection.getErrorStream() : connection.getInputStream();
            assert productStream != null;
            Reader productReader = new InputStreamReader(productStream, "UTF-8");
            assert productReader != null;
            InputSource productSource = new InputSource(productReader);
            assert productSource != null;
            productSource.setEncoding("UTF-8");
            //reader.parse(new InputSource(isError() ? connection.getErrorStream() : connection.getInputStream()));
            reader.parse(productSource);
            documentRoot = handler.getDocumentRoot();
            connection.disconnect();
        } catch(Exception e) {
            e.printStackTrace();
            throw new RemixException("Error parsing HTTP response", e);
        }
    }
    
    /**
     * Creates a new Response from an XML file of products (this applies
     * to cases where we already product XML files on disk and are not
     * making a network connection to the BestBuy API
     * 
     * @param productsResponse the absolute filename of the product XML file  
     * @throws RemixException thrown if an error occurs during the XML parsing
     * 
     * @author Roy Manzi Tumubweinee - added Sept.27, 2012, Manzia Corporation
     */
    public Response(File productsResponse) throws RemixException
    {
    	// check the input File
    	if (!productsResponse.isFile()) {
    		throw new IllegalArgumentException("Input File is invalid");
    	}
    	
    	try {
    		XMLReader reader = RemixUtil.createXMLReader();
    		assert reader != null;
    		XMLHandler handler = new XMLHandler();
    		assert handler != null;
    		reader.setContentHandler(handler);
    		reader.setErrorHandler(handler);
    		this.responseCode = HttpURLConnection.HTTP_OK;	// hardcode the HTTP Response Code as Success
    		this.httpHeaders = new HashMap<String, List<String>>();		//assign empty Headers
    		InputStream productStream = new FileInputStream(productsResponse);
    		assert productStream != null;
    		Reader utf8Reader = new InputStreamReader(productStream, "UTF-8");    		
    		assert utf8Reader != null;
    		InputSource productSource = new InputSource(utf8Reader);
    		assert productSource != null;
    		productSource.setEncoding("UTF-8");
    		reader.parse(productSource);
    		this.documentRoot = handler.getDocumentRoot();    		
    		productStream.close();
    	} catch(Exception e) {
    		e.printStackTrace();
    		throw new RemixException("Error parsing XML file", e);
    	} 
    }

    /**
     * Returns an attribute of the response document's root element (the collection element).
     * 
     * @param  name Name of the attribute to be accessed
     * @return Value of a document root element attribute
     */
    public String getAttribute(String name) {
        return null == documentRoot ? null : documentRoot.getAttribute(name);
    }

    /**
     * Retrieves the root element of the response document.
     * 
     * @return Root element of the response document
     */
    public Element getDocumentRoot() {
        return documentRoot;
    }

    /**
     * Returns an error document parsed from an error response.
     * 
     * @return ErrorDocument parsed from an error response
     */
    public ErrorDocument getError() {
        if(!isError()) {
            return null;
        } else if(null == error) {
            error = new ErrorDocument(responseCode, getDocumentRoot());
        }
        return error;
    }

    /**
     * Returns a List of values associated with a specified HTTP response header.
     * 
     * @return Map containing HTTP response headers
     */
    public List<String> getHeader(String name) {
        return httpHeaders.get(name);
    }

    /**
     * Returns an unmodifiable Map of response headers.
     * 
     * @return Map containing HTTP response headers
     */
    public Map<String,List<String>> getHeaders() {
        return httpHeaders;
    }

    /**
     * Returns the HTTP status code returned with this Response.
     * 
     * @return HTTP status code returned with this response
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Indicates whether an API call resulted in an error.
     * 
     * @return Whether any error occurred during an API call
     */
    public boolean isError() {
        return HttpURLConnection.HTTP_BAD_REQUEST <= responseCode;
    }
}
