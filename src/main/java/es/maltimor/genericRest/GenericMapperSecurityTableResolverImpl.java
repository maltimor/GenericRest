package es.maltimor.genericRest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.maltimor.genericUser.User;

public class GenericMapperSecurityTableResolverImpl implements GenericMapperSecurityTableResolver {
	private boolean defaultResponse=false;

	public boolean canSelect(User user, String table,GenericMapperInfoTable info, String filter) {
		System.out.println("GenericCrudMapperSecurityTableResolverImpl.canSelect: "+user.toString());
		if (info.getActionRoles().containsKey("S")){
			//obtengo un map con los atributos
			Map<String,String> dicc = getDiccionario(user,table,null,null);
			
			//recorro la lista de roles que pueden hacer el select
			List<String> roles = info.getActionRoles().get("S"); 
			System.out.println("Hay "+roles.size()+" roles definidos.");
			for(String rol:roles){
				System.out.println(rol+"?");
				if (canDo(rol,dicc,user)) return true;
				//if (user.hasRol(rol)) return true;
			}
			System.out.println("FALSE1");
			return false;
		}
		System.out.println("???->"+defaultResponse);
		return defaultResponse;
	}


	public boolean canGetById(User user, String table, GenericMapperInfoTable info, Object id) {
		if (info.getActionRoles().containsKey("S")){
			//obtengo un map con los atributos
			Map<String,String> dicc = getDiccionario(user,table,id,null);

			//recorro la lista de roles que pueden hacer el select
			List<String> roles = info.getActionRoles().get("S"); 
			for(String rol:roles){
				if (canDo(rol,dicc,user)) return true;
				//if (user.hasRol(rol)) return true;
			}
			return false;
		}
		return defaultResponse;
	}

	public boolean canInsert(User user, String table, GenericMapperInfoTable info, Map<String, Object> data) {
		if (info.getActionRoles().containsKey("I")){
			//obtengo un map con los atributos
			Map<String,String> dicc = getDiccionario(user,table,null,data);

			//recorro la lista de roles que pueden hacer el select
			List<String> roles = info.getActionRoles().get("I"); 
			for(String rol:roles){
				if (canDo(rol,dicc,user)) return true;
				//if (user.hasRol(rol)) return true;
			}
			return false;
		}
		return defaultResponse;
	}

	public boolean canUpdate(User user, String table, GenericMapperInfoTable info, Object id, Map<String, Object> data) {
		if (info.getActionRoles().containsKey("U")){
			//obtengo un map con los atributos
			Map<String,String> dicc = getDiccionario(user,table,id,data);

			//recorro la lista de roles que pueden hacer el select
			List<String> roles = info.getActionRoles().get("U"); 
			for(String rol:roles){
				if (canDo(rol,dicc,user)) return true;
				//if (user.hasRol(rol)) return true;
			}
			return false;
		}
		return defaultResponse;
	}

	public boolean canDelete(User user, String table, GenericMapperInfoTable info, Object id) {
		if (info.getActionRoles().containsKey("D")){
			//obtengo un map con los atributos
			Map<String,String> dicc = getDiccionario(user,table,id,null);

			//recorro la lista de roles que pueden hacer el select
			List<String> roles = info.getActionRoles().get("D"); 
			for(String rol:roles){
				if (canDo(rol,dicc,user)) return true;
				//if (user.hasRol(rol)) return true;
			}
			return false;
		}
		return defaultResponse;
	}

	public boolean canExecute(User user, String table, GenericMapperInfoTable info, Map<String, Object> data) {
		if (info.getActionRoles().containsKey("E")){
			//obtengo un map con los atributos
			Map<String,String> dicc = getDiccionario(user,table,null,data);

			//recorro la lista de roles que pueden hacer el select
			List<String> roles = info.getActionRoles().get("E"); 
			for(String rol:roles){
				if (canDo(rol,dicc,user)) return true;
				//if (user.hasRol(rol)) return true;
			}
			return false;
		}
		return defaultResponse;
	}
	
	public void setDefaultResponse(Boolean defaultResponse) {
		this.defaultResponse = defaultResponse;
	}
	
	private boolean canDo(String text,Map<String,String> dicc,User user){
		//TODO Meter en una cache el resultado del parseo del texto, optimiza
		List<String> lst = GenericFilter.parseFilter(text);
		if (lst==null) return false;
		if (lst.get(0).equals(GenericFilter.ERROR)) return false;
		
		System.out.println(dicc);
		
		boolean res=false;
		boolean first=true;
		int opBool=0;			//0=null, 1=AND, 2=OR
		for(String cad:lst){
			System.out.println("*"+cad+"     RES="+res);
			if (cad.startsWith("[")){
				String[] aux = cad.substring(1).split("\\|");
				String key = aux[0];
				String op = (aux.length>1)? aux[1] : "";
				String valor = (aux.length>2)? aux[2] : "";
				boolean valorKey = valor.startsWith("[");
				if (valorKey) valor = dicc.get(valor.substring(1).toLowerCase());	//obtengo el valor del diccionario
				
				boolean act=false;
				if (key.equals("NULL")){
					//caso de ROL o de [var]
					act=user.hasRol(valor);
				} else {
					key=dicc.get(key.toLowerCase());
					if (key==null) act=false;
					else if (op.equals("=")){
						act=(key.equals(valor));
					} else if (op.equals("=>")){	//key in lista (separada por comas)
						act=(valor.contains(","+key+","));
					} else {
						System.out.println("*** ERROR *** EXPRESION MAL CONSTRUIDA: "+cad);
						return false;
					}
				}
				
				System.out.println("ACT="+act+"   key="+key+" OP="+op+" valor="+valor);
				
				if (first){ 
					res=act;
					first=false;
				} else if (opBool==0){
					//esto es un error creo
					System.out.println("*** ERROR *** EXPRESION MAL CONSTRUIDA: "+cad);
					return false;
				} else {
					//res!=null,op!=null,act
					if (opBool==1) res=res && act;
					else res=res || act;
				}
					
					
				opBool=0;
				
				System.out.println("RES="+res);

			} else {
				if (cad.equals("&")) opBool=1;
				else if (cad.equals("|")) opBool=2;
				else {
					//esto es un error
					System.out.println("*** ERROR *** EXPRESION MAL CONSTRUIDA: "+cad);
					return false;
				}
			}
				
		}

		System.out.println("RESFINAL="+res);
		
		return res;
	}
	
	private Map<String, String> getDiccionario(User user, String table, Object id, Map<String, Object> data) {
		Map<String,String> map = new HashMap<String,String>();
		if (user!=null){
			map.put("user.login", user.getLogin());
			map.put("user.roles",getListValue(user.getRoles()));
			for(String key:user.getGrupos().keySet()){
				String val = user.getGrupo(key);
				map.put("user.grupos."+key.toLowerCase(),val);
			}
			putMapValue(map,"user.attr.",user.getAttr());
		}
		if (table!=null) map.put("table",table);
		if (id!=null) map.put("id",id.toString());
		if (data!=null) putMapValue(map,"data.",data);
		return map;
	}
	
	private String getListValue(List<String> lst){
		if (lst==null) return "";
		String res = ",";
		for(String val:lst) res+=val+",";
		return res;
	}
	private String getListValueObject(List<Object> lst){
		if (lst==null) return "";
		String res = ",";
		for(Object val:lst) res+=val+",";
		return res;
	}
	
	private void putMapValue(Map<String,String> map, String prefix, Map<String,Object> data){
		if (data==null) return;
		for(String key:data.keySet()){
			Object val = data.get(key);
			if (val!=null){
				if (val instanceof Map){
					putMapValue(map,prefix+key.toLowerCase()+".",(Map<String,Object>) val);
				} else if (val instanceof List){
					map.put(prefix+key.toLowerCase(),getListValueObject((List<Object>) val));
				} else map.put(prefix+key.toLowerCase(),val.toString());
			}
		}
	}

}
