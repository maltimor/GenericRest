package es.maltimor.genericRest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

public class GenericMapperInfoTable {
	private String type;		//tipo: tabla, procedure (^), function (=)
	private String table;
	private String virtualTable;
	private String driver;
	private boolean hasFields;
	private boolean hasKeys;
	private boolean hasSpecialColumn;
	private List<GenericMapperInfoColumn> fields;
	private Map<String,String> types;		//variable calculada con los tipos de cada columna <col.toLower,type>
	private List<String> keys;
	private String keySeparator;
	private String selectFilter;
	private Map<String,List<String>> actionRoles;
	private ApplicationContext context;
	private GenericMapperInfoTableResolver resolver;
	private GenericMapperSecurityTableResolver secResolver;

	// por ahora solo se coge el keys[0]
	public GenericMapperInfoTable() {
		this.fields = new ArrayList<GenericMapperInfoColumn>();
		this.types = new HashMap<String,String>();
		this.keys = new ArrayList<String>();
		this.hasFields = false;
		this.hasKeys = false;
		this.keySeparator = "/";								//separador por defecto de la plataforma
		this.actionRoles = new HashMap<String,List<String>>();
		this.resolver = new GenericMapperInfoTableResolverImpl();			//resolver por defecto
		this.secResolver = new GenericMapperSecurityTableResolverImpl();			//resolver por defecto
	}

	public void setInfo(String info) throws Exception {
		//System.out.println("## GenericCrudMapperInfoTable:" + info);
		String[] info_tokens = info.split("\\|");
		int info_size=info_tokens.length;
		String ID="";
		String FIELDS="";
		String KEYS="";
		String KEY_SEP="";
		String SELECT_FILTER="";
		if (info_size>=1) ID = info_tokens[0];
		if (info_size>=2) FIELDS = info_tokens[1];
		if (info_size>=3) KEYS = info_tokens[2];
		if (info_size>=4) KEY_SEP = info_tokens[3];
		if (info_size>=5) SELECT_FILTER = info_tokens[4];
		if (info_size>5) throw new Exception("Error en la sintaxis GenericCrudMapperInfo.setInfo( " + info + " )");

		//establezco los parametros de la tabla: nombre y separador de keys y resolvedorPordefecto
		//obtengo el tipo de servicio: tabla, procedure, funcion
		String head=ID;
		if (head.startsWith("=")) {
			type="FUNCTION";
			head=head.substring(1);
		} else if (head.startsWith("^")) {
			type="PROCEDURE";
			head=head.substring(1);
		}
		
		if ((head.contains(":")&&(!head.contains("@")))){
			System.out.println("CASO1");
			String[] vTable=head.split(":");
			table=vTable[1];
			virtualTable=vTable[0];
		} else if (head.contains("@")){
			String[] vTable=head.split("@");
			if(vTable[0].contains(":")){
				System.out.println("CASO2");
				String[] vTable2=vTable[0].split(":");
				table=vTable2[1];
				virtualTable=vTable2[0];
			} else {
				System.out.println("CASO3");
				table=vTable[0];
				virtualTable=vTable[0];
			}
			if (context==null) throw new Exception("Context No inicializada");
			Object bean=context.getBean(vTable[1]);
			if (bean instanceof GenericMapperInfoTableResolver){
				resolver = (GenericMapperInfoTableResolver) bean;
			} else throw new Exception("Error. Clase no implementa interfaz GenericCrudMapperInfoTableResolver. setInfo( " + info + " )");
			
		} else {
			System.out.println("CASO4");
			table=head;
			virtualTable=head;
		}
		
		//Convierto a mayusculas: nombre de la tabla, nombre de las keys, nombre de las columnas
		//TODO PONER TABLE A MAYUSCULAS?
		//this.table = this.table.toUpperCase();
		
		if (!KEY_SEP.equals("")) this.setKeySeparator(KEY_SEP);
		if (!SELECT_FILTER.equals("")) this.setSelectFilter(SELECT_FILTER);

		//TODO PONER KEYS A MAYUSCULAS?
		//CASO ESPECIAL DE NO EXISTENCIA DE KEYS
		if (KEYS.equals("")) this.hasKeys=false;
		else {
			String[] keys = KEYS.split(",");
			for (int i = 0; i < keys.length; i++)
				this.keys.add(keys[i]);
		}

		//CASO ESPECIAL DE *, "" o null
		if (FIELDS.equals("*")||FIELDS.equals("")) {
			this.hasFields=false;
			this.hasSpecialColumn=true;
		}
		else {
			this.hasFields=true;
			String[] fields = FIELDS.split(",");
			for (int i = 0; i < fields.length; i++) {
				// *
				// campo[:descripcion][#[T|F|N|S][#SIZE|#secuence]]
				if (fields[i].equals("*")){
					//CASO ESPECIAL DE COLUMNA *
					this.hasSpecialColumn=true;
					continue;
				}
				String[] t = fields[i].split("#");
	
				// mirar si hay descripcion
				String[] fd = t[0].split(":");
				String field = t[0];
				String descripcion = "";
				if (fd.length >= 2) {
					field = fd[0];
					descripcion = fd[1];
				}
				//determinar si este campo debe ser fullText o no (si el nombre del campo acaba en '-' No, si acaba en '+' si, si no pone nada->si
				boolean fullText = field.endsWith("-")?false:true; 
	
				String type = "";
				String size = "";
				String secuence = "";
				if (t.length > 1) {
					type = t[1];
					if (!"TNFDS".contains(type))
						throw new Exception("Error en la sintaxis GenericCrudMapperInfo.setInfo( " + info + " ), TIPOS VALIDOS: T,N,F,D,S");
					if (t.length>2) {
						if (type.equals("T")) size = t[2];
						else if (type.equals("S")) secuence = t[2];
					}
				}
				//TODO PONER FIELD A MAYSUCULAS?
				GenericMapperInfoColumn column = new GenericMapperInfoColumn(field, type, size, fullText, secuence, descripcion);
				this.fields.add(column);
				this.types.put(column.getName().toLowerCase(), column.getType());
			}
		}
		//System.out.println("## GenericCrudMapperInfoTable RES:" + this.toString());
	}
	
	public void setSecurity(String secInfo) throws Exception {
		System.out.println("## GenericCrudMapperInfoTable: SECURITY: " + secInfo);
		String[] tokens = secInfo.split("\\|");
		if (tokens.length < 2) throw new Exception("Error en la sintaxis GenericCrudMapperInfo.setSecurity( " + secInfo + " )");

		//establezco los parametros de la tabla: nombre y separador de keys y resolvedorPordefecto
		String head=tokens[0];
		if (head.contains("@")){
			String[] vTable=head.split("@");
			System.out.println("CASO3");
			table=vTable[0];
			virtualTable=vTable[0];
			if (context==null) throw new Exception("Context No inicializada");
			Object bean=context.getBean(vTable[1]);
			if (bean instanceof GenericMapperSecurityTableResolver){
				secResolver = (GenericMapperSecurityTableResolver) bean;
			} else throw new Exception("Error. Clase no implementa interfaz GenericCrudMapperSecurityTableResolver. setSecurity( " + secInfo + " )");
		} else {
			System.out.println("CASO4");
			table=head;
			virtualTable=head;
		}
		
		//troceo los actions por ,
		System.out.println("Actions="+tokens[1]);
		String actions[] = tokens[1].split(",");
		for(String action:actions){
			//separo key=valor
			System.out.println("Action="+action);
			int i1= action.indexOf("=");
			if (i1==-1) throw new Exception("Error. Falta = en definicion security:"+action+" -> "+tokens[1]+" -> "+secInfo);
			String key=action.substring(0,i1);
			String valor=action.substring(i1+1);
			
			System.out.println("key="+key+" valor="+valor);
			//CADA LETRA DEL KEY ES INTERPRETADA COMO UNA ACCION: SIUDE
			for(int i=0;i<key.length();i++){
				String k=key.substring(i,i+1);
				System.out.println(k);
				//tomo la lista de procesos asociada a la accion o la creo nueva
				List<String> lproc = actionRoles.get(k);
				if (lproc==null) {
					lproc = new ArrayList<String>();
					actionRoles.put(k, lproc);
				}
				
				//separo valor en sus correspondientes tokens :
				String procesos[] = valor.split(":");
				for(String proceso:procesos){
					System.out.println("proc="+proceso);
					if (!lproc.contains(proceso)) lproc.add(proceso);
				}
				System.out.println("....");
			}
		}
		System.out.println("------");
		
		//TODO Comprobar la sintaxis y la semantica
		//TODO cachear la expresion regular, optimizar el analisis sitactico 

	}

	//se encarga de tomar los requisitos de seguridad de una tabla
	public void changeSecurity(GenericMapperInfoTable tableData) {
		setActionRoles(tableData.getActionRoles());
		setSecResolver(tableData.getSecResolver());
	}
	
	
	public String toString() {
		String res = "{\"type\": \""+type+"\", \"table\": \""+table+"\", \"keys\": [";
		for (String key : keys) {
			res += "\""+ key + "\", ";
		}
		res+="\"\"], ";
		res += "\"keySeparator\": \""+keySeparator+"\", \"selectFilter\": \""+selectFilter+"\", ";
		res += "\"fileds\": [";
		for (GenericMapperInfoColumn field : fields)
			res += field.toString() + ",";
		res += "{} ], ";
		
		res+="\"security\": [";
		if (actionRoles!=null){
			for (String key:actionRoles.keySet()){
				List<String> lproc = actionRoles.get(key);
				res+="{ \"key\":\""+key+"\", \"roles\": \"";
				for(String proc:lproc) res+=proc+",";
				res+="\"}, ";
			}
			res+="{}";
		}
		res += "] }";
		return res;
	}

	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public List<GenericMapperInfoColumn> getFields() {
		return fields;
	}
	public void setFields(List<GenericMapperInfoColumn> fields) {
		this.fields = fields;
	}
	public List<String> getKeys() {
		return keys;
	}
	public void setKeys(List<String> keys) {
		this.keys = keys;
	}
	public String getKeySeparator() {
		return keySeparator;
	}
	public void setKeySeparator(String keySeparator) {
		this.keySeparator = keySeparator;
	}
	public String getVirtualTable() {
		return virtualTable;
	}
	public void setVirtualTable(String virtualTable) {
		this.virtualTable = virtualTable;
	}
	public GenericMapperInfoTableResolver getResolver() {
		return resolver;
	}
	public void setResolver(GenericMapperInfoTableResolver resolver) {
		this.resolver = resolver;
	}
	public ApplicationContext getContext() {
		return context;
	}
	public void setContext(ApplicationContext context) {
		this.context = context;
	}
	public Map<String, List<String>> getActionRoles() {
		return actionRoles;
	}
	public void setActionRoles(Map<String, List<String>> actionRoles) {
		this.actionRoles = actionRoles;
	}
	public GenericMapperSecurityTableResolver getSecResolver() {
		return secResolver;
	}
	public void setSecResolver(GenericMapperSecurityTableResolver secResolver) {
		this.secResolver = secResolver;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public Map<String, String> getTypes() {
		return types;
	}
	public void setTypes(Map<String, String> types) {
		this.types = types;
	}
	public boolean isHasFields() {
		return hasFields;
	}
	public void setHasFields(boolean hasFields) {
		this.hasFields = hasFields;
	}
	public boolean isHasKeys() {
		return hasKeys;
	}
	public void setHasKeys(boolean hasKeys) {
		this.hasKeys = hasKeys;
	}
	public boolean isHasSpecialColumn() {
		return hasSpecialColumn;
	}
	public void setHasSpecialColumn(boolean hasSpecialColumn) {
		this.hasSpecialColumn = hasSpecialColumn;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSelectFilter() {
		return selectFilter;
	}
	public void setSelectFilter(String selectFilter) {
		this.selectFilter = selectFilter;
	}
}
