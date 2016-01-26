package org.glycoinfo.ChemicalStructureUtility.util.analytical;

import java.util.ArrayList;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Molecule;
import org.glycoinfo.ChemicalStructureUtility.util.Chemical;

/**
 * Class for normalize molecule:
 * remove metal, omit charge, omit isotope and add hidden hydrogens
 * @author MasaakiMatsubara
 */
public class MoleculeNormalizer {

	private HiddenHydrogenAttacher m_objHydrogenAttacher = new HiddenHydrogenAttacher();

	/** Molecule for normalization*/
	private Molecule m_objMolecule;

	/**
	 * Normalize molecule.
	 * TODO: To make other normalize method
	 * @param mol
	 */
	public void normalize(Molecule mol) {
		this.m_objMolecule = mol;
		this.removeMetalAtoms();
		this.omitIsotope();
		this.omitCharge();
		this.addHiddenHydrogens();
	}

	/**
	 * Remove metal atoms.
	 */
	private void removeMetalAtoms(){
		ArrayList<Atom> removeAtoms = new ArrayList<Atom>();
		for(Atom atom : this.m_objMolecule.getAtoms()){
			if(!Chemical.isNonMetal(atom.getSymbol())){
				removeAtoms.add(atom);
			}
		}
		for(Atom atom : removeAtoms){
			this.m_objMolecule.remove(atom);
		}
	}

	/**
	 * Set isotope information (mass value) of all atoms to 0.
	 */
	private void omitIsotope(){
		for(Atom atom : this.m_objMolecule.getAtoms()){
			atom.setMass(0);
		}
	}

	/**
	 * Set charge information of all atoms to 0.
	 */
	private void omitCharge() {
		for(Atom atom : this.m_objMolecule.getAtoms()){
			atom.setCharge(0);
		}
	}

	/**
	 * Add hidden hydrogens.
	 */
	private void addHiddenHydrogens() {
		LinkedList<Connection> addConnect = new LinkedList<Connection>();
		for(Atom atom : this.m_objMolecule.getAtoms()){
			// Count valence of atoms and supply hidden hydrogens to the atom
			if ( !m_objHydrogenAttacher.attachHiddenHydrogensTo(atom) ) continue;
			// Collect hidden hydrogens if hidden hydrogens are supplied
			for ( Connection con : atom.getConnections() ) {
				if ( this.m_objMolecule.contains( con.endAtom() ) ) continue;
				addConnect.add(con);
			}
		}
		// TODO: remove print
		System.err.println("attach " + addConnect.size());
		// Add atom and bond of hidden hydrogens
		for ( Connection con : addConnect ) {
			this.m_objMolecule.add( con.endAtom() );
			this.m_objMolecule.add( con.getBond() );
		}
	}

}
