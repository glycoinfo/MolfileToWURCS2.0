package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.HashMap;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.MoleculeNormalizer;
import org.glycoinfo.WURCSFramework.buildingblock.SubMolecule;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPAtomAbstract;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPAtomCyclic;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPConnection;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPGraph;
import org.glycoinfo.WURCSFramework.wurcs.map.MAPStar;

public class MAPGraphToSubMolecule {

	private MAPGraph m_oMAPGraph;
	private SubMolecule m_oSubMol;
	private HashMap<MAPAtomAbstract, Atom> m_mapMAPAtomToAtom;

	public MAPGraphToSubMolecule(MAPGraph a_oGraph) {
		this.m_oMAPGraph = a_oGraph;
		this.m_oSubMol = new SubMolecule();
		this.m_mapMAPAtomToAtom = new HashMap<MAPAtomAbstract, Atom>();
	}

	public SubMolecule getSubMolecule() {
		return this.m_oSubMol;
	}

	public void start() {

		HashMap<MAPAtomAbstract, Atom> t_mapMAPAtomToAtom = new HashMap<MAPAtomAbstract, Atom>();
		for ( MAPAtomAbstract t_oMAPAtom : this.m_oMAPGraph.getAtoms() ) {
			// MAPAtom to Atom
			Atom t_oAtom = this.convertMAPAtom(t_oMAPAtom);

			// MAPConnection to Bond
			if ( t_oMAPAtom.getParentConnection() == null ) continue;
			MAPConnection t_oMAPParentConn = t_oMAPAtom.getParentConnection();
			Atom t_oParentAtom = t_mapMAPAtomToAtom.get( t_oMAPParentConn.getAtom() );

			int t_iBondType = t_oMAPParentConn.getBondType().getNumber();
			Bond t_oBond = new Bond(t_oParentAtom, t_oAtom, t_iBondType, 0);

			this.m_oSubMol.add(t_oBond);
		}

		// Normalize
		MoleculeNormalizer t_oNorm = new MoleculeNormalizer();
		t_oNorm.normalize(this.m_oSubMol);
	}

	private Atom convertMAPAtom(MAPAtomAbstract a_oMAPAtom) {
		// For cyclic
		if ( a_oMAPAtom instanceof MAPAtomCyclic ) {
			MAPAtomCyclic t_oCyclic = (MAPAtomCyclic)a_oMAPAtom;
			return this.m_mapMAPAtomToAtom.get( t_oCyclic.getCyclicAtom() );
		}

		Atom t_oAtom = new Atom( a_oMAPAtom.getSymbol() );

		// Set parameters
		if ( a_oMAPAtom.isAromatic() ) t_oAtom.setAromaticity();

		if ( a_oMAPAtom.getStereo() != null )
			t_oAtom.setChirality( ""+a_oMAPAtom.getStereo() );

		this.m_oSubMol.add(t_oAtom);

		// MAPStar to Backbone carbon
		if ( a_oMAPAtom instanceof MAPStar ) {
			MAPStar t_oStar = (MAPStar)a_oMAPAtom;
			this.m_oSubMol.setBackboneCarbon(t_oAtom);
			double t_dWeight = 0.01D;
			if ( t_oStar.getStarIndex() > 0 ) {
				t_dWeight += t_oStar.getStarIndex() * 0.01D;
			}
			this.m_oSubMol.setWeightForBackboneCarbon(t_oAtom, t_dWeight);
		}

		return t_oAtom;
	}

}
