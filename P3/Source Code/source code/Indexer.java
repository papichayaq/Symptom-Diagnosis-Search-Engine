/*
 * Jidapa		Sumanotham	 5888043	Section 1
 * Papichaya 	Quengdaeng	 5888146	Section 1
 * Intukorn		Limpachaveng 5888261	Section 1
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.TreeMap;

public class Indexer {
	
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
		private static Map<String, Integer> sympDict
			= new TreeMap<String, Integer>();
		// Term -> term id dictionary
		private static Map<String, Integer> termDict
			= new TreeMap<String, Integer>();
		// Diagnosis -> diagnosis id dictionary
		private static Map<String, Integer> diagDict
			= new TreeMap<String, Integer>();
		
		private static int termIdCounter = 0;
		private static BaseIndex index = new BasicIndex();
		private static long position;
		
		private static void writePosting(FileChannel fc, PostingList posting, Map<Integer, Pair<Long, Integer>> dict)
				throws IOException {
			/*
			 * TODO: Your code here
			 *	 
			 */
			int termId, freq;
			termId = posting.getTermId();
			freq = posting.getList().size();
			index.writePosting(fc, posting);
			Pair<Long, Integer> p = new Pair<Long, Integer>(position, freq);
			dict.put(termId, p);
			position += 8+4*freq;
		}
		
		public static int runIndexer() throws IOException{
			/*
			 * 
			 * 
			 * Create term-symptom index, sympDict, termDict, and termPostingDict
			 * 
			 * 
			*/
			BufferedReader sympReader = new BufferedReader(new FileReader(new File("sym_t.csv")));
			String line;
			int sid, tid;
			String title;
			sympReader.readLine();
			Map<Integer, PostingList> termPostings = new TreeMap<Integer, PostingList>();
			
			while((line = sympReader.readLine()) != null){
				String[] temp = line.trim().split("\",\"");
				//Split line into sid and title
				sid = Integer.parseInt(temp[0].replace("\"", ""));
				title = temp[1].replace("\"", "");
				if(!title.equals("")){
					//Put title and sid into sympDict
					sympDict.put(title, sid);
					//Split title into terms
					String[] tokens = title.trim().split("\\s+");
					for(String token: tokens){
						token = token.replace("(", "");
						token = token.replace(")", "");
						token = token.toLowerCase();
						//Put term and tid into termDict
						if(!termDict.containsKey(token)){
							termIdCounter++;
							termDict.put(token, termIdCounter);
						}
						tid = termDict.get(token);
						if(termPostings.containsKey(tid)){
							if(!termPostings.get(tid).getList().contains(sid)){
								termPostings.get(tid).getList().add(sid);
							}
						}
						else{
							PostingList post = new PostingList(tid);
							post.getList().add(sid);
							termPostings.put(tid, post);
						}
					}
				}
			}
			sympReader.close();
			
			File file = new File("termCorpus.index");
			RandomAccessFile bfc = new RandomAccessFile(file, "rw");
			//Write index to file
			FileChannel fc = bfc.getChannel();
			position = 0;
			for(int i: termPostings.keySet()){
				writePosting(fc, termPostings.get(i), termPostingDict);
			}
			bfc.close();
			//Write termDict, sympDict, and termPostingDict to file
			BufferedWriter termWriter = new BufferedWriter(new FileWriter(new File("term.dict")));
			for (String term : termDict.keySet()) {
				termWriter.write(term + "\t" + termDict.get(term) + "\n");
			}
			termWriter.close();
			
			BufferedWriter sympWriter = new BufferedWriter(new FileWriter(new File("symptom.dict")));
			for (String symptom : sympDict.keySet()) {
				sympWriter.write(symptom + "\t" + sympDict.get(symptom) + "\n");
			}
			sympWriter.close();
			
			BufferedWriter postWriter = new BufferedWriter(new FileWriter(new File("termPosting.dict")));
			for (Integer termId : termPostingDict.keySet()) {
				postWriter.write(termId + "\t" + termPostingDict.get(termId).getFirst()
						+ "\t" + termPostingDict.get(termId).getSecond() + "\n");
			}
			postWriter.close();
			
			/*
			 * 
			 * 
			 * Create diag_dict
			 * 
			 * 
			*/
			BufferedReader diagReader = new BufferedReader(new FileReader(new File("dia_t.csv")));
			int did;
			diagReader.readLine();
			while((line = diagReader.readLine()) != null){
				String[] temp = line.trim().split("\",\"");
				//Split line into did and title
				did = Integer.parseInt(temp[0].replace("\"", ""));
				title = temp[1].replace("\"", "");
				diagDict.put(title, did);
			}
			diagReader.close();
			//Write DiagDict to file
			BufferedWriter diagWriter = new BufferedWriter(new FileWriter(new File("diagnosis.dict")));
			for (String diagnosis : diagDict.keySet()) {
				diagWriter.write(diagnosis + "\t" + diagDict.get(diagnosis) + "\n");
			}
			diagWriter.close();
			
			/*
			 * 
			 * 
			 * Create symptom-diagnosis index, diagnosis-symptom index, sympPostingDict, and diagPostingDict
			 * 
			 * 
			*/
			BufferedReader reader = new BufferedReader(new FileReader(new File("diffsydiw.csv")));
			Map<Integer, PostingList> sympPostings = new TreeMap<Integer, PostingList>();
			Map<Integer, PostingList> diagPostings = new TreeMap<Integer, PostingList>();
			reader.readLine();
			while((line = reader.readLine()) != null){
				String[] temp = line.trim().split("\",\"");
				//Split line into sid and did
				sid = Integer.parseInt(temp[0].replace("\"", ""));
				did = Integer.parseInt(temp[1].replace("\"", ""));
				if(sympPostings.containsKey(sid)){
					if(!sympPostings.get(sid).getList().contains(did)){
						sympPostings.get(sid).getList().add(did);
					}
				}
				else{
					PostingList post = new PostingList(sid);
					post.getList().add(did);
					sympPostings.put(sid, post);
				}
				if(diagPostings.containsKey(did)){
					if(!diagPostings.get(did).getList().contains(sid)){
						diagPostings.get(did).getList().add(sid);
					}
				}
				else{
					PostingList post = new PostingList(did);
					post.getList().add(sid);
					diagPostings.put(did, post);
				}
			}
			reader.close();

			File sympfile = new File("sympCorpus.index");
			RandomAccessFile sympbfc = new RandomAccessFile(sympfile, "rw");
			//Write index to file
			FileChannel sympfc = sympbfc.getChannel();
			position = 0;
			for(int i: sympPostings.keySet()){
				writePosting(sympfc, sympPostings.get(i), sympPostingDict);
			}
			sympbfc.close();
			
			File diagfile = new File("diagCorpus.index");
			RandomAccessFile diagbfc = new RandomAccessFile(diagfile, "rw");
			//Write index to file
			FileChannel diagfc = diagbfc.getChannel();
			position = 0;
			for(int i: diagPostings.keySet()){
				writePosting(diagfc, diagPostings.get(i), diagPostingDict);
			}
			diagbfc.close();
			//Write sympPosting and diagPosting to file
			BufferedWriter sympPostWriter = new BufferedWriter(new FileWriter(new File("sympPosting.dict")));
			for (Integer sympId : sympPostingDict.keySet()) {
				sympPostWriter.write(sympId + "\t" + sympPostingDict.get(sympId).getFirst()
						+ "\t" + sympPostingDict.get(sympId).getSecond() + "\n");
			}
			sympPostWriter.close();
			
			BufferedWriter diagPostWriter = new BufferedWriter(new FileWriter(new File("diagPosting.dict")));
			for (Integer diagId : diagPostingDict.keySet()) {
				diagPostWriter.write(diagId + "\t" + diagPostingDict.get(diagId).getFirst()
						+ "\t" + diagPostingDict.get(diagId).getSecond() + "\n");
			}
			diagPostWriter.close();
			
			return 0;
		}
		
}
