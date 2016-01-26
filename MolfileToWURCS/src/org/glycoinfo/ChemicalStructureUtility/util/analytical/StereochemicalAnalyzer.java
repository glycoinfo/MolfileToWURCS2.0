package org.glycoinfo.ChemicalStructureUtility.util.analytical;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.ChemicalGraph;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.Chemical;
import org.glycoinfo.ChemicalStructureUtility.util.HierarchicalDigraph;
import org.glycoinfo.ChemicalStructureUtility.util.HierarchicalDigraphComparator;

/**
 * Class for stereochemical analyze of chemical graph
 * TODO: To make HierarchicalDigraphCreator
 * @author MasaakiMatsubara
 */
public class StereochemicalAnalyzer {

	private ChemicalGraph m_objGraph;

//	private HierarchicalDigraphCreator    m_objCreator    = new HierarchicalDigraphCreator();
	private HierarchicalDigraphComparator m_objComparator = new HierarchicalDigraphComparator();

	private HashMap<Atom, Boolean>       m_mapAtomToOrderIsUnique        = new HashMap<Atom, Boolean>();
	private HashMap<Atom, String>        m_mapAtomToStereo             = new HashMap<Atom, String>();
	private HashMap<Bond, String>        m_mapBondToStereo             = new HashMap<Bond, String>();
	private HashMap<Connection, Boolean> m_mapConnectionToFullSearchHasCompleted  = new HashMap<Connection, Boolean>();
	private HashMap<Connection, Boolean> m_mapConnectionToOrderIsUnique  = new HashMap<Connection, Boolean>();
	private HashMap<Connection, Integer> m_mapConnectionToCIPOrder     = new HashMap<Connection, Integer>();
	private HashMap<Atom, LinkedList<Connection>> m_mapAtomToSortedConnections = new HashMap<Atom, LinkedList<Connection>>();

	/** Get stereo of atom */
	public String getStereo(Atom atom) {
		return this.m_mapAtomToStereo.get(atom);
	}

	/** Get stereo of bond */
	public String getStereo(Bond bond) {
		return this.m_mapBondToStereo.get(bond);
	}

	/** Get CIP order of connection */
	public Integer getCIPOrder(Connection con) {
		return this.m_mapConnectionToCIPOrder.get(con);
	}

	//----------------------------
	// Private method (void)
	//----------------------------
	/** Clear member valiable */
	private void clear() {
		this.m_mapAtomToStereo.clear();
		this.m_mapAtomToOrderIsUnique.clear();
		this.m_mapConnectionToFullSearchHasCompleted.clear();
		this.m_mapConnectionToOrderIsUnique.clear();
		this.m_mapConnectionToCIPOrder.clear();
		this.m_mapBondToStereo.clear();
	}

	/** Initialize member valiable */
	private void initialize() {
		this.clear();
		for ( Atom atom : this.m_objGraph.getAtoms() ) {
			this.m_mapAtomToStereo.put(atom, null);
			this.m_mapAtomToOrderIsUnique.put(atom, null);
			for ( Connection con : atom.getConnections() ) {
				this.m_mapConnectionToFullSearchHasCompleted.put(con, false);
				this.m_mapConnectionToOrderIsUnique.put(con, false);
			}
		}
		for ( Bond bond : this.m_objGraph.getBonds() ) {
			this.m_mapBondToStereo.put(bond, null);
		}
	}

	/**
	 * Execute stereochemcial analysis for the molecule
	 * @param mol
	 */
	public void analyze(ChemicalGraph graph) {
		this.m_objGraph = graph;
		// initialize member valiable
		this.initialize();
//		this.setStereo();

		HashSet<Atom> t_setAnalyzedAtoms = new HashSet<Atom>();
//		HashMap<Atom, Boolean> t_mapAtomToAnalyzed = new HashMap<Atom, Boolean>();
//		for ( Atom atom : graph.getAtoms() ) {
//			t_mapAtomToAnalyzed.put(atom, false);
//		}

		// EZRS check
		this.m_objComparator.setCheckType(false);
//		this.analyzeStereo(t_mapAtomToAnalyzed);
		this.analyzeStereo(t_setAnalyzedAtoms);
		this.setStereoEZRS();

		// rs check, use EZRS results of other element.
		for(Atom atom : this.m_objGraph.getAtoms()){
			String stereo = this.m_mapAtomToStereo.get(atom);
//			t_mapAtomToAnalyzed.put(atom, ( stereo=="R" || stereo=="S" ) );
			if ( stereo != null && ( stereo.equals("R") || stereo.equals("S") ) )
				t_setAnalyzedAtoms.add(atom);
		}
		this.m_objComparator.setCheckType(true);
		this.m_objComparator.setAtomStereos(this.m_mapAtomToStereo);
		this.m_objComparator.setBondStereos(this.m_mapBondToStereo);
//		this.analyzeStereo(t_mapAtomToAnalyzed);
		this.analyzeStereo(t_setAnalyzedAtoms);
		this.setStereors();

	}

	/**
	 * Analyze stereo chemistry and set stero to stereoTmp.
	 */
//	private void analyzeStereo(HashMap<Atom, Boolean> a_mapAtomToAnalyzed){
	private void analyzeStereo(HashSet<Atom> a_setAnalyzedAtoms){

		boolean continueflg = true;
		int depth = 0;
		String t_strContinuedHistry = "";
		while(continueflg){
			continueflg = false;
			depth++;
			String t_strAtoms = "";
			for(Atom atom : this.m_objGraph.getAtoms()){
//				if(atom.connections.tmpflg) continue;
//				if ( a_mapAtomToAnalyzed.get(atom) ) continue;
				if ( a_setAnalyzedAtoms.contains(atom) ) continue;

				continueflg = true;

				// XXX: List continued atoms
				if ( depth > 1 ) {
					if ( !t_strAtoms.equals("") ) t_strAtoms += ",";
					t_strAtoms += atom.getSymbol();
//					t_strAtoms += "("+(this.m_objGraph.getAtoms().indexOf(atom)+1)+")";
					t_strAtoms += "("+(atom.getAtomID())+")";
				}

				// Set full search flag for connections
//				atom.connections.setIsCompletedFullSearch(false);
				for(Connection connection : atom.getConnections()){
					if ( !connection.endAtom().getSymbol().equals("H") && !this.m_objGraph.contains( connection.endAtom() ) ) continue;
					this.m_mapConnectionToFullSearchHasCompleted.put(connection, false);
				}

				// Construct HierarchicalDigraph with "depth"
				HierarchicalDigraph t_oHD = new HierarchicalDigraph(this.m_objGraph, atom, depth, this.m_objComparator);
				// XXX: remove print
				for ( Connection connection : atom.getConnections() ) {
					if ( connection.getBond().getType() != 2 ) continue;
					System.err.println(t_strAtoms);
					t_oHD.print(System.err);
				}
				// Set CIP order
				this.m_mapAtomToOrderIsUnique.put(atom, true);
//				atom.connections.isUniqOrder = true;
				int order = 0;
				boolean pre = true;
				for(HierarchicalDigraph t_oChildHD : t_oHD.getChildren()){
					if(pre||t_oChildHD.isUniqueOrder()) order++;
//					Connection connection = atom.getConnections().getConnect(child.atom);
//					Connection connection = child.getConnectionToParent();
					Connection connection = t_oChildHD.getConnection();

					if(connection!=null){
//						connection.isUniqOrder = child.isUniqOrder;
						this.m_mapConnectionToOrderIsUnique.put(connection, t_oChildHD.isUniqueOrder());
//						connection.isCompletedFullSearch = child.isCompletedFullSearch;
						this.m_mapConnectionToFullSearchHasCompleted.put(connection, t_oChildHD.isCompletedFullSearch());
//						connection.CIPorder = order;
						this.m_mapConnectionToCIPOrder.put(connection, order);
					}

//					if(!child.isUniqueOrder()) atom.connections.isUniqOrder = false;
					if(!t_oChildHD.isUniqueOrder()) this.m_mapAtomToOrderIsUnique.put(atom, false);
					pre = t_oChildHD.isUniqueOrder();
				}
//				atom.connections.sortByCIPorder();
				LinkedList<Connection> t_aConns = new LinkedList<Connection>();
				for ( Connection t_oConn : atom.getConnections() ) {
					if ( !t_oConn.endAtom().getSymbol().equals("H") && !this.m_objGraph.contains( t_oConn.endAtom() ) ) continue;
					t_aConns.add(t_oConn);
				}
				final HashMap<Connection, Integer> t_hashConnectionToCIPOrder = this.m_mapConnectionToCIPOrder;
//				atom.sortConnections( new Comparator<Connection>() {
				Collections.sort( t_aConns, new Comparator<Connection>() {
					public int compare(Connection connection1, Connection connection2) {
						Integer t_iOrder1 = t_hashConnectionToCIPOrder.get(connection1);
						Integer t_iOrder2 = t_hashConnectionToCIPOrder.get(connection2);
//						if ( t_iOrder1 == t_iOrder2 ) return 0;
//						if ( t_iOrder1 != null && t_iOrder2 == null ) return -1;
//						if ( t_iOrder1 == null && t_iOrder2 != null ) return 1;
						return t_iOrder1 - t_iOrder2;
//						return connection1.CIPorder - connection2.CIPorder;
					}
				});

				// 打ち切りチェック
				// maxDepthForHierarchicalDigraphの判定がおかしいのでやり直し
//				if(atom.connections.isUniqOrder){
				// Stop search if the atom is chiral or achiral
				// chiralであることが確定した場合、探索を打ち切る。
				// achiralであることが確定した場合、探索を打ち切る。
				if( this.m_mapAtomToOrderIsUnique.get(atom) || this.jadgeAchiral(t_aConns) ) {
//					atom.connections.tmpflg = true;
					a_setAnalyzedAtoms.add(atom);
					this.m_mapAtomToSortedConnections.put(atom, t_aConns);
				}

				// ここから追加
				// C-N=N=N-Oの場合、中央のNはachiralである。(ダミーのNが2つ付く)
				// この場合、ダミーのN2つは同じものとみなされユニークではなくなる為、atom.connections.isUniqOrder = falseとなってここに入ってくる。
				// しかし、ここではダミー以外の要素のみをチェックしているので、achiralが確定していても上記のチェックを逃れてしまう。
				// その為ここでチェックする。
//				if(hd.isCompletedFullSearch){
				if ( t_oHD.isCompletedFullSearch() ) {
//					atom.connections.tmpflg = true;
					a_setAnalyzedAtoms.add(atom);
					this.m_mapAtomToSortedConnections.put(atom, t_aConns);
				}
				// ここまで


			}
			// XXX:
			if ( ! t_strAtoms.equals("") )
				t_strContinuedHistry += depth +":"+ t_strAtoms + "\n";
		}

		// XXX: remove print
//		System.err.println("Analyzed stereo for "+ this.m_objGraph);
//		System.err.println(t_strContinuedHistry);

	}

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
	private boolean jadgeAchiral(LinkedList<Connection> a_aConns) {
		for(int ii=0; ii<a_aConns.size()-1; ii++){
			Connection connection1 = a_aConns.get(ii);
//			if( connection1.isUniqOrder) continue;
			if ( this.m_mapConnectionToOrderIsUnique.get(connection1) ) continue;
//			if(!connection1.isCompletedFullSearch) continue;
			if ( !this.m_mapConnectionToFullSearchHasCompleted.get(connection1) ) continue;
//			if ( t_aSearchedConnections.contains(connection1) ) continue;
			for ( int jj=ii+1; jj<a_aConns.size(); jj++ ) {
				Connection connection2 = a_aConns.get(jj);
//				if( connection2.isUniqOrder) break;
				if ( this.m_mapConnectionToOrderIsUnique.get(connection2) ) break;
//				if(!connection2.isCompletedFullSearch) continue;
				if ( !this.m_mapConnectionToFullSearchHasCompleted.get(connection2) ) continue;
//				if ( t_aSearchedConnections.contains(connection2) ) continue;
//				atom.connections.tmpflg = true;
				return true;
			}
		}
		return false;
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
			if ( !this.m_mapAtomToOrderIsUnique.get(atom) ) continue;
//			ConnectionList subgraphConnects = this.getConnects(atom);
//			LinkedList<Connection> subgraphConnects = this.getConnects(atom);
			LinkedList<Connection> subgraphConnects = this.m_mapAtomToSortedConnections.get(atom);
			if ( subgraphConnects.size()!=4 ) continue;
			String stereo = Chemical.sp3stereo(subgraphConnects.get(0), subgraphConnects.get(1), subgraphConnects.get(2), subgraphConnects.get(3));
//			atom.stereoTmp = stereo;
			this.m_mapAtomToStereo.put(atom, stereo);
		}
		// Judge EZ
		for ( Bond bond : this.m_objGraph.getBonds() ){
			if ( bond.getType()!=2 ) continue;
			Atom a0 = bond.getAtom1();
			Atom b0 = bond.getAtom2();
//			if(!a0.connections.isUniqOrder) continue;
//			if(!b0.connections.isUniqOrder) continue;
			if ( !this.m_mapAtomToOrderIsUnique.get(a0) ) continue;
			if ( !this.m_mapAtomToOrderIsUnique.get(b0) ) continue;
//			ConnectionList a0connects = this.getConnects(a0);
//			ConnectionList b0connects = this.getConnects(b0);
//			LinkedList<Connection> a0connects = this.getConnects(a0);
//			LinkedList<Connection> b0connects = this.getConnects(b0);
			LinkedList<Connection> a0connects = this.m_mapAtomToSortedConnections.get(a0);
			LinkedList<Connection> b0connects = this.m_mapAtomToSortedConnections.get(b0);
			if ( a0connects.size()<2 || b0connects.size()<2 ) continue;
			Atom a1 = (a0connects.get(0).endAtom() == b0) ? a0connects.get(1).endAtom() : a0connects.get(0).endAtom();
			Atom b1 = (b0connects.get(0).endAtom() == a0) ? b0connects.get(1).endAtom() : b0connects.get(0).endAtom();
			// Ignore if hydrogen is contained like imine
			if ( a1.getSymbol().equals("H") || b1.getSymbol().equals("H") ) continue;
			String stereo = Chemical.sp2stereo(a0, a1, b0, b1);
//			bond.stereoTmp = stereo;
			this.m_mapBondToStereo.put(bond, stereo);
		}
	}

	/** Set stereo to member valiable for rs. */
	private void setStereors() {
		// Using AtomIdentifier
		AtomIdentifier ident = new AtomIdentifier();
		// Judge rs
		for ( Atom atom : this.m_objGraph.getAtoms() ) {
//			if( atom.stereoTmp!=null) continue;
			if ( this.m_mapAtomToStereo.get(atom)!=null ) continue;
			ident.setAtom(atom);
//			if(!atom.hybridOrbital().equals("sp3")) continue;
			if(!ident.getHybridOrbital().equals("sp3")) continue;
//			if(!atom.connections.isUniqOrder) continue;
			if ( !this.m_mapAtomToOrderIsUnique.get(atom) ) continue;
//			ConnectionList subgraphConnects = this.getConnects(atom);
//			LinkedList<Connection> subgraphConnects = this.getConnects(atom);
			LinkedList<Connection> subgraphConnects = this.m_mapAtomToSortedConnections.get(atom);
			if( subgraphConnects.size()!=4) continue;
			String stereo = Chemical.sp3stereo(subgraphConnects.get(0), subgraphConnects.get(1), subgraphConnects.get(2), subgraphConnects.get(3)).toLowerCase();
//			atom.stereoTmp = stereo;
			// TODO: remove print
			System.err.println(stereo);
			this.m_mapAtomToStereo.put(atom, stereo);
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
