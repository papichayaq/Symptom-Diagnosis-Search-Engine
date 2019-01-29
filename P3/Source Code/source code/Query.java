/*
 * Jidapa		Sumanotham	 5888043	Section 1
 * Papichaya 	Quengdaeng	 5888146	Section 1
 * Intukorn		Limpachaveng 5888261	Section 1
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Query {
		// Term id -> (position in index file, frequency) dictionary
		private static Map<Integer, Pair<Long, Integer>> termPostingDict 
			= new TreeMap<Integer, Pair<Long, Integer>>();
		// Symptom id -> (position in index file, frequency) dictionary
		private static Map<Integer, Pair<Long, Integer>> sympPostingDict 
			= new TreeMap<Integer, Pair<Long, Integer>>();
		// Term id -> (position in index file, frequency) dictionary
		private static Map<Integer, Pair<Long, Integer>> diagPostingDict 
			= new TreeMap<Integer, Pair<Long, Integer>>();		
		// Symptom title -> symptom id dictionary
		private static Map<Integer, String> sympDict
			= new TreeMap<Integer, String>();
		// Term -> term id dictionary
		private static Map<String, Integer> termDict
			= new TreeMap<String, Integer>();
		// Diagnosis -> diagnosis id dictionary
		private static Map<Integer, String> diagDict
			= new TreeMap<Integer, String>();
		
		private static BaseIndex index = new BasicIndex();
		
		
		private  PostingList readPosting(FileChannel fc, int termId, Map<Integer, Pair<Long, Integer>> postingDict)
				throws IOException {
			//Read posting at the position as found in a particular dictionary
			long pos = postingDict.get(termId).getFirst();
			PostingList posting = index.readPosting(fc.position(pos));
			return posting;
		}
		
		private void loadDictionary() throws IOException{
			String line;
			/* Term dictionary */
			BufferedReader termReader = new BufferedReader(new FileReader(new File("term.dict")));
			while ((line = termReader.readLine()) != null) {
				String[] tokens = line.split("\t");
				termDict.put(tokens[0], Integer.parseInt(tokens[1]));
			}
			termReader.close();
			
			/* Symptom dictionary */
			BufferedReader sympReader = new BufferedReader(new FileReader(new File("symptom.dict")));
			while ((line = sympReader.readLine()) != null) {
				String[] tokens = line.split("\t");
				sympDict.put(Integer.parseInt(tokens[1]), tokens[0]);
			}
			sympReader.close();
			
			/* Diagnosis dictionary */
			BufferedReader diagReader = new BufferedReader(new FileReader(new File("diagnosis.dict")));
			while ((line = diagReader.readLine()) != null) {
				String[] tokens = line.split("\t");
				diagDict.put(Integer.parseInt(tokens[1]), tokens[0]);
			}
			diagReader.close();
			/* Term-Symptom Posting dictionary */
			BufferedReader termPostReader = new BufferedReader(new FileReader(new File("termPosting.dict")));
			while ((line = termPostReader.readLine()) != null) {
				String[] tokens = line.split("\t");
				termPostingDict.put(Integer.parseInt(tokens[0]), 
						new Pair<Long, Integer>(Long.parseLong(tokens[1]), Integer.parseInt(tokens[2])));
			}
			termPostReader.close();
			/* Symptom-Diagnosis Posting dictionary */
			BufferedReader sympPostReader = new BufferedReader(new FileReader(new File("sympPosting.dict")));
			while ((line = sympPostReader.readLine()) != null) {
				String[] tokens = line.split("\t");
				sympPostingDict.put(Integer.parseInt(tokens[0]), 
						new Pair<Long, Integer>(Long.parseLong(tokens[1]), Integer.parseInt(tokens[2])));
			}
			sympPostReader.close();
			/* Diagnosis-Symptom Posting dictionary */
			BufferedReader diagPostReader = new BufferedReader(new FileReader(new File("diagPosting.dict")));
			while ((line = diagPostReader.readLine()) != null) {
				String[] tokens = line.split("\t");
				diagPostingDict.put(Integer.parseInt(tokens[0]), 
						new Pair<Long, Integer>(Long.parseLong(tokens[1]), Integer.parseInt(tokens[2])));
			}
			diagPostReader.close();
		}
		/*
		 * retrieveSymptom turns a query symptom into terms and returns a list of symptoms that contains all terms. 
		 */
		private List<Integer> retrieveSymptom(String query) throws IOException{
			//split symptoms into tokens
			String[] tokens = query.trim().split("\\s+");
			//Set index file
			File file = new File("termCorpus.index");
			RandomAccessFile indexFile = new RandomAccessFile(file, "r");;
			List<Integer> list = new LinkedList<Integer>();
			PostingList posting;
			int i = 0;
			for(String token: tokens){
				if(termDict.containsKey(token)){
					int termId = termDict.get(token);
					//read posting of each term
					posting = readPosting(indexFile.getChannel(), termId, termPostingDict);
					if(i==0){
						for(int j: posting.getList()){
							list.add(j);
						}
					}
					else{
						//Intersect symptom lists of all terms
						for(Iterator<Integer> iterator = list.iterator(); iterator.hasNext();){
							int j = iterator.next();
							if(!posting.getList().contains(j)){
								iterator.remove();
							}
						}
					}
				}
				i = 1;
			}
			indexFile.close();
			Collections.sort(list);
			return list;
		}
		/*
		 * retrieveDiagnosis uses symptom ID to find posting on sympCorpus.index and returns a list of diagnosis that 
		 * contains the symptom
		 */
		private List<Integer> retrieveDiagnosis(Integer sid) throws IOException{
			//Set index file
			File file = new File("sympCorpus.index");
			RandomAccessFile indexFile = new RandomAccessFile(file, "r");;
			List<Integer> list = new LinkedList<Integer>();
			PostingList posting;
			//Read posting
			posting = readPosting(indexFile.getChannel(), sid, sympPostingDict);
			for(int j: posting.getList()){
				list.add(j);
			}
			
			indexFile.close();
			Collections.sort(list);
			return list;
		}
		/*
		 * retrieveSymptom uses diagnosis ID to find posting on diagCorpus.index and returns a list of symptoms that 
		 * the diagnosis contains
		 */
		private List<Integer> retrieveSymptom(Integer did) throws IOException{
			//Read posting from index
			File file = new File("diagCorpus.index");
			RandomAccessFile indexFile = new RandomAccessFile(file, "r");;
			List<Integer> list = new LinkedList<Integer>();
			PostingList posting;
			posting = readPosting(indexFile.getChannel(), did, diagPostingDict);
			for(int j: posting.getList()){
				list.add(j);
			}
			indexFile.close();
			Collections.sort(list);
			return list;
		}
		/*
		 * 
		 */
		public int runQueryService(String query) throws IOException{
			//Load all dictionaries
			loadDictionary();
			//split input into symptoms
			String[] symptoms = query.trim().split(",");
			List<List<Integer>> list = new LinkedList<List<Integer>>();
			for(String symptom: symptoms){
				List<Integer> symp = retrieveSymptom(symptom);
				if(symp.isEmpty()){
					System.out.println("No symptom matches your keyword \"" + symptom + "\". Please check your spelling or change your words.");
					return 0;
				}
				else{
					int n = 0;
					List<Integer> diagnosis = new LinkedList<Integer>();
					for(int i: symp){
						if(sympDict.containsKey(i)){
							List<Integer> diag = retrieveDiagnosis(i);
							if(n == 0){
								diagnosis = diag;
							}
							else{
								//Union lists of diagnosis of all possible symptom of an input symptom
								diagnosis.removeAll(diag);
								diagnosis.addAll(diag);
							}
							n++;
						}
					}
					list.add(diagnosis);
				}
				
			}
			List<Integer> finalDiag = list.get(0);
			//Intersect lists of all diagnosis of all symptoms
			for(int i=1; i<list.size(); i++){
				finalDiag.retainAll(list.get(i));
			}
			if(finalDiag.isEmpty()){
				System.out.println("No diagnosis matches all your symptoms.");
				return 0;
			}
			System.out.println("You might have one of these " + finalDiag.size() + " problems:");
			System.out.println();
			for(int i: finalDiag){
				List<Integer> symptom = retrieveSymptom(i);
				System.out.println(diagDict.get(i));
				System.out.print("The symptoms include: ");
				for(int j: symptom){
					if(sympDict.get(j) != null){
						System.out.print(sympDict.get(j) + ", ");
					}
				}
				System.out.println();
				System.out.println();
			}
			
			
			return 0;
		}
}
