package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.HashMap;
import java.util.HashSet;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraphOld;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.MoleculeNormalizer;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.StructureAnalyzer;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.AtomicNumberCalculator;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.StereochemistryAnalysis;
import org.glycoinfo.WURCSFramework.buildingblock.SubMolecule;

public class SubGraphToSubMolecule {

//	private HashMap<Atom, Atom> m_mapCopyToOrigAtom = new HashMap<Atom, Atom>();
	private HashSet<Atom> m_aBackboneCarbons;
	private HashSet<Atom> m_aUnknownStereoAtoms;
	private HashSet<Bond> m_aUnknownStereoBonds;
	private StringBuffer m_sbLog = new StringBuffer();

	public SubGraphToSubMolecule(HashSet<Atom> t_aBackboneCarbons) {
		this.m_aBackboneCarbons = t_aBackboneCarbons;
		this.clear();
	}

	public void clear() {
		this.m_aUnknownStereoAtoms = new HashSet<Atom>();
		this.m_aUnknownStereoBonds = new HashSet<Bond>();
		this.m_sbLog.setLength(0);
	}

	public SubMolecule convert(SubGraphOld a_oSubGraph) {
		// Init
		this.clear();

		return this.cleateSubMolecule(a_oSubGraph);
	}

	public void printLog() {
		System.err.println( this.m_sbLog.toString() );
	}

	/**
	 * Create SubMolecule from SubGraph
	 * @param a_oSubGraph Target SubGraph
	 * @return SubMolecule copied SubMolecule
	 */
	private SubMolecule cleateSubMolecule(SubGraphOld a_oSubGraph) {
		SubMolecule t_oSubMol = new SubMolecule();

		// Copy Graph without Backbone carbons
		HashMap<Atom, Atom> t_mapOrigToCopyAtom = new HashMap<Atom, Atom>();
		for ( Atom t_oOrigAtom : a_oSubGraph.getAtoms() ) {
			Atom t_oCopy = t_oOrigAtom.copy();
			// For aromatic atom
			if ( t_oOrigAtom.isAromatic() )
				t_oCopy.setAromaticity();
			t_mapOrigToCopyAtom.put(t_oOrigAtom, t_oCopy);
			t_oSubMol.add(t_oCopy);
			t_oSubMol.associateAtomWithOriginal(t_oCopy, t_oOrigAtom);
		}
		for ( Bond t_oOrigBond : a_oSubGraph.getBonds() ) {
			Atom t_oAtom1 = t_mapOrigToCopyAtom.get( t_oOrigBond.getAtom1() );
			Atom t_oAtom2 = t_mapOrigToCopyAtom.get( t_oOrigBond.getAtom2() );
			// For aromatic bond (bond type 4)
			int t_oBondType = t_oOrigBond.getType();
			if ( t_oAtom1.isAromatic() && t_oAtom2.isAromatic() )
				t_oBondType = 4;
			Bond t_oCopy = new Bond( t_oAtom1, t_oAtom2, t_oBondType, t_oOrigBond.getStereo() );
			t_oSubMol.add(t_oCopy);
			t_oSubMol.associateBondWithOriginal(t_oCopy, t_oOrigBond);
		}

		// For Backbone carbon from connection
		for ( Connection t_oExConn : a_oSubGraph.getExternalConnections() ) {
			if ( !this.m_aBackboneCarbons.contains( t_oExConn.endAtom() ) ) continue;
			// Copy Backbone carbon
			Atom t_oBackboneCarbon = t_oExConn.endAtom();
			Atom t_oCopyBackboneCarbon = t_oBackboneCarbon.copy();
			t_oSubMol.add(t_oCopyBackboneCarbon);
			t_oSubMol.setBackboneCarbon(t_oCopyBackboneCarbon);
			t_oSubMol.associateAtomWithOriginal(t_oCopyBackboneCarbon, t_oBackboneCarbon);

			// Copy bond
			Atom t_oAtom1 = t_oCopyBackboneCarbon;
			Atom t_oAtom2 = t_mapOrigToCopyAtom.get( t_oExConn.startAtom() );
			if ( t_oExConn.getBond().getAtom2().equals(t_oBackboneCarbon) ) {
				t_oAtom1 = t_oAtom2;
				t_oAtom2 = t_oCopyBackboneCarbon;
			}
			Bond t_oCopyBond = t_oExConn.getBond().copy(t_oAtom1, t_oAtom2);
			t_oSubMol.add(t_oCopyBond);
			t_oSubMol.associateBondWithOriginal(t_oCopyBond, t_oExConn.getBond());
		}

		// Normalize molecule mainly for add hidden hydrogens
		MoleculeNormalizer t_oNorm = new MoleculeNormalizer();
		t_oNorm.normalize(t_oSubMol);

		// Structureral analyze for molecule
		// Collect atoms which membered aromatic, pi cyclic and carbon cyclic rings
		StructureAnalyzer t_oStAnal = new StructureAnalyzer();
		t_oStAnal.analyze(t_oSubMol);

		// Calculate and set stereo weight for backbone carbons to SubMolecule
		this.addStereoWeightForBackboneCarbons(t_oSubMol);

		// Set stereo using additional weight for backbone carbon
		StereochemistryAnalysis t_oSA = new StereochemistryAnalysis();
		AtomicNumberCalculator t_oANumCalc = new AtomicNumberCalculator();
		for ( Atom t_oCarbon : t_oSubMol.getBackboneCarbons() )
			t_oANumCalc.setExtraWeightToAtom(t_oCarbon, t_oSubMol.getWeightOfBackboneCarbon(t_oCarbon) );
		t_oSA.setAtomicNumberCalculator(t_oANumCalc);
		t_oSA.setStereoTo(t_oSubMol);

//		t_oSA.printLog();

		// For atoms and bonds having unknown stereo
		for ( Atom t_oAtom : this.m_aUnknownStereoAtoms )
			t_oAtom.setChirality("X");
		for ( Bond t_oBond : this.m_aUnknownStereoBonds )
			t_oBond.setGeometric("X");

		// XXX: For P and S tautomer

		return t_oSubMol;
	}

	/**
	 * Add weight for backbone carbons changing stereo of chiral atoms and double bonds.
	 * @param a_oSubGraph Target SubMolecule
	 */
	private void addStereoWeightForBackboneCarbons(SubMolecule a_oSubMol) {
		// Stereo check for submolecule

		// Init carbon ranks for stereochemistry
		HashMap<Atom, Integer> t_mapCarbonToStereoRank = new HashMap<Atom,Integer>();
		for ( Atom t_oCarbon : a_oSubMol.getBackboneCarbons() ) {
			t_mapCarbonToStereoRank.put(t_oCarbon, 0);
		}

		// Search atoms having stereo which change by carbon priority
		StereochemistryAnalysis t_oSANoBias = new StereochemistryAnalysis();
		// Add extra score for backbone carbons
		AtomicNumberCalculator t_oANumCalc = new AtomicNumberCalculator();
		for ( Atom t_oCarbon : a_oSubMol.getBackboneCarbons() )
			t_oANumCalc.setExtraWeightToAtom(t_oCarbon, 0.01D);
		t_oSANoBias.setAtomicNumberCalculator( t_oANumCalc );
		// Calc stereo with no bias
		t_oSANoBias.start(a_oSubMol);

		// Search atoms and bonds having stereo which chage by carbon priority
		boolean t_bIsChangeStereo = false;
		HashMap<Atom, HashMap<Atom, String>> t_mapStereoAtomToBackboneCarbons = new HashMap<Atom, HashMap<Atom, String>>();
		HashMap<Bond, HashMap<Atom, String>> t_mapStereoBondToBackboneCarbons = new HashMap<Bond, HashMap<Atom, String>>();
		for ( Atom t_oCarbon : a_oSubMol.getBackboneCarbons() ) {
			// Bias for a backbone carbon
			t_oANumCalc.setExtraWeightToAtom(t_oCarbon, t_oANumCalc.getExtraWeight(t_oCarbon) + 1 );

			// Recalc stereo
			StereochemistryAnalysis t_oSABias = new StereochemistryAnalysis();
			t_oSABias.setAtomicNumberCalculator(t_oANumCalc);
			t_oSABias.start(a_oSubMol);

			// Compare to original stereo

			// For chirality
			for ( Atom t_oAtom : a_oSubMol.getAtoms() ) {
				if ( a_oSubMol.getBackboneCarbons().contains(t_oAtom) ) continue;
				if ( t_oAtom.getSymbol().equals("H") ) continue;

				String t_strStereo1 = t_oSANoBias.getAtomStereo(t_oAtom);
				String t_strStereo2 = t_oSABias.getAtomStereo(t_oAtom);

				// Ignore no change or no stereo
				if ( t_strStereo1==null && t_strStereo2==null ) continue;
				if ( t_strStereo2==null ) continue;
				if ( t_strStereo1!=null && t_strStereo2.equals(t_strStereo1) ) continue;

				t_bIsChangeStereo = true;

				// Map carbon and chenged stereo
				if ( !t_mapStereoAtomToBackboneCarbons.containsKey(t_oAtom) )
					t_mapStereoAtomToBackboneCarbons.put(t_oAtom, new HashMap<Atom, String>());
				t_mapStereoAtomToBackboneCarbons.get(t_oAtom).put(t_oCarbon, t_strStereo2);
			}

			// For double bond stereo
			for ( Bond t_oBond : a_oSubMol.getBonds() ) {
				if ( t_oBond.getType() != 2 ) continue;

				String t_strStereo1 = t_oSANoBias.getBondStereo(t_oBond);
				String t_strStereo2 = t_oSABias.getBondStereo(t_oBond);

				// Ignore no change or no stereo
				if (t_strStereo1==null && t_strStereo2==null) continue;
				if ( t_strStereo2==null ) continue;
				if ( t_strStereo1!=null && t_strStereo2.equals(t_strStereo1) ) continue;

				t_bIsChangeStereo = true;

				// Map carbon and changed stereo
				if ( !t_mapStereoBondToBackboneCarbons.containsKey(t_oBond) )
					t_mapStereoBondToBackboneCarbons.put(t_oBond, new HashMap<Atom, String>());
				t_mapStereoBondToBackboneCarbons.get(t_oBond).put(t_oCarbon, t_strStereo2);
			}

			// Reset score for calc stereo
			t_oANumCalc.setExtraWeightToAtom(t_oCarbon, t_oANumCalc.getExtraWeight(t_oCarbon) - 1 );
		}

		// Return if no change by additional backbone carbon weight
		if ( !t_bIsChangeStereo ) return;

		this.m_sbLog.append("\nStereochemistry has changed by some backbone carbon weights:\n");

		// Calculate additional weight for backbone carbons by changing chirality

		// Init additional carbon weights to 0.0D
		HashMap<Atom, Double> t_mapCarbonToStereoWeight = new HashMap<Atom, Double>();
		for ( Atom t_oCarbon : a_oSubMol.getBackboneCarbons() )
			t_mapCarbonToStereoWeight.put(t_oCarbon, 0.0D);

		// Calc scores
		for ( Atom t_oStereoAtom : t_mapStereoAtomToBackboneCarbons.keySet() ) {

			// Log original stereo information
			if ( a_oSubMol.getOriginalAtom(t_oStereoAtom).getChirality() != null ) {
				String t_strAtom = t_oStereoAtom.getSymbol()+"("+t_oStereoAtom.getAtomID()+")";
				this.m_sbLog.append( "Original stereo of "+t_strAtom+": " );
				this.m_sbLog.append( a_oSubMol.getOriginalAtom(t_oStereoAtom).getChirality()+"\n" );
			}

			// Score by chirality, R = 3, S = 2 and X = 1, dividing by number of related carbons
			boolean t_bIsUnknown = false;
			HashMap<Atom, String> t_mapCarbonToChangedStereo = t_mapStereoAtomToBackboneCarbons.get(t_oStereoAtom);
			int t_nRelatedCarbons = t_mapCarbonToChangedStereo.size();
			for ( Atom t_oCarbon : t_mapCarbonToChangedStereo.keySet() ) {
				String t_strStereo = t_mapCarbonToChangedStereo.get(t_oCarbon);
				int t_iScore = ( t_strStereo.equals("R") )? 3 :
							   ( t_strStereo.equals("S") )? 2 :
							   ( t_strStereo.equals("X") )? 1 :
							   0 ;
				t_mapCarbonToStereoWeight.put(t_oCarbon, t_mapCarbonToStereoWeight.get(t_oCarbon) + (double)t_iScore/t_nRelatedCarbons);

				// For unknown stereo
				if ( t_iScore == 1 ) t_bIsUnknown = true;

				// Log stereo and score
				String t_strCarbon = t_oCarbon.getSymbol()+"("+t_oCarbon.getAtomID()+")";
				this.m_sbLog.append( " "+t_strCarbon+": "+ t_strStereo+" - "+(double)t_iScore/t_nRelatedCarbons+"\n");
			}

			if ( t_bIsUnknown ) {
				this.m_aUnknownStereoAtoms.add(t_oStereoAtom);
				// Log unknown stereo assign
				String t_strAtom = t_oStereoAtom.getSymbol()+"("+t_oStereoAtom.getAtomID()+")";
				this.m_sbLog.append( "Unknown stereo \"X\" is assigned to "+t_strAtom+"\n" );
			}
		}
		for ( Bond t_oStereoBond : t_mapStereoBondToBackboneCarbons.keySet() ) {
			// Log original stereo information
			if ( a_oSubMol.getOriginalBond(t_oStereoBond).getGeometric()!=null ) {
				String t_strBond
				= t_oStereoBond.getAtom1().getSymbol()+"("+t_oStereoBond.getAtom1().getAtomID()+")="
				+ t_oStereoBond.getAtom2().getSymbol()+"("+t_oStereoBond.getAtom2().getAtomID()+")";
				this.m_sbLog.append( "Original stereo of "+t_strBond+": ");
				this.m_sbLog.append( a_oSubMol.getOriginalBond(t_oStereoBond).getGeometric()+"\n" );
			}

			// Score by chirality, Z = 3, E = 2 and X = 1, dividing by number of related carbons
			boolean t_bIsUnknown = false;
			HashMap<Atom, String> t_mapCarbonToChangedStereo = t_mapStereoBondToBackboneCarbons.get(t_oStereoBond);
			int t_nRelatedCarbons = t_mapCarbonToChangedStereo.size();
			for ( Atom t_oCarbon : t_mapCarbonToChangedStereo.keySet() ) {

				String t_strStereo = t_mapCarbonToChangedStereo.get(t_oCarbon);
				int t_iScore = ( t_strStereo.equals("Z") )? 3 :
							   ( t_strStereo.equals("E") )? 2 :
							   ( t_strStereo.equals("X") )? 1 :
							   0 ;
				t_mapCarbonToStereoWeight.put(t_oCarbon, t_mapCarbonToStereoWeight.get(t_oCarbon) + (double)t_iScore/t_nRelatedCarbons);

				// For unknown stereo
				if ( t_iScore == 1 ) t_bIsUnknown = true;

				// Log stereo and score
				String t_strCarbon = t_oCarbon.getSymbol()+"("+t_oCarbon.getAtomID()+")";
				this.m_sbLog.append( " "+t_strCarbon+": "+ t_strStereo+" - "+(double)t_iScore/t_nRelatedCarbons+"\n");
			}

			if ( t_bIsUnknown ) {
				this.m_aUnknownStereoBonds.add(t_oStereoBond);
				// Log unknown stereo assign
				String t_strBond
				= t_oStereoBond.getAtom1().getSymbol()+"("+t_oStereoBond.getAtom1().getAtomID()+")="
				+ t_oStereoBond.getAtom2().getSymbol()+"("+t_oStereoBond.getAtom2().getAtomID()+")";
				this.m_sbLog.append( "Unknown stereo \"X\" is assigned to "+t_strBond+"\n" );
			}
		}

		// Add stereo weights for Backbone carbons in the SubMolecule and AtomNumberCalculater
		for ( Atom t_oCarbon : t_mapCarbonToStereoWeight.keySet() ) {
			Double t_dWeight = a_oSubMol.getWeightOfBackboneCarbon(t_oCarbon);
			t_dWeight += t_mapCarbonToStereoWeight.get(t_oCarbon) * 0.01D;
			a_oSubMol.setWeightForBackboneCarbon(t_oCarbon, t_dWeight);
		}


		// Log result scores
		this.m_sbLog.append("Result scores:\n");

		// Calculate rank of backbone carbons from additional stereo weights
		for ( Atom t_oCarbon : t_mapCarbonToStereoWeight.keySet() ) {
			int t_iRank = 1;
			double t_dScore = t_mapCarbonToStereoWeight.get(t_oCarbon);
			for ( Atom t_oOtherCarbon : t_mapCarbonToStereoWeight.keySet() ) {
				if ( t_oCarbon.equals(t_oOtherCarbon) ) continue;
				double t_dOtherScore = t_mapCarbonToStereoWeight.get(t_oOtherCarbon);
				if ( t_dScore < t_dOtherScore ) t_iRank++;
			}
			t_mapCarbonToStereoRank.put(t_oCarbon, t_iRank);
		}
		for ( Atom t_oCarbon : t_mapCarbonToStereoWeight.keySet() ) {
			String t_strCarbon = t_oCarbon.getSymbol()+"("+t_oCarbon.getAtomID()+")";
			this.m_sbLog.append( " "+t_strCarbon+":"+t_mapCarbonToStereoWeight.get(t_oCarbon)+"("+t_mapCarbonToStereoRank.get(t_oCarbon)+")\n" );
		}
	}

}
