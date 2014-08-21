package chemicalgraph.subgraph.molecule;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.backbone.BackboneList;

/**
 * @author KenichiTanaka
 */
public class MoleculeList extends LinkedList<Molecule>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;

	//----------------------------
	// Public method
	//----------------------------
	public void sortByCompareIDwithSkeletoneCode() {
		Collections.sort(this, new Comparator<Molecule>() {
			public int compare(Molecule mol1, Molecule mol2) {
				// 糖を認識できなかったケースを優先：JMSDBではInositol:13件
				if( mol1.backbones.size()==0 && mol2.backbones.size()>0 ) return -1;
				if( mol1.backbones.size()>0  && mol2.backbones.size()==0) return 1;
				
				// 多糖認識しているレコードを優先
				if( mol1.backbones.size()>1  && mol2.backbones.size()==1) return -1;
				if( mol1.backbones.size()==1 && mol2.backbones.size()>1 ) return 1;
				
				// 糖毎の候補が絞り込めていないレコードを優先
				int candidate1 = 0;
				for(BackboneList backbones : mol1.candidateBackboneGroups){
					for(Backbone backbone : backbones){
						if(backbone.isBackbone) candidate1++;
					}
				}
				int candidate2 = 0;
				for(BackboneList backbones : mol2.candidateBackboneGroups){
					for(Backbone backbone : backbones){
						if(backbone.isBackbone) candidate2++;
					}
				}
				if(candidate1 != candidate2) return candidate2 - candidate1;
				
				// 認識した糖の中に同一SkeletoneCodeを含むレコードを末尾に出力
				boolean bool1 = mol1.hasSameSkeletoneCodeWithID();
				boolean bool2 = mol2.hasSameSkeletoneCodeWithID();
				if(bool1 != bool2){
					return bool1 ? 1 : -1;
				}
				
				// IDが空のレコード（WEB版のURL_NAMEが空を想定）を先頭に出力
				if( mol1.ID.equals("") && !mol2.ID.equals("")) return -1;
				if(!mol1.ID.equals("") &&  mol2.ID.equals("")) return 1;
				
				// 炭素鎖の短いレコードを優先
				int backbonelen1 = (mol1.backbones.size()!=0) ? mol1.backbones.get(0).size() : 0;
				int backbonelen2 = (mol2.backbones.size()!=0) ? mol2.backbones.get(0).size() : 0;
				if(backbonelen1 != backbonelen2) return backbonelen1 - backbonelen2;
				
				// SkeletoneCodeの文字列ソートで昇順
				String skeletonCode1 = (mol1.backbones.size()!=0) ? mol1.backbones.get(0).skeletonCode : "";
				String skeletonCode2 = (mol2.backbones.size()!=0) ? mol2.backbones.get(0).skeletonCode : "";
				return skeletonCode1.compareTo(skeletonCode2);
			}
		});
	}
	
	public void sortByBackboneInfo() {
		Collections.sort(this, new Comparator<Molecule>() {
			public int compare(Molecule mol1, Molecule mol2) {
				// Backboneが一つもヒットしていない構造を最上位に表示
				int monosaccharideNum1 = mol1.candidateBackboneGroups.size();
				int monosaccharideNum2 = mol2.candidateBackboneGroups.size();
				if ((monosaccharideNum1 == 0 || monosaccharideNum2 == 0) && (monosaccharideNum1 != monosaccharideNum2)) {
					return monosaccharideNum1 - monosaccharideNum2;
				}

				// 複数のBackboneが得られている（絞り切れていない場合）単糖を含む構造を次に表示
				int multiBackboneNum1 = 0;
				int multiBackboneNum2 = 0;
				for (BackboneList backbones : mol1.candidateBackboneGroups) {
					if (backbones.size() > 1 && backbones.get(1).isBackbone) multiBackboneNum1++;
				}
				for (BackboneList backbones : mol2.candidateBackboneGroups) {
					if (backbones.size() > 1 && backbones.get(1).isBackbone) multiBackboneNum2++;
				}
				if ((multiBackboneNum1 > 0 && multiBackboneNum2 == 0) || (multiBackboneNum1 == 0 && multiBackboneNum2 > 0)) {
					return multiBackboneNum2 - multiBackboneNum1;
				}

				// 各ケース(Non backbone, multi backbone, one backbone)毎に、単糖の数でソート
				if (monosaccharideNum1 != monosaccharideNum2) {
					return monosaccharideNum1 - monosaccharideNum2;
				}
				
				// 先頭のOxidation sequenceを比較し辞書順の逆順でソート
				if(monosaccharideNum1 > 0 && monosaccharideNum2 > 0){
					return mol2.candidateBackboneGroups.getFirst().getFirst().oxidationSequence.compareTo(mol1.candidateBackboneGroups.getFirst().getFirst().oxidationSequence);
				}
				
				return 0;
			}
		});
	}
}
