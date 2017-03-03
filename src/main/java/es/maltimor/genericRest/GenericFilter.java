package es.maltimor.genericRest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericFilter {
	public static final String ERROR = "***ERROR***";

	public static List<String> parseFilter(String text) {
		if (text==null||text.equals("")) return null;

		// optimizacion
		// Map<String,GenericMapperInfoColumn> map = new
		// HashMap<String,GenericMapperInfoColumn>();

		// reemplazo AND Y OR por & y |
		text = text.replace(" AND ", "&").replace(" OR ", "|");
		System.out.println("SEARCH=" + text);

		// analizo sintacticamente la cadena
		char[] buff = text.toCharArray();
		int len = buff.length;
		List<String> lst = new ArrayList<String>();
		int pos = 0;
		String token = "";
		int estado = 0;
		String key = "";
		String op = "";
		String valor = "";
		while (pos < len) {		//TODO MAXIMO TAMAÑO DEL BUFFER!
			char act = buff[pos];
			// System.out.println("act="+act+" estado="+estado);
			// System.out.println(estado+"|"+key+"|"+op+"|"+valor);
			switch (estado) {
			case 0:
				// paso inicial
				if (act == '[') {
					estado = 1;
					key = "";
					valor = "";
					op = "";
				} else if ("]<>=&|".indexOf(act) >= 0) {
					estado = -1;
				} else {
					key = "NULL";
					op = "NULL";
					//ahora valor puede ser del tipo [key]
					if (act=='['){
						valor="[";
						estado=4;
					} else {
						valor += act;
						estado = 3;
					}
				}
				break;
			case 1:
				// evaluando [ ->k ]=valor
				if (act == ']') {
					estado = 2;
				} else {
					key += act;
				}
				break;
			case 2:
				// evaluando [k] -> op valor
				if ("<>=".indexOf(act) >= 0) {
					op += act;
					// veo el siguiente char
					if (pos + 1 < len) {
						if ("<>=".indexOf(buff[pos + 1]) >= 0) {
							pos++;
							op += buff[pos];
						}
					}
					estado = 3;
				} else if (pos+4<len && text.substring(pos,pos+4).equalsIgnoreCase(" IS ")){
					pos+=3;//1 menos que lo necesario ya s eincrementa despues
					op += " IS ";
					estado = 3;
				/*} else if (pos+4<len && text.substring(pos,pos+4).equals(" IN ")){
					pos+=3;//1 menos que lo necesario ya s eincrementa despues
					op += " IN ";
					estado = 3;*/
				} else estado = -1;
				break;
			case 3:
				// evluanbdo [k] op -> valor
				// ahora valor puede ser del tipo [key]
				if (act=='['){
					estado=4;
					valor="[";
				} else if ("&|".indexOf(act) == -1) {
					valor += act;
				} else {
					//act=& o |
					// System.out.println("PUSH_"+act+"= key="+key+" valor="+valor+" op="+op);
					lst.add("[" + key + "|" + op + "|" + valor);
					lst.add("" + act);
					valor = "";
					estado = 0;
				}

				break;
			case 4:
				// evaluando ... [->valor]
				if (act == ')') {
					estado = -1;
				} else if (act == ']') {
					estado = 3;
				} else {
					valor += act;
				}
				break;
			}
			pos++;
		}
		//TODO QUE HACER CUANDO EL ESTADO es <> de 0 y 3 lanzar una excepcion????
		if (estado != 0 && estado!=3){
			lst.clear();
			lst.add(GenericFilter.ERROR);
			System.out.println(GenericFilter.ERROR);
		} else if (estado == 3) {
			// finalizo este caso especial
			// System.out.println("PUSH_END= key="+key+" valor="+valor+" op="+op);
			lst.add("[" + key + "|" + op + "|" + valor);
			estado = 0;
		}
		return lst;
	}
}
