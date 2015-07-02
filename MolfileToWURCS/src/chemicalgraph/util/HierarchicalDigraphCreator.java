package chemicalgraph.util;

import java.util.HashSet;
import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.ChemicalGraph;
import chemicalgraph.Connection;

public class HierarchicalDigraphCreator {

	/** Molecule for search */
	private ChemicalGraph m_objTargetGraph = null;
	/** Comparator for HierarchicalDigraph */
	private HierarchicalDigraphComparator m_objComparator = new HierarchicalDigraphComparator();
	/** Current searching HierarchicalDigraph */
	private HierarchicalDigraph m_objCurrentDigraph;
	/** Full search flag */
	private boolean m_objbFlagOfCompletedFullSearch;

	/** Searched atoms */
	private LinkedList<Atom> m_aAncestors     = new LinkedList<Atom>();
	private HashSet<Atom>    m_aAromaticAtoms = new HashSet<Atom>();

	public void clear() {
		this.m_aAncestors.clear();
		this.m_aAromaticAtoms.clear();
	}

	public void setGraph ( ChemicalGraph graph ) {
		this.m_objTargetGraph = graph;
	}

	public void setAromaticAtoms( HashSet<Atom> atoms ) {
		this.m_aAromaticAtoms.addAll(atoms);
	}

	public HierarchicalDigraphComparator getComparator() {
		return this.m_objComparator;
	}

	public HierarchicalDigraph create(Atom startAtom, int depth) {
//		this.depth = depth;
		HierarchicalDigraph graph = new HierarchicalDigraph(null, startAtom, Chemical.getAtomicNumber(startAtom.getSymbol()));

		// Search aromatic atoms
/*		Cyclization cyclic = new Cyclization();
		for ( Atom a : this.m_objTargetGraph.getAtoms() ) {
			cyclic.clear();
			if ( cyclic.aromatize(a) ) this.m_aAromaticAtoms.addAll(cyclic);
		}
*/
		this.depthSearch(graph, depth);
		return graph;
	}

	/**
	 * Construct HierarchicalDigraph using depth-first search
	 * @param atom Atom for search
	 * @param averageAtomicNumber Average atomic number, for conjugate system
	 * @param depth 探索する深さに制限を付ける場合に利用
	 */
	private boolean depthSearch(final HierarchicalDigraph graph, final int depth){
		Atom atom = graph.getAtom();
//		if( atom!=null && !atom.symbol.equals("H") && !targetgraph.contains(atom) ) return;
		if( atom == null ) return true;
		if( atom!=null && !atom.getSymbol().equals("H") && !this.m_objTargetGraph.contains(atom) ) return true;
		if(this.m_aAncestors.contains(atom)) return true;
		if(depth == 0){
//			currentgraph.setCompletedFullSearch(true);
			return true;
		}

		// Add Children
		int num = 0;
		int sumAtomicNumber = 0;
		this.m_aAncestors.add(atom);
		for(Connection connection : graph.getAtom().getConnections()){
			Atom conatom = connection.endAtom();
			// Skip if conatom is hydrogen or out of target graph
			if( !conatom.getSymbol().equals("H") && !this.m_objTargetGraph.contains(conatom) ) continue;
//			this.children.add(new HierarchicalDigraph(targetgraph, conatom, depth-1, (double)Chemical.getAtomicNumber(conatom.getSymbol()), ancestors, EZRScheck));
			// Add child sub graph for connect atom
			graph.addChild( new HierarchicalDigraph( graph, conatom, (double)Chemical.getAtomicNumber(conatom.getSymbol()) ) );
			// For conjugate or multiple bond, it is consider that same atom is duplecated.
//			if(this.atom.isAromatic && connection.atom.isAromatic){
			if( this.m_aAromaticAtoms.contains(atom) && this.m_aAromaticAtoms.contains(conatom) ){
				num++;
				sumAtomicNumber+=(double)Chemical.getAtomicNumber(conatom.getSymbol());
			}else if(connection.getBond().getType()==2 || connection.getBond().getType()==3){
				for(int ii=connection.getBond().getType(); ii>1; ii--){
					graph.addChild( new HierarchicalDigraph( graph, null, (double)Chemical.getAtomicNumber(conatom.getSymbol()) ) );
				}
			}
		}
		// Add duplecated atom as child for conjugate system
		if(num!=0){
			graph.addChild( new HierarchicalDigraph( graph, null, (double)sumAtomicNumber/(double)num ) );
		}
		this.m_aAncestors.removeLast();

		// Sort Children
		graph.sortChildren(this.m_objComparator);

		// Set false to "isUniqOrder" of child graph which CIP order is not unique
		// CIPorderがユニークにならない要素にfalseを立てる
		for ( HierarchicalDigraph child : graph.getChildren() ){
			child.setUniqOrder(true);
		}
		int childrenNum = graph.getChildren().size();

		for(int ii=0; ii<childrenNum-1; ii++){
			HierarchicalDigraph child1 = graph.getChildren().get(ii);
			HierarchicalDigraph child2 = graph.getChildren().get(ii+1);
//			if(tree1.compareTo(tree2, EZRScheck)!=0) continue;
			if( this.m_objComparator.compare(child1, child2)!=0 ) continue;
			child1.isUniqOrder = false;
			child2.isUniqOrder = false;
		}

		// depth search for children
		boolean isCompletedFullSearch = true;
		for ( HierarchicalDigraph child : graph.getChildren() ) {
			if ( this.depthSearch(child, depth-1) ) continue;
			isCompletedFullSearch = false;
		}
		return isCompletedFullSearch;

/*
		Collections.sort(this.children, new Comparator<HierarchicalDigraph>() {
			public int compare(HierarchicalDigraph tree1, HierarchicalDigraph tree2) {
				return tree1.compareTo(tree2, EZRScheck);
			}
		});
*/
	}

}
