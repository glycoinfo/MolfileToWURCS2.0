package carbohydrate;

import java.util.LinkedList;

/**
 * Class for linkage on the Backbone carbon
 * @author MasaakiMatsubara
 *
 */
public class Linkage {

	public static final int DIRECTION_0 = 0;
	public static final int DIRECTION_1 = 1;
	public static final int DIRECTION_2 = 2;

	/** Direction type of Modification on the Backbone carbon (calling "DMB" in WURCS) */
	private int m_iDirection = Linkage.DIRECTION_0;
	/** Carbon number in linked Backbone (calling "PCB" in WURCS) */
	private LinkedList<Integer> m_aBackbonePositions     = new LinkedList<Integer>();
	/** Carbon number in linked Modification (calling "PCA" in WURCS) */
	private LinkedList<Integer> m_aModificationPositions = new LinkedList<Integer>();


	public void setDirection(int direction) {
		this.m_iDirection = direction;
	}

	public int getDirection() {
		return this.m_iDirection;
	}

	public void addBackbonePosition(Integer iPosition) {
		this.m_aBackbonePositions.addLast(iPosition);
	}

	public LinkedList<Integer> getBackbonePositions() {
		return this.m_aBackbonePositions;
	}

	public void addModificationPosition(Integer iPosition) {
		this.m_aModificationPositions.addLast(iPosition);
	}

	public LinkedList<Integer> getModificationPositions() {
		return this.m_aModificationPositions;
	}
}
