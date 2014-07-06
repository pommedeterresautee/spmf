package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp.AlgoGoKrimp;
import ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp.DataReader;

/**
 *  Example of how to use the GoKrimp Algorithm in source code and print
 *  the result to the console.
 */
public class MainTestGoKrimp_printResultToConsole {
	
	public static void main(String [] arg) throws IOException{
		String inputDatabase = fileToPath("test_goKrimp.dat");  // the database
		String inputLabelFile = fileToPath("test_goKrimp.lab");  // the label file
//		String inputLabelFile = "";  // use this if no label file

        DataReader d=new DataReader();
        //GoKrimp g=d.readData(inputDatabase, inputLabelFile);
        AlgoGoKrimp g=d.readData_SPMF(inputDatabase, inputLabelFile);
        //g.printData();
        g.gokrimp();
		
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestGoKrimp_printResultToConsole.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
