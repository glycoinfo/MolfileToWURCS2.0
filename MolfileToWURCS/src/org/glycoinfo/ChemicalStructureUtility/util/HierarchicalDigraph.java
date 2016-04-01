package org.glycoinfo.ChemicalStructureUtility.util;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.ChemicalGraph;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

/**
 * Class for constructing HierarchicalDigraph and comparing the graph by CIP order<br>
 * Ionic conjugate system with odd-numbered members is not cosidered.
 * TODO: To devide this class into storage and create class which this and HierarchicalDigraphCreator
 * @author KenichiTanaka
 * @author MasaakiMatsubara
 * @see <a href=http://homepage1.nifty.com/nomenclator/text/seqrule.htm>化合物命名法談義</a>
 */
public class HierarchicalDigraph {
	//----------------------------
	// Member variable
	//----------------------------
	/** Atom of this graph */
	private Atom m_oAtom;
	/** Average of atomic number(s) (for conjucate system) */
	private double m_dAverageAtomicNumber;
	/** Parent of this graph (null if this graph is root) */
	private HierarchicalDigraph m_oParentHD = null;
	/** Chidren of this graph */
	private LinkedList<HierarchicalDigraph> m_aChildren = new LinkedList<HierarchicalDigraph>();
	/** Whether or not this graph is unique order */
	private boolean m_bIsUniqOrder;
	/** Whether or not full search of this graph has been completed */
	private boolean m_bHasCompletedFullSearch = true;
	/** Comparator of HierarchicalDigraph */
	private HierarchicalDigraphComparator m_oHDComp;
	/** Aromatic atoms */

//	private HashSet<Atom> m_aAromaticAtoms = new HashSet<Atom>();

	/** Target graph for search */
	private ChemicalGraph m_oTargetGraph;
	/** List of searched atoms */
	private LinkedList<Atom> m_aAncestorAtoms;
	/** Depth of search. For unlimited search, negative value is set.*/
	private int m_iDepth;

	//----------------------------
	// Constructor
	//----------------------------
	/**
	 * For root graph
	 * Construct HierarchicalDigraph stating from "atom".
	 * @param targetgraph Target graph for search
	 * @param atom Atom of start node
	 * @param depth Depth of search
	 * @param EZRSCheck Flag for EZRS check
	 */
	public HierarchicalDigraph(final ChemicalGraph targetgraph, final Atom atom, final int depth, final HierarchicalDigraphComparator comparator){
		this.m_oTargetGraph = targetgraph;
		this.m_oAtom = atom;
		this.m_iDepth = depth;
		this.m_dAverageAtomicNumber = Chemical.getAtomicNumber(atom.getSymbol());
		this.m_aAncestorAtoms = new LinkedList<Atom>();
		this.m_oHDComp = comparator;

/*
		// Search aromatic atoms
		Aromatization t_oAromatic = new Aromatization();
		for ( Atom a : this.m_oTargetGraph.getAtoms() ) {
			t_oAromatic.clear();
			if ( !this.m_aAromaticAtoms.contains(a) && t_oAromatic.start(a) )
				this.m_aAromaticAtoms.addAll(t_oAromatic);
		}
*/
		this.m_bHasCompletedFullSearch = this.depthSearch(atom, depth, this.m_dAverageAtomicNumber);
		this.initializeSearchFlag();
	}

	/**
	 * For child graph
	 * @param parent Parent graph
	 * @param atom Atom of start node
	 * @param averageAtomicNumber
	 */
	public HierarchicalDigraph(final HierarchicalDigraph parent, final Atom atom, final double averageAtomicNumber) {
		this.m_oParentHD = parent;
		this.m_oAtom = atom;
		this.m_iDepth = parent.m_iDepth - 1;
		this.m_dAverageAtomicNumber = averageAtomicNumber;
		this.m_oTargetGraph = parent.m_oTargetGraph;
		this.m_aAncestorAtoms = parent.m_aAncestorAtoms;
		this.m_oHDComp = parent.m_oHDComp;
//		this.m_aAromaticAtoms = parent.m_aAromaticAtoms;

		this.m_bHasCompletedFullSearch = this.depthSearch(atom, this.m_iDepth, averageAtomicNumber);
		this.initializeSearchFlag();
	}

	private void initializeSearchFlag() {
		if(this.m_aChildren == null) return;
		for(HierarchicalDigraph child : this.m_aChildren){
			if(child.m_bHasCompletedFullSearch) continue;
			this.m_bHasCompletedFullSearch = false;
		}
	}

	//----------------------------
	// Accessor
	//----------------------------
	public double getAverageAtomicNumber() {
		return this.m_dAverageAtomicNumber;
	}

	public Atom getAtom() {
		return this.m_oAtom;
	}

	public Connection getConnection() {
		if ( this.m_oParentHD == null ) return null;
		for ( Connection con : this.m_oParentHD.m_oAtom.getConnections() ) {
			if ( con.endAtom().equals( this.m_oAtom ) ) return con;
		}
		return null;
	}

	public Connection getConnectionToParent() {
		if ( this.m_oParentHD == null ) return null;
		for ( Connection con : this.m_oAtom.getConnections() ) {
			if ( con.endAtom().equals( this.m_oParentHD.m_oAtom ) ) return con;
		}
		return null;
	}

	public LinkedList<HierarchicalDigraph> getChildren() {
		return this.m_aChildren;
	}

	public void addChild(HierarchicalDigraph child) {
		this.m_aChildren.addLast(child);
	}

	public void sortChildren( HierarchicalDigraphComparator comparator ) {
		Collections.sort( this.m_aChildren, comparator );
	}

	/**
	 * Whether or not this graph is unique order
	 */
	public boolean isUniqueOrder() {
		return this.m_bIsUniqOrder;
	}

	public boolean isCompletedFullSearch() {
		return this.m_bHasCompletedFullSearch;
	}

	public void setCompleteFullSearch(boolean b) {
		this.m_bHasCompletedFullSearch = b;
	}

	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * Output HierarchicalDigraph to PrintStream.
	 */
	public void print(PrintStream ps){
		ps.println();
		this.print(new LinkedList<Boolean>(), ps);
	}

	//----------------------------
	// Public method (non void)
	//----------------------------

	//----------------------------
	// Private method
	//----------------------------
	/**
	 * Construct HierarchicalDigraph using depth-first search
	 * @param atom Atom for search
	 * @param averageAtomicNumber Average atomic number, for conjugate system
	 * @param depth 探索する深さに制限を付ける場合に利用
	 * @return True if full search is completed
	 */
	private boolean depthSearch(final Atom atom, final int depth, final double averageAtomicNumber){
//		if( atom!=null && !atom.symbol.equals("H") && !targetgraph.contains(atom) ) return;
		if( atom!=null && !atom.getSymbol().equals("H") && !this.m_oTargetGraph.contains(atom) ) return true;
//		this.m_oAtom = atom;
//		this.m_dAverageAtomicNumber = averageAtomicNumber;
//		if(this.m_oAtom == null) return true;
		if(atom == null) return true;
		// Return true if terminal atom
		boolean t_bIsTerminal = false;
		for ( Connection t_oConn : atom.getConnections() ) {
			if ( t_oConn.endAtom().getSymbol().equals("H") ) continue;
			if ( this.m_oTargetGraph.contains( t_oConn.endAtom() ) ) continue;
			t_bIsTerminal = true;
		}
		if ( t_bIsTerminal ) return true;
//		if(this.m_aAncestorAtoms.contains(this.m_oAtom)) return true;
		if(this.m_aAncestorAtoms.contains(atom)) return true;
		// Full search has not completed, reaching to depth end
		if(depth == 0) return false;

		// Add Children
		this.m_aChildren = new LinkedList<HierarchicalDigraph>();
		int num = 0;
		int sumAtomicNumber = 0;
//		m_aAncestorAtoms.addLast(this.m_oAtom);
		m_aAncestorAtoms.addLast(atom);
//		for(Connection connection : this.m_oAtom.getConnections()){
		for(Connection connection : atom.getConnections()){
			Atom conatom = connection.endAtom();
			// Skip if the connect atom is hydrogen or out of target graph
			if( !conatom.getSymbol().equals("H") && !this.m_oTargetGraph.contains(conatom) ) continue;
//			this.children.add(new HierarchicalDigraph(targetgraph, conatom, depth-1, (double)Chemical.getAtomicNumber(conatom.getSymbol()), ancestors, EZRScheck));
			// Add child sub graph for connect atom
			this.m_aChildren.add( new HierarchicalDigraph( this, conatom, (double)Chemical.getAtomicNumber(conatom.getSymbol()) ) );
			// For conjugate or multiple bond, it is consider that same atom is duplecated.
//			if(this.atom.isAromatic && connection.atom.isAromatic){
//			if( this.m_aAromaticAtoms.contains(this.m_oAtom) && this.m_aAromaticAtoms.contains(conatom) ){
//			if ( this.m_oAtom.isAromatic() && conatom.isAromatic() ) {
			if ( atom.isAromatic() && conatom.isAromatic() ) {
				num++;
				sumAtomicNumber+=(double)Chemical.getAtomicNumber(conatom.getSymbol());
			}else if(connection.getBond().getType()==2 || connection.getBond().getType()==3){
				for(int ii=connection.getBond().getType(); ii>1; ii--){
					this.m_aChildren.add( new HierarchicalDigraph( this, null, (double)Chemical.getAtomicNumber(conatom.getSymbol()) ) );
				}
			}
		}
		// 共益系 connect
		if(num!=0){
			this.m_aChildren.add( new HierarchicalDigraph( this, null, (double)sumAtomicNumber/(double)num ) );
		}
		m_aAncestorAtoms.removeLast();

		// Sort Children
		Collections.sort(this.m_aChildren, this.m_oHDComp);
/*
		Collections.sort(this.children, new Comparator<HierarchicalDigraph>() {
			public int compare(HierarchicalDigraph tree1, HierarchicalDigraph tree2) {
				return tree1.compareTo(tree2, EZRScheck);
			}
		});
*/
		// Set false to "isUniqOrder" of child graph which CIP order is not unique
		// CIPorderがユニークにならない要素にfalseを立てる
		for(HierarchicalDigraph child : this.m_aChildren){
			child.m_bIsUniqOrder = true;
		}
		int childrenNum = this.m_aChildren.size();
		for(int ii=0; ii<childrenNum-1; ii++){
			HierarchicalDigraph tree1 = this.m_aChildren.get(ii);
			HierarchicalDigraph tree2 = this.m_aChildren.get(ii+1);
//			if(tree1.compareTo(tree2, EZRScheck)!=0) continue;
			if( this.m_oHDComp.compare(tree1, tree2)!=0 ) continue;

			// Set complete full search flag if two tree has same branch
			if ( this.m_oHDComp.foundSameBranch() ) {
				tree1.setCompleteFullSearch(true);
				tree2.setCompleteFullSearch(true);
			}

			tree1.m_bIsUniqOrder = false;
			tree2.m_bIsUniqOrder = false;
		}

		return true;
	}

	/**
	 * Output HierarchicalDigraph to PrintStream.
	 * @param historys
	 * @param ps
	 */
	private void print(LinkedList<Boolean> historys, PrintStream ps){
		int ii=0;
		for(Boolean history : historys){
			if(ii>=historys.size()-1) break;
			ps.print(history ? " |" : "  ");
			ii++;
		}
		if(historys.size()>0){
			ps.print(" +");
		}
//		ps.print("-" + ((this.atom==null)?"null":(this.atom.getSymbol() + "(" + this. this.atom.molfileAtomNo + ")")) + "(" + this.averageAtomicNumber + ")" + " : ");
		ps.print("-" + ((this.m_oAtom==null)?"null":(this.m_oAtom.getSymbol() + "(" + this.m_oAtom.getAtomID() + ")")) + "(" + this.m_dAverageAtomicNumber + ")" + " : ");

		if(this.m_aChildren==null) return;

		for(HierarchicalDigraph child : this.m_aChildren){
			ps.print((this.m_aChildren.indexOf(child)+1) + "(" + (child.m_bIsUniqOrder?"o":"x") + ")" + "." + ((child.m_oAtom==null)?"null":(child.m_oAtom.getSymbol() + "(" + child.m_oAtom.getAtomID() + ")")) + "(" + child.m_dAverageAtomicNumber + "), ");
		}
		ps.println();

		for(HierarchicalDigraph child : m_aChildren){
			historys.addLast(m_aChildren.getLast()!=child);
			child.print(historys, ps);
			historys.removeLast();
		}
	}
}
