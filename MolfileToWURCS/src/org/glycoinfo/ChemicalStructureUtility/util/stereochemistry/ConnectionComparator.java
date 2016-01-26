package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import java.util.Comparator;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

public class ConnectionComparator implements Comparator<Connection> {

	private HierarchicalDigraphComparator m_oHDComp;

	public ConnectionComparator(HierarchicalDigraphComparator a_oHDComp) {
		this.m_oHDComp = a_oHDComp;
	}

	protected HierarchicalDigraphCreator getHierarchicalDigraphCreator( Connection a_oConnection, int a_iDepth ) {
		return new HierarchicalDigraphCreator( a_oConnection, a_iDepth );
	}

	@Override
	public int compare(Connection a_oConnection1, Connection a_oConnection2) {
		// Prioritize smaller bond type
		if ( a_oConnection1.getBond().getType() != a_oConnection1.getBond().getType() )
			return  a_oConnection1.getBond().getType() - a_oConnection1.getBond().getType();

		// Compare by CIP order
		int t_iDepth = 1;
		while ( true ) {
			// Calcurate CIP orders for each connection using HierarchicalDigraph
			boolean t_bIsCompletedFullSearch = true;

			// Create hierarchical digraph starting from connections on the atom
			HierarchicalDigraphCreator t_oHDCreate1 = this.getHierarchicalDigraphCreator(a_oConnection1, t_iDepth);
			HierarchicalDigraphCreator t_oHDCreate2 = this.getHierarchicalDigraphCreator(a_oConnection2, t_iDepth);
/*
			HierarchicalDigraphComparator t_oHDComp = new HierarchicalDigraphComparator();
			LinkedList<HierarchicalDigraphNode> t_aChildHDs = new LinkedList<HierarchicalDigraphNode>();
			HashMap<HierarchicalDigraphNode, Boolean> t_mapIsCompletedFullSearch = new HashMap<HierarchicalDigraphNode, Boolean>();
			for ( Connection t_oConn : a_aConns ) {
				HierarchicalDigraphCreator t_oHDCreate = this.getHierarchicalDigraphCreator(t_oConn, t_iDepth);
				t_mapIsCompletedFullSearch.put(t_oHDCreate.getHierarchicalDigraph(), t_oHDCreate.isCompletedFullSearch());
				if ( !t_oHDCreate.isCompletedFullSearch() )
					t_bIsCompletedFullSearch = false;
				t_aChildHDs.addLast( t_oHDCreate.getHierarchicalDigraph() );
			}

			// Sort and order hierarchical digraphs
			Collections.sort(t_aChildHDs, a_oHDComp);
			LinkedList<Connection> t_aSortedConnections = new LinkedList<Connection>();
			HierarchicalDigraphNode t_oPreHD = t_aChildHDs.getFirst();
			t_aSortedConnections.addFirst( t_oPreHD.getConnection() );
			boolean t_bIsUniqueOrder = true;
			for ( HierarchicalDigraphNode t_oHD : t_aChildHDs ) {
				if ( t_oHD.equals(t_oPreHD) ) continue;
				if ( a_oHDComp.compare(t_oPreHD, t_oHD) == 0 ) {
					t_bIsUniqueOrder = false;
					if ( t_mapIsCompletedFullSearch.get(t_oPreHD) && t_mapIsCompletedFullSearch.get(t_oHD) )
						t_bIsCompletedFullSearch = true;
				}
				t_aSortedConnections.addLast( t_oHD.getConnection() );
				t_oPreHD = t_oHD;
			}
			if ( t_bIsUniqueOrder ) return t_aSortedConnections;

			if ( t_bIsCompletedFullSearch ) return null;
*/
			t_iDepth++;
		}

//		return 0;
	}

}
