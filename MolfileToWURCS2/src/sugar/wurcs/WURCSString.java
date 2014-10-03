package sugar.wurcs;

/**
 * Abstract class for WURCS String strage
 * @author MasaakiMatsubara
 *
 */
public abstract class WURCSString {

	private String m_objCode;

	public String getCode() {
		makeCode();
		return this.m_objCode;
	}

	public abstract void makeCode();
}
