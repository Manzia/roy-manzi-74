package com.manzia.shopping.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.ext.mysql.MySqlConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xml.sax.InputSource;

public abstract class MzAbstractJPADbUnitTestCase extends MzAbstractJPATestCase {
	
	// We are not using an embedded database for testing since
	// we want to test the exact production environment and initially
	// we do not expect database testing to impose a long delay on
	// the build
	protected static MySqlConnection dbunitConnection;
	
	@BeforeClass
	public static void setupDbUnit() throws Exception {
		dbunitConnection = new MySqlConnection(connection, null);
	}
	
	@AfterClass
	public static void closeDbUnit() throws Exception {
		if (dbunitConnection != null) {
			dbunitConnection = null;	//connection is closed in superclass
		}
	}
	
	// Get the XML as a dataset
	protected IDataSet getDataSet(String name) throws Exception {
		InputStream inputStream = getClass().getResourceAsStream(name);
		assertNotNull("file"+name+" not found in classpath", inputStream);
		InputSource inputSource = new InputSource(inputStream);
		FlatXmlDataSet dataset = new FlatXmlDataSet(new FlatXmlProducer(inputSource));
		return dataset;
	}
	
	 public static String toString(IDataSet dataSet) throws DataSetException, IOException {
		    StringWriter writer = new StringWriter();
		    try {
		      if ( dataSet != null ) {
		        FlatXmlDataSet.write(dataSet, writer);       
		      } else {
		        writer.write("null");
		      }
		      return writer.toString();
		    } finally {
		      writer.close();
		    }
	}		  

}
