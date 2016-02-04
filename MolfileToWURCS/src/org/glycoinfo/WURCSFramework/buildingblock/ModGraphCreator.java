package org.glycoinfo.WURCSFramework.buildingblock;

import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.ChemicalGraph;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

/**
 * Class for creating subgraph
 * @author MasaakiMatsubara
 *
 */
public class ModGraphCreator {

	private ModGraph m_oModGraph;
	private ChemicalGraph m_oOriginalGraph;
	private HashSet<Connection> m_aAddedExternalConnections;

	public ModGraphCreator(ChemicalGraph a_oOriginal) {
		this.m_oOriginalGraph = a_oOriginal;
		this.m_oModGraph = new ModGraph(a_oOriginal);
	}

	private void clear() {
		this.m_aAddedExternalConnections = new HashSet<Connection>();
	}

	public ModGraph getSubGraph() {
		return this.m_oModGraph;
	}

	/**
	 * Judge hydrogen
	 * @return true if created modification subgraph is hydrogen
	 */
	public boolean isHydrogen() {
		LinkedList<Atom> t_oAtoms = this.m_oModGraph.getAtoms();
		return ( t_oAtoms.size() == 2 && t_oAtoms.getFirst().getSymbol().equals("H") );
	}

	/**
	 * Judge hydroxyl group
	 * @return true if created subgraph is hydroxy
	 */
	public boolean isHydroxy() {
		LinkedList<Atom> t_oAtoms = this.m_oModGraph.getAtoms();
		LinkedList<Bond> t_oBonds = this.m_oModGraph.getBonds();
		if ( t_oAtoms.size() != 3 && t_oBonds.size() != 2 ) return false;
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
	 * Start to create modification subgraph by recursively expanding from start atom in original chemical graph except for backbone carbons.
	 * @param a_oStart Start atom of this chemical graph
	 * @param a_aBackboneCarbons HashSet of Backbone carbons
	 */
	public void start(Atom a_oStart, HashSet<Atom> a_aBackboneCarbons) {
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
				if ( a_aBackboneCarbons.contains(t_oConnAtom) ) continue;

				// Add bond
				if ( !t_aConnectedBonds.contains(t_oConn.getBond()) )
					t_aConnectedBonds.add(t_oConn.getBond());

				if ( t_aConnectedAtoms.contains(t_oConnAtom) ) continue;
				if ( !t_oAtomGroup.contains(t_oConnAtom) )
					t_oAtomGroup.addLast(t_oConnAtom);
			}
		}

		// Build subgraph
		for ( Atom t_oAtom : t_aConnectedAtoms )
			this.m_oModGraph.copyOriginal(t_oAtom);
		for ( Bond t_oBond : t_aConnectedBonds )
			this.m_oModGraph.copyOriginal(t_oBond);

		// Add Backbone carbons
		this.addBackboneCarbons(a_aBackboneCarbons);
	}

	private void addBackboneCarbons( HashSet<Atom> t_aBackboneCarbons ) {
		for ( Connection t_oExConnOrig : this.getExternalOriginalConnections() ) {
			if ( !t_aBackboneCarbons.contains( t_oExConnOrig.endAtom() ) ) continue;

			// Add connection from backbone carbon
			this.m_oModGraph.addOriginalConnectionFromBackbone( t_oExConnOrig.getReverse() );
		}
	}

	/**
	 * Get connections directed from subgraph to external atoms in original chemical graph.
	 * @return HashSet of original connections directed from subgraph to external atoms
	 */
	private HashSet<Connection> getExternalOriginalConnections() {
		// Collect original atoms
		HashSet<Atom> t_oOriginalAtoms = new HashSet<Atom>();
		for ( Atom t_oAtom : this.m_oModGraph.getAtoms() )
			t_oOriginalAtoms.add(this.m_oModGraph.getOriginal(t_oAtom));

		// Collect external connections
		HashSet<Connection> t_oExternalConnections = new HashSet<Connection>();
		for ( Atom t_oOriginalAtom : t_oOriginalAtoms ) {
			for ( Connection t_oOriginalConn : t_oOriginalAtom.getConnections() ) {
				if ( t_oOriginalAtoms.contains(t_oOriginalConn.endAtom()) ) continue;

				t_oExternalConnections.add(t_oOriginalConn);
			}
		}
		return t_oExternalConnections;
	}

	/*
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
*/
}
