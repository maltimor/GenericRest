package es.maltimor.genericRest;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

public interface GenericDatabaseMapper {
	@SelectProvider(type=es.maltimor.genericRest.GenericDatabaseMapperProvider.class, method="getRows")
	public List<Map<String,Object>> getRows(Map<String,Object> params);
}
