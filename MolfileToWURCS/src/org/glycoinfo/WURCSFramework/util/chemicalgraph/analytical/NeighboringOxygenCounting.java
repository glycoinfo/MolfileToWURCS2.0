package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical;

import java.util.LinkedList;

public class NeighboringOxygenCounting extends CarbonChainAnalyzer{

	public String getNOCPass1() {
		String t_strNOCPass1 = "";
		for ( Integer t_nO : this.getOxygenCountSequence() ) {
			if ( !t_strNOCPass1.equals("") ) t_strNOCPass1 += "-";
			t_strNOCPass1 += t_nO;
		}
		return t_strNOCPass1;
	}

	public String getNOCPass2() {
		String t_strNOCPass2 = "";
		LinkedList<Integer> t_aOCountSequence = this.getOxygenCountSequence();
		for ( int i=0; i<t_aOCountSequence.size(); i++ ) {
			if ( !t_strNOCPass2.equals("") ) t_strNOCPass2 += "-";
			int t_iNOCPass2 = t_aOCountSequence.get(i);
			if ( i!=0 ) t_iNOCPass2 += t_aOCountSequence.get(i-1);
			if ( i!=t_aOCountSequence.size()-1 ) t_iNOCPass2 += t_aOCountSequence.get(i+1);
			t_strNOCPass2 += t_iNOCPass2;
		}
		return t_strNOCPass2;
	}

	public String getNOCPass2Ex() {
		String t_strNOCPass2 = "";
		LinkedList<Integer> t_aOCountSequence = this.getOxygenCountSequence();
		for ( int i=0; i<t_aOCountSequence.size(); i++ ) {
			if ( !t_strNOCPass2.equals("") ) t_strNOCPass2 += "-";
			double t_iNOCPass2 = (double)t_aOCountSequence.get(i);
			if ( i!=0 ) t_iNOCPass2 += (double)t_aOCountSequence.get(i-1)/2;
			else t_iNOCPass2 += (double)t_aOCountSequence.get(i+1)/2;
			if ( i!=t_aOCountSequence.size()-1 ) t_iNOCPass2 += (double)t_aOCountSequence.get(i+1)/2;
			else t_iNOCPass2 += (double)t_aOCountSequence.get(i-1)/2;
			t_strNOCPass2 += t_iNOCPass2;
		}
		return t_strNOCPass2;
	}
}
