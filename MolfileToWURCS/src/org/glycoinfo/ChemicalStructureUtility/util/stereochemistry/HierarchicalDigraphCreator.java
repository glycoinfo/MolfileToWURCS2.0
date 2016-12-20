package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

/**
 * Class for creating hierarchical digraph
 * @author MasaakiMatsubara
 *
 */
public class HierarchicalDigraphCreator {

	private AtomicNumberCalculator m_oANumCalc;
	private HierarchicalDigraphNode m_oRootHD = null;
	private int m_iDepthLimit = 1;
	private boolean m_bIsCompletedFullSearch = true;

	public HierarchicalDigraphCreator( AtomicNumberCalculator a_oANumCalc ) {
		this.m_oANumCalc = a_oANumCalc;
	}

	/**
	 * Make hierarchical digraph from the connection
	 * @param a_oGraph Target graph
	 * @param a_oStart Connection of start
	 * @param a_iDepth Depth limit of depth search, which must be an integer 1 or more
	 */
	public void start( Connection a_oStart, int a_iDepth ) {
		this.m_oRootHD = new HierarchicalDigraphNode( a_oStart, this.getAverageAtomicNumber(a_oStart.endAtom()) );
		this.m_iDepthLimit = a_iDepth;

		// Set start atom
		LinkedList<Atom> t_aAncestors = new LinkedList<Atom>();
		t_aAncestors.add(a_oStart.startAtom());

		// Do IDDFS
		this.m_bIsCompletedFullSearch = this.depthSearch( this.m_oRootHD, t_aAncestors );
		// XXX: Test print
//		this.print(System.err);
	}

	public HierarchicalDigraphNode getHierarchicalDigraph() {
		return this.m_oRootHD;
	}

	public boolean isCompletedFullSearch() {
		return this.m_bIsCompletedFullSearch;
	}

	public LinkedList<Atom> getSortedContainedAtoms( HierarchicalDigraphComparator a_oHDComp ) {
		LinkedList<Atom> t_aAtoms = new LinkedList<Atom>();
		LinkedList<HierarchicalDigraphNode> t_aDigraphs = new LinkedList<HierarchicalDigraphNode>();
		t_aDigraphs.add(this.m_oRootHD);
		while ( !t_aDigraphs.isEmpty() ) {
			HierarchicalDigraphNode t_oGraph = t_aDigraphs.removeFirst();
			if ( t_oGraph.getConnection() == null ) continue;
			t_aAtoms.addLast( t_oGraph.getConnection().endAtom() );

			if ( t_oGraph.getChildren().isEmpty() ) continue;
			LinkedList<HierarchicalDigraphNode> t_aChildren = t_oGraph.getChildren();
			Collections.sort(t_aChildren, a_oHDComp);
			for ( HierarchicalDigraphNode t_oChild : t_aChildren )
				t_aDigraphs.addLast(t_oChild);
		}
		return t_aAtoms;
	}

	protected boolean isIgnoreAtom(Atom a_oAtom) {
		return false;
	}

	private double getAverageAtomicNumber(Atom a_oAtom) {
		return this.m_oANumCalc.getAtomicNumber(a_oAtom);
	}

	/**
	 * Construct HierarchicalDigraph using depth-limited search
	 * @param a_oHD Current node of HierarchicalDigraph
	 * @param a_iDepth Distance from depth limit
	 * @param a_aAncestors list of ancestor atoms
	 * @return false if the search reachs depth limit.
	 */
	private boolean depthSearch( HierarchicalDigraphNode a_oHD, LinkedList<Atom> a_aAncestors ) {
		Atom t_oAtom = a_oHD.getConnection().endAtom();
		if ( this.isIgnoreAtom(t_oAtom) ) return true;
		if ( a_aAncestors.contains( t_oAtom ) ) return true;
		if ( a_aAncestors.size() > this.m_iDepthLimit ) return false;

		a_aAncestors.addLast(t_oAtom);

		boolean t_bIsCompletedFullSearch = true;

		// Add children
		int t_nAromaticConnection = 0;
		double t_nSumAtomicNumber = 0;
		for ( Connection t_oConn : t_oAtom.getConnections() ) {
			// Ignore out of subgraph except for hydrogen
			if ( this.isIgnoreAtom( t_oConn.endAtom() ) ) continue;

			double t_iAtomicNumber = this.getAverageAtomicNumber(t_oConn.endAtom());

			// Count aromatic connections and sum atomic number of naighbor aromatic atom
			boolean t_bIsAromatic = false;
			if ( t_oAtom.isAromatic() || t_oConn.endAtom().isAromatic() ) {
				t_nAromaticConnection++;
				t_nSumAtomicNumber += t_iAtomicNumber;
				t_bIsAromatic = true;
			}

			// Add child digraph as duplicated atoms for multiple connection
			int t_iBondType = t_oConn.getBond().getType();
			if ( t_iBondType == 3 || t_iBondType == 2 ) {
				if ( !t_bIsAromatic )
					a_oHD.addChild( new HierarchicalDigraphNode( null, t_iAtomicNumber ) );
				if ( t_iBondType == 3 )
					a_oHD.addChild( new HierarchicalDigraphNode( null, t_iAtomicNumber ) );
			}

			// Depth search for child digraph except for reverse connection
			if ( t_oConn.equals( a_oHD.getConnection().getReverse() ) ) continue;
			HierarchicalDigraphNode t_oChildHD = new HierarchicalDigraphNode( t_oConn, t_iAtomicNumber );
			a_oHD.addChild( t_oChildHD );
			// Set false to full search flag reaching depth limit
			if ( !this.depthSearch(t_oChildHD, a_aAncestors) )
				t_bIsCompletedFullSearch = false;
		}
		// Add duplicated atom for aromatic bond
		if ( t_nAromaticConnection != 0 )
			a_oHD.addChild( new HierarchicalDigraphNode( null, t_nSumAtomicNumber/(double)t_nAromaticConnection ) );

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

	private String print( HierarchicalDigraphNode a_oHD, LinkedList<Boolean> a_oHistories ) {
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

		if ( a_oHD.getChildren().isEmpty() ) return t_strHistory + "\n";

		// Sort children
		LinkedList<HierarchicalDigraphNode> t_oChildren = a_oHD.getChildren();
		Collections.sort( t_oChildren, new HierarchicalDigraphComparator() );
		String t_strChildren = "";
		String t_strChildHistories = "";
		int i=0;
		for ( HierarchicalDigraphNode t_oChild : t_oChildren ) {
			i++;
			if ( !t_strChildren.equals("") ) t_strChildren += ", ";
			t_strAtom = "null";
			if ( t_oChild.getConnection()!=null ) {
				Atom t_oAtom = t_oChild.getConnection().endAtom();
				t_strAtom = t_oAtom.getSymbol() + "(" + t_oAtom.getAtomID() + ")";
			}
			t_strChildren += i + "-" + t_strAtom + "(" + t_oChild.getAverageAtomicNumber() + ")";

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
