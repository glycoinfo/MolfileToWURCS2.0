package chemicalgraph.util.analytical;

import java.util.HashSet;

import chemicalgraph.Atom;
import chemicalgraph.Connection;
import chemicalgraph.Molecule;

/**
 * Class for molecule structure analysis to find aromatic, pi cyclic and carbon cyclic atoms and terminal carbons
 * @author MasaakiMatsubara
 *
 */
public class StructureAnalyzer {

	//----------------------------
	// Member variable
	//----------------------------
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

	//----------------------------
	// Accessor
	//----------------------------
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

	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * Structure analyze for molecule.
	 * Search and collect atoms of aromatic ring, pi ring, carbon ring and terminal carbons.
	 * @param a_objMol Molecule object for analyze
	 */
	public void analyze(Molecule a_objMol) {
		this.clear();

		// Collect cyclic atoms using Cyclization
		Cyclization t_objCyclize = new Cyclization();
		for ( Atom atom : a_objMol.getAtoms() ) {

			// Collect aromatic atoms
			if ( t_objCyclize.aromatize(atom) )
				this.m_aAromaticAtoms.addAll(t_objCyclize);

			// Collect pi cyclic atoms
			if ( t_objCyclize.piCyclize(atom) )
				this.m_aPiCyclicAtoms.addAll(t_objCyclize);

			// Collect carbon ring atoms
			if ( t_objCyclize.carbonCyclize(atom) )
				this.m_aCarbonCyclicAtoms.addAll(t_objCyclize);
		}

		// Set ignore atoms
		HashSet<Atom> ignoreAtoms = new HashSet<Atom>();
		ignoreAtoms.addAll(this.m_aAromaticAtoms);
		ignoreAtoms.addAll(this.m_aPiCyclicAtoms);
		ignoreAtoms.addAll(this.m_aCarbonCyclicAtoms);

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
	}
}
