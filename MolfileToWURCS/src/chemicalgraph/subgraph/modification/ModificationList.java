package chemicalgraph.subgraph.modification;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.backbone.BackboneList;


/**
 * @author KenichiTanaka
 */
public class ModificationList extends LinkedList<Modification>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;
	
	//----------------------------
	// Public method (void)
	//----------------------------
	public void sortForCanonicalWURCS(final BackboneList backbones) {
		// mod.connectedChains以外にも並べ替える対象があるかどうかチェック
		for(Modification mod : this){
			Collections.sort(mod.connectedBackbones, new Comparator<Backbone>() {
				public int compare(Backbone backbone1, Backbone backbone2) {
					return backbones.indexOf(backbone1) - backbones.indexOf(backbone2);
				}
			});
		}
		Collections.sort(this, new Comparator<Modification>() {
			public int compare(Modification mod1, Modification mod2) {
				return mod1.compareTo(mod2, backbones);
			}
		});
	}
}
