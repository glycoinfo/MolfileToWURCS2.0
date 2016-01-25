package org.glycoinfo.WURCSFramework.buildingblock;

import java.util.LinkedList;

public class BuildingBlock {

	private LinkedList<BackCarbonChain> m_aChains = new LinkedList<BackCarbonChain>();
	private LinkedList<ModGraph> m_aGraphs = new LinkedList<ModGraph>();

	public void addCarbonChain(BackCarbonChain a_oChain) {
		this.m_aChains.add(a_oChain);
	}

	public void addModGraph(ModGraph a_oGraph) {
		this.m_aGraphs.add(a_oGraph);
	}

	public LinkedList<BackCarbonChain> getCarbonChains() {
		return this.m_aChains;
	}

	public LinkedList<ModGraph> getModGraphs() {
		return this.m_aGraphs;
	}

}
