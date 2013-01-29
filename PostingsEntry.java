/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;

import com.sun.org.apache.xpath.internal.axes.SelfIteratorNoPredicate;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;
    public int offset;

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
	return Double.compare( other.score, score );
    }
    
    public PostingsEntry(int docID, double score, int offset) {
    	this.docID = docID;
    	this.score = score;
    	this.offset = offset;
	}
    
    public PostingsEntry(int docID, int offset) {
    	this(docID,0.0,offset);
	}
}

    
