package com.manzia.shopping.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.manzia.shopping.model.MzAbstractJPADbUnitTestCase;
import com.manzia.shopping.model.MzProductSkus;

public class TestMzProductSkusDataImpl extends MzAbstractJPADbUnitTestCase {
	
	private MzProductSkus productSku;
	private MzProductSkusDataImpl productSkusImpl;
	private static final String prodSKUId = "111222333";

	@Before
	public void setUp() throws Exception {
		productSkusImpl = new MzProductSkusDataImpl();
		assertNotNull(productSkusImpl);
		productSkusImpl.setEntityManager(manager);
	}

	@After
	public void tearDown() throws Exception {
		if (productSku != null) {
			manager.remove(productSku);
			manager.flush();
			productSku = null;
		}
		// clear implementation
		productSkusImpl = null;
	}

	@Test
	public void testGetSkusByModelAndBrand() throws Exception {
		// Insert a MzProductSkus instance
		IDataSet setupDataSet = getDataSet("/TestMzProductSkusFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);
		
		String productModel = "A11B22C33";
		String productBrand = "Manzia";
		List<MzProductSkus> prodList = productSkusImpl.getSkusByModelAndBrand(productModel, productBrand);
		assertNotNull(prodList);
		assertTrue("Unexpected number of MzProductSkus", prodList.size() == 1);
		assertTrue("Invalid Product Model retrieved", prodList.get(0).getProductModel().equalsIgnoreCase(productModel));
		assertTrue("Invalid Product Brand retrieved", prodList.get(0).getProductBrand().equalsIgnoreCase(productBrand));

		// Added so added entry can be removed
		productSku = manager.find(MzProductSkus.class, prodSKUId);
		assertNotNull("Retrieved MzProductSkus is null", productSku);
	}

	@Test
	public void testGetProductSkusByCategory() throws Exception {
		// Insert a MzProductSkus instance
		IDataSet setupDataSet = getDataSet("/TestMzProductSkusFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);

		List<String> skuList = productSkusImpl.getProductSkusByCategory("shopping apps");
		assertNotNull(skuList);
		assertTrue("Unexpected number of MzProductSkus", skuList.size() == 1);
		assertTrue("Invalid MzProductSkus retrieved", skuList.get(0).equalsIgnoreCase(prodSKUId));

		// Added so added entry can be removed
		productSku = manager.find(MzProductSkus.class, prodSKUId);
		assertNotNull("Retrieved MzProductSkus is null", productSku);
	}

	@Test
	public void testGetCategoryForSKU() throws Exception {
		// Insert a MzProductSkus instance
		IDataSet setupDataSet = getDataSet("/TestMzProductSkusFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);
				
		productSku = manager.find(MzProductSkus.class, prodSKUId);
		assertNotNull("Retrieved MzProductSkus is null", productSku);

		// Verify the Category
		assertTrue("Incorrect product category retrieved", 
				productSku.getProductCategory().equalsIgnoreCase("shopping apps"));		
	}

	@Test
	public void testGetAllProductCategories() throws Exception {
		// Insert a MzProductSkus instance
		IDataSet setupDataSet = getDataSet("/TestMzProductSkusFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);
		
		List<String> categories = productSkusImpl.getAllProductCategories();
		assertNotNull(categories);
		assertTrue("Unexpected number of categories", categories.size() == 1);
		assertTrue("Incorrect category retrieved", categories.get(0).equalsIgnoreCase("shopping apps"));
		
		// Added so added entry can be removed
		productSku = manager.find(MzProductSkus.class, prodSKUId);
		assertNotNull("Retrieved MzProductSkus is null", productSku);
	}

	@Test
	public void testGetMzProductSku() throws Exception {
		// Insert a MzProductSkus instance
		IDataSet setupDataSet = getDataSet("/TestMzProductSkusFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);

		productSku = manager.find(MzProductSkus.class, prodSKUId);
		assertNotNull("Retrieved MzProductSkus is null", productSku);

		// Verify the product SKU inserted
		assertTrue("Incorrect product SKU retrieved", 
				productSku.getProductSku().equalsIgnoreCase(prodSKUId));	
	}

	@Test
	public void testAddDeleteMzProductSku() {
		String productSkuId = "9253005311";
		MzProductSkus prodSku = 
				new MzProductSkus(productSkuId, "Z11Y22X33", "Manzia", "Shopping App", "Manzia Retail");
		assertNotNull(prodSku);
		
		// add product SKU
		productSkusImpl.addMzProductSku(prodSku);
		assertNotNull("MzProductSkus was not Added", manager.find(MzProductSkus.class, productSkuId));
		
		//retrieve product SKU
		MzProductSkus retrievedSku = productSkusImpl.getMzProductSku(productSkuId);
		assertNotNull("Retrieved MzProductSkus is null", retrievedSku);

		// delete the device
		String newSkuId = retrievedSku.getProductSku();
		assertNotNull(newSkuId);
		productSkusImpl.deleteMzProductSku(newSkuId);
		assertNull("Deleted MzProductSkus is not null", manager.find(MzProductSkus.class, newSkuId));
	}

	

}
