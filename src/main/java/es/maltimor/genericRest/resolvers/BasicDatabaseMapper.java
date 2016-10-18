package es.maltimor.genericRest.resolvers;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

public interface BasicDatabaseMapper {
	@SelectProvider(type=es.maltimor.genericRest.resolvers.BasicDatabaseMapperProvider.class, method="getSQL")
	public List<Map<String,Object>> getSQL(Map<String,Object> params);
}
