package es.maltimor.genericRest;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.mapping.StatementType;

public interface GenericMapperInfoMapper {
	@SelectProvider(type=es.maltimor.genericRest.GenericMapperInfoMapperProvider.class, method="getSQL")
	public List<Map<String,Object>> getSQL(Map<String,Object> params);

	@SelectProvider(type=es.maltimor.genericRest.GenericMapperInfoMapperProvider.class, method="getSQL")
	@Options(statementType = StatementType.CALLABLE)
	public Object getSQLFunction(Map<String,Object> params);
}
