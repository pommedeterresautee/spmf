package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.tools.dataset_converter.SequenceDatabaseConverter;
import ca.pfv.spmf.tools.dataset_generator.TransactionDatabaseGenerator;

/**
 * Example of how to use the random transaction database generator
 * from the source code.
 */
public class MainTestGenerateTransactionDatabase {

	public static void main(String [] arg) throws IOException{
		String outputFile = ".//output.txt";
		TransactionDatabaseGenerator generator = new TransactionDatabaseGenerator();
		generator.generateDatabase(5, 500, 4, outputFile);
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = SequenceDatabaseConverter.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
