package org.glycoinfo.ChemicalStructureUtility.util.analytical.cyclization;

import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

/**
 * Class for finding cyclic atoms
 * @author MasaakiMatsubara
 *
 */
public class Cyclization extends LinkedList<Atom> {

	private static final long serialVersionUID = 1L;

	protected boolean m_bIsSucceeded;

	public void clear() {
		super.clear();
		this.m_bIsSucceeded = false;
	}

	public boolean isSixMembered() {
		return (this.size() == 6);
	}

	public boolean isFiveMembered() {
		return (this.size() == 5);
	}

	public boolean isSevenMembered() {
		return (this.size() == 7);
	}

	protected boolean isCyclic() {
		return (this.size() > 2) && (this.getLast() == this.getFirst());
	}

	/**
	 * Start cyclization from a start atom
	 * @param a_oStart Atom as start node
	 * @return true if cyclic atom group contained start atom is found
	 */
	public boolean start(final Atom a_oStart) {
		this.clear();
		if ( !this.isTargetAtom(a_oStart) )
			return false;
		this.addFirst(a_oStart);
		this.search();
		return this.m_bIsSucceeded;
	}

	protected boolean isTargetAtom(Atom a_oAtom) {
		return true;
	}

	/**
	 * Judging break point for cyclization
	 * @return true if break point is reached
	 */
	protected boolean isBreak() {
		if ( this.isCyclic() ) {
			this.m_bIsSucceeded = true;
			return true;
		}
		return false;
	}

	private void search() {
		int t_iDepth = 3;
		while ( true ) {
			int t_iMinDepth = this.depthSearch(t_iDepth);
			if ( this.m_bIsSucceeded ) break;
			if ( this.size() == 1 && t_iMinDepth != 0 ) break;
			t_iDepth++;
		}
	}

	private int depthSearch(int a_iDepth) {
		// Return if depth has reached
		if ( a_iDepth == 0 ) return 0;

		if ( this.isBreak() ) return a_iDepth;

		// Depth search
		int t_iMinDepth = a_iDepth;
		for ( Connection t_oConn : this.getLast().getConnections() ) {
			Atom t_oConnAtom = t_oConn.endAtom();
			if ( !this.isTargetAtom(t_oConnAtom) ) continue;
			if ( this.contains(t_oConnAtom) && ( t_oConnAtom != this.getFirst() ) ) continue;
			if ( this.contains(t_oConnAtom) && this.size() < 3) continue;
			this.addLast(t_oConnAtom);
			int t_iDepth = this.depthSearch( a_iDepth - 1 );
			if ( t_iMinDepth > t_iDepth ) t_iMinDepth = t_iDepth;
			if ( this.m_bIsSucceeded ) return t_iMinDepth;
			this.removeLast();
		}
		return t_iMinDepth;
	}


}
