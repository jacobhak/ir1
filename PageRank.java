/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
	if (filename == null) {
	    runTests();
	} else{
	    int noOfDocs = readDocs( filename );
	    computePagerank( noOfDocs );
	}
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
		if ( out[i] == 0 )
		    numberOfSinks++;
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
	double[] xPrim = generateInitialState(numberOfDocs);
	double[] x = generateZeroes(numberOfDocs);
	double[][] probability = buildProbabilityMatrix(numberOfDocs);

	int iterations = 0;
	while(sumOfDiffs(x, xPrim) > EPSILON && iterations < MAX_NUMBER_OF_ITERATIONS) {
	    x = xPrim;
	    xPrim = multiplyVectorByMatrix(xPrim, probability);
	    iterations++;
	    System.out.println("it: " +iterations);
	    System.out.println(x[0] + ", " + x[1] + ", " + x[2] + ", "+ x[3]);

	}
	Map<String, Double> resultMap = buildResultMap(x);
	TreeMap<String, Double>sortedResult = buildMapSortedByValues(resultMap);
	printResultMap(sortedResult);
	Arrays.sort(x);
	printArray(x);

    }

    private void printResultMap(TreeMap<String, Double> map){
	Set<Map.Entry<String,Double>> entries = map.entrySet();
	String key;
	double value;
	int i = 1;
	for (Map.Entry<String, Double> entry : entries) {
	    key = (String) entry.getKey();
	    value = entry.getValue();
	    System.out.println("" + i + ". " + key + " " + value);
	    if (i == 50) break;
	    i++;
	}
    }

    private void printSumOfProbabilities(double[][] matrix) {
	for (int i = 0; i < matrix.length; i++) {
	    double value = 0.0;
	    for (int j = 0; j < matrix[i].length; j++) {
		value += matrix[i][j];
	    }
	    System.out.println(value);
	}
    }

    private TreeMap<String, Double> buildMapSortedByValues(Map<String, Double> map) {
	TreeMap<String, Double> result = new TreeMap<String, Double>(new MapValueComparator(map));
	result.putAll(map);
	return result;
    }

    private HashMap<String, Double> buildResultMap(double[] result) {
	HashMap<String, Double> resultMap = new HashMap<String, Double>();
	for (int i = 0; i < result.length; i++) {
	    resultMap.put(docName[i], result[i]);
	}
	return resultMap;
    }

    private double[] multiplyVectorByMatrix(double[] vector, double[][] matrix) {
	double[] result = new double[vector.length];
	for (int i = 0; i < vector.length; i++) {
	    double value = 0.0;
	    for (int j = 0; j < vector.length; j++) {
		value += vector[j] * matrix[j][i];
	    }
	    result[i] = value;
	}
	return result;
    }

    private void testMatrixMultiplication(){
	double[] v = {0.0, 0.1, 0.3};
	double[][] m = {{1.0, 1.0, 1.0}, {2.0, 2.0, 2.0}, {3.0, 3.0, 3.0}};
	double[] expected = {0.4, 0.8, 1.2};
	double[] result = multiplyVectorByMatrix(v, m);
	if (result.equals(expected)) {
	    System.out.println("JODU");
	} else {
	    System.out.println("FAIL");
	    printArray(result);
	    printArray(expected);
	}

    }

    private void printArray(double[] array) {
	String str = "[";
	for(double v : array){
	    str += v;
	    str += ", ";
	}
	str+= "]";
	System.out.println(str);
    }

    private double[][] buildProbabilityMatrix(int numberOfDocs) {
	double[][] result = new double[numberOfDocs][numberOfDocs];
	for (int i = 0; i < numberOfDocs; i++) {
	    if (link.get(i) == null){
		result[i] = generate1throughNVector(numberOfDocs);
	    }
	    else {
		result[i] = generateOutProbabilityVector(link.get(i), out[i], numberOfDocs);
	    }
	}
	printSumOfProbabilities(result);
	result = multiplyMatrixBy(result, 1-BORED);
	result = addToMatrix(result, BORED/numberOfDocs);
	return result;
    }

    private double[][] multiplyMatrixBy(double[][] matrix, double mult) {
	for (int i = 0; i < matrix.length; i++) {
	    for (int j = 0; j < matrix[i].length; j++) {
		matrix[i][j] = matrix[i][j] * mult;
	    }
	}
	return matrix;
    }

    private double[][] addToMatrix(double[][] matrix, double add) {
	for (int i = 0; i < matrix.length; i++) {
	    for (int j = 0; j < matrix[i].length; j++) {
		matrix[i][j] = matrix[i][j] + add;
	    }
	}
	return matrix;	
    }

    private double[] generateOutProbabilityVector(Hashtable<Integer,Boolean> ai,
						  int nOut, int numberOfDocs) {
	double[] result = new double[numberOfDocs];
	if (nOut == 0) {
	    System.out.println("nOut is zero!");
	}
	double probability = 1.0/nOut;
	for (int i = 0; i < numberOfDocs; i++) {
	    if (ai.get(i)!= null && ai.get(i) == true) {
		result[i] = probability;
		System.out.println("It happened and the prob is: " + probability + " and nOut is: " +nOut);
	    }
	    else result[i] = 0.0;
	}
	return result;
    }

    private double[] generate1throughNVector(int n) {
	double[] result = new double[n];
	double value = 1.0/n;
	for (int i = 0; i < n; i++) {
	    result[i] = value;
	}
	return result;
    }

    private double sumOfDiffs(double[] x, double[] xPrim) {
	double result = 0.0;
	for (int i = 0; i < x.length; i++) {
	    result += Math.abs(x[i] - xPrim[i]);
	}
	return result;
    }

    private double[] generateInitialState(int numberOfDocs) {
	double[] result = new double[numberOfDocs];
	result[0] = 1.0;
	for (int i = 1; i < numberOfDocs; i++) {
	    result[i] = 0.0;
	}
	return result;
    }

    private double[] generateZeroes(int numberOfDocs) {
	double[] result = new double[numberOfDocs];
	for (int i = 0; i < numberOfDocs; i++) {
	    result[i] = 0.0;
	}
	return result;
    }

    class MapValueComparator implements Comparator<String> {

	Map<String, Double> base;
	public MapValueComparator(Map<String, Double> base) {
	    this.base = base;
	}

	public int compare(String a, String b) {
	    if (base.get(a) >= base.get(b)) {
		return -1;
	    } else {
		return 1;
	    } // returning 0 would merge keys
	}
    }
    /* --------------------------------------------- */
    

    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    new PageRank(null); // Runs tests.
	}
	else {
	    new PageRank( args[0] );
	}
    }

    private void runTests() {
	testMatrixMultiplication();
    }
}

