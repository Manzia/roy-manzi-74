package com.manzia.shopping.vectorize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import com.manzia.shopping.vectorize.MzRankingServiceCosine.MzCategoryFileFilter;

/**
 * Helper class for the MzRankingService implementations that
 * handles a lot of the FileSystem related tasks like creating
 * directories, generating filenames, and cleaning up files and
 * directories after operations
 * 
 * @author Roy Manzi Tumubweinee, Oct 7, 2012, Manzia Corporation
 *
 */

public final class MzRankingServiceUtils {
	
	//Logger
	public static final Logger logger = 
				Logger.getLogger(MzRankingServiceUtils.class.getCanonicalName());
	
	// Common Directories
	//public static final String INPUT_DIR = "category-files";	// Input Directory with category-specific SequenceFile
	public static final String SEED_DIR = "search-files";			// Seed Directory with SequenceFile containing Search Vector
	private static final String NAMESEPARATOR = "-";
	private static final List<String> categoryList = MzSequenceFileGenerator.getCategoryList();
	
	
	/**
	 * Method creates and returns the Input Directory, (INPUT_DIR) that should be used by an instance of
	 * the MzRankingService implementation. This is the directory that should be passed as the "-i" option
	 * of the VectorDistanceSimilarityJob. The Input Directory returned is category-specific and is dependent
	 * on the name of the search Vector being processed by the MzRankingService implementation.
	 * 
	 * @return - Input Directory as a java.io.File object.
	 * @throws IOException 
	 */
	public static File createInputDirectory( String vectorName ) {
		assert vectorName != null;
		
		// Iterate over the Category List
		File inputDir;
		StringBuffer buffer = new StringBuffer();
		Set<String> categorySet = Collections.synchronizedSet( new HashSet<String>());
		categorySet.addAll(categoryList);
		if (categorySet.contains(vectorName)) {
			buffer.append("inputDir-").append(vectorName.replaceAll("\\s", ""));
			inputDir = new File( System.getProperty( "user.dir"), buffer.toString());
		} else {
			logger.log(Level.WARNING, "Invalid category name in provided search Vector name: {0}", new Object[]{vectorName});
			return null;
		}
		assert inputDir != null;
		
		// Create
		boolean success = false;
		if (!inputDir.exists()) {
			success = inputDir.mkdirs();
			
			// Copy the Category-specific SequenceFile that this and similar search Vectors will be compared
			// against. Ideally, this operation should only happen once when the inputDir is being created
			if (success) {
				File mergeSequenceDir = new File (MzSequenceFileGenerator.getMergeFileDirectory());
		    	if (!mergeSequenceDir.exists() || !mergeSequenceDir.isDirectory()) {
		    		logger.log(Level.SEVERE, "Directory of Merged Sequence Files does not exist!");
		    		throw new RuntimeException("Directory of Merged Sequence Files does not exist!");
		    	}
		    	FilenameFilter categoryFilter = new MzCategoryFileFilter(vectorName);
		    	assert categoryFilter != null;
		    	File categorySeqFile = mergeSequenceDir.listFiles(categoryFilter)[0];	// we assume we always have 1 file so we get first one
		    	assert categorySeqFile != null;
		    	
		    	if (!categorySeqFile.exists() || !categorySeqFile.isFile()) {
		    		logger.log(Level.SEVERE, "Category-specific Sequence File is invalid!");
		    		throw new RuntimeException("Category-specific Sequence Files is invalid!");
		    	}
		    	
		    	// copy the SequenceFile to the Input Directory
		    	File destFile = new File(inputDir, categorySeqFile.getName());
		    	assert destFile != null;
		    	try {
					MzRankingServiceUtils.sequenceFileCopy(categorySeqFile, destFile);
				} catch (FileNotFoundException e1) {
					logger.log(Level.SEVERE, "FileNotFound exception while copying category Sequence File to INPUTDIR!");
					e1.printStackTrace();
					throw new RuntimeException("FileNotFound Exception while copying Sequence File to INPUTDIR" + e1.getLocalizedMessage());					
				} catch (IOException e) {
					logger.log(Level.WARNING, "Failed to copy file {0} to file {1}", 
							new Object[]{categorySeqFile.getName(), destFile.getName()});					
					e.printStackTrace();
					throw new RuntimeException("IO Exception while copying Sequence Files to INPUTDIR" + e.getLocalizedMessage());
				}		    	
			} else {
				logger.log(Level.SEVERE, "Failed to create Input Directory at Path: {0}", new Object[]{inputDir.toString()});
				inputDir = null;
			}
		}
		return inputDir;
	}
	
		
	// Create SEED_DIR
	/**
	 * Seed Directory holds the Search NamedVector(s) that will be compared to
	 * all the Vectors (SequenceFile) in the INPUT_DIR. This is the equivalent to
	 * the -s option passed to the VectorDistanceSimilarityJob class
	 * @return - java.io.File object representing the Seed Directory
	 */
	public static File createSeedDirectory() {
		File searchPathDir = new File( System.getProperty("user.dir"), SEED_DIR);
    	assert searchPathDir != null;

    	// Create the SEED_DIR directory if it doesnot already exist
    	if (!searchPathDir.exists()) {
    		searchPathDir.mkdirs();
    	}
    	return searchPathDir;
	}
	
	// generate Search Vector's Sequence File name
	/**
	 * Method generates the unique file name for the Search Vector's SequenceFile
	 * in the Seed Directory. The same name is used to generate the Output Directory name
	 * 
	 * @return - java.io.File object representing the Search Vector's SequenceFile's name
	 */
	public static File generateSequenceFilename() {
		
		// Note that the temp file created below will be overwritten by the SequenceFile.Writer
    	File searchPathDir = MzRankingServiceUtils.createSeedDirectory();
    	assert searchPathDir != null;
		File newSearchPath;
		try {
			// Note that we use the "part-" as the prefix since this is expected by the VectorDistanceSimilarityJob
			// in Mahout when determining the SequenceFile of seed vectors.
			newSearchPath = File.createTempFile("part-", "search", searchPathDir);			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO Exception while creating Search Vector's SequenceFile name at Path: {0}", 
					new Object[] {searchPathDir.toString()} );
			e.printStackTrace();
			throw new RuntimeException("IO Exception while creating SequenceFile temp filename! for search Vector!");
		}
    	assert newSearchPath != null;
    	return newSearchPath;
	}
	
	
	/**
	 * Creates an OUTPUTDIR given a Search Vector's SequenceFile name that has been
	 * generated from {@code generateSequenceFilename()} method. The generated directory
	 * is passed as the "-o" option to the VectorDistanceSimilarityJob
	 * 
	 * @param seqFilename - Filename from which OUTPUTDIR will be generated
	 * @return - OUTPUTDIR as a java.io.File object.
	 */
	public static String createOutputDirectory( File seqFilename ) {
		
		// check input
		assert seqFilename != null;
		
		StringBuffer buffer = new StringBuffer();
		if (seqFilename.exists() && seqFilename.isFile()) {
			String fileString = seqFilename.getName();
			if (fileString.startsWith("part-")) {
				buffer.append("dir").append(fileString.split(NAMESEPARATOR, 2)[1]);
			} else {
				logger.log(Level.WARNING, "Invalid SequenceFile name provided: {0}...cannot create OUTPUTDIR!", 
						new Object[] {seqFilename.getName()});
				throw new RuntimeException("OUTPUTDIR cannot be created!");
			}			
		}
		
		// Create the OUTPUTDIR
		File outputDir = new File ( System.getProperty("user.dir"), buffer.toString());
		assert outputDir != null;
		boolean success = false;
		if (!outputDir.exists()) {
			success= outputDir.mkdirs();
		}
		if (success) {
			logger.log(Level.INFO, "Created OUTPUTDIR at Path: {0}", new Object[] {outputDir.getAbsolutePath()});
		}
		return outputDir.getName();		
	}
	
	/**
	 * Given a Search Vector's SequenceFile name that has been generated from 
	 * {@code generateSequenceFilename()} method, this method will delete the
	 * associated OUTPUTDIR and the SequenceFile of the search Vector. This operation
	 * is necessary since OUTPUTDIR are created specific to each search Vector and are
	 * not required after computing the TopK "closest" Vectors for a search Vector.
	 * 
	 * @param seqFilename - a search Vector's SequenceFile name
	 */
	public static void cleanUp(File seqFilename) {
		
		assert seqFilename != null;
		boolean dirDeleted = false;
		boolean fileDeleted = false;
		if (seqFilename.exists() && seqFilename.isFile()) {
			File outputDir = new File( MzRankingServiceUtils.createOutputDirectory(seqFilename));
			if (outputDir.exists()) {
				File[] outputFiles = outputDir.listFiles();
				boolean success = false;
				for (int i=0; i<outputFiles.length; i++) {
					success = outputFiles[i].delete();
				}
				if (success) {
					dirDeleted = outputDir.delete();
				}
			}			
			fileDeleted = seqFilename.delete();
			
		}
		// Log
		if(fileDeleted && dirDeleted) {
			logger.log(Level.INFO, "Success cleaning up for search SequenceFile: {0}", new Object[]{seqFilename.getAbsolutePath()});
		} else {
			logger.log(Level.INFO, "Clean up FAILED for search SequenceFile: {0}", new Object[]{seqFilename.getAbsolutePath()});
		}
	}
	/**
	 * Helper method that cleans up the SEED_DIR after the Search Vector has been compared
	 * to the Vectors in the OUTPUT_DIR. We expect to always have at most 1 Search Vector (seed vector)
	 * in the SEED_DIR at any one time
	 * 
	 * @param seedDir - Seed Directory equivalent to the "-s" option of the VectorDistanceSimilarityJob class
	 */
	public static void clearSeedDirectory( File seedDir ) {
		boolean fileDeleted = false;
		if (seedDir.isDirectory()) {
			File[] seedFiles = seedDir.listFiles();
			assert seedFiles != null;
			if (seedFiles.length > 0) {
				for (int i=0; i<seedFiles.length; i++) {
					fileDeleted = seedFiles[i].delete();
				}
			}			
			// Log
			if (fileDeleted) {
				logger.log(Level.INFO, "Deleted all Seed Files in Seed Directory at Path: {0}", 
						new Object[] {seedDir.getAbsolutePath()});
			}
		} else {
			logger.log(Level.WARNING, "Invalid directory..cannot clear Seed Files at Path: {0}", 
					new Object[] {seedDir.getAbsolutePath()});
		}
	}
	
	/**
	 * Helper utility method to copy a SequenceFile from one location to another on the local fileSystem.
	 * 
	 * @param source - source file
	 * @param destination - destination file
	 * @throws IOException - thrown if error reading/writing the source/destination files
	 */
	public static void sequenceFileCopy(File source, File destination) throws IOException {
		//check inputs
		assert source != null;
		assert destination != null;
		
		FileSystem fs;
		if (source.exists() && source.isFile()) {
			Configuration conf = new Configuration();
			fs = FileSystem.getLocal(conf);
			fs.copyFromLocalFile(false, true, new Path( source.getAbsolutePath()), new Path( destination.getAbsolutePath()));
			if (destination.exists() && destination.isFile()) {
				logger.log(Level.INFO, "Copied file: {0} to file: {1}", 
						new Object[]{source.getAbsolutePath(), destination.getAbsolutePath()});
			} else {
				// we have an invalid source file
				logger.log(Level.WARNING, "Invalid source file...cannot copy file: {0}", new Object[]{source.getAbsolutePath()});
			}
		}
	}
	
	/**
	 * Helper utility method to copy a SequenceFile from one location to another on the local fileSystem.
	 * To copy files between the Hadoop FileSystem and the local fileSystem, use the Hadoop FileSystem class
	 * and or the HadoopUtil class
	 * 
	 * @param source - source file
	 * @param destination - destination file
	 * @throws FileNotFoundException - thrown if source/destination is a directory, destination cannot be created etc..Check
	 * the javadocs for the java.io.FileInputStream and java.io.FileOutputStream for all scenarios 
	 */
	/*public static void sequenceFileCopy(File source, File destination) throws FileNotFoundException {
		
		//check inputs
		assert source != null;
		assert destination != null;
		
		//Streams
		int numberRead = 0;
		InputStream readerStream = null;
		OutputStream writerStream = null;
		byte buffer[] = new byte[512];
		
		if (source.exists() && source.isFile()) {
			readerStream = new FileInputStream(source);
			assert readerStream != null;
			writerStream = new FileOutputStream(destination);
			assert writerStream != null;
			
			try {
				while ((numberRead = readerStream.read(buffer)) != -1) {
					writerStream.write(buffer, 0, numberRead);
				}
			} catch (IOException ie) {
				logger.log(Level.SEVERE, "Error reading/writing sequence File from source: {0} to destination: {1}" , 
						new Object[]{source.getAbsolutePath(), destination.getAbsolutePath()});
				throw new RuntimeException("Error reading/writing sequence File " + ie.getLocalizedMessage());
			} finally {
				try {
					readerStream.close();
					writerStream.close();
				} catch (Exception e) {
					logger.log(Level.WARNING, "Exception while closing the Sequence File copy streams");
				}
			}
			logger.log(Level.INFO, "Copied file: {0} to file: {1}", 
					new Object[]{source.getAbsolutePath(), destination.getAbsolutePath()});			
		} else {
			// we have an invalid source file
			logger.log(Level.WARNING, "Invalid source file...cannot copy file: {0}", new Object[]{source.getAbsolutePath()});
		}
	}*/
}
