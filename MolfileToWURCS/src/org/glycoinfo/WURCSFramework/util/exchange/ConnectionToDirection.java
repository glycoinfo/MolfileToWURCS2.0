package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.Chemical;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.AtomIdentifier;
import org.glycoinfo.WURCSFramework.wurcs.graph.CarbonDescriptorInterface;
import org.glycoinfo.WURCSFramework.wurcs.graph.DirectionDescriptor;

public class ConnectionToDirection {

	private LinkedList<Atom> m_aBackboneCarbonChain;

	public ConnectionToDirection(LinkedList<Atom> a_oChain) {
		this.m_aBackboneCarbonChain = a_oChain;
	}

	public DirectionDescriptor convert(Connection a_oConn, CarbonDescriptorInterface a_enumCD) {
		String t_strDirection = "N";
		// Check omittable
		char t_cCD = a_enumCD.getChar();
		if ( t_cCD == 'c' || t_cCD == 'C' || t_cCD == 'M' || t_cCD == 'n' || t_cCD == 'N' )
			t_strDirection = this.getConnectionDirection( a_oConn, this.m_aBackboneCarbonChain );
		return this.convertDirectionDesctiptorString(t_strDirection);
	}

	/**
	 * Get direction for connection from backbone to modification
	 * @param conB2M Connection from backbone to modification
	 * @param chain Backbone carbon chain
	 * @return String of direction which indicate connection direction
	 */
	private String getConnectionDirection(Connection conB2M, LinkedList<Atom> chain) {
//		Atom Co = connectBackboneToMod.start();
//		Atom Mo = connectBackboneToMod.end();
		Atom Co = conB2M.startAtom();
		Atom Mo = conB2M.endAtom();
//		Backbone backbone = Co.backbone;
		LinkedList<Atom> backbone = chain;
		int indexCo = backbone.indexOf(Co);
		Atom CSmall = (Co != chain.getFirst()) ? chain.get(indexCo-1) : null;
		Atom CLarge = (Co != chain.getLast() ) ? chain.get(indexCo+1) : null;
		if ( CSmall==null || CLarge==null ) {
			// Consider ring ester atom as backbone carbon
			for ( Connection con : Co.getConnections() ) {
				Atom conatom = con.endAtom();
				if ( conatom == Mo) continue;
				if ( chain.contains(conatom) ) continue;
				int count = 0;
				for ( Connection concon : conatom.getConnections() ) {
					if ( chain.contains(concon.endAtom()) ) count++;
				}
//				if ( conatom.connections.count(backbone) > 1 ) {
				if ( count > 1 ) {
					if(CSmall==null) CSmall = conatom;
					if(CLarge==null) CLarge = conatom;
				}

			}
		}
//		Connection connectCsmall = (Csmall != null) ? Co.connections.getConnect(Csmall) : null;
//		Connection connectClarge = (Clarge != null) ? Co.connections.getConnect(Clarge) : null;
		Connection conCSmall = null;
		Connection conCLarge = null;
		for ( Connection con : Co.getConnections() ) {
			if ( con.endAtom()==CSmall ) conCSmall = con;
			if ( con.endAtom()==CLarge ) conCLarge = con;
		}

//		ConnectionList connectsMod = new ConnectionList();
		LinkedList<Connection> aConMod = new LinkedList<Connection>();
		for ( Connection con : Co.getConnections() ) {
			if(con == conCSmall) continue;
			if(con == conCLarge) continue;
			aConMod.addLast(con);
		}
		int orderMo = aConMod.indexOf(conB2M)+1;


		// Get orbital for Co
		String orbitalCo = (new AtomIdentifier()).setAtom(Co).getHybridOrbital0();
		if(orbitalCo.equals("sp3")){
			// ?Co-Mo?
//			if(Co.stereoMolecule!=null && Co.stereoMolecule.equals("X")){
			if ( Co.getChirality()!=null && Co.getChirality().equals("X") ) return "X";

			// sp3 non terminal
			if ( CSmall!=null && CLarge!=null ) {
				Connection conOther = (aConMod.get(0)==conB2M) ? aConMod.get(1) : aConMod.get(0);
				String turn = Chemical.sp3stereo(conB2M, conOther, conCLarge, conCSmall);
				if(turn.equals("R")) return "1";
				if(turn.equals("S")) return "2";
				return "0";
			}

			// sp3 terminal
			if ( Co.getChirality()==null ) return "0";

			Connection conC = (CSmall == null)? conCLarge : conCSmall;
			String turn = Chemical.sp3stereo(aConMod.get(0), aConMod.get(1), aConMod.get(2), conC);
//			if(turn.equals("S")) stereoForWURCS = "" + orderMo;
			String stereo = "" + orderMo;
			if(turn.equals("R")){
				if ( orderMo == 2) stereo = "3";
				if ( orderMo == 3) stereo = "2";
			}
			return stereo;
		}

		if(orbitalCo.equals("sp2")){
			// ?Co=Mo? or ?C=Co(-Mo)-? or ?C-C(-Mo)=?
			// Search double bond connection
			Connection conDBond = null;
			for ( Connection con : Co.getConnections() ) {
				if ( con.getBond().getType() != 2 ) continue;
				conDBond = con;
				break;
			}
			String stereoBond = conDBond.getBond().getGeometric();
			if( stereoBond == null     ) return "0";
			if( stereoBond.equals("X") ) return "X";

			// ?Co=Mo?
			if ( conDBond == conB2M ) {
				Connection conMp = null;
				Connection conMs = null;
				for ( Connection con : Mo.getConnections() ) {
					if(con.endAtom() == Co){ continue; }
					if(conMp == null){ conMp = con; continue; }
					if(conMs == null){ conMs = con; continue; }
				}

				// ?-Co=Mo=Mp
				if( conMp.getBond().getType() == 2) return "0";

				int conCType = (CSmall != null)? conCSmall.getBond().getType() : conCLarge.getBond().getType();
				//         Mp
				//        /
				// C=Co=Mo
				//        \
				//         Ms
				// TODO: When the Co is sp?
				if ( conCType == 2 ) return "0";

				// CSmall       Mp
				//       \     / <- conMp
				//        Co=Mo
				//       /     \ <- conMs
				// CLarge/Y     Ms
				if ( CSmall != null ) return Chemical.sp2stereo(Co, CSmall, Mo, conMp.endAtom());

				// Y       Mp
				//  \     / <- conMp
				//   Co=Mo
				//  /     \ <- conMs
				// C       Ms
				Atom Y = null;
				for ( Connection con : Co.getConnections() ) {
					Atom conatom = con.endAtom();
					if(conatom == Mo) continue;
					if(conatom == CLarge) continue;
					Y = conatom;
					break;
				}
				return Chemical.sp2stereo(Co, Y, Mo, conMp.endAtom());
			}

			//        Mo
			//       /
			// ?-C=Co
			//       \
			//        Y
			if ( conDBond == conCSmall || conDBond == conCLarge ) {
				Connection conC = (CSmall != null)? conCSmall : conCLarge;
				Connection conTarget = null;
				// Cが末端でない場合、先に延びている主鎖炭素と比較
				// If C is non-terminal, compare the next backbone carbon
				for(Connection con : conC.endAtom().getConnections()){
					if(  con == conC.getReverse()) continue;
					if( !chain.contains( con.endAtom() ) ) continue;
					conTarget = con;
					break;
				}
				// connectTargetが見つからない場合(Cが末端）、接続している修飾の内CIP優勢な修飾と比較
				// If C is terminal, compare modification of the highest priority
				if ( conTarget==null ) {
					for ( Connection connect : conC.endAtom().getConnections() ) {
						if ( connect.endAtom() == Co) continue;
						conTarget = connect;
						break;
					}
				}

				// TODO: When the Y is modificaiton, how distinguish Mo and Y ?
				//             Mo
				//            /
				// Target=C=Co
				//            \
				//             Y
				if ( conTarget.getBond().getType() == 2 ) return "0";

				// Target      Mo
				//       \    /
				//        C=Co
				//       /    \
				// Target      Y
				return Chemical.sp2stereo(conC.endAtom(), conTarget.endAtom(), Co, Mo);
			}

			// Mo
			//   \
			//    Co=Y?
			//   /
			//  C
			Connection conY = conDBond;
			Connection conYp = null;
			Connection conYs = null;
			for(Connection con : conY.endAtom().getConnections()){
				if(con.endAtom() == Co) continue;
				if(conYp == null){ conYp = con; continue; }
				if(conYs == null){ conYs = con; continue; }
			}

			// Mo
			//   \
			//    Co=Y=Yp
			//   /
			//  C
			if ( conYp.getBond().getType() == 2 ) return "0";

			// Mo      Yp
			//   \    /
			//    Co=Y
			//   /    \
			//  C      Ys
			return Chemical.sp2stereo(conY.endAtom(), conYp.endAtom(), Co, Mo);
		}

		// sp terminal
		// ?Co#Mo? or ?=Co=Mo? or ?#Co-Mo?
		if( orbitalCo.equals("sp") ) return "0";

		return "N";
	}

	/**
	 * Get DirectionDescriptor from DMB string
	 * @param strDMB String of DMB
	 * @return DirectionDescriptor of DMB
	 */
	private DirectionDescriptor convertDirectionDesctiptorString (String strDMB) {
		return	(strDMB.equals("N"))? DirectionDescriptor.N :
				(strDMB.equals("0"))? DirectionDescriptor.N :
				(strDMB.equals("1"))? DirectionDescriptor.U :
				(strDMB.equals("2"))? DirectionDescriptor.D :
				(strDMB.equals("3"))? DirectionDescriptor.T :
				(strDMB.equals("E"))? DirectionDescriptor.E :
				(strDMB.equals("Z"))? DirectionDescriptor.Z :
				(strDMB.equals("X"))? DirectionDescriptor.X : null;
	}

}
