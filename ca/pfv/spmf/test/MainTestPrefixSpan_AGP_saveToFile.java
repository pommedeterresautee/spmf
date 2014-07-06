package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.AlgoPrefixSpan_AGP;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.creators.AbstractionCreator_Qualitative;

/**
 * Example of how to use the algorithm GSP, saving the results in a given
 * file
 * @author agomariz
 */
public class MainTestPrefixSpan_AGP_saveToFile {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
    	String output = ".//output.txt";
        // Load a sequence database
        double support = (double)180/360;
        
        boolean keepPatterns = true;
        boolean verbose=false;

        AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();

        SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator);

        //sequenceDatabase.loadFile(fileToPath("salidaFormateadaCodificadaSinIDs.txt"), support);
        sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"), support);

        AlgoPrefixSpan_AGP algorithm = new AlgoPrefixSpan_AGP(support, abstractionCreator);

        System.out.println(sequenceDatabase.toString());

        //Put the concrete path file where we want to keep the output
        algorithm.runAlgorithm(sequenceDatabase,keepPatterns,verbose, output);
        System.out.println(algorithm.getNumberOfFrequentPatterns()+ " patterns found.");
        System.out.println(algorithm.printStatistics());
    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = MainTestPrefixSpan_AGP_saveToFile.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
    }
}
