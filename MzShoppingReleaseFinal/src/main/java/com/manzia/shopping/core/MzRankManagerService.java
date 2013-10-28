package com.manzia.shopping.core;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.manzia.shopping.ranking.RankResultType;
import com.manzia.shopping.ranking.RankResults;
import com.manzia.shopping.searches.MzQueryTranslator;
import com.manzia.shopping.searches.MzSearchQuery;
import com.manzia.shopping.searches.MzSimpleQueryValidator;

/**
 * Session Bean implementation class MzRankManagerService
 */
@Stateless
@Local(MzRankManagerInterface.class)
//@LocalBean
@MzRankCoreService
public class MzRankManagerService implements MzRankManagerInterface {
	
	// SolrServer instance
	@Inject @MzSolrServerOne
	private MzSolrService solrService;
	
	// Sku Key value
	private static final String skuKey = "sku";	
	
	//Logger
	public static final Logger logger = 
			Logger.getLogger(MzRankManagerService.class.getCanonicalName());

    /**
     * Default constructor. 
     */
    public MzRankManagerService() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Method is a wrapper around the {@link MzRankManagerService#getRankingDataForSKU(Map, MzSolrService)}
     * where we use the default {@link MzSolrService} <br>
     * @param queryMap - {@link Map} of Query Parameters
     */
	@Override
	public RankResults getRankingDataForSKU(Map<String, String> queryMap) throws SolrServerException {
		
		return getRankingDataForSKU(queryMap, solrService);		
	}
	
	/**
     * Main method that returns the {@link RankResults} given a {@link Map} of Query Parameters
     * as follows: <br>
     * 1- get the associated product SKU value from the {@link Map}<br>
     * 2- generate a {@link SolrQuery} for each sub-query in the input {@link Map} <br>
     * 3- Get the Top-50 sorted results {@link SolrDocumentList} for each sub-query from the {@link SolrServer} <br>
     * 4- the rank of the product with SKU in step 1 along each sub-query is its position in the sorted results. <br>
     * If the product is not found we assign a rank of >50 <br>
     * 5- Create and return a {@link RankResults} object <br>
     * 
     * @param queryMap - {@link Map} of Query Parameters
     * @param indexService - {@link MzSolrService} active Solr Server that will be queried
     * @throws SolrServerException  - thrown when the Query to the {@link SolrServer} fails
     */
	@Override
	public RankResults getRankingDataForSKU(Map<String, String> queryMap,
			MzSolrService indexService) throws SolrServerException {
		
		// Output
		RankResults rankResults = new com.manzia.shopping.ranking.ObjectFactory().createRankResults();

		// Check Inputs
		if (queryMap == null || queryMap.isEmpty()) {
			logger.log(Level.WARNING, "Null or Empty Map of Query Parameters specified..cannot generate RankResults");
			return rankResults;
		}

		// Check Parameters in the Map
		MzSimpleQueryValidator simpleValidator = new MzSimpleQueryValidator();
		boolean validMap = simpleValidator.containsValidQueries(queryMap);

		if (validMap) {

			// Generate Solr Queries
			MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
			assert searchQuery != null;
			MzQueryTranslator queryTranslator = new MzQueryTranslator();
			List<SolrQuery> queryList = queryTranslator.translateQueryList(searchQuery);
			assert queryList != null;

			// Iterate over the list of SolrQuery
			if (!queryList.isEmpty()) {
				SolrServer solrServer = indexService.getSolrServer();
				assert solrServer != null;
				SolrDocumentList docList;
				String productSku = queryMap.get(skuKey);

				if (productSku != null && !productSku.isEmpty()) {
					for (SolrQuery query : queryList) {

						// compute the Rank and populate the RankResults object
						if (query.getQuery() != null && !query.getQuery().isEmpty()) {
							System.out.println("Sub-Query submitted to Server: " + query.getQuery());
							QueryResponse queryResponse = solrServer.query(query);
							assert queryResponse != null;
							docList = queryResponse.getResults();
							assert docList != null;
							Integer productRank = computeRankForQuery(productSku, docList);
							RankResultType rankResultType = 
									new com.manzia.shopping.ranking.ObjectFactory().createRankResultType();
							rankResultType.setRankQuality(simpleValidator.extractQueryTerms(query.getQuery()));

							// Note that we add 1 to the computed Rank such that it will always range from 0
							// where a product Rank of 0 is interpreted as not found or greater than 25 (because
							// each SolrDocumentList has 25 SolrDocuments). We assume that if a Product's Rank > 25
							// then an end-user is not likely to care that much for what exactly the rank was.
							rankResultType.setRankRating(new BigDecimal(productRank.intValue()+1));
							rankResults.getRankResult().add(rankResultType);			        	       	
						}
					}
					// log
					logger.log(Level.INFO, "Computed: [{0}] rankings for Product SKU: {1}", 
							new Object[]{ rankResults.getRankResult().size(), productSku}); 
				} else {
					// No product SKU in query params
					logger.log(Level.WARNING, "Provided Map of Query Parameters does not have a product SKU Key/Value");
					return rankResults;
				}				
			}			

		} else {
			logger.log(Level.WARNING, "No Queries found in Map of Query Parameters specified..cannot generate RankResults");
			return rankResults;
		}
		return rankResults;
	}
	
	/**
	 * Method iterates through the {@link SolrDocumentList} and returns the position of the
	 * first {@link SolrDocument} whose "sku" value matches the input {@link String} productSku.<br>
	 * If no match is found, returns -1.
	 * <p> Note that we add 1 to the computed Rank such that it will always range from 0
	 * where a product Rank of 0 is interpreted as not found or greater than 25 (because
	 * each SolrDocumentList has 25 SolrDocuments). We assume that if a Product's Rank > 25
	 * then an end-user is not likely to care that much for what exactly the rank was. </p>
	 * 
	 * @param productSku - {@link String} productSku value
	 * @param resultList - {@link SolrDocumentList} query results
	 * @return - {@link Integer} rank value
	 */
	@Override
	public Integer computeRankForQuery( String productSku, SolrDocumentList resultList ) {
		
		// check Inputs
		Integer productRank = Integer.valueOf(-1);
		if (productSku == null || productSku.isEmpty()) {
			logger.log(Level.WARNING, "Null or Empty productSKU String specified..cannot compute Rank");
			return productRank;
		}
		if (resultList == null || resultList.isEmpty()) {
			logger.log(Level.WARNING, "Null or Empty SolrDocumentList specified..cannot compute Rank");
			return productRank;
		}
		boolean success = false;
				
		// NOTE: In order to get an "accurate" ranking, the SolrDocumentList must be sorted in ascending order
		// and sorted on relevance score
		//int count = -1;
		for (SolrDocument doc : resultList) {
			//count++;
			success = doc.getFieldValue(skuKey).equals(productSku) ? true : false;
			if (success) {
				productRank = Integer.valueOf(resultList.indexOf(doc));
				break;
			}
			
		}
		return productRank;		
	}	

}
