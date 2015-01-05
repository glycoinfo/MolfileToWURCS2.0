package org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor;

import java.util.Collections;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsglycan.Backbone;
import org.glycoinfo.WURCSFramework.wurcsglycan.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcsglycan.Modification;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSGraph;
import org.glycoinfo.WURCSFramework.wurcsglycan.util.comparator.BackboneComparator;
import org.glycoinfo.WURCSFramework.wurcsglycan.util.comparator.WURCSEdgeComparator;

public class WURCSGlycanExporterWURCS implements WURCSVisitor {

	private String m_strVersion = "2.0";
	private int    m_iBMUCounter = 0;
	private int    m_iMLUCounter = 0;
	private String m_strBMUs = "";
	private String m_strMLUs = "";
	private LinkedList<Backbone> m_aBackbones = new LinkedList<Backbone>();

	public void visit(Backbone a_objBackbone) throws WURCSVisitorException {
		if ( !this.m_aBackbones.contains(a_objBackbone) ) this.m_aBackbones.addLast(a_objBackbone);

		if ( this.checkBackboneSymmetry(a_objBackbone) ) {
			System.err.println("Symmetry is found:");
		}
		String skeleton = a_objBackbone.getSkeletonCode();
		if ( a_objBackbone.getAnomericPosition() != 0 ) {
			skeleton += "+" + a_objBackbone.getAnomericPosition();
			skeleton += ":" + a_objBackbone.getAnomericSymbol();
		}
		LinkedList<WURCSEdge> edges = a_objBackbone.getEdges();
		WURCSEdgeComparator edgeComp = new WURCSEdgeComparator();
		Collections.sort(edges, edgeComp);
		for ( WURCSEdge edge : edges ) {
			Modification mod = edge.getModification();
			if ( mod.getEdges().size() > 1 ) continue;
			String MAP = mod.getMAPCode();
			if ( MAP.equals("*O") || MAP.equals("*=O") ) continue;
			if ( MAP.equals("*O*") ) MAP = "";
			String COLIN = "";
			for ( LinkagePosition link : mod.getEdges().get(0).getLinkages() ) {
				if ( !COLIN.equals("") ) COLIN += ",";
				COLIN += link.getBackbonePosition();
			}
			skeleton += "|" + COLIN + MAP;
		}
		this.m_strBMUs += "["+skeleton+"]";
		System.err.println(skeleton);

		this.m_iBMUCounter++;
	}

	public void visit(Modification a_objModification) throws WURCSVisitorException {
		if ( a_objModification.getEdges().size() < 2 ) return;
		String str = "";
		int nAnomeric = 0;
		LinkedList<WURCSEdge> edges = a_objModification.getEdges();
		WURCSEdgeComparator edgeComp = new WURCSEdgeComparator();
		Collections.sort(edges, edgeComp);
		for ( WURCSEdge edge : edges ) {
			if ( !str.equals("") ) str += ",";
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

		this.m_strMLUs += "|" + str;
		this.m_iMLUCounter++;
	}

	public void visit(WURCSEdge a_objWURCSEdge) throws WURCSVisitorException {
		// nothing to do
	}

	@Override
	public void start(WURCSGraph a_objGraph) throws WURCSVisitorException {
		this.clear();

		System.err.println("Backbone count: "+a_objGraph.getBackbones().size());
		System.err.println("Modification count: "+a_objGraph.getModifications().size());

		WURCSGlycanTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverseGraph(a_objGraph);
//		makeWURCS(t_objTraverser);
		System.err.println("WURCS="+this.m_strVersion+"/"+this.m_iBMUCounter+","+this.m_iMLUCounter+"/"+this.m_strBMUs+this.m_strMLUs);
	}

	@Override
	public WURCSGlycanTraverser getTraverser(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		// TODO 自動生成されたメソッド・スタブ
		return new WURCSGlycanTraverserTree(a_objVisitor);
	}

	@Override
	public void clear() {
		this.m_strVersion = "2.0";
		this.m_iBMUCounter = 0;
		this.m_iMLUCounter = 0;
		this.m_strBMUs = "";
		this.m_strMLUs = "";
		this.m_aBackbones = new LinkedList<Backbone>();

	}

	private boolean checkBackboneSymmetry(Backbone backbone) {
		Backbone cloned   = backbone.clone();
		Backbone inverted = backbone.invert();
		System.err.println("Code:"+ cloned.getSkeletonCode() +" vs "+ inverted.getSkeletonCode() );
		System.err.println("AnomPos:"+ cloned.getAnomericPosition() +" vs "+ inverted.getAnomericPosition() );
		System.err.println("AnomSymbol:"+ cloned.getAnomericSymbol() +" vs "+ inverted.getAnomericSymbol() );
		BackboneComparator t_oComp = new BackboneComparator();
		System.err.println("Comp:"+ t_oComp.compare(cloned, inverted) );
		if ( t_oComp.compare(cloned, inverted) == 0 ) return true;
		return false;
	}
}
