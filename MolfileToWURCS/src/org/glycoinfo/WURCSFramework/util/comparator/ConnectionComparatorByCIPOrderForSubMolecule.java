package org.glycoinfo.WURCSFramework.util.comparator;

import java.util.Comparator;
import java.util.HashMap;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.ConnectionComparatorByCIPOrder;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.HierarchicalDigraphComparatorWithStereo;
import org.glycoinfo.WURCSFramework.buildingblock.SubMolecule;

public class ConnectionComparatorByCIPOrderForSubMolecule implements Comparator<Connection> {

	private ConnectionComparatorByCIPOrder m_oCIPComp;

	public ConnectionComparatorByCIPOrderForSubMolecule(SubMolecule a_oSubMol) {
		HierarchicalDigraphComparatorWithStereo t_oHDComp = new HierarchicalDigraphComparatorWithStereo();
		HashMap<Atom, String> t_mapAtomToStereo = new HashMap<Atom, String>();
		for ( Atom t_oAtom : a_oSubMol.getAtoms() )
			t_mapAtomToStereo.put(t_oAtom, t_oAtom.getChirality() );
		HashMap<Bond, String> t_mapBondToStereo = new HashMap<Bond, String>();
		for ( Bond t_oBond : a_oSubMol.getBonds() )
			t_mapBondToStereo.put(t_oBond, t_oBond.getGeometric() );
		t_oHDComp.setAtomStereos(t_mapAtomToStereo);
		t_oHDComp.setBondStereos(t_mapBondToStereo);
		this.m_oCIPComp = new ConnectionComparatorByCIPOrder(t_oHDComp);
	}

	public int compare(Connection a_oConn1, Connection a_oConn2) {
		return this.m_oCIPComp.compare(a_oConn1, a_oConn2);
	}

}
