package es.maltimor.genericRest.resolvers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import es.maltimor.genericRest.GenericMapperInfoColumn;
import es.maltimor.genericRest.GenericMapperInfoTable;
import es.maltimor.genericRest.GenericMapperInfoTableResolver;
import es.maltimor.genericRest.GenericMapperInfoTableResolverImpl;
import es.maltimor.genericUser.User;

public class BasicDatabaseResolver extends GenericMapperInfoTableResolverImpl {
	private BasicDatabaseMapper mapper;
	private String resourceTable;

	public String getTestSQL(String table) {
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("resourceTable", resourceTable);
		map.put("table", table);
		List<Map<String,Object>> ldoc=mapper.getSQL(map);
		if (ldoc.size()>0&&ldoc.get(0).containsKey("SELECT_VALUE")){
			String sql = (String) ldoc.get(0).get("SELECT_VALUE");
			sql = "SELECT T.* FROM ("+sql+") T WHERE 1=0";
			return sql;
		} else return super.getTestSQL(table);
	}

	public String getSQL(User user, String table) {
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("resourceTable", resourceTable);
		map.put("table", table);
		List<Map<String,Object>> ldoc=mapper.getSQL(map);
		if (ldoc.size()>0&&ldoc.get(0).containsKey("SELECT_VALUE")){
			String sql = (String) ldoc.get(0).get("SELECT_VALUE");
			String where = (String) ldoc.get(0).get("SELECT_FILTER");
			sql = (where!=null)? sql + " " + where : sql;
			return sql;
		} else return super.getSQL(user, table);
	}

	public String getQueryWhere(User user, String table, GenericMapperInfoTable info, String filter, UriInfo ui, Map<String, String> query, String action, String actualFilter) {
		return actualFilter;
	}

	public String getInsert(User user, String table, GenericMapperInfoTable info, Map<String, Object> data) {
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("resourceTable", resourceTable);
		map.put("table", table);
		List<Map<String,Object>> ldoc=mapper.getSQL(map);
		if (ldoc.size()>0&&ldoc.get(0).containsKey("INSERT_VALUE")){
			String sql = (String) ldoc.get(0).get("INSERT_VALUE");
			return sql;
		} else return super.getInsert(user, table, info, data);
	}

	public String getUpdate(User user, String table, GenericMapperInfoTable info, String id, Map<String, Object> data) {
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("resourceTable", resourceTable);
		map.put("table", table);
		List<Map<String,Object>> ldoc=mapper.getSQL(map);
		if (ldoc.size()>0&&ldoc.get(0).containsKey("UPDATE_VALUE")){
			String sql = (String) ldoc.get(0).get("UPDATE_VALUE");
			return sql;
		} else return super.getUpdate(user, table, info, id, data);
	}

	public String getDelete(User user, String table, GenericMapperInfoTable info, String id) {
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("resourceTable", resourceTable);
		map.put("table", table);
		List<Map<String,Object>> ldoc=mapper.getSQL(map);
		if (ldoc.size()>0&&ldoc.get(0).containsKey("DELETE_VALUE")){
			String sql = (String) ldoc.get(0).get("DELETE_VALUE");
			return sql;
		} else return super.getDelete(user, table, info, id);
	}

	public String getExecute(User user, String table, GenericMapperInfoTable info, Map<String, Object> data) {
		//SI LA TABLA ES DEL TIPO FUNCION/PROCEDURE (o lo que es lo mismo <>null)
		System.out.println("GET_EXECUTE_RESOLVER:"+table);
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("resourceTable", resourceTable);
		map.put("table", table);
		List<Map<String,Object>> ldoc=mapper.getSQL(map);
		if (ldoc.size()>0){
			Map<String,Object> doc=ldoc.get(0);
			if (doc.get("TYPE")!=null){
				System.out.println("EJECUTAR:"+doc.get("SELECT_VALUE"));
				return (String) doc.get("SELECT_VALUE");
			}
		}
		System.out.println("NO HAY DATOS");
		return super.getExecute(user, table, info, data);
	}

	public BasicDatabaseMapper getMapper() {
		return mapper;
	}
	public void setMapper(BasicDatabaseMapper mapper) {
		this.mapper = mapper;
	}
	public String getResourceTable() {
		return resourceTable;
	}
	public void setResourceTable(String resourceTable) {
		this.resourceTable = resourceTable;
	}
}
