package org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsglycan.Backbone;
import org.glycoinfo.WURCSFramework.wurcsglycan.Modification;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSComponent;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSException;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSGraph;
import org.glycoinfo.WURCSFramework.wurcsglycan.util.comparator.BackboneComparator;
import org.glycoinfo.WURCSFramework.wurcsglycan.util.comparator.WURCSEdgeComparator;

public class WURCSGlycanTraverserTree extends WURCSGlycanTraverser {

	private HashSet<WURCSEdge> m_aSearchedEdges = new HashSet<WURCSEdge>();

	public WURCSGlycanTraverserTree(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		super(a_objVisitor);
	}

	@Override
	public void traverse(WURCSComponent a_objResidue) throws WURCSVisitorException {

		// callback of the function before subtree
		this.m_iState = WURCSGlycanTraverser.ENTER;
		a_objResidue.accept(this.m_objVisitor);

		LinkedList<WURCSEdge> t_aEdges = a_objResidue.getEdges();
		WURCSEdgeComparator t_oComp = new WURCSEdgeComparator();
		Collections.sort(t_aEdges, t_oComp);
		for ( WURCSEdge t_oEdge : t_aEdges ) {

			if ( a_objResidue.getClass() == Backbone.class     &&  t_oEdge.isReverse() ) continue;
			if ( a_objResidue.getClass() == Modification.class && !t_oEdge.isReverse() ) continue;

			this.traverse( t_oEdge );
			// callback after return
//			this.m_iState = WURCSGlycanTraverser.RETURN;
//			a_objBackbone.accept(this.m_objVisitor);
		}
		// callback after subtree
//		this.m_iState = WURCSGlycanTraverser.LEAVE;
//		a_objBackbone.accept(this.m_objVisitor);
	}

	@Override
	public void traverse(WURCSEdge a_objEdge) throws WURCSVisitorException {

		// callback of the function before subtree
		this.m_iState = WURCSGlycanTraverser.ENTER;
		a_objEdge.accept(this.m_objVisitor);

		this.m_aSearchedEdges.add(a_objEdge);

		// traverse subtree

		if ( a_objEdge.isReverse() ) {
			this.traverse(a_objEdge.getBackbone());
		} else {
			this.traverse(a_objEdge.getModification());
		}

		// callback of the function after subtree
//		this.m_iState = WURCSGlycanTraverser.LEAVE;
//		a_objEdge.accept(this.m_objVisitor);


	}

	@Override
	public void traverseGraph(WURCSGraph a_objGlycan) throws WURCSVisitorException {
		ArrayList<Backbone> t_aRoot;
		try {
			// get root nodes of forest of graphs
			t_aRoot = a_objGlycan.getRootBackbones();
			// Priorize according to WURCSGlycan all isolated subgraphs and process consecutivly
			BackboneComparator t_oBComp = new BackboneComparator();
			Collections.sort(t_aRoot,t_oBComp);

			Iterator<Backbone> t_objIterator = t_aRoot.iterator();
			while ( t_objIterator.hasNext() )
				this.traverse(t_objIterator.next());
		}
		catch (WURCSException e) {
			throw new WURCSVisitorException(e.getMessage());
		}

	}

}
