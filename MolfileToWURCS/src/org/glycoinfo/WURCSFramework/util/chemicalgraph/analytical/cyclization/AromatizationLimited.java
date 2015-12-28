package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.cyclization;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

public class AromatizationLimited extends Aromatization {

	@Override
	public boolean start(final Atom a_oStart) {
		this.clear();
		this.addFirst(a_oStart);
		this.depthSearch();
		return this.m_bIsSucceeded;
	}

	@Override
	protected boolean isBreak() {
		if ( this.getLast().getNumberOfPiElectron() == 0 ) return true;

		if ( this.size() > 8 ) return true;

		if ( this.isCyclic() && this.isSatisfiedHuckelsRule() &&
		   ( this.isSixMembered() || this.isFiveMembered() || this.isSevenMembered() ) ) {
			this.m_bIsSucceeded = true;
			return true;
		}
		return false;
	}

	@Override
	protected boolean isBreakWithNeighbor() {
		if(this.getLast().getNumberOfPiElectron() == 0) return true;

		if ( this.m_aNeighborChain.size() > 7 ) return true;

			// Find cyclic
		if ( this.isCyclicWithNeighbor() ) {
			if ( this.isSatisfiedHuckelsRule() )
				this.m_bIsSucceeded = true;
			return true;
		}

		return false;
	}

	private void depthSearch() {
		String t_strHistory = "";
		for ( Atom t_oAtom : this ) {
			if ( !t_strHistory.equals("") ) t_strHistory += "-";
			t_strHistory += t_oAtom.getAtomID();
		}
		System.err.println(t_strHistory);

		if ( this.isBreak() ) return;

		// Depth search
		for(Connection connect : this.getLast().getConnections()){
			Atom conAtom = connect.endAtom();
			if(this.contains(conAtom) && (conAtom != this.getFirst())) continue;
			if(this.contains(conAtom) && this.size() < 3) continue;
			this.addLast(conAtom);
			this.depthSearch();
			this.removeLast();
			if ( this.m_bIsSucceeded ) return;
		}
	}

	/**
	 * Return true if the member of this list satisfied the huckels rule.
	 * @return true if the member of this list satisfied the huckels rule
	 */
	protected boolean isSatisfiedHuckelsRule(){
		LinkedList<Atom> uniqAtom = new LinkedList<Atom>();
		for ( Atom atom : this ) {
			if ( uniqAtom.contains(atom) ) continue;
			uniqAtom.add(atom);
		}

		int pi_num = 0;
		for ( Atom atom : uniqAtom ) {
//			pi_num += atom.pi;
			// TODO:
			pi_num += atom.getNumberOfPiElectron();
		}

		return ((pi_num - 2) % 4 == 0) ? true : false;
	}

}
