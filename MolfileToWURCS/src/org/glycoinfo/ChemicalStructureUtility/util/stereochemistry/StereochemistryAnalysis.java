package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.ChemicalGraph;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.Chemical;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.AtomIdentifier;

public class StereochemistryAnalysis {

	private HashMap<Atom, String> m_mapAtomToStereo;
	private HashMap<Bond, String> m_mapBondToStereo;
	private AtomicNumberCalculator m_oANumCalc;
	private ConnectionComparatorByCIPOrder m_oCIPComp;
	private StringBuffer m_sbLog;

	public StereochemistryAnalysis() {
		this.m_mapAtomToStereo = new HashMap<Atom, String>();
		this.m_mapBondToStereo = new HashMap<Bond, String>();
		this.m_oANumCalc = new AtomicNumberCalculator();
		this.m_sbLog = new StringBuffer();
	}

	public void setAtomicNumberCalculator(AtomicNumberCalculator a_oANumCalc) {
		this.m_oANumCalc = a_oANumCalc;
	}

	public String getAtomStereo(Atom a_oAtom) {
		return this.m_mapAtomToStereo.get(a_oAtom);
	}

	public String getBondStereo(Bond a_oBond) {
		return this.m_mapBondToStereo.get(a_oBond);
	}

	public ConnectionComparatorByCIPOrder getConnectionComparatorByCIPOrder() {
		return this.m_oCIPComp;
	}

	public void printLog() {
		System.err.println( this.m_sbLog );
	}

	public void setStereoTo(ChemicalGraph a_oGraph) {
		// Calculate stereochemistry
		this.start(a_oGraph);

		// Set stereo for atoms
		for ( Atom t_oAtom : a_oGraph.getAtoms() ) {
			t_oAtom.setChirality( this.m_mapAtomToStereo.get(t_oAtom) );
			if ( this.m_mapAtomToStereo.get(t_oAtom) == null ) continue;
			this.m_sbLog.append( t_oAtom.getSymbol()+"("+t_oAtom.getAtomID()+"): "+this.m_mapAtomToStereo.get(t_oAtom)+"\n" );
		}
		// Set cis-trans for bonds
		for ( Bond t_oBond : a_oGraph.getBonds() ) {
			t_oBond.setGeometric( this.m_mapBondToStereo.get(t_oBond) );
			if ( this.m_mapBondToStereo.get(t_oBond) == null ) continue;
			this.m_sbLog.append( t_oBond.getAtom1().getSymbol()+"("+t_oBond.getAtom1().getAtomID()+")="+t_oBond.getAtom2().getSymbol()+"("+t_oBond.getAtom2().getAtomID()+"): "+this.m_mapBondToStereo.get(t_oBond)+"\n" );
		}
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

		// Calculate EZ for double and aromatic bonds
		HashMap<Bond, String> t_mapBondToStereo = new HashMap<Bond, String>();
		LinkedList<Bond> t_aCandidateIsomerismBond = new LinkedList<Bond>();
		for ( Bond t_oBond : a_oGraph.getBonds() ) {
			if ( t_oBond.getType() != 2 && t_oBond.getType() != 4 ) continue;
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
	 * Calc and get geometrical isomerism of target bond
	 * @param a_oBond Target double bond
	 * @param a_oHDComp Comparator for HierarchicalDigraph
	 * @return String of isomerism ("E" or "Z", null if no isomerism)
	 */
	private String calcIsomerism(Bond a_oBond, HierarchicalDigraphComparator a_oHDComp) {
		Atom t_oA0 = a_oBond.getAtom1();
		Atom t_oB0 = a_oBond.getAtom2();

		// Sort connections on A0 except between bond atoms
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

		// Sort connections on B0 except between bond atoms
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
	protected LinkedList<Connection> sortConnectionsByCIPOrder( LinkedList<Connection> a_aConns, HierarchicalDigraphComparator a_oHDComp ) {
		this.m_oCIPComp = new ConnectionComparatorByCIPOrder(a_oHDComp, this.m_oANumCalc);
		Collections.sort(a_aConns, this.m_oCIPComp);
		Connection t_oPreConn = a_aConns.getFirst();
		for ( Connection t_oConn : a_aConns ) {
			if ( t_oPreConn.equals(t_oConn) ) continue;
			if ( this.m_oCIPComp.compare(t_oPreConn, t_oConn) == 0 ) return null;
			t_oPreConn = t_oConn;
		}
		return a_aConns;

	}
}
