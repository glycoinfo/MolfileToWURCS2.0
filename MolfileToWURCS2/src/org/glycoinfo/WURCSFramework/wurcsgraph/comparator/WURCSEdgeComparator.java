package org.glycoinfo.WURCSFramework.wurcsgraph.comparator;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.wurcsgraph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSEdge;

/**
 * Class for WURCSEdge comparison
 * @author MassaakiMatsubara
 *
 */
public class WURCSEdgeComparator implements Comparator<WURCSEdge> {

	@Override
	public int compare(WURCSEdge o1, WURCSEdge o2) {
		// Compare size of LinkagePosition, bigger comes first
		if (o1.getLinkages().size() > o2.getLinkages().size()) return 1;
		if (o1.getLinkages().size() < o2.getLinkages().size()) return -1;

		// If of equal size, compare linkages inside WURCSEdges
		LinkedList<LinkagePosition> t_aLinkages1 = o1.getLinkages();
		LinkedList<LinkagePosition> t_aLinkages2 = o2.getLinkages();
		LinkagePositionComparator t_oLinkComp = new LinkagePositionComparator();
		Collections.sort(t_aLinkages1,t_oLinkComp);
		Collections.sort(t_aLinkages2,t_oLinkComp);

		// Linkage comparison
		for (int i = 0; i < t_aLinkages1.size(); i++) {
			if (t_oLinkComp.compare(t_aLinkages1.get(i),t_aLinkages2.get(i))==0) continue;
			return t_oLinkComp.compare(t_aLinkages1.get(i),t_aLinkages2.get(i));
		}

		// Compare connected Backbone
		if ( !o1.getBackbone().equals(o2.getBackbone()) ) {
			BackboneComparator t_oBComp = new BackboneComparator();
			return t_oBComp.compare(o1.getBackbone(), o2.getBackbone());
		}

		// Compare connected Modification
		if ( !o1.getModification().equals(o2.getModification()) ) {
			ModificationComparator t_oMComp = new ModificationComparator();
			return t_oMComp.compare(o1.getModification(), o2.getModification());
		}


		return 0;
	}

}
