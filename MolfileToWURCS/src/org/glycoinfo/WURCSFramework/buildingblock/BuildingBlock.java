package org.glycoinfo.WURCSFramework.buildingblock;

import java.util.LinkedList;

public class BuildingBlock {

	private LinkedList<CarbonChain> m_aChains = new LinkedList<CarbonChain>();
	private LinkedList<ModGraph> m_aGraphs = new LinkedList<ModGraph>();

	public void addCarbonChain(CarbonChain a_oChain) {
		this.m_aChains.add(a_oChain);
	}

	public void addModGraph(ModGraph a_oGraph) {
		this.m_aGraphs.add(a_oGraph);
	}

	public LinkedList<CarbonChain> getCarbonChains() {
		return this.m_aChains;
	}

	public LinkedList<ModGraph> getModGraphs() {
		return this.m_aGraphs;
	}

}
