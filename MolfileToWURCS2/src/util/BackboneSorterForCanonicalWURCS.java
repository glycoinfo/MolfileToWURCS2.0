package util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import sugar.chemicalgraph.Atom;
import sugar.chemicalgraph.Connection;
import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.backbone.BackboneList;
import chemicalgraph.subgraph.modification.Modification;

/**
 * Class for Backbone sorter for canonical WURCS
 *  XXX: From BackboneList
 * @author Masaaki Matsubara
 */
public class BackboneSorterForCanonicalWURCS {

	private LinkedList<Backbone> m_aBackboneList = null;

	/**
	 * Sort Backbone list
	 * XXX: From BackboneList.sortForCanonicalWURCS()
	 * @param a_aBackboneList
	 */
	public void sort(LinkedList<Backbone> a_aBackboneList) {
		if(a_aBackboneList.size()<2) return;
		this.m_aBackboneList = a_aBackboneList;

		// 糖鎖に対してEC番号を計算
		this.updateECnumber();

		// 探索開始ノードのソート
		Collections.sort(this.m_aBackboneList, new Comparator<Backbone>() {
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
		LinkedList<Backbone> pathBackbones = new LinkedList<Backbone>();
		while(true){
			// 複数の糖鎖が含まれる場合、未探索の糖から開始点を見つけ格納(一時対応)
			Backbone topBackbone = null;
			for(Backbone backbone : this.m_aBackboneList){
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
			if(pathBackbones.size() == this.m_aBackboneList.size()) break;
		}

		a_aBackboneList.clear();
		a_aBackboneList.addAll(pathBackbones);
	}

	//----------------------------
	// Private method (void)
	//----------------------------
	/**
	 * @param val
	 */
	private void setTmp(final int val){
		for(Backbone backbone : this.m_aBackboneList){
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
			for(Backbone backbone : this.m_aBackboneList){
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
		for(Backbone backbone : this.m_aBackboneList){
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
		int num = this.m_aBackboneList.size();
		for(ii=0; ii<num; ii++){
			for(jj=ii+1; jj<num; jj++){
				if(this.m_aBackboneList.get(ii).tmp == this.m_aBackboneList.get(jj).tmp) break;
			}
			if(jj==num){
				uniqNum++;
			}
		}
		return uniqNum;
	}
}
