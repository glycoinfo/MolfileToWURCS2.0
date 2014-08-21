package chemicalgraph.subgraph.backbone;

import chemicalgraph.Atom;
import chemicalgraph.AtomList;
import chemicalgraph.Connection;
import chemicalgraph.ConnectionList;

/**
 * Class for backbone
 * @author KenichiTanaka
 * @author IssakuYAMADA
 */
public class Backbone extends AtomList{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;
	public int type = 0;
	public boolean isBackbone = false;
	public String oxidationSequence = null;
	public String coOCOSequence = null;
	public String skeletonCode;
	public ConnectionList connectsBackboneToModification;
	public int ECnumber;
	public int initialECnumber;
	public int tmp;

	//----------------------------
	// Public method (void)
	//----------------------------
	public void setPiCyclic(final boolean val){
		for(Atom atom : this){
			atom.isPiCyclic = val;
		}
	}
	
	public void setCarbonCyclic(final boolean val){
		for(Atom atom : this){
			atom.isCarbonCyclic = val;
		}
	}
	
	public void setOxidationSequence(){
		this.oxidationSequence = "";
		for(Atom atom : this){
			this.oxidationSequence += atom.oxidationNumber();
		}
	}

	public void setcoOCOSequence(final int minBackboneLength){
		this.coOCOSequence = "";
		int ii = 0;
		for(Atom atom : this){
			ii++;
			this.coOCOSequence += (!atom.isCarboxyLike() && (atom.isKetoneLike() || atom.isAldehydeLike() || atom.isKetalLike() || atom.isAcetalLike())) ? "1" : "0";
			if(ii>=minBackboneLength) break;
		}
	}
	
	public void aromatize(){
		if(this.size() == 0){
			System.err.println("please set start atom.");
			return;
		}
		if(this.getLast().pi == 0) return;
		
		if(this.isCyclic()){
			if(this.isSatisfiedHuckelsRule()){
				this.setAromatic(true);
			}
			return;
		}
		
		// depth search
		for(Connection connect : this.getLast().connections){
			Atom conAtom = connect.atom;
			if(this.contains(conAtom) && (conAtom != this.getFirst())) continue;
			if(this.contains(conAtom) && this.size() < 3) continue;
			this.addLast(conAtom);
			this.aromatize();
			this.removeLast();
		}
	}
	
	public void piCyclic(){
		if(this.size() == 0){
			System.err.println("please set start atom.");
			return;
		}
		if(this.getLast().pi == 0) return;
		
		if(this.isCyclic()){
			this.setPiCyclic(true);
			return;
		}
		
		// depth search
		for(Connection connect : this.getLast().connections){
			Atom conAtom = connect.atom;
			if(this.contains(conAtom) && (conAtom != this.getFirst())) continue;
			if(this.contains(conAtom) && this.size() < 3) continue;
			this.addLast(conAtom);
			this.piCyclic();
			this.removeLast();
		}
	}

	public void carbonCyclic(){
		if(this.size() == 0){
			System.err.println("please set start atom.");
			return;
		}
		if(!this.getLast().symbol.equals("C")) return;
		
		if(this.isCyclic()){
			this.setCarbonCyclic(true);
			return;
		}
		
		// depth search
		for(Connection connect : this.getLast().connections){
			Atom conAtom = connect.atom;
			if(this.contains(conAtom) && (conAtom != this.getFirst())) continue;
			if(this.contains(conAtom) && this.size() < 3) continue;
			this.addLast(conAtom);
			this.carbonCyclic();
			this.removeLast();
		}
	}

	public void convertCandidateBackbone(){
		// determination of C1 position
		Atom HeadCarbon = this.getFirst();
		Atom CyclicEtherCarbon = this.getFirstCyclicEtherCarbon();
		Atom C1 = null;
		if(CyclicEtherCarbon == null){
			C1 = HeadCarbon;
		}else if(HeadCarbon.isAldehydeLike() || HeadCarbon.connections.containsTwoNOS()){
			C1 = HeadCarbon;
		}else if(!CyclicEtherCarbon.isKetalLike()){
			C1 = HeadCarbon;
		}else{
			Backbone branch = new Backbone();
			for(Atom atom : this){
				if(atom == CyclicEtherCarbon) break;
				branch.add(atom);
			}
			C1 = (branch.isMonosaccharideLike()) ? HeadCarbon : CyclicEtherCarbon;
		}

		// shorten candidate backbone
		while(this.getFirst()!=C1){
			this.removeFirst();
		}
	}
	
	public void findCOLINs(){
		this.connectsBackboneToModification = new ConnectionList();
		for(Atom atom : this){
			for(Connection connect : atom.connections){
				if(connect.atom.isBackbone()) continue;
				if(connect.atom.symbol.equals("H")) continue;
				if(this.connectsBackboneToModification.contains(connect)) continue;
				this.connectsBackboneToModification.addLast(connect);
			}
		}
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * Backboneに含まれる2炭素が、一つのヘテロ原子で繋がっていた場合、ヘテロ原子に繋がっている2炭素の内、リストの最初に現れる炭素を返す。
	 * <pre>
	 * C0-C1-C2-C3-C4-C5
	 *    |        |
	 *    O--------+
	 * 上記の場合、C1を返す。
	 * </pre>
	 * @return the first carbon which connect hetero atom of cyclic ether
	 */
	public Atom getFirstCyclicEtherCarbon(){
		for(Atom atom1 : this){
			for(Connection connect1 : atom1.connections){
				if(connect1.atom.symbol.equals("C")) continue;
				if(this.contains(connect1.atom)) continue;
				for(Connection connect2 : connect1.atom.connections){
					if(connect2.atom == atom1) continue;
					if(this.contains(connect2.atom)){
						return atom1;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Return the skeletone code of this backbone.
	 * @return the skeletone code of this backbone.
	 */
	public String toSkeletonCode(){
		String skeletonCode = "";
		// 主鎖炭素の文字コードを付加
		for(Atom atom : this){
			ConnectTypeList types = new ConnectTypeList(atom, this);
			ConnectType bone1 = (types.uniqBackboneNum > 0) ? types.get(0) : null;
			ConnectType bone2 = (types.uniqBackboneNum > 1) ? types.get(1) : null;
			ConnectType X = (types.uniqModNum > 0) ? types.get(types.uniqBackboneNum + 0) : null;
			ConnectType Y = (types.uniqModNum > 1) ? types.get(types.uniqBackboneNum + 1) : null;
			ConnectType Z = (types.uniqModNum > 2) ? types.get(types.uniqBackboneNum + 2) : null;
			
			types.setStereo(atom, this, bone1, bone2, X, Y, Z);
			
			// types.is(軌道の種類, 主鎖との結合数, ユニークな修飾の数, 立体情報, 複数の主鎖炭素と結合している修飾が存在する(つまり環状の糖の先頭炭素か末端炭素の認識))
			// nullの場合は、該当する項目をチェックしない
			// ここでは、主鎖炭素と直接つながっている1原子のみを修飾として見ている点に注意
			// Terminal
			if(types.is("sp3", 1, 1, null, null) && bone1.bondtype==1 && X.is(3, 1, true,  "H")                                              ){ skeletonCode += "m"; continue; } // -C(H)(H)(H)
			if(types.is("sp3", 1, 1, null, null) && bone1.bondtype==1 && X.is(3, 1, false, "H")                                              ){ skeletonCode += "M"; continue; } // -C(X)(X)(X)
			if(types.is("sp3", 1, 2, null, null) && bone1.bondtype==1 && X.is(2, 1, true,  "H") && Y.is(1, 1, true,  "O")                    ){ skeletonCode += "h"; continue; } // -C(H)(H)(O)
			if(types.is("sp3", 1, 2, null, null) && bone1.bondtype==1 && X.is(2, 1, true,  "H") && Y.is(1, 1, false, "H", "O")               ){ skeletonCode += "H"; continue; } // -C(X)(X)(H)
			if(types.is("sp3", 1, 2, null, null) && bone1.bondtype==1 && X.is(2, 1, true,  "O") && Y.is(1, 1, false, "O")                    ){ skeletonCode += "b"; continue; } // -C(O)(O)(Y)
			if(types.is("sp3", 1, 2, null, null) && bone1.bondtype==1 && X.is(2, 1, false, "H") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "W"; continue; } // -C(X)(X)(H)
			if(types.is("sp3", 1, 2, null, null) && bone1.bondtype==1 && X.is(2, 1, true)       && Y.is(1, 1, true)                          ){ skeletonCode += "L"; continue; } // -C(X)(X)(Y)
			if(types.is("sp3", 1, 3, "S",  true) && bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true)      ){ skeletonCode += "1"; continue; } // -C(X)(Y)(Z)
			if(types.is("sp3", 1, 3, "R",  true) && bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true)      ){ skeletonCode += "2"; continue; } // -C(X)(Y)(Z)
			if(types.is("sp3", 1, 3, "l",  true) && bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true)      ){ skeletonCode += "3"; continue; } // -C(X)(Y)(Z)
			if(types.is("sp3", 1, 3, "r",  true) && bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true)      ){ skeletonCode += "4"; continue; } // -C(X)(Y)(Z)
			if(types.is("sp3", 1, 3, "X",  true) && bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true)      ){ skeletonCode += "X"; continue; } // -C(X)(Y)(Z)
			if(types.is("sp3", 1, 3, null, false)&& bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, true,  "H")){ skeletonCode += "U"; continue; } // -C(X)(Y)(Z)
			if(types.is("sp3", 1, 3, null, false)&& bone1.bondtype==1 && X.is(1, 1, true)       && Y.is(1, 1, true) && Z.is(1, 1, false, "H")){ skeletonCode += "R"; continue; } // -C(X)(Y)(Z)
			if(types.is("sp2", 1, 2, null, null) && bone1.bondtype==1 && X.is(1, 2, true,  "O") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "o"; continue; } // -C(=O)(H)
			if(types.is("sp2", 1, 2, null, null) && bone1.bondtype==1 && X.is(1, 2, true,  "O") && Y.is(1, 1, true,  "O")                    ){ skeletonCode += "a"; continue; } // -C(=O)(O)
			if(types.is("sp2", 1, 2, null, null) && bone1.bondtype==1 && X.is(1, 2, false, "O") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "O"; continue; } // -C(=X)(H)
			if(types.is("sp2", 1, 2, null, null) && bone1.bondtype==1 && X.is(1, 2, true)       && Y.is(1, 1, true)                          ){ skeletonCode += "A"; continue; } // -C(=X)(Y)
			if(types.is("sp2", 1, 1, null, null) && bone1.bondtype==2 && X.is(2, 1, true,  "H")                                              ){ skeletonCode += "v"; continue; } // -C(H)(H)
			if(types.is("sp2", 1, 1, null, null) && bone1.bondtype==2 && X.is(2, 1, true,  "O")                                              ){ skeletonCode += "c"; continue; } // -C(O)(O)
			if(types.is("sp2", 1, 1, null, null) && bone1.bondtype==2 && X.is(2, 1, false, "H", "O")                                         ){ skeletonCode += "V"; continue; } // -C(X)(X)
			if(types.is("sp2", 1, 2, "E",  null) && bone1.bondtype==2 && X.is(1, 1, false, "H") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "G"; continue; } // -C(X)(H)
			if(types.is("sp2", 1, 2, "Z",  null) && bone1.bondtype==2 && X.is(1, 1, false, "H") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "I"; continue; } // -C(X)(H)
			if(types.is("sp2", 1, 2, "N",  null) && bone1.bondtype==2 && X.is(1, 1, false, "H") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "P"; continue; } // -C(X)(H)
			if(types.is("sp2", 1, 2, "X",  null) && bone1.bondtype==2 && X.is(1, 1, false, "H") && Y.is(1, 1, true,  "H")                    ){ skeletonCode += "J"; continue; } // -C(X)(H)
			if(types.is("sp2", 1, 2, null, null) && bone1.bondtype==2 && X.is(1, 1, true)       && Y.is(1, 1, true)                          ){ skeletonCode += "C"; continue; } // -C(X)(H)
			if(types.is("sp",  1, 1, null, null) && bone1.bondtype==1 && X.is(1, 3, true)                                                    ){ skeletonCode += "Y"; continue; } // -C(#X)
			if(types.is("sp",  1, 1, null, null) && bone1.bondtype==2 && X.is(1, 2, true)                                                    ){ skeletonCode += "q"; continue; } // =C(=X)
			if(types.is("sp",  1, 1, null, null) && bone1.bondtype==3 && X.is(1, 1, true,  "H")                                              ){ skeletonCode += "t"; continue; } // #C(H)
			if(types.is("sp",  1, 1, null, null) && bone1.bondtype==3 && X.is(1, 1, false, "H")                                              ){ skeletonCode += "T"; continue; } // #C(X)

			// Non terminal
			if(types.is("sp3", 2, 1, null, null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(2, 1, true,  "O")                    ){ skeletonCode += "b"; continue; } // -C(O)(O)-
			if(types.is("sp3", 2, 1, null, null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(2, 1, true,  "H")                    ){ skeletonCode += "d"; continue; } // -C(H)(H)-
			if(types.is("sp3", 2, 1, null, null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(2, 1, false, "H", "O")               ){ skeletonCode += "D"; continue; } // -C(X)(X)-
			if(types.is("sp3", 2, 2, "S",  null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 1, true) && Y.is(1, 1, true)      ){ skeletonCode += "1"; continue; } // -C(X)(Y)-
			if(types.is("sp3", 2, 2, "R",  null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 1, true) && Y.is(1, 1, true)      ){ skeletonCode += "2"; continue; } // -C(X)(Y)-
			if(types.is("sp3", 2, 2, "l",  null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 1, true) && Y.is(1, 1, true)      ){ skeletonCode += "3"; continue; } // -C(X)(Y)-
			if(types.is("sp3", 2, 2, "r",  null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 1, true) && Y.is(1, 1, true)      ){ skeletonCode += "4"; continue; } // -C(X)(Y)-
			if(types.is("sp3", 2, 2, "X",  null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 1, true) && Y.is(1, 1, true)      ){ skeletonCode += "X"; continue; } // -C(X)(Y)-
			if(types.is("sp2", 2, 1, null, null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 2, true,  "O")                    ){ skeletonCode += "k"; continue; } // -C(=O)-
			if(types.is("sp2", 2, 1, null, null) && bone1.bondtype==1 && bone2.bondtype==1     && X.is(1, 2, false, "O")                    ){ skeletonCode += "K"; continue; } // -C(=X)-
			if(types.is("sp2", 2, 1, "E",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, true,  "H")                    ){ skeletonCode += "e"; continue; } // =C(H)-
			if(types.is("sp2", 2, 1, "Z",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, true,  "H")                    ){ skeletonCode += "z"; continue; } // =C(H)-
			if(types.is("sp2", 2, 1, "N",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, true,  "H")                    ){ skeletonCode += "n"; continue; } // =C(H)-
			if(types.is("sp2", 2, 1, "X",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, true,  "H")                    ){ skeletonCode += "f"; continue; } // =C(H)-
			if(types.is("sp2", 2, 1, "E",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, false, "H")                    ){ skeletonCode += "E"; continue; } // =C(X)-
			if(types.is("sp2", 2, 1, "Z",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, false, "H")                    ){ skeletonCode += "Z"; continue; } // =C(X)-
			if(types.is("sp2", 2, 1, "N",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, false, "H")                    ){ skeletonCode += "N"; continue; } // =C(X)-
			if(types.is("sp2", 2, 1, "X",  null) && bone1.bondtype==2 && bone2.bondtype==1     && X.is(1, 1, false, "H")                    ){ skeletonCode += "F"; continue; } // =C(X)-
			if(types.is("sp",  2, 0, null, null) && bone1.bondtype==2 && bone2.bondtype==2                                                  ){ skeletonCode += "q"; continue; } // =C=
			if(types.is("sp",  2, 0, null, null) && bone1.bondtype==3 && bone2.bondtype==1                                                  ){ skeletonCode += "y"; continue; } // #C-

			skeletonCode += "?";
		}
		return skeletonCode;
	}

	public boolean isMonosaccharideLike(){
		return this.size() - this.countNOSconnected() < 2;
	}

	public boolean isCyclic(){
		return (this.size() > 2) && (this.getLast() == this.getFirst());
	}
	
	public boolean hasConnectsWithAglycon(){
		for(Connection connect : this.connectsBackboneToModification){
			if(connect.atom.isAglycone()) return true;
		}
		return false;
	}
	
	public int countNOSconnected(){
		int NOSNum = 0;
		for(Atom atom : this){
			for(Connection connect : atom.connections){
				if(connect.atom.symbol.equals("N") || connect.atom.symbol.equals("O") || connect.atom.symbol.equals("S")){
					NOSNum++;
					break;
				}
			}
		}
		return NOSNum;
	}

	public int countSingleBondOxygenConnected() {
		int num = 0;
		for(Atom atom : this){
			if(atom.isCarboxyLike()) continue;
			for(Connection connect : atom.connections){
				if(this.contains(connect.atom)) continue;
				if(connect.atom.symbol.equals("O")&&connect.bond.type==1){
					num++;
					break;
				}
			}
		}
		return num;
	}
	
	public int compareTo(final Backbone target){
		// C1にketoneLike, aldehideLike, KetalLike, acetalLike 構造を持ち、CarboxylLikeでないかどうかの0,1からなる文字列を、辞書順で降順
		int coOCOSequenceCheck = target.coOCOSequence.compareTo(this.coOCOSequence);
		if(coOCOSequenceCheck != 0)	return coOCOSequenceCheck;
		
		// C1が環の一部(C1がヘテロ原子を挟んでBackboneの他の炭素に結合している)であるBackboneを優先
		if(this.getFirst() == this.getFirstCyclicEtherCarbon() && target.getFirst() != target.getFirstCyclicEtherCarbon()) return -1;
		if(this.getFirst() != this.getFirstCyclicEtherCarbon() && target.getFirst() == target.getFirstCyclicEtherCarbon()) return 1;
		
		// oxidationSequenceを辞書順で降順
		int oxidationSequenceCheck = target.oxidationSequence.compareTo(this.oxidationSequence);
		if(oxidationSequenceCheck != 0) return oxidationSequenceCheck;
		
		// これ以降、Backboneの長さは等しくなる。
		int backboneLen = this.size();
		for(int ii=0; ii<backboneLen; ii++){
			// initialize
			int[] atomNum1 = new int[200];
			int[] atomNum2 = new int[200];
			for(int jj=0; jj<200; jj++){
				atomNum1[jj] = 0;
				atomNum2[jj] = 0;
			}
			
			// Backboneのii番目の炭素に結合している原子の数を種類ごとにカウント
			for(Connection connect : this.get(ii).connections){
				atomNum1[connect.atom.atomicNumber()]++;
			}
			for(Connection connect : target.get(ii).connections){
				atomNum2[connect.atom.atomicNumber()]++;
			}
			
			// 窒素(原子番号7), 酸素(原子番号8), その他の原子（原子番号の大きな元素を優先）に結合数を比較
			if(atomNum1[7] != atomNum2[7]) return atomNum2[7] - atomNum1[7];
			if(atomNum1[8] != atomNum2[8]) return atomNum2[8] - atomNum1[8];
			for(int jj=199; jj>=0; jj--){
				if(atomNum1[jj] != atomNum2[jj]) return atomNum2[jj] - atomNum1[jj];
			}
		}
		
		//20140723 alditolなどで、D体が優先されるようにソート @Issaku YAMADA　ここから
		if (this.skeletonCode.length() > 2 && target.skeletonCode.length() > 2){
			int skchar = this.skeletonCode.length() - 1;
			int targetSkchar = target.skeletonCode.length() - 1;			
			int thisNum = 0;
			int targetNum = 0;
			try {
				thisNum = Integer.parseInt(this.skeletonCode.substring(skchar - 1, skchar));
			} catch (NumberFormatException nfex) {}
			try {
				targetNum = Integer.parseInt(target.skeletonCode.substring(targetSkchar -1, targetSkchar));
			} catch (NumberFormatException nfex) {}			
			return targetNum - thisNum;
		}
		//20140723 alditolなどで、D体が優先されるようにソート @Issaku YAMADA　ここまで
		
		// SkeletoneCodeを辞書順で昇順
		int skeletonCodeCheck = this.skeletonCode.compareTo(target.skeletonCode);
		if(skeletonCodeCheck != 0) return skeletonCodeCheck;
		
		return 0;
	}

	public Atom getAnomer() {
		for(Atom atom : this){
			for(Connection connect : atom.connections){
				if(connect.atom.isBackbone()) continue;
				if(connect.atom.connections.count(this)==1) continue;
				return atom;
			}
		}
		return this.getFirst();
	}
}
