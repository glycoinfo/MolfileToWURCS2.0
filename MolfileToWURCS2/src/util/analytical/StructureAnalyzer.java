package util.analytical;

import java.util.HashSet;

import chemicalgraph2.Atom;
import chemicalgraph2.Connection;
import chemicalgraph2.subgraph.Molecule;

/**
 *
 * @author Masaaki Matsubara
 *
 */
public class StructureAnalyzer {

	private Molecule m_objMolecule;

	private HashSet<Atom> m_aAromaticAtoms     = new HashSet<Atom>();
	private HashSet<Atom> m_aPiCyclicAtoms     = new HashSet<Atom>();
	private HashSet<Atom> m_aCarbonCyclicAtoms = new HashSet<Atom>();
	private HashSet<Atom> m_aTerminalCarbons   = new HashSet<Atom>();

	//----------------------------
	// Constructor
	//----------------------------
	public StructureAnalyzer() {
	}

	//----------------------------
	// Accessor
	//----------------------------
	public void clear() {
		this.m_objMolecule = null;
		this.m_aAromaticAtoms.clear();
		this.m_aPiCyclicAtoms.clear();
		this.m_aCarbonCyclicAtoms.clear();
		this.m_aTerminalCarbons.clear();
	}

	public Molecule getMolecule() {
		return this.m_objMolecule;
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

	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * Structure analyze for molecule.
	 * Search and collect atoms of aromatic ring, pi ring, carbon ring and terminal carbons.
	 * @param a_objMol Molecule object for analyze
	 */
	public void analyze(Molecule a_objMol) {
		this.m_objMolecule = a_objMol;
//		this.setStereoMolecule();
		this.findAromaticRings();
		this.findPiRings();
		this.findCarbonRings();
		this.findTerminalCarbons();
	}

	/**
	 * Collect aromatic atoms. The atoms are member of an aromatic ring,
	 *  which all atoms have pi electorn(s) and total number of pi electorons is 4n+2.
	 * using class Cyclization
	 */
	public void findAromaticRings(){
		Cyclization t_objCyclize = new Cyclization();
		for(Atom atom : this.m_objMolecule.getAtoms()){
			t_objCyclize.clear();
			if ( t_objCyclize.aromatize(atom) ) {
				this.m_aAromaticAtoms.addAll(t_objCyclize);
			}
		}
	}

	/**
	 * Collect pi cyclic atoms. The atoms are member of a ring which all atoms have pi electorn(s).
	 * using class Cyclization
	 */
	public void findPiRings(){
		Cyclization t_objCyclize = new Cyclization();
		for(Atom atom : this.m_objMolecule.getAtoms()){
			t_objCyclize.clear();
			if ( t_objCyclize.piCyclize(atom) ) {
				this.m_aPiCyclicAtoms.addAll(t_objCyclize);
			}
		}
	}

	/**
	 * Collect carbon cyclic atoms. The atoms are member of a ring which all atoms are carbon.
	 * using class Cyclization
	 */
	public void findCarbonRings(){
		Cyclization t_objCyclize = new Cyclization();
		for(Atom atom : this.m_objMolecule.getAtoms()){
			t_objCyclize.clear();
			if ( t_objCyclize.carbonCyclize(atom) ) {
				this.m_aCarbonCyclicAtoms.addAll(t_objCyclize);
			}
		}
	}

	/**
	 * Collect terminal carbons. The atoms are terminal carbons without aromatic, pi cyclic, and carbon cyclic rings.
	 */
	public void findTerminalCarbons() {
		// Set ignore atoms
		HashSet<Atom> ignoreAtoms = new HashSet<Atom>();
		ignoreAtoms.addAll(this.m_aAromaticAtoms);
		ignoreAtoms.addAll(this.m_aPiCyclicAtoms);
		ignoreAtoms.addAll(this.m_aCarbonCyclicAtoms);

		// Search terminal carbons
		for ( Atom atom : this.m_objMolecule.getAtoms() ) {
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
	}
}
