package org.glycoinfo.WURCSFramework.wurcsglycan.util.comparator;

import java.util.Comparator;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsglycan.Backbone;
import org.glycoinfo.WURCSFramework.wurcsglycan.BackboneCarbon;
import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSEdge;

/**
 * Class for Backbone comparison
 * @author MasaakiMatsubara
 */
public class BackboneComparator implements Comparator<Backbone> {

	@Override
	public int compare(Backbone b1, Backbone b2) {

		// Prioritize root node
		WURCSEdge t_oAnomEdge1 = b1.getAnomericEdge();
		WURCSEdge t_oAnomEdge2 = b2.getAnomericEdge();
		boolean isRoot1 = ( t_oAnomEdge1 == null || t_oAnomEdge1.getModification().isAglycone() );
		boolean isRoot2 = ( t_oAnomEdge2 == null || t_oAnomEdge2.getModification().isAglycone() );
		if (  isRoot1 && !isRoot2  ) return -1;
		if ( !isRoot1 &&  isRoot2  ) return 1;

		// Compare number of parent backbone
		// Prioritize smaller number of parent
		LinkedList<Backbone> t_aParents1 = new LinkedList<Backbone>();
		LinkedList<Backbone> t_aParents2 = new LinkedList<Backbone>();
		if ( !isRoot1 )
			for ( WURCSEdge parentEdge : t_oAnomEdge1.getModification().getEdges() )
				t_aParents1.add( parentEdge.getBackbone() );
		if ( !isRoot2 )
			for ( WURCSEdge parentEdge : t_oAnomEdge2.getModification().getEdges() )
				t_aParents2.add( parentEdge.getBackbone() );
		if ( t_aParents1.size() != t_aParents2.size() )
			return t_aParents1.size() - t_aParents2.size();

		// Compare number of connected backbone and modification
		// If a backbone connected to this backbone with anomeric position, the backbone is child.
		/*
		 *  B0->M1-A->B      ---> : edge to non-anomeric position of other backbone
		 *   |   +--->B      -A-> : edge to anomeric position of other backbone
		 *   |   +-A->B      "-A->" means edge to child backbone from B0
		 *   +->M2--->B
		 *   +->M3-A->B      In the case, there is 3 child of 5 connected backbone
		 */
		int t_nChildCount1 = 0;
		int t_nChildCount2 = 0;
		int t_nBackboneCount1 = 0;
		int t_nBackboneCount2 = 0;
		int t_nModificationCount1 = 0;
		int t_nModificationCount2 = 0;
		for ( WURCSEdge edgeB2M : b1.getEdges() ) {
			// Ignore parent edge
			if ( edgeB2M.equals(t_oAnomEdge1) ) continue;
			for ( WURCSEdge edgeM2B : edgeB2M.getModification().getEdges() ) {
				// Ignore reverse edge
				if ( edgeM2B.equals(edgeB2M) ) continue;
				t_nBackboneCount1++;
				if ( edgeM2B.getBackbone().getAnomericEdge() != null &&
					!edgeM2B.getBackbone().getAnomericEdge().equals(edgeM2B) ) continue;
				t_nChildCount1++;
			}
			// Ignore modification which can omit
			if ( edgeB2M.getModification().canOmit() ) continue;
			t_nModificationCount1++;
		}
		for ( WURCSEdge edgeB2M : b1.getEdges() ) {
			// Ignore parent edge
			if ( edgeB2M.equals(t_oAnomEdge1) ) continue;
			for ( WURCSEdge edgeM2B : edgeB2M.getModification().getEdges() ) {
				// Ignore reverse edge
				if ( edgeM2B.equals(edgeB2M) ) continue;
				t_nBackboneCount1++;
				if ( edgeM2B.getBackbone().getAnomericEdge() != null &&
					!edgeM2B.getBackbone().getAnomericEdge().equals(edgeM2B) ) continue;
				t_nChildCount1++;
			}
			// Ignore modification which can omit
			if ( edgeB2M.getModification().canOmit() ) continue;
			t_nModificationCount2++;
		}
		// Prioritize larger number of child backbone
		if ( t_nChildCount1 != t_nChildCount2 )
			return t_nChildCount2 - t_nChildCount1;
		// Prioritize larger number of connected backbone
		if ( t_nBackboneCount1 != t_nBackboneCount2 )
			return t_nBackboneCount2 - t_nBackboneCount1;
		// Prioritize larger number of connected modification
		if ( t_nModificationCount1 != t_nModificationCount2 )
			return t_nModificationCount2 - t_nModificationCount1;


		// Compare ambiguousness
		// Prioritize no unknown length
		if ( !b1.hasUnknownLength() ||  b2.hasUnknownLength() ) return -1;
		if (  b1.hasUnknownLength() || !b2.hasUnknownLength() ) return 1;

		// Compare backbone length
		// Prioritize longer backbone
		if ( b1.getBackboneCarbons().size() != b2.getBackboneCarbons().size() )
			return b2.getBackboneCarbons().size() - b1.getBackboneCarbons().size();

		// TODO: Compare backbone carbons
		for ( BackboneCarbon bc : b1.getBackboneCarbons() ) {

		}

		return 0;
	}

}
