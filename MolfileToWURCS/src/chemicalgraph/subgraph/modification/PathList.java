package chemicalgraph.subgraph.modification;

import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.AtomList;
import chemicalgraph.Bond;
import chemicalgraph.BondList;
import chemicalgraph.ChemicalGraph;

/**
 * @author KenichiTanaka
 */
public class PathList extends LinkedList<Path>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;

	//----------------------------
	// Public method (void)
	//----------------------------
	public void debug(final ChemicalGraph subst){
		System.err.println("length of path : " + this.size());
		System.err.println("No\tStart\tEnd\tAtom\tBond");
		for(Path path : this){
			System.err.println(
					this.indexOf(path) + "\t" +
					((path.pathStart!=null) ? this.indexOf(path.pathStart) : "null") + "\t" +
					this.indexOf(path.pathEnd) + "\t" +
					((path.atom!=null) ? path.atom.symbol + "(" + path.atom.molfileAtomNo + ")" : "null") + "\t" +
					((path.bond!=null) ? subst.bonds.indexOf(path.bond) : "null") + "\t");
		}
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	public String toALIN(){
		String ALIN = "";
		boolean aromatic = false;
		for(Path path : this){
			// Aromatic
			if(aromatic==false &&  path.pathEnd.atom.isAromatic) ALIN += "(";
			if(aromatic==true  && !path.pathEnd.atom.isAromatic) ALIN += ")";
			aromatic = path.pathEnd.atom.isAromatic;

			// 分岐開始
			if(path.pathStart!=null && this.indexOf(path.pathStart)!=this.indexOf(path)-1){
				ALIN += "/" + (this.indexOf(path.pathStart) + 1);
			}

			// 結合表示
			if(path.bond!=null && !(path.pathStart.atom.isAromatic && path.pathEnd.atom.isAromatic)){
				if(path.bond.type == 2) ALIN += "=";
				if(path.bond.type == 3) ALIN += "#";
			}
			if(path.bond!=null && path.bond.stereoModification!=null){
				ALIN += "^" + path.bond.stereoModification;
			}

			if(path.atom==null){
		    	ALIN += "$" + (this.indexOf(path.pathEnd) + 1);
		    }else if(path.atom.isBackbone()){
				ALIN += "*";
			}else{
				ALIN += path.atom.symbol;
			}
			if(path.atom!=null && path.atom.stereoModification!=null){
				ALIN += "^" + path.atom.stereoModification;
			}
		}
		if(this.getLast().pathEnd.atom.isAromatic) ALIN += ")";
		
		return ALIN;
	}
	
	public AtomList atoms(){
		AtomList atoms = new AtomList();
		for(Path path : this){
			if(path.atom == null) continue;
			atoms.add(path.atom);
		}
		return atoms;
	}
	
	public BondList bonds(){
		BondList bonds = new BondList();
		for(Path path : this){
			if(path.bond == null) continue;
			bonds.add(path.bond);
		}
		return bonds;
	}
	
	public Path get(final Atom atom){
		for(Path path : this){
			if(path.atom == atom) return path;
		}
		return null;		
	}
	
	public int indexOf(final Atom atom){
		for(Path path : this){
			if(path.atom == atom) return this.indexOf(path);
		}
		return -1;
	}
	
	public boolean contains(final Atom atom){
		for(Path path : this){
			if(path.atom == atom) return true;
		}
		return false;
	}
	
	public boolean contains(final Bond bond){
		for(Path path : this){
			if(path.bond == bond) return true;
		}
		return false;
	}
}
