package org.glycoinfo.WURCSFramework.wurcsgraph.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsgraph.Backbone;
import org.glycoinfo.WURCSFramework.wurcsgraph.Modification;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSException;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSGraph;
import org.glycoinfo.WURCSFramework.wurcsgraph.comparator.BackboneComparator;

/**
 * Class for normalizer of WURCSGraph
 * @author MasaakiMatsubara
 *
 */
public class WURCSGraphNormalizer implements WURCSVisitor {

	private LinkedList<Backbone> m_aBackbones = new LinkedList<Backbone>();
	private HashSet<Backbone> m_aSearchedBackbones = new HashSet<Backbone>();
	private LinkedList<Backbone> m_aSymmetricBackbone = new LinkedList<Backbone>();

	private BackboneComparator m_objBackboneComp = new BackboneComparator();

	public void visit(Backbone a_objBackbone) throws WURCSVisitorException {
		if ( !this.m_aBackbones.contains(a_objBackbone) ) this.m_aBackbones.addLast(a_objBackbone);
		if ( this.m_aSearchedBackbones.contains(a_objBackbone) ) return;
		this.m_aSearchedBackbones.add(a_objBackbone);

		// Symmetry check
		Backbone copy   = a_objBackbone.copy();
		Backbone invert = a_objBackbone.copy();
		invert.invert();
		int iComp = this.m_objBackboneComp.compare(copy, invert);
		if ( iComp < 0 ) return;
		if ( iComp > 0 ) {
			a_objBackbone.invert();
			return;
		}

		System.err.println( "For invert backbone: " + a_objBackbone.getSkeletonCode() );
		this.m_aSymmetricBackbone.addLast(a_objBackbone);
	}

	public void visit(Modification a_objModification) throws WURCSVisitorException {
//		if ( !a_objModification.isGlycosidic() ) return;

	}

	public void visit(WURCSEdge a_objWURCSEdge) throws WURCSVisitorException {
		// nothing to do
	}

	@Override
	public void start(WURCSGraph a_objGraph) throws WURCSVisitorException {
		this.clear();

		System.err.println("Backbone count: "+a_objGraph.getBackbones().size());
		System.err.println("Modification count: "+a_objGraph.getModifications().size());

		WURCSGraphTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverseGraph(a_objGraph);

		// Invert backbone for symmetric backbones
		try {
			HashMap<Backbone, Backbone> t_hashOrigToInvert = new HashMap<Backbone, Backbone>();
			a_objGraph.copy(t_hashOrigToInvert);
			for ( Backbone origBackbone : this.m_aSymmetricBackbone ) {
				Backbone copyBackbone = t_hashOrigToInvert.get(origBackbone);
				copyBackbone.invert();
				if ( this.m_objBackboneComp.compare(origBackbone, copyBackbone) > 0 ) {
					System.err.println("Invert Backbone");
					origBackbone.invert();
				}
			}
		} catch (WURCSException e) {
			throw new WURCSVisitorException(e.getErrorMessage());
		}
	}

	@Override
	public WURCSGraphTraverser getTraverser(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		return new WURCSGraphTraverserTree(a_objVisitor);
	}

	@Override
	public void clear() {
		this.m_aBackbones = new LinkedList<Backbone>();
		this.m_aSearchedBackbones = new HashSet<Backbone>();
		this.m_aSymmetricBackbone = new LinkedList<Backbone>();

	}
}
