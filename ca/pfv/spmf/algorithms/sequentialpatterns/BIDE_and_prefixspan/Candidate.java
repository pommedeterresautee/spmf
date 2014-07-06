package ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan;

import java.util.List;

public class Candidate implements Comparable<Candidate>{
	
	SequentialPattern prefix;
	List<PseudoSequence> databaseBeforeProjection;
	Integer item;
	Boolean isPostfix;

	public Candidate(SequentialPattern prefix, List<PseudoSequence>  databaseBeforeProjection,
			Integer item, Boolean isPostfix) {
		this.prefix = prefix;
		this.databaseBeforeProjection = databaseBeforeProjection;
		this.item = item;
		this.isPostfix = isPostfix;
		
	}
	
	public int compareTo(Candidate o) {
		if(o == this){
			return 0;
		}
		int compare = this.prefix.getAbsoluteSupport() - o.prefix.getAbsoluteSupport();
		if(compare !=0){
			return compare;
		}
		compare = this.hashCode() - o.hashCode();
		if(compare !=0){
			return compare;
		}
		compare = this.item - o.item;
		if(compare !=0){
			return compare;
		}
		return  prefix.size() - prefix.size();
	}

}
