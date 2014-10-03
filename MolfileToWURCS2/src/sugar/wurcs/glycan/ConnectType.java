package sugar.wurcs.glycan;

import java.util.LinkedList;

import sugar.chemicalgraph.Connection;

/**
 * SkeletoneCodeを生成する際に、主鎖に含まれる各炭素＋1原子の状況からどのタイプに属するのかを判定する為のクラス
 * Class for judge type of connection on backbone to generate SkeltoneCode
 * @author KenichiTanaka
 * @author MasaakiMatsubara
 */
public class ConnectType {
	//----------------------------
	// Member variable
	//----------------------------
	public LinkedList<Connection> connects = new LinkedList<Connection>();
	public boolean isBackbone;
	public int bondtype;
	public String symbol;
	public int connectedBackboneNum;

	//----------------------------
	// Constructor
	//----------------------------
	public ConnectType(final Connection connect, final Backbone backbone){
		this.isBackbone = backbone.contains(connect.endAtom());
		this.bondtype = connect.getBond().getType();
		this.symbol = connect.endAtom().getSymbol();
		int num = 0;
		for( Connection con : connect.endAtom().getConnections() ){
			if ( backbone.contains(con.endAtom()) ) num++;
		}
		this.connectedBackboneNum = num;
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	public boolean is(final int num, final int bondtype, final boolean isElement, final String... elements){
		if(this.connects.size() != num) return false;
		if(this.bondtype != bondtype) return false;
		int elementsNum = elements.length;
		for(int ii=0; ii<elementsNum; ii++){
			if(this.symbol.equals(elements[ii]) != isElement) return false;
		}
		return true;
	}
}
