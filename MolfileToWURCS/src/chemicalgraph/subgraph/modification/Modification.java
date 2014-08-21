package chemicalgraph.subgraph.modification;

import java.util.Collections;
import java.util.Comparator;

import chemicalgraph.Atom;
import chemicalgraph.AtomList;
import chemicalgraph.Bond;
import chemicalgraph.ChemicalGraph;
import chemicalgraph.Connection;
import chemicalgraph.ConnectionList;
import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.backbone.BackboneList;
import chemicalgraph.subgraph.backbone.ConnectTypeList;

import utility.Chemical;

/**
 * Class for modification
 * @author KenichiTanaka
 */
public class Modification extends ChemicalGraph{
	//----------------------------
	// Member variable
	//----------------------------
	/** List of backbones which connect with this modification. */
	public BackboneList connectedBackbones;
	/** List of backbone atoms which connect with this modification. (super.atoms = atomsOfBackbone + atomsOfModification) */
	public AtomList atomsOfBackbone;
	/** List of atoms of modification. (super.atoms = atomsOfBackbone + atomsOfModification) */
	public AtomList atomsOfModification;
	/** List of connections from a backbone to this modification */
	public ConnectionList connectionsFromBackboneToModification;
	/** 修飾文字列表示を目的として修飾の原子を探索した結果 */
	public PathList paths;

	//----------------------------
	// Public method (void)
	//----------------------------
	public void setStereoModification() {
		super.setStereoTmp();
		for(Atom atom : this.atoms){
			atom.stereoModification = atom.stereoTmp;
			atom.stereoTmp = null;
		}
		for(Bond bond : this.bonds){
			bond.stereoModification = bond.stereoTmp;
			bond.stereoTmp = null;
		}
	}
	
	public void findCanonicalPaths() {
		// EC番号を付加
		this.updateECnumber(null, null);

		// 初期EC番号を保存
		for(Atom atom : this.atoms){
			atom.initialECnumber = atom.subgraphECnumber;
		}

		// 探索開始ノードのソート
		Collections.sort(this.atoms, new Comparator<Atom>() {
			public int compare(Atom atom1, Atom atom2) {
				// １．主鎖炭素を優先
				if( atom1.isBackbone() && !atom2.isBackbone()) return -1;
				if(!atom1.isBackbone() &&  atom2.isBackbone()) return 1;
				// ２．EC番号が小さい修飾原子を優先
				if( atom1.subgraphECnumber != atom2.subgraphECnumber ) return atom1.subgraphECnumber - atom2.subgraphECnumber;
				// ３．原子番号が小さい修飾原子を優先
				if( atom1.atomicNumber()   != atom2.atomicNumber()   ) return atom1.atomicNumber()   - atom2.atomicNumber();
				// ４．立体等に基づいて優先順位を追加したい場合はここに比較関数を追加
				
				return 0;
			}
		});

		// Pathの構築
		ConnectionList connects = new ConnectionList();
		this.paths = new PathList();
		this.paths.add(new Path(null, null, this.atoms.get(0), null));
		while(true){
			final Atom tailAtom = this.paths.getLast().pathEnd.atom;

			// 隣接Connectを抽出
			for(Connection connect : tailAtom.connections){
				if(!this.contains(connect.bond)) continue;	     // 対象外
				if( this.paths.contains(connect.bond)) continue; // 探索済み
				if( connects.contains(connect.bond)) continue;   // 候補に格納済み
				connects.add(connect);
			}

			// 隣接要素がなくなったら終了
			if(connects.size()==0) break;

			// 接続要素の2原子が共に探索済みの場合、start()が末端に近いConnectを採用する。
			for(Connection connect : connects){
				if(this.contains(connect.atom)){
					if(this.paths.indexOf(connect.start()) < this.paths.indexOf(connect.atom)){
						int index = connects.indexOf(connect);
						for(Connection connect2 : connect.atom.connections){
							if(connect2.bond == connect.bond){
								connects.set(index, connect2);
								break;
							}
						}
					}
				}
			}

			// EC番号再計算
			this.updateECnumber(this.paths.bonds(), this.paths.atoms());

			// 隣接Connectをソート
			final AtomList tmpPathAtom = this.paths.atoms();
			Collections.sort(connects, new Comparator<Connection>() {
				public int compare(Connection connect1, Connection connect2) {
					// １．繋がっている芳香環はまとめて出したい
					if(tailAtom.isAromatic){
						if( connect1.end().isAromatic   && !connect2.end().isAromatic  ) return -1;
						if(!connect1.end().isAromatic   &&  connect2.end().isAromatic  ) return 1;
						if( connect1.start().isAromatic && !connect2.start().isAromatic) return -1;
						if(!connect1.start().isAromatic &&  connect2.start().isAromatic) return 1;
					}
					// ２．後半に探索した修飾原子から伸びている結合を優先
					if(tmpPathAtom.indexOf(connect1.start()) != tmpPathAtom.indexOf(connect2.start())) return tmpPathAtom.indexOf(connect2.start()) - tmpPathAtom.indexOf(connect1.start());
					// ３．主鎖炭素に結合している修飾原子を優先
					int backboneNum1 = connect1.end().connections.backbones().size();
					int backboneNum2 = connect2.end().connections.backbones().size();
					if(backboneNum1!=0 && backboneNum2==0) return -1;
					if(backboneNum1==0 && backboneNum2!=0) return 1;
					// ４．EC番号が大きい修飾原子を優先(未探索部分の中心に向かっていく)
					if( connect1.end().subgraphECnumber != connect2.end().subgraphECnumber) return connect2.end().subgraphECnumber- connect1.end().subgraphECnumber;
					// ５．初期EC番号が大きい修飾原子を優先(初期構造の中心に向かっていく)
					if( connect1.end().initialECnumber  != connect2.end().initialECnumber ) return connect2.end().initialECnumber - connect1.end().initialECnumber;
					// ６．原子番号が小さい修飾原子を優先
					if( connect1.end().atomicNumber()   != connect2.end().atomicNumber()  ) return connect1.end().atomicNumber()  - connect2.end().atomicNumber();
					// ７．BondTypeが少ない方を優先
					if(connect1.bond.type != connect2.bond.type) return connect1.bond.type - connect2.bond.type;
					// ８．CIP順位が優位な方を優先 (StereoModificationを計算する処理がStereoMoleculeの後にあるので上書きされている。)
					if(connect1.CIPorder != connect2.CIPorder) return connect1.CIPorder - connect2.CIPorder;
					
					return 0;
				}
			});

			// もっともスコアの高い隣接要素を追加
			Connection newConnect = connects.removeFirst();
			Path start = this.paths.get(newConnect.start());
			Path end   = this.paths.get(newConnect.end());
			this.paths.add(new Path(start, end, newConnect.atom, newConnect.bond));
		}
	}
	
	public void findConnectedBackbones(){
		this.connectedBackbones = new BackboneList();
		AtomList atoms = this.paths.atoms();
		for(Atom atom : atoms){
			if(!atom.isBackbone()) continue;
			if(this.connectedBackbones.contains(atom.backbone)) continue;
			this.connectedBackbones.add(atom.backbone);
		}
	}

	public void findAtomsOfBackbones(){
		this.atomsOfBackbone = new AtomList();
		AtomList atoms = this.paths.atoms();
		for(Atom atom : atoms){
			if(!atom.isBackbone()) continue;
			if(this.atomsOfBackbone.contains(atom)) continue;
			this.atomsOfBackbone.add(atom);
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

	public void findCOLINs() {
		this.connectionsFromBackboneToModification = new ConnectionList();
		for(Atom atom : atomsOfBackbone){
			for(Connection connect : atom.connections){
				if(!this.atomsOfModification.contains(connect.atom)) continue;
				this.connectionsFromBackboneToModification.add(connect);
			}
		}

		// connectionsFromBackboneToModificationにstereo情報を付加
		// 主鎖からみたStereo情報は主鎖炭素結合を基準に情報を入れる。
		for(Connection connectBackboneToMod : this.connectionsFromBackboneToModification){
			Atom Co = connectBackboneToMod.start();
			Atom Mo = connectBackboneToMod.end();
			Backbone backbone = Co.backbone;
			int indexCo = backbone.indexOf(Co);
			Atom Csmall = (Co != backbone.getFirst()) ? backbone.get(indexCo-1) : null;
			Atom Clarge = (Co != backbone.getLast())  ? backbone.get(indexCo+1) : null;
			if(Csmall==null || Clarge==null){
				// 環状糖の環を構成している原子をダミーの主鎖炭素として扱う処理
				for(Connection connect : Co.connections){
					if(connect.atom == Mo) continue;
					if(connect.atom.isBackbone()) continue;
					if(connect.atom.connections.count(backbone) > 1){
						if(Csmall==null) Csmall = connect.atom;
						if(Clarge==null) Clarge = connect.atom;
					}
				}
			}
			Connection connectCsmall = (Csmall != null) ? Co.connections.getConnect(Csmall) : null;
			Connection connectClarge = (Clarge != null) ? Co.connections.getConnect(Clarge) : null;

			ConnectionList connectsMod = new ConnectionList();
			for(Connection connect : Co.connections){
				if(connect == connectCsmall) continue;
				if(connect == connectClarge) continue;
				connectsMod.addLast(connect);
			}
			int orderMo = connectsMod.indexOf(connectBackboneToMod)+1;
			
			if(Co.hybridOrbital().equals("sp3")){
				// ?-Co-Mo-?
				if(Co.stereoMolecule!=null && Co.stereoMolecule.equals("X")){
					connectBackboneToMod.stereoForWURCS = "x";
				}else if(Csmall!=null && Clarge!=null){
					// sp3 non terminal
					Connection connectOther = (connectsMod.get(0)==connectBackboneToMod) ? connectsMod.get(1) : connectsMod.get(0);
					String turn = Chemical.sp3stereo(connectBackboneToMod, connectOther, connectClarge, connectCsmall);
					if(turn.equals("R")){
						connectBackboneToMod.stereoForWURCS = "1";
					}else if(turn.equals("S")){
						connectBackboneToMod.stereoForWURCS = "2";
					}else{
						connectBackboneToMod.stereoForWURCS = "0";
					}
				}else{
					// sp3 terminal
					if(Co.stereoMolecule == null){
						connectBackboneToMod.stereoForWURCS = "0";
					}else{
						if(Csmall == null){
							// sp3 terminal
							String turn = Chemical.sp3stereo(connectsMod.get(0), connectsMod.get(1), connectsMod.get(2), connectClarge);
							if(turn.equals("S")){
								connectBackboneToMod.stereoForWURCS = "" + orderMo;
							}else if(turn.equals("R")){
								if(     orderMo == 1){ connectBackboneToMod.stereoForWURCS = "1"; }
								else if(orderMo == 2){ connectBackboneToMod.stereoForWURCS = "3"; }
								else if(orderMo == 3){ connectBackboneToMod.stereoForWURCS = "2"; }
							}
						}else if(Clarge == null){
							// sp3 terminal
							String turn = Chemical.sp3stereo(connectsMod.get(0), connectsMod.get(1), connectsMod.get(2), connectCsmall);
							if(turn.equals("R")){
								connectBackboneToMod.stereoForWURCS = "" + orderMo;
							}else if(turn.equals("S")){
								if(     orderMo == 1){ connectBackboneToMod.stereoForWURCS = "1"; }
								else if(orderMo == 2){ connectBackboneToMod.stereoForWURCS = "3"; }
								else if(orderMo == 3){ connectBackboneToMod.stereoForWURCS = "2"; }
							}
						}
					}
				}
			}else if(Co.hybridOrbital().equals("sp2")){
				if(connectBackboneToMod.bond.type==2){
					// ?-Co=Mo-?
					if(     connectBackboneToMod.bond.stereoMolecule == null    ){ connectBackboneToMod.stereoForWURCS = "0"; }
					else if(connectBackboneToMod.bond.stereoMolecule.equals("X")){ connectBackboneToMod.stereoForWURCS = "x"; }
					else{
						Connection connectMp = null;
						Connection connectMs = null;
						for(Connection connect : Mo.connections){
							if(connect.atom == Co){ continue; }
							if(connectMp == null){ connectMp = connect; continue; }
							if(connectMs == null){ connectMs = connect; continue; }
						}

						if(connectMp.bond.type == 2){
							//      ?-Co=Mo=Mp
							connectBackboneToMod.stereoForWURCS = "0";
						}else if(Csmall != null && connectCsmall.bond.type == 2){
							//              Mp
							//             /
							// Csmall=Co=Mo
							//             \
							//              Ms
							connectBackboneToMod.stereoForWURCS = "0";
						}else if(Clarge != null && connectClarge.bond.type == 2){
							//              Mp
							//             /
							// Clarge=Co=Mo
							//             \
							//              Ms
							connectBackboneToMod.stereoForWURCS = "0";
						}else if(Csmall != null){
							// Csmall       Mp
							//       \     / <- connectMp
							//        Co=Mo
							//       /     \ <- connectMs
							// Clarge/Y     Ms
							connectBackboneToMod.stereoForWURCS = Chemical.sp2stereo(Co, Csmall, Mo, connectMp.atom);
						}else{
							//      Y       Mp
							//       \     / <- connectMp
							//        Co=Mo
							//       /     \ <- connectMs
							// Clarge       Ms
							Atom Y = null;
							for(Connection connect : Co.connections){
								if(connect.atom == Mo) continue;
								if(connect.atom == Clarge) continue;
								Y = connect.atom;
								continue;
							}
							connectBackboneToMod.stereoForWURCS = Chemical.sp2stereo(Co, Y, Mo, connectMp.atom);
						}
					}
				}else if(connectCsmall != null && connectCsmall.bond.type == 2){
					//             Mo
					//            /
					// ?-Csmall=Co
					//            \
					//             Y
					if(     connectCsmall.bond.stereoMolecule == null    ){ connectBackboneToMod.stereoForWURCS = "0"; }
					else if(connectCsmall.bond.stereoMolecule.equals("X")){ connectBackboneToMod.stereoForWURCS = "x"; }
					else{
						Connection connectTarget = null;
						// Csmallが末端でない場合、先に延びている主鎖炭素と比較
						for(Connection connect : Csmall.connections){
							if( connect.atom == Co) continue;
							if(!connect.atom.isBackbone()) continue;
							connectTarget = connect;
							break;
						}
						// Csmallが末端だった場合（connectTargetが見つからない場合）、接続している修飾の内CIP優勢な修飾と比較
						if(connectTarget==null){
							for(Connection connect : Csmall.connections){
								if(connect.atom == Co) continue;
								connectTarget = connect;
								break;
							}
						}
						if(connectTarget.bond.type == 2){
							//                  Mo
							//                 /
							// Target=Csmall=Co
							//                 \
							//                  Y
							connectBackboneToMod.stereoForWURCS = "0";
						}else{
							connectBackboneToMod.stereoForWURCS = Chemical.sp2stereo(Csmall, connectTarget.atom, Co, Mo);
						}
					}
				}else if(connectClarge != null && connectClarge.bond.type == 2){
					// Y
					//  \
					//   Co=Clarge-?
					//  /
					// Mo
					if(     connectClarge.bond.stereoMolecule == null    ){ connectBackboneToMod.stereoForWURCS = "0"; }
					else if(connectClarge.bond.stereoMolecule.equals("X")){ connectBackboneToMod.stereoForWURCS = "x"; }
					else{
						Connection connectTarget = null;
						// Clargeが末端でない場合、先に延びている主鎖炭素と比較
						for(Connection connect : Clarge.connections){
							if( connect.atom == Co) continue;
							if(!connect.atom.isBackbone()) continue;
							connectTarget = connect;
							break;
						}
						// Clargeが末端だった場合（connectTargetが見つからない場合）、接続している修飾の内CIP優勢な修飾と比較
						if(connectTarget==null){
							for(Connection connect : Clarge.connections){
								if(connect.atom == Co) continue;
								connectTarget = connect;
								break;
							}
						}
						if(connectTarget.bond.type == 2){
							// Y
							//  \
							//   Co=Clarge=Target
							//  /
							// Mo
							connectBackboneToMod.stereoForWURCS = "0";
						}else{
							// Y           Target
							//  \         /
							//   Co=Clarge  
							//  /         \
							// Mo          Target
							connectBackboneToMod.stereoForWURCS = Chemical.sp2stereo(Clarge, connectTarget.atom, Co, Mo);
						}
					}
				}else{
					//            Mo
					//              \
					//               Co=Y-?
					//              /
					// Csmall/Clarge
					Connection connectY = null;
					for(Connection connect : Co.connections){
						if(connect.atom.isBackbone()) continue;
						if(connect.atom == Mo) continue;
						connectY = connect;
						break;
					}
					Connection connectYp = null;
					Connection connectYs = null;
					for(Connection connect : connectY.atom.connections){
						if(connect.atom == connectY.atom){ continue; }
						if(connectYp == null){ connectYp = connect; continue; }
						if(connectYs == null){ connectYs = connect; continue; }
					}
					if(     connectY.bond.stereoMolecule == null    ){ connectBackboneToMod.stereoForWURCS = "0"; }
					else if(connectY.bond.stereoMolecule.equals("X")){ connectBackboneToMod.stereoForWURCS = "x"; }
					else{
						//            Mo      Yp
						//              \    /
						//               Co=Y
						//              /    \
						// Csmall/Clarge      Ys
						connectBackboneToMod.stereoForWURCS = Chemical.sp2stereo(connectY.atom, connectYp.atom, Co, Mo);
					}
				}
			}
			else if(Co.hybridOrbital().equals("sp")){
				// sp terminal
				connectBackboneToMod.stereoForWURCS = "0";
			}
			if(connectBackboneToMod.stereoForWURCS == null) connectBackboneToMod.stereoForWURCS = "?";
		}
	}

	/**
	 * デバッグ関数
	 */
	public void debug(){
		System.err.print("Atom : ");
		for(Atom atom : this.atoms){
			System.err.print(atom.molfileAtomNo + "-");
		}
		System.err.println();
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * Return string of connections between backbone to modification.
	 * @param backbones
	 * @param outputFullInformation
	 * @return string of connections between backbone to modification.
	 */
	public String toCOLIN(final BackboneList backbones, final boolean outputFullInformation){
		String COLIN = "";
		for(Connection connect : this.connectionsFromBackboneToModification){
			if(!COLIN.equals("")) COLIN += ",";

			// NB : Number of Backbones
			if(outputFullInformation || this.connectedBackbones.size()!=1){
				COLIN += (backbones.indexOf(connect.start().backbone) + 1) + "+";
			}

			// PCB : Position of the connected Carbon in the Backbone
			COLIN += connect.start().backbone.indexOf(connect.start()) + 1;

			// DMB : Direction of Modification on the Backbone carbon
			ConnectTypeList types = new ConnectTypeList(connect.start(), connect.start().backbone);
			if(outputFullInformation || types.get(types.uniqBackboneNum).connects.size() != 1){
				// 6:0, 5:0 などSkeletonCodeで”h”の水酸基にBMUが結合する際に”：０”が表示されないようにする処理 (Issaku YAMADA 2014/01/18)
				if(!connect.stereoForWURCS.equals("0")){
					// backbone炭素に結合している原子３か２個のなかで、一つのみしか結合し得ない場合省略すると、変更する必要がある。(Issaku YAMADA 2014/01/18)
					//if (The around atoms on the carbon atom in the backbone has only one valence for the each atom.)
					COLIN += ":" + connect.stereoForWURCS;
				}
			}

			// PCA : Position of connected backbone Carbon in ALIN
			if(outputFullInformation || !(this.atomsOfBackbone.size()==2 && this.atomsOfModification.size()==1) && this.atomsOfBackbone.size() > 1){
				COLIN += "-" + (this.atomsOfBackbone.indexOf(connect.start())+1);
			}
		}
		
		return COLIN;
	}
	
	/**
	 * @param outputFullInformation
	 * @return ALIN: Atomic LInear Notation
	 */
	public String toALIN(final boolean outputFullInformation){
		// 両端が主鎖であるエーテル結合の酸素は省略*O*
		if(outputFullInformation==false && this.atomsOfBackbone.size()==2 && this.atomsOfModification.size()==1 && this.atomsOfModification.get(0).symbol.equals("O")) return "";
		return this.paths.toALIN();
	}
	
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
			Path path1 = paths1.get(ii);
			Path path2 = paths2.get(ii);

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
	 * @return true if modification is ElipseTarget
	 */
	public boolean isEllipseTarget(){
		if( this.atomsOfModification.size()!=1) return false;
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
