package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.cyclization;

public class AromatizationLimited extends Aromatization {

	@Override
	protected boolean isBreak() {
		if ( this.getLast().getNumberOfPiElectron() == 0 ) return true;

		if ( this.size() > 8 ) return true;

		if ( this.isCyclic() && this.isSatisfiedHuckelsRule() &&
		   ( this.isSixMembered() || this.isFiveMembered() || this.isSevenMembered() ) ) {
			this.m_bIsSucceeded = true;
			return true;
		}
		return false;
	}

	@Override
	protected boolean isBreakWithNeighbor() {
		if(this.getLast().getNumberOfPiElectron() == 0) return true;

		// Find cyclic
		if ( this.isCyclicWithNeighbor() ) {
			if ( this.isSatisfiedHuckelsRule() )
				this.m_bIsSucceeded = true;
			return true;
		}

		return false;
	}

}
