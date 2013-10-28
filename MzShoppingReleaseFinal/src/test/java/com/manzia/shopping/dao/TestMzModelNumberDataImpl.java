package com.manzia.shopping.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.manzia.shopping.model.MzAbstractJPADbUnitTestCase;
import com.manzia.shopping.model.MzModelNumber;
import com.manzia.shopping.model.MzModelNumberHelper;
import com.manzia.shopping.model.MzModelNumberPK;
import com.manzia.shopping.model.MzProductResult;
import com.manzia.shopping.model.MzProductResultHelper;

public class TestMzModelNumberDataImpl extends MzAbstractJPADbUnitTestCase {
	private MzModelNumberDataImpl modelData;
	private MzProductResult productResult;
	private MzModelNumberPK keyModel;
	MzModelNumber retrievedModel;
	private static final String resultBrand = "Manzia";
	private static final String resultAvail = "In Stock";
	private static final float resultPrice = 25.75f;
	private static final String categoryTest = "Shopping App";
	
	@Before
	public void setUp() throws Exception {
		modelData = new MzModelNumberDataImpl();
		modelData.setEntityManager(manager);
	}

	@After
	public void tearDown() throws Exception {
		if (retrievedModel != null) {
			manager.remove(retrievedModel);	// cascade should also remove productResult
			manager.flush();
			retrievedModel = null;
		}
		// clear implementation
		modelData = null;
	}

	@Test
	public void testAddProductResult() throws Exception {
		// Insert a MzModelNumber instance
		IDataSet setupDataSet = getDataSet("/TestMzModelNumberFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);
		
		productResult = MzProductResultHelper.newMzProductResult();
		keyModel = MzModelNumberHelper.testPrimaryKey();
		modelData.addProductResult(productResult, keyModel);
		retrievedModel = manager.find(MzModelNumber.class, keyModel);
		assertNotNull("RetrievedModel is null", retrievedModel);
		
		// compare what we inserted to what we retrieved
		MzProductResult retrievedResult = retrievedModel.getProductResults().get(0);
		assertTrue("Brand does not match", retrievedResult.getProductBrand().equalsIgnoreCase(resultBrand));
		assertTrue("Availability does not match", retrievedResult.getProductAvail().equalsIgnoreCase(resultAvail));
		assertTrue("Price does not match", retrievedResult.getProductPrice() == resultPrice); 
	}

	@Test
	public void testDeleteProductResult() throws Exception {
		// Insert a MzModelNumber instance
		IDataSet setupDataSet = getDataSet("/TestMzModelNumberFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);
		
		productResult = MzProductResultHelper.newMzProductResult();
		keyModel = MzModelNumberHelper.testPrimaryKey();
		modelData.addProductResult(productResult, keyModel);
		retrievedModel = manager.find(MzModelNumber.class, keyModel);
		assertNotNull("RetrievedModel is null", retrievedModel);
		
		// check what we inserted then remove it and check again
		MzProductResult insertResult = retrievedModel.getProductResults().get(0);
		assertNotNull("ProductResult is null before delete", insertResult);
		modelData.deleteProductResult(insertResult);
		assertTrue("Retrieved Model is not empty", retrievedModel.getProductResults().isEmpty());
	}

	@Test
	public void testGetModelNumberByCategory() throws Exception {
		// Insert a MzModelNumber instance
		IDataSet setupDataSet = getDataSet("/TestMzModelNumberFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);
		keyModel = MzModelNumberHelper.testPrimaryKey();
		retrievedModel = manager.find(MzModelNumber.class, keyModel);
		assertNotNull("RetrievedModel is null", retrievedModel);
		
		// get the List of MzModelNumber instance
		List<MzModelNumber> models = modelData.getModelNumberByCategory(categoryTest);
		for (MzModelNumber model : models) {
			assertTrue("ModelNumber has incorrect ModelCategory", 
					model.getModelCategory().startsWith(categoryTest));
		}
	}

	@Test
	public void testGetAllModelCategories() throws Exception {
		// Insert a MzModelNumber instance
		IDataSet setupDataSet = getDataSet("/TestMzModelNumberFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);
		keyModel = MzModelNumberHelper.testPrimaryKey();
		retrievedModel = manager.find(MzModelNumber.class, keyModel);
		assertNotNull("RetrievedModel is null", retrievedModel);

		// get the List of MzModelNumber instance
		List<String> categories = modelData.getAllModelCategories();
		boolean success = false;
		for (String category : categories) {
			if (category.startsWith(categoryTest)) {
				success = true;
			}
		}
		assertTrue("Did not find inserted category", success);
	}

}
