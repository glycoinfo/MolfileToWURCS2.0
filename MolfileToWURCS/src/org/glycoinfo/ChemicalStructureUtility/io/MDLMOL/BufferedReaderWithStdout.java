package org.glycoinfo.ChemicalStructureUtility.io.MDLMOL;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Output contents read by BufferedReader.
 * Use for add information to output contents.
 * @author KenichiTanaka
 * @author MasaakiMatsubara
 */
public class BufferedReaderWithStdout extends BufferedReader{

	//----------------------------
	// Constructor
	//----------------------------
	public BufferedReaderWithStdout(FileReader fr){
		super(fr);
//		br = new BufferedReader(fr);
	}

	//----------------------------
	// Public method
	//----------------------------
	public String readLine(boolean outputStdout, String ignore){
		String line = "";
		try {
			line = super.readLine();
			if(line==null) return null;
			if(outputStdout){
				if(ignore==null || !line.equals(ignore)){
					System.out.println(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}

	public String readLine(boolean outputStdout){
		return this.readLine(outputStdout, null);
	}
}
