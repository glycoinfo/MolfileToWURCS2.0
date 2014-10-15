package chemicalgraph.util.analytical;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.Bond;
import chemicalgraph.ChemicalGraph;
import chemicalgraph.Connection;
import chemicalgraph.util.Chemical;
import chemicalgraph.util.HierarchicalDigraph;
import chemicalgraph.util.HierarchicalDigraphComparator;
import chemicalgraph.util.HierarchicalDigraphCreator;

/**
 * Class for stereochemical analyze of chemical graph
 * TODO: To make HierarchicalDigraphCreator
 * @author MasaakiMatsubara
 */
public class StereochemicalAnalyzer {

	private ChemicalGraph m_objGraph;

	private HierarchicalDigraphCreator    m_objCreator    = new HierarchicalDigraphCreator();
	private HierarchicalDigraphComparator m_objComparator = new HierarchicalDigraphComparator();

	private HashMap<Atom, Boolean>       m_hashAtomIsUniqueOrder        = new HashMap<Atom, Boolean>();
	private HashMap<Atom, String>        m_hashAtomToStereo             = new HashMap<Atom, String>();
	private HashMap<Bond, String>        m_hashBondToStereo             = new HashMap<Bond, String>();
	private HashMap<Connection, Boolean> m_hashConnectionHasFullSearch  = new HashMap<Connection, Boolean>();
	private HashMap<Connection, Boolean> m_hashConnectionIsUniqueOrder  = new HashMap<Connection, Boolean>();
	private HashMap<Connection, Integer> m_hashConnectionToCIPOrder     = new HashMap<Connection, Integer>();

	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * Execute stereochemcial analysis for the molecule
	 * @param mol
	 */
	public void analyze(ChemicalGraph graph) {
		this.m_objGraph = graph;
		// initialize member valiable
		this.initialize();
//		this.setStereo();

		HashMap<Atom, Boolean> t_aAnalyzedAtoms = new HashMap<Atom, Boolean>();
		for ( Atom atom : graph.getAtoms() ) {
			t_aAnalyzedAtoms.put(atom, false);
		}

		// EZRS check
		this.m_objComparator.setCheckType(false);
		this.analyzeStereo(t_aAnalyzedAtoms);
		this.setStereoEZRS();

		// rs check, use EZRS results of other element.
		for(Atom atom : this.m_objGraph.getAtoms()){
			String stereo = this.m_hashAtomToStereo.get(atom);
			t_aAnalyzedAtoms.put(atom, ( stereo=="R" || stereo=="S" ) );
		}
		this.m_objComparator.setCheckType(true);
		this.m_objComparator.setAtomStereos(this.m_hashAtomToStereo);
		this.m_objComparator.setBondStereos(this.m_hashBondToStereo);
		this.analyzeStereo(t_aAnalyzedAtoms);
		this.setStereors();
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	/** Get stereo of atom */
	public String getStereo(Atom atom) {
		return this.m_hashAtomToStereo.get(atom);
	}

	/** Get bond to stereo */
	public String getStereo(Bond bond) {
		return this.m_hashBondToStereo.get(bond);
	}

	//----------------------------
	// Private method (void)
	//----------------------------
	/** Clear member valiable */
	private void clear() {
		this.m_hashAtomToStereo.clear();
		this.m_hashAtomIsUniqueOrder.clear();
		this.m_hashConnectionHasFullSearch.clear();
		this.m_hashConnectionIsUniqueOrder.clear();
		this.m_hashConnectionToCIPOrder.clear();
		this.m_hashBondToStereo.clear();
	}

	/** Initialize member valiable */
	private void initialize() {
		this.clear();
		for ( Atom atom : this.m_objGraph.getAtoms() ) {
			this.m_hashAtomToStereo.put(atom, null);
			this.m_hashAtomIsUniqueOrder.put(atom, null);
			for ( Connection con : atom.getConnections() ) {
				this.m_hashConnectionHasFullSearch.put(con, false);
				this.m_hashConnectionIsUniqueOrder.put(con, false);
			}
		}
		for ( Bond bond : this.m_objGraph.getBonds() ) {
			this.m_hashBondToStereo.put(bond, null);
		}
	}

	/**
	 * Analyze stereo chemistry and set stero to stereoTmp.
	 */
	private void analyzeStereo(HashMap<Atom, Boolean> analyzedAtoms){
		// Set type for HierarchicalDigraph comparator

		boolean continueflg = true;
		int depth = 0;
		while(continueflg){
			continueflg = false;
			depth++;
			for(Atom atom : this.m_objGraph.getAtoms()){
//				if(atom.connections.tmpflg) continue;
				if ( analyzedAtoms.get(atom) ) continue;

				continueflg = true;
				// Set full search flag for connections
//				atom.connections.setIsCompletedFullSearch(false);
				for(Connection connection : atom.getConnections()){
					this.m_hashConnectionHasFullSearch.put(connection, false);
				}

				// Construct HierarchicalDigraph with "depth"
				HierarchicalDigraph hd = new HierarchicalDigraph(this.m_objGraph, atom, depth, this.m_objComparator);
				// Set CIP order
				this.m_hashAtomIsUniqueOrder.put(atom, true);
//				atom.connections.isUniqOrder = true;
				int order = 0;
				boolean pre = true;
				for(HierarchicalDigraph child : hd.getChildren()){
					if(pre||child.isUniqueOrder()) order++;
//					Connection connection = atom.getConnections().getConnect(child.atom);
//					Connection connection = child.getConnectionToParent();
					Connection connection = child.getConnection();

					if(connection!=null){
//						connection.isUniqOrder = child.isUniqOrder;
						this.m_hashConnectionIsUniqueOrder.put(connection, child.isUniqueOrder());
//						connection.isCompletedFullSearch = child.isCompletedFullSearch;
						this.m_hashConnectionHasFullSearch.put(connection, child.isCompletedFullSearch());
//						connection.CIPorder = order;
						this.m_hashConnectionToCIPOrder.put(connection, order);
					}

//					if(!child.isUniqueOrder()) atom.connections.isUniqOrder = false;
					if(!child.isUniqueOrder()) this.m_hashAtomIsUniqueOrder.put(atom, false);
					pre = child.isUniqueOrder();
				}
//				atom.connections.sortByCIPorder();
				final HashMap<Connection, Integer> t_hashConnectionToCIPOrder = this.m_hashConnectionToCIPOrder;
				atom.sortConnections( new Comparator<Connection>() {
					public int compare(Connection connection1, Connection connection2) {
						return t_hashConnectionToCIPOrder.get(connection1) - t_hashConnectionToCIPOrder.get(connection2);
//						return connection1.CIPorder - connection2.CIPorder;
					}
				});

				// 打ち切りチェック
				// maxDepthForHierarchicalDigraphの判定がおかしいのでやり直し
//				if(atom.connections.isUniqOrder){
				if( this.m_hashAtomIsUniqueOrder.get(atom) ){
					// Stop search if the atom is chiral
					// chiralであることが確定した場合、探索を打ち切る。
//					atom.connections.tmpflg = true;
					analyzedAtoms.put(atom, true);
					continue;
				}else{
					// achiralであることが確定した場合、探索を打ち切る。
					// 順位が等しい結合のペアが存在し、かつそのペアが全探索が完了している場合
					// atom.connectionsは優先順位でソートされている
					// Stop search if the atom is achiral.
					// If there are bond pair with same order and the pair completed full search,
					// atom.connections has been sorted by the priority.
					//
					// Check the connection which is not with "isUniqueOrder "
					// isUniqOrder==falseとなるconnectionは順位のつかなかったconnectionを意味しており、順位が等しい別のconnectionが存在する
					// Listの中で連続してisUniqOrder==falseとなっているconnectionが順位が等しいconnectionとなっている
					// 順位が等しいconnectionペアがどちらも全探索が完了している場合にachiralである事が確定する。
					for(int ii=0; ii<atom.getConnections().size()-1; ii++){
						Connection connection1 = atom.getConnections().get(ii);
//						if( connection1.isUniqOrder) continue;
						if ( this.m_hashConnectionIsUniqueOrder.get(connection1) ) continue;
//						if(!connection1.isCompletedFullSearch) continue;
						if ( !this.m_hashConnectionHasFullSearch.get(connection1) ) continue;
//						if ( t_aSearchedConnections.contains(connection1) ) continue;
						for ( int jj=ii+1; jj<atom.getConnections().size(); jj++ ) {
							Connection connection2 = atom.getConnections().get(jj);
//							if( connection2.isUniqOrder) break;
							if ( this.m_hashConnectionIsUniqueOrder.get(connection2) ) break;
//							if(!connection2.isCompletedFullSearch) continue;
							if ( !this.m_hashConnectionHasFullSearch.get(connection2) ) continue;
//							if ( t_aSearchedConnections.contains(connection2) ) continue;
//							atom.connections.tmpflg = true;
							analyzedAtoms.put(atom, true);
						}
					}
				}

				// ここから追加
				// C-N=N=N-Oの場合、中央のNはachiralである。(ダミーのNが2つ付く)
				// この場合、ダミーのN2つは同じものとみなされユニークではなくなる為、atom.connections.isUniqOrder = falseとなってここに入ってくる。
				// しかし、ここではダミー以外の要素のみをチェックしているので、achiralが確定していても上記のチェックを逃れてしまう。
				// その為ここでチェックする。
//				if(hd.isCompletedFullSearch){
				if ( hd.isCompletedFullSearch() ) {
//					atom.connections.tmpflg = true;
					analyzedAtoms.put(atom, true);
				}
				// ここまで
			}
		}
	}

	/** Set stereo to member valiable for EZRS. */
	private void setStereoEZRS() {
		// Using AtomIdentifier
		AtomIdentifier ident = new AtomIdentifier();
		// Judge RS
		for ( Atom atom : this.m_objGraph.getAtoms() ) {
			ident.setAtom(atom);
//			if(!atom.hybridOrbital().equals("sp3")) continue;
			if ( !ident.getHybridOrbital().equals("sp3") ) continue;
//			if(!atom.connections.isUniqOrder) continue;
			if ( !this.m_hashAtomIsUniqueOrder.get(atom) ) continue;
//			ConnectionList subgraphConnects = this.getConnects(atom);
			LinkedList<Connection> subgraphConnects = this.getConnects(atom);
			if ( subgraphConnects.size()!=4 ) continue;
			String stereo = Chemical.sp3stereo(subgraphConnects.get(0), subgraphConnects.get(1), subgraphConnects.get(2), subgraphConnects.get(3));
//			atom.stereoTmp = stereo;
			this.m_hashAtomToStereo.put(atom, stereo);
		}
		// Judge EZ
		for ( Bond bond : this.m_objGraph.getBonds() ){
			if ( bond.getType()!=2 ) continue;
			Atom a0 = bond.getAtom1();
			Atom b0 = bond.getAtom2();
//			if(!a0.connections.isUniqOrder) continue;
//			if(!b0.connections.isUniqOrder) continue;
			if ( !this.m_hashAtomIsUniqueOrder.get(a0) ) continue;
			if ( !this.m_hashAtomIsUniqueOrder.get(b0) ) continue;
//			ConnectionList a0connects = this.getConnects(a0);
//			ConnectionList b0connects = this.getConnects(b0);
			LinkedList<Connection> a0connects = this.getConnects(a0);
			LinkedList<Connection> b0connects = this.getConnects(b0);
			if ( a0connects.size()<2 ) continue;
			if ( b0connects.size()<2 ) continue;
			Atom a1 = (a0connects.get(0).endAtom() == b0) ? a0connects.get(1).endAtom() : a0connects.get(0).endAtom();
			Atom b1 = (b0connects.get(0).endAtom() == a0) ? b0connects.get(1).endAtom() : b0connects.get(0).endAtom();
			String stereo = Chemical.sp2stereo(a0, a1, b0, b1);
//			bond.stereoTmp = stereo;
			this.m_hashBondToStereo.put(bond, stereo);
		}
	}

	/** Set stereo to member valiable for rs. */
	private void setStereors() {
		// Using AtomIdentifier
		AtomIdentifier ident = new AtomIdentifier();
		// Judge rs
		for ( Atom atom : this.m_objGraph.getAtoms() ) {
//			if( atom.stereoTmp!=null) continue;
			if ( this.m_hashAtomToStereo.get(atom)!=null ) continue;
			ident.setAtom(atom);
//			if(!atom.hybridOrbital().equals("sp3")) continue;
			if(!ident.getHybridOrbital().equals("sp3")) continue;
//			if(!atom.connections.isUniqOrder) continue;
			if ( !this.m_hashAtomIsUniqueOrder.get(atom) ) continue;
//			ConnectionList subgraphConnects = this.getConnects(atom);
			LinkedList<Connection> subgraphConnects = this.getConnects(atom);
			if( subgraphConnects.size()!=4) continue;
			String stereo = Chemical.sp3stereo(subgraphConnects.get(0), subgraphConnects.get(1), subgraphConnects.get(2), subgraphConnects.get(3)).toLowerCase();
//			atom.stereoTmp = stereo;
			System.err.println(stereo);
			this.m_hashAtomToStereo.put(atom, stereo);
		}
	}

	//----------------------------
	// Private method (non void)
	//----------------------------
	/**
	 * Return the connections of input atom which included in this chemical graph.
	 * @param atom
	 * @return the connections of input atom which included in this chemical graph.
	 */
	private LinkedList<Connection> getConnects(Atom atom){
		LinkedList<Connection> connections = new LinkedList<Connection>();
		for(Connection con : atom.getConnections()){
			if ( con.endAtom().getSymbol().equals("H")
			  || ( this.m_objGraph.contains(con.getBond()) && this.m_objGraph.contains(con.endAtom()) ) ) {
				connections.add(con);
			}
		}
		return connections;
	}
}
