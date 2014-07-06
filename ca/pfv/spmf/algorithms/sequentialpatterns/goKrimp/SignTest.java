
package ca.pfv.spmf.algorithms.sequentialpatterns.goKrimp;

/**
 * SignTest class implements the standard Sign Test to compare two populations and test if 
 * they are sampled from the distributions with the same mean value 
 * <br/><br/>
 * 
 * For more information please refer to the paper Mining Compressing Sequential Patterns in the Journal Statistical Analysis and Data Mining
 * * <br/><br/>
 * 
 * Copyright (c) 2014  Hoang Thanh Lam (TU Eindhoven and IBM Research)
 * Toon Calders (Université Libre de Bruxelles), Fabian Moerchen (Amazon.com inc)
 * and Dmitriy Fradkin (Siemens Corporate Research)
 * <br/><br/>
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * <br/><br/>
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <br/><br/>
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <br/><br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see DataReader
 * @see Event
 * @see MyPattern
 * @see AlgoGoKrimp
*  @author  Hoang Thanh Lam (TU Eindhoven and IBM Research)
*/
public class SignTest {
    static final double alpha=0.01; //the significant level
    static final int N=25; // the minimum number of pairs must be at least  25  to ensure taht the sign test is correct
    int Npairs; // the number of pairs (X,Y)
    double Nplus; // the number of pairs (X,Y) such that X>Y
    /**
     * the cdf function of the Standard Normal distribution 
     * @param xx input value
     * @return cdf of standard normal distribution
     */
    double standard_normal_cdf(double xx){
	double x=xx;
	if (xx<0)
		x=-x;		
	double b0=0.2316419, b1=0.319381530, b2=-0.356563782, b3=1.781477937, b4=-1.821255978, b5=1.330274429;
	double t=1/(1+b0*x);
	double pi=4.0*Math.atan(1.0);
	double pdf= 1/Math.sqrt(2*pi)*Math.exp(-0.5*x*x); //standard normal distribution's pdf
	if (xx>0)	
		return 1-pdf*(b1*t+b2*t*t+b3*t*t*t+b4*t*t*t*t+b5*t*t*t*t*t);
	else
		return pdf*(b1*t+b2*t*t+b3*t*t*t+b4*t*t*t*t+b5*t*t*t*t*t);
    }

    /**
     *  return true if it passes the test
     */
    boolean sign_test(){
        if(Npairs<N){ // the number of pairs must be at least N=25 to perform the test
           return false;
        }else {
            double x=Math.abs(Nplus -0.5*Npairs)/Math.sqrt(Npairs+0.0);
            if(1-standard_normal_cdf(x)<alpha){
                return true;
            }else
                return false;
        }
    }   
    
    SignTest(int np, double npp){
        Npairs=np;
        Nplus=npp;
    }
}
