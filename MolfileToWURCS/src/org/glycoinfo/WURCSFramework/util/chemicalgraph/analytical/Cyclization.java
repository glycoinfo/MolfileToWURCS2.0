package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

/**
 * Class for finding cyclic atoms which aromatic, pi cyclic or carbon cyclic atoms
 * @author MasaakiMatsubara
 *
 */
public class Cyclization extends LinkedList<Atom>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;

	private static final int AROMATIC = 0;
	private static final int PI_CYCLIC = 1;
	private static final int CARBON_CYCLIC = 2;

	private boolean isSucceeded = false;

	public void clear() {
		super.clear();
		this.isSucceeded = false;
	}

	public boolean isSixMembered() {
		return (this.size() == 6);
	}

	public boolean isFiveMembered() {
		return (this.size() == 5);
	}

	public boolean isSevenMembered() {
		return (this.size() == 7);
	}

	//----------------------------
	// Public method (void)
	//----------------------------
	public boolean aromatize(final Atom start) {
		this.clear();
		this.addFirst(start);
		this.searchCyclic(Cyclization.AROMATIC);
		if ( this.isSucceeded ) return true;
		return false;
	}

	public boolean piCyclize(final Atom start){
		this.clear();
		this.addFirst(start);
		this.searchCyclic(Cyclization.PI_CYCLIC);
		if ( this.isSucceeded ) return true;
		return false;
	}

	public boolean carbonCyclize(final Atom start){
		this.clear();
		this.addFirst(start);
		this.searchCyclic(Cyclization.CARBON_CYCLIC);
		if ( this.isSucceeded ) return true;
		return false;
	}

	//----------------------------
	// Private method
	//----------------------------
	/**
	 * Search cyclic structure recursively
	 * @param type Number of cyclization type
	 */
	private void searchCyclic(final int type) {
		if ( type == Cyclization.AROMATIC ) {
//			if(this.getLast().pi == 0) return;
			// TODO:
			if(this.getLast().getNumberOfPiElectron() == 0) return;

			if(this.isCyclic()){
				if(this.isSatisfiedHuckelsRule()){
					this.isSucceeded = true;
					return;
				}
				return;
			}
		} else if ( type == Cyclization.PI_CYCLIC ) {
//			if(this.getLast().pi == 0) return;
			// TODO:
			if(this.getLast().getNumberOfPiElectron() == 0) return;

			if(this.isCyclic()){
				this.isSucceeded = true;
				return;
			}
		} else if ( type == Cyclization.CARBON_CYCLIC ) {
//			if(!this.getLast().symbol.equals("C")) return;
			// TODO:
			if(!this.getLast().getSymbol().equals("C")) return;

			if(this.isCyclic()){
				this.isSucceeded = true;
				return;
			}
		} else {
			System.err.println("Unknown cyclic type is set.");
			return;
		}

		// depth search
//		for(Connection connect : this.getLast().connections){
//		Atom conAtom = connect.atom;
		// TODO:
		for(Connection connect : this.getLast().getConnections()){
			Atom conAtom = connect.endAtom();
			if(this.contains(conAtom) && (conAtom != this.getFirst())) continue;
			if(this.contains(conAtom) && this.size() < 3) continue;
			this.addLast(conAtom);
			this.searchCyclic(type);
			this.removeLast();
		}
	}

	private boolean isCyclic(){
		return (this.size() > 2) && (this.getLast() == this.getFirst());
	}

	/**
	 * Return true if the member of this list satisfied the huckels rule.
	 * @return true if the member of this list satisfied the huckels rule
	 */
	private boolean isSatisfiedHuckelsRule(){
		LinkedList<Atom> uniqAtom = new LinkedList<Atom>();
		for(Atom atom : this){
			if(uniqAtom.contains(atom))continue;
			uniqAtom.add(atom);
		}

		int pi_num = 0;
		for(Atom atom : uniqAtom){
//			pi_num += atom.pi;
			// TODO:
			pi_num += atom.getNumberOfPiElectron();
		}

		return ((pi_num - 2) % 4 == 0) ? true : false;
	}
}
