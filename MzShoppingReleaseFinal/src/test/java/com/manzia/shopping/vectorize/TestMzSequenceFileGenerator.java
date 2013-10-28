package com.manzia.shopping.vectorize;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.math.VectorWritable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.manzia.shopping.vectorize.MzSequenceFileGenerator.MzSeqFileFilter;

public class TestMzSequenceFileGenerator {

	private static final String inputDir = "testDir";
	private static final String seqFileDir = "sequenceFiles";
	private static final String mergeFileDir = "merge-seqFiles";
	private static final String propertiesFile = "ManziaBestBuyVectorMap.txt";
	private static final String modelNumberLaptop = "Laptops:2000-2a10nr:HP";
	private static final String modelNumberTV = "TVs:KDL46HX750:Sony";
	private static final String modelNumberPrinter = "Printers:P2035:HP";
	private static final String modelNumberPhone = "Mobile Phones:SPH-L710:Samsung";
	private static final String modelNumberTablet = "Tablets:GT-P5113TSYXAR:Samsung";
	private static Set<String> modelNumberSet;
	private static File productsDirPath;
	
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		productsDirPath = new File (System.getProperty("user.dir"), inputDir);
		assertNotNull(productsDirPath);
		modelNumberSet = new HashSet<String>();
		modelNumberSet.add(modelNumberLaptop);
		modelNumberSet.add(modelNumberPhone);
		modelNumberSet.add(modelNumberPrinter);
		modelNumberSet.add(modelNumberTV);
		modelNumberSet.add(modelNumberTablet);
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		productsDirPath = null;
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		
		
	}
	
	/**
	 * Test that we can successfully read the Properties file
	 * @throws Exception
	 */
	@Test
	public void testMainProperties() throws Exception {
		Properties testFile;
		String[] arguments = {productsDirPath.toString(), propertiesFile};
		File seqDir = new File (inputDir, seqFileDir);
		MzSequenceFileGenerator.setSeqFileDirectory(seqDir.toString());
		File mergeDir = new File( inputDir, mergeFileDir);
		MzSequenceFileGenerator.setMergeFileDirectory(mergeDir.toString());
		MzSequenceFileGenerator.main(arguments);
		testFile = MzSequenceFileGenerator.getProperties();
		assertNotNull(testFile);
		assertFalse("Properties File is Empty", testFile.isEmpty());
		//System.out.println("Value for System Memory (RAM) Expandable To key is: " + testFile.getProperty("SystemMemory(RAM)ExpandableTo"));
		assertTrue("Value for manufacturer key is not Brand", 
				testFile.getProperty("manufacturer").equalsIgnoreCase("Brand"));
		
		
	}
	
		
	/*
	 * In this test, we create a few SequenceFiles by running the MzSequenceFileGenerator class
	 * then read each SequenceFile and test the Vectors contained within it
	*/	
	@Test
	public void testMainSequenceFileGenerator() throws Exception {
		String[] arguments = {productsDirPath.toString(), propertiesFile};
		
		// Generate the SequenceFiles
		File seqDir = new File (inputDir, seqFileDir);
		MzSequenceFileGenerator.setSeqFileDirectory(seqDir.toString());
		File mergeDir = new File( inputDir, mergeFileDir);
		MzSequenceFileGenerator.setMergeFileDirectory(mergeDir.toString());
		MzSequenceFileGenerator.main(arguments);
				
		// Read in the Sequence Files
		Configuration conf = new Configuration();
		FileSystem fs;
		MzSeqFileFilter seqFilter = new MzSeqFileFilter();
		List<File> mergeFiles = Arrays.asList(mergeDir.listFiles(seqFilter));
		assertNotNull(mergeFiles);
		ListIterator<File> mergeIterator = mergeFiles.listIterator();
		assertNotNull(mergeIterator);
		fs = FileSystem.get(conf);
		while (mergeIterator.hasNext()) {
			try {
				
				Path seqFilePath = new Path( mergeIterator.next().toString());
				assertNotNull(seqFilePath);
				SequenceFile.Reader reader = new SequenceFile.Reader(fs, seqFilePath, conf);
				assertNotNull(reader);
				Text key = new Text();
				VectorWritable value = new VectorWritable();
				
				// Deserializes the vector data and check that the Vector's name is an expected category:ModelNumber:Brand string
				while (reader.next(key, value)) {
					System.out.println("Vector Names: " + key.toString());
					assertTrue("Model Number is not key", modelNumberSet.contains(key.toString()));
					//System.out.println(" " + value.get().asFormatString());
				}
				reader.close();
			} catch (IOException e) {
				System.out.println("IO Exception....");
				e.printStackTrace();
				throw new RuntimeException("IO Exception while reading Sequence Files!" + e.getLocalizedMessage());
			}
		}		
		
	}
	
	@Test
	public void testMainSequenceFileGeneratorMergeFiles() throws Exception
	{
		String[] arguments = {productsDirPath.toString(), propertiesFile};
		File seqDir = new File (inputDir, seqFileDir);
		MzSequenceFileGenerator.setSeqFileDirectory(seqDir.toString());
		File mergeDir = new File( inputDir, mergeFileDir);
		MzSequenceFileGenerator.setMergeFileDirectory(mergeDir.toString());
		MzSequenceFileGenerator.main(arguments);
		
		// Test the MERGE_SEQFILE_DIR
		MzSeqFileFilter seqFilter = new MzSeqFileFilter();
		List<File> mergeFiles = Arrays.asList(mergeDir.listFiles(seqFilter));
		assertNotNull(mergeFiles);
		assertFalse("No files in Merge Directory", mergeFiles.isEmpty());
		assertTrue("Merged Files count does not equal Categories", 
				mergeFiles.size() == MzSequenceFileGenerator.getCategoryList().size());
		
		// Test SEQUENCE_DIR
		List<File> seqFiles = Arrays.asList(seqDir.listFiles(seqFilter));
		assertNotNull(seqFiles);
		assertTrue("Existing Sequence Files in Sequence Dir", seqFiles.isEmpty());
	}
	
	// Test that 

}
