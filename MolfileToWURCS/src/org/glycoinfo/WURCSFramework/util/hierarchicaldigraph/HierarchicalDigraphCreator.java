package org.glycoinfo.WURCSFramework.util.hierarchicaldigraph;

import java.io.PrintStream;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.ChemicalGraph;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.Chemical;

/**
 * Class for creating hierarchical digraph
 * @author MasaakiMatsubara
 *
 */
public class HierarchicalDigraphCreator {

	private ChemicalGraph m_oGraph;
	private HierarchicalDigraph m_oRootHD;
	private int m_iDepthLimit = 0;
	private boolean m_bIsCompletedFullSearch = true;

	/**
	 * Construct hierarchical digraph from the connection
	 * @param a_oGraph Target graph
	 * @param a_oStart Connection of start
	 * @param a_iDepth Depth limit of depth search
	 */
	public HierarchicalDigraphCreator( ChemicalGraph a_oGraph, Connection a_oStart, int a_iDepth ) {
		this.m_oGraph = a_oGraph;
		this.m_oRootHD = new HierarchicalDigraph( null, a_oStart, Chemical.getAtomicNumber(a_oStart.endAtom().getSymbol()) );
		this.m_iDepthLimit = a_iDepth;
		this.m_bIsCompletedFullSearch = this.depthSearch( this.m_oRootHD, new LinkedList<Atom>() );
		// XXX: Test print
		this.print(System.err);
	}

	public HierarchicalDigraph getHierarchicalDigraph() {
		return this.m_oRootHD;
	}

	public boolean isCompletedFullSearch() {
		return this.m_bIsCompletedFullSearch;
	}

	/**
	 * Construct HierarchicalDigraph using depth-limited search
	 * @param a_oHD Current node of HierarchicalDigraph
	 * @param a_iDepth Distance from depth limit
	 * @param a_aAncestors list of ancestor atoms
	 * @return false if the search reachs depth limit.
	 */
	private boolean depthSearch( HierarchicalDigraph a_oHD, LinkedList<Atom> a_aAncestors ) {
		Atom t_oAtom = a_oHD.getConnection().endAtom();
		if ( !t_oAtom.getSymbol().equals("H") && !this.m_oGraph.contains(t_oAtom) ) return true;
		if ( a_aAncestors.contains( t_oAtom ) ) return true;

		a_aAncestors.addLast(t_oAtom);
		if ( a_aAncestors.size() >= this.m_iDepthLimit ) return false;

		boolean t_bIsCompletedFullSearch = true;

		// Add children
		int t_nAromaticConnection = 0;
		double t_nSumAtomicNumber = 0;
		for ( Connection t_oConn : t_oAtom.getConnections() ) {
			// Ignore out of subgraph except for hydrogen
			if ( !t_oConn.endAtom().getSymbol().equals("H") && !this.m_oGraph.contains( t_oConn.endAtom() ) ) return true;

			double t_iAtomicNumber = (double)Chemical.getAtomicNumber(t_oConn.endAtom().getSymbol());

			// For aromatic atoms
			if ( t_oAtom.isAromatic() || t_oConn.endAtom().isAromatic() ) {
				t_nAromaticConnection++;
				t_nSumAtomicNumber += t_iAtomicNumber;
			}

			// Add child digraph as duplicated atoms for multiple connection
			int t_iBondType = t_oConn.getBond().getType();
			if ( t_iBondType == 3 || t_iBondType == 2 ) {
				a_oHD.addChild( new HierarchicalDigraph( a_oHD, null, t_iAtomicNumber ) );
				if ( t_iBondType == 3 )
					a_oHD.addChild( new HierarchicalDigraph( a_oHD, null, t_iAtomicNumber ) );
			}

			// Depth search for child digraph except for reverse connection
			if ( t_oConn.equals( a_oHD.getConnection().getReverse() ) ) continue;
			HierarchicalDigraph t_oChildHD = new HierarchicalDigraph( a_oHD, t_oConn, t_iAtomicNumber );
			// Set false to full search flag reaching depth limit
			if ( !this.depthSearch(t_oChildHD, a_aAncestors) )
				t_bIsCompletedFullSearch = false;
		}
		// Add duplicated atom for aromatic bond
		if ( t_nAromaticConnection != 0 )
			a_oHD.addChild( new HierarchicalDigraph( a_oHD, null, t_nSumAtomicNumber/(double)t_nAromaticConnection ) );

		a_aAncestors.removeLast();

		return t_bIsCompletedFullSearch;
	}

	/**
	 * Output HierarchicalDigraph to PrintStream.
	 * @param ps
	 */
	private void print(PrintStream ps){
		ps.println();
		ps.print( this.print( this.m_oRootHD, new LinkedList<Boolean>() ) );
	}

	private String print( HierarchicalDigraph a_oHD, LinkedList<Boolean> a_oHistories ) {
		String t_strHistory = "";
		int ii=0;
		for ( Boolean t_bHistory : a_oHistories ) {
			if ( ii >= a_oHistories.size() - 1 ) break;
			t_strHistory += t_bHistory ? " |" : "  ";
			ii++;
		}
		if ( a_oHistories.size() > 0 ){
			t_strHistory += " +";
		}
		String t_strAtom = "null";
		if ( a_oHD.getConnection()!=null ) {
			Atom t_oAtom = a_oHD.getConnection().endAtom();
			t_strAtom = t_oAtom.getSymbol() + "(" + t_oAtom.getAtomID() + ")";
		}
		t_strHistory += "-" + t_strAtom + "(" + a_oHD.getAverageAtomicNumber() + ")" + " : ";

		if ( a_oHD.getChildren() == null ) return t_strHistory + "\n";

		String t_strChildren = "";
		String t_strChildHistories = "";
		int i=0;
		for ( HierarchicalDigraph t_oChild : a_oHD.getChildren() ) {
			i++;
			if ( !t_strChildren.equals("") ) t_strChildren += ", ";
			t_strAtom = "null";
			if ( t_oChild.getConnection()!=null ) {
				Atom t_oAtom = t_oChild.getConnection().endAtom();
				t_strAtom = t_oAtom.getSymbol() + "(" + t_oAtom.getAtomID() + ")";
			}
			t_strChildren += i + "-" + t_strAtom + "(" + a_oHD.getAverageAtomicNumber() + ")";

			// Print child's histories
			a_oHistories.addLast( a_oHD.getChildren().getLast() != t_oChild );
			t_strChildHistories += this.print(t_oChild, a_oHistories);
			a_oHistories.removeLast();
		}
		t_strHistory += t_strChildren + "\n";
		t_strHistory += t_strChildHistories;

		return t_strHistory;
	}
}
