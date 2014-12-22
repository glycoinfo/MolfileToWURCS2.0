package org.glycoinfo.WURCSFramework.wurcsglycan.util.visitor;

import java.util.Collections;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsglycan.Backbone;
import org.glycoinfo.WURCSFramework.wurcsglycan.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcsglycan.Modification;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSGlycan;
import org.glycoinfo.WURCSFramework.wurcsglycan.util.comparator.WURCSEdgeComparator;

public class WURCSGlycanExporterWURCS implements WURCSVisitor {

	private String m_strBMUs;
	private String m_strMLUs;
	private LinkedList<Backbone> m_aBackbones = new LinkedList<Backbone>();

	@Override
	public void visit(Backbone a_objBackbone) throws WURCSVisitorException {
		// TODO 自動生成されたメソッド・スタブ
		if ( !this.m_aBackbones.contains(a_objBackbone) ) this.m_aBackbones.addLast(a_objBackbone);
		String skeleton = a_objBackbone.getSkeletonCode();
		if ( a_objBackbone.getAnomericPosition() != 0 ) {
			skeleton += "+" + a_objBackbone.getAnomericPosition();
			skeleton += ":" + a_objBackbone.getAnomericSymbol();
		}
		LinkedList<WURCSEdge> edges = a_objBackbone.getEdges();
		WURCSEdgeComparator edgeComp = new WURCSEdgeComparator();
		Collections.sort(edges, edgeComp);
		for ( WURCSEdge edge : a_objBackbone.getEdges() ) {
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

	}

	@Override
	public void visit(Modification a_objModification) throws WURCSVisitorException {
		// TODO 自動生成されたメソッド・スタブ
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
		System.err.println((nAnomeric>0)? (nAnomeric>1)? " both of anomeric" : "" : " at non-anomeric" );

	}

	@Override
	public void visit(WURCSEdge a_objWURCSEdge) throws WURCSVisitorException {
		// TODO 自動生成されたメソッド・スタブ
/*		Backbone backbone = a_objWURCSEdge.getBackbone();

		Modification modification = a_objWURCSEdge.getModification();
		System.err.println(
			this.m_aBackbones.indexOf(backbone)+1 +":"+ backbone.getSkeletonCode() + " - " +
			a_objWURCSEdge.getLinkages().get(0).getBackbonePosition()  +":"+ modification.getMAPCode() );

		for ( LinkagePosition link : a_objWURCSEdge.getLinkages() ) {
			System.err.print( link.getCOLINCode(0,true) + " " );
		}
		System.err.println();
*/
	}

	@Override
	public void start(WURCSGlycan a_objGraph) throws WURCSVisitorException {
		// TODO 自動生成されたメソッド・スタブ

		System.err.println("Backbone count: "+a_objGraph.getBackbones().size());
		System.err.println("Modification count: "+a_objGraph.getModifications().size());

		WURCSGlycanTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverseGraph(a_objGraph);
//		makeWURCS(t_objTraverser);
	}

	@Override
	public WURCSGlycanTraverser getTraverser(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		// TODO 自動生成されたメソッド・スタブ
		return new WURCSGlycanTraverserTree(a_objVisitor);
	}

	@Override
	public void clear() {
		// TODO 自動生成されたメソッド・スタブ

	}

}
