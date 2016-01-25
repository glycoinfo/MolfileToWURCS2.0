package org.glycoinfo.WURCSFramework.chemicalgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Class for creating subgraph
 * @author MasaakiMatsubara
 *
 */
public class SubGraphCreator {

	private SubGraph m_oSubGraph;
	private ChemicalGraph m_oOriginalGraph;
	private HashMap<Atom, Atom> m_mapAtomToOriginal;
//	private HashMap<Atom, Atom> m_mapOriginalToAtom;
	private HashMap<Bond, Bond> m_mapBondToOriginal;
	private HashMap<Connection, Connection> m_mapConnectionToOriginal;

	public SubGraphCreator(ChemicalGraph a_oOriginal) {
		this.m_oOriginalGraph = a_oOriginal;
	}

	private void clear() {
		this.m_oSubGraph = new SubGraph();
		this.m_mapAtomToOriginal = new HashMap<Atom, Atom>();
		this.m_mapBondToOriginal = new HashMap<Bond, Bond>();
		this.m_mapConnectionToOriginal = new HashMap<Connection, Connection>();
	}

	public SubGraph getSubGraph() {
		return this.m_oSubGraph;
	}

	/**
	 * Judge hydrogen
	 * @return true if created subgraph is hydrogen
	 */
	public boolean isHydrogen() {
		LinkedList<Atom> t_oAtoms = this.m_oSubGraph.getAtoms();
		return ( t_oAtoms.size() == 1 && t_oAtoms.getFirst().getSymbol().equals("H") );
	}

	/**
	 * Judge hydroxyl group
	 * @return true if created subgraph is hydroxy
	 */
	public boolean isHydroxy() {
		LinkedList<Atom> t_oAtoms = this.m_oSubGraph.getAtoms();
		LinkedList<Bond> t_oBonds = this.m_oSubGraph.getBonds();
		if ( t_oAtoms.size() != 2 && t_oBonds.size() != 1 ) return false;
		int t_nOHBond = 0;
		for ( Atom t_oAtom : t_oAtoms ) {
			if ( !t_oAtom.getSymbol().equals("O") ) continue;
			for ( Connection t_oConn : t_oAtom.getConnections() ) {
				if ( t_oConn.getBond().getType() != 1 ) continue;
				if ( !t_oConn.endAtom().getSymbol().equals("H") ) continue;
				t_nOHBond++;
			}
		}
		return ( t_nOHBond == 1 );
	}

	/**
	 * Start to create subgraph by recursively expanding from start atom and ignore atoms are not contained.
	 * @param a_oStart Start atom of this chemical graph
	 * @param a_aIgnoreAtoms HashSet of ignore Atoms
	 */
	public void start(Atom a_oStart, HashSet<Atom> a_aIgnoreAtoms) {
		this.clear();

		// Search and collect atoms and bonds connecting from start atom
		HashSet<Atom> t_aConnectedAtoms = new HashSet<Atom>();
		HashSet<Bond> t_aConnectedBonds = new HashSet<Bond>();
		LinkedList<Atom> t_oAtomGroup = new LinkedList<Atom>();
		t_oAtomGroup.addLast(a_oStart);
		while ( !t_oAtomGroup.isEmpty() ) {
			Atom t_oAtom = t_oAtomGroup.removeFirst();
			// Add atom
			t_aConnectedAtoms.add(t_oAtom);
			for ( Connection t_oConn : t_oAtom.getConnections() ) {
				Atom t_oConnAtom = t_oConn.endAtom();
				if ( a_aIgnoreAtoms.contains(t_oConnAtom) ) continue;

				// Add bond
				if ( !t_aConnectedBonds.contains(t_oConn.getBond()) )
					t_aConnectedBonds.add(t_oConn.getBond());

				if ( t_aConnectedAtoms.contains(t_oConnAtom) ) continue;
				if ( !t_oAtomGroup.contains(t_oConnAtom) )
					t_oAtomGroup.addLast(t_oConnAtom);
			}
		}

		// Build subgraph
		String t_strAtoms = "";
		HashMap<Atom, Atom> t_mapOriginalToAtom = new HashMap<Atom, Atom>();
		for ( Atom t_oAtom : t_aConnectedAtoms ) {
			Atom t_oSubAtom = t_oAtom.copy();

			// XXX: remove print;
			if ( !t_strAtoms.equals("") ) t_strAtoms += ",";
			t_strAtoms += t_oAtom.getSymbol()+"("+t_oAtom.getAtomID()+")";

			this.m_mapAtomToOriginal.put(t_oSubAtom, t_oAtom);
			t_mapOriginalToAtom.put(t_oAtom, t_oSubAtom);
			this.m_oSubGraph.add(t_oSubAtom);
		}
		System.err.println(t_strAtoms);
		for ( Bond t_oBond : t_aConnectedBonds ){
			Atom t_oSubAtom1 = t_mapOriginalToAtom.get( t_oBond.getAtom1() );
			Atom t_oSubAtom2 = t_mapOriginalToAtom.get( t_oBond.getAtom2() );

			Bond t_oSubBond = new Bond( t_oSubAtom1, t_oSubAtom2, t_oBond.getType(), t_oBond.getStereo() );
			this.m_mapBondToOriginal.put(t_oSubBond, t_oBond);
			this.m_oSubGraph.add(t_oSubBond);
		}

	}

	/**
	 * Get connections directed from subgraph to external atoms in original chemical graph.
	 * @return HashSet of original connections directed from subgraph to external atoms
	 */
	public HashSet<Connection> getExternalOriginalConnections() {
		HashSet<Connection> t_oExternalConnections = new HashSet<Connection>();
		for ( Atom t_oAtom : this.m_oSubGraph.getAtoms() ) {
			Atom t_oOriginalAtom = this.m_mapAtomToOriginal.get(t_oAtom);
			for ( Connection t_oOriginalConn : t_oOriginalAtom.getConnections() ) {
				Atom t_oOriginalConnAtom = t_oOriginalConn.endAtom();
				if ( this.m_mapAtomToOriginal.containsValue(t_oOriginalConnAtom) ) continue;

				t_oExternalConnections.add(t_oOriginalConn);
			}
		}
		return t_oExternalConnections;
	}

	public ChemicalGraph getOriginalGraph() {
		return this.m_oOriginalGraph;
	}

	public LinkedList<Atom> getOriginalAtoms() {
		LinkedList<Atom> t_oOriginals = new LinkedList<Atom>();
		for ( Atom t_oSubAtom : this.m_oSubGraph.m_aAtoms ) {
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
