package chemicalgraph.util.forwurcsglycan;

import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.Bond;
import chemicalgraph.ChemicalGraph;

/**
 * Class for atomic path in modification to generate ALIN code
 * @author KenichiTanaka
 * @author MasaakiMatsubara
 */
public class Path extends LinkedList<PathSection>{
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
		for(PathSection path : this){
			System.err.println(
					this.indexOf(path) + "\t" +
					((path.getLast()!=null) ? this.indexOf(path.getLast()) : "null") + "\t" +
					this.indexOf(path.getNext()) + "\t" +
					((path.getAtom()!=null) ? path.getAtom().getSymbol() + "(" + subst.getAtoms().indexOf(path.getAtom()) + ")" : "null") + "\t" +
					((path.getBond()!=null) ? subst.getBonds().indexOf(path.getBond()) : "null") + "\t");
		}
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
/*	public String toALIN(){
		String ALIN = "";
		boolean aromatic = false;
		for(Path path : this){
			// For aromatic
//			if(aromatic==false &&  path.pathEnd.atom.isAromatic) ALIN += "(";
//			if(aromatic==true  && !path.pathEnd.atom.isAromatic) ALIN += ")";
			if(aromatic==false &&  path.isAromatic() ) ALIN += "(";
			if(aromatic==true  && !path.isAromatic() ) ALIN += ")";
			aromatic = path.isAromatic();

			// 分岐開始
			// For starting brach
			if ( path.getStart()!=null && this.indexOf(path.getStart())!=this.indexOf(path)-1 ) {
				ALIN += "/" + (this.indexOf(path.getStart()) + 1);
			}

			// 結合表示
			// For bond type
			Bond bond = path.getBond();
			if ( bond!=null && !(path.getStart().isAromatic() && path.getEnd().isAromatic()) ) {
				if ( bond.getType() == 2) ALIN += "=";
				if ( bond.getType() == 3) ALIN += "#";
			}
			if(bond!=null && path.getStereo()!=null){
				ALIN += "^" + path.getStereo();
			}

			Atom atom = path.getAtom();
			if(atom==null){
				ALIN += "$" + (this.indexOf(path.getEnd()) + 1);
			}else if(path.isBackbone()){
				ALIN += "*";
			}else{
				ALIN += atom.getSymbol();
			}
			if(atom!=null && path.getStereo()!=null){
				ALIN += "^" + path.getStereo();
			}
		}
		if(this.getLast().getEnd().isAromatic()) ALIN += ")";

		return ALIN;
	}
*/
	public LinkedList<Atom> atoms(){
		LinkedList<Atom> atoms = new LinkedList<Atom>();
		for(PathSection path : this){
			if(path.getAtom() == null) continue;
			atoms.add(path.getAtom());
		}
		return atoms;
	}

	public LinkedList<Bond> bonds(){
		LinkedList<Bond> bonds = new LinkedList<Bond>();
		for(PathSection path : this){
			if(path.getBond() == null) continue;
			bonds.add(path.getBond());
		}
		return bonds;
	}

	public PathSection get(final Atom atom){
		for(PathSection path : this){
			if(path.getAtom() == atom) return path;
		}
		return null;
	}

	public int indexOf(final Atom atom){
		for(PathSection path : this){
			if(path.getAtom() == atom) return this.indexOf(path);
		}
		return -1;
	}

	public boolean contains(final Atom atom){
		for(PathSection path : this){
			if(path.getAtom() == atom) return true;
		}
		return false;
	}

	public boolean contains(final Bond bond){
		for(PathSection path : this){
			if(path.getBond() == bond) return true;
		}
		return false;
	}
}


