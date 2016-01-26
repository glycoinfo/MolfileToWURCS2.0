package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Molecule;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraph;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraphCreator;
import org.glycoinfo.ChemicalStructureUtility.io.MDLMOL.MoleculeReader;
import org.glycoinfo.ChemicalStructureUtility.io.MDLMOL.ParameterReader;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.MoleculeNormalizer;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.StructureAnalyzer;
import org.glycoinfo.WURCSFramework.util.exchange.CarbonChainFinder;

public class TestStereo {

	public static void main(String[] args) {
		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);

		MoleculeReader t_oMolRead = new MoleculeReader(t_objParam);

		// Set skip IDs
		LinkedList<String> t_aSkipIDs = new LinkedList<String>();
//		t_aSkipIDs.add("CHEBI:52917");

		while ( t_oMolRead.readNext() ) {
			// Read Molecule
			Molecule t_oMol = t_oMolRead.getMolecule();
			String t_strID = t_oMolRead.getID();
			try {
				t_strID = String.format("%1$05d", Integer.parseInt(t_strID) );
			} catch (NumberFormatException e) {
			}
			if ( t_aSkipIDs.contains(t_strID) ) {
				System.err.println(t_strID + " is skipped.");
				continue;
			}
			MoleculeNormalizer t_oNormalize = new MoleculeNormalizer();
			t_oNormalize.normalize(t_oMol);

			// Structureral analyze for molecule
			StructureAnalyzer t_oStAnal = new StructureAnalyzer();

			// Stop if there is no carbon
			if ( !t_oStAnal.findCarbonIn(t_oMol) ) {
				System.err.println("There is no carbon in the molecule.");
				break;
			}

			// Collect atoms which membered aromatic, pi cyclic and carbon cyclic rings
			t_oStAnal.analyze(t_oMol);

			// Set start atoms for carbon chain finder
			HashSet<Atom> t_setTerminalCarbons = t_oStAnal.getTerminalCarbons();
			// Set Ignore atoms for carbon chain finder
			HashSet<Atom> t_setIgnoreAtoms = new HashSet<Atom>();
			t_setIgnoreAtoms.addAll( t_oStAnal.getAromaticAtoms() );
			t_setIgnoreAtoms.addAll( t_oStAnal.getPiCyclicAtoms() );
			t_setIgnoreAtoms.addAll( t_oStAnal.getCarbonCyclicAtoms() );

			// Calculate stereo for original molecule
			StereochemistryAnalysis t_oStereo = new StereochemistryAnalysis();
			t_oStereo.setStereoTo(t_oMol);

			CarbonChainFinder t_oCCFinder = new CarbonChainFinder();
			t_oCCFinder.setParameters(2, 2, 3, 999, 2.0f);
			t_oCCFinder.find(t_setTerminalCarbons, t_setIgnoreAtoms);
			t_oCCFinder.getCandidateCarbonChains();

			HashSet<Atom> t_aBackboneCarbons = new HashSet<Atom>();
			for ( LinkedList<Atom> t_oChain : t_oCCFinder.getCandidateCarbonChains() ) {
				t_aBackboneCarbons.addAll(t_oChain);
			}

			SubGraphCreator t_oCreateSub = new SubGraphCreator(t_oMol);
			HashMap<SubGraph, SubGraphCreator> t_mapGraphToCreator = new HashMap<SubGraph, SubGraphCreator>();
			LinkedList<SubGraph> t_aSubGraphs = new LinkedList<SubGraph>();
			LinkedList<Atom> t_aSubAtoms = new LinkedList<Atom>();
			for ( Atom t_oAtom : t_oMol.getAtoms() ) {
				if ( t_aBackboneCarbons.contains(t_oAtom) ){
					System.err.println("Ignore "+t_oAtom.getSymbol()+"("+t_oAtom.getAtomID()+")");
					continue;
				}
				if ( t_aSubAtoms.contains(t_oAtom) ) continue;
				t_oCreateSub.start(t_oAtom, t_aBackboneCarbons);
				// Ignore subgraph which contained only one hydrogen
				if ( t_oCreateSub.isHydrogen() ) continue;
				t_aSubGraphs.add( t_oCreateSub.getSubGraph() );
				t_aSubAtoms.addAll( t_oCreateSub.getOriginalAtoms() );

				// Add backbone carbon
				for ( Connection t_oExConn : t_oCreateSub.getExternalOriginalConnections() ) {
					if ( !t_aBackboneCarbons.contains( t_oExConn.endAtom() ) ) continue;
					t_oCreateSub.addExternalConnection( t_oExConn );
				}
			}

			for ( SubGraph t_oSub : t_aSubGraphs ) {
				t_oStereo.setStereoTo(t_oSub);
			}

		}
	}

}
