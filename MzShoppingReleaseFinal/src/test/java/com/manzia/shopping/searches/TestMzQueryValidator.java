package com.manzia.shopping.searches;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMzQueryValidator {
	
	private static EJBContainer container;
	private MzQueryValidator queryValidator;
	private @MzImmediateSearch MzSearchServiceImmediate immediateSearch;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Use the same container instance for all the tests
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(EJBContainer.APP_NAME, "SearchServiceBean-ejb" );
		container = EJBContainer.createEJBContainer(props);
		assert container != null;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Close the container instance
		if (container != null) {
			container.close();
		}
		container = null;
	}

	@Before
	public void setUp() throws Exception {
		immediateSearch = (MzSearchServiceImmediate) this.getBean("MzSearchServiceImmediate");
		assertNotNull("MzSearchServiceImmediate instance is null", immediateSearch);
		queryValidator = new MzQueryValidator();
		assertNotNull(queryValidator);
	}

	@After
	public void tearDown() throws Exception {
		if (immediateSearch != null) {
			immediateSearch = null;
		}
		if (queryValidator != null) {
			queryValidator = null;
		}
	}

	@Test
	public void testValidateQueryParameters() {
		
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("Category", "TVs");
		queryParams.put("Condition", "new");
		queryParams.put("Brand", "Sony");
		queryParams.put("Regular Price", "1000.00");
		queryParams.put("Display Type", "Plasma");
		queryParams.put("Vertical Resolution", "1080p");
		queryParams.put("Refresh Rate", "60Hz");
		queryParams.put("TV Height", "41inch");
		queryParams.put("TV Depth", "21inch");
		queryParams.put("DLNA", "YES");
		queryParams.put("HDMI Inputs", "2");
		queryParams.put("USB Input", "Yes");
		queryParams.put("Video Inputs", "2");
		queryParams.put("Composite Inputs", "2");
		queryParams.put("Media Card", "Yes");
		queryParams.put("Speakers", "True");
		queryParams.put("Output Power", "20W");
		queryParams.put("V-Chip", "Yes");
		queryParams.put("Aspect Ratio", "4:3");
		queryParams.put("PC Inputs", "Yes");
		queryParams.put("Audio Outputs", "3");
		
		//Invalid parameter inserted
		queryParams.put("Cores", "Dual core");
		
		// Test removal
		Map<String, String> validParams = queryValidator.validateQueryParameters(queryParams, immediateSearch);
		assertNotNull("Query Parameter are invalid", validParams);
		assertTrue("InvalidKey was not removed", !validParams.containsKey("Cores"));		
	}
	
	@Test
	public void validateQueryParametersCategory() {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("Category", "invalidCategory");		// test for invalid category value
		queryParams.put("Condition", "new");
		queryParams.put("Brand", "Sony");
		queryParams.put("Regular Price", "1000.00");
		queryParams.put("Display Type", "Plasma");
		queryParams.put("Vertical Resolution", "1080p");
		queryParams.put("Refresh Rate", "60Hz");
		queryParams.put("TV Height", "41inch");
		queryParams.put("TV Depth", "21inch");
		queryParams.put("DLNA", "YES");
		queryParams.put("HDMI Inputs", "2");
		queryParams.put("USB Input", "Yes");
		queryParams.put("Video Inputs", "2");
		queryParams.put("Composite Inputs", "2");
		queryParams.put("Media Card", "Yes");
		queryParams.put("Speakers", "True");
		queryParams.put("Output Power", "20W");
		queryParams.put("V-Chip", "Yes");
		queryParams.put("Aspect Ratio", "4:3");
		queryParams.put("PC Inputs", "Yes");
		queryParams.put("Audio Outputs", "3");
		
			
		// Test for invalid category value, should return null
		Map<String, String> validParams = queryValidator.validateQueryParameters(queryParams, immediateSearch);
		assertNull("Map created with invalid Category value", validParams);
	}
	
	// Method that returns an instance of the request bean from the
	// EJBContainer
	public Object getBean(String bean) throws NamingException {
		return container.getContext()
				.lookup("java:global/SearchServiceBean-ejb/classes/" + bean);
	}

}
