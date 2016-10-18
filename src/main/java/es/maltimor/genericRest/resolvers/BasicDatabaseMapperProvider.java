package es.maltimor.genericRest.resolvers;

import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

public class BasicDatabaseMapperProvider {
	//resourceTable(String), table(String)
	public String getSQL(Map<String,Object> params){
		String resourceTable = (String) params.get("resourceTable");
		String table = (String) params.get("table");
		String res = "SELECT * FROM "+resourceTable+" WHERE TABLE_NAME='"+table+"'";
		System.out.println("BASIC_DATABASEMAPPER:"+res);
		return res;
	}
}
