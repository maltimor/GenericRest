package es.maltimor.genericRest;

import java.util.Map;

public class GenericMapperInfoMapperProvider {
	//sql(String)
	public String getSQL(Map<String,Object> params){
		String sql = (String) params.get("sql");
		//System.out.println("GenericMapperInfoMapper:"+sql);
		return sql;
	}
}
