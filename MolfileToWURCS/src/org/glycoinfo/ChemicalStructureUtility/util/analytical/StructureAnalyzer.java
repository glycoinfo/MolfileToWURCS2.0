package org.glycoinfo.ChemicalStructureUtility.util.analytical;

import java.util.HashSet;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Molecule;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.cyclization.Aromatization;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.cyclization.CarbonCyclization;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.cyclization.PiCyclization;

/**
 * Class for molecule structure analysis to find aromatic, pi cyclic and carbon cyclic atoms and terminal carbons
 * @author MasaakiMatsubara
 *
 */
public class StructureAnalyzer {

	/** Aromatic atoms. The member of an aromatic ring,
	 *  which all atoms have pi electorn(s) and total number of pi electorons is 4n+2.
	 */
	private HashSet<Atom> m_aAromaticAtoms     = new HashSet<Atom>();
	/** Pi cyclic atoms. The member of a ring which all atoms have pi electorn(s). */
	private HashSet<Atom> m_aPiCyclicAtoms     = new HashSet<Atom>();
	/** Carbon cyclic atoms. The member of a ring which all atoms are carbon. */
	private HashSet<Atom> m_aCarbonCyclicAtoms = new HashSet<Atom>();
	/** Terminal carbons. Not contained aromatic, pi cyclic and carbon cyclic atoms. */
	private HashSet<Atom> m_aTerminalCarbons   = new HashSet<Atom>();


	public void clear() {
		this.m_aAromaticAtoms.clear();
		this.m_aPiCyclicAtoms.clear();
		this.m_aCarbonCyclicAtoms.clear();
		this.m_aTerminalCarbons.clear();
	}

	public HashSet<Atom> getAromaticAtoms() {
		return this.m_aAromaticAtoms;
	}

	public HashSet<Atom> getPiCyclicAtoms() {
		return this.m_aPiCyclicAtoms;
	}

	public HashSet<Atom> getCarbonCyclicAtoms() {
		return this.m_aCarbonCyclicAtoms;
	}

	public HashSet<Atom> getTerminalCarbons() {
		return this.m_aTerminalCarbons;
	}

	/**
	 *
	 * @param a_oMol Target Molecule
	 * @return true if there is one or more carbon
	 */
	public boolean findCarbonIn(Molecule a_oMol) {
		for ( Atom t_oAtom : a_oMol.getAtoms() ) {
			if ( t_oAtom.getSymbol().equals("C") ) return true;
		}
		return false;
	}

	/**
	 * Structure analyze for molecule.
	 * Search and collect atoms of aromatic ring, pi ring, carbon ring and terminal carbons.
	 * @param a_objMol Molecule object for analyze
	 */
	public void analyze(Molecule a_objMol) {
		this.clear();

		// Collect cyclic atoms using Cyclization
/*
		CyclizationOld t_objCyclize = new CyclizationOld();
		for ( Atom atom : a_objMol.getAtoms() ) {

			// Collect aromatic atoms
			if ( !this.m_aAromaticAtoms.contains(atom)     && t_objCyclize.aromatize(atom) )
				this.m_aAromaticAtoms.addAll(t_objCyclize);

			// Collect pi cyclic atoms
			if ( !this.m_aPiCyclicAtoms.contains(atom)     && t_objCyclize.piCyclize(atom) )
				this.m_aPiCyclicAtoms.addAll(t_objCyclize);

			// Collect carbon ring atoms
			if ( !this.m_aCarbonCyclicAtoms.contains(atom) && t_objCyclize.carbonCyclize(atom) )
				this.m_aCarbonCyclicAtoms.addAll(t_objCyclize);
		}
*/
		// Collect aromatic ring atoms for other atoms
		Aromatization t_oAromatize = new Aromatization();
		for ( Atom t_oAtom : a_objMol.getAtoms() ) {
			if ( this.m_aAromaticAtoms.contains(t_oAtom) ) continue;
			if ( !t_oAromatize.start(t_oAtom) ) continue;
			this.m_aAromaticAtoms.addAll(t_oAromatize);
		}
		// Set aromaticity
		for ( Atom t_oAtom : this.m_aAromaticAtoms ) {
			t_oAtom.setAromaticity();
		}

		// Collect pi cyclic atoms
		this.m_aPiCyclicAtoms.addAll(this.m_aAromaticAtoms);
		PiCyclization t_oPiCyclize = new PiCyclization();
		for ( Atom t_oAtom : a_objMol.getAtoms() ) {
			if ( this.m_aPiCyclicAtoms.contains(t_oAtom) ) continue;
			if ( !t_oPiCyclize.start(t_oAtom) ) continue;
			this.m_aPiCyclicAtoms.addAll(t_oPiCyclize);
		}

		// Set ignore atoms
		HashSet<Atom> ignoreAtoms = new HashSet<Atom>();
		ignoreAtoms.addAll(this.m_aAromaticAtoms);
		ignoreAtoms.addAll(this.m_aPiCyclicAtoms);

		// Search and collect terminal carbons
		for ( Atom atom : a_objMol.getAtoms() ) {
			if ( !atom.getSymbol().equals("C") ) continue;
			if ( ignoreAtoms.contains(atom) ) continue;
			int numC = 0;
			for ( Connection con : atom.getConnections() ) {
				Atom conAtom = con.endAtom();
				if ( !conAtom.getSymbol().equals("C")) continue;
				if ( ignoreAtoms.contains(conAtom) ) continue;
				numC++;
			}
			if ( numC == 1 ) this.m_aTerminalCarbons.add(atom);
		}

		// Search and collect branch carbons
		HashSet<Atom> t_aBranchCarbons = new HashSet<Atom>();
		t_aBranchCarbons.addAll(this.m_aTerminalCarbons);
		while ( true ) {
			HashSet<Atom> t_aNextCarbons = new HashSet<Atom>();
			for ( Atom atom : a_objMol.getAtoms() ) {
				if ( !atom.getSymbol().equals("C") ) continue;
				if ( t_aBranchCarbons.contains(atom) ) continue;
				int numC = 0;
				for ( Connection con : atom.getConnections() ) {
					Atom conAtom = con.endAtom();
					if ( !conAtom.getSymbol().equals("C")) continue;
					if ( t_aBranchCarbons.contains(conAtom) ) continue;
					numC++;
				}
				if ( numC == 1 ) t_aNextCarbons.add(atom);
			}
			if ( t_aNextCarbons.size() == 0 ) break;
			t_aBranchCarbons.addAll(t_aNextCarbons);
		}

		// Collect carbon cyclic atoms
		CarbonCyclization t_oCarbonCyclize = new CarbonCyclization();
		for ( Atom t_oAtom : a_objMol.getAtoms() ) {
			if ( t_aBranchCarbons.contains(t_oAtom) ) continue;
			if ( this.m_aCarbonCyclicAtoms.contains(t_oAtom) ) continue;
			if ( !t_oCarbonCyclize.start(t_oAtom) ) continue;
			this.m_aCarbonCyclicAtoms.addAll(t_oCarbonCyclize);
		}

		ignoreAtoms.addAll(this.m_aCarbonCyclicAtoms);

	}
}
