package utility;

import chemicalgraph.Atom;

/**
 * 内積計算および外積計算
 * @author KenichiTanaka
 */
public class Calculation {

	//----------------------------
	// Public method
	//----------------------------
	public static double innerProduct(Atom o, Atom a, Atom b){
		double result = 0;
		for(int ii=0; ii< 3; ii++){
			result += (a.coordinate[ii]-o.coordinate[ii])*(b.coordinate[ii]-o.coordinate[ii]);
		}
		return result;
	}

	public static double innerProduct(double[] a, double[] b){
		int mindim = Math.min(a.length, b.length);
		double result = 0;
		for(int ii=0; ii< mindim; ii++){
			result += a[ii]*b[ii];
		}
		return result;
	}

	public static double innerProduct(double[] o, double[] a, double[] b){
		int mindim = Math.min(a.length, b.length);
		mindim = Math.min(o.length, mindim);
		double result = 0;
		for(int ii=0; ii< mindim; ii++){
			result += (a[ii]-o[ii])*(b[ii]-o[ii]);
		}
		return result;
	}

	/* 外積計算 */
	public static double[] outerProduct(Atom o, Atom a, Atom b) {
		double[] output = new double[3];
		output[0] = (a.coordinate[1]-o.coordinate[1]) * (b.coordinate[2]-o.coordinate[2]) - (a.coordinate[2]-o.coordinate[2]) * (b.coordinate[1]-o.coordinate[1]);
		output[1] = (a.coordinate[2]-o.coordinate[2]) * (b.coordinate[0]-o.coordinate[0]) - (a.coordinate[0]-o.coordinate[0]) * (b.coordinate[2]-o.coordinate[2]);
		output[2] = (a.coordinate[0]-o.coordinate[0]) * (b.coordinate[1]-o.coordinate[1]) - (a.coordinate[1]-o.coordinate[1]) * (b.coordinate[0]-o.coordinate[0]);
		return output;
	}

	public static double[] outerProduct(double[] a, double[] b) {
		double[] output = new double[3];
		output[0] = a[1]*b[2] - a[2]*b[1];
		output[1] = a[2]*b[0] - a[0]*b[2];
		output[2] = a[0]*b[1] - a[1]*b[0];
		return output;
	}

	public static double[] outerProduct(double[] o, double[] a, double[] b) {
		double[] output = new double[3];
		output[0] = (a[1]-o[1]) * (b[2]-o[2]) - (a[2]-o[2]) * (b[1]-o[1]);
		output[1] = (a[2]-o[2]) * (b[0]-o[0]) - (a[0]-o[0]) * (b[2]-o[2]);
		output[2] = (a[0]-o[0]) * (b[1]-o[1]) - (a[1]-o[1]) * (b[0]-o[0]);
		return output;
	}

}
