package org.glycoinfo.WURCSFramework.util.hierarchicaldigraph;

public class DigraphEdge {

	private DigraphNode m_oParentNode;
	private DigraphNode m_oChildNode;

	public DigraphEdge( DigraphNode a_oParent, DigraphNode a_oChild ) {
		this.m_oParentNode = a_oParent;
		this.m_oChildNode  = a_oChild;
	}

	public DigraphNode getParentNode() {
		return this.m_oParentNode;
	}

	public DigraphNode getChildNode() {
		return this.m_oChildNode;
	}
}
