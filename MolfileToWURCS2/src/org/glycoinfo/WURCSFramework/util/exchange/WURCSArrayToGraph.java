package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.util.WURCSDataConverter;
import org.glycoinfo.WURCSFramework.wurcs.GLIP;
import org.glycoinfo.WURCSFramework.wurcs.GLIPs;
import org.glycoinfo.WURCSFramework.wurcs.LIN;
import org.glycoinfo.WURCSFramework.wurcs.LIP;
import org.glycoinfo.WURCSFramework.wurcs.LIPs;
import org.glycoinfo.WURCSFramework.wurcs.MOD;
import org.glycoinfo.WURCSFramework.wurcs.RES;
import org.glycoinfo.WURCSFramework.wurcs.UniqueRES;
import org.glycoinfo.WURCSFramework.wurcs.WURCSArray;
import org.glycoinfo.WURCSFramework.wurcs.WURCSFormatException;
import org.glycoinfo.WURCSFramework.wurcsgraph.Backbone;
import org.glycoinfo.WURCSFramework.wurcsgraph.BackboneCarbon;
import org.glycoinfo.WURCSFramework.wurcsgraph.CarbonDescriptor;
import org.glycoinfo.WURCSFramework.wurcsgraph.DirectionDescriptor;
import org.glycoinfo.WURCSFramework.wurcsgraph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcsgraph.Modification;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSException;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSGraph;

public class WURCSArrayToGraph {

	private WURCSGraph m_oGraph = new WURCSGraph();
	private LinkedList<Backbone> m_aBackbones = new LinkedList<Backbone>();

	public void start(WURCSArray a_oArray) throws WURCSFormatException, WURCSException {

		for ( RES t_oRES : a_oArray.getRESs() ) {
			int t_iURESID = t_oRES.getUniqueRESID();
			UniqueRES t_oURES = a_oArray.getUniqueRESs().get(t_iURESID-1);

			Backbone t_oBackbone = this.convertToBackbone( t_oURES );
			// For each modification in RES
			for ( MOD t_oMOD : t_oURES.getMODs() ) {
				Modification t_oModif = new Modification( t_oMOD.getMAPCode() );
				for ( LIPs t_oLIPs : t_oMOD.getListOfLIPs() ) {
					WURCSEdge t_oEdge = new WURCSEdge();
					for ( LIP t_oLIP : t_oLIPs.getLIPs() ) {
						t_oEdge.addLinkage( this.convertToLinkagePosition(t_oLIP) );
					}
					this.m_oGraph.addResidues(t_oBackbone, t_oEdge, t_oModif);
				}
			}
			this.m_aBackbones.addLast(t_oBackbone);
		}

		for ( LIN t_oLIN : a_oArray.getLINs() ) {
			Modification t_oModif = new Modification( t_oLIN.getMAPCode() );
			for ( GLIPs t_oGLIPs : t_oLIN.getListOfGLIPs() ) {
				HashMap<Backbone, WURCSEdge> t_mapB2Edge = new HashMap<Backbone, WURCSEdge>();
				for ( GLIP t_oGLIP : t_oGLIPs.getGLIPs() ) {
					int t_iRESID = WURCSDataConverter.convertRESIndexToID( t_oGLIP.getRESIndex() );
					Backbone t_oBackbone = this.m_aBackbones.get(t_iRESID-1);
					if ( !t_mapB2Edge.containsKey(t_oBackbone) )
						t_mapB2Edge.put(t_oBackbone, new WURCSEdge() );
					WURCSEdge t_oEdge = t_mapB2Edge.get(t_oBackbone);
					t_oEdge.addLinkage( this.convertToLinkagePosition(t_oGLIP) );
				}
				for ( Backbone t_oBackbone : t_mapB2Edge.keySet() ) {
					this.m_oGraph.addResidues(t_oBackbone, t_mapB2Edge.get(t_oBackbone), t_oModif);
				}
			}
		}
	}

	public WURCSGraph getGraph() {
		return this.m_oGraph;
	}

	/**
	 * Convert to LinkagePosition from LIP
	 * @param t_oLIP LIP in WURCSArray
	 * @return LinkagePosition Converted from LIP
	 * @throws WURCSFormatException
	 */
	private LinkagePosition convertToLinkagePosition(LIP t_oLIP) throws WURCSFormatException {
		int     t_iBPos      = t_oLIP.getBackbonePosition();
		char    t_cDirection = t_oLIP.getBackboneDirection();
		boolean t_bCompressDirection = ( t_cDirection == ' ' );
		int     t_iMPos      = t_oLIP.getModificationPosition();
		boolean t_bCompressMPos = ( t_iMPos == 0 );
		DirectionDescriptor t_enumDirection = DirectionDescriptor.forChar( t_oLIP.getBackboneDirection() );
		if ( t_enumDirection == null )
			throw new WURCSFormatException("Unknown DirectionDescriptor is found.");
		return new LinkagePosition(t_iBPos, t_enumDirection, t_bCompressDirection, t_iMPos, t_bCompressMPos);
	}

	private Backbone convertToBackbone(UniqueRES a_oURES) throws WURCSFormatException {
		Backbone t_oBackbone = new Backbone();
		LinkedList<String> t_aCDString = this.parseSkeletonCode( a_oURES.getSkeletonCode() );
		for ( String t_strCD : t_aCDString ) {
			int t_iPos = t_aCDString.indexOf(t_strCD)+1;
			boolean t_bIsTerminal = ( t_aCDString.getFirst().equals(t_strCD) || t_aCDString.getFirst().equals(t_strCD) );
			boolean t_bIsAnomeric = ( t_iPos == a_oURES.getAnomericPosition() );

			BackboneCarbon t_oBC;
			// For unknown length carbon
			if ( t_strCD.equals("<nx>") ) {
				if ( t_bIsAnomeric )
					throw new WURCSFormatException("SkeletonCode with unknown length must not be anomeric position : "+a_oURES.getSkeletonCode());
				if ( t_bIsTerminal )
					throw new WURCSFormatException("SkeletonCode with unknown length must not be terminal : "+a_oURES.getSkeletonCode());

				t_oBC = new BackboneCarbon( t_oBackbone, CarbonDescriptor.forCharacter('x' ,t_bIsTerminal), t_bIsAnomeric, true );
			} else {
				t_oBC = new BackboneCarbon( t_oBackbone, CarbonDescriptor.forCharacter(t_strCD.charAt(0) ,t_bIsTerminal), t_bIsAnomeric );
			}
			t_oBackbone.addBackboneCarbon(t_oBC);
		}

		return t_oBackbone;
	}

	private LinkedList<String> parseSkeletonCode(String a_strSkeletonCode) throws WURCSFormatException {
		LinkedList<String> t_aCDString = new LinkedList<String>();
		int length = a_strSkeletonCode.length();
		for ( int i=0; i<length; i++ ) {
			char t_cName = a_strSkeletonCode.charAt(i);
			if ( Character.isAlphabetic(t_cName) ) {
				t_aCDString.add(""+t_cName);
				continue;
			}
			// For unknown length
			if ( !a_strSkeletonCode.substring(i, i+3).equals("<nx>") )
				throw new WURCSFormatException("unknown CarbonDescriptor is found : "+a_strSkeletonCode);
			i += 3;
			t_aCDString.add("<nx>");
		}
		return t_aCDString;
	}

}
