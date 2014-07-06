package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.savers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;

/**
 * This is an implementation of a class implementing the Saver interface. By
 * means of these lines, the user choose to keep his patterns in a file whose
 * path is given to this class.
 *
 * Copyright Antonio Gomariz Peñalver 2013
 *
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author agomariz
 */
public class SaverIntoFile implements Saver {

    private BufferedWriter writer = null;
    private String path = null;

    public SaverIntoFile(String outputFilePath) throws IOException {
        path = outputFilePath;
        writer = new BufferedWriter(new FileWriter(outputFilePath));
    }

    @Override
    public void savePattern(Pattern p) {
        if (writer != null) {
            // create a stringbuffer
            StringBuilder r = new StringBuilder("");
            // for each itemset in this sequential pattern
            r.append(p.toStringToFile());
            try {
                // write the string to the file
                writer.write(r.toString());
                // start a new line
                writer.newLine();
            } catch (IOException ex) {
                Logger.getLogger(SaverIntoFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void finish() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(SaverIntoFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void clear() {
        writer = null;
    }

    @Override
    public String print() {
        return "Content at file " + path;
    }

    @Override
    public void savePatterns(Collection<Pattern> patterns) {
        for(Pattern pattern:patterns){
            this.savePattern(pattern);
        }
    }
}
