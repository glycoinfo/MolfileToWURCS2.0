package org.glycoinfo.WURCSFramework.util.comparator;

import java.util.Comparator;
import java.util.HashMap;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.MorganAlgorithm;
import org.glycoinfo.ChemicalStructureUtility.util.MorganAlgorithmWithAtomType;
import org.glycoinfo.WURCSFramework.buildingblock.SubMolecule;

/**
 * Comparator for Backbone carbons in a SubMolecule
 * @author MasaakiMatsubara
 *
 */
public class BackboneCarbonComparator implements Comparator<Atom> {

	private SubMolecule m_oSubMol;
	private HashMap<Atom, Integer> m_mapAtomToMorganNumber;
	private HashMap<Atom, Integer> m_mapAtomToMorganNumberWithAtomType;
	private ConnectionComparatorByCIPOrderForSubMolecule m_oCIPComp;

	public BackboneCarbonComparator(SubMolecule a_oSubMol) {
		this.m_oSubMol = a_oSubMol;
		// Calc initial Morgan number
		MorganAlgorithm t_oMA = new MorganAlgorithm(this.m_oSubMol);
		t_oMA.calcMorganNumber(null, null);
		this.m_mapAtomToMorganNumber = t_oMA.getAtomToMorganNumber();
		// Calc Initial Morgan number with atom type
		t_oMA = new MorganAlgorithmWithAtomType(this.m_oSubMol);
		t_oMA.calcMorganNumber(null, null);
		this.m_mapAtomToMorganNumberWithAtomType = t_oMA.getAtomToMorganNumber();

		// Set CIP order calculator
		this.m_oCIPComp = new ConnectionComparatorByCIPOrderForSubMolecule(a_oSubMol);
	}

	public int compare(Atom a_oCarbon1, Atom a_oCarbon2) {

		int t_iComp = 0;

		// Compare Morgan number, lower number is prior than higher one
		int t_iMorganNum1 = this.m_mapAtomToMorganNumber.get(a_oCarbon1);
		int t_iMorganNum2 = this.m_mapAtomToMorganNumber.get(a_oCarbon2);
		t_iComp = t_iMorganNum1 - t_iMorganNum2;
		if ( t_iComp != 0 ) return t_iComp;

		// Compare Morgan number with atom type, lower number is prior than higher one
		t_iMorganNum1 = this.m_mapAtomToMorganNumberWithAtomType.get(a_oCarbon1);
		t_iMorganNum2 = this.m_mapAtomToMorganNumberWithAtomType.get(a_oCarbon2);
		t_iComp = t_iMorganNum1 - t_iMorganNum2;
		if ( t_iComp != 0 ) return t_iComp;

		// Compare CIP order TODO: remove this compare
		Connection t_oB2M1 = this.m_oSubMol.getConnectionFromBackbone(a_oCarbon1);
		Connection t_oB2M2 = this.m_oSubMol.getConnectionFromBackbone(a_oCarbon2);
		t_iComp = this.m_oCIPComp.compare(t_oB2M1, t_oB2M2);
		if ( t_iComp != 0 ) return t_iComp;

		// Compare stereo weight
		Double t_dStereoWeight1 = this.m_oSubMol.getWeightOfBackboneCarbon(a_oCarbon1);
		Double t_dStereoWeight2 = this.m_oSubMol.getWeightOfBackboneCarbon(a_oCarbon2);
		WeightComparator t_oWComp = new WeightComparator();
		t_iComp = t_oWComp.compare(t_dStereoWeight1, t_dStereoWeight2);
		if ( t_iComp != 0 ) return t_iComp;

		return 0;
	}

}
