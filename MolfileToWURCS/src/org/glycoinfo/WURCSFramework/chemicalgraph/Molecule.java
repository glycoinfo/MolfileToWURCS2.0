package org.glycoinfo.WURCSFramework.chemicalgraph;

import java.util.ArrayList;

public class Molecule extends ChemicalGraph{

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
