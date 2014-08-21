package chemicalgraph.subgraph.backbone;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.Connection;

import utility.Chemical;

/**
 * @author KenichiTanaka
 */
public class ConnectTypeList extends LinkedList<ConnectType>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;
	
	public String hybridOrbital;
	public int uniqBackboneNum;
	public int uniqModNum;
	public int maxConnectBackboneNum;
	public String stereo;
	
	//----------------------------
	// Constructor
	//----------------------------
	public ConnectTypeList(final Atom atom, final Backbone backbone){
		for(Connection connect : atom.connections){
			this.addUniqueConnect(connect, backbone);
		}
		
		this.hybridOrbital = atom.hybridOrbital();
		
		this.uniqBackboneNum = 0;
		this.uniqModNum = 0;
		for(ConnectType type : this){
			if(type.isBackbone){
				this.uniqBackboneNum++;
			}else{
				this.uniqModNum++;
			}
		}
		
		this.maxConnectBackboneNum = 0;
		for(ConnectType type : this){
			if(type.isBackbone) continue;
			this.maxConnectBackboneNum = Math.max(this.maxConnectBackboneNum, type.connectedBackboneNum);
		}
		
		Collections.sort(this, new Comparator<ConnectType>() {
			public int compare(ConnectType type1, ConnectType type2) {
				if( type1.isBackbone           != type2.isBackbone          ) return (type1.isBackbone) ? -1 : 1;
				if( type1.connects.size()      != type2.connects.size()     ) return type2.connects.size() - type1.connects.size();
				if( type1.bondtype             != type2.bondtype            ) return type2.bondtype - type1.bondtype;
				if(!type1.symbol.equals(type2.symbol)                       ) return Chemical.GetAtomicNumber(type2.symbol) - Chemical.GetAtomicNumber(type1.symbol);
				if( type1.connectedBackboneNum != type2.connectedBackboneNum) return type2.connectedBackboneNum - type1.connectedBackboneNum;
				return 0; // ここには入らないはず。
			}
		});
	}
	
	//----------------------------
	// Public method
	//----------------------------
	// 追加済みの結合と同一タイプの結合であれば、カウントアップのみを行い。
	// 新しいタイプの結合であれば、新規にリストに追加する。
	// 主鎖炭素との結合は、SkeletoneCode割り当て時の判定文を短くする為、別とみなす。
	public void addUniqueConnect(final Connection connect, final Backbone backbone){
		// 修飾の場合は、同一タイプは一つにまとめる。
		if(!backbone.contains(connect.atom)){
			for(ConnectType type : this){
				if(type.isBackbone                                             ) continue;
				if(type.bondtype != connect.bond.type                          ) continue;
				if(!type.symbol.equals(connect.atom.symbol)                    ) continue;
				if(type.connectedBackboneNum != connect.atom.connections.count(backbone)) continue;
				type.connects.add(connect);
				return;
			}
		}
		
		// 主鎖もしくは新規修飾の場合は新タイプを追加し、connectを新タイプに追加する。
		ConnectType type =  new ConnectType(connect, backbone);
		type.connects.add(connect);
		this.add(type);
	}

	public boolean is(final String hybridOrbital, final Integer backboneNum, final Integer uniqModNum, final String stereo, final Boolean isConnectMultiChains){
		if((hybridOrbital        != null) && (!this.hybridOrbital.equals(hybridOrbital)               )) return false;
		if((backboneNum          != null) && ( this.uniqBackboneNum != backboneNum                    )) return false;
		if((uniqModNum           != null) && ( this.uniqModNum   != uniqModNum                        )) return false;
		if((stereo               != null) && (!this.stereo.equals(stereo)                             )) return false;
		if((isConnectMultiChains != null) && ((this.maxConnectBackboneNum > 1) != isConnectMultiChains)) return false;
		return true;
	}

	// IUPACを参考にして、Stereo判定を行う対象かどうかのチェックをまず入れる。
	// ここでのStereoはIUPACのStereoの決定方法とは異なるので注意
	// 糖鎖の主鎖炭素と隣接原子の種類が等しければ、同一のSKeletoneCodeを出力させたい
	// この為、主鎖炭素と隣接する原子のみを対象として、立体判定を行っている。
	// また、EZ判定では、主鎖炭素の向きが2重結合の両端で等しいかどうかを判定基準として用いている。
	// WURCSを生成する過程において、修飾付加記号の部分で化合物全体の立体が一意になるようにする。
	public void setStereo(final Atom atom, final Backbone backbone, final ConnectType backbone1, final ConnectType backbone2, final ConnectType mod1, final ConnectType mod2, final ConnectType mod3){
		this.stereo = "";
		
		// 楔の状態等からStereo判定対象かどうかの確認

		// sp3 terminal
		if(this.is("sp3", 1, 3, null, true)){
			Connection connectBackbone1 = backbone1.connects.get(0);
			Connection M0 = null;
			Connection M1 = null;
			Connection M2 = null;
			if(mod3.connectedBackboneNum > 1){ M0 = mod3.connects.get(0); M1 = mod1.connects.get(0); M2 = mod2.connects.get(0); }
			if(mod2.connectedBackboneNum > 1){ M0 = mod2.connects.get(0); M1 = mod1.connects.get(0); M2 = mod3.connects.get(0); }
			if(mod1.connectedBackboneNum > 1){ M0 = mod1.connects.get(0); M1 = mod2.connects.get(0); M2 = mod3.connects.get(0); }
			if(backbone.indexOf(connectBackbone1.atom) < backbone.indexOf(atom)){
				this.stereo = Chemical.sp3stereo(connectBackbone1, M0, M1, M2);
			}else{
				this.stereo = Chemical.sp3stereo(M0, connectBackbone1, M1, M2);
			}
			return;
		}

		// sp3 non terminal
		if(this.is("sp3", 2, 2, null, null)){
			Connection connectBackbone1 = backbone1.connects.get(0);
			Connection connectBackbone2 = backbone2.connects.get(0);
			Connection M1 = mod1.connects.get(0);
			Connection M2 = mod2.connects.get(0);
			if(backbone.indexOf(connectBackbone1.atom) < backbone.indexOf(connectBackbone2.atom)){
				this.stereo = Chemical.sp3stereo(connectBackbone1, connectBackbone2, M1, M2);
			}else{
				this.stereo = Chemical.sp3stereo(connectBackbone2, connectBackbone1, M1, M2);
			}
			return;
		}
		
		// sp2 terminal
		if(this.is("sp2", 1, 2, null, null) && backbone1.bondtype == 2){
			Atom A0 = atom;
			Atom B0 = backbone1.connects.get(0).atom;
			Atom A1 = mod1.connects.get(0).atom;
			Atom B1 = null;
			for(Connection connect : B0.connections){
				if(connect.atom == A0) continue;
				if(!backbone.contains(connect.atom)) continue;
				B1 = connect.atom;
				break;
			}
			this.stereo = Chemical.sp2stereo(A0, A1, B0, B1);
			return;
		}
		
		// sp2 non terminal
		if(this.is("sp2", 2, 1, null, null) && backbone1.bondtype == 2){
			Atom A0 = atom;
			Atom B0 = backbone1.connects.get(0).atom;
			Atom A1 = backbone2.connects.get(0).atom;
			Atom B1 = null;
			for(Connection connect : B0.connections){
				if(connect.atom == A0) continue;
				if(!backbone.contains(connect.atom)) continue;
				B1 = connect.atom;
				break;
			}
			this.stereo = Chemical.sp2stereo(A0, A1, B0, B1);
			return;
		}
	}
}
