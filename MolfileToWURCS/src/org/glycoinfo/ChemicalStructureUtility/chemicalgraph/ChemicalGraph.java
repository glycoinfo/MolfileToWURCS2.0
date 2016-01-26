package org.glycoinfo.ChemicalStructureUtility.chemicalgraph;

import java.util.ArrayList;
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
	 * Remove objects which connect with input atom, then remove input atom.
	 * @param atom
	 * @return true if this chemical graph contains input atom.
	 */
	public boolean remove(final Atom atom){
		ArrayList<Bond> removeBonds = new ArrayList<Bond>();
		for(Connection connection : atom.getConnections()){
			removeBonds.add(connection.getBond());
		}
		for(Bond bond : removeBonds){
			Atom atom0 = bond.getAtom1();
			Atom atom1 = bond.getAtom2();
			atom0.getConnections().remove(atom1);
			atom1.getConnections().remove(atom0);
			this.m_aBonds.remove(bond);
		}
		return this.m_aAtoms.remove(atom);
	}

}
