
package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.Abstraction_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.ItemAbstractionPairCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.patterns.PatternCreator;


/**
 * This class is used to remove all those static structures. Sometimes can be useful if we execute a automaticed tests
 * 
 * Copyright Antonio Gomariz Peñalver 2013
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author agomariz
 */
public class RemoveStatics {

    public static void clear() {
        ItemAbstractionPairCreator.clear();
        Abstraction_Qualitative.clear();
        AbstractionCreator_Qualitative.sclear();
        PatternCreator.sclear();
    }
}
