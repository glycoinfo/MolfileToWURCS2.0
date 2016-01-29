package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import java.util.Comparator;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

/**
 * Comparator class for coonections by CIP order
 * @author matsubara
 *
 */
public class ConnectionComparatorByCIPOrder implements Comparator<Connection> {

	private HierarchicalDigraphComparator m_oHDComp;

	public ConnectionComparatorByCIPOrder(HierarchicalDigraphComparator a_oHDComp) {
		this.m_oHDComp = a_oHDComp;
	}

	public int compare(Connection a_oConnection1, Connection a_oConnection2) {

		// Compare by CIP order using HierarchicalDigraph with iterative deepening depth-first search
		int t_iDepth = 1;
		while ( true ) {
			// Create hierarchical digraph starting from connection
			HierarchicalDigraphCreator t_oHDCreate1 = new HierarchicalDigraphCreator(a_oConnection1, t_iDepth);
			HierarchicalDigraphCreator t_oHDCreate2 = new HierarchicalDigraphCreator(a_oConnection2, t_iDepth);

			HierarchicalDigraphNode t_oHD1 = t_oHDCreate1.getHierarchicalDigraph();
			HierarchicalDigraphNode t_oHD2 = t_oHDCreate2.getHierarchicalDigraph();
			t_oHD1.sortChildren(this.m_oHDComp);
			t_oHD2.sortChildren(this.m_oHDComp);
			// Compare CIP orders using HierarchicalDigraph
			int t_iComp = this.m_oHDComp.compare(t_oHD1, t_oHD2);
			if ( t_iComp != 0 ) return t_iComp;
			if ( this.m_oHDComp.foundSameBranch() ) return 0;

			// Return if full search has completed
			if ( t_oHDCreate1.isCompletedFullSearch() && t_oHDCreate2.isCompletedFullSearch() ) return 0;

			t_iDepth++;
		}

	}

}
