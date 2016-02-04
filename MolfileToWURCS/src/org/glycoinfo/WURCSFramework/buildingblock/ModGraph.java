package org.glycoinfo.WURCSFramework.buildingblock;

import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.ChemicalGraph;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraph;

public class ModGraph extends SubGraph {

	private LinkedList<Connection> m_aConnectionsFromBackbone = new LinkedList<Connection>();

	public ModGraph(ChemicalGraph a_oOriginalGraph) {
		super(a_oOriginalGraph);
	}

	public LinkedList<Connection> getConnectionsFromBackbone() {
		return this.m_aConnectionsFromBackbone;
	}

	public void addOriginalConnectionFromBackbone( Connection a_oConn ) {
		if ( !this.m_oOriginalGraph.contains( a_oConn.getBond() ) ) return;
		if ( !this.m_mapOriginalToAtom.containsKey( a_oConn.endAtom() ) ) return;
		Atom t_oCopyModAtom = this.m_mapOriginalToAtom.get( a_oConn.endAtom() );

		// Copy and map backbone carbon
		Atom t_oCopyBackCarbon = a_oConn.startAtom().copy();
		this.m_aAtoms.addLast(t_oCopyBackCarbon);
		this.m_mapAtomToOriginal.put(t_oCopyBackCarbon, a_oConn.startAtom());

		// Copy and map bond
		Atom t_oAtom1 = t_oCopyBackCarbon;
		Atom t_oAtom2 = t_oCopyModAtom;
		if ( a_oConn.endAtom().equals( a_oConn.getBond().getAtom1() ) ) {
			t_oAtom1 = t_oCopyModAtom;
			t_oAtom2 = t_oCopyBackCarbon;
		}
		Bond t_oCopyBond = a_oConn.getBond().copy(t_oAtom1, t_oAtom2);
		this.m_aBonds.addLast(t_oCopyBond);
		this.m_mapBondToOriginal.put(t_oCopyBond, a_oConn.getBond());

		// Map connection
		Connection t_oCopyConnFromBack = t_oCopyBackCarbon.getConnections().getFirst();
		this.m_mapConnectionToOriginal.put(t_oCopyConnFromBack, a_oConn);
		this.m_mapConnectionToOriginal.put(t_oCopyConnFromBack.getReverse(), a_oConn.getReverse());
		this.m_aConnectionsFromBackbone.addLast(t_oCopyConnFromBack);
	}
}
