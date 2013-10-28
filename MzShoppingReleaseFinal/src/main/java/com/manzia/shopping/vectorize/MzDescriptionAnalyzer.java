package com.manzia.shopping.vectorize;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

/**
 *  Class that tokenizes and "filters" the description and features
 *  attributes of a Product object. The included filters strip out
 *  any HTML, stop words, words less than 3 characters in length etc.
 *  This is necessary since we shall be feature-hashing the description
 *  and features attributes as text-like for comparison with the options
 *  selected by the User
 * @author Roy Manzi Tumubweinee, Oct 25, 2012, Manzia Corporation
 *
 */

public final class MzDescriptionAnalyzer extends Analyzer {
	
	//Logger
		public static final Logger logger = 
					Logger.getLogger(MzDescriptionAnalyzer.class.getCanonicalName());

	// Alphabet pattern
			private final Pattern alphabets = Pattern.compile("[a-z]+");

			@Override
			public TokenStream tokenStream(String fieldName, Reader reader) {
				
				// Start by stripping out any HTML markup
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
				result = new LengthFilter(false, result, 3, 25);
				
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
					logger.log(Level.SEVERE, "IOException while incrementing to next Token: {0}", 
							new Object[]{ex.getLocalizedMessage()});
					throw new RuntimeException("IOException while incrementing Token" + ex.getLocalizedMessage());
				}
				
				return new WhitespaceTokenizer(Version.LUCENE_36, new StringReader(stringBuilder.toString()));
			}
}
