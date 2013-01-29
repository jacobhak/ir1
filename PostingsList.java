/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.Serializable;
import ir.PostingsEntry;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {

	/** The postings list as a linked list. */
	private LinkedList<PostingsEntry> list;
	
	public PostingsList() {
		list = new LinkedList<PostingsEntry>();
	}


	/**  Number of postings in this list  */
	public int size() {
		return list.size();
	}

	/**  Returns the ith posting */
	public PostingsEntry get( int i ) {
		return list.get( i );
	}

	public void add(int docID, int offset) {
        for (PostingsEntry pe : list){
            if (pe.docID == docID) return;
        }
		PostingsEntry pe = new PostingsEntry(docID,offset);
		list.add(pe);
	}

    public void add(PostingsEntry pe) {
        list.add(pe);
    }

    public void sortByDocID() {
        Collections.sort(list,new Comparator<PostingsEntry>() {
            @Override
            public int compare(PostingsEntry postingsEntry, PostingsEntry postingsEntry2) {
                if (postingsEntry.docID<postingsEntry2.docID) return -1;
                else if (postingsEntry.docID == postingsEntry2.docID) return 0;
                else return 1;
            }
        });
    }

    public Iterator<PostingsEntry> iterator(){
        return list.iterator();
    }
}