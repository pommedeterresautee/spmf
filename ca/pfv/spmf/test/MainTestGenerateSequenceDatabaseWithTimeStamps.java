package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.tools.dataset_converter.SequenceDatabaseConverter;
import ca.pfv.spmf.tools.dataset_generator.SequenceDatabaseGenerator;

/**
 * Example of how to use the random sequence database generator, from
 * the source code, to generate a database with timestamps.

 */
public class MainTestGenerateSequenceDatabaseWithTimeStamps {
	public static void main(String [] arg) throws IOException{
		
		// BMS, Kosarak
		String outputFile = ".//output.txt";
		
		SequenceDatabaseGenerator generator = new SequenceDatabaseGenerator();
		generator.generateDatabase(5, 500, 2, 8, outputFile, true);
	}

	

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = SequenceDatabaseConverter.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
