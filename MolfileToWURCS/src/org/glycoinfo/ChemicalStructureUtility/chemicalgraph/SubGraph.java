package org.glycoinfo.ChemicalStructureUtility.chemicalgraph;

import java.util.HashMap;
import java.util.LinkedList;

public class SubGraph extends ChemicalGraph {

	protected ChemicalGraph m_oOriginalGraph;
	protected HashMap<Atom, Atom> m_mapAtomToOriginal = new HashMap<Atom, Atom>();
	protected HashMap<Atom, Atom> m_mapOriginalToAtom = new HashMap<Atom, Atom>();
	protected HashMap<Bond, Bond> m_mapBondToOriginal = new HashMap<Bond, Bond>();
	protected HashMap<Bond, Bond> m_mapOriginalToBond = new HashMap<Bond, Bond>();
	protected HashMap<Connection, Connection> m_mapConnectionToOriginal = new HashMap<Connection, Connection>();

	public SubGraph(ChemicalGraph a_oOriginalGraph) {
		this.m_oOriginalGraph = a_oOriginalGraph;
	}

	public boolean copyOriginal( Atom a_oOriginal ) {
		// Return false if original graph does not have the atom
		if ( !this.m_oOriginalGraph.contains(a_oOriginal) ) return false;
		// Return false if already added
		if ( this.m_mapOriginalToAtom.containsKey(a_oOriginal) ) return false;

		Atom t_oAtom = a_oOriginal.copy();
		this.m_aAtoms.addLast(t_oAtom);
		this.m_mapAtomToOriginal.put(t_oAtom, a_oOriginal);
		this.m_mapOriginalToAtom.put(a_oOriginal, t_oAtom);
		return true;
	}

	public boolean copyOriginal( Bond a_oOriginal ) {
		// Return false if original graph does not have the bond
		if ( !this.m_oOriginalGraph.contains(a_oOriginal) ) return false;
		// Return false if already added
		if ( this.m_mapOriginalToBond.containsKey(a_oOriginal) ) return false;
		// Return false if this graph does not have connecting atoms
		if ( !this.m_mapOriginalToAtom.containsKey( a_oOriginal.getAtom1() )
		  && !this.m_mapOriginalToAtom.containsKey( a_oOriginal.getAtom2() ) ) return false;

		Atom t_oAtom1 = this.m_mapOriginalToAtom.get( a_oOriginal.getAtom1() );
		Atom t_oAtom2 = this.m_mapOriginalToAtom.get( a_oOriginal.getAtom2() );
		Bond t_oBond = a_oOriginal.copy(t_oAtom1, t_oAtom2);
		this.m_aBonds.addLast(t_oBond);
		this.m_mapBondToOriginal.put(t_oBond, a_oOriginal);
		this.m_mapOriginalToBond.put(a_oOriginal, t_oBond);

		for ( Connection t_oConn : t_oAtom1.getConnections() ) {
			if ( !t_oConn.getBond().equals(t_oBond) ) continue;
			for ( Connection t_oConnOriginal : a_oOriginal.getAtom1().getConnections() ) {
				if ( !t_oConnOriginal.getBond().equals(a_oOriginal) ) continue;
				this.m_mapConnectionToOriginal.put(t_oConn, t_oConnOriginal);
				this.m_mapConnectionToOriginal.put(t_oConn.getReverse(), t_oConnOriginal.getReverse());
			}
		}
		return true;
	}

	public ChemicalGraph getOriginalGraph() {
		return this.m_oOriginalGraph;
	}

	public LinkedList<Atom> getOriginalAtoms() {
		LinkedList<Atom> t_oOriginals = new LinkedList<Atom>();
		for ( Atom t_oSubAtom : this.m_aAtoms ) {
			t_oOriginals.add( this.m_mapAtomToOriginal.get(t_oSubAtom) );
		}
		return t_oOriginals;
	}

	public Atom getOriginal(Atom a_oAtom) {
		return this.m_mapAtomToOriginal.get(a_oAtom);
	}

	public Bond getOriginal(Bond a_oBond) {
		return this.m_mapBondToOriginal.get(a_oBond);
	}

	public Connection getOriginal(Connection a_oConn) {
		return this.m_mapConnectionToOriginal.get(a_oConn);
	}

}
