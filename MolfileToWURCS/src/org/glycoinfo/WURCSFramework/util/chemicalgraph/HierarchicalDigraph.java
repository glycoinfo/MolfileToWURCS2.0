package org.glycoinfo.WURCSFramework.util.chemicalgraph;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.ChemicalGraph;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.Cyclization;

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
	private Atom atom;
	/** Average of atomic number(s) (for conjucate system) */
	private double averageAtomicNumber;
	/** Parent of this graph (null if this graph is root) */
	private HierarchicalDigraph parent = null;
	/** Chidren of this graph */
	private LinkedList<HierarchicalDigraph> children = new LinkedList<HierarchicalDigraph>();
	/** Whether or not this graph is unique order */
	private boolean isUniqOrder;
	/** Whether or not full search of this graph has been completed */
	private boolean isCompletedFullSearch = true;
	/** Comparator of HierarchicalDigraph */
	private HierarchicalDigraphComparator comparator;
	/** Aromatic atoms */

	private HashSet<Atom> m_aAromaticAtoms = new HashSet<Atom>();

	/** Target graph for search */
	private ChemicalGraph targetGraph;
	/** List of searched atoms */
	private LinkedList<Atom> ancestors;
	/** Depth of search. For unlimited search, negative value is set.*/
	private int depth;

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
		this.targetGraph = targetgraph;
		this.depth = depth;
		this.averageAtomicNumber = Chemical.getAtomicNumber(atom.getSymbol());
		this.ancestors = new LinkedList<Atom>();
		this.comparator = comparator;

		// Search aromatic atoms
		Cyclization cyclic = new Cyclization();
		for ( Atom a : this.targetGraph.getAtoms() ) {
			cyclic.clear();
			if ( cyclic.aromatize(a) ) this.m_aAromaticAtoms.addAll(cyclic);
		}

		this.isCompletedFullSearch = true;
		this.depthSearch(atom, depth, this.averageAtomicNumber);
		if(this.children != null){
			for(HierarchicalDigraph child : this.children){
				if(child.isCompletedFullSearch==false){
					this.isCompletedFullSearch = false;
				}
			}
		}
	}

	/**
	 * For child graph
	 * @param parent Parent graph
	 * @param atom Atom of start node
	 * @param averageAtomicNumber
	 */
	public HierarchicalDigraph(final HierarchicalDigraph parent, final Atom atom, final double averageAtomicNumber) {
		this.parent = parent;
		this.atom = atom;
		this.depth = parent.depth - 1;
		this.targetGraph = parent.targetGraph;
		this.ancestors = parent.ancestors;
		this.comparator = parent.comparator;

		this.isCompletedFullSearch = true;
		this.depthSearch(atom, this.depth, averageAtomicNumber);
		if(this.children != null){
			for(HierarchicalDigraph child : this.children){
				if(child.isCompletedFullSearch==false){
					this.isCompletedFullSearch = false;
				}
			}
		}
	}

	//----------------------------
	// Accessor
	//----------------------------
	public double getAverageAtomicNumber() {
		return this.averageAtomicNumber;
	}

	public Atom getAtom() {
		return this.atom;
	}

	public Connection getConnection() {
		if ( this.parent == null ) return null;
		for ( Connection con : this.parent.atom.getConnections() ) {
			if ( con.endAtom().equals( this.atom ) ) return con;
		}
		return null;
	}

	public Connection getConnectionToParent() {
		if ( this.parent == null ) return null;
		for ( Connection con : this.atom.getConnections() ) {
			if ( con.endAtom().equals( this.parent.atom ) ) return con;
		}
		return null;
	}

	public LinkedList<HierarchicalDigraph> getChildren() {
		return this.children;
	}

	public void addChild(HierarchicalDigraph child) {
		this.children.addLast(child);
	}

	public void sortChildren( HierarchicalDigraphComparator comparator ) {
		Collections.sort( this.children, comparator );
	}

	/**
	 * Whether or not this graph is unique order
	 */
	public boolean isUniqueOrder() {
		HierarchicalDigraph child1, child2;
		int numChildren = this.children.size();
		if ( numChildren == 0 ) return true;
		for(int ii=0; ii<numChildren-1; ii++){
			child1 = this.children.get(ii);
			child2 = this.children.get(ii+1);
//			if(tree1.compareTo(tree2, EZRScheck)!=0) continue;
			if ( this.comparator.compare(child1, child2)!=0 ) continue;
//			if ( child1.isUniqueOrder() && child2.isUniqueOrder() ) continue;
			return false;
		}
		return true;
	}

	public boolean isCompletedFullSearch() {
		return this.isCompletedFullSearch;
	}

	public void setCompleteFullSearch(boolean b) {
		this.isCompletedFullSearch = b;
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
	 * @param ancestors 探索済み原子のリスト、該当原子に到達したら引き返す。
	 * @param depth 探索する深さに制限を付ける場合に利用
	 */
	private void depthSearch(final Atom atom, final int depth, final double averageAtomicNumber){
//		if( atom!=null && !atom.symbol.equals("H") && !targetgraph.contains(atom) ) return;
		if( atom!=null && !atom.getSymbol().equals("H") && !this.targetGraph.contains(atom) ) return;
		this.atom = atom;
		this.averageAtomicNumber = averageAtomicNumber;
		this.children = new LinkedList<HierarchicalDigraph>();
		if(this.atom == null) return;
		if(this.ancestors.contains(this.atom)) return;
		if(depth == 0){
			this.isCompletedFullSearch = false;
			return;
		}

		// Add Children
		int num = 0;
		int sumAtomicNumber = 0;
		ancestors.addLast(this.atom);
		for(Connection connection : this.atom.getConnections()){
			Atom conatom = connection.endAtom();
			// Skip if the connect atom is hydrogen or out of target graph
			if( !conatom.getSymbol().equals("H") && !this.targetGraph.contains(conatom) ) continue;
//			this.children.add(new HierarchicalDigraph(targetgraph, conatom, depth-1, (double)Chemical.getAtomicNumber(conatom.getSymbol()), ancestors, EZRScheck));
			// Add child sub graph for connect atom
			this.children.add( new HierarchicalDigraph( this, conatom, (double)Chemical.getAtomicNumber(conatom.getSymbol()) ) );
			// For conjugate or multiple bond, it is consider that same atom is duplecated.
//			if(this.atom.isAromatic && connection.atom.isAromatic){
			if( this.m_aAromaticAtoms.contains(this.atom) && this.m_aAromaticAtoms.contains(conatom) ){
				num++;
				sumAtomicNumber+=(double)Chemical.getAtomicNumber(conatom.getSymbol());
			}else if(connection.getBond().getType()==2 || connection.getBond().getType()==3){
				for(int ii=connection.getBond().getType(); ii>1; ii--){
					this.children.add( new HierarchicalDigraph( this, null, (double)Chemical.getAtomicNumber(conatom.getSymbol()) ) );
				}
			}
		}
		// 共益系 connect
		if(num!=0){
			this.children.add( new HierarchicalDigraph( this, null, (double)sumAtomicNumber/(double)num ) );
		}
		ancestors.removeLast();

		// Sort Children
		Collections.sort(this.children, this.comparator);
/*
		Collections.sort(this.children, new Comparator<HierarchicalDigraph>() {
			public int compare(HierarchicalDigraph tree1, HierarchicalDigraph tree2) {
				return tree1.compareTo(tree2, EZRScheck);
			}
		});
*/
		// Set false to "isUniqOrder" of child graph which CIP order is not unique
		// CIPorderがユニークにならない要素にfalseを立てる
		for(HierarchicalDigraph child : this.children){
			child.isUniqOrder = true;
		}
		int childrenNum = this.children.size();
		for(int ii=0; ii<childrenNum-1; ii++){
			HierarchicalDigraph tree1 = this.children.get(ii);
			HierarchicalDigraph tree2 = this.children.get(ii+1);
//			if(tree1.compareTo(tree2, EZRScheck)!=0) continue;
			if( this.comparator.compare(tree1, tree2)!=0 ) continue;
			tree1.isUniqOrder = false;
			tree2.isUniqOrder = false;
		}
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
		ps.print("-" + ((this.atom==null)?"null":(this.atom.getSymbol() + "(" + this.targetGraph.getAtoms().indexOf(this.atom) + ")")) + "(" + this.averageAtomicNumber + ")" + " : ");

		if(this.children==null) return;

		for(HierarchicalDigraph child : this.children){
			ps.print((this.children.indexOf(child)+1) + "(" + (child.isUniqOrder?"o":"x") + ")" + "." + ((child.atom==null)?"null":(child.atom.getSymbol() + "(" + this.targetGraph.getAtoms().indexOf(this.atom) + ")")) + "(" + child.averageAtomicNumber + "), ");
		}
		ps.println();

		for(HierarchicalDigraph child : children){
			historys.addLast(children.getLast()!=child);
			child.print(historys, ps);
			historys.removeLast();
		}
	}
}
