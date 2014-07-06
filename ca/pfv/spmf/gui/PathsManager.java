package ca.pfv.spmf.gui;
/*
 * Copyright (c) 2008-2013 Philippe Fournier-Viger
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
 */
import java.util.prefs.Preferences;

/**
 * This class is used to manage registry keys for
 * storing user preferences for the SPMF GUI.
 * 
 * @see MainWindow
 * @author Philippe Fournier-Viger
 */
class PathsManager {   
	// We use two registry key to store
	// the paths of the last folders used by the user
	// for input and output files.
    public static final String REGKEY_SPMF_INPUT_FILE = "ca.pfv.spmf.gui.input";
    public static final String REGKEY_SPMF_OUTPUT_FILE = "ca.pfv.spmf.gui.output";

    // Implemented as a singleton
    private static PathsManager instance;

    /**
     * Default constructor
     */
    private PathsManager(){

    }
    
    /**
     * Get the only instance of this class (a singleton)
     * @return the instance
     */
    public static PathsManager getInstance(){
        if(instance == null){
            instance = new PathsManager();
        }
        return instance;
    }
    
    /**
     * Get the input file path stored in the registry
     * @return a path as a string
     */
    public String getInputFilePath() {
        //      read back from registry HKCurrentUser
        Preferences p = Preferences.userRoot();
        return p.get(REGKEY_SPMF_INPUT_FILE, null);
    }
    
    /**
     * Store an input file path in the registry
     * @param filepath a path as a string
     */
    public void setInputFilePath(String filepath) {
        // write into HKCurrentUser
        Preferences p = Preferences.userRoot();
        p.put(REGKEY_SPMF_INPUT_FILE, filepath);
    }
    
    /**
     * Get the output file path stored in the registry
     * @return a path as a string
     */
    public String getOutputFilePath() {
        //      read back from registry HKCurrentUser
        Preferences p = Preferences.userRoot();
        return p.get(REGKEY_SPMF_OUTPUT_FILE, null);
    }

    /**
     * Store an output file path in the registry
     * @param filepath a path as a string
     */
    public void setOutputFilePath(String filepath) {
        // write into HKCurrentUser
        Preferences p = Preferences.userRoot();
        p.put(REGKEY_SPMF_OUTPUT_FILE, filepath);
    }
}
