package es.maltimor.genericRest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import es.maltimor.genericUser.User;

/*
 * Esta clase es la que centraliza la seguridad de todo el servicio rest, delegara en GenericMapperInfoTable 
 * la implementacion para poder definir la seguridad a nivel individual de tabla
 */
public class GenericSecurityDaoCache implements GenericSecurityDao {
	private GenericSecurityDao securityDao;
	private Map<String,Boolean> map;

	public GenericSecurityDaoCache(){
		map = new HashMap<String,Boolean>();
	}
	
	private String hash(User user,String table,String filter,Map<String, Object> data,Object id, UriInfo ui){
		String res="|";
		if (user!=null) res+=user.getLogin()+"|";
		else res+="null|";
		res+=table+"|";
		res+=filter+"|";
		res+=data+"|";
		res+=id+"|";
		res+=ui+"|";
		return res;
	}
	
	public boolean canSelect(User user, String table, String filter,UriInfo ui) throws Exception {
		String h=hash(user,table,filter,null,null,ui);
		if (map.containsKey(h)) return map.get(h);
		
		boolean res = securityDao.canSelect(user, table, filter, ui);
		map.put(h, res);
		return res;
	}

	public boolean canGetById(User user, String table, Object id,UriInfo ui) throws Exception {
		String h=hash(user,table,null,null,id,ui);
		if (map.containsKey(h)) return map.get(h);

		boolean res = securityDao.canGetById(user, table, id, ui);
		map.put(h, res);
		return res;
	}

	public boolean canInsert(User user, String table, Map<String, Object> data,UriInfo ui) throws Exception {
		String h=hash(user,table,null,data,null,ui);
		if (map.containsKey(h)) return map.get(h);

		boolean res = securityDao.canInsert(user, table, data, ui);
		map.put(h, res);
		return res;
	}

	public boolean canUpdate(User user, String table, Object id, Map<String, Object> data,UriInfo ui) throws Exception {
		String h=hash(user,table,null,data,id,ui);
		if (map.containsKey(h)) return map.get(h);

		boolean res = securityDao.canUpdate(user, table, id, data, ui);
		map.put(h, res);
		return res;
	}

	public boolean canDelete(User user, String table, Object id,UriInfo ui) throws Exception {
		String h=hash(user,table,null,null,id,ui);
		if (map.containsKey(h)) return map.get(h);

		boolean res = securityDao.canDelete(user, table, id, ui);
		map.put(h, res);
		return res;
	}

	public boolean canExecute(User user, String table, Map<String, Object> data,UriInfo ui) throws Exception {
		String h=hash(user,table,null,data,null,ui);
		if (map.containsKey(h)) return map.get(h);

		boolean res = securityDao.canExecute(user, table, data, ui);
		map.put(h, res);
		return res;
	}

	public GenericSecurityDao getSecurityDao() {
		return securityDao;
	}
	public void setSecurityDao(GenericSecurityDao securityDao) {
		this.securityDao = securityDao;
	}
}
