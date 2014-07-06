package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators;

import java.util.List;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.EquivalenceClass;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator;

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

    public abstract Abstraction_Generic createDefaultAbstraction();

    public abstract List<EquivalenceClass> getFrequentSize2Sequences(List<Sequence> sequences, IdListCreator idListCreator);

    public abstract Pattern getSubpattern(Pattern extension, int i);

    public abstract List<EquivalenceClass> getFrequentSize2Sequences(Map<Integer, Map<Item, List<Integer>>> database, Map<Item, EquivalenceClass> frequentItems, IdListCreator idListCreator);

    public abstract void clear();
}
