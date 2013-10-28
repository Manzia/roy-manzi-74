package com.manzia.shopping.services;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import com.manzia.shopping.services.CalculateAWSSignature;

/** Example URL request
* http://webservices.amazon.com/onca/xml?
* Service=AWSECommerceService
* &AWSAccessKeyId=AKIAIOSFODNN7EXAMPLE
* &Operation=ItemLookup
* &ItemId =0679722769
* &ResponseGroup=ItemAttributes,Offers,Images,Reviews
* &Version=2009-01-06
* &Timestamp=2009-01-01T12:00:00Z
*/

public class TestCalculateAWSSignature {
	private CalculateAWSSignature signedRequest;
	private Map<String, String> params;
	
	@Before
	public void instantiate() throws Exception {
		// create the CalculateAWSSignature object to test
		signedRequest = new CalculateAWSSignature();
		
		// create the Map of key-value pairs that make up the URL request
		params = new HashMap<String, String>();
		params.put("Service", "AWSEcommerceService");
		params.put("Operation", "ItemLookup");
		params.put("ItemId", "0679722769");
		params.put("ResponseGroup", "ItemAttributes,Offers,Images,Reviews");
		params.put("Version", "2011-01-06");
	}
		

	@Test
	public void testSign() {
		String result = signedRequest.sign(params);
		int equalSignIndex = result.lastIndexOf("=");
		String signatureString = "&Signature";
		int signatureKeyLength = signatureString.length();
		CharSequence signatureSequence = signatureString.subSequence(0, 
				signatureKeyLength-1);
		
		//Test signature key-value pair
		assertTrue("Signature key appended", result.contains(signatureSequence));
		assertTrue("Signature value appended", result.substring(
				equalSignIndex).length() >1);
	}

}

