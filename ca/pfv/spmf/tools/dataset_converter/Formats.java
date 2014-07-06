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

/**
 * Enumeration of the data formats that can be read by the database
 * converters. All output are to the SPMF format.
 * <br/><br/>
 * For a sequence database, the following format can be converted to SPMF:
 * CSV, IBMGenerator, Kosarak, Snake and BMS (see documentation for a description
 * of these formats).
 * <br/><br/>
 * For a transaction database, the following format can be converted to SPMF:
 * CSV, ARF

@see TransactionDatabaseConverter
@see SequenceDatabaseConverter
* @author Philippe Fournier-Viger
 */
public enum Formats {
	SPMF, CSV_INTEGER, IBMGenerator, Kosarak, Snake, BMS, 
	ARFF, ARFF_WITH_MISSING_VALUES 
}
