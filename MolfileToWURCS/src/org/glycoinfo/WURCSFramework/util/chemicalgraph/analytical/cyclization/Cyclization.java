package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.cyclization;

import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

/**
 * Class for finding cyclic atoms
 * @author MasaakiMatsubara
 *
 */
public class Cyclization extends LinkedList<Atom>{

	private static final long serialVersionUID = 1L;

	protected boolean m_bIsSucceeded;
	protected HashSet<Atom> m_aNeighborAtoms;
	protected LinkedList<Atom> m_aNeighborChain;

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

	protected boolean isCyclicWithNeighbor() {
		return ( this.m_aNeighborAtoms.contains( this.m_aNeighborChain.getFirst() )
				&& this.m_aNeighborAtoms.contains( this.m_aNeighborChain.getLast() )
				&& this.m_aNeighborChain.getFirst() != this.m_aNeighborChain.getLast() );
	}

	public boolean start(final Atom a_oStart) {
		this.clear();
		this.addFirst(a_oStart);
//		this.depthSearch();
		this.search();
		return this.m_bIsSucceeded;
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

	protected boolean isBreakWithNeighbor() {
		// Find cyclic
		if ( this.isCyclicWithNeighbor() ){
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

	private void search() {
		while ( true ) {
			// Reset flag
			this.m_bIsSucceeded = false;

			// Collect neighbor atoms
			this.m_aNeighborAtoms = new HashSet<Atom>();
			for ( Atom t_oAtom : this ) {
				for ( Connection t_oConn : t_oAtom.getConnections() ) {
					Atom t_oConnAtom = t_oConn.endAtom();
					if ( this.contains(t_oConnAtom) ) continue;
					this.m_aNeighborAtoms.add(t_oConnAtom);
				}
			}
			if ( this.m_aNeighborAtoms.isEmpty() ) break;

			// Start from neighbor atoms
			int t_nAtom = this.size();
			for ( Atom t_oNeighbor : this.m_aNeighborAtoms ) {
				this.m_aNeighborChain = new LinkedList<Atom>();
				this.m_aNeighborChain.addFirst(t_oNeighbor);
				this.depthSearchNeighbor();
				if ( !this.m_bIsSucceeded ) continue;
				this.addAll( this.m_aNeighborChain );
				break;
			}

			String t_strHistory = "";
			for ( Atom t_oAtom : this ) {
				if ( !t_strHistory.equals("") ) t_strHistory += ",";
				t_strHistory += t_oAtom.getAtomID();
			}
			System.err.println(t_strHistory);

			if ( t_nAtom == this.size() ) break;
		}
	}

	private void depthSearchNeighbor() {
		String t_strHistory = "";
		for ( Atom t_oAtom : this.m_aNeighborChain ) {
			if ( !t_strHistory.equals("") ) t_strHistory += "-";
			t_strHistory += t_oAtom.getAtomID();
		}
		System.err.println(this.getClass().getName() +":"+ t_strHistory);

		if ( this.isBreakWithNeighbor() ) return;

		// Depth search
		for ( Connection t_oConn : this.m_aNeighborChain.getLast().getConnections() ) {
			Atom t_oConnAtom = t_oConn.endAtom();
			if ( this.contains(t_oConnAtom) ) continue;
//			if ( t_oConnAtom.equals( this.m_aNeighborChain.getFirst() ) ) continue;
			if ( this.m_aNeighborChain.contains( t_oConnAtom ) ) continue;
			this.m_aNeighborChain.addLast(t_oConnAtom);
			this.depthSearchNeighbor();
			if ( this.m_bIsSucceeded ) return;
			this.m_aNeighborChain.removeLast();
		}
	}
}
