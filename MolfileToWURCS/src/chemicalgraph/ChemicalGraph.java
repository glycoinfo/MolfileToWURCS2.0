package chemicalgraph;

import chemicalgraph.subgraph.aglycone.Aglycone;
import chemicalgraph.subgraph.modification.Modification;
import utility.Chemical;
import utility.HierarchicalDigraph;

/**
 * Class for chemical graph
 * @author KenichiTanaka
 */
public class ChemicalGraph {
	//----------------------------
	// Member variable
	//----------------------------
	/** List of atoms */
	public AtomList atoms = new AtomList();
	/** List of bonds */
	public BondList bonds = new BondList();
	
	//----------------------------
	// Public method (void)
	//----------------------------
	/** Calculate stereo chemistry and set stero to stereoTmp. */
	public void setStereoTmp(){
		// EZRS check
		for(Atom atom : this.atoms){
			atom.connections.setTmpFlg(false);
		}
		this.setStereoTmp(false);
		// rs check, use EZRS results of other element.
		for(Atom atom : this.atoms){
			atom.connections.setTmpFlg(atom.stereoTmp=="R"||atom.stereoTmp=="S");
		}
		this.setStereoTmp(true);
	}
	
	/**
	 * <pre>
	 * Recursively expand this chemical graph by following: If connected element of this chemical graph is aglycone then add the element to this chemical graph.
	 * 
	 * 格納済みのAtomに結合している要素をチェックし、Aglyconeの可能性がある要素(つまり主鎖炭素ではない要素)を追加する処理を再帰的に繰り返すことでグラフを拡張します。
	 * (使い方)
	 * 主鎖炭素を決定した後、
	 *  1.ChemicalGraphオブジェクトを作成
	 *  2.主鎖炭素に隣接している原子を種原子としてChemicalGraphオブジェクトにセット
	 *  3.本関数を実行し、Aglyconeの可能性がある隣接要素をChemicalGraphオブジェクトに追加する処理を再帰的に繰り返す。
	 *  4.isAgrycone関数でAglyconeかそうでないか（C1以外の主鎖炭素に繋がっていたらAglyconeではない）を判定し、Aglyconeであれば、リストに保存
	 * といった流れでAglyconeの取得を行います。
	 * ここでは水素をいれずに、EZRS判定では隣接水素は取り込むといった処理を行っているので、逆にした方がいいかもしれない・・・
	 * </pre>
	 */
	public void expandCandidateAglycone(){
		for(int ii=0; ii<this.atoms.size(); ii++){
			Atom atom = this.atoms.get(ii);
			for(Connection connection : atom.connections){
				if(connection.atom.isBackbone()) continue;
				if(connection.atom.symbol.equals("H")) continue;
				if(!this.atoms.contains(connection.atom)){
					this.atoms.add(connection.atom);
				}
				if(!this.bonds.contains(connection.bond)){
					this.bonds.add(connection.bond);
				}
			}
		}
	}

	/**
	 * Recursively expand this chemical graph by following: If connected element of this chemical graph is modification then add the element to this chemical graph.
	 * 格納済みのAtomに結合している要素がModificationであれば追加する処理を再帰的に繰り返します。
	 */
	public void expandModification() {
		for(int ii=0; ii<this.atoms.size(); ii++){
			Atom atom = this.atoms.get(ii);
			if(atom.isBackbone()) continue;
			for(Connection connection : atom.connections){
				if(connection.atom.isAglycone()) continue;
				if(connection.atom.symbol.equals("H")) continue;
				if(!this.atoms.contains(connection.atom)){
					this.atoms.add(connection.atom);
				}
				if(!this.bonds.contains(connection.bond)){
					this.bonds.add(connection.bond);
				}
			}
		}
	}

	/**
	 * Calculate ECnumber of this chemical graph without ignoreAtoms and ignoreBonds.
	 * @param ignoreBonds
	 * @param ignoreAtoms
	 */
	public void updateECnumber(final BondList ignoreBonds, final AtomList ignoreAtoms){
		this.atoms.setTmp(1);
		int uniqUpdateECnumber = this.atoms.countUniqTmp();
		int uniqECnumber;
		do{
			this.atoms.copyTmpToECNumber();
			uniqECnumber = uniqUpdateECnumber;
			
			this.atoms.setTmp(0);
			for(Atom atom : this.atoms){
				if( ignoreAtoms != null && ignoreAtoms.contains(atom)) continue;
				for(Connection connection : atom.connections){
					if(!this.bonds.contains(connection.bond)) continue;
					if( ignoreBonds != null && ignoreBonds.contains(connection.bond)) continue;
					if( ignoreAtoms != null && ignoreAtoms.contains(connection.atom)) continue;
					atom.tmp += connection.atom.subgraphECnumber;
				}
			}
			uniqUpdateECnumber = this.atoms.countUniqTmp();
		}while(uniqECnumber < uniqUpdateECnumber);
	}
	
	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * Return Aglycone object.
	 * @return Aglycone object
	 */
	public Aglycone toAglycone(){
		Aglycone aglycone = new Aglycone();
		aglycone.atoms = this.atoms;
		aglycone.bonds = this.bonds;
		for(Atom atom : aglycone.atoms){
			atom.aglycone = aglycone;
		}
		return aglycone;
	}

	/**
	 * Return Modification object.
	 * @return Modification object
	 */
	public Modification toModification() {
		Modification mod = new Modification();
		mod.atoms = this.atoms;
		mod.bonds = this.bonds;
		for(Atom atom : mod.atoms){
			atom.modification = mod;
		}
		return mod;
	}

	/**
	 * Return max/min coordinates of this object.
	 * @return max/min coordinates of this object.
	 */
	public double[][] getMoleculeSize(){
		if(this.atoms.size() == 0) return null;
		double molsize[][] = new double[2][3];
		molsize[0][0] = this.atoms.get(0).coordinate[0];
		molsize[0][1] = this.atoms.get(0).coordinate[1];
		molsize[0][2] = this.atoms.get(0).coordinate[2];
		molsize[1][0] = this.atoms.get(0).coordinate[0];
		molsize[1][1] = this.atoms.get(0).coordinate[1];
		molsize[1][2] = this.atoms.get(0).coordinate[2];
		for(Atom atom : this.atoms){
			molsize[0][0] = Math.min(molsize[0][0], atom.coordinate[0]);
			molsize[0][1] = Math.min(molsize[0][1], atom.coordinate[1]);
			molsize[0][2] = Math.min(molsize[0][2], atom.coordinate[2]);
			molsize[1][0] = Math.max(molsize[1][0], atom.coordinate[0]);
			molsize[1][1] = Math.max(molsize[1][1], atom.coordinate[1]);
			molsize[1][2] = Math.max(molsize[1][2], atom.coordinate[2]);
		}
		return molsize;
	}

	/**
	 * Return ture if this chemical graph is aglycone.(if chemical graph connect with only C1 carbon of backbones, chemical graph is aglycone.)
	 * @return ture if this chemical graph is aglycone.(if chemical graph connect with only C1 carbon of backbones, chemical graph is aglycone.)
	 */
	public boolean isAglycone(){
		for(Atom atom : this.atoms){
			for(Connection connection : atom.connections){
				if(!connection.atom.isBackbone()) continue;
				if( connection.atom.isAnomer()) continue;
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Return true if this chemical graph contains input atom.
	 * @param atom
	 * @return true if this chemical graph contains input atom.
	 */
	public boolean contains(final Atom atom){
		return this.atoms.contains(atom);
	}
	
	/**
	 * Return true if this chemical graph contains input bond.
	 * @param bond
	 * @return true if this chemical graph contains input bond.
	 */
	public boolean contains(final Bond bond){
		return this.bonds.contains(bond);
	}

	/**
	 * Remove objects which connect with input atom, then remove input atom.
	 * @param atom
	 * @return true if this chemical graph contains input atom.
	 */
	public boolean remove(final Atom atom){
		BondList removeBonds = new BondList();
		for(Connection connection : atom.connections){
			removeBonds.add(connection.bond);
		}
		for(Bond bond : removeBonds){
			this.bonds.remove(bond);
		}
		return this.atoms.remove(atom);
	}
	
	//----------------------------
	// Private method (void)
	//----------------------------
	/**
	 * Calculate stereo chemistry and set stero to stereoTmp.
	 * @param useEZRS true : EZRS check, false : rs check
	 */
	private void setStereoTmp(boolean useEZRS){
		boolean continueflg = true;
		int depth = 0;
		while(continueflg){
			continueflg = false;
			depth++;
			for(Atom atom : this.atoms){
				if(atom.connections.tmpflg) continue;
				continueflg = true;
				// 全探索完了チェックフラグをセット
				atom.connections.setIsCompletedFullSearch(false);
				// 深さdephtのHierarchicalDigraphを構築
				HierarchicalDigraph hd = new HierarchicalDigraph(this, atom, depth, (double) atom.atomicNumber(), new AtomList(),useEZRS);
				// CIP順位をセット
				atom.connections.isUniqOrder = true;
				int order = 0;
				boolean pre = true;
				for(HierarchicalDigraph child : hd.children){
					if(pre||child.isUniqOrder) order++;
					Connection connection = atom.connections.getConnect(child.atom);
					if(connection!=null){
						connection.isUniqOrder = child.isUniqOrder;
						connection.isCompletedFullSearch = child.isCompletedFullSearch;
						connection.CIPorder = order;
					}
					
					if(!child.isUniqOrder) atom.connections.isUniqOrder = false;
					pre = child.isUniqOrder;
				}
				atom.connections.sortByCIPorder();
				
				// 打ち切りチェック
				// maxDepthForHierarchicalDigraphの判定がおかしいのでやり直し
				if(atom.connections.isUniqOrder){
					// chiralであることが確定した場合、探索を打ち切る。
					atom.connections.tmpflg = true;
					continue;
				}else{
					// achiralであることが確定した場合、探索を打ち切る。
					// 順位が等しい結合のペアが存在し、かつそのペアが全探索が完了している場合
					// atom.connectionsは優先順位でソートされている
					// isUniqOrder==falseとなるconnectionは順位のつかなかったconnectionを意味しており、順位が等しい別のconnectionが存在する
					// Listの中で連続してisUniqOrder==falseとなっているconnectionが順位が等しいconnectionとなっている
					// 順位が等しいconnectionペアがどちらも全探索が完了している場合にachiralである事が確定する。
					for(int ii=0; ii<atom.connections.size()-1; ii++){
						Connection connection1 = atom.connections.get(ii);
						if( connection1.isUniqOrder) continue;
						if(!connection1.isCompletedFullSearch) continue;
						for(int jj=ii+1; jj<atom.connections.size(); jj++){
							Connection connection2 = atom.connections.get(jj);
							if( connection2.isUniqOrder) break;
							if(!connection2.isCompletedFullSearch) continue;
							atom.connections.tmpflg = true;
						}
					}
				}
				
				// ここから追加
				// C-N=N=N-Oの場合、中央のNはachiralである。(ダミーのNが2つ付く)
				// この場合、ダミーのN2つは同じものとみなされユニークではなくなる為、atom.connections.isUniqOrder = falseとなってここに入ってくる。
				// しかし、ここではダミー以外の要素のみをチェックしているので、achiralが確定していても上記のチェックを逃れてしまう。
				// その為ここでチェックする。
				if(hd.isCompletedFullSearch){
					atom.connections.tmpflg = true;
				}
				// ここまで
			}
		}
		
		if(useEZRS==false){
			// RS判定
			for(Atom atom : this.atoms){
				if(!atom.hybridOrbital().equals("sp3")) continue;
				if(!atom.connections.isUniqOrder) continue;
				ConnectionList subgraphConnects = this.getConnects(atom);
				if( subgraphConnects.size()!=4) continue;
				String stereo = Chemical.sp3stereo(subgraphConnects.get(0), subgraphConnects.get(1), subgraphConnects.get(2), subgraphConnects.get(3));
				atom.stereoTmp = stereo;
			}
			// EZ判定
			for(Bond bond : this.bonds){
				if( bond.type!=2) continue;
				Atom a0 = bond.atoms[0];
				Atom b0 = bond.atoms[1];
				if(!a0.connections.isUniqOrder) continue;
				if(!b0.connections.isUniqOrder) continue;
				ConnectionList a0connects = this.getConnects(a0);
				ConnectionList b0connects = this.getConnects(b0);
				if( a0connects.size()<2) continue;
				if( b0connects.size()<2) continue;
				Atom a1 = (a0connects.get(0).atom == b0) ? a0connects.get(1).atom : a0connects.get(0).atom;
				Atom b1 = (b0connects.get(0).atom == a0) ? b0connects.get(1).atom : b0connects.get(0).atom;
				String stereo = Chemical.sp2stereo(a0, a1, b0, b1);
				bond.stereoTmp = stereo;
			}
		}else{
			// rs判定
			for(Atom atom : this.atoms){
				if( atom.stereoTmp!=null) continue;
				if(!atom.hybridOrbital().equals("sp3")) continue;
				if(!atom.connections.isUniqOrder) continue;
				ConnectionList subgraphConnects = this.getConnects(atom);
				if( subgraphConnects.size()!=4) continue;
				String stereo = Chemical.sp3stereo(subgraphConnects.get(0), subgraphConnects.get(1), subgraphConnects.get(2), subgraphConnects.get(3)).toLowerCase();
				atom.stereoTmp = stereo;
			}
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
	private ConnectionList getConnects(Atom atom){
		ConnectionList connections = new ConnectionList();
		for(Connection connection : atom.connections){
			if(connection.atom.symbol.equals("H")||(this.contains(connection.atom) && this.contains(connection.bond))){
				connections.add(connection);
			}
		}
		return connections;
	}
}
