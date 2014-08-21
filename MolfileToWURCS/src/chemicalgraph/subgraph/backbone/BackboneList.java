package chemicalgraph.subgraph.backbone;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.AtomList;
import chemicalgraph.Connection;
import chemicalgraph.subgraph.modification.Modification;

/**
 * @author KenichiTanaka
 */
public class BackboneList extends LinkedList<Backbone>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;
	
	//----------------------------
	// Constructor
	//----------------------------
	public BackboneList(){
	}

	public BackboneList(final AtomList atoms, final int minNOS, final int minO, final int minBackboneLength, final int maxBackboneLength, final float ratioBackboneNOS){
		BackboneList candidateBackboneList = new BackboneList();
		Backbone candidateBackbone = new Backbone();
		for(Atom atom : atoms){
			if(!atom.isTerminalCarbon()) continue;
			candidateBackbone.clear();
			candidateBackbone.add(atom);
			candidateBackboneList.searchCandidateBackbone(candidateBackbone, minNOS, minO, ratioBackboneNOS); //Issaku YAMADA
		}
		
		// C1判定を行い、2位以降の炭素がC1としてふさわしかった場合、炭素鎖を短縮する。
		candidateBackboneList.convertCandidateBackbone();
		
		// 炭素鎖長に関して条件を満たさない物は除外する。
		for(Backbone candidateBackbone2 : candidateBackboneList){
			int backboneLength = candidateBackbone2.size();
			if(minBackboneLength <= backboneLength && backboneLength <= maxBackboneLength){
				this.add(candidateBackbone2);
			}
		}
	}

	//----------------------------
	// Public method (void)
	//----------------------------
	public void searchCandidateBackbone(final Backbone candidateBackbone, final int minNOS, final int minO, float ratioBackboneNOS){ //Issaku YAMADA
		if(candidateBackbone.size() == 0){
			System.err.println("please set start atom.");
			return;
		}
		Atom tailAtom = candidateBackbone.getLast();
		if(!tailAtom.symbol.equals("C")) return;
		if( tailAtom.isAromatic) return;
		if( tailAtom.isPiCyclic) return;
		if( tailAtom.isCarbonCyclic) return;

		// add backbone to backbones
		if(candidateBackbone.size() != 1 && tailAtom.isTerminalCarbon()){
			if(candidateBackbone.countNOSconnected() < minNOS) return;
			if(candidateBackbone.countSingleBondOxygenConnected() < minO) return;
			
			//2014/07/28 Issaku YAMADA m_ratoBackboneNOS
			// Backbone炭素数をBackboneに結合しているNOSの数で割った値が > 2 であればBackboneに追加しない。
			System.err.println("candidateBackbone.size(): " + candidateBackbone.size());
			System.err.println("candidateBackbone.countNOSconnected(): " + candidateBackbone.countNOSconnected());
			if (candidateBackbone.size() / candidateBackbone.countNOSconnected()  > ratioBackboneNOS) return;
			
			
			Backbone newCandidateBackbone = new Backbone();
			for(Atom atom : candidateBackbone){
				newCandidateBackbone.addLast(atom);
			}
			this.add(newCandidateBackbone);
			return;
		}
		
		// depth search
		for(Connection connect : tailAtom.connections){
			Atom nextAtom = connect.atom;
			if(candidateBackbone.contains(nextAtom)) continue;
			candidateBackbone.addLast(nextAtom);
			this.searchCandidateBackbone(candidateBackbone, minNOS, minO, ratioBackboneNOS);  //Issaku YAMADA
			candidateBackbone.removeLast();
		}
	}

	/**
	 * 
	 */
	public void convertCandidateBackbone(){
		for(Backbone backbone : this){
			backbone.convertCandidateBackbone();
		}
	}

	/**
	 * @param minBackboneLength
	 */
	public void sortByMonoSaccharideBackboneLikeness(final int minBackboneLength){
		Collections.sort(this, new Comparator<Backbone>() {
			public int compare(Backbone backbone1, Backbone backbone2) {
				return backbone1.compareTo(backbone2);
			}
		});
		
	}
	
	/**
	 * 
	 */
	public void setBackboneFlag(){
		for(Backbone backbone : this){
			backbone.isBackbone = true;
		}
		// 主鎖として採用される可能性の残っていないbackboneにbackbone.isBackbone=falseを立てる。
		// イテレータを使った処理に変更出来たら後で対応する。
		int num = this.size();
		for(int ii=0; ii<num-1; ii++){
			Backbone backbone1 = this.get(ii);
			Backbone backbone2 = this.get(ii+1);
			int result = backbone1.compareTo(backbone2);
			if(result != 0){
				for(int jj=ii+1; jj<num; jj++){
					Backbone backbone3 = this.get(jj);
					backbone3.isBackbone = false;
				}
			}
		}
	}

	/**
	 * 
	 */
	public void setOxidationSequence(){
		for(Backbone backbone : this){
			backbone.setOxidationSequence();
		}
	}
	
	/**
	 * @param minBackboneLength
	 */
	public void setcoOCOSequence(final int minBackboneLength){
		for(Backbone backbone : this){
			backbone.setcoOCOSequence(minBackboneLength);
		}
	}
	
	/**
	 * 
	 */
	public void setSkeletoneCode(){
		for(Backbone backbone : this){
			backbone.skeletonCode = backbone.toSkeletonCode();
		}
	}

	/**
	 * 
	 */
	public void sortForCanonicalWURCS() {
		if(this.size()<2) return;
		
		// 糖鎖に対してEC番号を計算
		this.updateECnumber();
		
		// 探索開始ノードのソート
		Collections.sort(this, new Comparator<Backbone>() {
			public int compare(Backbone backbone1, Backbone backbone2) {
				Atom C1Backbone1 = backbone1.getFirst();
				Atom C1Backbone2 = backbone2.getFirst();
				
				//最初に還元末端を探索するコードを追加する。

				// １．Aglyconに結合しているBackboneを優先
				boolean backboneCheck1 = backbone1.hasConnectsWithAglycon();
				boolean backboneCheck2 = backbone2.hasConnectsWithAglycon();
				if( backboneCheck1 &&!backboneCheck2) return -1;
				if(!backboneCheck1 && backboneCheck2) return 1;
				
				// ２．C1と結合している他の糖の数が少ない糖を優先
				BackboneList connectBackboneList1 = new BackboneList();
				for(Connection connect : C1Backbone1.connections){
					if(!connect.atom.isModification()) continue;
					Modification mod = connect.atom.modification;
					for(Connection backboneToMod : mod.connectionsFromBackboneToModification){
						if(backboneToMod.start().backbone == backbone1) continue;
						if(connectBackboneList1.contains(backboneToMod.start().backbone)) continue;
						connectBackboneList1.add(backboneToMod.start().backbone);
					}
				}
				BackboneList connectBackboneList2 = new BackboneList();
				for(Connection connect : C1Backbone2.connections){
					if(!connect.atom.isModification()) continue;
					Modification mod = connect.atom.modification;
					for(Connection backboneToMod : mod.connectionsFromBackboneToModification){
						if(backboneToMod.start().backbone == backbone2) continue;
						if(connectBackboneList2.contains(backboneToMod.start().backbone)) continue;
						connectBackboneList2.add(backboneToMod.start().backbone);
					}
				}
				if(connectBackboneList1.size() != connectBackboneList2.size()) return connectBackboneList1.size() - connectBackboneList2.size();
				
				// ３．他の糖のC1との結合数の多い糖を優先
				int connectC1Num1 = 0;
				for(Connection connect : backbone1.getFirst().connections){
					if(!connect.atom.isModification()) continue;
					Modification mod = connect.atom.modification;
					for(Connection backboneToMod : mod.connectionsFromBackboneToModification){
						if(backboneToMod.start().backbone == backbone1) continue;
						if(backboneToMod.start().backbone.indexOf(backboneToMod.start()) == 0) connectC1Num1++;
					}
				}
				int connectC1Num2 = 0;
				for(Connection connect : backbone2.getFirst().connections){
					if(!connect.atom.isModification()) continue;
					Modification mod = connect.atom.modification;
					for(Connection backboneToMod : mod.connectionsFromBackboneToModification){
						if(backboneToMod.start().backbone == backbone2) continue;
						if(backboneToMod.start().backbone.indexOf(backboneToMod.start()) == 0) connectC1Num2++;
					}
				}
				if(connectC1Num1!=connectC1Num2) return connectC1Num2 - connectC1Num1;

				// ４．糖をノードとした糖鎖グラフに対して計算されたMorgan EC番号が小さい糖を優先
				if(backbone1.ECnumber != backbone2.ECnumber) return backbone1.ECnumber - backbone2.ECnumber;
				
				// ５．文字列ソート
				if(!backbone1.skeletonCode.equals(backbone2.skeletonCode)) return backbone1.skeletonCode.compareTo(backbone2.skeletonCode);

				// ６．糖をノードとした糖鎖グラフに対して、糖周りのCIPツリーを構築した後比較
				// 下記のコメントアウトの部分を参考にして、ここに糖鎖版HierarchicalDigraphの構築処理および比較処理を記述する。
				// System.out.print("\tcheck");
/*
				// ６．backbone1およびbackbone2のC1周りのCIPツリーを構築した後比較
				HierarchicalDigraph hd1 = new HierarchicalDigraph(null, backbone1.getFirst());
				HierarchicalDigraph hd2 = new HierarchicalDigraph(null, backbone2.getFirst());
				int result = hd1.compareTo(hd2);
				if(result!=0) return result;
*/
				
				return 0;
			}
		});
		
		// Pathの構築
		BackboneList pathBackbones = new BackboneList();
		while(true){
			// 複数の糖鎖が含まれる場合、未探索の糖から開始点を見つけ格納(一時対応)
			Backbone topBackbone = null;
			for(Backbone backbone : this){
				if(pathBackbones.contains(backbone)) continue;
				topBackbone = backbone;
				break;
			}
			
			// 連結している糖(つまり糖鎖)を取得
			while(true){
				if(topBackbone==null) break;
				pathBackbones.addFirst(topBackbone);
				
				topBackbone = null;
				for(Backbone backbone : pathBackbones){
					for(Connection connect : backbone.connectsBackboneToModification){
						if(connect.atom.isAglycone()) continue;
						for(Backbone connectedBackbone : connect.atom.modification.connectedBackbones){
							if(pathBackbones.contains(connectedBackbone)) continue;
							topBackbone = connectedBackbone;
							break;
						}
						if(topBackbone!=null) break;
					}
					if(topBackbone!=null) break;
				}
			}

			// 全てのbackboneの探索が完了したら終了
			if(pathBackbones.size() == this.size()) break;
		}
		
		this.clear();
		this.addAll(pathBackbones);
	}
	
	//----------------------------
	// Private method (void)
	//----------------------------
	/**
	 * @param val
	 */
	private void setTmp(final int val){
		for(Backbone backbone : this){
			backbone.tmp = val;
		}
	}
	
	/**
	 * 
	 */
	private void updateECnumber() {
		this.setTmp(1);
		int uniqUpdateECnumber = this.countUniqTmp();
		int uniqECnumber;
		do{
			this.copyTmpToECNumber();
			uniqECnumber = uniqUpdateECnumber;
			this.setTmp(0);
			for(Backbone backbone : this){
				for(Connection connect : backbone.connectsBackboneToModification){
					backbone.tmp += connect.start().backbone.ECnumber;
				}
			}
			uniqUpdateECnumber = this.countUniqTmp();
		}while(uniqECnumber < uniqUpdateECnumber);
	}

	/**
	 * 
	 */
	private void copyTmpToECNumber() {
		for(Backbone backbone : this){
			backbone.ECnumber = backbone.tmp;
		}
	}

	//----------------------------
	// Private method (non void)
	//----------------------------
	/**
	 * Return the number of unique tmp of list.
	 * @return the number of unique tmp of list.
	 */
	private int countUniqTmp() {
		int ii;
		int jj;
		int uniqNum = 0;
		int num = this.size();
		for(ii=0; ii<num; ii++){
			for(jj=ii+1; jj<num; jj++){
				if(this.get(ii).tmp == this.get(jj).tmp) break;
			}
			if(jj==num){
				uniqNum++;
			}
		}
		return uniqNum;
	}
}
