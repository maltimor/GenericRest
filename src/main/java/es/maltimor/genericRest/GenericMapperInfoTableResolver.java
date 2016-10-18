package es.maltimor.genericRest;

import java.util.Map;

import javax.ws.rs.core.UriInfo;

import es.maltimor.genericUser.User;

/*
 * Clase que permite personalizar el mapperProvider asociado servicio rest Genericcrud
 * NOTA: si se quiere hacer referencia a los campos con ibatys hay que escribir "#{data.CAMPO}"
 */
public interface GenericMapperInfoTableResolver {
	public String getTestSQL(String table);
	public String getSQL(User user,String table);
	public String getQueryWhere(User user,String table,GenericMapperInfoTable info,String filter,UriInfo ui,Map<String,String> query,String action,String actualFilter);
	public String getInsert(User user,String table,GenericMapperInfoTable info, Map<String, Object> data);
	public String getUpdate(User user,String table,GenericMapperInfoTable info, String id, Map<String, Object> data);
	public String getDelete(User user,String table,GenericMapperInfoTable info, String id);
	public String getExecute(User user,String table,GenericMapperInfoTable info, Map<String, Object> data);
}

