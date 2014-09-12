package chemicalgraph2.subgraph;

import java.util.ArrayList;
import java.util.HashMap;

import util.analytical.StereochemicalAnalyzer;
import chemicalgraph2.Atom;
import chemicalgraph2.Bond;
import chemicalgraph2.ChemicalGraph;
import chemicalgraph2.Connection;

public class Molecule extends ChemicalGraph{

	private HashMap<Atom, String> m_hashAtomToStereo = new HashMap<Atom, String>();
	private HashMap<Bond, String> m_hashBondToStereo = new HashMap<Bond, String>();

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

	/**
	 * Calculate the stereo chemistry for each atom. target is whole molecule.
	 */
	public void setStereo() {
		StereochemicalAnalyzer stereoAnalizer = new StereochemicalAnalyzer();
//		this.setStereo();
		stereoAnalizer.analyze(this);
		for(Atom atom : this.getAtoms()){
			this.m_hashAtomToStereo.put(atom, stereoAnalizer.m_hashAtomToStereo.get(atom));
			atom.stereoMolecule = atom.stereoTmp;
			atom.stereoTmp = null;
		}
		for(Bond bond : this.getBonds()){
			bond.stereoMolecule = bond.stereo;
			bond.stereo = null;
		}
	}

}
