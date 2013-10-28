package com.manzia.shopping.vectorize;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier for the MzRankingService implementation that
 * utilizes a Cosine Distance metric for Vector Similarity
 * 
 * @author Roy Manzi Tumubweinee, Oct 4, 2012, Manzia Corporation
 *
 */

@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface MzCosineDistance {

}
