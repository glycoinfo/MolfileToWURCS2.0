package org.glycoinfo.WURCSFramework.buildingblock;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcs.map.MAPGraph;

public class BuildingBlock {

	private LinkedList<BackCarbonChain> m_aChains = new LinkedList<BackCarbonChain>();
	private LinkedList<MAPGraph> m_aGraphs = new LinkedList<MAPGraph>();

	public void addCarbonChain(BackCarbonChain a_oChain) {
		this.m_aChains.add(a_oChain);
	}

	public void addModGraph(MAPGraph a_oGraph) {
		this.m_aGraphs.add(a_oGraph);
	}

	public LinkedList<BackCarbonChain> getCarbonChains() {
		return this.m_aChains;
	}

	public LinkedList<MAPGraph> getModGraphs() {
		return this.m_aGraphs;
	}

}
