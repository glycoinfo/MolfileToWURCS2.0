package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.cyclization;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;

public class Aromatization extends PiCyclization {

	@Override
	protected boolean isBreak() {
		if(this.isCyclic()){
			if(this.isSatisfiedHuckelsRule())
				this.m_bIsSucceeded = true;
			return true;
		}
		return false;
	}

	/**
	 * Return true if the member of this list satisfied the huckels rule.
	 * @return true if the member of this list satisfied the huckels rule
	 */
	private boolean isSatisfiedHuckelsRule(){
		LinkedList<Atom> uniqAtom = new LinkedList<Atom>();
		for ( Atom atom : this ) {
			if ( uniqAtom.contains(atom) ) continue;
			uniqAtom.add(atom);
		}

		int pi_num = 0;
		for ( Atom atom : uniqAtom ) {
//			pi_num += atom.pi;
			// TODO:
			pi_num += atom.getNumberOfPiElectron();
		}

		return ((pi_num - 2) % 4 == 0) ? true : false;
	}
}
