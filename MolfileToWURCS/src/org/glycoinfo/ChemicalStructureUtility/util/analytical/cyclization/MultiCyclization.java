package org.glycoinfo.ChemicalStructureUtility.util.analytical.cyclization;

import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

/**
 * Class for finding multi cyclic atoms
 * @author MasaakiMatsubara
 *
 */
public class MultiCyclization extends LinkedList<Atom>{

	private static final long serialVersionUID = 1L;

	protected boolean m_bIsSucceeded;
	protected HashSet<Atom> m_aNeighborAtoms;
	protected LinkedList<Atom> m_aNeighborChain;

	public void clear() {
		super.clear();
		this.m_bIsSucceeded = false;
	}

	private boolean isCyclicWithNeighbor() {
		return ( this.m_aNeighborAtoms.contains( this.m_aNeighborChain.getFirst() )
				&& this.m_aNeighborAtoms.contains( this.m_aNeighborChain.getLast() )
				&& this.m_aNeighborChain.getFirst() != this.m_aNeighborChain.getLast() );
	}

	public boolean start(final Atom a_oStart) {
		this.clear();
		this.addFirst(a_oStart);
//		this.depthSearch();
		this.search();
		return (this.size() > 1);
	}

	/**
	 * Judging break point for cyclization
	 * @return true if break point is reached
	 */
	protected boolean isBreakWithNeighbor() {
		return false;
	}

	private void search() {
		int t_iDepth = 5;
		while ( !this.searchNeighbor(t_iDepth) ) {
			t_iDepth++;
		}
/*
		String t_strHistory = "";
		for ( Atom t_oAtom : this ) {
			if ( !t_strHistory.equals("") ) t_strHistory += ",";
			t_strHistory += t_oAtom.getAtomID();
		}
		t_strHistory += "\n"+this.size()+":"+t_iDepth;
		System.err.println(t_strHistory);
*/
	}

	/**
	 * Search atom groups with depth
	 * @param a_iDepth
	 * @return true if search is finished
	 */
	private boolean searchNeighbor(int a_iDepth) {
		// Reset flag
		this.m_bIsSucceeded = false;

		// Collect neighbor atoms
		this.m_aNeighborAtoms = this.collectNeighborAtoms();
		if ( this.m_aNeighborAtoms.isEmpty() ) return true;

		// Start depth search from neighbor atoms
		HashSet<Atom> t_aBridgedAtoms = new HashSet<Atom>();
		int t_iMinDepth = a_iDepth;
		for ( Atom t_oNeighbor : this.m_aNeighborAtoms ) {
			this.m_aNeighborChain = new LinkedList<Atom>();
			this.m_aNeighborChain.addFirst(t_oNeighbor);
			t_iMinDepth = this.depthSearchNeighbor(a_iDepth);
			if ( !this.m_bIsSucceeded ) continue;
			t_aBridgedAtoms.addAll( this.m_aNeighborChain );
			break;
		}
		if ( t_aBridgedAtoms.isEmpty() ){
			if ( t_iMinDepth == 0 ) return false;
			return true;
		}
		this.addAll(t_aBridgedAtoms);

		return searchNeighbor(a_iDepth);
	}

	/**
	 * Collect neighbor atoms connected this atom group
	 * @return t_aNeighborAtoms neighbor atoms around this atom group
	 */
	private HashSet<Atom> collectNeighborAtoms() {
		HashSet<Atom> t_aNeighborAtoms = new HashSet<Atom>();
		while ( true ) {
			t_aNeighborAtoms = new HashSet<Atom>();
			HashSet<Atom> t_aBridgedAtoms = new HashSet<Atom>();
			for ( Atom t_oAtom : this ) {
				for ( Connection t_oConn : t_oAtom.getConnections() ) {
					Atom t_oConnAtom = t_oConn.endAtom();
					if ( this.contains(t_oConnAtom) ) continue;
					// Check bridged atom
					if ( t_aNeighborAtoms.contains(t_oConnAtom) )
						t_aBridgedAtoms.add(t_oConnAtom);
					t_aNeighborAtoms.add(t_oConnAtom);
				}
			}
			if ( t_aBridgedAtoms.isEmpty() ) break;

			// Add bridged atoms to this atom group
			this.addAll(t_aBridgedAtoms);
		}

		return t_aNeighborAtoms;
	}

	/**
	 * Depth search for finding atom chain bridging cyclic atom group
	 * @param a_iDepth depth of search
	 * @return minimum depth value
	 */
	private int depthSearchNeighbor(int a_iDepth) {
		// Return if maximum depth is reached
		if ( a_iDepth == 0 ) return a_iDepth;
/*
		String t_strHistory = "";
		for ( Atom t_oAtom : this.m_aNeighborChain ) {
			if ( !t_strHistory.equals("") ) t_strHistory += "-";
			t_strHistory += t_oAtom.getAtomID();
		}
		System.err.println(this.getClass().getSimpleName() +":"+ t_strHistory);
*/
		// Jadge break point
		if ( this.isBreakWithNeighbor() ) return a_iDepth;

		// Find cyclic part
		if ( this.isCyclicWithNeighbor() ){
			this.m_bIsSucceeded = true;
			return a_iDepth;
		}

		// Depth search
		int t_iMinDepth = a_iDepth;
		for ( Connection t_oConn : this.m_aNeighborChain.getLast().getConnections() ) {
			Atom t_oConnAtom = t_oConn.endAtom();
			if ( this.contains(t_oConnAtom) ) continue;
//			if ( t_oConnAtom.equals( this.m_aNeighborChain.getFirst() ) ) continue;
			if ( this.m_aNeighborChain.contains(t_oConnAtom) ) continue;
			this.m_aNeighborChain.addLast(t_oConnAtom);
			int t_iDepth = this.depthSearchNeighbor(a_iDepth-1);
			if ( t_iMinDepth > t_iDepth ) t_iMinDepth = t_iDepth;
			if ( this.m_bIsSucceeded ) return t_iMinDepth;
			this.m_aNeighborChain.removeLast();
		}
		return t_iMinDepth;
	}
}
