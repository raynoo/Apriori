package edu.dm.object;

import java.util.ArrayList;
import java.util.Collection;

public class AssociationRule implements Comparable<AssociationRule> {

	String premise; //comma separated values
	String consequence;
	
	int premiseSupport;
	int premiseConfidence;
	int consequenceSupport;
	int consequenceConfidence;
	
	int totalSupport;
	int totalConfidence;
	
	public String getPremise(){
		return premise;
	}

	public String getConsequence(){
		return consequence;
	}
	
	public int getPremiseSupport() {
		return premiseSupport;
	}

	public void setPremiseSupport(int premiseSupport) {
		this.premiseSupport = premiseSupport;
	}

	public int getPremiseConfidence() {
		return premiseConfidence;
	}

	public void setPremiseConfidence(int premiseConfidence) {
		this.premiseConfidence = premiseConfidence;
	}

	public int getConsequenceSupport() {
		return consequenceSupport;
	}

	public void setConsequenceSupport(int consequenceSupport) {
		this.consequenceSupport = consequenceSupport;
	}

	public int getConsequenceConfidence() {
		return consequenceConfidence;
	}

	public void setConsequenceConfidence(int consequenceConfidence) {
		this.consequenceConfidence = consequenceConfidence;
	}

	public int getTotalSupport() {
		return totalSupport;
	}

	public void setTotalSupport(int totalSupport) {
		this.totalSupport = totalSupport;
	}

	public int getTotalConfidence() {
		return totalConfidence;
	}

	public void setTotalConfidence(int totalConfidence) {
		this.totalConfidence = totalConfidence;
	}

	public void setPremise(String premise) {
		this.premise = premise;
	}

	public void setConsequence(String consequence) {
		this.consequence = consequence;
	}

	public int compareTo(AssociationRule other) {
    	return -Double.compare(getTotalConfidence(), other.getTotalConfidence());
  	}

	public boolean equals(Object other) {
		if (!(other instanceof AssociationRule)) {
			return false;
		}

		AssociationRule otherRule = (AssociationRule)other;
		boolean result = (this.getPremise().equals(otherRule.getPremise()) &&
			this.getConsequence().equals(otherRule.getConsequence())) ||
			(this.getPremise().equals(otherRule.getConsequence()) &&
				this.getConsequence().equals(otherRule.getPremise()))
			;
		
		return result;
	}

	@Override
	public String toString() {
		
		return this.getPremise() + " --> " + this.getConsequence();
	}

	public boolean containsItems(ArrayList<String> items, boolean useOr) {
		int numItems = items.size();
		int count = 0;

//		for (String i : getPremise()) {
		String i = getPremise();
			if (items.contains(i)) {
				if (useOr) {
					return true; // can stop here
				} else {
					count++;
				}
			}
//		}

//		for (String i : getConsequence()) {
		i = getConsequence();
			if (items.contains(i)) {
				if (useOr) {
					return true; // can stop here
				} else {
					count++;
				}
			}
//		}

		if (!useOr) {
			if (count == numItems) {
				return true;
			}
		}

		return false;
	}
}