package glycan;

import java.util.LinkedList;

import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.modification.Modification;

/**
 * @author KenichiTanaka
 */
public class GlycanList extends LinkedList<Glycan>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;

	//----------------------------
	// Public method (non void)
	//----------------------------
	public boolean contains(final Backbone backbone){
		for(Glycan glycan : this){
			if(glycan.contains(backbone)) return true;
		}
		return false;
	}
	
	public boolean contains(final Modification mod){
		for(Glycan glycan : this){
			if(glycan.contains(mod)) return true;
		}
		return false;
	}

}
