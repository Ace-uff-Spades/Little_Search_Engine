package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		
		Scanner sc = new Scanner(new File(docFile));
		HashMap<String,Occurrence> kw = new HashMap<String, Occurrence>();
		
		while(sc.hasNext()){
			String nextW = sc.next();
			if(getKeyWord(nextW)!=null && getKeyWord(nextW)!=""){
				nextW = getKeyWord(nextW);
				if(noiseWords.containsKey(nextW) == false){
					if(kw.containsKey(nextW)){
						kw.get(nextW).frequency++; 
					}
					else {
						kw.put(nextW, new Occurrence(docFile,1));
					}
				}
			}
		}
		return kw;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		if(kws.size()==0){
			return;
		}
		
		for(String key : kws.keySet()){
			if(!keywordsIndex.containsKey(key)){
				keywordsIndex.put(key, new ArrayList<Occurrence>());
				keywordsIndex.get(key).add(new Occurrence(kws.get(key).document, kws.get(key).frequency));
			}
			else{
				
				keywordsIndex.get(key).add(new Occurrence(kws.get(key).document,kws.get(key).frequency));
				insertLastOccurrence(keywordsIndex.get(key));
			}
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		String temp = word; 
		temp = temp.toLowerCase();
		int EndingIndex = 0; 
		int StartingIndex =0;
		int i;
		String punc = ".,?:;!-'(){}[]";
		
		if(word.length()==0){
			return "";
		}
		//STARTING INDEX OF THE WORD
		for(i = 0;i<temp.length(); i++){
			String c = temp.substring(i, i+1);
			if(!punc.contains(c)){
				StartingIndex = i;
				break;
			}
			else if(punc.contains(c)){
				
			}
		}
	
		//LAST INDEX OF A WORD
		for(i = 0;i<temp.length(); i++){
			String c1 = temp.substring(i, i+1);
			if(!punc.contains(c1)){
				EndingIndex = i;
			}
			else if(punc.contains(c1)){
				
			}
		}
		//CHECK IF THE WORD HAS ANY PUNCTUATION IN BETWEEN
			for(i=StartingIndex; i<=EndingIndex; i++){
				String c2 = temp.substring(i, i+1);
				if(punc.contains(c2)){
					return null; 
				}
			}
			
		return temp.substring(StartingIndex, EndingIndex+1);

	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		if(occs.size() == 0){
			return null; 
		}
		ArrayList<Integer> midP = new ArrayList<Integer>();
		ArrayList<Integer> freq = new ArrayList<Integer>();
		for(Occurrence key : occs){
			freq.add(key.frequency);
		}
		if(freq.size() == 1){
			return null;
		}
		
		int Last = freq.get(freq.size()-1);
		Occurrence Last1 = occs.get(occs.size()-1);
		freq.remove(freq.size()-1);
		occs.remove(occs.size()-1);
		//BINARY SEARCH IN RESULTING ARRAY 
		int mid =0;
		int lo = 0;
        int hi = freq.size()- 1;
        while (lo <= hi) {
            mid = lo + (hi - lo) / 2;
            midP.add(mid);
            if(Last < freq.get(mid)){
            	lo = mid + 1;
            	if(lo>hi){
            		freq.add(mid+1, Last);
            		occs.add(mid+1, Last1);
            	}
            }
            else if (Last > freq.get(mid)) {
            	hi = mid - 1;
            	if(lo>hi){
            		freq.add(mid,Last);
            		occs.add(mid, Last1);
            	}
            }
            else {
            	freq.add(mid,Last);
            	occs.add(mid, Last1);
            	break;
            }
        }
        
        for(String key : keywordsIndex.keySet()){
        	if(key.equals(freq)){
        		keywordsIndex.put(key, occs);
        	}
        }
    
		return midP;
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		
		ArrayList<String> topF = new ArrayList<String>();
		if(kw1.equals("") && kw2.equals("")){
			return null;
		}
		if(!keywordsIndex.containsKey(kw1) && !keywordsIndex.containsKey(kw2)){
			return null;
		}
		if(keywordsIndex.containsKey(kw1) || keywordsIndex.containsKey(kw2)){
			ArrayList<Occurrence> comb = new ArrayList<Occurrence>();
			if(keywordsIndex.containsKey(kw1)){
				comb = keywordsIndex.get(kw1);
			}
			if(keywordsIndex.containsKey(kw2)){
				for(Occurrence key : keywordsIndex.get(kw2)){
					comb.add(key);
				}
			}
			
		
			for(int r =0; r<comb.size(); r++){
				Occurrence temp = comb.get(r);
				
				for(int c = r+1; c<comb.size();c++){
					if(temp.document.equals(comb.get(c).document)){								
						if(temp.frequency>comb.get(c).frequency){								
							comb.remove(c);
						}
						else if(temp.frequency<comb.get(c).frequency){								
							comb.remove(r);
							r = r-1;
						}
						else if(temp.frequency==comb.get(c).frequency){							
							comb.remove(c);
						}
					}
				}
			}
			
			
			comb = sort(comb);
			
			System.out.println(comb);
			if(comb.size()>5){
				for(int a = 0; a<5; a++){
					topF.add(comb.get(a).document);
				}
			}
			else{
				for(int y =0; y<comb.size(); y++){
					topF.add(comb.get(y).document);
				}
			}
		}
		
		return topF;
	}
	
	public ArrayList<Occurrence> sort(ArrayList<Occurrence> unsorted){
		
		ArrayList<Occurrence> freq = new ArrayList<Occurrence>();
		for(Occurrence key : unsorted){
			freq.add(key);
		}
		
		Occurrence temp;
        for (int i = 0; i < freq.size(); i++) {
            for(int j = i ; j > 0 ; j--){
                if(freq.get(j).frequency > freq.get(j-1).frequency){
                    temp = freq.get(j-1);
                    freq.set(j-1, freq.get(j));
                    freq.set(j, temp);
                }
            }
        }
		return freq;
	}
	public void display(HashMap<String, ArrayList<Occurrence>> disp){
		for(String key : disp.keySet()){
			String k = key; 
			
			System.out.println(key + "  " + disp.get(key));
		}
	}
}



