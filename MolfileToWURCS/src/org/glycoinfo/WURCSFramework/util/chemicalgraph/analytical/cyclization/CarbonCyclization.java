package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.cyclization;

public class CarbonCyclization extends Cyclization {

	@Override
	protected boolean isBreak() {
		if ( !this.getLast().getSymbol().equals("C") ) return true;

		if ( this.isCyclic() ) {
			this.m_bIsSucceeded = true;
			return true;
		}

		return false;
	}
}
