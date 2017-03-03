package es.maltimor.genericRest;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import es.maltimor.genericRest.GenericMapperInfoTable;
import es.maltimor.genericRest.GenericServiceDao;
import es.maltimor.genericUser.User;
import es.maltimor.genericUser.UserDao;

@Path("/genericRestService/")
@Consumes("application/json")
@Produces("application/json")
public class GenericService {
	private GenericServiceDao service;
	private GenericSecurityDao securityDao;
	private UserDao userDao;
	private String app;

	public GenericServiceDao getService() {
		return service;
	}
	public void setService(GenericServiceDao service) {
		this.service = service;
	}
	public GenericSecurityDao getSecurityDao() {
		return securityDao;
	}
	public void setSecurityDao(GenericSecurityDao securityDao) {
		this.securityDao = securityDao;
	}
	public UserDao getUserDao() {
		return userDao;
	}
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	public String getApp() {
		return app;
	}
	public void setApp(String app) {
		this.app = app;
	}

	/*
	 * Inicializa los datos de un usuario
	 */
	@GET
	@Path("/_inituser")
	public Response initUser(){
		System.out.println("REST: INIT USER:");
		
		try {
			User res = userDao.initUser(userDao.getLogin(), app);
			return Response.ok(res).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("/_getuser")
	public Response getUser(){
		System.out.println("REST: GET USER:");

		try {
			User res = userDao.getUser(userDao.getLogin(), app);
			return Response.ok(res).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("/{table}/count")
	// TODO Pensar en los valores por defecto para hacer mas facill invocar a este servicio REST
	public Response cntAll(@PathParam("table") String table, @QueryParam("filter") String filter,@Context UriInfo ui){
		System.out.println("REST: GET COUNT "+table);

		try {
			//comprobar que exista
			if (!service.isTable(table)){
				System.out.println("---- no EXISTE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}
			//comprobar la seguridad
			User user = userDao.getUser(userDao.getLogin(), app);
			GenericMapperInfoTable t = service.getMapperInfoTable(user, table);
			if (t.getType()!=null){
				System.out.println("---- no PROCEDE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}

			if (!securityDao.canSelect(user, table, filter, ui)){
				System.out.println("---- no tiene permisos");
				return Response.status(HttpServletResponse.SC_UNAUTHORIZED).entity("No tiene permisos").build();
			}
			
			long size = service.cntAll(user,table,filter,ui);
			System.out.println("REST: GET COUNT "+table+"="+size);
			return Response.status(200).entity("{\"count\":"+size+"}").build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	@GET
	@Path("/{table}")
	// TODO Pensar en los valores por defecto para hacer mas facill invocar a este servicio REST
	public Response getAll(
			@PathParam("table") String table,
			@QueryParam("filter") String filter,
			@QueryParam("limit") @DefaultValue("30") long limit,	//TODO ESTABLECER EL LIMITE POR DEFECTO
			@QueryParam("offset") @DefaultValue("0") long offset,
			@QueryParam("orderby") String orderby,
			@QueryParam("order") String order,
			@QueryParam("fields") @DefaultValue("*") String fields,
			@QueryParam("format") @DefaultValue("JSON") String format,
			@Context UriInfo ui){
		System.out.println("REST: GET "+table+":filter="+filter+" limit="+limit+" offset="+offset+" orderBy="+orderby+" order="+order+" fields="+fields+" format="+format);

		try {
			//comprobar que exista
			if (!service.isTable(table)){
				System.out.println("---- no EXISTE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}
			//comprobar la seguridad y que proceda
			User user = userDao.getUser(userDao.getLogin(), app);
			GenericMapperInfoTable t = service.getMapperInfoTable(user, table);
			if (t.getType()!=null){
				System.out.println("---- no PROCEDE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}

			if (!securityDao.canSelect(user, table, filter, ui)){
				System.out.println("---- no tiene permisos");
				return Response.status(HttpServletResponse.SC_UNAUTHORIZED).entity("No tiene permisos").build();
			}
			List<Map<String,Object>> data = service.getAll(user,table,filter,limit,offset,orderby,order,fields,ui);
			if (format.equals("JSON")) return Response.ok(data).build();
			else {
				//preparo la exportacion
				Map<String,Object> param=new HashMap<String,Object>();
				param.put("excel",false);
				param.put("table",true);
				param.put("blankWhenNull", true);
				param.put("separator", ",");
				if (format.equals("XLS")) param.put("excel",true);
				else if (format.equals("CSV")) param.put("table",false);
				else if (format.equals("TXT")) param.put("table",false);
				Object out = toText(data,getFieldList(t,fields),param);
				//salida y content type
				if (format.equals("XLS")) return Response.ok(out,"application/vnd.ms-excel; charset=utf-8; name=exec.xls").header("Content-Disposition","inline;filename=exec.xls").build();
				else if (format.equals("CSV")) return Response.ok(out,"text/csv; charset=utf-8; name=exec.xls").header("Content-Disposition","inline;filename=exec.csv").build();
				else if (format.equals("HTML")) return Response.ok(out,"text/html; charset=utf-8").build();
				else if (format.equals("TXT")) return Response.ok(out,"text/plain; charset=utf-8").build();
				else return Response.serverError().entity("Formato no soportado").build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("/{table}/{id:.*}")
	public Response getById(@PathParam("table") String table, @PathParam("id") String id,@Context UriInfo ui){
		System.out.println("REST:GET "+table+":"+id);

		try {
			//comprobar que exista
			if (!service.isTable(table)){
				System.out.println("---- no EXISTE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}
			//comprobar la seguridad y que proceda
			User user = userDao.getUser(userDao.getLogin(), app);
			GenericMapperInfoTable t = service.getMapperInfoTable(user, table);
			if (t.getType()!=null){
				System.out.println("---- no PROCEDE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}

			if (!securityDao.canGetById(user, table, id, ui)){
				System.out.println("---- no tiene permisos");
				return Response.status(HttpServletResponse.SC_UNAUTHORIZED).entity("No tiene permisos").build();
			}

			Map<String,Object> data = service.getById(user,table,id,ui);
			return Response.ok(data).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	@POST
	@Path("/{table}")
	public Response insert(@PathParam("table") String table, @Context UriInfo ui, Map<String,Object> data){
		System.out.println("REST: POST "+table+":"+data.size());

		try {
			//comprobar que exista
			if (!service.isTable(table)){
				System.out.println("---- no EXISTE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}
			//comprobar la seguridad
			User user = userDao.getUser(userDao.getLogin(), app);
			GenericMapperInfoTable t = service.getMapperInfoTable(user, table);
			String type=t.getType();
			if (((type==null) && (!securityDao.canInsert(user, table, data, ui)))
				||((type!=null) && (!securityDao.canExecute(user, table, data, ui)))){
				System.out.println("---- no tiene permisos");
				return Response.status(HttpServletResponse.SC_UNAUTHORIZED).entity("No tiene permisos").build();
			}

			//Aqui determino si hay que hacer un insert o un update
			if (type==null){	//caso normal de tabla, insert
				if (service.insert(user,table,data,ui)){
					return Response.ok(data).build();
				} else {
					return Response.serverError().entity("Error: No se ha podido insertar.").build();
				}
			} else {	//caso de funcion o procedure: execute
				Object res = service.execute(user, table, data, ui);
				return Response.ok(res).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	@DELETE
	@Path("/{table}/{id:.*}")
	@Consumes("text/plain")
	public Response delete(@PathParam("table") String table, @PathParam("id") String id, @Context UriInfo ui){
		System.out.println("REST: DELETE "+table+":"+id);

		try {
			//comprobar que exista
			if (!service.isTable(table)){
				System.out.println("---- no EXISTE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}
			//comprobar la seguridad y que proceda
			User user = userDao.getUser(userDao.getLogin(), app);
			GenericMapperInfoTable t = service.getMapperInfoTable(user, table);
			if (t.getType()!=null){
				System.out.println("---- no PROCEDE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}

			if (!securityDao.canDelete(user, table, id, ui)){
				System.out.println("---- no tiene permisos");
				return Response.status(HttpServletResponse.SC_UNAUTHORIZED).entity("No tiene permisos").build();
			}

			if (service.delete(user,table,id,ui)){
				return Response.ok().build();
			} else {
				return Response.serverError().entity("Error: No se ha podido eliminar.").build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	@PUT
	@Path("/{table}/{id:.*}")
	public Response update(@PathParam("table")String table,@PathParam("id") String id, @Context UriInfo ui, Map<String,Object> data){
		System.out.println("REST: PUT "+table+":"+id+":"+data.size());

		try {
			//comprobar que exista
			if (!service.isTable(table)){
				System.out.println("---- no EXISTE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}
			//comprobar la seguridad y que proceda
			User user = userDao.getUser(userDao.getLogin(), app);
			GenericMapperInfoTable t = service.getMapperInfoTable(user, table);
			if (t.getType()!=null){
				System.out.println("---- no PROCEDE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}

			if (!securityDao.canUpdate(user, table, id, data, ui)){
				System.out.println("---- no tiene permisos");
				return Response.status(HttpServletResponse.SC_UNAUTHORIZED).entity("No tiene permisos").build();
			}

			if (service.update(user,table,id,data,ui)){
				return Response.ok(data).build();
			} else {
				return Response.serverError().entity("Error: No se ha podido actualizar.").build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	
	@GET
	@Path("/mapperinfotable/{table}")
	public Response getMapperInfoTable(@PathParam("table") String table){
		System.out.println("REST: GET mapperinfotable "+table);

		try {
			//comprobar que exista
			if (!service.isTable(table)){
				System.out.println("---- no EXISTE");
				return Response.status(HttpServletResponse.SC_NOT_FOUND).entity("No existe el proceso").build();
			}
			//comprobar la seguridad
			User user = userDao.getUser(userDao.getLogin(), app);
	/*		if (!user.hasRole("C_GRUPOS")) {
				System.out.println("---- no tiene permisos");
				return Response.status(HttpServletResponse.SC_UNAUTHORIZED).build();
			}
			System.out.println("---- si tiene permisos");
	*/		

			GenericMapperInfoTable tabla = service.getMapperInfoTable(user,table);
			
			if (tabla != null) {
				
				return Response.ok(tabla.toString()).build();
			} else {
				return Response.serverError().entity("Error: No se ha encontrado la tabla: " + table).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	
	@GET
	@Path("/mapperinfolist/")
	public Response getMapperInfoList(){
		System.out.println("REST: GET mapperinfoList ");

		try {
			//comprobar la seguridad
			User user = userDao.getUser(userDao.getLogin(), app);
	/*		if (!user.hasRole("C_GRUPOS")) {
				System.out.println("---- no tiene permisos");
				return Response.status(HttpServletResponse.SC_UNAUTHORIZED).build();
			}
			System.out.println("---- si tiene permisos");
	*/		
			
			List<String> data = service.getMapperInfoList(user);
			Collections.sort(data);
			if (data != null) {
				return Response.ok(data).build();
			} else {
				return Response.serverError().entity("Error: No se han definido tablas. ").build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
	
	private List<String> getFieldList(GenericMapperInfoTable t,String lfields){
		List<String> fields=new ArrayList<String>();
		
		if (lfields.equals("*")){
			//for (GenericMapperInfoColumn column : t.getFields()) if (!column.getType().endsWith("LOB")) fields.add(column.getName());
			for (GenericMapperInfoColumn column : t.getFields()) fields.add(column.getName().toUpperCase());
		} else {
			String[] arr=lfields.split(",");
			for(int i=0;i<arr.length;i++){
				for (GenericMapperInfoColumn column : t.getFields()) if (column.getName().toUpperCase().equals(arr[i].toUpperCase())){
					fields.add(column.getName().toUpperCase());
				}
			}
		}
		return fields;
	}

	//fields,decorator
	private Object toText(List<Map<String,Object>> data,List<String> fields,Map<String,Object> params){
		boolean excel = params.containsKey("excel")?(Boolean) params.get("excel"):false;
		boolean table = params.containsKey("table")?(Boolean) params.get("table"):true;
		String separator = params.containsKey("separator")?(String) params.get("separator"):"";
		boolean blankWhenNull = params.containsKey("blankWhenNull")?(Boolean) params.get("blankWhenNull"):true;
		
		StringBuilder res=new StringBuilder();
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("pagina 1");
        int rownum=0;
		
		//encabezado
		if (excel){
            HSSFRow row = sheet.createRow(rownum);
			for(int i=0;i<fields.size();i++) row.createCell(i).setCellValue(fields.get(i));
			rownum++;
		} else if (table){
			res.append("<table><thead><tr>");
			for(int i=0;i<fields.size();i++) res.append("<th>").append(fields.get(i)).append("</th>");
			res.append("</tr></thead><tbody>");
		} else {
			for(int i=0;i<fields.size();i++) {
				res.append(fields.get(i));
				if (i<fields.size()-1) res.append(separator);
			}
			res.append("\n");
		}
		//cuerpo
		for(Map<String,Object> map:data){
			if (excel){
	            HSSFRow row = sheet.createRow(rownum);
				for(int i=0;i<fields.size();i++) {
					Object value=map.get(fields.get(i));
					if (value==null && blankWhenNull) value="";
					row.createCell(i).setCellValue(value.toString());
				}
				rownum++;
			} else if (table){
				res.append("<tr>");
				for(int i=0;i<fields.size();i++) {
					Object value=map.get(fields.get(i));
					if (value==null && blankWhenNull) value="";
					res.append("<td>").append(value).append("</td>");
				}
				res.append("</tr>");
			} else {
				for(int i=0;i<fields.size();i++) {
					Object value=map.get(fields.get(i));
					if (value==null && blankWhenNull) value="";
					res.append(value);
					if (i<fields.size()-1) res.append(separator);
				}
				res.append("\n");
			}
		}
		//fin
		if (table) res.append("</tbody></table>");
		
		
		//devolver resultados
		if (excel){
            try{
            	ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            	workbook.write(bOut);
            	bOut.close();
                return bOut.toByteArray();
            } catch (Exception e){
            	return "Ocurrio un error: "+e.getMessage();
            }
		} else return res.toString();
		
		

	}
}
