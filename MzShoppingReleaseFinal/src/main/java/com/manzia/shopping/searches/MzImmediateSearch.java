package com.manzia.shopping.searches;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier for the MzSearchService bean that will immediately
 * hit the Retailer APIs over the networks to return products
 * that match the query parameters of the specified search.
 * 
 * @author Roy Manzi Tumubweinee, Oct 4, 2012, Manzia Corp
 *
 */

@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface MzImmediateSearch {

}
