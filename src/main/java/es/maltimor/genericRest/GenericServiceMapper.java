package es.maltimor.genericRest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.mapping.StatementType;

public interface GenericServiceMapper {
	@Select("Select * FROM dual")	
	String getTypeVarChar();
	
	@Select("Select * FROM dual")	
	List<Map<String,Object>> getTypeCursor();
	
	@Select("Select * FROM dual")	
	Date getTypeDate();
	
	@Select("Select * FROM dual")	
	List<Map<String,Object>> getTypeNumberic();
	
	@Select("Select * FROM dual")	
	List<Map<String,Object>> getTypeGeneric();
	
	@Select("Select * FROM dual")	
	List<Map<String,Object>> getTypeBLOB();
	
	//@Param("user") User user,@Param("table") String table,@Param("info") GenericCrudMapperInfoTable info,@Param("filter") String filter);
	@SelectProvider(type=es.maltimor.genericRest.GenericServiceMapperProvider.class, method="cntAll")
	public Long cntAll(Map<String,Object> params);

	//@Param("user") User user,@Param("table") String table,@Param("info")  GenericCrudMapperInfoTable info,@Param("filter") String filter,@Param("limit") Long limit,@Param("offset") Long offset,@Param("orderby") String orderby,@Param("order") String order,@Param("fields") String fields);
	@SelectProvider(type=es.maltimor.genericRest.GenericServiceMapperProvider.class, method="getAll")
	public List<Map<String, Object>> getAll(Map<String,Object> params);

	//@Param("user") User user,@Param("table") String table,@Param("info") GenericCrudMapperInfoTable info,@Param("id") Object id);
	@SelectProvider(type=es.maltimor.genericRest.GenericServiceMapperProvider.class, method="getById")
	public Map<String, Object> getById(Map<String,Object> params);

	//@Param("user") User user,@Param("table") String table,@Param("info") GenericCrudMapperInfoTable info,@Param("data") Map<String, Object> data);
	@InsertProvider(type=es.maltimor.genericRest.GenericServiceMapperProvider.class, method="insert")
	public void insert(Map<String,Object> params);

	//@Param("user") User user,@Param("table") String table,@Param("info") GenericCrudMapperInfoTable info,@Param("id") Object id,@Param("data") Map<String, Object> data);
	@UpdateProvider(type=es.maltimor.genericRest.GenericServiceMapperProvider.class, method="update")
	public void update(Map<String,Object> params);

	//@Param("user") User user,@Param("table") String table,@Param("info") GenericCrudMapperInfoTable info,@Param("id") Object id);
	@DeleteProvider(type=es.maltimor.genericRest.GenericServiceMapperProvider.class, method="delete")
	public void delete(Map<String,Object> params);

	@SelectProvider(type=es.maltimor.genericRest.GenericServiceMapperProvider.class, method="execute")
	@Options(statementType = StatementType.CALLABLE)
	public Object execute(Map<String,Object> params);

	//@Param("user") User user,@Param("table") String table,@Param("info") GenericCrudMapperInfoTable info,@Param("data") Map<String, Object> data);
	@SelectProvider(type=es.maltimor.genericRest.GenericServiceMapperProvider.class, method="getSecuenceValue")
	public Map<String, Object> getSecuenceValue(String secuence);

}
