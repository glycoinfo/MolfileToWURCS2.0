package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.util.WURCSDataConverter;
import org.glycoinfo.WURCSFramework.util.WURCSExporter;
import org.glycoinfo.WURCSFramework.wurcs.GLIP;
import org.glycoinfo.WURCSFramework.wurcs.GLIPs;
import org.glycoinfo.WURCSFramework.wurcs.LIN;
import org.glycoinfo.WURCSFramework.wurcs.LIP;
import org.glycoinfo.WURCSFramework.wurcs.LIPs;
import org.glycoinfo.WURCSFramework.wurcs.MOD;
import org.glycoinfo.WURCSFramework.wurcs.RES;
import org.glycoinfo.WURCSFramework.wurcs.UniqueRES;
import org.glycoinfo.WURCSFramework.wurcs.WURCSArray;
import org.glycoinfo.WURCSFramework.wurcsgraph.Backbone;
import org.glycoinfo.WURCSFramework.wurcsgraph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcsgraph.Modification;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSGraph;
import org.glycoinfo.WURCSFramework.wurcsgraph.comparator.WURCSEdgeComparator;
import org.glycoinfo.WURCSFramework.wurcsgraph.visitor.WURCSGraphTraverser;
import org.glycoinfo.WURCSFramework.wurcsgraph.visitor.WURCSGraphTraverserTree;
import org.glycoinfo.WURCSFramework.wurcsgraph.visitor.WURCSVisitor;
import org.glycoinfo.WURCSFramework.wurcsgraph.visitor.WURCSVisitorException;

public class WURCSGraphToArray implements WURCSVisitor {

	private String m_strVersion = "2.0";
	private LinkedList<Backbone>     m_aBackbones;
	private LinkedList<Modification> m_aGlycosidicModifications;

	private WURCSArray m_oWURCS = null;
	private LinkedList<String>    m_aURESString;
	private LinkedList<UniqueRES> m_aURES;
	private LinkedList<RES>       m_aRES;
	private LinkedList<LIN>       m_aLIN;

	private WURCSEdgeComparator m_oEdgeComp = new WURCSEdgeComparator();
	private WURCSExporter       m_oExporter = new WURCSExporter();

	@Override
	public void visit(Backbone a_objBackbone) throws WURCSVisitorException {
		if ( this.m_aBackbones.contains(a_objBackbone) ) return;
		this.m_aBackbones.addLast(a_objBackbone);

		// Make candidate UniqueRES
		UniqueRES t_oURESCandidate
			= new UniqueRES(
				this.m_aURES.size()+1,
				a_objBackbone.getSkeletonCode(),
				a_objBackbone.getAnomericPosition(),
				a_objBackbone.getAnomericSymbol()
			);

		// Searce edges for MOD
		LinkedList<WURCSEdge> edges = a_objBackbone.getEdges();
		Collections.sort( edges, this.m_oEdgeComp );
		HashSet<Modification> searchedMods = new HashSet<Modification>();
		for ( WURCSEdge t_oMODEdge : edges ) {
			Modification t_oModif = t_oMODEdge.getModification();
			if ( searchedMods.contains(t_oModif) ) continue;
			if ( t_oModif.isGlycosidic() ) continue;

			searchedMods.add(t_oModif);

			// Make MOD
			MOD t_oMOD = this.makeMOD(t_oModif);
			if ( t_oMOD == null ) continue;
			t_oURESCandidate.addMOD( this.makeMOD(t_oModif) );

			if ( !t_oMODEdge.isReverse() ) continue;
			System.err.println("has parent");
		}

		// Check unique
		String t_strNewURES = this.m_oExporter.getUniqueRESString(t_oURESCandidate);
		if (! this.m_aURESString.contains(t_strNewURES) ) {
			this.m_aURESString.addLast(t_strNewURES);
			this.m_aURES.addLast(t_oURESCandidate);
		}

		// Make new RES
		int t_iURESID = this.m_aURESString.indexOf(t_strNewURES)+1;
		String t_strRESIndex =  WURCSDataConverter.convertRESIDToIndex( this.m_aBackbones.size()+1 );
		this.m_aRES.addLast( new RES( t_iURESID, t_strRESIndex ) );
	}

	@Override
	public void visit(Modification a_objModification) throws WURCSVisitorException {
		if ( !a_objModification.isGlycosidic() ) return;
		// Add modifiation at glycosidic linkage
		this.m_aGlycosidicModifications.addLast(a_objModification);
	}

	@Override
	public void visit(WURCSEdge a_objWURCSEdge) throws WURCSVisitorException {
		// Nothing to do
	}

	@Override
	public void start(WURCSGraph a_objGraph) throws WURCSVisitorException {
		this.clear();

		WURCSGraphTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverseGraph(a_objGraph);

		// Make LIN list
		for ( Modification mod : this.m_aGlycosidicModifications ) {
			this.m_aLIN.addLast( this.makeLIN(mod) );
		}

		this.m_oWURCS = new WURCSArray(this.m_strVersion, this.m_aURES.size(), this.m_aRES.size(), this.m_aLIN.size());
		for ( UniqueRES t_oURES : this.m_aURES )
			this.m_oWURCS.addUniqueRES(t_oURES);

		for ( RES t_oRES : this.m_aRES )
			this.m_oWURCS.addRES(t_oRES);

		for ( LIN t_oLIN : this.m_aLIN )
			this.m_oWURCS.addLIN(t_oLIN);
	}

	@Override
	public WURCSGraphTraverser getTraverser(WURCSVisitor a_objVisitor) throws WURCSVisitorException {
		return new WURCSGraphTraverserTree(a_objVisitor);
	}

	@Override
	public void clear() {
		this.m_aBackbones = new LinkedList<Backbone>();
		this.m_aGlycosidicModifications = new LinkedList<Modification>();

		this.m_strVersion = "2.0";
		this.m_aURESString = new LinkedList<String>();
		this.m_aURES = new LinkedList<UniqueRES>();
		this.m_aRES = new LinkedList<RES>();
		this.m_aLIN = new LinkedList<LIN>();
	}

	public WURCSArray getWURCSArray() {
		System.out.println( this.m_oExporter.getWURCSString(this.m_oWURCS) );
		return this.m_oWURCS;
	}

	private MOD makeMOD(Modification a_oMod) {

		String t_strMAP = a_oMod.getMAPCode();
		// Omittion
		if ( t_strMAP.equals("*O") || t_strMAP.equals("*=O") ) return null;
		if ( t_strMAP.equals("*O*") ) t_strMAP = "";

		MOD t_oMOD = new MOD(t_strMAP);

		LinkedList<WURCSEdge> edges = a_oMod.getEdges();
		Collections.sort( edges, this.m_oEdgeComp );
		for ( WURCSEdge t_oMODEdge : edges )
			t_oMOD.addLIPs( this.makeLIPs(t_oMODEdge) );

		return t_oMOD;
	}

	private LIPs makeLIPs(WURCSEdge a_oEdge) {
		boolean t_bCanOmitModif = a_oEdge.getModification().canOmitMAP();
		LinkedList<LIP> t_aLIPs = new LinkedList<LIP>();

		for ( LinkagePosition t_oLinkPos : a_oEdge.getLinkages() ) {
			LIP t_oLIP = new LIP(
					t_oLinkPos.getBackbonePosition(),
					t_bCanOmitModif ? ' ' : t_oLinkPos.getDirection().getName(),
					t_bCanOmitModif ? 0 : t_oLinkPos.getModificationPosition()
				);
			t_aLIPs.addLast(t_oLIP);
		}
		return new LIPs(t_aLIPs);
	}

	private LIN makeLIN(Modification a_oMod) {

		String t_strMAP = a_oMod.getMAPCode();
		if ( t_strMAP.equals("*O*") ) t_strMAP = "";
		LIN t_oLIN = new LIN(t_strMAP);

		LinkedList<WURCSEdge> edges = a_oMod.getEdges();
		Collections.sort( edges, this.m_oEdgeComp );
		for ( WURCSEdge t_oEdge : edges ) {
			Backbone backbone = t_oEdge.getBackbone();
			int t_iRES = this.m_aBackbones.indexOf(backbone)+1;
			String t_strRESIndex = WURCSDataConverter.convertRESIDToIndex(t_iRES);
			t_oLIN.addGLIPs( this.makeGLIPs(t_oEdge, t_strRESIndex) );
		}

		return t_oLIN;
	}

	private GLIPs makeGLIPs(WURCSEdge a_oEdge, String t_strRESIndex) {
		boolean t_bCanOmitModif = a_oEdge.getModification().canOmitMAP();
		LinkedList<GLIP> t_aGLIPs = new LinkedList<GLIP>();
		for ( LinkagePosition t_oLinkPos : a_oEdge.getLinkages() ) {
			GLIP t_oGLIP = new GLIP(
					t_strRESIndex,
					t_oLinkPos.getBackbonePosition(),
					t_bCanOmitModif ? ' ' : t_oLinkPos.getDirection().getName(),
					t_bCanOmitModif ? 0 : t_oLinkPos.getModificationPosition()
				);
			t_aGLIPs.addLast(t_oGLIP);
		}
		return new GLIPs(t_aGLIPs);
	}


}
