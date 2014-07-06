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
/**
 * The main method of this class reset the input/output file paths
 * that are stored in the registry by the GUI version of SPMF 
 * (this class is intended for developers only).
 * 
 * @author Philippe Fournier-Viger
 */
 class ResetPaths {

	public static void main(String[] args) {
		PathsManager.getInstance().setInputFilePath("");
		PathsManager.getInstance().setOutputFilePath("");
	}

}
