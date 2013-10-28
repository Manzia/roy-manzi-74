package com.manzia.shopping.reviews;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharReader;
import org.apache.lucene.analysis.CharStream;
import org.apache.lucene.analysis.LengthFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.vectorizer.DictionaryVectorizer;
import org.apache.mahout.vectorizer.DocumentProcessor;

/**
 * Creates Term-Frequency (TF) vectors from a SequenceFile of
 * concatenated reviews. The SequenceFile is of format: Text Key, Text Value
 * where the Key is category::productSKU and Value is all reviews associated
 * with that productSKU. The output SequenceFile of TF vectors is the input
 * to the CVB-LDA algorithm.
 * 
 * @author Roy Manzi Tumubweinee, Jan 16, 2013, Manzia Corporation
 *
 */
public class MzReviewsVectorizer {
	
	private static final String fileSystemURI = "hdfs://localhost:9000/user/admin";
	
	//Logger
	public static final Logger logger = 
					Logger.getLogger(MzReviewsVectorizer.class.getCanonicalName());

	/**
	 * @param args - takes 2 directory paths /inputDir /outputDir. Note that
	 * these are directory paths in the HDFS filesystem not local filesystem
	 */
	public static void main(String[] args) {
		
		// Check inputs
		if (args.length != 1) {
			System.err.println("Usage: MzReviewsVectorizer <inputPathDir>");			
			System.exit(-1);
		}
		
		// Minimum frequency of the term in the collection to be considered part
		// of the dictionary file. Terms with lesser frequency are ignored.
		int minSupport = 2;	

		// Minimum document frequency - minimum number of documents term should
		// appear before inclusion in dictionary file. Terms with lesser frequency
		// are ignored
		int minDf = 3;

		// Max document frequency percentage - max number of documents term should
		// appear before inclusion in dictionary file. Terms with higher frequency
		// are ignored
		long maxDFPercent = 90;

		// Max size of n-grams to be selected from the collection
		int maxNGramSize = 2;

		//If n-gram size > 1, significant n-grams have large scores (i.e 1000) while
		// less significant have lower scores
		int minLLRValue = 50;

		// Number of reducer tasks to execute in parallel. Should be set to the max
		// number of nodes in the hadoop cluster
		int reduceTasks = 1;

		// Perform vectorization in multiple stages/chunks for large collections
		// unit in MB
		int chunkSize = 200;

		//Normalization to use in the Lp space. Uses a p-norm.
		float norm = 2.0f;

		//Generates SequentialAccessSparseVectors
		boolean sequentialAccessOutput = true;
		
		// Output Directory
		String outputDir = "reviews-seqFiles";
		
		// Create the TF vectors
		// Use default key/value configuration parameters based on resource HDFS config
		Configuration conf = new Configuration();

		try {
			// Get an HDFS instance
			URI inputURI = new URI(fileSystemURI);
			FileSystem fs = FileSystem.get(inputURI, conf);
			logger.log(Level.INFO, "Created new HDFS fileSystem instance!");

			// Get the input files from the Hadoop filesystem
			Path hdfsInput = new Path(args[0]);
			Path inputPathDir = fs.getFileStatus(hdfsInput).getPath();
			assert inputPathDir != null;
			
			// Check we have valid input/output directories
			if (!fs.getFileStatus(inputPathDir).isDir()) {
				logger.log(Level.SEVERE, "Invalid input directory specified: {0}", new Object[]{inputPathDir.getName()});
				System.err.format("Invalid input directory specified %s%n", inputPathDir.getName());			
				System.exit(-1);
			}
			
			/*Path outputPath = new Path(args[1]);
			assert outputPath != null;
			if (!fs.getFileStatus(outputPath).isDir()) {
				logger.log(Level.SEVERE, "Invalid Output directory specified: {0}", new Object[]{outputPath.getName()});
				System.err.format("Invalid Output directory specified %s%n", outputPath.getName());			
				System.exit(-1);
			} */
			logger.log(Level.INFO, "Input and Output Directories are valid...will process!");

			// delete the outputDir if it exists
			HadoopUtil.delete(conf, new Path(outputDir));
			Path tokenizedPath = new Path( outputDir,
					DocumentProcessor.TOKENIZED_DOCUMENT_OUTPUT_FOLDER);

			// Use our custom Lucene Analyzer
			MzDocumentAnalyzer analyzer = new MzDocumentAnalyzer();
			DocumentProcessor.tokenizeDocuments(inputPathDir, 
					analyzer.getClass().asSubclass(Analyzer.class), tokenizedPath, conf);

			// Create TF vectors
			DictionaryVectorizer.createTermFrequencyVectors(tokenizedPath, new Path(outputDir), 
					DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER, conf, minSupport, maxNGramSize, 
					minLLRValue, norm, true, reduceTasks, chunkSize, sequentialAccessOutput, false);
			
		} catch (IOException ie) {
			System.err.format("IOException %s%n", ie);

		} catch (InterruptedException ex) {
			System.err.format("InterruptedException %s%n", ex);

		} catch (ClassNotFoundException ec) {
			System.err.format("ClassNotFoundException %s%n", ec);

		} catch (URISyntaxException e) {
			System.err.format("URISyntaxException %s%n", e);
			e.printStackTrace();
		}

	}
	
public static final class MzDocumentAnalyzer extends Analyzer {
		
		// Alphabet pattern
		private final Pattern alphabets = Pattern.compile("[a-z]+");

		@Override
		public TokenStream tokenStream(String fieldName, Reader reader) {
			
			// Start by stripping out the HTML markup
			CharStream charStream = CharReader.get(reader);
			reader = new HTMLStripCharFilter(charStream);
			
			TokenStream result = new StandardTokenizer(Version.LUCENE_36, reader);
			
			// Use Lucene Filters to improve document vector generation
			// and remove as much noise as possible...the order of filter
			// chaining is important
			result = new LowerCaseFilter(Version.LUCENE_36, result);
			result = new StopFilter(Version.LUCENE_36, result, 
					StandardAnalyzer.STOP_WORDS_SET);
			result = new StandardFilter(Version.LUCENE_36, result);
			
			// Removed PorterStemmer from the chain as it seems to be too
			// aggressive
			//result = new PorterStemFilter(result);
			result = new LengthFilter(false, result, 3, 50);
			
			CharTermAttribute termAttribute = 
					(CharTermAttribute)result.addAttribute(CharTermAttribute.class);
			StringBuilder stringBuilder = new StringBuilder();
			
			try {
				while (result.incrementToken()) {
					
					// Filter out small tokens
					if (termAttribute.length() < 3) continue;
					String word = new String(termAttribute.buffer(), 0, termAttribute.length());
					Matcher match = alphabets.matcher(word);
					
					//Filter non-alphabetic tokens
					if (match.matches()) {
						stringBuilder.append(word).append(" ");
					}
				}
			} catch (IOException ex) {
				System.err.format("IOException: %s%n", ex);
			}
			
			return new WhitespaceTokenizer(Version.LUCENE_36, new StringReader(stringBuilder.toString()));
		}
		
	}	

}
