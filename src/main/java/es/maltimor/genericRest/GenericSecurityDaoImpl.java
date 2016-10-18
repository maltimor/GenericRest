package es.maltimor.genericRest;

import java.util.Map;

import javax.ws.rs.core.UriInfo;

import es.maltimor.genericUser.User;

/*
 * Esta clase es la que centraliza la seguridad de todo el servicio rest, delegara en GenericMapperInfoTable 
 * la implementacion para poder definir la seguridad a nivel individual de tabla
 */
public class GenericSecurityDaoImpl implements GenericSecurityDao {
	private GenericMapperInfo info;

	public boolean canSelect(User user, String table, String filter,UriInfo ui) throws Exception {
		GenericMapperInfoTable infoT = info.getInfoTable(table);
		return infoT.getSecResolver().canSelect(user, table, infoT, filter);
	}

	public boolean canGetById(User user, String table, Object id,UriInfo ui) throws Exception {
		GenericMapperInfoTable infoT = info.getInfoTable(table);
		return infoT.getSecResolver().canGetById(user, table, infoT, id);
	}

	public boolean canInsert(User user, String table, Map<String, Object> data,UriInfo ui) throws Exception {
		GenericMapperInfoTable infoT = info.getInfoTable(table);
		return infoT.getSecResolver().canInsert(user, table, infoT, data);
	}

	public boolean canUpdate(User user, String table, Object id, Map<String, Object> data,UriInfo ui) throws Exception {
		GenericMapperInfoTable infoT = info.getInfoTable(table);
		return infoT.getSecResolver().canUpdate(user, table, infoT, id, data);
	}

	public boolean canDelete(User user, String table, Object id,UriInfo ui) throws Exception {
		GenericMapperInfoTable infoT = info.getInfoTable(table);
		return infoT.getSecResolver().canDelete(user, table, infoT, id);
	}

	public boolean canExecute(User user, String table, Map<String, Object> data,UriInfo ui) throws Exception {
		GenericMapperInfoTable infoT = info.getInfoTable(table);
		return infoT.getSecResolver().canExecute(user, table, infoT, data);
	}

	public GenericMapperInfo getInfo() {
		return info;
	}
	public void setInfo(GenericMapperInfo info) {
		this.info = info;
	}
}
