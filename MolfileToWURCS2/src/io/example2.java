package io;

import java.io.IOException;
import java.util.ArrayList;

import util.WURCSGlycanImporter;
import util.analytical.MoleculeNormalizer;
import util.analytical.StereochemicalAnalyzer;
import util.analytical.StructureAnalyzer;
import chemicalgraph2.Atom;
import chemicalgraph2.subgraph.Molecule;

public class example2 extends example {

	public static void main(String[] args) {
		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);

		WURCSGlycanImporter t_objGlycanImporter = new WURCSGlycanImporter();
		t_objGlycanImporter.setBackboneParameters(t_objParam.m_minNOS, t_objParam.m_minO, t_objParam.m_minBackboneLength, t_objParam.m_maxBackboneLength, t_objParam.m_ratioBackboneNOS);

		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		for ( String t_strFilepath : t_objParam.getCTfileList() ){
			// read CTFiles
			CTFileReader t_objCTReader = new CTFileReader(t_strFilepath, t_objParam.m_sdfileOutput);
			while(true){
				// read a record from CTFile
				Molecule mol = t_objCTReader.getMolecule();
				if(mol==null) break;
				if(!t_objParam.m_sdfileOutput){
					System.out.print( t_objCTReader.getFieldData(t_objParam.m_ID) );
				}
				System.err.println( t_objCTReader.getFieldData("ID") );

				// Normalize molecule
				MoleculeNormalizer t_objMolNorm = new MoleculeNormalizer();
				t_objMolNorm.normalize(mol);

				// Analyze stereochemistry
				StereochemicalAnalyzer t_objStereo = new StereochemicalAnalyzer();
				t_objStereo.analyze(mol);
				for ( Atom atom : mol.getAtoms() ) {
					if ( t_objStereo.getStereo(atom) == null ) continue;
					System.err.println( t_objStereo.getStereo(atom) );
				}

				StructureAnalyzer t_objSA = new StructureAnalyzer();
				t_objSA.analyze(mol);

//				mol.generateWURCS(t_objParam.m_minNOS, t_objParam.m_minO, t_objParam.m_minBackboneLength, t_objParam.m_maxBackboneLength, t_objParam.m_ratioBackboneNOS);  //Issaku YAMADA
				// generate WURCS
/*				try {
					t_objGlycanImporter.generateWURCS(mol);
				} catch (WURCSGlycanObjectException e) {
					e.printStackTrace();
				}
*/
				if(mols!=null) mols.add(mol);
			}

			// close CTfile
			try {
				t_objCTReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
