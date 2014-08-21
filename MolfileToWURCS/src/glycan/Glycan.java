package glycan;

import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.backbone.BackboneList;
import chemicalgraph.subgraph.modification.Modification;
import chemicalgraph.subgraph.modification.ModificationList;

/**
 * Glycan class
 * @author KenichiTanaka
 * @author IssakuYAMADA
 */
public class Glycan {
	//----------------------------
	// Member variable
	//----------------------------
	public BackboneList backbones = new BackboneList();
	public ModificationList modifications = new ModificationList();
	public String WURCS = null;
	
	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * 主鎖、結合情報、修飾の順にソート
	 */
	public void sortForCanonicalWURCS() {
		this.backbones.sortForCanonicalWURCS();
		for(Modification mod : this.modifications){
			mod.connectionsFromBackboneToModification.sortForCanonicalWURCS(this.backbones);
		}
		this.modifications.sortForCanonicalWURCS(this.backbones);
	}

	/**
	 * @param outputFullInformation
	 */
	public void generateWURCS(final boolean outputFullInformation){
		// count number of modifications which connects more than 2 backbone.
		int modNumConnectMultiBackbone = 0;
		for(Modification mod : this.modifications){
			if(mod.connectedBackbones.size()!=1) modNumConnectMultiBackbone++;
		}
		
		// WURCS生成
		this.WURCS = "WURCS=";
		
		// Version
		this.WURCS += "1.0";
		//this.WURCS += "1.0-BH13.13";
		
		// Output backbone & modification num
		this.WURCS += "/" + this.backbones.size() + "," + modNumConnectMultiBackbone + "/";

		// Output backbone
		for(Backbone backbone : this.backbones){
			this.WURCS += "[";
			this.WURCS += backbone.skeletonCode;
			for(Modification mod : this.modifications){
				// 出力中のbackboneのみに結合している修飾を出力
				if( mod.connectedBackbones.size()!=1) continue;
				if(!mod.connectedBackbones.contains(backbone)) continue;
				if( mod.isEllipseTarget()) continue;

				this.WURCS += "|";
				this.WURCS += mod.toCOLIN(this.backbones, outputFullInformation);
				this.WURCS += mod.toALIN(outputFullInformation);
			}
			this.WURCS += "]";
		}

		// Output other modifications
		int count = 0;
		for(Modification mod : this.modifications){
			if(mod.connectedBackbones.size()==1) continue;
			if(count>0) this.WURCS += "|";
			count++;
			this.WURCS += mod.toCOLIN(this.backbones, outputFullInformation);
			this.WURCS += mod.toALIN(outputFullInformation);
		}
	}
	
	//----------------------------
	// Public method (non void)
	//----------------------------
	public boolean add(final Backbone backbone){
		return this.backbones.add(backbone);
	}
	
	public boolean add(final Modification mod){
		return this.modifications.add(mod);
	}
	
	public boolean contains(final Backbone backbone){
		return this.backbones.contains(backbone);
	}
	
	public boolean contains(final Modification mod){
		return this.modifications.contains(mod);
	}
}
