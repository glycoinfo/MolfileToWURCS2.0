package chemicalgraph.subgraph.backbone;

import chemicalgraph.Connection;
import chemicalgraph.ConnectionList;

/**
 * SkeletoneCodeを生成する際に、主鎖に含まれる各炭素＋1原子の状況からどのタイプに属するのかを判定する為のクラス
 * @author KenichiTanaka
 */
public class ConnectType {
	//----------------------------
	// Member variable
	//----------------------------
	public ConnectionList connects = new ConnectionList();;
	public boolean isBackbone;
	public int bondtype;
	public String symbol;
	public int connectedBackboneNum;
		
	//----------------------------
	// Constructor
	//----------------------------
	public ConnectType(final Connection connect, final Backbone backbone){
		this.isBackbone = backbone.contains(connect.atom);
		this.bondtype = connect.bond.type;
		this.symbol = connect.atom.symbol;
		this.connectedBackboneNum = connect.atom.connections.count(backbone);
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
