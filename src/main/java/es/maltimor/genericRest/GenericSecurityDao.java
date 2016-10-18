package es.maltimor.genericRest;

import java.util.Map;

import javax.ws.rs.core.UriInfo;

import es.maltimor.genericUser.User;

public interface GenericSecurityDao {
	public boolean canSelect(User user, String table,String filter,UriInfo ui) throws Exception;
	public boolean canGetById(User user,String table,Object id,UriInfo ui) throws Exception;
	public boolean canInsert(User user, String table,Map<String,Object> data,UriInfo ui) throws Exception;
	public boolean canUpdate(User user, String table,Object id,Map<String,Object> data,UriInfo ui) throws Exception;
	public boolean canDelete(User user, String table,Object id,UriInfo ui) throws Exception;
	public boolean canExecute(User user, String table,Map<String,Object> data,UriInfo ui) throws Exception;
//	public boolean canGetMapperInfoTable(User user, String table) throws Exception;
//	public boolean canGetMapperInfoList(User user) throws Exception;
}
