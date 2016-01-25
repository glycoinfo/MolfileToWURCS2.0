package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.HashSet;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Molecule;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.MoleculeNormalizer;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.StructureAnalyzer;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

public class MoleculeToWURCSGraph {

	private WURCSGraph m_oGraph;

	public void start(Molecule a_oMolecule) {

	}

	private void prepareMolecule(Molecule a_oMolecule) throws WURCSException {
		// Normalize molecule
		MoleculeNormalizer t_oMolNorm = new MoleculeNormalizer();
		t_oMolNorm.normalize(a_oMolecule);

		// Throw exeption if there is no carbon
		int t_nCarbon = 0;
		for ( Atom t_oAtom :a_oMolecule.getAtoms() ) {
			if ( t_oAtom.getSymbol().equals("C") ) t_nCarbon++;
		}
		if ( t_nCarbon == 0 )
			throw new WURCSException("There is no carbon in the molecule.");

		// Structureral analyze for molecule
		// Collect atoms which membered aromatic, pi cyclic and carbon cyclic rings
		StructureAnalyzer t_oStAnal = new StructureAnalyzer();
		t_oStAnal.analyze(a_oMolecule);

		// Set start atoms for carbon chain finder
		HashSet<Atom> t_setTerminalCarbons = t_oStAnal.getTerminalCarbons();
		// Set Ignore atoms for carbon chain finder
		HashSet<Atom> t_setIgnoreAtoms = new HashSet<Atom>();
		t_setIgnoreAtoms.addAll( t_oStAnal.getAromaticAtoms() );
		t_setIgnoreAtoms.addAll( t_oStAnal.getPiCyclicAtoms() );
		t_setIgnoreAtoms.addAll( t_oStAnal.getCarbonCyclicAtoms() );

	}
}
