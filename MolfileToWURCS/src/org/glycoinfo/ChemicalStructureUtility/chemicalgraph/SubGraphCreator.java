package org.glycoinfo.ChemicalStructureUtility.chemicalgraph;

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
	private HashMap<Bond, Bond> m_mapBondToOriginal;
	private HashMap<Connection, Connection> m_mapConnectionToOriginal;
	private HashSet<Atom> m_aAddedExternalAtoms;

	public SubGraphCreator(ChemicalGraph a_oOriginal) {
		this.m_oOriginalGraph = a_oOriginal;
	}

	private void clear() {
		this.m_oSubGraph = new SubGraph();
		this.m_mapAtomToOriginal = new HashMap<Atom, Atom>();
		this.m_mapBondToOriginal = new HashMap<Bond, Bond>();
		this.m_mapConnectionToOriginal = new HashMap<Connection, Connection>();
		this.m_aAddedExternalAtoms = new HashSet<Atom>();
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

			this.m_oSubGraph.add(t_oSubAtom);
			this.m_mapAtomToOriginal.put(t_oSubAtom, t_oAtom);
			t_mapOriginalToAtom.put(t_oAtom, t_oSubAtom);
		}
		System.err.println(t_strAtoms);
		for ( Bond t_oBond : t_aConnectedBonds ){
			Atom t_oSubAtom1 = t_mapOriginalToAtom.get( t_oBond.getAtom1() );
			Atom t_oSubAtom2 = t_mapOriginalToAtom.get( t_oBond.getAtom2() );

			Bond t_oSubBond = new Bond( t_oSubAtom1, t_oSubAtom2, t_oBond.getType(), t_oBond.getStereo() );
			this.m_oSubGraph.add(t_oSubBond);
			this.m_mapBondToOriginal.put(t_oSubBond, t_oBond);
		}
		// Map connections
		for ( Atom t_oSubStart : this.m_oSubGraph.getAtoms() ) {
			Atom t_oStart = this.m_mapAtomToOriginal.get(t_oSubStart);
			for ( Connection t_oSubConn : t_oSubStart.getConnections() ) {
				Atom t_oEnd = this.m_mapAtomToOriginal.get( t_oSubConn.endAtom() );
				for ( Connection t_oConn : t_oStart.getConnections() ) {
					if ( !t_oEnd.equals(t_oConn.endAtom()) ) continue;
					this.m_mapConnectionToOriginal.put(t_oSubConn , t_oConn);
					break;
				}
			}
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
				if ( this.m_mapConnectionToOriginal.containsValue(t_oOriginalConn) ) continue;

				t_oExternalConnections.add(t_oOriginalConn);
			}
		}
		return t_oExternalConnections;
	}

	public void addExternalConnection( Connection a_oConn ) {
		if ( !this.getExternalOriginalConnections().contains(a_oConn) ) return;

		// Return if the bond already exists in subgraph
		if ( this.m_mapBondToOriginal.containsValue(a_oConn.getBond()) ) return;

		// Get start atom in subgraph
		Atom t_oStartAtom = null;
		for ( Atom t_oAtom : this.m_mapAtomToOriginal.keySet() ) {
			if ( !this.m_mapAtomToOriginal.get(t_oAtom).equals(a_oConn.startAtom()) ) continue;
			t_oStartAtom = t_oAtom;
			break;
		}
		// Return if the atom is not found in subgraph
		if ( t_oStartAtom == null ) return;


		// Create and map end atom in subgraph (Do not add to subgraph)
		Atom t_oEndAtom = a_oConn.endAtom().copy();
//		this.m_oSubGraph.add(t_oEndAtom);
		this.m_mapAtomToOriginal.put(t_oEndAtom, a_oConn.endAtom());
		this.m_aAddedExternalAtoms.add(t_oEndAtom);

		// Create and map bond in subgraph (Do not add to subgraph)
		Bond t_oExBond = a_oConn.getBond();
		Atom t_oSubAtom1 = t_oStartAtom;
		Atom t_oSubAtom2 = t_oEndAtom;
		if ( t_oExBond.getAtom1().equals( a_oConn.endAtom() ) ) {
			t_oSubAtom1 = t_oEndAtom;
			t_oSubAtom2 = t_oStartAtom;
		}
		Bond t_oSubBond = new Bond( t_oSubAtom1, t_oSubAtom2, t_oExBond.getType(), t_oExBond.getStereo() );
//		this.m_oSubGraph.add(t_oSubBond);
		this.m_mapBondToOriginal.put(t_oSubBond, t_oExBond);

		// Map connections
		for ( Connection t_oConn : t_oStartAtom.getConnections() ) {
			if ( !t_oConn.endAtom().equals( t_oEndAtom ) ) continue;
			this.m_mapConnectionToOriginal.put(t_oConn, a_oConn);
			this.m_mapConnectionToOriginal.put(t_oConn.getReverse(), a_oConn.getReverse());
		}
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
