package chemicalgraph.util.forwurcsglycan;

import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.Bond;
import chemicalgraph.ChemicalGraph;

/**
 * Class for atomic path in modification to generate MAP code
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


