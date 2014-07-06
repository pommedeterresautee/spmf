package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.tools.dataset_converter.SequenceDatabaseConverter;
import ca.pfv.spmf.tools.dataset_generator.SequenceDatabaseGenerator;

/**
 * Example of how to use the random sequence database generator to
 * generate a sequence database, from the source code.
 */
public class MainTestGenerateSequenceDatabase {

	public static void main(String [] arg) throws IOException{

		String outputFile = ".//output.txt";
		
		SequenceDatabaseGenerator generator = new SequenceDatabaseGenerator();
		generator.generateDatabase(5, 500, 2, 8, outputFile, false);
	}

	

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = SequenceDatabaseConverter.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
