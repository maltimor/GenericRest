package es.maltimor.genericRest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import es.maltimor.genericUser.User;

public class GenericMapperInfoTableResolverImpl implements GenericMapperInfoTableResolver,GenericTableResolverTestParams {

	public String getSQL(User user, String table) {
		return "SELECT * FROM "+table;
	}

	public String getTestSQL(String table) {
		return "SELECT * FROM "+table;
	}

	public String getQueryWhere(User user, String table, GenericMapperInfoTable info, String filter, UriInfo ui, Map<String, String> query, String action, String actualFilter) {
		return actualFilter;
	}

	public String getInsert(User user, String table, GenericMapperInfoTable info, Map<String, Object> data) {
		//TODO tratar tipos de datos con tama√±os
		String colNames = "";
		String colValues = "";
		boolean first = true;
		for(GenericMapperInfoColumn column : info.getFields()){
			String key = column.getName().toUpperCase();
			if (data.containsKey(key) || column.getType().equals("S")){
				if (!first) {
					colNames+=",";
					colValues+=",";
				}
				colNames+=key;
				if (column.getType().equals("S")) colValues+=column.getSecuenceName()+".nextVal";		//caso de numero de secuencia  
				else {
					colValues+="#{data."+key;
					if (column.getType().equals("B")) colValues+=",jdbcType=BLOB";
					else if (column.getType().equals("C"))colValues+=",jdbcType=CLOB";
					colValues+="}";
				}
				first=false;
			}
		}
		
		String sql = "INSERT INTO "+table+"("+colNames+") VALUES ("+colValues+")";
		//System.out.println(sql);
		return sql;
	}
	
	public String getUpdate(User user, String table, GenericMapperInfoTable info, String id, Map<String, Object> data) {
		List<String> keys = info.getKeys();
		String colSets = "";
		String colKeys = "";
		boolean firstSet = true;
		boolean firstKey = true;

		for(GenericMapperInfoColumn column : info.getFields()){
			String key = column.getName().toUpperCase();
			if (data.containsKey(key)){
				if (!firstSet) colSets+=",";
				colSets+=key+"=#{data."+key;
				if (column.getType().equals("B")) colSets+=",jdbcType=BLOB";
				else if (column.getType().equals("C"))colSets+=",jdbcType=CLOB";
				colSets+="}";
				firstSet=false;
			}
		}
		for(int i=0;i<keys.size();i++) {
			colKeys+=keys.get(i)+"=#{id"+i+"}";
			if (i<keys.size()-1) colKeys+=" AND ";
		}
		
		String sql = "UPDATE "+table+" SET "+colSets+" WHERE "+colKeys;
		return sql;
	}

	public String getDelete(User user, String table, GenericMapperInfoTable info, String id) {
		List<String> keys = info.getKeys();
		
		
		String sql = "DELETE FROM "+table;
		
		//AÒadido un caso super especial de eliminacion de TODOS los registros
		if (id!=null){
			sql+=" WHERE ";
			for(int i=0;i<keys.size();i++) {
				sql+=keys.get(i)+"=#{id"+i+"}";
				if (i<keys.size()-1) sql+=" AND ";
			}
		}
		return sql;
	}

	public String getExecute(User user, String table, GenericMapperInfoTable info, Map<String, Object> data) {
		//UNA TABLA DE ESTE TIPO NO TIENE EXECUTE
		return null;
	}

	public Map<String, Object> getTestParams(String table) {
		//POR DEFECTO NO DEVUELVE PARAMETROS
		return new HashMap<String,Object>();
	}
	
}
