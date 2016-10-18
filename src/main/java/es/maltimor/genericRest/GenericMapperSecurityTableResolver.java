package es.maltimor.genericRest;

import java.util.Map;

import es.maltimor.genericUser.User;

public interface GenericMapperSecurityTableResolver {
	public boolean canSelect(User user, String table,GenericMapperInfoTable info,String filter);
	public boolean canGetById(User user,String table,GenericMapperInfoTable info,Object id);
	public boolean canInsert(User user, String table,GenericMapperInfoTable info,Map<String,Object> data);
	public boolean canUpdate(User user, String table,GenericMapperInfoTable info,Object id,Map<String,Object> data);
	public boolean canDelete(User user, String table,GenericMapperInfoTable info,Object id);
	public boolean canExecute(User user, String table,GenericMapperInfoTable info,Map<String,Object> data);
}
