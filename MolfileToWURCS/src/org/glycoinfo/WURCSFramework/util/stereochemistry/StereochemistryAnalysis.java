package org.glycoinfo.WURCSFramework.util.stereochemistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Bond;
import org.glycoinfo.WURCSFramework.chemicalgraph.ChemicalGraph;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.Chemical;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.AtomIdentifier;

public class StereochemistryAnalysis {

	private HashMap<Atom, String> m_mapAtomToStereo = new HashMap<Atom, String>();
	private HashMap<Bond, String> m_mapBondToStereo = new HashMap<Bond, String>();

	public String getAtomStereo(Atom a_oAtom) {
		return this.m_mapAtomToStereo.get(a_oAtom);
	}

	public String getBondStereo(Bond a_oBond) {
		return this.m_mapBondToStereo.get(a_oBond);
	}

	public void setStereoTo(ChemicalGraph a_oGraph) {
		// Set stereo for atoms
		for ( Atom t_oAtom : a_oGraph.getAtoms() ) {
			t_oAtom.setChirality( this.m_mapAtomToStereo.get(t_oAtom) );
			if ( this.m_mapAtomToStereo.get(t_oAtom) == null ) continue;
			System.err.println( t_oAtom.getSymbol()+"("+t_oAtom.getAtomID()+"): "+this.m_mapAtomToStereo.get(t_oAtom) );
		}
		// Set cis-trans for bonds
		for ( Bond t_oBond : a_oGraph.getBonds() ) {
			t_oBond.setGeometric( this.m_mapBondToStereo.get(t_oBond) );
			if ( this.m_mapBondToStereo.get(t_oBond) == null ) continue;
			System.err.println( t_oBond.getAtom1().getSymbol()+"("+t_oBond.getAtom1().getAtomID()+")="+t_oBond.getAtom2().getSymbol()+"("+t_oBond.getAtom2().getAtomID()+"): "+this.m_mapBondToStereo.get(t_oBond) );
		}
		// XXX: remove print
		System.err.println();
	}

	public void start(ChemicalGraph a_oGraph) {
		HashMap<Atom, String> t_mapAtomToStereo = new HashMap<Atom, String>();
		LinkedList<Atom> t_aCandidateStereoAtoms = new LinkedList<Atom>();
		AtomIdentifier t_oIdent = new AtomIdentifier();
		for ( Atom t_oAtom : a_oGraph.getAtoms() ) {
			if ( !t_oIdent.setAtom(t_oAtom).getHybridOrbital0().equals("sp3") ) continue;
			if ( t_oAtom.getConnections().size() != 4 ) continue;
			t_aCandidateStereoAtoms.add(t_oAtom);
		}

		// Calculate RS
		HierarchicalDigraphComparator t_oHDComp = new HierarchicalDigraphComparator();
		for ( Atom t_oAtom : t_aCandidateStereoAtoms ) {
			this.m_mapAtomToStereo.put(t_oAtom, this.calcChirality(t_oAtom, t_oHDComp) );
		}

		// Calculate EZ for double bonds
		HashMap<Bond, String> t_mapBondToStereo = new HashMap<Bond, String>();
		LinkedList<Bond> t_aCandidateIsomerismBond = new LinkedList<Bond>();
		for ( Bond t_oBond : a_oGraph.getBonds() ) {
			if ( t_oBond.getType() != 2 ) continue;
			t_aCandidateIsomerismBond.add(t_oBond);
			this.m_mapBondToStereo.put(t_oBond, this.calcIsomerism(t_oBond, t_oHDComp) );
		}

		// Calculate rs
		HierarchicalDigraphComparatorWithStereo t_oHDComp2 = new HierarchicalDigraphComparatorWithStereo();
		t_oHDComp2.setAtomStereos(t_mapAtomToStereo);
		t_oHDComp2.setBondStereos(t_mapBondToStereo);
		for ( Atom t_oAtom : t_aCandidateStereoAtoms ) {
			if ( this.m_mapAtomToStereo.get(t_oAtom) != null ) continue;
			String t_strStereo = this.calcChirality(t_oAtom, t_oHDComp2);
			if ( t_strStereo != null ) t_strStereo = t_strStereo.toLowerCase();
			this.m_mapAtomToStereo.put(t_oAtom, t_strStereo );
		}
	}

	protected HierarchicalDigraphCreator getHierarchicalDigraphCreator( Connection a_oConnection, int a_iDepth ) {
		return new HierarchicalDigraphCreator( a_oConnection, a_iDepth );
	}

	/**
	 * Calc and get chirality of target atom
	 * @param a_oAtom Target atom
	 * @param a_oHDComp Comparator for HierarchicalDigraph
	 * @return String of chirarity ("S" or "R", null if no chirality)
	 */
	private String calcChirality(Atom a_oAtom, HierarchicalDigraphComparator a_oHDComp) {
		// Sort connections on atom
		LinkedList<Connection> t_aSortedConnections = this.sortConnectionsByCIPOrder(a_oAtom.getConnections(), a_oHDComp);
		if ( t_aSortedConnections == null ) return null;
		return Chemical.sp3stereo(
				t_aSortedConnections.get(0),
				t_aSortedConnections.get(1),
				t_aSortedConnections.get(2),
				t_aSortedConnections.get(3)
				);
	}

	/**
	 * Calc and get geometrical isomerism of target double bond
	 * @param a_oBond Target double bond
	 * @param a_oHDComp Comparator for HierarchicalDigraph
	 * @return String of isomerism ("E" or "Z", null if no isomerism)
	 */
	private String calcIsomerism(Bond a_oBond, HierarchicalDigraphComparator a_oHDComp) {
		Atom t_oA0 = a_oBond.getAtom1();
		Atom t_oB0 = a_oBond.getAtom2();

		// Sort connections on A0 except for double bond
		LinkedList<Connection> t_aConnections1 = new LinkedList<Connection>();
		for ( Connection t_oConn : t_oA0.getConnections() ) {
			if ( t_oConn.endAtom().equals(t_oB0) ) continue;
			t_aConnections1.add(t_oConn);
		}
		if ( t_aConnections1.isEmpty() ) return null;
		Atom t_oA1 = t_aConnections1.getFirst().endAtom();
		if ( t_aConnections1.size() == 1 && t_oA1.getSymbol().equals("H") ) return null;
		if ( t_aConnections1.size() > 1 ) {
			LinkedList<Connection> t_aSortedConnections =  this.sortConnectionsByCIPOrder(t_aConnections1, a_oHDComp);
			if ( t_aSortedConnections == null ) return null;
			t_oA1 = t_aSortedConnections.getFirst().endAtom();
		}

		// Sort connections on B0 except for double bond
		LinkedList<Connection> t_aConnections2 = new LinkedList<Connection>();
		for ( Connection t_oConn : t_oB0.getConnections() ) {
			if ( t_oConn.endAtom().equals(t_oA0) ) continue;
			t_aConnections2.add(t_oConn);
		}
		if ( t_aConnections2.isEmpty() ) return null;
		Atom t_oB1 = t_aConnections2.getFirst().endAtom();
		if ( t_aConnections2.size() == 1 &&  t_oB1.getSymbol().equals("H") ) return null;
		if ( t_aConnections2.size() > 1 ) {
			LinkedList<Connection> t_aSortedConnections =  this.sortConnectionsByCIPOrder(t_aConnections2, a_oHDComp);
			if ( t_aSortedConnections == null ) return null;
			t_oB1 = t_aSortedConnections.getFirst().endAtom();
		}
		return Chemical.sp2stereo(t_oA0, t_oA1, t_oB0, t_oB1);
	}

	/**
	 * Sort connections by CIP order using hierarchical digraph
	 * @param a_aConns Target connections
	 * @param a_oHDComp Comparator for HierarchicalDigraph
	 * @return Sorted connections (null if connections are not unique order)
	 */
	private LinkedList<Connection> sortConnectionsByCIPOrder( LinkedList<Connection> a_aConns, HierarchicalDigraphComparator a_oHDComp ) {
		int t_iDepth = 1;
		while ( true ) {
			// Calcurate CIP orders for each connection using HierarchicalDigraph
			boolean t_bIsCompletedFullSearch = true;

			// Create hierarchical digraph starting from connections on the atom
			LinkedList<HierarchicalDigraphNode> t_aChildHDs = new LinkedList<HierarchicalDigraphNode>();
			HashMap<HierarchicalDigraphNode, Boolean> t_mapIsCompletedFullSearch = new HashMap<HierarchicalDigraphNode, Boolean>();
			for ( Connection t_oConn : a_aConns ) {
				HierarchicalDigraphCreator t_oHDCreate = this.getHierarchicalDigraphCreator(t_oConn, t_iDepth);
				t_mapIsCompletedFullSearch.put(t_oHDCreate.getHierarchicalDigraph(), t_oHDCreate.isCompletedFullSearch());
				if ( !t_oHDCreate.isCompletedFullSearch() )
					t_bIsCompletedFullSearch = false;
				t_aChildHDs.addLast( t_oHDCreate.getHierarchicalDigraph() );
			}

			// Sort and order hierarchical digraphs
			Collections.sort(t_aChildHDs, a_oHDComp);
			LinkedList<Connection> t_aSortedConnections = new LinkedList<Connection>();
			HierarchicalDigraphNode t_oPreHD = t_aChildHDs.getFirst();
			t_aSortedConnections.addFirst( t_oPreHD.getConnection() );
			boolean t_bIsUniqueOrder = true;
			for ( HierarchicalDigraphNode t_oHD : t_aChildHDs ) {
				if ( t_oHD.equals(t_oPreHD) ) continue;
				if ( a_oHDComp.compare(t_oPreHD, t_oHD) == 0 ) {
					t_bIsUniqueOrder = false;
					if ( t_mapIsCompletedFullSearch.get(t_oPreHD) && t_mapIsCompletedFullSearch.get(t_oHD) )
						t_bIsCompletedFullSearch = true;
				}
				t_aSortedConnections.addLast( t_oHD.getConnection() );
				t_oPreHD = t_oHD;
			}
			if ( t_bIsUniqueOrder ) return t_aSortedConnections;

			if ( t_bIsCompletedFullSearch ) return null;

			t_iDepth++;
		}

	}
}
