package es.maltimor.genericRest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.maltimor.genericUser.User;
import es.maltimor.webUtils.MapperUtils;

@SuppressWarnings("rawtypes")
public class GenericServiceMapperProvider {
	final Logger log = LoggerFactory.getLogger(GenericServiceMapperProvider.class);
	
	//user,table,info,filter
	public String cntAll(Map params) {
		//campos obligatorios
		User user = (User) params.get("user");
		String table = (String) params.get("table");
		UriInfo ui = (UriInfo) params.get("ui");
		Map<String,String> query = (Map<String,String>) params.get("query");
		GenericMapperInfoTable info = (GenericMapperInfoTable) params.get("info");
		GenericMapperInfoTableResolver resolver = info.getResolver();
		String driver = info.getDriver();
		List<String> fields = new ArrayList<String>();
		List<GenericMapperInfoColumn> columns =  info.getFields();
		for (GenericMapperInfoColumn column : columns) {
			fields.add(column.getName());
		}
		String filter = (String) params.get("filter");
		if (filter==null) filter="";
		else filter = filter.replace("'", "''");
		
//		String sql = "SELECT count(*) AS COUNT FROM ("+resolver.getSQL(user,table)+")";
		String sql = "SELECT count(*) FROM ("+resolver.getSQL(user,table)+")";
		if (driver.equals("mysql") || driver.equals("informix") || driver.equals("access")) sql+=" t";
//		if (fields!=null && fields.size()>0 && !filter.equals("")){
//			//sql += filtrosWhere(info, fields, filter);
//			sql += " WHERE "+getQueryWhere(filter,info);
//		}
		
		String actualFilter = "";
		if (fields!=null && fields.size()>0 && !filter.equals("")) actualFilter = getQueryWhere(filter,info);
		actualFilter = resolver.getQueryWhere(user, table, info, filter, ui, query, "cntAll", actualFilter);
		if (!actualFilter.equals("")) sql+= " WHERE "+actualFilter;
		
		log.debug("### cntAll:"+sql);
		
		return sql;
	}

	//user,table,info,filter,limit,offset,orderby,order,fields
	@SuppressWarnings("unchecked")
	public String getAll(Map params) throws Exception{
		User user = (User) params.get("user");
		String table = (String) params.get("table");
		String lfields = (String) params.get("fields");
		UriInfo ui = (UriInfo) params.get("ui");
		Map<String,String> query = (Map<String,String>) params.get("query");
		
		GenericMapperInfoTable info = (GenericMapperInfoTable) params.get("info");
		GenericMapperInfoTableResolver resolver = info.getResolver();
		String driver = info.getDriver();
		List<String> fields = new ArrayList<String>();
		List<String> types = new ArrayList<String>();
		List<GenericMapperInfoColumn> columns =  info.getFields();
		//incorporo aqui la lista de campos que se van a exportar
		// TODO elimino los BLOB y CLOB AQUI y en esta llamada
		if (lfields.equals("*")){
			for (GenericMapperInfoColumn column : columns) {
//				if (!column.getType().endsWith("LOB")) {
					fields.add(column.getName().toUpperCase());
					types.add(column.getType());
//				}
			}
		} else {
			//Se supone que la lista de nombres viene separada por comas, como truco se inserta una coma al principio y a l final
			lfields = (","+lfields+",").toUpperCase();
			for (GenericMapperInfoColumn column : columns) {
				String name = column.getName().toUpperCase();
				if (lfields.contains(","+name+",")) {
					fields.add(name);
					types.add(column.getType());
				}
			}
		}
		//for(int i=0;i<fields.size();i++){
			//System.out.println("FIELD["+i+"]="+fields.get(i)+"|"+types.get(i)+"|");
		//}

		String filter = (String) params.get("filter");
		String orderby = (String) params.get("orderby");
		String order = (String) params.get("order");
		//campos opcionales
		if (!params.containsKey("limit")) params.put("limit",30);	//TODO
		if (!params.containsKey("offset")) params.put("offset",0);
		Long offset = (Long) params.get("offset");
		Long limit = (Long) params.get("limit");
		
		//ORDERBY debe estar dentro de los fields
		//permito que orderby y order sean una lista separada por comas
		String orderClause=null;
		if (orderby!=null && order!=null){
			orderby=orderby.toUpperCase();
			order=order.toUpperCase();
			String[] ordersby=orderby.split(",");
			String[] orders=order.split(",");
			if (ordersby.length!=orders.length) throw new Exception("Diferente tamaño en la lista de ordenacion:"+orderby+"-"+order);
			for(String oby:ordersby) if (!fields.contains(oby)) throw new Exception("No se puede ordenar por "+oby+" : "+fields+" : "+ordersby);
			for(String o:orders) if (!"ASC,DESC".contains(o)) throw new Exception("Parametro order incorrecto.");
			//llegado aqui me he asegurado que los datos son correctos: reconstruyo la clausula order
			orderClause="";
			for(int i=0;i<ordersby.length;i++){
				orderClause+=", "+ordersby[i]+" "+orders[i];
			}
			orderClause=orderClause.substring(1);
		}

		if (filter==null) filter="";
		else filter = filter.replace("'", "''");

		String sql = "";
		if (driver.equals("mysql") || driver.equals("informix") || driver.equals("access")){
			sql+= "SELECT ";
			if (driver.equals("informix")) sql+=" SKIP "+(offset)+" LIMIT "+(limit)+" ";
			if (driver.equals("access")) sql+=" TOP "+(limit)+" ";
			if (fields!=null && fields.size()>0){
				for(int i=0;i<fields.size();i++){
					//AÑADIR CAMBIO DE DATO PARA FECHAS PARA ACOMODARLO A UN UNICO ESTANDAR YYYY-MM-DD HH:MM:SS
					// TODO Gestion ferchas en informix falta añadir tambien el nombre del alias entre ""
					String type=types.get(i);
					if (type.equals("F")) {
						if (driver.equals("mysql") || driver.equals("informix")) sql+="DATE_FORMAT(t."+fields.get(i)+",'%Y-%m-%dT%H:%i:%s') AS "+fields.get(i);
						else if (driver.equals("access")) sql+="FORMAT(t."+fields.get(i)+",'yyyy-mm-ddThh:nn:ss') AS "+fields.get(i);
					} else if (type.equals("D")) {
						if (driver.equals("mysql") || driver.equals("informix")) sql+="DATE_FORMAT(t."+fields.get(i)+",'%Y-%m-%d') AS "+fields.get(i);
						else if (driver.equals("access")) sql+="FORMAT(t."+fields.get(i)+",'yyyy-mm-dd') AS "+fields.get(i);
					} else sql+="t."+fields.get(i)+(driver.equals("informix")?" AS \""+fields.get(i).toUpperCase()+"\"":"");
					if (i<fields.size()-1) sql+=", ";
				}
			}
			sql+=" FROM ("+resolver.getSQL(user,table)+") t";
//			if (fields!=null && fields.size()>0 && !filter.equals("")){
//				//sql += filtrosWhere(info, fields, filter);
//				sql += " WHERE "+getQueryWhere(filter,info);
//			}
			String actualFilter = "";
			if (fields!=null && fields.size()>0 && !filter.equals("")) actualFilter = getQueryWhere(filter,info);
			actualFilter = resolver.getQueryWhere(user, table, info, filter, ui, query, "getAll", actualFilter);
			if (!actualFilter.equals("")) sql+= " WHERE "+actualFilter;

			if (driver.equals("mysql")){
				if (orderClause!=null) sql+=" ORDER BY "+orderClause;
				sql+=" LIMIT "+(offset)+" , "+(limit);
			} else {
				//informix
				if (orderClause!=null) sql+=" ORDER BY "+orderClause.toLowerCase();
			}
		} else {
			sql+= "SELECT * FROM (SELECT ";
			if (fields!=null && fields.size()>0){
				for(int i=0;i<fields.size();i++){
					//AÑADIR CAMBIO DE DATO PARA FECHAS PARA ACOMODARLO A UN UNICO ESTANDAR YYYY-MM-DD HH:MM:SS
					String type=types.get(i);
					if (type.equals("F")) sql+="TO_CHAR("+fields.get(i)+",'YYYY-MM-DD\"T\"HH24:MI:SS') AS "+fields.get(i);
					else if (type.equals("D")) sql+="TO_CHAR("+fields.get(i)+",'YYYY-MM-DD') AS "+fields.get(i);
					else sql+=fields.get(i);
					sql+=", ";
				}
			}
			if (orderClause!=null) sql+="ROW_NUMBER() OVER (ORDER BY "+orderClause+" ) rnk ";
			else sql+="ROWNUM rnk ";
			sql+="FROM ("+resolver.getSQL(user,table)+") t";
//			if (fields!=null && fields.size()>0 && !filter.equals("")){
//				//sql += filtrosWhere(info, fields, filter);
//				sql += " WHERE "+getQueryWhere(filter,info);
//			}
			String actualFilter = "";
			if (fields!=null && fields.size()>0 && !filter.equals("")) actualFilter = getQueryWhere(filter,info);
			actualFilter = resolver.getQueryWhere(user, table, info, filter, ui, query, "getAll", actualFilter);
			if (!actualFilter.equals("")) sql+= " WHERE "+actualFilter;
			
			sql+=" ) t2 WHERE ( rnk BETWEEN "+(offset+1)+" AND "+(offset+limit)+" ) ";
		}
		
		log.debug("### getAll:"+sql);
		//System.out.println("LLAMADA SQL: "+sql);
		return sql;
	}

	//user,table,info,id
	public String getById(Map params){
		User user = (User) params.get("user");
		String table = (String) params.get("table");
		String id = (String) params.get("id"); 
		GenericMapperInfoTable info = (GenericMapperInfoTable) params.get("info");
		GenericMapperInfoTableResolver resolver = info.getResolver();
		String driver = info.getDriver();
		List<String> keys = info.getKeys();
		splitKeys(params,info,id);
		
		List<String> fields = new ArrayList<String>();
		List<String> types = new ArrayList<String>();
		List<GenericMapperInfoColumn> columns =  info.getFields();
		//incorporo aqui la lista de campos que se van a exportar
		for (GenericMapperInfoColumn column : columns) {
			fields.add(column.getName().toUpperCase());
			types.add(column.getType());
		}

		String sql = "SELECT ";
		if (driver.equals("mysql") || driver.equals("informix") || driver.equals("access")){
			for(int i=0;i<fields.size();i++){
				//TODO tratar fechas añadir "" en informix
				String type=types.get(i);
				if (type.equals("F")) {
					if (driver.equals("mysql") || driver.equals("informix")) sql+="DATE_FORMAT(t."+fields.get(i)+",'%Y-%m-%dT%H:%i:%s') AS "+fields.get(i);
					else if (driver.equals("access")) sql+="FORMAT(t."+fields.get(i)+",'yyyy-mm-ddThh:nn:ss') AS "+fields.get(i);
				} else if (type.equals("D")) {
					if (driver.equals("mysql") || driver.equals("informix")) sql+="DATE_FORMAT(t."+fields.get(i)+",'%Y-%m-%d') AS "+fields.get(i);
					else if (driver.equals("access")) sql+="FORMAT(t."+fields.get(i)+",'yyyy-mm-dd') AS "+fields.get(i);
				} else sql+="t."+fields.get(i)+(driver.equals("informix")?" AS \""+fields.get(i).toUpperCase()+"\"":"");
				if (i<fields.size()-1) sql+=", ";
			}
		} else {
			for(int i=0;i<fields.size();i++){
				String type=types.get(i);
				if (type.equals("F")) sql+="TO_CHAR("+fields.get(i)+",'YYYY-MM-DD\"T\"HH24:MI:SS') AS "+fields.get(i);
				else if (type.equals("D")) sql+="TO_CHAR("+fields.get(i)+",'YYYY-MM-DD') AS "+fields.get(i);
				else sql+=fields.get(i);
				if (i<fields.size()-1) sql+=", ";
			}
		}
		sql+=" FROM ("+resolver.getSQL(user,table)+")";
		
		if (driver.equals("mysql") || driver.equals("informix") || driver.equals("access")) sql+=" t";
		sql+=" WHERE ";
		for(int i=0;i<keys.size();i++) {
			//TODO keys de tipo fecha
			sql+=keys.get(i)+"=#{id"+i+"}";
			if (i<keys.size()-1) sql+=" AND ";
		}
		log.debug("### getById:"+sql);
		return sql;
	}

	//user,table,data,info
	@SuppressWarnings("unchecked")
	public String insert(Map params){
		User user = (User) params.get("user");
		String table = (String) params.get("table");
		
		GenericMapperInfoTable info = (GenericMapperInfoTable) params.get("info");
		GenericMapperInfoTableResolver resolver = info.getResolver();
		Map<String, Object> map = (Map<String, Object>) params.get("data");		
		
		for (GenericMapperInfoColumn column : info.getFields()) {
			trataTipo(info, map, column.getName());
		}
		
		String res = resolver.getInsert(user, table, info, map);

		log.debug("insert:"+res);
		//System.out.println("LLAMADA PARA INSERTAR:" + res);
		return res;
	}

	//user,table,data,info,id
	@SuppressWarnings("unchecked")
	public String update(Map params){
		User user = (User) params.get("user");
		String table = (String) params.get("table");	
		String id = (String) params.get("id"); 

		GenericMapperInfoTable info = (GenericMapperInfoTable) params.get("info");
		GenericMapperInfoTableResolver resolver = info.getResolver();
		Map<String, Object> map = (Map<String, Object>) params.get("data");
		
		//System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ UPDATE");
		
		splitKeys(params,info,id);

		for (GenericMapperInfoColumn column : info.getFields()) {
			trataTipo(info, map, column.getName());
		}
		
		
		String sql = resolver.getUpdate(user,table,info,id,map);

		log.debug("update:"+sql);
		return sql;
	}

	//user,table,info,id
	public String delete(Map params){
		User user = (User) params.get("user");
		String table = (String) params.get("table");
		String id = (String) params.get("id"); 
		GenericMapperInfoTable info = (GenericMapperInfoTable) params.get("info");
		GenericMapperInfoTableResolver resolver = info.getResolver();

		//mejorar esto para que admita otros rangos e incluso nulos en id
		//actualmente keys.size=arr.size
		//TODO de momento solo dejo el caso super especial de id=null
		if (id!=null) splitKeys(params,info,id);

		String sql = resolver.getDelete(user,table,info,id);
		
		log.debug("### delete:"+sql);
		return sql;
	}

	//user,table,data,info
	@SuppressWarnings("unchecked")
	public String execute(Map params){
		User user = (User) params.get("user");
		String table = (String) params.get("table");
		
		GenericMapperInfoTable info = (GenericMapperInfoTable) params.get("info");
		GenericMapperInfoTableResolver resolver = info.getResolver();
		Map<String, Object> map = (Map<String, Object>) params.get("data");		
		
		for (GenericMapperInfoColumn column : info.getFields()) {
			trataTipo(info, map, column.getName());
		}
		
		String res = resolver.getExecute(user, table, info, map);

		log.debug("insert:"+res);
		//System.out.println("LLAMADA PARA INSERTAR:" + res);
		return res;
	}
	
	//secuence
	public String getSecuenceValue(String secuence){
		//String secuence = (String) params.get("secuence");
		String res = "SELECT "+secuence+".CURRVAL AS VALUE FROM DUAL";
		//System.out.println("SECUENCE:"+secuence);
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private void splitKeys(Map params, GenericMapperInfoTable info, String id){
		if (id==null) return;
		List<String> keys = info.getKeys();
		String separator = info.getKeySeparator();
		String[] arr = id.split(separator);
		for(int i=0;i<keys.size();i++){
			String key = keys.get(i);
			String field = "id"+i;
			params.put(field, arr[i]);
			trataTipo(info,params,key,field);
		}
	}

	private void trataTipo(GenericMapperInfoTable table, Map<String, Object> map, String key) {
		trataTipo(table,map,key,key);
	}
	
	private void trataTipo(GenericMapperInfoTable table, Map<String, Object> map, String keyIn, String keyOut) {
/*		Map<String,String> types = new HashMap<String, String>();
		for (GenericMapperInfoColumn column : table.getFields()) {
			types.put(column.getName().toLowerCase(), column.getType());
		}*/
		
		//System.out.println("### TRATA TIPO:"+keyIn+" -> "+keyOut);
		
		
		Map<String,String> types = table.getTypes();
		
		//System.out.println("### TRATA TIPO: tipo:"+types.get(keyIn.toLowerCase()));
		
		String type=types.get(keyIn.toLowerCase());
		if (type.equals("T")) {
			//TODO : dara error si supera el tamaÃ±o establecido
			try {
				//TODO : convertir a un varchar de tamaÃ±o dado 
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (type.equals("F")|type.equals("D")) {
			try {
				//System.out.println("### TRATA TIPO: TODATE");
				
				MapperUtils.toDate(map, keyOut);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (type.equals("N")){
			try {
				MapperUtils.toDouble(map, keyOut);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String filtrosWhere(GenericMapperInfoTable table, List<String> fields, String filter) {
		//diferencia si el campo es o no una fecha.
		String sql=" WHERE ";		
		
		for(int i=0;i<fields.size();i++){
			String field=fields.get(i);
			
			String type = "";
			for (GenericMapperInfoColumn column : table.getFields()) {
				if (column.getName().equals(field.toLowerCase()))
					type = column.getType();
			}
			
			if (type.equals("F")) 
				sql+="UPPER( TO_CHAR("+field+", 'DD-MM-YYYY') ) like '%"+filter.toUpperCase()+"%'";
			else
				//TODO : pitch insensitive
				sql+="UPPER("+field+") like '%"+filter.toUpperCase()+"%'";
			if (i<fields.size()-1) sql+=" OR ";
		}
		return sql;
	}
	
	//filtra la cadena eliminando todos los caracteres de control de MyBatis
	//{}'"
	//TODO ver si elimino $ # 
	//TODO ver si transformo ' por '' y si es necesario eliminar las "
	private String filterValue(String value) {
		return value.replace("{", "").replace("}", "").replace("'", "").replace("\"","");
	}
	
	/*
	 * cols se utiliza para obtener informacion de la columnas, sobre todo el tipo de dato, si es key y si actua en fulltext
	 * Dado una query por url siguiendo la sintaxis:
	 * en el query estring se definen estos parametros:
	 * query=[sintaxis_query]
	 * start=[offset a partir del cual se empieza a mostrar datos, por defecto es 0]
	 * count=[numero de registros que se devuevle, por defecto es 30]
	 * order=[sintaxis_order]
	 * Sintaxis query:
	 * Q -> valor
	 * Q -> '[' key ']' op valor
	 * Q -> Q AND Q  -- AND
	 * Q -> Q OR Q  -- OR
	 * valor -> cadena de texto
	 * key -> nombre del campo
	 * op -> =, >, <, >=, <=, ==
	 * Sintaxis order:
	 * O -> Campo [ASC|DESC] [, O]
	 * Ejemplos de la sintaxis:
	 * ....?start=100&count=5&order=Campo1 ASC, Campo2 DESC&query=[Campo1]=Andres AND [Campo2]>5
	 */
	private String getQueryWhere(String text,GenericMapperInfoTable table){
		List<String> lst = GenericFilter.parseFilter(text);
		//System.out.println("********************* "+lst);
		if (lst==null) return "";
		if (lst.get(0).equals(GenericFilter.ERROR)) return "(1=0)";
		
		String res = "";
		String driver = table.getDriver();
		
		List<GenericMapperInfoColumn> cols = table.getFields();
		
		//optimizacion
		Map<String,GenericMapperInfoColumn> map = new HashMap<String,GenericMapperInfoColumn>();
		for(GenericMapperInfoColumn col:cols) map.put(col.getName().toLowerCase(), col);
		
		//analizo semanticamente la cadena
		res = "";
		int TYPE_TEXT = 1;
		int TYPE_NUMBER=2;
		int TYPE_DATE = 3;

		for(String cad:lst){
			//aplico el filtro de manera global aqui: ahorro hacerlo muchas veces
			//esto debería evitar el sql injection
			cad = filterValue(cad);
			//System.out.println("************** "+cad);
			
			//System.out.println("*"+cad);
			//por defecto el tipo de datos es texto
			int type=TYPE_TEXT;
			if (cad.startsWith("[")){
				String[] aux = cad.substring(1).split("\\|",3);
				String key = aux[0];
				String op = aux[1];
				String valor = (aux.length>2)? aux[2] : "";
				boolean valorKey = valor.startsWith("[");
				if (valorKey) valor = "#{"+valor.substring(1)+"}";	//convierto la expresion por algo que ibatis entiende
				
				//aqui deberia recuperar el tipo de dato segun la columna
				GenericMapperInfoColumn col = null;
				if (!key.equals("NULL")) {
					col = map.get(key.toLowerCase());
					if (col!=null) {
						String strType = col.getType().toUpperCase();
						if (strType.equals("T")) type = TYPE_TEXT;
						else if (strType.equals("N")) type = TYPE_NUMBER;
						else if (strType.equals("F")) type = TYPE_DATE;
						else if (strType.equals("D")) type = TYPE_DATE;
						else type = TYPE_TEXT;
						//System.out.println("COL:"+col);
					} else key = "#{"+key+"}";	//evita sql injection?
				}

				//tengo en cuenta que por defecto todos los operadores añaden % al principio y al final
				//= ==  <>  !== !=
				if (op.equals("=")||op.equals("!=")) {
					//si el tipo de datos es TEXT la semantica es del tipo LIKE
					//en otro caso el = el lo mismo que ==
					if (type==TYPE_TEXT){
						if (!valorKey) valor = "%"+valor.replace("*", "%")+"%";
						op = op.equals("=")? " LIKE " : " NOT LIKE ";
					} else if (type==TYPE_DATE){
						//comprobar que sea una fecha y si no lo es poner NULL en el campo
						try {
				            SimpleDateFormat formatoFecha = new SimpleDateFormat("d/M/yy");
				            formatoFecha.setLenient(false);
				            //formatoFecha.parse(valor);
				        } catch (Exception e) {
							//System.out.println("Fecha NO valida:"+valor);
				        	valor="";
				        }
					}
					//aplico la transformacion a valor en funcion de su tipo de datos
					if (!valorKey&&((type==TYPE_TEXT)||(type==TYPE_DATE))) valor = "'"+valor+"'";
				} else if (op.equals("==")||op.equals("!==")){
					//este operador es identico a = excepto para TYPE_TEXT que permite comodines
					if (type==TYPE_TEXT){
						//si el valor contiene comodin, en vez de = pongo like
						if (valor.contains("%")) op = op.equals("==")? " LIKE " : " NOT LIKE ";
						else if (valor.contains("*")){
							valor = valor.replace("*", "%");
							op = op.equals("==")? " LIKE " : " NOT LIKE ";
						} else op = op.equals("==")? "=" : "!=";
					} else {
						//en cualquier otro caso, el == es lo mismo que =
						op = op.equals("==")? "=" : "!=";
					}
					//aplico la transformacion a valor en funcion de su tipo de datos
					if (!valorKey&&((type==TYPE_TEXT)||(type==TYPE_DATE))) valor = "'"+valor+"'";
				} else if (op.equals("=>")){
					//aplico la transformacion a valor en funcion de su tipo de datos
					if (!valorKey) valor = "'"+valor+"'";
				} else if (op.equals(" IS ")){
					//para esta operacion solo se permmite NULL y NOT NULL
					if (valor.equalsIgnoreCase("NULL") || valor.equalsIgnoreCase("NOT NULL")){
						valor = valor.toUpperCase();
					} else if (!valorKey) valor = "'"+valor+"'";
				} else if (op.equals(" IN ")) {
					//aplico la transformacion a valor en funcion de su tipo de datos
					if (!valorKey&&((type==TYPE_TEXT)||(type==TYPE_DATE))) valor = "'"+valor.replace(",", "','")+"'";
				} else {
					//aplico la transformacion a valor en funcion de su tipo de datos
					if (!valorKey&&((type==TYPE_TEXT)||(type==TYPE_DATE))) valor = "'"+valor+"'";
				}
				
				
				if (key.equals("NULL")){
					//iterar por todas las columnas de la vista que esten marcadas para la busqueda fulltext
					//aqui se permite que col==null
					//solo admito campos tipo text, y si es number o date se excluyen por complicar las consultas
					res+=" ( (1=0) ";
					for(GenericMapperInfoColumn cd:cols){
						//elimino ademas los campos que empiezan por $
						//System.out.println("**"+cd.getName()+","+cd.getType()+","+cd.isFullText()+" valor="+valor);
						boolean valid=cd.isFullText();
						String t=cd.getType().toUpperCase();
						valor=(aux.length>2)? aux[2] : "";		//restablezco el valor a su posicion original
						if (valid&&t.equals("T")){
							//no hago cuenta a los comodines, se los pongo yo por defecto por delante y por detras
							//si el valor contiene comodin, en vez de = pongo like
							valor = "'%"+valor+"%'";
							valor = valor.toLowerCase();
							if (driver.equals("mysql") || driver.equals("informix")){
								res+=" OR ( LOWER("+cd.getName()+") LIKE  "+valor+" ) ";
							} else if (driver.equals("access")){
								res+=" OR ( LCASE("+cd.getName()+") LIKE  "+valor+" ) ";
							} else {
								res+=" OR ( LOWER(CONVERT("+cd.getName()+",'US7ASCII')) LIKE CONVERT( "+valor+" ,'US7ASCII') ) ";
							}
						} else if (valid&&t.equals("N")){
							//si valor es un numero incluyo esta columna
							try{
								double a = Double.parseDouble(valor);
								if (driver.equals("mysql") || driver.equals("informix")){
									res+=" OR ( LOWER("+cd.getName()+") = "+a+" ) ";
								} else if (driver.equals("access")){
									res+=" OR ( LCASE("+cd.getName()+") = "+a+" ) ";
								} else {
									res+=" OR ( LOWER(CONVERT("+cd.getName()+",'US7ASCII')) = "+a+" ) ";
								}
							} catch (Exception e){
								//no hago nada
							}
						}
					}
					res+=" ) ";
					//res+="FULLTEXT("+valor+")";
				} else {
					//en funcion del tipo de dato se ponen ' o no
					//hay que escapar datos
					//TODO si col==null lo añado aqui?
					if ((col!=null) && (op.equals(" LIKE ")||op.equals(" NOT LIKE "))) {
						if (driver.equals("mysql") || driver.equals("informix")){
							key = "LOWER("+col.getName()+")";
							valor = valor.toLowerCase();
						} else if (driver.equals("access")){
							key = "LCASE("+col.getName()+")";
							valor = valor.toLowerCase();
						} else {
							key = "LOWER(CONVERT("+col.getName()+",'US7ASCII'))";
							valor = "CONVERT( "+valor.toLowerCase()+" ,'US7ASCII') ";
						}
						res+= key+op+valor;
					} else if (op.equals("=>")){
						//TODO en informix esto no esta soportado
						op = " IN ";
						valor = "(SELECT TRIM(REGEXP_SUBSTR("+valor+", '[^,]+', 1, LEVELS.COLUMN_VALUE)) "
								+ " FROM TABLE(CAST(MULTISET(SELECT LEVEL FROM DUAL "
								+ " CONNECT BY LEVEL <= LENGTH (REGEXP_REPLACE("+valor+", '[^,]+')) + 1) AS SYS.OdciNumberList)) LEVELS)";
						res+= key+op+valor;
					} else if (op.equals(" IN ")){
						if (!valorKey) {
							res+= key+op+"("+valor+")";
						} else {
							res+= "INSTR(','||"+valor+"||','  ,  ','||"+key+"||',')>0";
						}
					} else {
						//resto de casos
						res+= key+op+valor;
					}

				}
			} else {
				if (cad.equals("&")) res+= " AND ";
				else if (cad.equals("|")) res += " OR ";
				else if (cad.equals("(")) res += "(";
				else if (cad.equals(")")) res += ")";
			}
			//System.out.println(cad);
			//System.out.println("************** "+res);
		}
		//System.out.println("SQLUTILS:"+res);
		return res;
	}
}
