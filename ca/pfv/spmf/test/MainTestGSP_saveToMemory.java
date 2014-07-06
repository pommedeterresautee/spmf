package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.AlgoGSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator_Qualitative;

/**
 * Example of how to use the algorithm GSP, saving the results in the main
 * memory
 * @author agomariz
 */
public class MainTestGSP_saveToMemory {


    public static void main(String[] args) throws IOException {
        // Load a sequence database
        double support = (double)0.5, mingap = 0, maxgap = Integer.MAX_VALUE, windowSize = 0;

        boolean keepPatterns = true;
        boolean verbose=false;

        AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
        SequenceDatabase sequenceDatabase = new SequenceDatabase(abstractionCreator);
        
        sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"), support);

        AlgoGSP algorithm = new AlgoGSP(support, mingap, maxgap, windowSize,abstractionCreator);


        System.out.println(sequenceDatabase.toString());

        algorithm.runAlgorithm(sequenceDatabase,keepPatterns,verbose,null);
        System.out.println(algorithm.getNumberOfFrequentPatterns()+ " frequent pattern found.");

        System.out.println(algorithm.printedOutputToSaveInFile());
        
        //System.out.println(algorithm.printStatistics());
    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = MainTestGSP_saveToMemory.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
    }
}
