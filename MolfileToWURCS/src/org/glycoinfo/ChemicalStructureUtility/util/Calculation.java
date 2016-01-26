package org.glycoinfo.ChemicalStructureUtility.util;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;

/**
 * Class for calculate inner product and outer product
 * @author KenichiTanaka
 * @author MasaakiMatsubara
 */
public class Calculation {

	//----------------------------
	// Public method
	//----------------------------
	/**
	 * Calculate inner product from three atoms.
	 * @param o Center atom
	 * @param a Side atom 1
	 * @param b Side atom 2
	 * @return Result value of inner product
	 */
	public static double innerProduct(Atom o, Atom a, Atom b){
		double[] ocrd = o.getCoordinate();
		double[] acrd = a.getCoordinate();
		double[] bcrd = b.getCoordinate();
		double result = 0;
		for(int ii=0; ii< 3; ii++){
			result += (acrd[ii]-ocrd[ii])*(bcrd[ii]-ocrd[ii]);
		}
		return result;
	}

	/**
	 * Calculate inner product from two coordinates.
	 * @param a Atom coordinate 1
	 * @param b Atom coordinate 2
	 * @return Result value of inner product
	 */
	public static double innerProduct(double[] a, double[] b){
		int mindim = Math.min(a.length, b.length);
		double result = 0;
		for(int ii=0; ii< mindim; ii++){
			result += a[ii]*b[ii];
		}
		return result;
	}

	/**
	 * Calculate inner product from three coordinates.
	 * @param o Center atom coordinate
	 * @param a Side atom coordinate 1
	 * @param b Side atom coordinate 2
	 * @return Result value of inner product
	 */
	public static double innerProduct(double[] o, double[] a, double[] b){
		int mindim = Math.min(a.length, b.length);
		mindim = Math.min(o.length, mindim);
		double result = 0;
		for(int ii=0; ii< mindim; ii++){
			result += (a[ii]-o[ii])*(b[ii]-o[ii]);
		}
		return result;
	}

	/**
	 * Calculate outer product from three atoms.
	 * @param o Center atom
	 * @param a Side atom 1
	 * @param b Side atom 2
	 * @return Result vector of outer product
	 */
	public static double[] outerProduct(Atom o, Atom a, Atom b) {
		double[] ocrd = o.getCoordinate();
		double[] acrd = a.getCoordinate();
		double[] bcrd = b.getCoordinate();
		double[] output = new double[3];
		output[0] = (acrd[1]-ocrd[1]) * (bcrd[2]-ocrd[2]) - (acrd[2]-ocrd[2]) * (bcrd[1]-ocrd[1]);
		output[1] = (acrd[2]-ocrd[2]) * (bcrd[0]-ocrd[0]) - (acrd[0]-ocrd[0]) * (bcrd[2]-ocrd[2]);
		output[2] = (acrd[0]-ocrd[0]) * (bcrd[1]-ocrd[1]) - (acrd[1]-ocrd[1]) * (bcrd[0]-ocrd[0]);
		return output;
	}

	/**
	 * Calculate outer product from two coordinates.
	 * @param a Atom coordinate 1
	 * @param b Atom coordinate 2
	 * @return Result vector of outer product
	 */
	public static double[] outerProduct(double[] a, double[] b) {
		double[] output = new double[3];
		output[0] = a[1]*b[2] - a[2]*b[1];
		output[1] = a[2]*b[0] - a[0]*b[2];
		output[2] = a[0]*b[1] - a[1]*b[0];
		return output;
	}

	/**
	 * Calculate inner product from three coordinates.
	 * @param o Center atom coordinate
	 * @param a Side atom coordinate 1
	 * @param b Side atom coordinate 2
	 * @return Result vector of outer product
	 */
	public static double[] outerProduct(double[] o, double[] a, double[] b) {
		double[] output = new double[3];
		output[0] = (a[1]-o[1]) * (b[2]-o[2]) - (a[2]-o[2]) * (b[1]-o[1]);
		output[1] = (a[2]-o[2]) * (b[0]-o[0]) - (a[0]-o[0]) * (b[2]-o[2]);
		output[2] = (a[0]-o[0]) * (b[1]-o[1]) - (a[1]-o[1]) * (b[0]-o[0]);
		return output;
	}

}
