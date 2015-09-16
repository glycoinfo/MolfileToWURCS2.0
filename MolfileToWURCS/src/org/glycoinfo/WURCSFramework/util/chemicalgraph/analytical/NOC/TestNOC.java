package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.NOC;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Molecule;
import org.glycoinfo.WURCSFramework.exec.FileIOUtils;
import org.glycoinfo.WURCSFramework.io.MDLMOL.CTFileReader;
import org.glycoinfo.WURCSFramework.io.MDLMOL.ParameterReader;

public class TestNOC {

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);

		for ( String t_strFilepath : t_objParam.getCTfileList() ) {
			// New SDFile
			String t_strOutSDFile = t_strFilepath;
			if ( t_strOutSDFile.contains(".mol") )
				t_strOutSDFile = t_strOutSDFile.replace(".mol", "");
			t_strOutSDFile += ".sdf";
			System.out.println(t_strOutSDFile);
			try {
				PrintWriter t_pwNewSDF = FileIOUtils.openTextFileW(t_strOutSDFile);
				readCTFile(t_strFilepath, t_pwNewSDF);
				t_pwNewSDF.close();
			} catch (Exception e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}

	public static void readCTFile(String a_strFilePath, PrintWriter a_pwOut) {
		// read CTFiles
		CTFileReader t_objCTReader = new CTFileReader(a_strFilePath, false);

		while(true){
			// read a record from CTFile
			Molecule mol = t_objCTReader.getMolecule();
			if(mol==null) break;
			String ID = t_objCTReader.getFieldData("ChEBI_ID");
			System.out.println(ID);

			LinkedList<Integer> t_aCarbonIDs = new LinkedList<Integer>();
			NOCApproach t_oNOC = new NOCApproach();
			t_oNOC.countNabourOxygen(mol);
			String t_strPhaseI = "";
			String t_strPhaseII = "";
			HashSet<Atom> t_aMSCarbons = t_oNOC.getMonosaccharideCarbon();
			for ( int i=0; i<mol.getAtoms().size(); i++ ) {
				System.out.print( (i+1)+":" );
				Atom t_oAtom = mol.getAtoms().get(i);
				System.out.print( t_oAtom.getSymbol()+": " );
				if ( !t_oAtom.getSymbol().equals("C") ) {
					System.out.println("-");
					continue;
				}

				if ( !t_strPhaseI.equals("") ) t_strPhaseI += ",";
				t_strPhaseI  += (i+1)+":"+t_oNOC.getNOCNumMapPhase1().get(t_oAtom);
				if ( !t_strPhaseII.equals("") ) t_strPhaseII += ",";
				t_strPhaseII += (i+1)+":"+t_oNOC.getNOCNumMapPhase2().get(t_oAtom);

				System.out.println(
						t_oNOC.getNOCNumMapPhase1().get(t_oAtom)
						+"-"
						+t_oNOC.getNOCNumMapPhase2().get(t_oAtom)
						+"--"+(t_aMSCarbons.contains(t_oAtom))
					);
				if ( t_aMSCarbons.contains(t_oAtom) ) {
					t_aCarbonIDs.add(i+1);
				}
			}
			Collections.sort(t_aCarbonIDs);

			String t_strTag = "> <NOC-PhaseI>\n";
			t_strTag += t_strPhaseI+"\n\n";
			t_strTag += "> <NOC-PhaseII>\n";
			t_strTag += t_strPhaseII+"\n\n";
			// Make hit atoms tag
			t_strTag += "> <HitAtoms>\n";
			String t_strIDList = "";
			for ( int t_iID : t_aCarbonIDs ) {
				if ( !t_strIDList.equals("") ) t_strIDList += ",";
				t_strIDList += t_iID;
			}
			t_strTag += t_strIDList+"\n\n";

			a_pwOut.write( t_objCTReader.getMOLString()+t_strTag+"$$$$\n" );
			a_pwOut.flush();
		}
	}

}
