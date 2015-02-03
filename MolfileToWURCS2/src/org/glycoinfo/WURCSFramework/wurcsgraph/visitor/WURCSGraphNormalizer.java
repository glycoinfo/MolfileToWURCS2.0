package org.glycoinfo.WURCSFramework.wurcsgraph.visitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsgraph.Backbone;
import org.glycoinfo.WURCSFramework.wurcsgraph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcsgraph.Modification;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSException;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSGraph;
import org.glycoinfo.WURCSFramework.wurcsgraph.comparator.BackboneComparator;
import org.glycoinfo.WURCSFramework.wurcsgraph.comparator.WURCSEdgeComparator;

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
		if ( this.m_objBackboneComp.compare(copy, invert) == 0 ) {
			System.err.println( "Symmetry backbone: " + a_objBackbone.getSkeletonCode() );
			this.m_aSymmetricBackbone.addLast(a_objBackbone);
		}

		String skeleton = a_objBackbone.getSkeletonCode();
		if ( a_objBackbone.getAnomericPosition() != 0 )
			skeleton += "-" + a_objBackbone.getAnomericPosition() + a_objBackbone.getAnomericSymbol();

		LinkedList<WURCSEdge> edges = a_objBackbone.getEdges();
		WURCSEdgeComparator edgeComp = new WURCSEdgeComparator();
		Collections.sort(edges, edgeComp);
		HashSet<Modification> searchedMods = new HashSet<Modification>();
		for ( WURCSEdge edge : edges ) {
			Modification mod = edge.getModification();
			if ( searchedMods.contains(mod) ) continue;
			if ( mod.isGlycosidic() ) continue;

			String MOD = this.makeMOD(mod);
			if ( MOD == null ) continue;
			skeleton += "_" + MOD;

			searchedMods.add(mod);
			if ( !edge.isReverse() ) continue;
			System.err.println("has parent");
		}
		System.err.println(skeleton);

	}

	public void visit(Modification a_objModification) throws WURCSVisitorException {
		if ( !a_objModification.isGlycosidic() ) return;
		String str = "";
		int nAnomeric = 0;
		LinkedList<WURCSEdge> edges = a_objModification.getEdges();
		WURCSEdgeComparator edgeComp = new WURCSEdgeComparator();
		Collections.sort(edges, edgeComp);
		for ( WURCSEdge edge : edges ) {
			if ( !str.equals("") ) str += "_";
			Backbone backbone = edge.getBackbone();
			if ( !this.m_aBackbones.contains(backbone) ) this.m_aBackbones.addLast(backbone);
//			str += this.m_aBackbones.indexOf(backbone)+1 +"("+ backbone.getSkeletonCode() +")";
			for ( LinkagePosition link : edge.getLinkages() ) {
				str += link.getCOLINCode(this.m_aBackbones.indexOf(backbone)+1,true);
				if ( link.getBackbonePosition() == backbone.getAnomericPosition() ) nAnomeric++;
			}
		}
		str += ( a_objModification.getMAPCode().equals("*O*") )? "" : a_objModification.getMAPCode();
		System.err.print(str);
		System.err.println((nAnomeric>0)? (nAnomeric>1)? " both of anomeric:"+nAnomeric : "" : " at non-anomeric" );

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
		try {
			for ( Backbone origBackbone : this.m_aSymmetricBackbone ) {
				HashMap<Backbone, Backbone> t_hashOrigToInvert = new HashMap<Backbone, Backbone>();
				a_objGraph.copy(t_hashOrigToInvert);
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

	private String makeMOD(Modification mod) {
		String MOD = "";
		LinkedList<WURCSEdge> edges = mod.getEdges();
		WURCSEdgeComparator edgeComp = new WURCSEdgeComparator();
		Collections.sort(edges, edgeComp);
		for ( WURCSEdge modEdge : edges ) {
			if ( !MOD.equals("") ) MOD += "-";
			MOD += this.makeLIP(modEdge);
		}
		String MAP = mod.getMAPCode();
		// Omittion
		if ( MAP.equals("*O") || MAP.equals("*=O") ) return null;
		if ( MAP.equals("*O*") ) MAP = "";
		MOD += MAP;
		return MOD;
	}

	private String makeLIP(WURCSEdge edge) {
		String COLINs = "";
		for ( LinkagePosition link : edge.getLinkages() ) {
			if (! COLINs.equals("") ) COLINs += "|";
			String COLIN = ""+link.getBackbonePosition();
			COLINs += COLIN;
		}
		return COLINs;
	}
}
