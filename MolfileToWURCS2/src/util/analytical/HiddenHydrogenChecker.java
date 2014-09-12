package util.analytical;

import java.util.ArrayList;

import chemicalgraph2.Atom;
import chemicalgraph2.Bond;
import chemicalgraph2.Connection;

/**
 *
 * @author Masaaki Matsubara
 */
public class HiddenHydrogenChecker extends AtomIdentifier{
	//
	public HiddenHydrogenChecker(Atom atom) {
		this.setAtom(atom);
	}

	/**
	 * Add hidden hydrogen atoms and calculate coordinates of the atoms.
	 */
	public void addHiddenHydrogens(){
		// Add hidden hydrogen
		ArrayList<Atom> hiddenHydrogens = new ArrayList<Atom>();
		int hiddenHydrogenNumber = this.getHiddenHydrogenNumber();
		if ( hiddenHydrogenNumber == 0 ) return;

		for(int ii=0; ii<hiddenHydrogenNumber; ii++){
			Atom hiddenHydrogen = new Atom("H");
			hiddenHydrogen.setCharge(0);
			new Bond(this.m_objAtom, hiddenHydrogen, 1, 0);
			hiddenHydrogens.add(hiddenHydrogen);
		}

		// Calculate coordinates
		if(hiddenHydrogens.size() == 1){
			double sumX = 0;
			double sumY = 0;
			double sumZ = 0;
			double sumBondLength = 0;
			int    sumBond = 0;
			for(Connection connection : this.m_objAtom.getConnections()){
				if(hiddenHydrogens.contains(connection.endAtom())) continue;
				double[] unitVector3D = connection.unitVector3D();
				sumX += unitVector3D[0];
				sumY += unitVector3D[1];
				sumZ += unitVector3D[2];
				sumBondLength += connection.getBond().length();
				sumBond++;
			}
			Atom hiddenHydrogen = hiddenHydrogens.get(0);
			double[] crd = new double[3];
			double[] atomcrd = this.m_objAtom.getCoordinate();
			crd[0] = atomcrd[0] - (sumX * sumBondLength / sumBond);
			crd[1] = atomcrd[1] - (sumY * sumBondLength / sumBond);
			crd[2] = atomcrd[2] - (sumZ * sumBondLength / sumBond);
			hiddenHydrogen.setCoordinate(crd);
		}else{
			// hiddenHydrogenNumber = 1の場合と同じ処理を取りあえず記述
			double sumX = 0;
			double sumY = 0;
			double sumZ = 0;
			double sumBondLength = 0;
			int    sumBond = 0;
			for(Connection connection : this.m_objAtom.getConnections()){
				if(hiddenHydrogens.contains(connection.endAtom())) continue;
				double[] unitVector3D = connection.unitVector3D();
				sumX += unitVector3D[0];
				sumY += unitVector3D[1];
				sumZ += unitVector3D[2];
				sumBondLength += connection.getBond().length();
				sumBond++;
			}

			for(Atom hiddenHydrogen : hiddenHydrogens){
				double[] crd = new double[3];
				double[] atomcrd = this.m_objAtom.getCoordinate();
				crd[0] = atomcrd[0] - (sumX * sumBondLength / sumBond);
				crd[1] = atomcrd[1] - (sumY * sumBondLength / sumBond);
				crd[2] = atomcrd[2] - (sumZ * sumBondLength / sumBond);
				hiddenHydrogen.setCoordinate(crd);
			}
		}
	}

	/**
	 * Return the number of hidden hydrogen.
	 * @return the number of hidden hydrogen
	 */
	private int getHiddenHydrogenNumber(){
		int numValence = this.m_objAtom.getConnections().size();
		// sp2
		if(this.is("C" , 0, 2, 1, 0)) return 3 - numValence;
		if(this.is("Si", 0, 2, 1, 0)) return 3 - numValence;
		if(this.is("Ge", 0, 2, 1, 0)) return 3 - numValence;
		if(this.is("N" , 0, 1, 1, 0)) return 2 - numValence;
		if(this.is("N" , 1, 2, 1, 0)) return 3 - numValence;
		if(this.is("O",  0, 0, 1, 0)) return 1 - numValence;

		// sp3
		if(this.is("B", -1, 4, 0, 0)) return 4 - numValence;

		if(this.is("C",  0, 4, 0, 0)) return 4 - numValence;
		if(this.is("Si", 0, 4, 0, 0)) return 4 - numValence;
		if(this.is("Ge", 0, 4, 0, 0)) return 4 - numValence;
		if(this.is("Sn", 0, 4, 0, 0)) return 4 - numValence;

		if(this.is("N",  1, 4, 0, 0)) return 4 - numValence;
		if(this.is("N",  0, 3, 1, 0)) return 4 - numValence;
		if(this.is("N",  0, 3, 0, 0)) return 3 - numValence;
		if(this.is("P",  1, 4, 0, 0)) return 4 - numValence;
		if(this.is("P",  0, 3, 1, 0)) return 4 - numValence;
		if(this.is("As", 1, 4, 0, 0)) return 4 - numValence;

		if(this.is("O",  0, 2, 0, 0)) return 2 - numValence;
		if(this.is("S",  1, 3, 1, 0)) return 4 - numValence;
		if(this.is("S",  1, 3, 0, 0)) return 3 - numValence;
		if(this.is("S",  0, 2, 2, 0)) return 4 - numValence;
		if(this.is("S",  0, 2, 1, 0)) return 3 - numValence;
		if(this.is("Se", 1, 3, 1, 0)) return 4 - numValence;
		if(this.is("Se", 1, 3, 0, 0)) return 3 - numValence;
		if(this.is("Se", 0, 2, 2, 0)) return 4 - numValence;
		if(this.is("Se", 0, 2, 1, 0)) return 3 - numValence;

		// sp ※InChIのドキュメントになかったので気がついたケースについて記述
		// 主鎖炭素判定に必要な分をとりあえず記述
		if(this.is("C" , 0, 1, 0, 1)) return 2 - numValence;
		if(this.is("C" , 0, 0, 2, 0)) return 2 - numValence;

		return 0;
	}

}
