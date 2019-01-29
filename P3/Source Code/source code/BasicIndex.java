/*
 * Jidapa		Sumanotham	 5888043	Section 1
 * Papichaya 	Quengdaeng	 5888146	Section 1
 * Intukorn		Limpachaveng 5888261	Section 1
 */
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class BasicIndex implements BaseIndex {
	@Override
	public PostingList readPosting(FileChannel fc) {
		/*
		 * TODO: Your code here
		 *       Read and return the postings list from the given file.
		 */
		
		ByteBuffer buff = ByteBuffer.allocate(4);
		
		PostingList posting = null;
		try {
			if(fc.position() >= fc.size()){
				return null;
			}
			else{
			int numBytesRead = fc.read(buff);
			buff.flip();
			int termId = buff.getInt();
			buff.clear();
			posting = new PostingList(termId);
			numBytesRead = fc.read(buff);
			buff.flip();
			int freq = buff.getInt();
			buff.clear();
			for(int i=0; i<freq; i++){
				numBytesRead = fc.read(buff);
				buff.flip();
				int docId = buff.getInt();
				posting.getList().add(docId);
				buff.clear();
			}
			}
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
		
		
	return posting;
	}

	@Override
	public void writePosting(FileChannel fc, PostingList p){
		/*
		 * TODO: Your code here
		 *       Write the given postings list to the given file.
		 */
		int freq = p.getList().size();
		int size = 8+(4*freq);
		ByteBuffer buff = ByteBuffer.allocate(size);
		buff.putInt(p.getTermId());
		buff.putInt(freq);
		for(int i: p.getList()){
			buff.putInt(i);
		}
		buff.flip();
		try {
			fc.write(buff);
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
}

