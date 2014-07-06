package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.tools.dataset_converter.Formats;
import ca.pfv.spmf.tools.dataset_converter.SequenceDatabaseConverter;

/**
 * Example of how to convert a sequence database from a given format
 * to the SPMF format.
 */
public class MainTestConvertSequenceDatabase {
	
	public static void main(String [] arg) throws IOException{
		
		String inputFile = fileToPath("contextCSV.txt");
		String outputFile = ".//output.txt";
		Formats inputFileformat = Formats.CSV_INTEGER;
		int sequenceCount = Integer.MAX_VALUE;
		
		SequenceDatabaseConverter converter = new SequenceDatabaseConverter();
		converter.convert(inputFile, outputFile, inputFileformat, sequenceCount);
	}

	

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestConvertSequenceDatabase.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
