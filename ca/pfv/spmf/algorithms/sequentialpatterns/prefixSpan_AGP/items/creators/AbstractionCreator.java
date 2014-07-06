package ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.creators;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Pair;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.PseudoSequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.patterns.Pattern;

/**
 * Abstract class that is thought to make it possible the creation of any kind
 * of abstractions.
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
public abstract class AbstractionCreator {
  
    public abstract Abstraction_Generic CreateDefaultAbstraction();

    public abstract Map<Item, Set<Abstraction_Generic>> createAbstractions(Sequence sequence, Map<Item, BitSet> frequentItems);

    public abstract Set<Pair> findAllFrequentPairs(List<PseudoSequence> sequences);

    public abstract Abstraction_Generic createAbstractionFromAPrefix(Pattern prefix,Abstraction_Generic abstraction);
}
