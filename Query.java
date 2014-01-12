/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.HashMap;


public class Query {
    private static final double A = 0.1;
    private static final double B = 0.8;
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    /**
     *  Creates a new empty Query 
     */
    public Query() {
	}
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
		StringTokenizer tok = new StringTokenizer( queryString );
		while ( tok.hasMoreTokens() ) {
			terms.add( tok.nextToken() );
			weights.add( new Double(1) );
		}    
	}
	
    /**
     *  Returns the number of terms
     */
    public int size() {
		return terms.size();
	}
	
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
		Query queryCopy = new Query();
		queryCopy.terms = (LinkedList<String>) terms.clone();
		queryCopy.weights = (LinkedList<Double>) weights.clone();
		return queryCopy;
	}
	
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
	HashMap<String, Double> newQuery = new HashMap<String, Double>();
	HashMap<String, Double> relevantTerms = new HashMap<String, Double>();
	newQuery = addOldQueryWeightsToNewQuery(newQuery, (HashedIndex)indexer.index);
	int i = 0;
	for (boolean isRelevant : docIsRelevant) {
	    if (isRelevant) putRelevantTerms(results.get(i).docID, relevantTerms, (HashedIndex)indexer.index);
	    i++;
	}
	weighRelevantTerms(relevantTerms);
	mergeNewQueryRelevantTerms(newQuery, relevantTerms);
	addTermsAndWeightsToSelf(newQuery);
    }

    private void addTermsAndWeightsToSelf(HashMap<String, Double> newQuery){
	LinkedList<String> newTerms = new LinkedList<String>();
	LinkedList<Double> newWeights = new LinkedList<Double>();
	for (String term : newQuery.keySet()) {
	    newTerms.add(term);
	    newWeights.add(newQuery.get(term));
	}
	terms = newTerms;
	weights = newWeights;
    }

    private void mergeNewQueryRelevantTerms(HashMap<String, Double> newQuery,
					    HashMap<String, Double> relevantTerms) {
	for (String term : relevantTerms.keySet()) {
	    if (newQuery.get(term) != null) {
		newQuery.put(term, newQuery.get(term) + relevantTerms.get(term));
	    } else {
		newQuery.put(term, relevantTerms.get(term));
	    }
	}

    }

    private void weighRelevantTerms(HashMap<String, Double> relevantTerms) {
	double inverseNTerms = 1.0/relevantTerms.keySet().size();
	for (String term : relevantTerms.keySet()) {
	    relevantTerms.put(term, relevantTerms.get(term) * B * inverseNTerms);
	}

    }

    private void putRelevantTerms(int docID, HashMap<String, Double> relevantTerms, HashedIndex hashIndex) {
	HashMap<String, Integer> document = hashIndex.docIndex.get(docID);
	double[] tfIdfVector = hashIndex.tfIdfVector(document);
	int i = 0;
	for (String term : document.keySet()) {
	    if (relevantTerms.get(term) != null) {
		relevantTerms.put(term, relevantTerms.get(term) + tfIdfVector[i]);
	    } else {
		relevantTerms.put(term, tfIdfVector[i]);
	    }
	    i++;
	}

    }

    private HashMap<String, Double> addOldQueryWeightsToNewQuery(HashMap<String, Double> newQuery, HashedIndex hashIndex) {
	double[] tfIdfVector = hashIndex.tfIdf(this);
	double inverseNTerms = 1.0/terms.size();
	for (int i = 0; i < tfIdfVector.length; i++) {
	    newQuery.put(terms.get(i),A *tfIdfVector[i] * inverseNTerms);
	}
	return newQuery;
    }
}

    
