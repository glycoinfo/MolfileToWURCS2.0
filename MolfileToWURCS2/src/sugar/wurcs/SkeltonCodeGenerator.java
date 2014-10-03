package sugar.wurcs;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import sugar.chemicalgraph.Atom;
import sugar.chemicalgraph.Connection;
import sugar.wurcs.glycan.ConnectType;
import util.Chemical;
import util.analytical.AtomIdentifier;
import carbohydrate.Backbone;

public class SkeltonCodeGenerator {
	private String hybridOrbital;
	private int uniqBackboneNum;
	private int uniqModNum;
	private int maxConnectBackboneNum;
	private String stereo;
	private LinkedList<ConnectType> connectTypes = new LinkedList<ConnectType>();

	//----------------------------
	// Constructor
	//----------------------------

	/**
	 * Return the skeletone code of this backbone.
	 * @return the skeletone code of this backbone.
	 */
	public String makeSkeletonCode(Backbone backbone){
		String skeletonCode = "";
		// 主鎖炭素の文字コードを付加
		for(Atom atom : backbone){
			LinkedList<ConnectType> types = this.getConnectTypes(atom, backbone);
			ConnectType bone1 = (this.uniqBackboneNum > 0) ? types.get(0) : null;
			ConnectType bone2 = (this.uniqBackboneNum > 1) ? types.get(1) : null;
			ConnectType X = (this.uniqModNum > 0) ? types.get(this.uniqBackboneNum + 0) : null;
			ConnectType Y = (this.uniqModNum > 1) ? types.get(this.uniqBackboneNum + 1) : null;
			ConnectType Z = (this.uniqModNum > 2) ? types.get(this.uniqBackboneNum + 2) : null;

			this.setStereo(atom, backbone, bone1, bone2, X, Y, Z);

			// types.is(軌道の種類, 主鎖との結合数, ユニークな修飾の数, 立体情報, 複数の主鎖炭素と結合している修飾が存在する(つまり環状の糖の先頭炭素か末端炭素の認識))
			// nullの場合は、該当する項目をチェックしない
			// ここでは、主鎖炭素と直接つながっている1原子のみを修飾として見ている点に注意
			// Terminal
			if(this.is("sp3", 1, 1, null, null) && bone1.bondtype==1 && X.is(3, 1, true,  "H")                                              ){ skeletonCode += "m"; continue; } // -C(H)(H)(H)
			if(this.is("sp3", 1, 1, null, null) && bone1.bondtype==1 && X.is(3, 1, false, "H")                                              ){ skeletonCode += "M"; continue; } // -C(X)(X)(X)
			if(this.is("sp3", 1, 2, null, null) && bone1.bondtype==1 && X.is(2, 1, true,  "H") && Y.is(1, 1, true,  "O")                    ){ skeletonCode += "h"; continue; } // -C(H)(H)(O)
			if(this.is("sp3", 1, 2, null, null) && bone1.bondtype==1 && X.is(2, 1, true,  "H") && Y.is(1, 1, false, "H", "O")               ){ skeletonCode += "H"; continue; } // -C(X)(X)(H)
			if(this.is("sp3", 1, 2, null, null) && bone1.bondtype==1 && X.is(2, 1, true,  "O") && Y.is(1, 1, false, "O")                    ){ skeletonCode += "b"; continue; } // -C(O)(O)(Y)
			if(this.is("sp3", 1, 2, null, null) && bone1.bondtype==1 && X.is(2, 1, false, "H") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "W"; continue; } // -C(X)(X)(H)
			if(this.is("sp3", 1, 2, null, null) && bone1.bondtype==1 && X.is(2, 1, true)       && Y.is(1, 1, true)                          ){ skeletonCode += "L"; continue; } // -C(X)(X)(Y)
			if(this.is("sp3", 1, 3, "S",  true) && bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true)      ){ skeletonCode += "1"; continue; } // -C(X)(Y)(Z)
			if(this.is("sp3", 1, 3, "R",  true) && bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true)      ){ skeletonCode += "2"; continue; } // -C(X)(Y)(Z)
			if(this.is("sp3", 1, 3, "l",  true) && bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true)      ){ skeletonCode += "3"; continue; } // -C(X)(Y)(Z)
			if(this.is("sp3", 1, 3, "r",  true) && bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true)      ){ skeletonCode += "4"; continue; } // -C(X)(Y)(Z)
			if(this.is("sp3", 1, 3, "X",  true) && bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true)      ){ skeletonCode += "X"; continue; } // -C(X)(Y)(Z)
			if(this.is("sp3", 1, 3, null, false)&& bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true,  "H")){ skeletonCode += "U"; continue; } // -C(X)(Y)(Z)
			if(this.is("sp3", 1, 3, null, false)&& bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, false, "H")){ skeletonCode += "R"; continue; } // -C(X)(Y)(Z)
			if(this.is("sp2", 1, 2, null, null) && bone1.bondtype==1 && X.is(1, 2, true,  "O") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "o"; continue; } // -C(=O)(H)
			if(this.is("sp2", 1, 2, null, null) && bone1.bondtype==1 && X.is(1, 2, true,  "O") && Y.is(1, 1, true,  "O")                    ){ skeletonCode += "a"; continue; } // -C(=O)(O)
			if(this.is("sp2", 1, 2, null, null) && bone1.bondtype==1 && X.is(1, 2, false, "O") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "O"; continue; } // -C(=X)(H)
			if(this.is("sp2", 1, 2, null, null) && bone1.bondtype==1 && X.is(1, 2, true)       && Y.is(1, 1, true)                          ){ skeletonCode += "A"; continue; } // -C(=X)(Y)
			if(this.is("sp2", 1, 1, null, null) && bone1.bondtype==2 && X.is(2, 1, true,  "H")                                              ){ skeletonCode += "v"; continue; } // -C(H)(H)
			if(this.is("sp2", 1, 1, null, null) && bone1.bondtype==2 && X.is(2, 1, true,  "O")                                              ){ skeletonCode += "c"; continue; } // -C(O)(O)
			if(this.is("sp2", 1, 1, null, null) && bone1.bondtype==2 && X.is(2, 1, false, "H", "O")                                         ){ skeletonCode += "V"; continue; } // -C(X)(X)
			if(this.is("sp2", 1, 2, "E",  null) && bone1.bondtype==2 && X.is(1, 1, false, "H") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "G"; continue; } // -C(X)(H)
			if(this.is("sp2", 1, 2, "Z",  null) && bone1.bondtype==2 && X.is(1, 1, false, "H") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "I"; continue; } // -C(X)(H)
			if(this.is("sp2", 1, 2, "N",  null) && bone1.bondtype==2 && X.is(1, 1, false, "H") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "P"; continue; } // -C(X)(H)
			if(this.is("sp2", 1, 2, "X",  null) && bone1.bondtype==2 && X.is(1, 1, false, "H") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "J"; continue; } // -C(X)(H)
			if(this.is("sp2", 1, 2, null, null) && bone1.bondtype==2 && X.is(1, 1, true)       && Y.is(1, 1, true)                          ){ skeletonCode += "C"; continue; } // -C(X)(H)
			if(this.is("sp",  1, 1, null, null) && bone1.bondtype==1 && X.is(1, 3, true)                                                    ){ skeletonCode += "Y"; continue; } // -C(#X)
			if(this.is("sp",  1, 1, null, null) && bone1.bondtype==2 && X.is(1, 2, true)                                                    ){ skeletonCode += "q"; continue; } // =C(=X)
			if(this.is("sp",  1, 1, null, null) && bone1.bondtype==3 && X.is(1, 1, true,  "H")                                              ){ skeletonCode += "t"; continue; } // #C(H)
			if(this.is("sp",  1, 1, null, null) && bone1.bondtype==3 && X.is(1, 1, false, "H")                                              ){ skeletonCode += "T"; continue; } // #C(X)

			// Non terminal
			if(this.is("sp3", 2, 1, null, null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(2, 1, true,  "O")                    ){ skeletonCode += "b"; continue; } // -C(O)(O)-
			if(this.is("sp3", 2, 1, null, null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(2, 1, true,  "H")                    ){ skeletonCode += "d"; continue; } // -C(H)(H)-
			if(this.is("sp3", 2, 1, null, null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(2, 1, false, "H", "O")               ){ skeletonCode += "D"; continue; } // -C(X)(X)-
			if(this.is("sp3", 2, 2, "S",  null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 1, true) && Y.is(1, 1, true)      ){ skeletonCode += "1"; continue; } // -C(X)(Y)-
			if(this.is("sp3", 2, 2, "R",  null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 1, true) && Y.is(1, 1, true)      ){ skeletonCode += "2"; continue; } // -C(X)(Y)-
			if(this.is("sp3", 2, 2, "l",  null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 1, true) && Y.is(1, 1, true)      ){ skeletonCode += "3"; continue; } // -C(X)(Y)-
			if(this.is("sp3", 2, 2, "r",  null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 1, true) && Y.is(1, 1, true)      ){ skeletonCode += "4"; continue; } // -C(X)(Y)-
			if(this.is("sp3", 2, 2, "X",  null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 1, true) && Y.is(1, 1, true)      ){ skeletonCode += "X"; continue; } // -C(X)(Y)-
			if(this.is("sp2", 2, 1, null, null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 2, true,  "O")                    ){ skeletonCode += "k"; continue; } // -C(=O)-
			if(this.is("sp2", 2, 1, null, null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 2, false, "O")                    ){ skeletonCode += "K"; continue; } // -C(=X)-
			if(this.is("sp2", 2, 1, "E",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, true,  "H")                    ){ skeletonCode += "e"; continue; } // =C(H)-
			if(this.is("sp2", 2, 1, "Z",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, true,  "H")                    ){ skeletonCode += "z"; continue; } // =C(H)-
			if(this.is("sp2", 2, 1, "N",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, true,  "H")                    ){ skeletonCode += "n"; continue; } // =C(H)-
			if(this.is("sp2", 2, 1, "X",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, true,  "H")                    ){ skeletonCode += "f"; continue; } // =C(H)-
			if(this.is("sp2", 2, 1, "E",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, false, "H")                    ){ skeletonCode += "E"; continue; } // =C(X)-
			if(this.is("sp2", 2, 1, "Z",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, false, "H")                    ){ skeletonCode += "Z"; continue; } // =C(X)-
			if(this.is("sp2", 2, 1, "N",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, false, "H")                    ){ skeletonCode += "N"; continue; } // =C(X)-
			if(this.is("sp2", 2, 1, "X",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, false, "H")                    ){ skeletonCode += "F"; continue; } // =C(X)-
			if(this.is("sp",  2, 0, null, null) && bone1.bondtype==2 && bone2.bondtype==2                                                  ){ skeletonCode += "q"; continue; } // =C=
			if(this.is("sp",  2, 0, null, null) && bone1.bondtype==3 && bone2.bondtype==1                                                  ){ skeletonCode += "y"; continue; } // #C-

			skeletonCode += "?";
		}
		return skeletonCode;
	}

	private LinkedList<ConnectType> getConnectTypes(final Atom atom, final Backbone backbone){
		// Collect unique connect
		for(Connection connect : atom.getConnections()){
			this.addUniqueConnect(connect, backbone);
		}
		// Set hybrid orbital of the atom
		AtomIdentifier ident = new AtomIdentifier();
		ident.setAtom(atom);
		this.hybridOrbital = ident.getHybridOrbital();

		// Count number of backbone and modification
		this.uniqBackboneNum = 0;
		this.uniqModNum = 0;
		for(ConnectType type : this.connectTypes){
			if(type.isBackbone){
				this.uniqBackboneNum++;
			}else{
				this.uniqModNum++;
			}
		}

		// Count max number of connected backbone
		this.maxConnectBackboneNum = 0;
		for(ConnectType type : this.connectTypes){
			if(type.isBackbone) continue;
			this.maxConnectBackboneNum = Math.max(this.maxConnectBackboneNum, type.connectedBackboneNum);
		}

		Collections.sort(this.connectTypes, new Comparator<ConnectType>() {
			public int compare(ConnectType type1, ConnectType type2) {
				if( type1.isBackbone           != type2.isBackbone          ) return (type1.isBackbone) ? -1 : 1;
				if( type1.connects.size()      != type2.connects.size()     ) return type2.connects.size() - type1.connects.size();
				if( type1.bondtype             != type2.bondtype            ) return type2.bondtype - type1.bondtype;
				if(!type1.symbol.equals(type2.symbol)                       ) return Chemical.getAtomicNumber(type2.symbol) - Chemical.getAtomicNumber(type1.symbol);
				if( type1.connectedBackboneNum != type2.connectedBackboneNum) return type2.connectedBackboneNum - type1.connectedBackboneNum;
				return 0; // ここには入らないはず。
			}
		});
		return this.connectTypes;
	}

	//----------------------------
	// Public method
	//----------------------------
	// 追加済みの結合と同一タイプの結合であれば、カウントアップのみを行い。
	// 新しいタイプの結合であれば、新規にリストに追加する。
	// 主鎖炭素との結合は、SkeletoneCode割り当て時の判定文を短くする為、別とみなす。
	private void addUniqueConnect(final Connection connect, final Backbone backbone){
		// 修飾の場合は、同一タイプは一つにまとめる。
		if(!backbone.contains(connect.endAtom())){
			for(ConnectType type : this.connectTypes){
				if(type.isBackbone                                             ) continue;
				if(type.bondtype != connect.getBond().getType()                ) continue;
				if(!type.symbol.equals(connect.endAtom().getSymbol())          ) continue;
				if(type.connectedBackboneNum != this.countConnect(connect.endAtom(), backbone) ) continue;
				type.connects.add(connect);
				return;
			}
		}

		// 主鎖もしくは新規修飾の場合は新タイプを追加し、connectを新タイプに追加する。
		ConnectType type =  new ConnectType(connect, backbone);
		type.connects.add(connect);
		this.connectTypes.add(type);
	}

	public boolean is(final String hybridOrbital, final Integer backboneNum, final Integer uniqModNum, final String stereo, final Boolean isConnectMultiChains){
		if((hybridOrbital        != null) && (!this.hybridOrbital.equals(hybridOrbital)               )) return false;
		if((backboneNum          != null) && ( this.uniqBackboneNum != backboneNum                    )) return false;
		if((uniqModNum           != null) && ( this.uniqModNum   != uniqModNum                        )) return false;
		if((stereo               != null) && (!this.stereo.equals(stereo)                             )) return false;
		if((isConnectMultiChains != null) && ((this.maxConnectBackboneNum > 1) != isConnectMultiChains)) return false;
		return true;
	}

	// IUPACを参考にして、Stereo判定を行う対象かどうかのチェックをまず入れる。
	// ここでのStereoはIUPACのStereoの決定方法とは異なるので注意
	// 糖鎖の主鎖炭素と隣接原子の種類が等しければ、同一のSKeletoneCodeを出力させたい
	// この為、主鎖炭素と隣接する原子のみを対象として、立体判定を行っている。
	// また、EZ判定では、主鎖炭素の向きが2重結合の両端で等しいかどうかを判定基準として用いている。
	// WURCSを生成する過程において、修飾付加記号の部分で化合物全体の立体が一意になるようにする。
	private void setStereo(final Atom atom, final Backbone backbone, final ConnectType backbone1, final ConnectType backbone2, final ConnectType mod1, final ConnectType mod2, final ConnectType mod3){
		this.stereo = "";

		// 楔の状態等からStereo判定対象かどうかの確認

		// sp3 terminal
		if(this.is("sp3", 1, 3, null, true)){
			Connection connectBackbone1 = backbone1.connects.get(0);
			Connection M0 = null;
			Connection M1 = null;
			Connection M2 = null;
			if(mod3.connectedBackboneNum > 1){ M0 = mod3.connects.get(0); M1 = mod1.connects.get(0); M2 = mod2.connects.get(0); }
			if(mod2.connectedBackboneNum > 1){ M0 = mod2.connects.get(0); M1 = mod1.connects.get(0); M2 = mod3.connects.get(0); }
			if(mod1.connectedBackboneNum > 1){ M0 = mod1.connects.get(0); M1 = mod2.connects.get(0); M2 = mod3.connects.get(0); }
			if(backbone.indexOf(connectBackbone1.endAtom()) < backbone.indexOf(atom)){
				this.stereo = Chemical.sp3stereo(connectBackbone1, M0, M1, M2);
			}else{
				this.stereo = Chemical.sp3stereo(M0, connectBackbone1, M1, M2);
			}
			return;
		}

		// sp3 non terminal
		if(this.is("sp3", 2, 2, null, null)){
			Connection connectBackbone1 = backbone1.connects.get(0);
			Connection connectBackbone2 = backbone2.connects.get(0);
			Connection M1 = mod1.connects.get(0);
			Connection M2 = mod2.connects.get(0);
			if(backbone.indexOf(connectBackbone1.endAtom()) < backbone.indexOf(connectBackbone2.endAtom())){
				this.stereo = Chemical.sp3stereo(connectBackbone1, connectBackbone2, M1, M2);
			}else{
				this.stereo = Chemical.sp3stereo(connectBackbone2, connectBackbone1, M1, M2);
			}
			return;
		}

		// sp2 terminal
		if(this.is("sp2", 1, 2, null, null) && backbone1.bondtype == 2){
			Atom A0 = atom;
			Atom B0 = backbone1.connects.get(0).endAtom();
			Atom A1 = mod1.connects.get(0).endAtom();
			Atom B1 = null;
			for(Connection connect : B0.getConnections()){
				Atom conatom = connect.endAtom();
				if(conatom == A0) continue;
				if(!backbone.contains(conatom)) continue;
				B1 = conatom;
				break;
			}
			this.stereo = Chemical.sp2stereo(A0, A1, B0, B1);
			return;
		}

		// sp2 non terminal
		if(this.is("sp2", 2, 1, null, null) && backbone1.bondtype == 2){
			Atom A0 = atom;
			Atom B0 = backbone1.connects.get(0).endAtom();
			Atom A1 = backbone2.connects.get(0).endAtom();
			Atom B1 = null;
			for(Connection connect : B0.getConnections()){
				Atom conatom = connect.endAtom();
				if(conatom == A0) continue;
				if(!backbone.contains(conatom)) continue;
				B1 = conatom;
				break;
			}
			this.stereo = Chemical.sp2stereo(A0, A1, B0, B1);
			return;
		}
	}

	/**
	 * @param atom
	 * @param atoms
	 * @return the number of connections which connect input atoms
	 */
	private int countConnect(Atom atom, final LinkedList<Atom> atoms){
		int num = 0;
		for(Connection connection : atom.getConnections()){
			if(atoms.contains(connection.endAtom())) num++;
		}
		return num;
	}
}
