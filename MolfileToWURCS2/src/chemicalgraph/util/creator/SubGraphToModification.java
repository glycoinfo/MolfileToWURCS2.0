package chemicalgraph.util.creator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import wurcs.Modification;
import chemicalgraph.Atom;
import chemicalgraph.Bond;
import chemicalgraph.Connection;
import chemicalgraph.SubGraph;
import chemicalgraph.util.Chemical;

public class SubGraphToModification {
	//----------------------------
	// Member variable
	//----------------------------

	private HashSet<Atom> m_aAromaticAtoms = new HashSet<Atom>();
	private HashSet<Atom> m_aBackboneAtoms = new HashSet<Atom>();
	private HashMap<Atom, LinkedList<Atom>> m_hashAtomToBackbone = new HashMap<Atom, LinkedList<Atom>>();

	private SubGraph m_objModificationGraph;
	private LinkedList<Atom> m_aBackboneAtomsInModification = new LinkedList<Atom>();
	/** List of backbones which connect with this modification. */
//	public BackboneList connectedBackbones;
//	public LinkedList<>
	/** List of backbone atoms which connect with this modification. (super.atoms = atomsOfBackbone + atomsOfModification) */
//	public AtomList atomsOfBackbone;
	/** List of atoms of modification. (super.atoms = atomsOfBackbone + atomsOfModification) */
//	public AtomList atomsOfModification;
	/** List of connections from a backbone to this modification */
//	public ConnectionList connectionsFromBackboneToModification;
	/** 修飾文字列表示を目的として修飾の原子を探索した結果 */
//	public PathList paths;
	private Path path;

	//----------------------------
	// Constructor
	//----------------------------
	public SubGraphToModification(HashSet<Atom> aromaticAtoms, LinkedList<LinkedList<Atom>> backboneChains) {
		this.m_aAromaticAtoms.addAll(aromaticAtoms);
		for ( LinkedList<Atom> chain : backboneChains ) {
			this.m_aBackboneAtoms.addAll(chain);
			for ( Atom atom : chain ) {
				this.m_hashAtomToBackbone.put(atom, chain);
			}
		}
	}

	public Modification convert(SubGraph graph) {
		if(this.path != null) this.path.clear();

		this.path = this.findCanonicalPaths(graph);
		String ALIN = this.makeALINCode(graph, this.path);
		System.err.println( ALIN );

		Modification modification = new Modification(ALIN);
		return modification;
	}

	//----------------------------
	// Public method (void)
	//----------------------------
	public Path findCanonicalPaths(final SubGraph graph) {
		// EC番号を付加
		// Set initial EC number
		graph.updateECnumber(null, null);
		// 初期EC番号を保存
		// Store initial EC numbers
		final HashMap<Atom, Integer> initialECNumber = graph.getAtomToECNumber();
//		for ( Atom atom : graph.getAtoms() ) {
//			atom.initialECnumber = atom.subgraphECnumber;
//		}

		// 探索開始ノードのソート
		// Sort atoms to get start node
		final HashSet<Atom> backboneAtoms = this.m_aBackboneAtoms;
		graph.sortAtoms( new Comparator<Atom>() {
			public int compare(Atom atom1, Atom atom2) {
				// １．主鎖炭素を優先
//				if( atom1.isBackbone() && !atom2.isBackbone()) return -1;
//				if(!atom1.isBackbone() &&  atom2.isBackbone()) return 1;
				if (  backboneAtoms.contains(atom1) && !backboneAtoms.contains(atom2) ) return -1;
				if ( !backboneAtoms.contains(atom1) &&  backboneAtoms.contains(atom2) ) return 1;
				// ２．EC番号が小さい修飾原子を優先
//				if( atom1.subgraphECnumber != atom2.subgraphECnumber ) return atom1.subgraphECnumber - atom2.subgraphECnumber;
				// 2. Prioritize smoller EC number
				if ( initialECNumber.get(atom1) != initialECNumber.get(atom2) )
					return initialECNumber.get(atom1) - initialECNumber.get(atom2);
				// ３．原子番号が小さい修飾原子を優先
				// 3. Prioritize smoller atomic number
//				if( atom1.atomicNumber()   != atom2.atomicNumber()   ) return atom1.atomicNumber()   - atom2.atomicNumber();
				if ( Chemical.getAtomicNumber(atom1.getSymbol()) != Chemical.getAtomicNumber(atom2.getSymbol()) )
					return Chemical.getAtomicNumber(atom1.getSymbol()) - Chemical.getAtomicNumber(atom2.getSymbol());
				// ４．立体等に基づいて優先順位を追加したい場合はここに比較関数を追加

				return 0;
			}
		});
		Atom startAtom = graph.getAtoms().getFirst();

		final HashSet<Atom> aromaticAtoms = this.m_aAromaticAtoms;

		// Pathの構築
		// Constract paths
		LinkedList<Connection> connects = new LinkedList<Connection>();
		Path path = new Path();
		path.add(new PathSection( startAtom ));
		while(true){
			final Atom tailAtom = path.getLast().getNext().getAtom();

			// 隣接Connectを抽出
			// Select connections
			for ( Connection con : tailAtom.getConnections() ) {
				Bond bond = con.getBond();
				if ( !graph.contains(bond) ) continue;    // not consider
				if (  path.contains(bond) ) continue;    // has searched
				if (  connects.contains(con) ) continue;  // has stored
				connects.add(con);
			}

			// 隣接要素がなくなったら終了
			// Finish search for no connects
			if(connects.size()==0) break;

			// 接続要素の2原子が共に探索済みの場合、start()が末端に近いConnectを採用する。
			// Take near teminal connection if connecting two atoms has searched
			for ( Connection con : connects ) {
				if ( !graph.contains( con.endAtom() ) ) continue;
				if ( path.indexOf( con.startAtom() ) < path.indexOf( con.endAtom() ) ){
					int index = connects.indexOf(con);
					for ( Connection connect2 : con.endAtom().getConnections() ){
						if ( connect2.getBond().equals( con.getBond() ) ){
							connects.set(index, connect2);
							break;
						}
					}
				}
			}

			// EC番号再計算
			// Recalculation EC numbers
			graph.updateECnumber(path.bonds(), path.atoms());
			final HashMap<Atom, Integer> subgraphECNumber = graph.getAtomToECNumber();

			// 隣接Connectをソート
			// Sort vicinal connections
			final LinkedList<Atom> tmpPathAtom = path.atoms();
			Collections.sort(connects, new Comparator<Connection>() {
				public int compare(Connection con1, Connection con2) {
					Atom end1 = con1.endAtom();
					Atom end2 = con2.endAtom();
					Atom start1 = con1.startAtom();
					Atom start2 = con1.startAtom();

					// １．繋がっている芳香環はまとめて出したい
					// 1. Get together connecting aromatic atoms
					if ( aromaticAtoms.contains(tailAtom) ){
						if(  aromaticAtoms.contains(end1)   && !aromaticAtoms.contains(end2)   ) return -1;
						if( !aromaticAtoms.contains(end1)   &&  aromaticAtoms.contains(end2)   ) return 1;
						if(  aromaticAtoms.contains(start1) && !aromaticAtoms.contains(start2) ) return -1;
						if( !aromaticAtoms.contains(start1) &&  aromaticAtoms.contains(start2) ) return 1;
					}

					// ２．後半に探索した修飾原子から伸びている結合を優先
					// 2. Prioritize connection which connected the latter searched path
					if(tmpPathAtom.indexOf(start1) != tmpPathAtom.indexOf(start2))
						return tmpPathAtom.indexOf(start2) - tmpPathAtom.indexOf(start1);

					// ３．主鎖炭素に結合している修飾原子を優先
					// 3. Prioritize connecting backbone
//					int backboneNum1 = end1.connections.backbones().size();
//					int backboneNum2 = end2.connections.backbones().size();
					int nBackbone1 = 0;
					int nBackbone2 = 0;
					for ( Connection con : end1.getConnections() ) {
						if ( backboneAtoms.contains(con.endAtom()) ) nBackbone1++;
					}
					for ( Connection con : end2.getConnections() ) {
						if ( backboneAtoms.contains(con.endAtom()) ) nBackbone2++;
					}
					if(nBackbone1!=0 && nBackbone2==0) return -1;
					if(nBackbone1==0 && nBackbone2!=0) return 1;

					// ４．EC番号が大きい修飾原子を優先(未探索部分の中心に向かっていく)
					// 4. Prioritize large EC number (toword center of non-search region)
					if( subgraphECNumber.get(end1) != subgraphECNumber.get(end2) )
						return subgraphECNumber.get(end2) - subgraphECNumber.get(end1);

					// ５．初期EC番号が大きい修飾原子を優先(初期構造の中心に向かっていく)
					// 5. Prioritize large initial EC number (toword center of all region)
					if( initialECNumber.get(end1) != initialECNumber.get(end2) )
						return initialECNumber.get(end2) - initialECNumber.get(end1);

					// ６．原子番号が小さい修飾原子を優先
					// 6. Prioritize smaller atomic number
//					if( connect1.endAtom().atomicNumber()   != connect2.end().atomicNumber()  ) return connect1.end().atomicNumber()  - connect2.end().atomicNumber();
					if ( Chemical.getAtomicNumber(end1.getSymbol()) != Chemical.getAtomicNumber(end2.getSymbol()) )
						return Chemical.getAtomicNumber(end1.getSymbol()) - Chemical.getAtomicNumber(end2.getSymbol());

					// ７．BondTypeが少ない方を優先
					// 7. Prioritize lower number of bond type (bond order)
					if(con1.getBond().getType() != con2.getBond().getType())
						return con1.getBond().getType() - con2.getBond().getType();

					// ８．CIP順位が優位な方を優先 (StereoModificationを計算する処理がStereoMoleculeの後にあるので上書きされている。)
					// 8. Prioritize prior CIP order
//					if(connect1.CIPorder != connect2.CIPorder) return connect1.CIPorder - connect2.CIPorder;
					if( graph.getCIPOrder(con1) != graph.getCIPOrder(con2) )
						return graph.getCIPOrder(con1) - graph.getCIPOrder(con2);

					return 0;
				}
			});

			// もっともスコアの高い隣接要素を追加
			// Make path section of the most high score connection and add to path
			Connection newConnect = connects.removeFirst();
			PathSection start = path.get(newConnect.startAtom());
			PathSection end   = path.get(newConnect.endAtom());
			path.add( new PathSection( start, end, newConnect ) );
		}
		return path;
	}

	public String makeALINCode(final SubGraph graph, final Path path) {
		String ALIN = "";
		boolean inAromatic = false;
		for(PathSection section : path){
			// For aromatic
			boolean isAromatic = this.m_aAromaticAtoms.contains(section.getAtom());
//			if(aromatic==false &&  section.pathEnd.atom.isAromatic) ALIN += "(";
//			if(aromatic==true  && !path.pathEnd.atom.isAromatic) ALIN += ")";
			if( !inAromatic &&  isAromatic ) ALIN += "(";
			if(  inAromatic && !isAromatic ) ALIN += ")";
			inAromatic = isAromatic;

			// 分岐開始
			// For starting brach
			if ( section.getLast()!=null && path.indexOf(section.getLast())!=path.indexOf(section)-1 ) {
				ALIN += "/" + (path.indexOf(section.getLast()) + 1);
			}

			// 結合表示
			// For bond
			Bond bond = section.getBond();
			if ( bond != null ) {
				Boolean lastIsAromatic = this.m_aAromaticAtoms.contains( section.getLast().getAtom() );
				Boolean nextIsAromatic = this.m_aAromaticAtoms.contains( section.getNext().getAtom() );
				if ( !(lastIsAromatic&&nextIsAromatic) ) {
					if ( bond.getType() == 2) ALIN += "=";
					if ( bond.getType() == 3) ALIN += "#";
				}
				if ( graph.getStereo(bond)!=null ) ALIN += "^" + graph.getStereo(bond);
			}

			// For atom
			Atom atom = section.getAtom();
			if ( atom==null ) {
				ALIN += "$" + (path.indexOf(section.getNext()) + 1);
				continue;
			}
			ALIN += ( this.m_aBackboneAtoms.contains( section.getAtom() ) )? "*" : atom.getSymbol();
			if ( graph.getStereo(atom)!=null ) ALIN += "^" + graph.getStereo(atom);
		}
		// For last aromatic atom
		if ( this.m_aAromaticAtoms.contains( path.getLast().getNext().getAtom() ) ) ALIN += ")";

		return ALIN;
	}

	public LinkedList<Atom> getBackboneAtoms(){
		LinkedList<Atom> aBackboneAtoms = new LinkedList<Atom>();
		LinkedList<Atom> atoms = this.path.atoms();
		for(Atom atom : atoms){
			if( !this.m_aBackboneAtoms.contains(atom) ) continue;
			if( aBackboneAtoms.contains(atom)) continue;
			aBackboneAtoms.add(atom);
		}
		return aBackboneAtoms;
	}

	/*
	public void findConnectedBackbones(){
		this.connectedBackbones = new BackboneList();
		AtomList atoms = this.paths.atoms();
		for(Atom atom : atoms){
			if(!atom.isBackbone()) continue;
			if(this.connectedBackbones.contains(atom.backbone)) continue;
			this.connectedBackbones.add(atom.backbone);
		}
	}

	public void findAtomsOfModification() {
		this.atomsOfModification = new AtomList();
		AtomList atoms = this.paths.atoms();
		for(Atom atom : atoms){
			if(atom.isBackbone()) continue;
			if(this.atomsOfModification.contains(atom)) continue;
			this.atomsOfModification.add(atom);
		}
	}
	*/

	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * Compare to other modification
	 * @param target
	 * @param backbones
	 * @return negative number if this have precedence over target. positive number if target have precedence over this. 0 otherwise.
	 */
	public int compareTo(final Modification target, final BackboneList backbones){
		// 主鎖との結合情報を各修飾に持たせている。
		// 結合情報を複数持つ修飾の場合、本関数が実行される前に結合情報はソート済みである。
		//////////////////////////
		// 主鎖情報を用いたソート
		//////////////////////////
		// １．接続しているユニークな主鎖の数が少ない修飾を優先(同じ主鎖に複数接続している場合は1個としてカウント。)
		int uniqBackboneNum1 = this.connectedBackbones.size();
		int uniqBackboneNum2 = target.connectedBackbones.size();
		if(uniqBackboneNum1!=uniqBackboneNum2) return uniqBackboneNum1 - uniqBackboneNum2;

		// ２．WURCS上で先頭に近い主鎖に接続している修飾を優先
		for(int ii=0; ii<uniqBackboneNum1; ii++){
			int indexOfBackbone1 = backbones.indexOf(this.connectedBackbones.get(ii));
			int indexOfBackbone2 = backbones.indexOf(target.connectedBackbones.get(ii));
			if(indexOfBackbone1!=indexOfBackbone2) return indexOfBackbone1-indexOfBackbone2;
		}

		// ３．主鎖との接続が多い修飾を優先
		int connectNum1 = this.atomsOfBackbone.size();
		int connectNum2 = target.atomsOfBackbone.size();
		if(connectNum1!=connectNum2) return connectNum2 - connectNum1;

		// ４．先頭の結合情報から、上位の主鎖炭素に接続している修飾を優先
		int minNum = Math.min(this.atomsOfBackbone.size(), target.atomsOfBackbone.size());
		for(int ii=0; ii<minNum; ii++){
			int indexOfAtom1 = this.atomsOfBackbone.get(ii).backbone.indexOf(this.atomsOfBackbone.get(ii));
			int indexOfAtom2 = target.atomsOfBackbone.get(ii).backbone.indexOf(target.atomsOfBackbone.get(ii));
			if(indexOfAtom1!=indexOfAtom2) return indexOfAtom1-indexOfAtom2;
		}

		// ５．先頭の結合情報から、立体情報（0, 1, 2, 3, e, z, x）を文字列ソートした順序
		minNum = Math.min(this.connectionsFromBackboneToModification.size(), target.connectionsFromBackboneToModification.size());
		for(int ii=0; ii<minNum; ii++){
			String stereo1 = this.connectionsFromBackboneToModification.get(ii).stereoForWURCS;
			String stereo2 = target.connectionsFromBackboneToModification.get(ii).stereoForWURCS;
			if(!stereo1.equals(stereo2)) return stereo1.compareTo(stereo2);
		}

		////////////////////
		// 修飾情報を用いたソート
		////////////////////
		// ６．修飾に含まれる原子数が少ない方を優先
		if(this.atoms.size() != target.atoms.size()) return this.atoms.size() - target.atoms.size();

		// ７．修飾に含まれる結合数が少ない方を優先
		if(this.bonds.size() != target.bonds.size()) return this.bonds.size() - target.bonds.size();

		// ８．ALIN情報を用いたソート
		PathList paths1 = this.paths;
		PathList paths2 = target.paths;
		minNum = Math.min(paths1.size(), paths2.size());
		for(int ii=0; ii<minNum; ii++){
			PathSection path1 = paths1.get(ii);
			PathSection path2 = paths2.get(ii);

			//////////////////////////////
			// Graphの分枝構造に着目したソート
			// ８－１．開始点がPathの後方である方を優位(直鎖構造を優位とする)
			if( paths1.indexOf(path1.pathStart) != paths2.indexOf(path2.pathStart) ){
				return paths2.indexOf(path2.pathStart) - paths1.indexOf(path1.pathStart);
			}

			// 	８－２．到着点がPathの後方である方を優位(直鎖構造を優位とする)
			if( paths1.indexOf(path1.pathEnd) != paths2.indexOf(path2.pathEnd) ){
				return paths2.indexOf(path2.pathEnd) - paths1.indexOf(path1.pathEnd);
			}

			//////////////////////////////
			// ノード情報に着目したソート
			// ８－３．主鎖炭素を優先
			if( path1.pathEnd.atom.isBackbone() && !path2.pathEnd.atom.isBackbone()) return -1;
			if(!path1.pathEnd.atom.isBackbone() &&  path2.pathEnd.atom.isBackbone()) return 1;

			// ８－４．原子番号の小さなものを優先
			if( path1.pathEnd.atom.atomicNumber() != path2.pathEnd.atom.atomicNumber()) return path1.pathEnd.atom.atomicNumber() - path2.pathEnd.atom.atomicNumber();

			// ８－５．修飾を対象としたRS判定を利用したソート（未実装）

			//////////////////////////////
			// エッジ情報に着目したソート※先頭のpathはbondが空
			if( path1.bond!=null && path2.bond!=null ){
				// ８－４．結合比較
				if(path1.bond.type != path2.bond.type) return path1.bond.type - path2.bond.type;

				// ８－５．修飾を対象としたEZ判定を利用したソート（未実装）
			}
		}
		return 0;
	}

	/**
	 * 修飾に含まれる原子が酸素原子のみで、修飾が結合している主鎖炭素の他の修飾が-OH, -H, -O-(他の糖に結合していない)のみである場合他の省略対象である場合はtrueを返す。
	 * Whether or not the modificaiton is ellipsis target,
	 *  which all the other modifications on the connected backbone carbon are -OH, -H or -O- (not connect to other backbone).
	 * @return true if modification is ElipseTarget
	 */
	public boolean isEllipsisTarget ( SubGraph graph, String ALIN ) {
		if ( ALIN.equals("") )
		if( graph.getAtoms().size() .atomsOfModification.size()!=1) return false;
		if(!this.atomsOfModification.get(0).symbol.equals("O")) return false;
		if( this.atomsOfBackbone.size()!=1) return false;

		Atom backboneAtom = this.atomsOfBackbone.get(0);
		for(Connection connect : backboneAtom.connections){
			if(connect.atom.isBackbone()) continue;
			if(connect.atom.symbol.equals("H")) continue;
			if(connect.atom.modification==this) continue;

			Modification otherModification = connect.atom.modification;

			// 2つ以上の主鎖炭素に結合している修飾が含まれていたら、出力する
			if(otherModification.connectedBackbones.size()>1) return false;

			//今のルールの場合、修飾Aを出力表示するかしないかは、修飾Aが結合している主鎖炭素の他の修飾をチェックして
			//　１．グリコシド結合を持つ修飾が含まれていたら出力することを確定する。（falseを返す。）
			//　２．酸素だけの修飾（-OHもしくはエーテル環の酸素）は判定対象外とする。
			//　３．酸素以外のエーテル環？（例えば窒素が主鎖炭素の1位と5位に結合して環状糖を構成している場合等）は判定対象外とする。
			//修飾が結合しているのがC1以外の主鎖炭素で、他の修飾に上記の３に該当する修飾が存在する場合の-OHは出力する。
			if(backboneAtom.backbone.getFirst() != backboneAtom && otherModification.atomsOfModification.size()==1
					&& otherModification.atomsOfBackbone.size()==2 && otherModification.connectedBackbones.size()==1) return false;

			// Oだけの修飾は無視
			if(otherModification.atomsOfModification.size()==1 && otherModification.atomsOfModification.get(0).symbol.equals("O")) continue;

			// 環状糖鎖を構成している1原子からなる修飾は無視
			if(otherModification.atomsOfModification.size()==1 && otherModification.atomsOfBackbone.size()==2 && otherModification.connectedBackbones.size()==1) continue;

			return false;
		}

		return true;
	}

	/**
	 * Return true if modification is Aglycone
	 * Modificationを取得してからAglyconeチェックを行うのではなく、Aglyconeだったら主鎖と結合している修飾のみをModificationとして取得するように処理を書きかえる。
	 * @return true if modification is Aglycone
	 */
	public boolean isAglycone() {
		if(this.atomsOfModification.size()==1) return false;
		for(Atom atom : this.atomsOfBackbone){
			if(atom.backbone.indexOf(atom)!=0) return false;
		}
		return true;
	}

	/**
	 * Return true if modification is HydroxyGroup
	 * @return true if modification is HydroxyGroup
	 */
	public boolean isHydroxyGroup(){
		if( this.atomsOfBackbone.size()!=1) return false;
		if( this.atomsOfModification.size()!=1) return false;
		if(!this.atomsOfModification.getFirst().symbol.equals("O")) return false;
		return true;
	}

	/**
	 * Return true if modification is Ether
	 * @return true if modification is Ether
	 */
	public boolean isEther(){
		if( this.atomsOfBackbone.size()!=2) return false;
		if( this.atomsOfModification.size()!=1) return false;
		if(!this.atomsOfModification.getFirst().symbol.equals("O")) return false;
		return true;
	}

}
