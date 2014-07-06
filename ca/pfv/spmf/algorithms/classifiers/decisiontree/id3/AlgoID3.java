package ca.pfv.spmf.algorithms.classifiers.decisiontree.id3;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
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
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is an implementation of the ID3 algorithm for creating a decision tree.
 * <br/><br/>
 * ID3 is a very popular algorithms described in many artificial intelligence
 * and data mining textbooks.
 * 
 * @author Philippe Fournier-Viger
 */
public class AlgoID3 {
	// the list of attributes
	private String[] allAttributes; 
	// the position of the target attribute in the list of attributes
	private int indexTargetAttribute = -1; 
	// the set of values for the target attribute
	private Set<String> targetAttributeValues = new HashSet<String>(); 
	
	// for statistics
	private long startTime; // start time of the latest execution
	private long endTime;   // end time of the latest execution

	/**
	 * Create a decision tree from a set of training instances.
	 * @param input path to an input file containing training instances
	 * @param targetAttribute the target attribute (that will be used for classification)
	 * @param separator  the separator in the input file (e.g. space).
	 * @return a decision tree
	 * @throws IOException exception if error reading the file
	 */
	public DecisionTree runAlgorithm(String input, String targetAttribute,
			String separator) throws IOException {
		// record the start time
		startTime = System.currentTimeMillis();
		
		// create an empty decision tree
		DecisionTree tree = new DecisionTree();

		// (1) read input file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line = reader.readLine();

		// Read the first line and note the name of the attributes.
		// At the same time identify the position of the target attribute and
		// other attributes.
		allAttributes = line.split(separator);
		
		// make an array to store the attributes except the target attribute
		int[] remainingAttributes = new int[allAttributes.length - 1];
		int pos = 0;
		// for each attribute
		for (int i = 0; i < allAttributes.length; i++) {
			// if it is the target attribute
			if (allAttributes[i].equals(targetAttribute)) {
				// save the position of the target attribute. It will be useful
				// later.
				indexTargetAttribute = i;
			} else {
				// otherwise add the attribute to the array of attributes
				remainingAttributes[pos++] = i;
			}
		}

		// Read instances into memory (line by line until end of file)
		List<String[]> instances = new ArrayList<String[]>();
		while (((line = reader.readLine()) != null)) { 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the line
			String[] lineSplit = line.split(separator);
			// process the instance
			instances.add(lineSplit);
			// remember the value for the target attribute
			targetAttributeValues.add(lineSplit[indexTargetAttribute]);
		}
		reader.close(); // close input file

		// (2) Start the recusive process
		
		// create the tree
		tree.root = id3(remainingAttributes, instances);
		tree.allAttributes = allAttributes;
		
		endTime = System.currentTimeMillis();  // record end time
		
		return tree; // return the tree
	}

	/**
	 * Method to create a subtree according to a set of attributes and training
	 * instances.
	 * @param remainingAttributes remaining attributes to create the tree
	 * @param instances a list of training instances
	 * @return node of the subtree created
	 */
	private Node id3(int[] remainingAttributes, List<String[]> instances) {
		// if only one remaining attribute,
		// return a class node with the most common value in the instances
		if (remainingAttributes.length == 0) {
			// Count the frequency of class
			Map<String, Integer> targetValuesFrequency = calculateFrequencyOfAttributeValues(
					instances, indexTargetAttribute);
			
			// Loop over the values to find the class with the highest frequency
			int highestCount = 0;
			String highestName = "";
			for (Entry<String, Integer> entry : targetValuesFrequency
					.entrySet()) {
				// if the frequency is higher
				if (entry.getValue() > highestCount) {
					highestCount = entry.getValue();
					highestName = entry.getKey();
				}
			}
			// return a class node with the value having the highest frequency
			ClassNode classNode = new ClassNode();
			classNode.className = highestName;
			return classNode;
		}

		// Calculate the frequency of each target attribute value and
		// at the same time check if there is a single class.
		Map<String, Integer> targetValuesFrequency = calculateFrequencyOfAttributeValues(
				instances, indexTargetAttribute);

		// if all instances are from the same class
		if (targetValuesFrequency.entrySet().size() == 1) {
			ClassNode classNode = new ClassNode();
			classNode.className = (String) targetValuesFrequency.keySet()
					.toArray()[0];
			return classNode;
		}

		// Calculate global entropy
		double globalEntropy = 0d;
		// for each value
		for (String value : targetAttributeValues) {
			// calculate frequency
			Integer frequencyInt = targetValuesFrequency.get(value);
			// if the frequency is not zero
			if(frequencyInt != null) {

				// calculate the frequency has a double
				double frequencyDouble = frequencyInt / (double) instances.size();

				// update the global entropy
				globalEntropy -= frequencyDouble * Math.log(frequencyDouble) / Math.log(2);
			}
		}
		// System.out.println("Global entropy = " + globalEntropy);

		// Select the attribute from remaining attributes such that if we split
		// the dataset on this
		// attribute, we will get the higher information gain
		int attributeWithHighestGain = 0;
		double highestGain = -99999;
		for (int attribute : remainingAttributes) {
			double gain = calculateGain(attribute, instances, globalEntropy);
			// System.out.println("Process " + allAttributes[attribute] +
			// " gain = " + gain);
			if (gain >= highestGain) {
				highestGain = gain;
				attributeWithHighestGain = attribute;
			}
		}
		
		
		// if the highest gain is 0....
		if (highestGain == 0) {
			ClassNode classNode = new ClassNode();
			// take the most frequent classes
			int topFrequency = 0;
			String className = null;
			for(Entry<String, Integer> entry: targetValuesFrequency.entrySet()) {
				if(entry.getValue() > topFrequency) {
					topFrequency = entry.getValue();
					className = entry.getKey();
				}
			}
			classNode.className = className;
			return classNode;
		}

		// Create a decision node for the attribute
		// System.out.println("Attribute with highest gain = " +
		// allAttributes[attributeWithHighestGain] + " " + highestGain);
		DecisionNode decisionNode = new DecisionNode();
		decisionNode.attribute = attributeWithHighestGain;

		// calculate the list of remaining attribute after we remove the
		// attribute
		int[] newRemainingAttribute = new int[remainingAttributes.length - 1];
		int pos = 0;
		for (int i = 0; i < remainingAttributes.length; i++) {
			if (remainingAttributes[i] != attributeWithHighestGain) {
				newRemainingAttribute[pos++] = remainingAttributes[i];
			}
		}

		// Split the dataset into partitions according to the selected attribute
		Map<String, List<String[]>> partitions = new HashMap<String, List<String[]>>();
		for (String[] instance : instances) {
			String value = instance[attributeWithHighestGain];
			List<String[]> listInstances = partitions.get(value);
			if (listInstances == null) {
				listInstances = new ArrayList<String[]>();
				partitions.put(value, listInstances);
			}
			listInstances.add(instance);
		}

		// Create the values for the subnodes
		decisionNode.nodes = new Node[partitions.size()];
		decisionNode.attributeValues = new String[partitions.size()];

		// For each partition, make a recursive call to create
		// the corresponding branches in the tree.
		int index = 0;
		for (Entry<String, List<String[]>> partition : partitions.entrySet()) {
			decisionNode.attributeValues[index] = partition.getKey();
			decisionNode.nodes[index] = id3(newRemainingAttribute,
					partition.getValue()); // recursive call
			index++;
		}
		
		// return the root node of the subtree created
		return decisionNode;
	}

	/**
	 * Calculate the information gain of an attribute for a set of instance
	 * @param attributePos the position of the attribute
	 * @param instances a list of instances
	 * @param globalEntropy the global entropy
	 * @return the gain
	 */
	private double calculateGain(int attributePos, List<String[]> instances,
			double globalEntropy) {
		// Count the frequency of each value for the attribute
		Map<String, Integer> valuesFrequency = calculateFrequencyOfAttributeValues(
				instances, attributePos);

		// Calculate the gain
		double sum = 0;
		// for each value
		for (Entry<String, Integer> entry : valuesFrequency.entrySet()) {
			// make the sum 
			sum += entry.getValue()
					/ ((double) instances.size())
					* calculateEntropyIfValue(instances, attributePos,
							entry.getKey());
		}
		// subtract the sum from the global entropy
		return globalEntropy - sum;
	}

	/**
	 * Calculate the entropy for the target attribute, if a given attribute has
	 * a given value.
	 * 
	 * @param instances
	 *            : list of instances
	 * @param attributeIF
	 *            : the given attribute
	 * @param valueIF
	 *            : the given value
	 * @return entropy
	 */
	private double calculateEntropyIfValue(List<String[]> instances,
			int attributeIF, String valueIF) {
		
		// variable to count the number of instance having the value for that
		// attribute
		int instancesCount = 0;
		
		// variable to count the frequency of each value
		Map<String, Integer> valuesFrequency = new HashMap<String, Integer>();
		
		// for each instance
		for (String[] instance : instances) {
			// if that instance has the value for the attribute
			if (instance[attributeIF].equals(valueIF)) {
				String targetValue = instance[indexTargetAttribute];
				// increase the frequency
				if (valuesFrequency.get(targetValue) == null) {
					valuesFrequency.put(targetValue, 1);
				} else {
					valuesFrequency.put(targetValue,
							valuesFrequency.get(targetValue) + 1);
				}
				// increase the number of instance having the value for that
				// attribute
				instancesCount++; 
			}
		}
		// calculate entropy
		double entropy = 0;
		// for each value of the target attribute
		for (String value : targetAttributeValues) {
			// get the frequency
			Integer count = valuesFrequency.get(value);
			// if the frequency is not null
			if (count != null) {
				// update entropy according to the formula
				double frequency = count / (double) instancesCount;
				entropy -= frequency * Math.log(frequency) / Math.log(2);
			}
		}
		return entropy;
	}

	/**
	 * This method calculates the frequency of each value for an attribute in a
	 * given set of instances
	 * 
	 * @param instances
	 *            A set of instances
	 * @param indexAttribute
	 *            The attribute.
	 * @return A map where the keys are attributes and values are the number of
	 *         times that the value appeared in the set of instances.
	 */
	private Map<String, Integer> calculateFrequencyOfAttributeValues(
			List<String[]> instances, int indexAttribute) {
		// A map to calculate the frequency of each value:
		// Key: a string indicating a value
		// Value:  the frequency
		Map<String, Integer> targetValuesFrequency = new HashMap<String, Integer>();
		
		// for each instance of the training set
		for (String[] instance : instances) {
			// get the value of the attribute for that instance
			String targetValue = instance[indexAttribute];
			// increase the frequency by 1
			if (targetValuesFrequency.get(targetValue) == null) {
				targetValuesFrequency.put(targetValue, 1);
			} else {
				targetValuesFrequency.put(targetValue,
						targetValuesFrequency.get(targetValue) + 1);
			}
		}
		// return the map
		return targetValuesFrequency;
	}

	/**
	 * Print statistics about the execution of this algorithm
	 */
	public void printStatistics() {
		System.out.println("Time to construct decision tree = "
				+ (endTime - startTime) + " ms");
		System.out.println("Target attribute = "
				+ allAttributes[indexTargetAttribute]);
		System.out.print("Other attributes = ");
		for (String attribute : allAttributes) {
			if (!attribute.equals(allAttributes[indexTargetAttribute])) {
				System.out.print(attribute + " ");
			}
		}
		System.out.println();
	}
}
