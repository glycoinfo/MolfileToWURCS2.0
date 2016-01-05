package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.cyclization;

public class PiCyclization extends MultiCyclization {

	@Override
	protected boolean isBreak() {
		if ( this.getLast().getNumberOfPiElectron() == 0 ) return true;

		if ( this.isCyclic() ) {
			this.m_bIsSucceeded = true;
			return true;
		}

		return false;
	}

	@Override
	protected boolean isBreakWithNeighbor() {
		if ( this.getLast().getNumberOfPiElectron() == 0 ) return true;

		// Find cyclic
		if ( this.isCyclicWithNeighbor() ){
			this.m_bIsSucceeded = true;
			return true;
		}

		return false;
	}

}
