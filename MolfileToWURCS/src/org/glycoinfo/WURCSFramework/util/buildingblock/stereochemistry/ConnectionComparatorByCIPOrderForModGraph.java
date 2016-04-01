package org.glycoinfo.WURCSFramework.util.buildingblock.stereochemistry;

import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.ConnectionComparatorByCIPOrder;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.HierarchicalDigraphComparator;
import org.glycoinfo.ChemicalStructureUtility.util.stereochemistry.HierarchicalDigraphNode;
import org.glycoinfo.WURCSFramework.buildingblock.ModGraph;

public class ConnectionComparatorByCIPOrderForModGraph extends ConnectionComparatorByCIPOrder {

	private ModGraph m_oModGraph;

	public ConnectionComparatorByCIPOrderForModGraph(HierarchicalDigraphComparator a_oHDComp, ModGraph a_oModGraph) {
		super(a_oHDComp);
		this.m_oModGraph = a_oModGraph;
	}

	@Override
	protected int compareHierarchicalDigraph( HierarchicalDigraphNode a_oHD1, HierarchicalDigraphNode a_oHD2 ) {
		int t_iComp = super.compareHierarchicalDigraph(a_oHD1, a_oHD2);
		if ( t_iComp != 0 ) return t_iComp;

		// Compare backbone carbon in modification
		LinkedList<Connection> t_aHDConns1 = this.getHierarchicalDigraphConnections(a_oHD1);
		LinkedList<Connection> t_aHDConns2 = this.getHierarchicalDigraphConnections(a_oHD2);

		// Compare number of backbone carbon
		LinkedList<Connection> t_aConnsB2M = this.m_oModGraph.getConnectionsToBackbone();
		int t_nBackCarbon1 = 0;
		int t_nBackCarbon2 = 0;
		for ( Connection t_oConnB2M : t_aConnsB2M ) {
			if ( t_aHDConns1.contains( t_oConnB2M.getReverse() ) ) t_nBackCarbon1++;
			if ( t_aHDConns2.contains( t_oConnB2M.getReverse() ) ) t_nBackCarbon2++;
		}
		// Prioritize larger number
		t_iComp = t_nBackCarbon2 - t_nBackCarbon1;
		if ( t_iComp != 0 ) return t_iComp;

		// Compare index of Backbone carbon
		// TODO: Need it?
		LinkedList<Connection> t_aConnB2MList1 = new LinkedList<Connection>();
		LinkedList<Connection> t_aConnB2MList2 = new LinkedList<Connection>();
		int t_nSize = t_aHDConns1.size();
		for ( int i=0; i<t_nSize; i++ ) {
			Connection t_oHDConn1Rev = t_aHDConns1.get(i).getReverse();
			Connection t_oHDConn2Rev = t_aHDConns2.get(i).getReverse();
			if ( !t_aConnsB2M.contains(t_oHDConn1Rev) && !t_aConnsB2M.contains(t_oHDConn2Rev) ) continue;

			// Prioritize smaller index
			if (  t_aConnsB2M.contains(t_oHDConn1Rev) && !t_aConnsB2M.contains(t_oHDConn2Rev) ) return -1;
			if ( !t_aConnsB2M.contains(t_oHDConn1Rev) &&  t_aConnsB2M.contains(t_oHDConn2Rev) ) return 1;

			// Collect connections from backbone carbon to modification
			t_aConnB2MList1.addLast(t_oHDConn1Rev);
			t_aConnB2MList2.addLast(t_oHDConn2Rev);
		}

		// Compare connections from backbone carbon to modification
		t_nSize = t_aConnB2MList1.size();
		for ( int i=0; i<t_nSize; i++ ) {
			Connection t_oConn1 = t_aConnB2MList1.get(i);
			Connection t_oConn2 = t_aConnB2MList2.get(i);
			if ( t_oConn1.equals(t_oConn2) ) continue;
			// Prioritize higher order in modification
			t_iComp = t_aConnsB2M.indexOf(t_oConn2) - t_aConnsB2M.indexOf(t_oConn1);
			if ( t_iComp != 0 ) return t_iComp;
		}

		return 0;
	}

	private LinkedList<Connection> getHierarchicalDigraphConnections(HierarchicalDigraphNode a_oHD) {
		LinkedList<Connection> t_aHDConns = new LinkedList<Connection>();
		LinkedList<HierarchicalDigraphNode> t_aHDWidth = new LinkedList<HierarchicalDigraphNode>();
		t_aHDWidth.add(a_oHD);
		while ( !t_aHDWidth.isEmpty() ) {
			HierarchicalDigraphNode t_oHDNode = t_aHDWidth.removeFirst();
			if ( t_oHDNode.getConnection() == null ) continue;
			t_aHDConns.addLast(t_oHDNode.getConnection());
			for ( HierarchicalDigraphNode t_oChild : t_oHDNode.getChildren() ) {
				t_aHDWidth.addLast(t_oChild);
			}
		}
		return t_aHDConns;
	}
}
