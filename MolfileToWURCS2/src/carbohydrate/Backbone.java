package carbohydrate;

/**
 * Class for backbone of saccharide
 * @author MasaakiMatsubara
 *
 */
public class Backbone extends CarbohydrateComponent{

	public String getSkeletonCode() {
		String code = "";
		for ( CarbonType ctype : this.getCarbonTypes() ) {
			code += ctype.getChar();
		}
		return code;
	}
}
