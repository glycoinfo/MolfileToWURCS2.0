package org.glycoinfo.WURCSFramework.util.unuse;

import java.util.Collections;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.ConnectionComparatorByCIPOrder;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.HierarchicalDigraphComparator;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.StereochemistryAnalysis;

public class StereochemistryAnalysisForModGraph extends StereochemistryAnalysis {

	private ModGraph m_oModGraph;

	public void start( ModGraph a_oGraph ) {
		this.m_oModGraph = a_oGraph;
		super.start(a_oGraph);
	}

	protected LinkedList<Connection> sortConnectionsByCIPOrder( LinkedList<Connection> a_aConns, HierarchicalDigraphComparator a_oHDComp ) {
		ConnectionComparatorByCIPOrder t_oConnComp = new ConnectionComparatorByCIPOrderForModGraph(a_oHDComp, this.m_oModGraph);
		Collections.sort(a_aConns, t_oConnComp);
		Connection t_oPreConn = a_aConns.getFirst();
		for ( Connection t_oConn : a_aConns ) {
			if ( t_oPreConn.equals(t_oConn) ) continue;
			if ( t_oConnComp.compare(t_oPreConn, t_oConn) == 0 ) return null;
			t_oPreConn = t_oConn;
		}
		return a_aConns;

	}
}
