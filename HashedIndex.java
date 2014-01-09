/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */  


package ir;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
    	PostingsList list = index.get(token);
    	if (list == null) {
    		list = new PostingsList();
    	}
    	list.add(docID,offset);
        index.put(token, list);
		
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        return index.get(token);
    }

    private double idf(String term) {
	int df = index.get(term).size();
	return Math.log(1000/df); //1000 is the number of docs
    }

    public void calculateScores() {
	for (String key : index.keySet()) {
	    PostingsList postingsList = index.get(key);
	    for (int i = 0; i < postingsList.size(); i++) {
		PostingsEntry pe = postingsList.get(i);
		pe.score = pe.offsets.size() * idf(key); //tf-idf
	    }
	}

    }

    private int[] tf (Query query) {
	int[] tfvector = new int[query.terms.size()];
	for (int i = 0; i < query.terms.size(); i++) {
	    int tf = 0;
	    for (String term2 : query.terms) {
		if (query.terms.get(i) == term2) {
		    tf++;
		}
	    }
	    tfvector[i] = tf;
	}
	return tfvector;
    }

    private double[] tfIdf (Query query) {
	int[] tf = tf(query);
	double[] tfIdfVector = new double[query.terms.size()];
	for (int i = 0; i < query.terms.size(); i++) {
	    tfIdfVector[i] = tf[i] * idf(query.terms.get(i));
	}
	return tfIdfVector;
    }

    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType ) {
        ArrayList<PostingsList> postingsLists = new ArrayList<PostingsList>();
        for (String term : query.terms) postingsLists.add(index.get(term));
	if (queryType == INTERSECTION_QUERY) {
	    if (postingsLists.size() > 1) return intersection(postingsLists);
	    return postingsLists.get(0);
	} else if (queryType == PHRASE_QUERY) {
	    if (postingsLists.size() > 1) return phrase(postingsLists, query.terms);
	    return postingsLists.get(0);
	}
	return postingsLists.get(0);
    }

    private PostingsList phrase(ArrayList<PostingsList> postingsLists, LinkedList<String> terms) {
	System.out.println(terms);

        PostingsList result = new PostingsList();
        Iterator<PostingsList> iterator = postingsLists.iterator();
        PostingsList p1 = iterator.next();
        PostingsList p2 = iterator.next();
        p1.sortByDocID();
        p2.sortByDocID();
        Iterator<PostingsEntry> itP1,itP2;
        PostingsEntry pe1,pe2;
	int currentTerm = 0;
	boolean first = true;
        while (iterator.hasNext() || first){
	    if (!first) {
		p2 = iterator.next();
                p2.sortByDocID();
		currentTerm++;
	    }
            itP1 = p1.iterator();
            itP2 = p2.iterator();
            pe1 = itP1.next();
            pe2 = itP2.next();
            while (itP1.hasNext() && itP2.hasNext()){

                if (pe1.docID == pe2.docID){
		    boolean found = false;
		    for (int offset1 : pe1.offsets) {
			for (int offset2 : pe2.offsets) {
			    if (offset2-offset1 > 0 &&
				offset2 - offset1 < terms.get(currentTerm).length()+1){
				result.add(pe2);
				found = true;
				break;
			    }
			}
			if (found) break;
		    }
		    pe1 = itP1.next();
		    pe2 = itP2.next();
                }
                else if (pe1.docID < pe2.docID){
		    pe1 = itP1.next();
		}
                else {
		    pe2 = itP2.next();
		}
            }
	    first = false;
            if (result.size()>0 && iterator.hasNext()) {
                result.sortByDocID();
                p1 = result;
                result = new PostingsList();
                System.out.println("Starting new intersection. Previous intersection size: "+p1.size());

            }
        }
        result.sortByDocID();
        return result;
    }

    private PostingsList intersection(ArrayList<PostingsList> postingsLists) {
        PostingsList result = new PostingsList();
        Iterator<PostingsList> iterator = postingsLists.iterator();
        PostingsList p1 = iterator.next();
        PostingsList p2 = iterator.next();
        p1.sortByDocID();
        p2.sortByDocID();
        Iterator<PostingsEntry> itP1,itP2;
	boolean first = true;
        PostingsEntry pe1,pe2;
        while (iterator.hasNext()||first){
	    if (!first) {
		p2 = iterator.next();
                p2.sortByDocID();	    
	    }
            itP1 = p1.iterator();
            itP2 = p2.iterator();
            pe1 = itP1.next();
            pe2 = itP2.next();
            while (itP1.hasNext() && itP2.hasNext()){
                if (pe1.docID == pe2.docID) {
                    System.out.println("Adding docID "+pe1.docID);

                    result.add(pe1);
                    pe1 = itP1.next();
                    pe2 = itP2.next();
                }
                else if (pe1.docID < pe2.docID) pe1 = itP1.next();
                else pe2 = itP2.next();
            }
	    first = false;
            if (result.size()>0 && iterator.hasNext()) {
                result.sortByDocID();
                p1 = result;
                result = new PostingsList();
                System.out.println("Starting new intersection. Previous intersection size: "+p1.size());

            }
        }
        result.sortByDocID();
        return result;
    }

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
