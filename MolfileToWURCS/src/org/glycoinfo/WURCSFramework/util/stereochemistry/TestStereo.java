package org.glycoinfo.WURCSFramework.util.stereochemistry;

import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Molecule;
import org.glycoinfo.WURCSFramework.chemicalgraph.SubGraphNew;
import org.glycoinfo.WURCSFramework.io.MDLMOL.CTFileReader;
import org.glycoinfo.WURCSFramework.io.MDLMOL.ParameterReader;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.MoleculeNormalizer;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.StructureAnalyzer;
import org.glycoinfo.WURCSFramework.util.exchange.CarbonChainFinder;

public class TestStereo {

	public static void main(String[] args) {
		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);

		for ( String t_strFilepath : t_objParam.getCTfileList() ){
			readCTFile(t_strFilepath, t_objParam.m_ID, t_objParam.m_sdfileOutput);
		}

	}

	public static void readCTFile(String a_strFilePath, String a_strFieldID, boolean a_bOutput) {

		// read CTFiles
		CTFileReader t_objCTReader = new CTFileReader(a_strFilePath, a_bOutput);

		// Set skip IDs
		LinkedList<String> t_aSkipIDs = new LinkedList<String>();
//		t_aSkipIDs.add("CHEBI:52917");

		while ( t_objCTReader.readNext() ) {
			// read a record from CTFile
			Molecule mol = t_objCTReader.getMolecule();
			String ID = t_objCTReader.getFieldData(a_strFieldID);
			try {
				ID = String.format("%1$05d", Integer.parseInt(ID) );
			} catch (NumberFormatException e) {
			}
			if ( t_aSkipIDs.contains(ID) ) {
				System.err.println(ID + " is skipped.");
				continue;
			}
			MoleculeNormalizer t_oNormalize = new MoleculeNormalizer();
			t_oNormalize.normalize(mol);

			// strop if there is no carbon
			int t_nCarbon = 0;
			for ( Atom t_oAtom : mol.getAtoms() ) {
				if ( t_oAtom.getSymbol().equals("C") ) t_nCarbon++;
			}
			if ( t_nCarbon == 0 ) {
				System.err.println("There is no carbon in the molecule.");
				break;
			}

			// Structureral analyze for molecule
			// Collect atoms which membered aromatic, pi cyclic and carbon cyclic rings
			StructureAnalyzer t_oStAnal = new StructureAnalyzer();
			t_oStAnal.analyze(mol);

			// Set start atoms for carbon chain finder
			HashSet<Atom> t_setTerminalCarbons = t_oStAnal.getTerminalCarbons();
			// Set Ignore atoms for carbon chain finder
			HashSet<Atom> t_setIgnoreAtoms = new HashSet<Atom>();
			t_setIgnoreAtoms.addAll( t_oStAnal.getAromaticAtoms() );
			t_setIgnoreAtoms.addAll( t_oStAnal.getPiCyclicAtoms() );
			t_setIgnoreAtoms.addAll( t_oStAnal.getCarbonCyclicAtoms() );

			// Calculate stereo for original molecule
			StereochemistryAnalysis t_oStereo = new StereochemistryAnalysis();
			t_oStereo.start(mol);
			t_oStereo.setStereoTo(mol);

			CarbonChainFinder t_oCCFinder = new CarbonChainFinder();
			t_oCCFinder.setParameters(2, 2, 3, 999, 2.0f);
			t_oCCFinder.find(t_setTerminalCarbons, t_setIgnoreAtoms);
			t_oCCFinder.getCandidateCarbonChains();

			HashSet<Atom> t_aBackboneCarbons = new HashSet<Atom>();
			for ( LinkedList<Atom> t_oChain : t_oCCFinder.getCandidateCarbonChains() ) {
				t_aBackboneCarbons.addAll(t_oChain);
			}

			SubGraphNew t_oSubgraph = new SubGraphNew(mol);
			for ( Atom t_oAtom : mol.getAtoms() ) {
				if ( t_aBackboneCarbons.contains(t_oAtom) ){
					System.err.println("Ignore "+t_oAtom.getSymbol()+"("+t_oAtom.getAtomID()+")");
					continue;
				}
				t_oSubgraph.addByOriginal(t_oAtom);
			}

			t_oStereo.start(t_oSubgraph);
			t_oStereo.setStereoTo(t_oSubgraph);

		}
	}

}
