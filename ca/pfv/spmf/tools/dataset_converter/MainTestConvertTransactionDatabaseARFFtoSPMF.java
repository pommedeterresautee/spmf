package ca.pfv.spmf.tools.dataset_converter;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Example of how to convert a transaction database from the ARFF format to the
 * SPMF format.
 * <br/><br/>
 * 
 * The ARFF format is a format used by other data mining software (rapid-miner,
 * etc.). It allows representing a relational table as a text file. In SPMF, a
 * good effort has been made to support the conversion from the ARFF format to a
 * transaction database in SPMF format. All features of the ARFF format
 * specification are supported (see
 * http://weka.wikispaces.com/ARFF+%28stable+version%29 for the full ARFF
 * specification), except that the character "=" is forbidden and that escape
 * characters are not supported.
 * <br/><br/>
 * 
 * For example, the following input file (example.arff) is in ARFF format and
 * can be converted to SPMF format by the tool. This file defines a table named
 * "sunburn" having 6 attributes. The first five attributes have a limited set
 * of possible values. For example, the attribute "weight" is an enumeration
 * that can only take "blonde", "brown" or "red" as value, or "?" if the value
 * is unknown. The instances in this table are represented by each line after
 * the @data instruction. In this example, there is 9 such instances. The first
 * 8 instances are in regular ARFF format: each attribute value is separated by
 * a comma. The last instance is in sparse ARFF format. This means that some
 * attributes having a value of "0" are ommitted. Finally, note that lines
 * starting with "%" are comments". For additional details about the ARFF
 * format, please refer to the official specification.
 * 
 * % % SUNBURN DATA % This file is a modified version of
 * http://www.hakank.org/weka/sunburn.arf % additional
 * 
 * @RELATION 'sunburn'% THIS IS A COMMENT
 * @ATTRIBUTE 'weight' {blonde, brown, red}
 * @ATTRIBUTE 'height' {short, average, tall}
 * @ATTRIBUTE weight {light, average, heavy}
 * @ATTRIBUTE 'lotion' {yes,no}
 * @ATTRIBUTE 'burned' {burned, none} % THIS IS A COMMENT
 * @attribute col_17 INTEGER
 * @DATA% THIS IS A TEST ?, average,light, no, burned, 1 % THIS IS A COMMENT
 *        blonde, tall, average,yes, none, 2% THIS IS A COMMENT brown, short,
 *        average,yes, none, 3 blonde, short, average,no, burned, 4 red,
 *        average,heavy, no, burned, 5 brown, tall, heavy, no, none, 6 brown,
 *        average,heavy, no, none, 7 blonde, short, light, yes, none, 4 {1
 *        blonde, 2 average, 3, heavy, 4 none} % THIS IS A SPARSE DATA INSTANCE
 *        SPECIFICATION
 * 
 *        Note that according to the ARFF format, an unknown value for an
 *        attribute is represented by the character "?". In SPMF, if you choose
 *        the format "ARFF", the unknown values will be ommitted during the
 *        conversion. If you want to keep the unknown values, then choose the
 *        alternative format "ARFF_WITH_MISSING_VALUES" for conversion, which
 *        will keep the unknown values.
 * 
 *        The result of the conversion of the previous ARFF file is a file in
 *        SPMF format:
 * @CONVERTED_FROM_ARFF
 * @RELATION_NAME=sunburn=
 * @ATTRIBUTE=weight=ENUMERATION=blonde=brown=red=
 * @ATTRIBUTE=height=ENUMERATION=short=average=tall=
 * @ATTRIBUTE=weight=ENUMERATION=light=average=heavy=
 * @ATTRIBUTE=lotion=ENUMERATION=yes=no=
 * @ATTRIBUTE=burned=ENUMERATION=burned=none=
 * @ATTRIBUTE=col_17=INTEGER=
 * @ITEM=1=height=average
 * @ITEM=2=weight=light
 * @ITEM=3=lotion=no
 * @ITEM=4=burned=burned
 * @ITEM=5=col_17=1 1 2 3 4 5
 * @ITEM=6=weight=blonde
 * @ITEM=7=height=tall
 * @ITEM=8=weight=average
 * @ITEM=9=lotion=yes
 * @ITEM=10=burned=none
 * @ITEM=11=col_17=2 6 7 8 9 10 11
 * @ITEM=12=weight=brown
 * @ITEM=13=height=short
 * @ITEM=14=col_17=3 8 9 10 12 13 14
 * @ITEM=15=col_17=4 3 4 6 8 13 15
 * @ITEM=16=weight=red
 * @ITEM=17=weight=heavy
 * @ITEM=18=col_17=5 1 3 4 16 17 18
 * @ITEM=19=col_17=6 3 7 10 12 17 19
 * @ITEM=20=col_17=7 1 3 10 12 17 20 2 6 9 10 13 15
 * 
 *                   The first line indicates that this file was obtained by a
 *                   conversion from ARFF to SPMF format. The second line
 *                   indicates the name of the original relational table
 *                   specified in the ARFF file. Each line starting with
 *                   "@attribute" defines an attribute in the table, its type
 *                   and the possible values that the attribute can take if the
 *                   type is "ENUMERATION". For example, the attribute "weight"
 *                   is an enumeration that can take a value from "blonde",
 *                   "brown" or "red". The lines starting with
 *                   "@ITEM= indicates a mapping between a unique ID an an attribute value. For example, the line "
 *                   @ITEM=1=height=average
 *                   " means that the ID 1 represents the value "
 *                   average" for the attribute "height
 *                   ". Finally, the lines that are a list of integers separated by spaces each represents a data instances. For example, the line "
 *                   1 2 3 4
 *                   5" represents the data instance with the value "average
 *                   " for the attribute "
 *                   height", the value "light" for the attribute "
 *                   weight", the value "
 *                   no" for the attribute "lotion", the value "
 *                   burned" for the attribute "burned" and the value 1" for the
 *                   attribute "col_17".
 * 
 *                   Note that all lines starting with "@" are metadata that are
 *                   not used by the algorithms in SPMF". But this data is kept
 *                   so that the results found by the algorithms can be
 *                   interpreted.
 */
class MainTestConvertTransactionDatabaseARFFtoSPMF {

	public static void main(String[] arg) throws IOException {

		String inputFile = fileToPath("example.arff"); // the file to be converted in ARFF format
		String outputFile = ".//output.txt"; // the resulting converted file in SPMF format
		Formats inputFileformat = Formats.ARFF;  // the format of the input file (ARFF)
		int transactionCount = Integer.MAX_VALUE;  // the number of transaction from the input file to be converted

		// Create a converter
		TransactionDatabaseConverter converter = new TransactionDatabaseConverter();
		// Call the method to convert the input file from ARFF to the SPMF format
		converter
				.convert(inputFile, outputFile, inputFileformat, transactionCount);
	}

	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestConvertTransactionDatabaseARFFtoSPMF.class
				.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
