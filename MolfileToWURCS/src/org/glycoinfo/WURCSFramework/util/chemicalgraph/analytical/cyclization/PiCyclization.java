package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.cyclization;

public class PiCyclization extends Cyclization {

	@Override
	protected boolean isBreak() {
		if ( this.getLast().getNumberOfPiElectron() == 0 ) return true;
		if ( this.isCyclic() ) {
			this.m_bIsSucceeded = true;
			return true;
		}
		return false;
	}

}
