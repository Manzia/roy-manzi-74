package com.manzia.shopping.core;

import java.util.Map;

import javax.ejb.Local;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;

import com.manzia.shopping.ranking.RankResults;

/**
 * Local business interface for the MzRankManagerService implementations
 * that generates Rankings for Products given User-specified Product Qualities. <br>
 * 
 * @author Roy Manzi Tumubweinee, Feb 25, 2012, Manzia Corporation
 *
 */


public interface MzRankManagerInterface {
	
	/**
	 * Given a product SKU value and a {@link Map} of Query Parameters
	 * compute and return the relevant Ranking Data. <br>
	 * 
	 * @param queryMap - {@link Map} of Query Parameters
	 * @return
	 * @throws SolrServerException - thrown when query to Solr Server fails
	 */
	public RankResults getRankingDataForSKU( Map<String, String> queryMap ) throws SolrServerException;
	
	/**
	 * Given a product SKU value and a {@link Map} of Query Parameters
	 * compute and return the relevant Ranking Data. <br>
	 * 
	 * @param queryMap - {@link Map} of Query Parameters
	 * @param indexService - {@link MzSolrService}
	 * @return - {@link RankResults}
	 * @throws SolrServerException - thrown when querying the {@link SolrServer} fails
	 */
	public RankResults getRankingDataForSKU( Map<String, String> queryMap, MzSolrService indexService) 
	throws SolrServerException;
	
	/**
	 * Computes the Rank of the Product (given the product SKU) for a specific query <br>
	 * 
	 * @param productSku - {@link String} productSku
	 * @param resultList - {@link SolrDocumentList}
	 * @return - {@link Integer} product Rank
	 * @see MzRankManagerService#computeRankForQuery(String, SolrDocumentList)
	 */
	public Integer computeRankForQuery( String productSku, SolrDocumentList resultList );
}
