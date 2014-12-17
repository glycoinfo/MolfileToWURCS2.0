package wurcsglycan.util.comparator;

import java.util.Comparator;

import wurcsglycan.Backbone;
import wurcsglycan.BackboneCarbon;

/**
 *
 * @author MasaakiMatsubara
 * TODO:
 */
public class BackboneComparator implements Comparator<Backbone> {

	@Override
	public int compare(Backbone b1, Backbone b2) {

		// Compare ambiguousness
		if ( b1 ) {

		}

		for ( BackboneCarbon bc : b1.getBackboneCarbons() ) {

		}
		return 0;
	}

}
