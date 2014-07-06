package com.taj.caller;

import ca.pfv.spmf.tools.dataset_converter.TransactionDatabaseConverter;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Wrapper to call the library from R.
 */
public class AnalyseFP {
    public static void doIt(String arffPath, String convertedFilePath) throws IOException {

        if (!new File(arffPath).exists()) {
            throw new IllegalArgumentException("Input file " + arffPath + " doesn't exist.\nNo conversion possible.");
        }

        File output = new File(convertedFilePath);

        if(output.exists()) {
            output.delete();
        }

        int transactionCount = Integer.MAX_VALUE;  // the number of transaction from the input file to be converted

        // Create a converter
        TransactionDatabaseConverter converter = new TransactionDatabaseConverter();
        // Call the method to convert the input file from ARFF to the SPMF format
        Map<Integer, String> conversionMap = converter.convertARFFandReturnMap(arffPath, convertedFilePath, transactionCount);



    }
}
