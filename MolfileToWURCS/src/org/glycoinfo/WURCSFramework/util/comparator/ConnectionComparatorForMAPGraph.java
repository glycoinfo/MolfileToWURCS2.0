package org.glycoinfo.WURCSFramework.util.comparator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.Chemical;
import org.glycoinfo.ChemicalStructureUtility.util.MorganAlgorithm;
import org.glycoinfo.WURCSFramework.buildingblock.SubMolecule;

/**
 * Connection comparator for generating MAPGraph
 * @author MasaakiMatsubara
 *
 */
public class ConnectionComparatorForMAPGraph implements Comparator<Connection> {

	private SubMolecule m_oSubMol;
	private LinkedList<Atom> m_aIgnoreAtoms;
	private LinkedList<Bond> m_aIgnoreBonds;
	private HashMap<Atom, Integer> m_mapAtomToInitialMorganNumber;
	private HashMap<Atom, Integer> m_mapAtomToCurrentMorganNumber;
	private ConnectionComparatorByCIPOrderForSubMolecule m_oCIPComp;

	public ConnectionComparatorForMAPGraph(SubMolecule a_oSubMol) {
		this.m_oSubMol = a_oSubMol;
		this.m_aIgnoreAtoms = new LinkedList<Atom>();
		this.m_aIgnoreBonds = new LinkedList<Bond>();
		// Ignore hydrogens and their bonds
		for ( Atom t_oAtom : a_oSubMol.getAtoms() ) {
			if ( !t_oAtom.getSymbol().equals("H") ) continue;
			this.m_aIgnoreAtoms.addLast(t_oAtom);
		}
		for ( Bond t_oBond : a_oSubMol.getBonds() ) {
			if ( t_oBond.getAtom1().getSymbol().equals("H")
			  || t_oBond.getAtom2().getSymbol().equals("H") )
				this.m_aIgnoreBonds.addLast(t_oBond);
		}
		this.m_mapAtomToInitialMorganNumber = this.calcMorganNumber();
		this.m_mapAtomToCurrentMorganNumber = this.calcMorganNumber();
		this.m_oCIPComp = new ConnectionComparatorByCIPOrderForSubMolecule(a_oSubMol);
	}

	/**
	 * Add tail connection
	 * @param a_oConn tail connection
	 */
	public void addTailConnection(Connection a_oConn) {
		if ( !this.m_aIgnoreAtoms.contains( a_oConn.startAtom() ) )
			this.m_aIgnoreAtoms.addLast( a_oConn.startAtom() );
		this.m_aIgnoreAtoms.addLast( a_oConn.endAtom() );
		this.m_aIgnoreBonds.addLast( a_oConn.getBond() );
		// Recalculate Morgan number for current state
		this.m_mapAtomToCurrentMorganNumber = this.calcMorganNumber();
	}

	/**
	 * Calculate Morgan numbers for current state
	 * @return HashMap of atom to morgan number
	 */
	public HashMap<Atom, Integer> calcMorganNumber() {
		MorganAlgorithm t_oMA = new MorganAlgorithm(this.m_oSubMol);
		t_oMA.calcMorganNumber(this.m_aIgnoreBonds, this.m_aIgnoreAtoms);
		return t_oMA.getAtomToMorganNumber();
	}

	@Override
	public int compare(Connection a_oConn1, Connection a_oConn2) {

		int t_iComp = 0;

		Atom t_oStart1 = a_oConn1.startAtom();
		Atom t_oStart2 = a_oConn2.startAtom();
		Atom t_oEnd1 = a_oConn1.endAtom();
		Atom t_oEnd2 = a_oConn2.endAtom();

		// 1. Prioritize connected aromatic atom
		if ( this.m_aIgnoreAtoms.getLast().isAromatic() ){
			if(  t_oEnd1.isAromatic()   && !t_oEnd2.isAromatic()   ) return -1;
			if( !t_oEnd1.isAromatic()   &&  t_oEnd2.isAromatic()   ) return 1;
			if(  t_oStart1.isAromatic() && !t_oStart2.isAromatic() ) return -1;
			if( !t_oStart1.isAromatic() &&  t_oStart2.isAromatic() ) return 1;
		}

		// 2. Prioritize a connection of the path searched latter
		t_iComp = this.m_aIgnoreAtoms.indexOf(t_oStart2) - this.m_aIgnoreAtoms.indexOf(t_oStart1);
		if ( t_iComp != 0 ) return t_iComp;

		// 3. Prioritize a connection connecting backbone carbon
		LinkedList<Atom> t_aBackboneCarbons = this.m_oSubMol.getBackboneCarbons();
		int t_nBackbone1 = 0;
		int t_nBackbone2 = 0;
		for ( Connection con : t_oEnd1.getConnections() )
			if ( t_aBackboneCarbons.contains(con.endAtom()) ) t_nBackbone1++;
		for ( Connection con : t_oEnd2.getConnections() ) {
			if ( t_aBackboneCarbons.contains(con.endAtom()) ) t_nBackbone2++;
		}
		if ( t_nBackbone1!=0 && t_nBackbone2==0 ) return -1;
		if ( t_nBackbone1==0 && t_nBackbone2!=0 ) return 1;

		// 4. Prioritize higher Morgan number (toword center of non-search region)
		t_iComp = this.m_mapAtomToCurrentMorganNumber.get(t_oEnd2) - this.m_mapAtomToCurrentMorganNumber.get(t_oEnd1);
		if ( t_iComp != 0 ) return t_iComp;

		// 5. Prioritize large initial EC number (toword center of all region)
		t_iComp = this.m_mapAtomToInitialMorganNumber.get(t_oEnd2) - this.m_mapAtomToInitialMorganNumber.get(t_oEnd1);
		if ( t_iComp != 0 ) return t_iComp;

		// 6. Prioritize smaller atomic number
		t_iComp = Chemical.getAtomicNumber(t_oEnd1.getSymbol()) - Chemical.getAtomicNumber(t_oEnd2.getSymbol());
		if ( t_iComp != 0 ) return t_iComp;

		// 7. Prioritize lower number of bond type (bond order)
		t_iComp = a_oConn1.getBond().getType() - a_oConn2.getBond().getType();
		if ( t_iComp != 0 ) return t_iComp;

		// 8. Compare by CIP order
		t_iComp = this.m_oCIPComp.compare(a_oConn1, a_oConn2);
		if ( t_iComp != 0 ) return t_iComp;

		// For Backbone carbons
		if ( !t_aBackboneCarbons.contains(t_oEnd1) || !t_aBackboneCarbons.contains(t_oEnd2) ) return 0;

		// 9. Prioritize backbone carbon having higher weight
		Double t_dStereoWeight1 = this.m_oSubMol.getWeightOfBackboneCarbon(t_oEnd1);
		Double t_dStereoWeight2 = this.m_oSubMol.getWeightOfBackboneCarbon(t_oEnd2);
		WeightComparator t_oWComp = new WeightComparator();
		t_iComp = t_oWComp.compare(t_dStereoWeight1, t_dStereoWeight2);
		if ( t_iComp != 0 ) return t_iComp;

		return 0;
	}

}
