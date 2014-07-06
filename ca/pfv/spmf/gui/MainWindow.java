package ca.pfv.spmf.gui;
/*
 * Copyright (c) 2008-2014 Philippe Fournier-Viger
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
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import ca.pfv.spmf.algorithms.associationrules.IGB.AlgoIGB;
import ca.pfv.spmf.algorithms.associationrules.Indirect.AlgoINDIRECT;
import ca.pfv.spmf.algorithms.associationrules.MNRRules.AlgoMNRRules;
import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.Database;
import ca.pfv.spmf.algorithms.associationrules.closedrules.AlgoClosedRules;
import ca.pfv.spmf.algorithms.associationrules.fhsar.AlgoFHSAR;
import ca.pfv.spmf.algorithms.clustering.hierarchical_clustering.AlgoHierarchicalClustering;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoKMeans;
import ca.pfv.spmf.algorithms.frequentpatterns.MSApriori.AlgoMSApriori;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori.AlgoApriori;
import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTID.AlgoAprioriTID;
import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTIDClose.AlgoAprioriTIDClose;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_HT.AlgoAprioriHT;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_close.AlgoAprioriClose;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_inverse.AlgoAprioriInverse;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_rare.AlgoAprioriRare;
import ca.pfv.spmf.algorithms.frequentpatterns.cfpgrowth.AlgoCFPGrowth;
import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoCharmMFI;
import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoCharm_Bitset;
import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoDCharm_Bitset;
import ca.pfv.spmf.algorithms.frequentpatterns.dci_closed_optimized.AlgoDCI_Closed_Optimized;
import ca.pfv.spmf.algorithms.frequentpatterns.defme.AlgoDefMe;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat.AlgoDEclat;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat.AlgoDEclat_Bitset;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat.AlgoEclat;
import ca.pfv.spmf.algorithms.frequentpatterns.eclat.AlgoEclat_Bitset;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.algorithms.frequentpatterns.hmine.AlgoHMine;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoFHM;
import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoHUIMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.pascal.AlgoPASCAL;
import ca.pfv.spmf.algorithms.frequentpatterns.relim.AlgoRelim;
import ca.pfv.spmf.algorithms.frequentpatterns.two_phase.AlgoTwoPhase;
import ca.pfv.spmf.algorithms.frequentpatterns.uapriori.AlgoUApriori;
import ca.pfv.spmf.algorithms.frequentpatterns.vme.AlgoVME;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.AlgoZart;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.TFTableFrequent;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.TZTableClosed;
import ca.pfv.spmf.algorithms.sequential_rules.cmdeogun.AlgoCMDeogun;
import ca.pfv.spmf.algorithms.sequential_rules.cmrules.AlgoCMRules;
import ca.pfv.spmf.algorithms.sequential_rules.rulegen.AlgoRuleGen;
import ca.pfv.spmf.algorithms.sequential_rules.rulegrowth.AlgoRULEGROWTH;
import ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.AlgoTNS;
import ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.AlgoTopSeqRules;
import ca.pfv.spmf.algorithms.sequential_rules.trulegrowth.AlgoTRuleGrowth;
import ca.pfv.spmf.algorithms.sequential_rules.trulegrowth_with_strings.AlgoTRuleGrowth_withStrings;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoBIDEPlus;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoFEAT;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoFSGP;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoMaxSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoPrefixSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoTSP_nonClosed;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan_with_strings.AlgoPrefixSpan_with_Strings;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.AlgoCM_ClaSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.AlgoClaSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.AlgoCloSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoFournierViger08;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoPrefixSpanMDSPM;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.AlgoDim;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.AlgoSeqDim;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.MDSequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp.AlgoGoKrimp;
import ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp.DataReader;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.AlgoGSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.lapin.AlgoLAPIN_LCI;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.AlgoPrefixSpan_AGP;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.AlgoCMSPADE;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.AlgoSPADE;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration.CandidateGenerator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration.CandidateGenerator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator_FatBitmap;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoCMSPAM;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoSPAM;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoTKS;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoVGEN;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoVMSP;
import ca.pfv.spmf.input.sequence_database_list_strings.SequenceDatabase;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.test.MainTestApriori_saveToFile;
import ca.pfv.spmf.tools.dataset_converter.Formats;
import ca.pfv.spmf.tools.dataset_converter.SequenceDatabaseConverter;
import ca.pfv.spmf.tools.dataset_converter.TransactionDatabaseConverter;
import ca.pfv.spmf.tools.dataset_generator.SequenceDatabaseGenerator;
import ca.pfv.spmf.tools.dataset_generator.TransactionDatabaseGenerator;
import ca.pfv.spmf.tools.dataset_stats.SequenceStatsGenerator;
import ca.pfv.spmf.tools.resultConverter.ResultConverter;

/**
 * This is a simple user interface to run the main algorithms in SPMF.
 * 
 * @author Philippe Fournier-Viger
 */
public class MainWindow extends JFrame {

    // variable for the current version of SPMF
    public static String SPMF_VERSION = "0.96k";
    // current input file
    private String inputFile = null;
    // current output file
    private String outputFile = null;
    private static final long serialVersionUID = 1L;
    /**
     * The following fields are components of the user interface. They are
     * generated automatically by the Visual Editor plugin of Eclipse.
     */
    private JPanel contentPane;
    private JTextField textFieldParam1;
    private JTextField textFieldParam2;
    private JTextField textFieldParam3;
    private JTextField textFieldParam4;
    private JTextField textFieldParam5;
    private JTextField textFieldParam6;
    private JLabel labelParam1;
    private JLabel labelParam2;
    private JLabel labelParam3;
    private JLabel labelParam4;
    private JLabel labelParam5;
    private JLabel labelParam6;
    private JLabel lbHelp1;
    private JLabel lbHelp2;
    private JLabel lbHelp3;
    private JLabel lbHelp4;
    private JLabel lbHelp5;
    private JLabel lbHelp6;
    private JTextField textFieldInput;
    private JTextField textFieldOutput;
    private JComboBox<String> comboBox;
    private JTextArea textArea;
    private JButton buttonRun;
    private JCheckBox checkboxOpenOutput;
    private JButton buttonExample;
    private JLabel lblSetOutputFile;
    private JButton buttonOutput;
    private JButton buttonInput;
    private JLabel lblChooseInputFile;

    /**
     * Method to launch the software. If there are command line arguments, it
     * means that the software is launched from the command line. Otherwise,
     * this method launches the graphical user interface.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
    	// The following commented lines of code are for debugging purposes:
		//args = new String[]{"run", "SPADE", "C:\\Users\\ph\\Desktop\\SPMF\\test_files\\contextPrefixSpan.txt", "output.txt",  "50%", "100"};
//C:\Users\ph\Desktop\SPMF\test_files
//		System.out.println("Command " + Arrays.toString(args));
        
    	// If there are command line arguments, we don't launch
        // the user interface. It means that the user is using
        // the command line.
        if (args.length != 0) {
        	processCommandLineArguments(args); // process command line arguments.
        } else {
            // Else, we launch the graphical user interface.
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    try {
                        // Create the window
                        MainWindow frame = new MainWindow();
                        frame.setVisible(true); // show it to the user
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Create the frame.
     */
    public MainWindow() {
        setResizable(false);
        addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent arg0) {
                System.exit(0);
            }
        });
        // set the title of the window
        setTitle("SPMF v" + SPMF_VERSION);

        // When the user clicks the "x" the software will close.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // size of the window
        setBounds(100, 100, 644, 564);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // Combo box to store the list of algorithms.
        comboBox = new JComboBox<String>(new Vector<String>());
        comboBox.setMaximumRowCount(20);
        comboBox.addItem("");
        comboBox.addItem("  ---- SEQUENTIAL PATTERN MINING ----");
        comboBox.addItem("BIDE+");
        comboBox.addItem("BIDE+_with_strings");
        comboBox.addItem("ClaSP");
        comboBox.addItem("CloSpan");
        comboBox.addItem("CM-SPADE");
        comboBox.addItem("CM-SPAM");
        comboBox.addItem("CM-ClaSP");
        comboBox.addItem("FEAT");
        comboBox.addItem("FSGP");
        comboBox.addItem("Fournier08-Closed+time");
        comboBox.addItem("GoKrimp");
        comboBox.addItem("GSP");
        comboBox.addItem("HirateYamana");
        comboBox.addItem("LAPIN");
        comboBox.addItem("MaxSP");
        comboBox.addItem("PrefixSpan");
        comboBox.addItem("PrefixSpan_AGP");
        comboBox.addItem("PrefixSpan_PostProcessingClosed");
        comboBox.addItem("PrefixSpan_with_strings");
        comboBox.addItem("SPADE");
        comboBox.addItem("SPADE_Parallelized");
        comboBox.addItem("SPAM");
        comboBox.addItem("SPAM_AGP");
        comboBox.addItem("SPAM_PostProcessingClosed");
        //comboBox.addItem("Fournier08-Closed+time+valued_items");
        comboBox.addItem("SeqDim_(PrefixSpan+Apriori)");
        comboBox.addItem("SeqDim_(PrefixSpan+Apriori)+time");
        comboBox.addItem("SeqDim_(BIDE+AprioriClose)");
        comboBox.addItem("SeqDim_(BIDE+AprioriClose)+time");
        comboBox.addItem("SeqDim_(BIDE+Charm)");
        comboBox.addItem("SeqDim_(BIDE+Charm)+time");
        comboBox.addItem("TKS");
        comboBox.addItem("TSP_nonClosed");
        comboBox.addItem("VGEN");
        comboBox.addItem("VMSP");
        comboBox.addItem("  ---- SEQUENTIAL RULE MINING ----");
        comboBox.addItem("CMRules");
        comboBox.addItem("CMDeo");
        comboBox.addItem("RuleGen");
        comboBox.addItem("RuleGrowth");
        comboBox.addItem("TRuleGrowth");
        comboBox.addItem("TRuleGrowth_with_strings");
        comboBox.addItem("TopSeqRules");
        comboBox.addItem("TNS");
        comboBox.addItem("  ---- ITEMSET MINING----");
        comboBox.addItem("Apriori");
        comboBox.addItem("Apriori_with_hash_tree");
        comboBox.addItem("Apriori_TID");
        comboBox.addItem("Apriori_TID_bitset");
        comboBox.addItem("Apriori_TIDClose");
        comboBox.addItem("AprioriClose");
        comboBox.addItem("AprioriRare");
        comboBox.addItem("AprioriInverse");
        comboBox.addItem("CFPGrowth++");
        comboBox.addItem("Charm_bitset");
        comboBox.addItem("dCharm_bitset");
        comboBox.addItem("Charm_MFI");
        comboBox.addItem("DCI_Closed");
        comboBox.addItem("DefMe");
        comboBox.addItem("Eclat");
        comboBox.addItem("dEclat");
        comboBox.addItem("Eclat_bitset");
        comboBox.addItem("dEclat_bitset");
        comboBox.addItem("FHM");
        comboBox.addItem("FPGrowth_itemsets");
        comboBox.addItem("FPGrowth_itemsets_with_strings");
        comboBox.addItem("HMine");
        comboBox.addItem("HUI-Miner");
        comboBox.addItem("MSApriori");
        comboBox.addItem("Pascal");
        comboBox.addItem("Relim");
        comboBox.addItem("Two-Phase");
        comboBox.addItem("UApriori");
        comboBox.addItem("VME");
        comboBox.addItem("Zart");
        comboBox.addItem("  ---- ASSOCIATION RULE MINING ----");
        comboBox.addItem("Apriori_association_rules");
        comboBox.addItem("Closed_association_rules");
        comboBox.addItem("FHSAR");
        comboBox.addItem("FPGrowth_association_rules");
        comboBox.addItem("FPGrowth_association_rules_with_lift");
        comboBox.addItem("CFPGrowth++_association_rules");
        comboBox.addItem("CFPGrowth++_association_rules_with_lift");
        comboBox.addItem("IGB");
        comboBox.addItem("Indirect_association_rules");
        comboBox.addItem("MNR");
        comboBox.addItem("Sporadic_association_rules");
        comboBox.addItem("TopKRules");
        comboBox.addItem("TNR");
        comboBox.addItem("  ---- CLUSTERING ----");
        comboBox.addItem("Hierarchical_clustering");
        comboBox.addItem("KMeans");
        comboBox.addItem("  ---- DATASET TOOLS ----");
        comboBox.addItem("Calculate_stats_for_a_sequence_database");
        comboBox.addItem("Convert_a_sequence_database_to_SPMF_format");
        comboBox.addItem("Convert_a_transaction_database_to_SPMF_format");
        comboBox.addItem("Generate_a_sequence_database");
        comboBox.addItem("Generate_a_sequence_database_with_timestamps");
        comboBox.addItem("Generate_a_transaction_database");

        // What to do when the user choose an algorithm : 
        comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				// We need to update the user interface:
				updateUserInterfaceAfterAlgorithmSelection(evt.getItem().toString(),
						evt.getStateChange() == ItemEvent.SELECTED);
			}
        	
        });
        comboBox.setBounds(263, 74, 306, 20);
        contentPane.add(comboBox);

        // The button "Run algorithm"
        buttonRun = new JButton("Run algorithm");
        buttonRun.setEnabled(false);
        buttonRun.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {

                // When the user clicks "run":
                processRunAlgorithmCommandFromGUI();

            }
        });
        buttonRun.setBounds(277, 340, 119, 23);
        contentPane.add(buttonRun);

        JLabel lblChooseAnAlgorithm = new JLabel("Choose an algorithm:");
        lblChooseAnAlgorithm.setBounds(22, 73, 204, 20);
        contentPane.add(lblChooseAnAlgorithm);

        JLabel lblNewLabel = new JLabel("New label");
        lblNewLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
            	// Show the webpage of the SPMF project
                openWebPage("http://www.philippe-fournier-viger.com/spmf/");
            }
        });
        lblNewLabel.setIcon(new ImageIcon(MainWindow.class.getResource("spmf.png")));
        lblNewLabel.setBounds(0, 0, 186, 62);
        contentPane.add(lblNewLabel);

        textFieldParam1 = new JTextField();
        textFieldParam1.setBounds(263, 164, 157, 20);
        contentPane.add(textFieldParam1);
        textFieldParam1.setColumns(10);

        buttonInput = new JButton("...");
        buttonInput.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                askUserToChooseInputFile();
            }
        });
        
        buttonInput.setBounds(430, 104, 32, 23);
        contentPane.add(buttonInput);

        buttonOutput = new JButton("...");
        buttonOutput.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                askUserToChooseOutputFile();
            }
        });
        buttonOutput.setBounds(430, 133, 32, 23);
        contentPane.add(buttonOutput);

        labelParam1 = new JLabel("Parameter 1:");
        labelParam1.setBounds(22, 167, 204, 14);
        contentPane.add(labelParam1);

        labelParam2 = new JLabel("Parameter 2:");
        labelParam2.setBounds(22, 192, 204, 14);
        contentPane.add(labelParam2);

        labelParam3 = new JLabel("Parameter 3:");
        labelParam3.setBounds(22, 217, 204, 14);
        contentPane.add(labelParam3);

        labelParam4 = new JLabel("Parameter 4:");
        labelParam4.setBounds(22, 239, 231, 14);
        contentPane.add(labelParam4);

        labelParam5 = new JLabel("Parameter 5:");
        labelParam5.setBounds(22, 264, 156, 14);
        contentPane.add(labelParam5);

        labelParam6 = new JLabel("Parameter 6:");
        labelParam6.setBounds(22, 289, 156, 14);
        contentPane.add(labelParam6);

        textFieldParam2 = new JTextField();
        textFieldParam2.setColumns(10);
        textFieldParam2.setBounds(263, 189, 157, 20);
        contentPane.add(textFieldParam2);

        textFieldParam3 = new JTextField();
        textFieldParam3.setColumns(10);
        textFieldParam3.setBounds(263, 214, 157, 20);
        contentPane.add(textFieldParam3);

        textFieldParam4 = new JTextField();
        textFieldParam4.setColumns(10);
        textFieldParam4.setBounds(263, 236, 157, 20);
        contentPane.add(textFieldParam4);

        textFieldParam5 = new JTextField();
        textFieldParam5.setColumns(10);
        textFieldParam5.setBounds(263, 261, 157, 20);
        contentPane.add(textFieldParam5);

        textFieldParam6 = new JTextField();
        textFieldParam6.setColumns(10);
        textFieldParam6.setBounds(263, 286, 157, 20);
        contentPane.add(textFieldParam6);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 377, 618, 148);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        scrollPane.setViewportView(textArea);
        System.setOut(new PrintStream(new TextAreaOutputStream(textArea)));

        textFieldInput = new JTextField();
        textFieldInput.setEditable(false);
        textFieldInput.setBounds(263, 105, 157, 20);
        contentPane.add(textFieldInput);
        textFieldInput.setColumns(10);

        textFieldOutput = new JTextField();
        textFieldOutput.setEditable(false);
        textFieldOutput.setColumns(10);
        textFieldOutput.setBounds(263, 134, 157, 20);
        contentPane.add(textFieldOutput);

        checkboxOpenOutput = new JCheckBox("Open output file when the algorithm terminates");
        checkboxOpenOutput.setSelected(true);
        checkboxOpenOutput.setBounds(22, 310, 358, 23);
        contentPane.add(checkboxOpenOutput);

        buttonExample = new JButton("?");
        buttonExample.setEnabled(false);
        buttonExample.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {

                // When the user clicks on the "?",
                // we open the webpage corresponding to the algorithm
                // that is currently selected.
                String choice = (String) comboBox.getSelectedItem();
                openHelpWebPageForAlgorithm(choice);
            }

			
        });
        buttonExample.setBounds(579, 73, 49, 23);
        contentPane.add(buttonExample);

        lblChooseInputFile = new JLabel("Choose input file");
        lblChooseInputFile.setBounds(22, 108, 97, 14);
        contentPane.add(lblChooseInputFile);

        lblSetOutputFile = new JLabel("Set output file");
        lblSetOutputFile.setBounds(22, 137, 97, 14);
        contentPane.add(lblSetOutputFile);

        lbHelp1 = new JLabel("help1");
        lbHelp1.setBounds(430, 167, 157, 14);
        contentPane.add(lbHelp1);

        lbHelp2 = new JLabel("help2");
        lbHelp2.setBounds(430, 192, 157, 14);
        contentPane.add(lbHelp2);

        lbHelp3 = new JLabel("help3");
        lbHelp3.setBounds(430, 217, 157, 14);
        contentPane.add(lbHelp3);

        lbHelp4 = new JLabel("help4");
        lbHelp4.setBounds(430, 239, 157, 14);
        contentPane.add(lbHelp4);

        lbHelp5 = new JLabel("help5");
        lbHelp5.setBounds(430, 264, 157, 14);
        contentPane.add(lbHelp5);

        lbHelp6 = new JLabel("help6");
        lbHelp6.setBounds(430, 289, 157, 14);
        contentPane.add(lbHelp6);

        hideAllParams();
    }


    /**
     * This method updates the user interface according to what the user has selected or unselected
     * in the list of algorithms. For example, if the user choose the "PrefixSpan" algorithm
     * the parameters of the PrefixSpan algorithm will be shown in the user interface.
     * @param algorithmName  the algorithm name. 
     * @boolean isSelected indicate if the algorithm has been selected or unselected
     */
	private void updateUserInterfaceAfterAlgorithmSelection(String algorithmName, boolean isSelected) {
        // COMBOBOX ITEM SELECTION - ITEM STATE CHANGED
        if (isSelected) {
            buttonRun.setEnabled(true);
            buttonExample.setEnabled(true);

            if ("SPAM".equals(algorithmName)
                    || "PrefixSpan".equals(algorithmName)
                    || "FEAT".equals(algorithmName)
                    || "FSGP".equals(algorithmName)
                    || "CM-SPAM".equals(algorithmName)
                    || "VMSP".equals(algorithmName)
                    || "VGEN".equals(algorithmName)
                    ) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.5 or 50%)");
                setParam(textFieldParam2, "Max pattern length:", labelParam2, "(e.g. 4 items)");

            } else if ("HirateYamana".equals(algorithmName)
                    || "Fournier08-Closed+time".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.5 or 50%)");
                setParam(textFieldParam2, "Min time interval:", labelParam2, "(e.g. 0 itemsets)");
                setParam(textFieldParam3, "Max time interval:", labelParam3, "(e.g. 2 itemsets)");
                setParam(textFieldParam4, "Min whole time interval:", labelParam4, "(e.g. 0 itemsets)");
                setParam(textFieldParam5, "Max whole time interval:", labelParam5, "(e.g. 2 itemsets)");

            } //					else if(
            //							"Fournier08-Closed+time+valued_items".equals(algorithmName))
            //					{
            //						// show the parameters of this algorithm
            //						hideAllParams(); 
            //						setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.5 or 50%)");
            //						setParam(textFieldParam2, "Min time interval:", labelParam2, "(e.g. 0 itemsets)");
            //						setParam(textFieldParam3, "Max time interval:", labelParam3, "(e.g. 2 itemsets)");
            //						setParam(textFieldParam4, "Min whole time interval:", labelParam4, "(e.g. 0 itemsets)");
            //						setParam(textFieldParam5, "Max whole time interval:", labelParam5, "(e.g. 2 itemsets)");
            //						setParam(textFieldParam6, "Max whole time interval:", labelParam6, "(e.g. 2 )");
            //						setParam(textFieldParam7, "Max whole time interval:", labelP, "(e.g. 2 )");
            //						
            //					}
            //					comboBox.addItem("Fournier08-Closed+time");
            //					comboBox.addItem("Fournier08-Closed+time+valued_items");
            else if ("SeqDim_(PrefixSpan+Apriori)".equals(algorithmName)
                    || "SeqDim_(BIDE+AprioriClose)".equals(algorithmName)
                    || "SeqDim_(BIDE+Charm)".equals(algorithmName)
                    || "PrefixSpan_with_strings".equals(algorithmName)
                    || "BIDE+".equals(algorithmName)
                    || "BIDE+_with_strings".equals(algorithmName)
                    || "PrefixSpan_AGP".equals(algorithmName)
                    || "PrefixSpan_PostProcessingClosed".equals(algorithmName)
                    || "GSP".equals(algorithmName)
                    || "SPADE".equals(algorithmName)
                    || "CM-SPADE".equals(algorithmName)
                    || "CM-ClaSP".equals(algorithmName)
                    || "SPADE_Parallelized".equals(algorithmName)
                    || "SPAM_AGP".equals(algorithmName)
                    || "SPAM_PostProcessingClosed".equals(algorithmName)
                    || "ClaSP".equals(algorithmName)
                    || "CloSpan".equals(algorithmName)
                    || "LAPIN".equals(algorithmName)
                    || "MaxSP".equals(algorithmName)
                    || "FPGrowth_itemsets".equals(algorithmName)
                    || "FPGrowth_itemsets_with_strings".equals(algorithmName)
                    || "Apriori".equals(algorithmName)
                    || "Apriori_TID_bitset".equals(algorithmName)
                    || "Apriori_TID".equals(algorithmName)
                    || "Apriori_TIDClose".equals(algorithmName)
                    || "AprioriClose".equals(algorithmName)
                    || "AprioriRare".equals(algorithmName)
                    || "Eclat".equals(algorithmName)
                    || "dEclat".equals(algorithmName)
                    || "Charm_MFI".equals(algorithmName)
                    || "Charm_bitset".equals(algorithmName)
                    || "dCharm_bitset".equals(algorithmName)
                    || "dEclat_bitset".equals(algorithmName)
                    || "Relim".equals(algorithmName)
                    || "Eclat_bitset".equals(algorithmName)
                    || "Pascal".equals(algorithmName)
                    || "DefMe".equals(algorithmName)
                    || "Zart".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.4 or 40%)");
            } else if ("Apriori_with_hash_tree".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.4 or 40%)");
                setParam(textFieldParam2, "Hash-tree branch count:", labelParam2, "(default: 30)");
            } else if ("SeqDim_(PrefixSpan+Apriori)+time".equals(algorithmName)
                    || "SeqDim_(BIDE+AprioriClose)+time".equals(algorithmName)
                    || "SeqDim_(BIDE+Charm)+time".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g.  0.5  or 50 %)");
                setParam(textFieldParam2, "Choose minInterval:", labelParam2, "(e.g.  1)");
                setParam(textFieldParam3, "Choose maxInterval:", labelParam3, "(e.g.  5)");
                setParam(textFieldParam4, "Choose minWholeInterval:", labelParam4, "(e.g.  1)");
                setParam(textFieldParam5, "Choose maxWholeInterval:", labelParam5, "(e.g.  5)");
            } else if ("HMine".equals(algorithmName)
                    || "DCI_Closed".equals(algorithmName)
                    ) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (integer):", labelParam1, "(e.g. 2)");
            } else if ("VME".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose threshold (%):", labelParam1, "(e.g. 0.15 or 15%)");
            } else if ("AprioriInverse".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.001 or 0.1%)");
                setParam(textFieldParam2, "Choose maxsup (%):", labelParam2, "(e.g. 0.06 or 6%)");
            } else if ("UApriori".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose expected support (%):", labelParam1, "(e.g. 0.10)");
            } else if ("FPGrowth_association_rules".equals(algorithmName)
                    || "Apriori_association_rules".equals(algorithmName)
                    || "RuleGrowth".equals(algorithmName)
                    || "CMRules".equals(algorithmName)
                    || "CMDeo".equals(algorithmName)
                    || "IGB".equals(algorithmName)
                    || "Closed_association_rules".equals(algorithmName)
                    || "MNR".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.5 or 50%)");
                setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.6 or 60%)");
            }//
            else if ("Sporadic_association_rules".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.1 or 10%)");
                setParam(textFieldParam2, "Choose maxsup (%):", labelParam2, "(e.g. 0.6 or 60%)");
                setParam(textFieldParam3, "Choose minconf (%):", labelParam2, "(e.g. 0.6 or 60%)");
            } else if ("Indirect_association_rules".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.6 or 60%)");
                setParam(textFieldParam2, "Choose ts (%):", labelParam2, "(e.g. 0.5 or 50%)");
                setParam(textFieldParam3, "Choose minconf (%):", labelParam2, "(e.g. 0.1 or 10%)");
            } else if ("RuleGen".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (integer):", labelParam1, "(e.g. 3)");
                setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.6 or 60%)");
            } else if ("KMeans".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose K:", labelParam1, "(e.g. 3)");
            } else if ("Hierarchical_clustering".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose max distance:", labelParam1, "(e.g. 4)");
            } else if ("FPGrowth_association_rules_with_lift".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.5 or 50%)");
                setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.6 or 60%)");
                setParam(textFieldParam3, "Choose minlift:", labelParam3, "(e.g. 0.2)");
            } else if ("CFPGrowth++_association_rules_with_lift".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "MIS file name:", labelParam1, "(e.g. MIS.txt)");
                setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.6 or 60%)");
                setParam(textFieldParam3, "Choose minlift:", labelParam3, "(e.g. 0.2)");
            } else if ("CFPGrowth++_association_rules".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "MIS file name:", labelParam1, "(e.g. MIS.txt)");
                setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.6 or 60%)");
            } 
            else if ("TopSeqRules".equals(algorithmName)
                    || "TopKRules".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose k:", labelParam1, "(e.g. 3)");
                setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.8 or 80%)");
            } else if ("TKS".equals(algorithmName)
                    || "TSP_nonClosed".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose k:", labelParam1, "(e.g. 5)");
            } 
            
            else if ("TNR".equals(algorithmName) || "TNS".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose k:", labelParam1, "(e.g. 10)");
                setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.5 or 50%)");
                setParam(textFieldParam3, "Choose delta:", labelParam3, "(e.g. 2)");
            } else if ("Two-Phase".equals(algorithmName) || "HUI-Miner".equals(algorithmName)
            		|| "FHM".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minutility:", labelParam1, "(e.g. 30)");
            } else if ("FHSAR".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.5 or 50%)");
                setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.6 or 60%)");
                setParam(textFieldParam3, "SAR file name:", labelParam3, "(e.g. sar.txt)");

            } else if ("GoKrimp".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Label file name (optional:", labelParam1, "(e.g. test_goKrimp.lab)");
            } 
            else if ("MSApriori".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose beta:", labelParam1, "(e.g. 0.4 or 40%)");
                setParam(textFieldParam2, "Choose LS:", labelParam2, "(e.g. 0.2 or 20%)");
            } else if ("CFPGrowth++".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "MIS file name:", labelParam1, "(e.g. MIS.txt)");
            } else if ("TRuleGrowth".equals(algorithmName)
                    || "TRuleGrowth_with_strings".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose minsup (%):", labelParam1, "(e.g. 0.7 or 70%)");
                setParam(textFieldParam2, "Choose minconf (%):", labelParam2, "(e.g. 0.8 or 80%)");
                setParam(textFieldParam3, "Choose window_size:", labelParam3, "(e.g. 3)");
            } else if ("Convert_a_sequence_database_to_SPMF_format".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose input format:", labelParam1, "(e.g. CSV_INTEGER)");
                setParam(textFieldParam2, "Choose sequence count:", labelParam2, "(e.g. 5)");
            } //Convert_a_transaction_database_to_SPMF_format
            else if ("Convert_a_transaction_database_to_SPMF_format".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose input format:", labelParam1, "(e.g. CSV_INTEGER)");
                setParam(textFieldParam2, "Choose sequence count:", labelParam2, "(e.g. 5)");
            } else if ("Generate_a_sequence_database".equals(algorithmName)
                    || "Generate_a_sequence_database_with_timestamps".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose sequence count:", labelParam1, "(e.g. 100)");
                setParam(textFieldParam2, "Choose max. distinct items:", labelParam2, "(e.g. 1000)");
                setParam(textFieldParam3, "Choose item count by itemset:", labelParam3, "(e.g. 3)");
                setParam(textFieldParam4, "Choose itemset count per sequence:", labelParam4, "(e.g. 7)");
                lblChooseInputFile.setVisible(false);
                buttonInput.setVisible(false);
                textFieldInput.setVisible(false);
            } else if ("Generate_a_transaction_database".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                setParam(textFieldParam1, "Choose transaction count:", labelParam1, "(e.g. 100)");
                setParam(textFieldParam2, "Choose max. distinct items:", labelParam2, "(e.g. 1000)");
                setParam(textFieldParam3, "Max. item count per transaction:", labelParam3, "(e.g. 10)");
                lblChooseInputFile.setVisible(false);
                buttonInput.setVisible(false);
                textFieldInput.setVisible(false);
            } else if ("Calculate_stats_for_a_sequence_database".equals(algorithmName)) {
                // show the parameters of this algorithm
                hideAllParams();
                lblSetOutputFile.setVisible(false);
                buttonOutput.setVisible(false);
                textFieldOutput.setVisible(false);
                checkboxOpenOutput.setVisible(false);
            } else {
                // This is for the command line version
                // If the name of the algorithm is not recognized:
                if (isVisible() == false) {
                    System.out.println("There is no algorithm with this name. "
                            + " To fix this problem, you may check the command syntax in the SPMF documentation"
                            + " and/or verify if there is a new version of SPMF on the SPMF website.");
                }

                hideAllParams();
                buttonRun.setEnabled(false);
                buttonExample.setEnabled(false);
            }
        } else {
            // if no algorithm is chosen, we hide all parameters.
            hideAllParams();
            buttonRun.setEnabled(false);
            buttonExample.setEnabled(false);
        }
	}

    private  void setParam(JTextField textfield, String name, JLabel label, String helpText) {
        label.setText(name);
        textfield.setEnabled(true);
        textfield.setVisible(true);
        label.setVisible(true);
        if (textfield == textFieldParam1) {
            lbHelp1.setText(helpText);
            lbHelp1.setVisible(true);
        } else if (textfield == textFieldParam2) {
            lbHelp2.setText(helpText);
            lbHelp2.setVisible(true);
        } else if (textfield == textFieldParam3) {
            lbHelp3.setText(helpText);
            lbHelp3.setVisible(true);
        } else if (textfield == textFieldParam4) {
            lbHelp4.setText(helpText);
            lbHelp4.setVisible(true);
        } else if (textfield == textFieldParam5) {
            lbHelp5.setText(helpText);
            lbHelp5.setVisible(true);
        } else if (textfield == textFieldParam6) {
            lbHelp6.setText(helpText);
            lbHelp6.setVisible(true);
        }
    }

//    private  static void setHelpTextForParam(JLabel label, String name) {
//        label.setText(name);
//        label.setVisible(true);
//    }

    /**
     * Method to convert a parameter given as a string to a double. For example,
     * convert something like "50%" to 0.5.
     *
     * @param value a string
     * @return a double
     */
    private  static double getParamAsDouble(String value) {
        if (value.contains("%")) {
            value = value.substring(0, value.length() - 1);
            return Double.parseDouble(value) / 100d;
        }
        return Double.parseDouble(value);
    }

    /**
     * Method to transform a string to an integer
     *
     * @param value a string
     * @return an integer
     */
    private static int getParamAsInteger(String value) {
        return Integer.parseInt(value);
    }

    /**
     * Method to get a parameter as a string. Note: this method just return the
     * string taken as parameter.
     *
     * @param value a string
     * @return a string
     */
    private static String getParamAsString(String value) {
        return value;
    }

    /**
     * Hide all parameters from the user interface. This is used to hide fields
     * when the user change algorithms or when the JFrame is first created.
     */
    private  void hideAllParams() {
        labelParam1.setVisible(false);
        labelParam2.setVisible(false);
        labelParam3.setVisible(false);
        labelParam4.setVisible(false);
        labelParam5.setVisible(false);
        labelParam6.setVisible(false);
//		.setVisible(false);
        lbHelp1.setVisible(false);
        lbHelp2.setVisible(false);
        lbHelp3.setVisible(false);
        lbHelp4.setVisible(false);
        lbHelp5.setVisible(false);
        lbHelp6.setVisible(false);
        textFieldParam1.setVisible(false);
        textFieldParam2.setVisible(false);
        textFieldParam3.setVisible(false);
        textFieldParam4.setVisible(false);
        textFieldParam5.setVisible(false);
        textFieldParam6.setVisible(false);

        lblSetOutputFile.setVisible(true);
        buttonOutput.setVisible(true);
        textFieldOutput.setVisible(true);
        lblChooseInputFile.setVisible(true);
        buttonInput.setVisible(true);
        textFieldInput.setVisible(true);


        checkboxOpenOutput.setVisible(true);
    }

    static class TextAreaOutputStream extends OutputStream {

        JTextArea textArea;

        public TextAreaOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        public void flush() {
            textArea.repaint();
        }

        public void write(int b) {
            textArea.append(new String(new byte[]{(byte) b}));
        }
    }

    private static class SwingAction extends AbstractAction {

        public SwingAction() {
            putValue(NAME, "SwingAction");
            putValue(SHORT_DESCRIPTION, "Some short description");
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    /**
     * This method open a URL in the default web browser.
     *
     * @param url : URL of the webpage
     */
    private void openWebPage(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method run an algorithm. It is called from the GUI interface or when
     * the user run the jar file from the command line.
     *
     * @param algorithmName the name of the algorithm
     * @param inputFile the input file for the algorithm
     * @param outputFile the output file for the algorithm
     * @param parameters the parameters of the algorithm
     * @return true if no error occured
     */
    private static boolean runAlgorithm(String algorithmName, String inputFile, String outputFile, String[] parameters) {
//		System.out.println("C" + algorithmName);

        try {
            // **** CHECK IF ARFF AS INPUT FILE *****
            // FIRST WE WILL CHECK IF IT IS AN ARFF FILE...
            // IF YES, WE WILL CONVERT IT TO SPMF FORMAT FIRST,
            // THEN WE WILL RUN THE ALGORITHM, AND FINALLY CONVERT THE RESULT SO THAT IT CAN
            // BE SHOWED TO THE USER.
            // This map is to store the mapping from ItemID to Attribute value for the conversion
            // from ARFF to SPMF.
            Map<Integer, String> mapItemAttributeValue = null;
            // This variable store the path of the original output file
            String originalOutputFile = null;
            // This variable store the path of the original input file
            String originalInputFile = null;
            // If the file is ARFF
            if (inputFile != null && (inputFile.endsWith(".arff") || inputFile.endsWith(".ARFF"))) {
                // Convert it
                TransactionDatabaseConverter converter = new TransactionDatabaseConverter();
                System.out.println("Converting ARFF to SPMF format.");
                // save the file paths selected by the user
                originalOutputFile = outputFile;
                originalInputFile = inputFile;
                // change the ouptut file path to a temporary file
                inputFile = inputFile + ".tmp";
                outputFile = outputFile + ".tmp";
                mapItemAttributeValue = converter.convertARFFandReturnMap(originalInputFile, inputFile, Integer.MAX_VALUE);
                System.out.println("Conversion completed.");
            }

            // ****** NEXT WE WILL APPLY THE DESIRED ALGORITHM ******
            // There is a if condition for each algorithm.
            // I will not describe them one by one because it is
            // straightforward.
            if ("PrefixSpan".equals(algorithmName)) {
                ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase();
                sequenceDatabase.loadFile(inputFile);
//				sequenceDatabase.print();
                int minsup = (int) Math.ceil((getParamAsDouble(parameters[0]) * sequenceDatabase.size())); // we use a minimum support of 2 sequences.

                AlgoPrefixSpan algo = new AlgoPrefixSpan();
                if ("".equals(parameters[1]) == false) {
                    algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
                }
                algo.runAlgorithm(sequenceDatabase, outputFile, minsup);
                algo.printStatistics(sequenceDatabase.size());
            } else if ("PrefixSpan_with_strings".equals(algorithmName)) {

                SequenceDatabase sequenceDatabase = new SequenceDatabase();
                sequenceDatabase.loadFile(inputFile);

                // Create an instance of the algorithm with minsup = 50 %
                AlgoPrefixSpan_with_Strings algo = new AlgoPrefixSpan_with_Strings();

                int minsup = (int) Math.ceil((getParamAsDouble(parameters[0]) * sequenceDatabase.size())); // we use a minimum support of 2 sequences.

                // execute the algorithm
                algo.runAlgorithm(sequenceDatabase, outputFile, minsup);
                algo.printStatistics(sequenceDatabase.size());
            } else if ("SeqDim_(PrefixSpan+Apriori)".equals(algorithmName)) {

                double minsup = getParamAsDouble(parameters[0]); // we use a minimum support of 2 sequences.

                MDSequenceDatabase contextMDDatabase = new MDSequenceDatabase(); //
                contextMDDatabase.loadFile(inputFile);
//				contextMDDatabase.printContext();

                // If the second boolean is true, the algorithm will use
                // CHARM instead of AprioriClose for mining frequent closed itemsets.
                // This options is offered because on some database, AprioriClose does not
                // perform very well. Other algorithms could be added.
                AlgoDim algoDim = new AlgoDim(false, false);

                AlgoSeqDim algoSeqDim = new AlgoSeqDim();

                // Apply algorithm
                AlgoPrefixSpanMDSPM prefixSpan = new AlgoPrefixSpanMDSPM(minsup);
                algoSeqDim.runAlgorithm(contextMDDatabase, prefixSpan, algoDim, false, outputFile);

                // Print results
                algoSeqDim.printStatistics(contextMDDatabase.size());
            } else if ("HirateYamana".equals(algorithmName)) {

                double minsup = getParamAsDouble(parameters[0]); // we use a minimum support of 2 sequences.
                double minInterval = getParamAsDouble(parameters[1]);
                double maxInterval = getParamAsDouble(parameters[2]);
                double minWholeInterval = getParamAsDouble(parameters[3]);
                double maxWholeInterval = getParamAsDouble(parameters[4]);

                ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.SequenceDatabase database = new ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.SequenceDatabase();
                database.loadFile(inputFile);

                // Apply algorithm
                AlgoFournierViger08 algo = new AlgoFournierViger08(minsup,
                        minInterval, maxInterval, minWholeInterval, maxWholeInterval, null, false, false);

                algo.runAlgorithm(database, outputFile);

                algo.printStatistics();
                // NOTE : IF YOU DON'T WANT TO MINE *CLOSED* MD-SEQUENCES, JUST CHANGE THE FOUR VALUES "true" for
                // "FALSE" in this example. 
            } else if ("SeqDim_(PrefixSpan+Apriori)+time".equals(algorithmName)) {

                double minsup = getParamAsDouble(parameters[0]); // we use a minimum support of 2 sequences.
                double minInterval = getParamAsDouble(parameters[1]);
                double maxInterval = getParamAsDouble(parameters[2]);
                double minWholeInterval = getParamAsDouble(parameters[3]);
                double maxWholeInterval = getParamAsDouble(parameters[4]);


                MDSequenceDatabase contextMDDatabase = new MDSequenceDatabase(); //
                contextMDDatabase.loadFile(inputFile);
//				contextMDDatabase.printContext();

                AlgoDim algoDim = new AlgoDim(false, false); // <-- here

                AlgoSeqDim algoSeqDim2 = new AlgoSeqDim();

                // Apply algorithm
                AlgoFournierViger08 algoPrefixSpanHirateClustering = new AlgoFournierViger08(minsup,
                        minInterval, maxInterval, minWholeInterval, maxWholeInterval, null, false, false);
                algoSeqDim2.runAlgorithm(contextMDDatabase, algoPrefixSpanHirateClustering, algoDim, false, outputFile);

                // Print results
                algoSeqDim2.printStatistics(contextMDDatabase.size());
                // NOTE : IF YOU DON'T WANT TO MINE *CLOSED* MD-SEQUENCES, JUST CHANGE THE FOUR VALUES "true" for
                // "FALSE" in this example. 
            } else if ("SeqDim_(BIDE+AprioriClose)+time".equals(algorithmName)) {

                double minsup = getParamAsDouble(parameters[0]); // we use a minimum support of 2 sequences.
                double minInterval = getParamAsDouble(parameters[1]);
                double maxInterval = getParamAsDouble(parameters[2]);
                double minWholeInterval = getParamAsDouble(parameters[3]);
                double maxWholeInterval = getParamAsDouble(parameters[4]);


                MDSequenceDatabase contextMDDatabase = new MDSequenceDatabase(); //
                contextMDDatabase.loadFile(inputFile);
//				contextMDDatabase.printContext();

                AlgoDim algoDim = new AlgoDim(true, false); // <-- here

                AlgoSeqDim algoSeqDim2 = new AlgoSeqDim();

                // Apply algorithm
                AlgoFournierViger08 algoPrefixSpanHirateClustering = new AlgoFournierViger08(minsup,
                        minInterval, maxInterval, minWholeInterval, maxWholeInterval, null, true, true);
                algoSeqDim2.runAlgorithm(contextMDDatabase, algoPrefixSpanHirateClustering, algoDim, true, outputFile);

                // Print results
                algoSeqDim2.printStatistics(contextMDDatabase.size());
                // NOTE : IF YOU DON'T WANT TO MINE *CLOSED* MD-SEQUENCES, JUST CHANGE THE FOUR VALUES "true" for
                // "FALSE" in this example. 
            } else if ("SeqDim_(BIDE+Charm)+time".equals(algorithmName)) {

                double minsup = getParamAsDouble(parameters[0]); // we use a minimum support of 2 sequences.
                double minInterval = getParamAsDouble(parameters[1]);
                double maxInterval = getParamAsDouble(parameters[2]);
                double minWholeInterval = getParamAsDouble(parameters[3]);
                double maxWholeInterval = getParamAsDouble(parameters[4]);


                MDSequenceDatabase contextMDDatabase = new MDSequenceDatabase(); //
                contextMDDatabase.loadFile(inputFile);
//				contextMDDatabase.printContext();

                AlgoDim algoDim = new AlgoDim(false, true); // <-- here

                AlgoSeqDim algoSeqDim2 = new AlgoSeqDim();

                // Apply algorithm
                AlgoFournierViger08 algoPrefixSpanHirateClustering = new AlgoFournierViger08(minsup,
                        minInterval, maxInterval, minWholeInterval, maxWholeInterval, null, true, true);
                algoSeqDim2.runAlgorithm(contextMDDatabase, algoPrefixSpanHirateClustering, algoDim, true, outputFile);

                // Print results
                algoSeqDim2.printStatistics(contextMDDatabase.size());
                // NOTE : IF YOU DON'T WANT TO MINE *CLOSED* MD-SEQUENCES, JUST CHANGE THE FOUR VALUES "true" for
                // "FALSE" in this example. 
            } else if ("SeqDim_(BIDE+AprioriClose)".equals(algorithmName)) {

                double minsup = getParamAsDouble(parameters[0]); // we use a minimum support of 2 sequences.

                MDSequenceDatabase contextMDDatabase = new MDSequenceDatabase(); //
                contextMDDatabase.loadFile(inputFile);
//				contextMDDatabase.printContext();

                AlgoDim algoDim = new AlgoDim(true, false);

                AlgoSeqDim algoSeqDim = new AlgoSeqDim();

                // Apply algorithm
                ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoBIDEPlus bideplus = new ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoBIDEPlus(minsup);
                algoSeqDim.runAlgorithm(contextMDDatabase, bideplus, algoDim, true, outputFile);

                // Print results
                algoSeqDim.printStatistics(contextMDDatabase.size());
            } else if ("SeqDim_(BIDE+Charm)".equals(algorithmName)) {

                double minsup = getParamAsDouble(parameters[0]); // we use a minimum support of 2 sequences.

                MDSequenceDatabase contextMDDatabase = new MDSequenceDatabase(); //
                contextMDDatabase.loadFile(inputFile);
//				contextMDDatabase.printContext();

                AlgoDim algoDim = new AlgoDim(false, true);

                AlgoSeqDim algoSeqDim = new AlgoSeqDim();

                // Apply algorithm
                ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoBIDEPlus bideplus = new ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoBIDEPlus(minsup);
                algoSeqDim.runAlgorithm(contextMDDatabase, bideplus, algoDim, true, outputFile);

                // Print results
                algoSeqDim.printStatistics(contextMDDatabase.size());
            } else if ("SPAM".equals(algorithmName)) {
                AlgoSPAM algo = new AlgoSPAM();
                if ("".equals(parameters[1]) == false) {
                    algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
                }
                algo.runAlgorithm(inputFile, outputFile, getParamAsDouble(parameters[0]));
                algo.printStatistics(); 
            } else if ("CM-SPAM".equals(algorithmName)) {
            	AlgoCMSPAM algo = new AlgoCMSPAM();
                if ("".equals(parameters[1]) == false) {
                    algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
                }
        		
        		// execute the algorithm with minsup = 2 sequences  (50 %)
        		algo.runAlgorithm(inputFile, outputFile, getParamAsDouble(parameters[0]));     // minsup = 106   k = 1000   BMS
        		algo.printStatistics();
            } else if ("VMSP".equals(algorithmName)) {
            	AlgoVMSP algo = new AlgoVMSP();
                if ("".equals(parameters[1]) == false) {
                    algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
                }
        		
        		// execute the algorithm with minsup = 2 sequences  (50 %)
        		algo.runAlgorithm(inputFile, outputFile, getParamAsDouble(parameters[0]));     // minsup = 106   k = 1000   BMS
        		algo.printStatistics();
            }else if ("FEAT".equals(algorithmName)) {
            	AlgoFEAT algo = new AlgoFEAT();
                if ("".equals(parameters[1]) == false) {
                    algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
                }
                ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase();
                sequenceDatabase.loadFile(inputFile);
                int minsup = (int) (getParamAsDouble(parameters[0]) * sequenceDatabase.size()); // we use a minimum support of 2 sequences.
                
                algo.runAlgorithm(sequenceDatabase, outputFile, minsup); 
                algo.printStatistics(sequenceDatabase.size());
            }else if ("FSGP".equals(algorithmName)) {
            	AlgoFSGP algo = new AlgoFSGP();
                if ("".equals(parameters[1]) == false) {
                    algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
                }
                
                ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase();
                sequenceDatabase.loadFile(inputFile);
                int minsup = (int) (getParamAsDouble(parameters[0]) * sequenceDatabase.size()); // we use a minimum support of 2 sequences.
                
                algo.runAlgorithm(sequenceDatabase, outputFile, minsup, true); // PERFORM PRUNING ACTIVATED 
                algo.printStatistics(sequenceDatabase.size());
            }else if ("VGEN".equals(algorithmName)) {
            	AlgoVGEN algo = new AlgoVGEN();
                if ("".equals(parameters[1]) == false) {
                    algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
                }

        		// execute the algorithm 
        		algo.runAlgorithm(inputFile, outputFile, getParamAsDouble(parameters[0])); 
        		algo.printStatistics();
            }else if ("LAPIN".equals(algorithmName)) {
            	AlgoLAPIN_LCI algo = new AlgoLAPIN_LCI();
        		// execute the algorithm 
        		algo.runAlgorithm(inputFile, outputFile, getParamAsDouble(parameters[0])); 
        		algo.printStatistics();
            }                   
            else if ("GSP".equals(algorithmName)) {
                AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
                double minSupport = getParamAsDouble(parameters[0]);

                AlgoGSP algo = new AlgoGSP(minSupport, 0, Integer.MAX_VALUE, 0, abstractionCreator);
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */
                ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.SequenceDatabase(abstractionCreator);
                sd.loadFile(inputFile, minSupport);

                algo.runAlgorithm(sd, true, false, outputFile);
                System.out.println(algo.printStatistics());
            }else if ("PrefixSpan_AGP".equals(algorithmName)) {
                ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.creators.AbstractionCreator abstractionCreator = ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.creators.AbstractionCreator_Qualitative.getInstance();
                double minSupport = getParamAsDouble(parameters[0]);

                AlgoPrefixSpan_AGP algo = new AlgoPrefixSpan_AGP(minSupport, abstractionCreator);
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */
                ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.SequenceDatabase(abstractionCreator);
                sd.loadFile(inputFile, minSupport);

                algo.runAlgorithm(sd, true, false, outputFile);
                System.out.println(algo.printStatistics());
            }else if ("SPADE".equals(algorithmName)) {
                ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator abstractionCreator = ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator_Qualitative.getInstance();
                IdListCreator idListCreator = IdListCreator_FatBitmap.getInstance();
                CandidateGenerator candidateGenerator = CandidateGenerator_Qualitative.getInstance();
                
                double minSupport = getParamAsDouble(parameters[0]);

                AlgoSPADE algo = new AlgoSPADE(minSupport,true,abstractionCreator);
                
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */
                ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase(abstractionCreator, idListCreator);
                sd.loadFile(inputFile, minSupport);

                algo.runAlgorithm(sd,candidateGenerator,true, false, outputFile);
                System.out.println(algo.printStatistics());
            }else if ("SPADE_Parallelized".equals(algorithmName)) {
                ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator abstractionCreator = ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator_Qualitative.getInstance();
                IdListCreator idListCreator = IdListCreator_FatBitmap.getInstance();
                CandidateGenerator candidateGenerator = CandidateGenerator_Qualitative.getInstance();
                
                double minSupport = getParamAsDouble(parameters[0]);

                AlgoSPADE algo = new AlgoSPADE(minSupport,true,abstractionCreator);
                
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */
                ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase(abstractionCreator, idListCreator);
                sd.loadFile(inputFile, minSupport);

                algo.runAlgorithmParallelized(sd,candidateGenerator,true, false, outputFile);
                System.out.println(algo.printStatistics());
            }else if ("CM-SPADE".equals(algorithmName)) {
                ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator abstractionCreator = ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator_Qualitative.getInstance();
                IdListCreator idListCreator = IdListCreator_FatBitmap.getInstance();
                CandidateGenerator candidateGenerator = CandidateGenerator_Qualitative.getInstance();
                
                double minSupport = getParamAsDouble(parameters[0]);

                AlgoCMSPADE algo = new AlgoCMSPADE(minSupport,true,abstractionCreator);
                
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */
                ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase(abstractionCreator, idListCreator);
                sd.loadFile(inputFile, minSupport);

                algo.runAlgorithm(sd,candidateGenerator,true, false, outputFile);
                System.out.println(algo.printStatistics());
            }
            else if ("SPAM_AGP".equals(algorithmName)) {
                ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator abstractionCreator = ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator_Qualitative.getInstance();
                IdListCreator idListCreator = IdListCreator_FatBitmap.getInstance();
                CandidateGenerator candidateGenerator = CandidateGenerator_Qualitative.getInstance();
                
                double minSupport = getParamAsDouble(parameters[0]);

                ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.AlgoSPAM_AGP algo = new ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.AlgoSPAM_AGP(minSupport);
                
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */
                ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase(abstractionCreator, idListCreator);
                sd.loadFile(inputFile, minSupport);

                algo.runAlgorithm(sd,true, false, outputFile);
                System.out.println(algo.printStatistics());
            } else if ("SPAM_PostProcessingClosed".equals(algorithmName)) {                
                double minSupport = getParamAsDouble(parameters[0]);
                
                ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator abstractionCreator = ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator_Qualitative.getInstance();
                ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreator idListCreator = ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreatorStandard_Map.getInstance();
                ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase(abstractionCreator, idListCreator);
                
                double relativeMinSup=sd.loadFile(inputFile, minSupport);

                AlgoClaSP algo = new AlgoClaSP(relativeMinSup,abstractionCreator,true,false);
                
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */                

                algo.runAlgorithm(sd,true, false, outputFile);
                System.out.println(algo.printStatistics());
            } else if ("ClaSP".equals(algorithmName)) {
                double minSupport = getParamAsDouble(parameters[0]);
                
                ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator abstractionCreator = ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator_Qualitative.getInstance();
                ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreator idListCreator = ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreatorStandard_Map.getInstance();
                ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase(abstractionCreator, idListCreator);
                
                double relativeMinSup=sd.loadFile(inputFile, minSupport);

                AlgoClaSP algo = new AlgoClaSP(relativeMinSup,abstractionCreator,true,true);
                
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */                

                algo.runAlgorithm(sd,true, false, outputFile);
                System.out.println(algo.printStatistics());
            }else if ("CM-ClaSP".equals(algorithmName)) {
                double minSupport = getParamAsDouble(parameters[0]);
                
                ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator abstractionCreator = ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.creators.AbstractionCreator_Qualitative.getInstance();
                ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreator idListCreator = ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.creators.IdListCreatorStandard_Map.getInstance();
                ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.database.SequenceDatabase(abstractionCreator, idListCreator);
                
                double relativeMinSup=sd.loadFile(inputFile, minSupport);

                AlgoCM_ClaSP algo = new AlgoCM_ClaSP(relativeMinSup,abstractionCreator,true,true);
                
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */                

                algo.runAlgorithm(sd,true, false, outputFile);
                System.out.println(algo.printStatistics());
            } else if ("PrefixSpan_PostProcessingClosed".equals(algorithmName)) {
                ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator abstractionCreator = ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator_Qualitative.getInstance();
                
                double minSupport = getParamAsDouble(parameters[0]);

                AlgoCloSpan algo = new AlgoCloSpan(minSupport,abstractionCreator,true,false);
                
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */
                ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.SequenceDatabase();
                sd.loadFile(inputFile, minSupport);

                algo.runAlgorithm(sd,true, false, outputFile);
                System.out.println(algo.printStatistics());
            } else if ("CloSpan".equals(algorithmName)) {
                ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator abstractionCreator = ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator_Qualitative.getInstance();
                
                double minSupport = getParamAsDouble(parameters[0]);

                AlgoCloSpan algo = new AlgoCloSpan(minSupport,abstractionCreator,true,true);
                
                /*
                 * if("".equals(parameters[1]) == false){
                 * algo.setMaximumPatternLength(getParamAsInteger(parameters[1]));
				}
                 */
                ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.SequenceDatabase sd = new ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.SequenceDatabase();
                sd.loadFile(inputFile, minSupport);

                algo.runAlgorithm(sd,true, false, outputFile);
                System.out.println(algo.printStatistics());
                
                /////////////////////////////////////////////////////////////end adding by Antonio Gomariz//////////////////////////////////////////////////////////
                
            } else if ("BIDE+".equals(algorithmName)) {
                ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase();
                sequenceDatabase.loadFile(inputFile);
//				sequenceDatabase.print();
                int minsup = (int) Math.ceil(getParamAsDouble(parameters[0]) * sequenceDatabase.size()); // we use a minimum support of 2 sequences.

                AlgoBIDEPlus algo = new AlgoBIDEPlus();
                algo.runAlgorithm(sequenceDatabase, outputFile, minsup);
                algo.printStatistics(sequenceDatabase.size());
            } else if ("BIDE+_with_strings".equals(algorithmName)) {
                SequenceDatabase sequenceDatabase = new SequenceDatabase();
                sequenceDatabase.loadFile(inputFile);
//				sequenceDatabase.print();
                int minsup = (int) Math.ceil((getParamAsDouble(parameters[0]) * sequenceDatabase.size())); // we use a minimum support of 2 sequences.

                ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan_with_strings.AlgoBIDEPlus_withStrings algo = new ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan_with_strings.AlgoBIDEPlus_withStrings();
                algo.runAlgorithm(sequenceDatabase, outputFile, minsup);
                algo.printStatistics(sequenceDatabase.size());
            } else if ("RuleGrowth".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);

                AlgoRULEGROWTH algo = new AlgoRULEGROWTH();
                algo.runAlgorithm(minsup, minconf, inputFile, outputFile);
                algo.printStats();
            } else if ("TRuleGrowth".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);
                int window = getParamAsInteger(parameters[2]);

                AlgoTRuleGrowth algo = new AlgoTRuleGrowth();
                algo.runAlgorithm(minsup, minconf, inputFile, outputFile, window);
                algo.printStats();
            } else if ("TRuleGrowth_with_strings".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);
                int window = getParamAsInteger(parameters[2]);

                AlgoTRuleGrowth_withStrings algo = new AlgoTRuleGrowth_withStrings();
                algo.runAlgorithm(minsup, minconf, inputFile, outputFile, window);
                algo.printStats();
            } else if ("CMRules".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);

                AlgoCMRules algo = new AlgoCMRules();
                algo.runAlgorithm(inputFile, outputFile, minsup, minconf);
                algo.printStats();
            } else if ("CMDeo".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);
                AlgoCMDeogun algo = new AlgoCMDeogun();
                algo.runAlgorithm(inputFile, outputFile, minsup, minconf);
                algo.printStats();
            } else if ("RuleGen".equals(algorithmName)) {
                int minsup = getParamAsInteger(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);

                AlgoRuleGen rulegen = new AlgoRuleGen();
                rulegen.runAlgorithm(minsup, minconf, inputFile, outputFile);
                rulegen.printStats();

            } else if ("TopSeqRules".equals(algorithmName)) {
                int k = getParamAsInteger(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);

                ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase();
                sequenceDatabase.loadFile(inputFile);

                AlgoTopSeqRules algo = new AlgoTopSeqRules();
                algo.runAlgorithm(k, sequenceDatabase, minconf);
                algo.printStats();
                algo.writeResultTofile(outputFile);   // to save results to file
            }else if ("TKS".equals(algorithmName)) {
                int k = getParamAsInteger(parameters[0]);

        		AlgoTKS algo = new AlgoTKS(); 
        		
        		// execute the algorithm
        		algo.runAlgorithm(inputFile, outputFile, k);    
        		algo.writeResultTofile(outputFile);   // to save results to file
        		algo.printStatistics();
            }else if ("TSP_nonClosed".equals(algorithmName)) {
                int k = getParamAsInteger(parameters[0]);
                
                ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase sequenceDatabase = new ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase(); 
        		sequenceDatabase.loadFile(inputFile);

                AlgoTSP_nonClosed algo = new AlgoTSP_nonClosed(); 
        		
        		// execute the algorithm
        		algo.runAlgorithm(sequenceDatabase, k);    
        		algo.writeResultTofile(outputFile);   // to save results to file
        		algo.printStatistics(sequenceDatabase.size());
            }
            else if ("TopKRules".equals(algorithmName)) {
                Database database = new Database();
                database.loadFile(inputFile);

                int k = getParamAsInteger(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);

                ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.AlgoTopKRules algo = new ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.AlgoTopKRules();
                algo.runAlgorithm(k, minconf, database);
                algo.printStats();
                algo.writeResultTofile(outputFile);   // to save results to file
            } else if ("TNR".equals(algorithmName)) {
                ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.Database database = new ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.Database();
                database.loadFile(inputFile);

                int k = getParamAsInteger(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);
                int delta = getParamAsInteger(parameters[2]);

                ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.AlgoTNR algo = new ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.AlgoTNR();
                algo.runAlgorithm(k, minconf, database, delta);
                algo.printStats();
                algo.writeResultTofile(outputFile);   // to save results to file
            } else if ("TNS".equals(algorithmName)) {
                // Load database into memory
                ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase database = new ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase();
                database.loadFile(inputFile);

                int k = getParamAsInteger(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);
                int delta = getParamAsInteger(parameters[2]);

                AlgoTNS algo = new AlgoTNS();
                algo.runAlgorithm(k, database, minconf, delta);
                algo.printStats();
                algo.writeResultTofile(outputFile);   // to save results to file

            } else if ("FPGrowth_itemsets".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                AlgoFPGrowth algo = new AlgoFPGrowth();
                algo.runAlgorithm(inputFile, outputFile, minsup);
                algo.printStats();
            } else if ("FPGrowth_itemsets_with_strings".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth_with_strings.AlgoFPGrowth_Strings algo = new ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth_with_strings.AlgoFPGrowth_Strings();
                algo.runAlgorithm(inputFile, outputFile, minsup);
                algo.printStats();
            } else if ("Apriori_association_rules".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);


                AlgoApriori apriori = new AlgoApriori();
                ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets patterns = apriori.runAlgorithm(minsup, inputFile, null);
                apriori.printStats();
                int databaseSize = apriori.getDatabaseSize();

                // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
                ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94 algoAgrawal = new ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94();
                algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize, minconf);
                algoAgrawal.printStats();
            } //Sporadic_association_rules
            else if ("Sporadic_association_rules".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double maxsup = getParamAsDouble(parameters[1]);
                double minconf = getParamAsDouble(parameters[2]);

                AlgoAprioriInverse apriori = new AlgoAprioriInverse();
                ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets patterns = apriori.runAlgorithm(minsup, maxsup, inputFile, null);
                apriori.printStats();
                int databaseSize = apriori.getDatabaseSize();

                // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
                ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94 algoAgrawal = new ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94();
                algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize, minconf);
                algoAgrawal.printStats();
            } else if ("Closed_association_rules".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);

                // Loading the transaction database
                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//				database.printDatabase();

                // STEP 1: Applying the Charm algorithm to find frequent closed itemsets
                AlgoCharm_Bitset algo = new AlgoCharm_Bitset();
                ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemsets patterns = algo.runAlgorithm(null, database, minsup, true, 10000);
                algo.printStats();
                
                // STEP 2: Generate all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
                AlgoClosedRules algoAgrawal = new AlgoClosedRules();
                algoAgrawal.runAlgorithm(patterns, outputFile, database.size(), minconf);
                algoAgrawal.printStats();
            } // IGB
            else if ("IGB".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);

                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Applying the Zart algorithm
                AlgoZart zart = new AlgoZart();
                TZTableClosed results = zart.runAlgorithm(database, minsup);
                zart.printStatistics();
                // Generate IGB association rules
                AlgoIGB algoIGB = new AlgoIGB();
                algoIGB.runAlgorithm(results, database.getTransactions().size(), minconf, outputFile);
                algoIGB.printStatistics();
            } // Indirect_association_rules
            else if ("Indirect_association_rules".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double ts = getParamAsDouble(parameters[1]);
                double minconf = getParamAsDouble(parameters[2]);

                AlgoINDIRECT indirect = new AlgoINDIRECT();
                indirect.runAlgorithm(inputFile, outputFile, minsup, ts, minconf);
                indirect.printStats();
            }// MNR
            else if ("MNR".equals(algorithmName)) {

                System.out.println("STEP 1: APPLY ZART TO FIND CLOSED ITEMSETS AND GENERATORS");
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);
                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Applying the Zart algorithm
                AlgoZart zart = new AlgoZart();
                TZTableClosed results = zart.runAlgorithm(database, minsup);
                zart.printStatistics();

                System.out.println("STEP 2 : CALCULATING MNR ASSOCIATION RULES");
                // Run the algorithm to generate MNR rules
                AlgoMNRRules algoMNR = new AlgoMNRRules();
                algoMNR.runAlgorithm(outputFile, minconf, results, database.size());
                algoMNR.printStatistics();
            } else if ("FPGrowth_association_rules".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);


                ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth fpgrowth = new ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth();
                ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets patterns = fpgrowth.runAlgorithm(inputFile, null, minsup);
                fpgrowth.printStats();
                int databaseSize = fpgrowth.getDatabaseSize();

                // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
                ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94 algoAgrawal = new ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94();
                algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize, minconf);
                algoAgrawal.printStats();
            } else if ("FPGrowth_association_rules_with_lift".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);
                double minlift = getParamAsDouble(parameters[2]);

                ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth fpgrowth = new ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth();
                ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets patterns = fpgrowth.runAlgorithm(inputFile, null, minsup);
                fpgrowth.printStats();
                int databaseSize = fpgrowth.getDatabaseSize();

                // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
                ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94 algoAgrawal = new ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94();
                algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize, minconf, minlift);
                algoAgrawal.printStats();
            }else if ("CFPGrowth++_association_rules".equals(algorithmName)) {
            	String misFile = parameters[0];
                double minconf = getParamAsDouble(parameters[1]);

                File file = new File(inputFile);
                String misFileFullPath;
                if (file.getParent() == null) {
                	misFileFullPath = misFile;
                } else {
                	misFileFullPath = file.getParent() + File.separator + misFile;
                }

                AlgoCFPGrowth cfpgrowth = new AlgoCFPGrowth();
                ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets patterns = cfpgrowth.runAlgorithm(inputFile, null, misFileFullPath);
                cfpgrowth.printStats();
                int databaseSize = cfpgrowth.getDatabaseSize();

                // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
                ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94 algoAgrawal = new ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94();
                algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize, minconf);
                algoAgrawal.printStats();
            } else if ("CFPGrowth++_association_rules_with_lift".equals(algorithmName)) {
                String misFile = parameters[0];
                double minconf = getParamAsDouble(parameters[1]);
                double minlift = getParamAsDouble(parameters[2]);

                File file = new File(inputFile);
                String misFileFullPath;
                if (file.getParent() == null) {
                	misFileFullPath = misFile;
                } else {
                	misFileFullPath = file.getParent() + File.separator + misFile;
                }

                AlgoCFPGrowth cfpgrowth = new AlgoCFPGrowth();
                ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets patterns = cfpgrowth.runAlgorithm(inputFile, null, misFileFullPath);
                cfpgrowth.printStats();
                int databaseSize = cfpgrowth.getDatabaseSize();

                // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
                ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94 algoAgrawal = new ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94();
                algoAgrawal.runAlgorithm(patterns, outputFile, databaseSize, minconf, minlift);
                algoAgrawal.printStats();
            } 
            
            
            
//            else if ("CFPGrowth".equals(algorithmName)) {

//
//                // Applying the  algorithm
//                AlgoCFPGrowth algo = new AlgoCFPGrowth();
//                algo.runAlgorithm(inputFile, outputFile, misFileFullPath);
//                algo.printStats();
            
            
            
            else if ("Apriori_TID_bitset".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                AlgoAprioriTID apriori = new AlgoAprioriTID();
                apriori.runAlgorithm(inputFile, outputFile, minsup);
                apriori.printStats();
            } else if ("Apriori".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                // Applying the Apriori algorithm, optimized version
                ca.pfv.spmf.algorithms.frequentpatterns.apriori.AlgoApriori apriori = new ca.pfv.spmf.algorithms.frequentpatterns.apriori.AlgoApriori();
                apriori.runAlgorithm(minsup, inputFile, outputFile);
                apriori.printStats();
            } else if ("Apriori_with_hash_tree".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                int branch_count = getParamAsInteger(parameters[1]);

                // Applying the Apriori algorithm, optimized version
                AlgoAprioriHT apriori = new AlgoAprioriHT();
                apriori.runAlgorithm(minsup, inputFile, outputFile, branch_count);
                apriori.printStats();
            } else if ("AprioriClose".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                AlgoAprioriClose apriori = new AlgoAprioriClose();
                apriori.runAlgorithm(minsup, inputFile, outputFile);
                apriori.printStats();
            } else if ("Apriori_TID".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                AlgoAprioriTID apriori = new AlgoAprioriTID();
                apriori.runAlgorithm(inputFile, outputFile, minsup);
                apriori.printStats();
            } else if ("Apriori_TIDClose".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                TransactionDatabase database = new TransactionDatabase();
                database.loadFile(inputFile);
                AlgoAprioriTIDClose apriori = new AlgoAprioriTIDClose();
                apriori.runAlgorithm(database, minsup, outputFile);
                apriori.printStats();
            } else if ("AprioriRare".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                AlgoAprioriRare apriori2 = new AlgoAprioriRare();
                // apply the algorithm
                apriori2.runAlgorithm(minsup, inputFile, outputFile);
                apriori2.printStats();
            } else if ("AprioriInverse".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double maxsup = getParamAsDouble(parameters[1]);

                AlgoAprioriInverse apriori = new AlgoAprioriInverse();
                apriori.runAlgorithm(minsup, maxsup, inputFile, outputFile);
                apriori.printStats();
            } else if ("MSApriori".equals(algorithmName)) {
                double beta = getParamAsDouble(parameters[0]);
                double ls = getParamAsDouble(parameters[1]);

                // Applying the MSApriori algorithm
                AlgoMSApriori apriori = new AlgoMSApriori();
                apriori.runAlgorithm(inputFile, outputFile, beta, ls);
                apriori.printStats();
            } else if ("CFPGrowth++".equals(algorithmName)) {
                String misFile = parameters[0];

                File file = new File(inputFile);
                String misFileFullPath;
                if (file.getParent() == null) {
                    misFileFullPath = misFile;
                } else {
                    misFileFullPath = file.getParent() + File.separator + misFile;
                }

                // Applying the  algorithm
                AlgoCFPGrowth algo = new AlgoCFPGrowth();
                algo.runAlgorithm(inputFile, outputFile, misFileFullPath);
                algo.printStats();
            } else if ("FHSAR".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                double minconf = getParamAsDouble(parameters[1]);
                // file for sensitive
                String sarFile = parameters[2];

                File file = new File(inputFile);
                String sarFileFullPath;
                if (file.getParent() == null) {
                    sarFileFullPath = sarFile;
                } else {
                    sarFileFullPath = file.getParent() + File.separator + sarFile;
                }

                // STEP 1: Applying the FHSAR algorithm to hide association rules
                AlgoFHSAR algorithm = new AlgoFHSAR();
                algorithm.runAlgorithm(inputFile, sarFileFullPath, outputFile, minsup, minconf);
                algorithm.printStats();
            }else if ("GoKrimp".equals(algorithmName)) {
                // file for sensitive
                String labelFilePath = parameters[0];
                if(labelFilePath == null) {
                	labelFilePath = "";
                }else {
                    File file = new File(inputFile);
                    if (file.getParent() == null) {
                    	labelFilePath = parameters[0];
                    } else {
                    	labelFilePath = file.getParent() + File.separator + parameters[0];
                    }
                }

                DataReader d=new DataReader();
                AlgoGoKrimp g=d.readData_SPMF(inputFile, labelFilePath);
                g.setOutputFilePath(outputFile); // if not set, then result will be printed to console
                g.gokrimp();
            }  
            else if ("VME".equals(algorithmName)) {
                double threshold = getParamAsDouble(parameters[0]);

                // Applying the  algorithm
                AlgoVME algo = new AlgoVME();
                algo.runAlgorithm(inputFile, outputFile, threshold);
                algo.printStats();
            } else if ("KMeans".equals(algorithmName)) {
                int k = getParamAsInteger(parameters[0]);
                // Apply the algorithm
                AlgoKMeans algoKMeans = new AlgoKMeans();
                algoKMeans.runAlgorithm(inputFile, k);
                algoKMeans.printStatistics();
                algoKMeans.saveToFile(outputFile);
            } else if ("Hierarchical_clustering".equals(algorithmName)) {
                int maxDistance = getParamAsInteger(parameters[0]);

                // Apply the algorithm
                AlgoHierarchicalClustering algo = new AlgoHierarchicalClustering();
                algo.runAlgorithm(inputFile, maxDistance);
                algo.printStatistics();
                algo.saveToFile(outputFile);
            } else if ("UApriori".equals(algorithmName)) {
                double expectedsup = getParamAsDouble(parameters[0]);

                ca.pfv.spmf.algorithms.frequentpatterns.uapriori.UncertainTransactionDatabase context = new ca.pfv.spmf.algorithms.frequentpatterns.uapriori.UncertainTransactionDatabase();
                context.loadFile(inputFile);
                AlgoUApriori apriori = new AlgoUApriori(context);
                apriori.runAlgorithm(expectedsup, outputFile);
                apriori.printStats();
            } else if ("HMine".equals(algorithmName)) {
                int minsup = getParamAsInteger(parameters[0]);
                AlgoHMine algorithm = new AlgoHMine();
                algorithm.runAlgorithm(inputFile, outputFile, minsup);
                algorithm.printStatistics();
            } else if ("DCI_Closed".equals(algorithmName)) {
                int minsup = getParamAsInteger(parameters[0]);
                AlgoDCI_Closed_Optimized algorithm = new AlgoDCI_Closed_Optimized();
                algorithm.runAlgorithm(inputFile, outputFile, minsup);
            } else if ("DefMe".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                AlgoDefMe algorithm = new AlgoDefMe();
                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                algorithm.runAlgorithm(outputFile, database, minsup); 
                algorithm.printStats();
            }  else if ("Charm_bitset".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                AlgoCharm_Bitset algo = new AlgoCharm_Bitset();
                
                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                algo.runAlgorithm(outputFile, database,  minsup, true, 10000);
                algo.printStats();
            }else if ("dCharm_bitset".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);
                AlgoDCharm_Bitset algo = new AlgoDCharm_Bitset();
                
                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                algo.runAlgorithm(outputFile, database,  minsup, true, 10000);
                algo.printStats();
            } else if ("Charm_MFI".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                // Loading the binary context
                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//				database.printDatabase();

                // Applying the Charm algorithm
                AlgoCharm_Bitset charm = new AlgoCharm_Bitset();
                charm.runAlgorithm(null, database, minsup, false, 10000);

                // Run CHARM MFI
                AlgoCharmMFI charmMFI = new AlgoCharmMFI();
                charmMFI.runAlgorithm(outputFile, charm.getClosedItemsets());
                charmMFI.printStats(database.size());
            } else if ("Eclat".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                // Loading the transaction database
                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                AlgoEclat algo = new AlgoEclat();
                algo.runAlgorithm(outputFile, database, minsup, true); 
                algo.printStats();
            }  else if ("dEclat".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                // Loading the transaction database
                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                AlgoDEclat algo = new AlgoDEclat();
                algo.runAlgorithm(outputFile, database, minsup, true); 
                algo.printStats();
            } else if ("Relim".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                // Applying the RELIM algorithm
                AlgoRelim algo = new AlgoRelim();
                algo.runAlgorithm(minsup, inputFile, outputFile);
                algo.printStatistics();
            } else if ("Eclat_bitset".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                // Loading the transaction database
                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                AlgoEclat_Bitset algo = new AlgoEclat_Bitset();
                algo.runAlgorithm(outputFile, database, minsup, true); 
                algo.printStats();
            }  else if ("dEclat_bitset".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                // Loading the transaction database
                TransactionDatabase database = new TransactionDatabase();
                try {
                    database.loadFile(inputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                AlgoDEclat_Bitset algo = new AlgoDEclat_Bitset();
                algo.runAlgorithm(outputFile, database, minsup, true); 
                algo.printStats();
            } else if ("Two-Phase".equals(algorithmName)) {
                int minutil = getParamAsInteger(parameters[0]);
                ca.pfv.spmf.algorithms.frequentpatterns.two_phase.UtilityTransactionDatabaseTP database = new ca.pfv.spmf.algorithms.frequentpatterns.two_phase.UtilityTransactionDatabaseTP();
                database.loadFile(inputFile);

                // Applying the Two-Phase algorithm
                AlgoTwoPhase twoPhase = new AlgoTwoPhase();
                ca.pfv.spmf.algorithms.frequentpatterns.two_phase.ItemsetsTP highUtilityItemsets = twoPhase.runAlgorithm(database, minutil);

                highUtilityItemsets.saveResultsToFile(outputFile, database.getTransactions().size());

                twoPhase.printStats();

            } else if ("HUI-Miner".equals(algorithmName)) {
                int minutil = getParamAsInteger(parameters[0]);
                // Applying the algorithm
                AlgoHUIMiner huiminer = new AlgoHUIMiner();
                huiminer.runAlgorithm(inputFile, outputFile, minutil);
                huiminer.printStats();
            }else if ("FHM".equals(algorithmName)) {
                int minutil = getParamAsInteger(parameters[0]);
                // Applying the algorithm
                AlgoFHM fhm = new AlgoFHM();
                fhm.runAlgorithm(inputFile, outputFile, minutil);
                fhm.printStats();
            } else if ("Zart".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                // Load a binary context
                TransactionDatabase context = new TransactionDatabase();
                context.loadFile(inputFile);

                // Apply the Zart algorithm
                AlgoZart zart = new AlgoZart();
                TZTableClosed results = zart.runAlgorithm(context, minsup);
                TFTableFrequent frequents = zart.getTableFrequent();
                zart.printStatistics();
                zart.saveResultsToFile(outputFile);
            }else if ("Pascal".equals(algorithmName)) {
                double minsup = getParamAsDouble(parameters[0]);

                // Applying the Apriori algorithm, optimized version
                AlgoPASCAL algo = new AlgoPASCAL();
                algo.runAlgorithm(minsup, inputFile, outputFile);
                algo.printStats();
            } 
            else if ("Convert_a_sequence_database_to_SPMF_format".equals(algorithmName)) {
                String format = getParamAsString(parameters[0]);
                int seqCount = getParamAsInteger(parameters[1]);

                long startTime = System.currentTimeMillis();
                SequenceDatabaseConverter converter = new SequenceDatabaseConverter();
                converter.convert(inputFile, outputFile, Formats.valueOf(format), seqCount);
                long endTIme = System.currentTimeMillis();
                System.out.println("Sequence database converted.  Time spent for conversion = " + (endTIme - startTime) + " ms.");
            } else if ("Convert_a_transaction_database_to_SPMF_format".equals(algorithmName)) {
                String format = getParamAsString(parameters[0]);
                int transactionCount = getParamAsInteger(parameters[1]);

                long startTime = System.currentTimeMillis();
                TransactionDatabaseConverter converter = new TransactionDatabaseConverter();
                converter.convert(inputFile, outputFile, Formats.valueOf(format), transactionCount);
                long endTIme = System.currentTimeMillis();
                System.out.println("Transaction database converted.  Time spent for conversion = " + (endTIme - startTime) + " ms.");
            } else if ("Generate_a_sequence_database".equals(algorithmName)) {
                int p1 = getParamAsInteger(parameters[0]);
                int p2 = getParamAsInteger(parameters[1]);
                int p3 = getParamAsInteger(parameters[2]);
                int p4 = getParamAsInteger(parameters[3]);

                SequenceDatabaseGenerator generator = new SequenceDatabaseGenerator();
                generator.generateDatabase(p1, p2, p3, p4, outputFile, false);
                System.out.println("Sequence database generated.  ");

            } else if ("Generate_a_sequence_database_with_timestamps".equals(algorithmName)) {
                int p1 = getParamAsInteger(parameters[0]);
                int p2 = getParamAsInteger(parameters[1]);
                int p3 = getParamAsInteger(parameters[2]);
                int p4 = getParamAsInteger(parameters[3]);

                SequenceDatabaseGenerator generator = new SequenceDatabaseGenerator();
                generator.generateDatabase(p1, p2, p3, p4, outputFile, true);
                System.out.println("Sequence database generated.  ");
            } else if ("Generate_a_transaction_database".equals(algorithmName)) {
                int p1 = getParamAsInteger(parameters[0]);
                int p2 = getParamAsInteger(parameters[1]);
                int p3 = getParamAsInteger(parameters[2]);

                TransactionDatabaseGenerator generator = new TransactionDatabaseGenerator();
                generator.generateDatabase(p1, p2, p3, outputFile);
                System.out.println("Transaction database generated.  ");
            } else if ("Calculate_stats_for_a_sequence_database".equals(algorithmName)) {
                try {
                    SequenceStatsGenerator sequenceDatabase = new SequenceStatsGenerator();
                    sequenceDatabase.getStats(inputFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // IF THE FILE WAS AN ARFF FILE, WE NEED TO CONVERT BACK THE RESULT
            // SO THAT IT IS PRESENTED IN TERMS OF VALUES
            if (mapItemAttributeValue != null) {
                ResultConverter converter = new ResultConverter();
                System.out.println("Post-processing to show result in terms of ARFF attribute values.");
                converter.convert(mapItemAttributeValue, outputFile, originalOutputFile);
                System.out.println("Post-processing completed.");
                // delete the temporary files
//				System.out.println("Delete : " + outputFile);
                File file = new File(outputFile);
                file.delete();
//				System.out.println("Delete : " + inputFile);
                File file2 = new File(inputFile);
                file2.delete();
                // set the original outputFile and inputFile
                outputFile = originalOutputFile;
                inputFile = originalInputFile;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Error. Please check the parameters of the algorithm.  The format for numbers is incorrect. \n"
                    + "\n ERROR MESSAGE = " + e.toString(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(null,
                    "An error while trying to run the algorithm. \n ERROR MESSAGE = " + e.toString(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * This method process the command line arguments when the spmf.jar file is
     * called from the command line.
     *
     * @param args command line arguments.
     */
    public static void processCommandLineArguments(String[] args) {
        //java -Xmx1024m -jar spmfGUIv090b.jar run PrefixSpan /home/ph/Bureau/contextPrefixSpan.txt /home/ph/Bureau/test3.txt 60%
        //java -Xmx1024m -jar spmfGUIv090b.jar run PrefixSpan contextPrefixSpan.txt test3.txt 60%
//		System.out.println(" \n\n-- SPMF version " + SPMF_VERSION + " --\n\n");

        // "version" --> show the current version
        if ("version".equals(args[0])) {
            System.out.println(" \n-- SPMF version " + SPMF_VERSION + " --\n");
        } // "help" --> show the link to read the documentation
        else if ("help".equals(args[0])) {
            System.out.println("\n\nFor help, please check the documentation section of the SPMF website: http://philippe-fournier-viger.com/spmf/ \n\n");
        } //"run" -->  the user wants to run an algorithm
        else if ("run".equals(args[0])) {
            // We get the parameters :
            String algoName = args[1]; // algorithm name
            String input = args[2];  // input file
            String output = args[3]; // output file
            // create an array to store the parameters of the algorithm
            String parameters[] = new String[args.length - 4];
            // copy the arguments in the array of parameters:
            if (args.length > 4) {
                System.arraycopy(args, 4, parameters, 0, args.length - 4);
            }
            // run the algorithm:
            runAlgorithm(algoName, input, output, parameters);
        } // "test" --> this is to run a test file for developers only.
        else if ("test".equals(args[0])) {
            String testName = args[1];
            try {
                Class testClass = Class.forName("ca.pfv.spmf.tests." + testName);
                Method mainMethod = testClass.getMethod("main", String[].class);
                String[] params = null;
                mainMethod.invoke(null, (Object) params);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // if any other commands that we don't recognize,
            // we show this:
            System.out.println("\n\n Command not recognized.\n For help, please check the documentation section of the SPMF website: http://philippe-fournier-viger.com/spmf/ \n \n");
        }
    }
    
    /**
     * This method show the help webpage for a given algorithm in the default browser of the user.
     * @param choice the algorithm name (e.g. "PrefixSpan")
     */
    private void openHelpWebPageForAlgorithm(String choice) {
		if ("PrefixSpan".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#examplePrefixSpan");
        } else if ("HirateYamana".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example11");
        } else if ("PrefixSpan_with_strings".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#examplePrefixSpan");
        } else if ("SeqDim_(PrefixSpan+Apriori)".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleMDSPM1");
        } else if ("SeqDim_(BIDE+AprioriClose)".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleMDSPM1");
        } else if ("SeqDim_(BIDE+Charm)".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleMDSPM1");
        } else if ("SeqDim_(PrefixSpan+Apriori)+time".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example14");
        } else if ("SeqDim_(BIDE+AprioriClose)+time".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example14");
        } else if ("SeqDim_(BIDE+Charm)+time".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example14");
        } else if ("SPAM".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#spam");
        } else if ("BIDE+".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleBIDE");
        } else if ("BIDE+_with_strings".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#exampleBIDE");
        } else if ("RuleGrowth".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#rulegrowth");
        } else if ("TRuleGrowth".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#trulegrowth");
        } else if ("TRuleGrowth_with_strings".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#trulegrowth");
        } else if ("CMRules".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cmrules");
        } else if ("CMDeo".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cmdeo");
        }//Sporadic_association_rules
        else if ("Sporadic_association_rules".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example19");
        }// Closed_association_rules
        else if ("Closed_association_rules".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example20");
        } //IGB
        else if ("IGB".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example7");
        }// MNR
        else if ("MNR".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example21");
        } //Indirect_association_rules
        else if ("Indirect_association_rules".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#indirect");
        } else if ("RuleGen".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#rulegen");
        } else if ("TopSeqRules".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#topseqrules");
        } else if ("TopKRules".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#topkrules");
        } else if ("TNR".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#tnr");
        } else if ("FPGrowth_itemsets".equals(choice) || "FPGrowth_itemsets_with_strings".equals("choice")) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#growth");
        } else if ("Apriori_association_rules".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#allassociationrules");
        } else if ("FPGrowth_association_rules".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#allassociationrules");
        } else if ("FPGrowth_association_rules_with_lift".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#lift");
        }else if ("CFPGrowth++_association_rules".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cfpgrowth_ar");
        } else if ("CFPGrowth++_association_rules_with_lift".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cfpgrowth_ar");
        } else if ("Apriori".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example1");
        } else if ("Apriori_with_hash_tree".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example1");
        } else if ("AprioriClose".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example2");
        } else if ("Apriori_TID".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example2");
        } else if ("Apriori_TIDClose".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example2");
        } else if ("AprioriRare".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example17");
        } else if ("AprioriInverse".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example18");
        } else if ("VME".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#erasable");
        } else if ("UApriori".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#uapriori");
        } else if ("MSApriori".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#msapriori");
        } else if ("CFPGrowth++".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cfpgrowth");
        } else if ("Apriori_TID_bitset".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#aprioritid");
        } else if ("HMine".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#hmine");
        } else if ("DCI_Closed".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#dciclosed");
        } else if ("DefMe".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#defme");
        } else if ("Charm_MFI".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#e3");
        } else if ("dCharm_bitset".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#e2");
        } else if ("Charm_bitset".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#e2");
        } else if ("Eclat".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#e1");
        }  else if ("dEclat".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#e1");
        }  else if ("dEclat_bitset".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#e1");
        } else if ("Eclat_bitset".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#e1");
        } else if ("Relim".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#c23");
        } else if ("Zart".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#zart");
        } else if ("Pascal".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#pascal");
        } else if ("Two-Phase".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#twophase");
        } else if ("HUI-Miner".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#huiminer");
        }  else if ("FHM".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#fhm");
        } else if ("FHSAR".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#FHSAR");
        } else if ("KMeans".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example8");
        } else if ("Hierarchical_clustering".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#example10");
        } else if ("Convert_a_sequence_database_to_SPMF_format".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#convseq");
        } else if ("Convert_a_transaction_database_to_SPMF_format".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#convtdb");
        } else if ("Generate_a_sequence_database".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#genseq");
        } else if ("Generate_a_sequence_database_with_timestamps".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#genseqt");
        } else if ("Generate_a_transaction_database".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#gentrans");
        } else if ("Calculate_stats_for_a_sequence_database".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#statsseq");
        } else if ("TNS".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#tns");
        } else if ("TNR".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#tnr");
        } else if ("CM-SPAM".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cmspam");
        } else if ("CM-SPADE".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cmspade");
        }else if ("CM-ClaSP".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#cmclasp");
        } else if ("MaxSP".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#maxsp");
        }else if ("VMSP".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#vmsp");
        } else if ("TKS".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#tks");
        }else if ("TSP_nonClosed".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#tsp");
        }else if ("VGEN".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#vgen");
        }else if ("FEAT".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#feat");
        }else if ("FSGP".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#fsgp");
        }else if ("LAPIN".equals(choice)) {
            openWebPage("http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php#lapin");
        }
	}

    /**
     * This method ask the user to choose the input file. This method is
     * called when the user click on the button to choose the input file.
     */
	private void askUserToChooseInputFile() {
		try {
		    // WHEN THE USER CLICK TO CHOOSE THE INPUT FILE

		    File path;
		    // Get the last path used by the user, if there is one
		    String previousPath = PathsManager.getInstance().getInputFilePath();
		    if (previousPath == null) {
		        // If there is no previous path (first time user), 
		        // show the files in the "examples" package of
		        // the spmf distribution.
		        URL main = MainTestApriori_saveToFile.class.getResource("MainTestApriori_saveToFile.class");
		        if (!"file".equalsIgnoreCase(main.getProtocol())) {
		            path = null;
		        } else {
		            path = new File(main.getPath());
		        }
		    } else {
		        // Otherwise, the user used SPMF before, so
		        // we show the last path that he used.
		        path = new File(previousPath);
		    }

		    // Create a file chooser to let the user
		    // select the file.
		    final JFileChooser fc = new JFileChooser(path);
		    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		    int returnVal = fc.showOpenDialog(MainWindow.this);

		    // if he chose a file
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
		        File file = fc.getSelectedFile();
		        textFieldInput.setText(file.getName());
		        inputFile = file.getPath(); // remember the file he chose
		    }
		    // remember this folder for next time.
		    if (fc.getSelectedFile() != null) {
		        PathsManager.getInstance().setInputFilePath(fc.getSelectedFile().getParent());
		    }
		} catch (Exception e) {
		    JOptionPane.showMessageDialog(null,
		            "An error occured while opening the input file dialog. ERROR MESSAGE = " + e.toString(), "Error",
		            JOptionPane.ERROR_MESSAGE);
		}
	}

    /**
     * This method ask the user to choose the output file. This method is
     * called when the user click on the button to choose the input file.
     */
	private void askUserToChooseOutputFile() {
		try {
		    // WHEN THE USER CLICK TO CHOOSE THE OUTPUT FILE

		    File path;
		    // Get the last path used by the user, if there is one
		    String previousPath = PathsManager.getInstance().getOutputFilePath();
		    // If there is no previous path (first time user), 
		    // show the files in the "examples" package of
		    // the spmf distribution.
		    if (previousPath == null) {
		        URL main = MainTestApriori_saveToFile.class.getResource("MainTestApriori_saveToFile.class");
		        if (!"file".equalsIgnoreCase(main.getProtocol())) {
		            path = null;
		        } else {
		            path = new File(main.getPath());
		        }
		    } else {
		        // Otherwise, use the last path used by the user.
		        path = new File(previousPath);
		    }

		    // ASK THE USER TO CHOOSE A FILE
		    final JFileChooser fc;
		    if (path != null) {
		        fc = new JFileChooser(path.getAbsolutePath());
		    } else {
		        fc = new JFileChooser();
		    }
		    int returnVal = fc.showSaveDialog(MainWindow.this);

		    // If the user chose a file
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
		        File file = fc.getSelectedFile();
		        textFieldOutput.setText(file.getName());
		        outputFile = file.getPath(); // save the file path
		        // save the path of this folder for next time.
		        if (fc.getSelectedFile() != null) {
		            PathsManager.getInstance().setOutputFilePath(fc.getSelectedFile().getParent());
		        }
		    }

		} catch (Exception e) {
		    JOptionPane.showMessageDialog(null,
		            "An error occured while opening the output file dialog. ERROR MESSAGE = " + e.toString(), "Error",
		            JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This method is called when the user click the "Run" button of the user interface,
	 * to launch the chosen algorithm and thereafter catch exception if one occurs.
	 */
	private void processRunAlgorithmCommandFromGUI() {
		// Get the parameters
		String choice = (String) comboBox.getSelectedItem();
		String parameters[] = new String[6];
		parameters[0] = textFieldParam1.getText();
		parameters[1] = textFieldParam2.getText();
		parameters[2] = textFieldParam3.getText();
		parameters[3] = textFieldParam4.getText();
		parameters[4] = textFieldParam5.getText();
		parameters[5] = textFieldParam6.getText();
		textArea.setText("");

		// run the selected algorithm
		boolean succeed = runAlgorithm(choice, inputFile, outputFile, parameters);  // END IF - CHECBOX

		// IF - the algorithm terminates...
		if (succeed && checkboxOpenOutput.isSelected() && lblSetOutputFile.isVisible()) {
		    // open the output file if the checkbox is checked 
		    Desktop desktop = Desktop.getDesktop();
		    // check first if we can open it on this operating system:
		    if (desktop.isSupported(Desktop.Action.OPEN)) {
		        try {
		            // if yes, open it
		            desktop.open(new File(outputFile));
		        } catch (IOException e) {
		            JOptionPane.showMessageDialog(null,
		                    "The output file failed to open with the default application. "
		                    + "\n This error occurs if there is no default application on your system "
		                    + "for opening the output file or the application failed to start. "
		                    + "\n\n"
		                    + "To fix the problem, consider changing the extension of the output file to .txt."
		                    + "\n\n ERROR MESSAGE = " + e.toString(), "Error",
		                    JOptionPane.ERROR_MESSAGE);
		        } catch (SecurityException e) {
		            JOptionPane.showMessageDialog(null,
		                    "A security error occured while trying to open the output file. ERROR MESSAGE = " + e.toString(), "Error",
		                    JOptionPane.ERROR_MESSAGE);
		        } catch (Throwable e) {
		            JOptionPane.showMessageDialog(null,
		                    "An error occured while opening the output file. ERROR MESSAGE = " + e.toString(), "Error",
		                    JOptionPane.ERROR_MESSAGE);
		        }
		    }
		}
	}
}
