package es.maltimor.genericRest;

import java.util.Map;

public class GenericDatabaseMapperProvider {
	//resourceTable(String)
	public String getRows(Map<String,Object> params){
		String resourceTable = (String) params.get("resourceTable");
		String res = "SELECT * FROM "+resourceTable;
		//System.out.println("DATABASEMAPPER:"+res);
		return res;
	}
}
