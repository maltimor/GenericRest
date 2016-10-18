package es.maltimor.genericRest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import es.maltimor.genericUser.User;

public interface GenericServiceDao {
	public long cntAll(User user, String table, String filter,UriInfo ui) throws Exception;
	public List<Map<String,Object>> getAll(User user, String table, String filter, long limit, long offset, String orderby,String order,String fields,UriInfo ui) throws Exception;
	public Map<String,Object> getById(User user, String table,Object id,UriInfo ui) throws Exception;
	public boolean insert(User user, String table,Map<String,Object> data,UriInfo ui) throws Exception;
	public boolean update(User user, String table,Object id,Map<String,Object> data,UriInfo ui) throws Exception;
	public boolean delete(User user, String table,Object id,UriInfo ui) throws Exception;
	public Object execute(User user, String table,Map<String,Object> data,UriInfo ui) throws Exception;
	public GenericMapperInfoTable getMapperInfoTable(User user, String table) throws Exception;
	public List<String> getMapperInfoList(User user);
	public boolean isTable(String table) throws Exception;
}
