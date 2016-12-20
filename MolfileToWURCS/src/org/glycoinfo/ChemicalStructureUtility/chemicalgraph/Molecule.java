package org.glycoinfo.ChemicalStructureUtility.chemicalgraph;

import java.util.HashMap;


public class Molecule extends ChemicalGraph{

	/**
	 * Copy Molecule without atom map
	 * @return Copied molecule
	 */
	public Molecule copy() {
		return this.copy( new HashMap<Atom, Atom>() );
	}

	/**
	 * Copy molecule
	 * @param a_mapOrigToCopyAtom
	 * @return Copied molecule
	 */
	public Molecule copy(HashMap<Atom, Atom> a_mapOrigToCopyAtom) {
		Molecule t_oCopyMol = new Molecule();
		// Copy
		HashMap<Atom, Atom> t_mapOrigToCopyAtom = new HashMap<Atom, Atom>();
		for ( Atom t_oOrigAtom : this.getAtoms() ) {
			Atom t_oCopy = t_oOrigAtom.copy();
			t_mapOrigToCopyAtom.put(t_oOrigAtom, t_oCopy);
			t_oCopyMol.add(t_oCopy);
		}
		for ( Bond t_oOrigBond : this.getBonds() ) {
			Atom t_oAtom1 = t_mapOrigToCopyAtom.get( t_oOrigBond.getAtom1() );
			Atom t_oAtom2 = t_mapOrigToCopyAtom.get( t_oOrigBond.getAtom2() );
			Bond t_oCopy = t_oOrigBond.copy(t_oAtom1, t_oAtom2);
			t_oCopyMol.add(t_oCopy);
		}
		return t_oCopyMol;
	}
}
