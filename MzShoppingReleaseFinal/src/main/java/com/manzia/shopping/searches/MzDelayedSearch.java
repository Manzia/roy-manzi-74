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
 * Qualifier for the MzSearchService bean that persists specified
 * search objects as MzSearchDetail entity object and continuously
 * monitors each search objects duration property. After a set interval
 * the bean calls the ImmediateSearch bean to retrieve a new set of
 * products with "slightly modified" query parameters
 * 
 * @author Roy Manzi Tumubweinee, Oct 4, 2012, Manzia Corporation
 *
 */

@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface MzDelayedSearch {

}
