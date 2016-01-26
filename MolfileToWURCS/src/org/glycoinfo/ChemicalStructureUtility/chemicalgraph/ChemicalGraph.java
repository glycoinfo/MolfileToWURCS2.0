package org.glycoinfo.ChemicalStructureUtility.chemicalgraph;

import java.util.LinkedList;

/**
 * Class for chemical graph
 * @author MasaakiMatsubara
 */
public abstract class ChemicalGraph {
	//----------------------------
	// Member variable
	//----------------------------
	/** List of atoms */
	protected LinkedList<Atom> m_aAtoms = new LinkedList<Atom>();
	/** List of bonds */
	protected LinkedList<Bond> m_aBonds = new LinkedList<Bond>();

	//----------------------------
	// Accessor
	//----------------------------
	public LinkedList<Atom> getAtoms() {
		return this.m_aAtoms;
	}

	public LinkedList<Bond> getBonds() {
		return this.m_aBonds;
	}

	public void clear() {
		this.m_aAtoms.clear();
		this.m_aBonds.clear();
	}

	public void add(Atom atom) {
		this.m_aAtoms.addLast(atom);
	}

	public void add(Bond bond) {
		this.m_aBonds.addLast(bond);
	}

	//----------------------------
	// Public method
	//----------------------------
	/**
	 * Whether or not this chemical graph contains the atom.
	 * @param atom
	 * @return true if this chemical graph contains the atom.
	 */
	public boolean contains(final Atom atom){
		return this.m_aAtoms.contains(atom);
	}

	/**
	 * Whether or not this chemical graph contains the bond.
	 * @param bond
	 * @return true if this chemical graph contains the bond.
	 */
	public boolean contains(final Bond bond){
		return this.m_aBonds.contains(bond);
	}

	/**
	 * Remove objects which connect with the atom, then remove the atom.
	 * @param atom Remove atom
	 * @return true if the objects are removed successfully.
	 */
	public abstract boolean remove(final Atom atom);

}
