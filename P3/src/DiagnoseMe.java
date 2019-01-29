/*
 * Jidapa		Sumanotham	 5888043	Section 1
 * Papichaya 	Quengdaeng	 5888146	Section 1
 * Intukorn		Limpachaveng 5888261	Section 1
 */
import java.io.IOException;
import java.util.Scanner;

public class DiagnoseMe {
	public static void main(String[] args) throws IOException {
		//Indexing
		Indexer.runIndexer();
		//Querying
		Query queryService = new Query();
		System.out.println("Enter your symptoms (separate each symptom with , ): ");
		Scanner s = new Scanner(System.in);
		String input = s.nextLine();
		queryService.runQueryService(input);
		s.close();
	}
}
