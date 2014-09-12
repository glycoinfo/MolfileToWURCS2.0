package util.analytical;

import java.util.ArrayList;

import utility.Chemical;
import chemicalgraph2.Atom;
import chemicalgraph2.subgraph.Molecule;

/**
 * Class for normalize molecule:
 * remove metal, omit charge, omit isotope and add hidden hydrogens
 * @author MasaakiMatsubara
 */
public class MoleculeNormalizer {
	/** Molecule for normalization*/
	private Molecule m_objMolecule;

	/**
	 * Normalize molecule.
	 * TODO: Add other normalize method
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
			if(Chemical.isMetal(atom.getSymbol())){
				removeAtoms.add(atom);
			}
		}
		for(Atom atom : removeAtoms){
			this.m_objMolecule.remove(atom);
		}
	}

	/**
	 * Set isotope information of all atoms to 0.
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
	public void addHiddenHydrogens() {
		for(Atom atom : this.m_objMolecule.getAtoms()){
			HiddenHydrogenChecker t_objHiddenCheck = new HiddenHydrogenChecker(atom);
			t_objHiddenCheck.addHiddenHydrogens();
		}
	}


}
