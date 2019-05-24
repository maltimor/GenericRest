package es.maltimor.genericRest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.maltimor.genericUser.User;
import es.maltimor.springUtils.ContextAware;
import es.maltimor.webUtils.JDBCBridge;


//NOTA: la indexacion se hace por medio del campo virtualTable
public class GenericMapperInfo {
	private Map<String, GenericMapperInfoTable> data;
	private String info;
	private String security;
	private String driver;
	private ApplicationContext context;

	private GenericMapperInfoTableResolver defaultResolver;
	private GenericMapperSecurityTableResolver defaultSecResolver;
	
	private GenericDatabaseInfo dbInfo;
	
	//variables internas para guardar el estado inicial de info y security
	private String _infoOrg;
	private String _securityOrg;
	
	//mapper para ralizar el test de comprobacion y de metadatos
	private GenericMapperInfoMapper mapper;
	private SqlSessionFactory sqlSessionFactory;
	
	//cache de objetos mapperinfo
	
	
	
	
	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}


	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}


	public GenericMapperInfoMapper getMapper() {
		return mapper;
	}


	public void setMapper(GenericMapperInfoMapper mapper) {
		this.mapper = mapper;
	}


	public GenericMapperInfo() {
		this.context = ContextAware.getContext();
		this.data = new HashMap<String, GenericMapperInfoTable>();
		this.defaultResolver = new GenericMapperInfoTableResolverImpl();			//resolver por defecto
		this.defaultSecResolver = new GenericMapperSecurityTableResolverImpl();			//resolver por defecto
		this.dbInfo=null;
	}

	
	public GenericMapperInfoTableResolver getDefaultResolver() {
		return defaultResolver;
	}
	public void setDefaultResolver(
			GenericMapperInfoTableResolver defaultResolver) {
		this.defaultResolver = defaultResolver;
	}
	public GenericMapperSecurityTableResolver getDefaultSecResolver() {
		return defaultSecResolver;
	}
	public void setDefaultSecResolver(
			GenericMapperSecurityTableResolver defaultSecResolver) {
		this.defaultSecResolver = defaultSecResolver;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public GenericDatabaseInfo getDbInfo() {
		return dbInfo;
	}
	public void setDbInfo(GenericDatabaseInfo dbInfo) {
		this.dbInfo = dbInfo;
	}
	public String getInfo() {
		return info;
	}

	// este metodo es el que se usara para simplificar la entrada de datos desde
	// spring
	public void setInfo(String info) throws Exception {
		//System.out.println("## GenericCrudMapperInfo:" + info);
		_infoOrg = info;
		//justo en este momento, si dbInfo tiene algo lo anexo aqui
		if (dbInfo!=null) {
			info=info.trim()+"\n"+dbInfo.getInfo();
			info=info.trim();
			//System.out.println("DBInfo:" + info);
		}
		setInfoInternal(info);
	}
	
	private void setInfoInternal(String info) throws Exception {
		//System.out.println("SetinfoInternal:->" + info+"<-");
		this.info = info;
		String[] tables = info.split("\n");
		for (int j = 0; j < tables.length; j++) {
			// ahora lo añado en data
			String t = tables[j].replace("\r", "").trim();
			if (!t.equals("")) {
				//TODO Comprobar que no exista en la tabla previamente, permite ampliar la definicion de seguridad despues
				//TODO Localizar la tabla en funcion de su clave que ahora es la virtualTable
				//TOD Ver si crear o recoger del array de tablas
				GenericMapperInfoTable tableData = new GenericMapperInfoTable();
				tableData.setDriver(driver);
				tableData.setResolver(defaultResolver);
				tableData.setSecResolver(defaultSecResolver);
				tableData.setContext(context);
				tableData.setInfo(t);
				//NOTA: la indexacion se hace por medio del virtualTable
				//Al hacer un put se machaca la tabla que hubiera antes.
				data.put(tableData.getVirtualTable().toLowerCase(), tableData);
			}
		}
		//System.out.println("## GenericCrudMapperInfo.setInfoInternal RES:" + this.toString());
		comprobador();
	}
	
	//este metodo se encarga de la definicion de la seguridad
	public void setSecurity(String security) throws Exception{
		//System.out.println("## GenericCrudMapperInfo.SetSecurity:" + security);
		_securityOrg = security;
		//justo en este momento, si dbInfo tiene algo lo anexo aqui
		if (dbInfo!=null) {
			security=security.trim()+"\n"+dbInfo.getSecurity();
			security=security.trim();
			//System.out.println("DBInfo:" + security);
		}
		setSecurityInternal(security);
	}
	
	private void setSecurityInternal(String security) throws Exception {
		//System.out.println("SetSecurityInternal:->" + security+"<-");
		this.security = security;
		String[] tables = security.split("\n");
		for (int j = 0; j < tables.length; j++) {
			// ahora lo añado en data
			String t = tables[j].replace("\r", "").trim();
			if (!t.equals("")) {
				GenericMapperInfoTable tableData = new GenericMapperInfoTable();
				tableData.setDriver(driver);
				tableData.setResolver(defaultResolver);
				tableData.setSecResolver(defaultSecResolver);
				tableData.setContext(context);
				tableData.setSecurity(t);
				//NOTA: la indexacion se hace por medio del virtualTable
				//Al hacer un put se machaca la tabla que hubiera antes.
				//antes de insertar la tabla veo si ya estaba creada por medio de setInfo.
				//Si es asi, tomo la informacion de seguridad y la fusiono con la tabla ya existente
				if (data.containsKey(tableData.getVirtualTable().toLowerCase())){
					GenericMapperInfoTable table = data.get(tableData.getVirtualTable().toLowerCase());
					table.changeSecurity(tableData);
				} else data.put(tableData.getVirtualTable().toLowerCase(), tableData);
			}
		}
		//System.out.println("## GenericCrudMapperInfo.setSecurityInternal RES:" + this.toString());
	}
	
	/* Esta operacion recorre todos los elementos de la tabla y por cada elemento compara sus columnas con las columnas recibidas de la base de datos
	 * Si la tabla no esta en la base de datos nos dara una excepcion como que esta tabla no existe
	 * Si la columna no esta en la base de datos nos dara una excepcion como que esa columna no existe
	 * TODO Si la pk no esta en la base de datos nos dara una excepcion como que esa columna no existe [falta comprobar si es realmente una pk]
	 * Si la descripcion,tipo,tamaño estan vacios se auto rellenan con la informacion recibida sino comprueba que el dado es el mismo que el de la BD
	 * Mapea los tamaños y los tipos para estadisticas
	 * 
	 */
	private void comprobador() throws Exception {
		Connection conn = null;
		conn = JDBCBridge.getConnection();
		//MAPA DE TIPOS Y TAMA�OS:
		Map<String, Integer> tipos = new HashMap<String, Integer>();
		Map<Integer, Integer> sizes = new HashMap<Integer, Integer>();
		
		boolean lanzaExcepcion=false;
		String mensajesExcepcion="";
		
		long ini = System.currentTimeMillis();
		
		////		
		for (GenericMapperInfoTable table : data.values()) {
			//LOS CASOS ESPECIALES DE FUNCION Y PROCEDURE SE OMITEN
			if (!table.isSelectable()) {
				//System.out.println("OMITIENDO "+table.getType()+" "+table.getVirtualTable());
				continue;
			}
			
			GenericMapperInfoTableResolver resolver = table.getResolver();
			
			//TODO de momento solo puedo comprobar las sql sencillas (sin parametros de url)
			String sql = resolver.getTestSQL(table.getTable());
			//System.out.println("SQL="+sql);
			
			//if ((sql.contains("#{")||sql.contains("${"))) {
			//	System.out.println("Omitiendo comprobacion de tabla:"+table.getTable());
			//	continue;
			//}
			
			ResultSet rs =null;
			Statement stmt =null;
			try {
				//rs = stmt.executeQuery("SELECT * FROM (" + resolver.getSQL(null, table.getTable())+")");
				//System.out.println("### ANTES DE SQL:"+sql);
				
				ResultSetMetaData md=null;
				
				//SI HAY MAPPER LO HAGO POR AQUI
				Map<String,Object> params = new HashMap<String,Object>();
				//ademas para el caso especial de BasicDatabaseMapper incluyo los parametros
				if (resolver instanceof GenericTableResolverTestParams){
					GenericTableResolverTestParams presolver = (GenericTableResolverTestParams) resolver;
					params = presolver.getTestParams(table.getTable());
					if (params!=null && params.size()>0){
						System.out.println("* PARAMS:"+params);
						System.out.println("* SQL:"+sql);
						//otra forma de hacer test es tratando los parametros #{} como un string y sustituyendo en params
						for(String key:params.keySet()){
							Object value = params.get(key);
							if (value!=null){
								String search = "#{"+key+"}";
								String replace = ""+value;
								sql=sql.replace(search, replace);
							}
						}
						System.out.println("* SQL END:"+sql);
					}
				}
				params.put("sql", sql);
				if (mapper!=null){
					GenericResultSetHandler.md = null;
					List<Map<String,Object>> res = mapper.getSQL(params);
					//System.out.println("MAPPER:"+res);
					//System.out.println("md:"+GenericResultSetHandler.md);
					md = GenericResultSetHandler.md;
				}
				
				//SI HAY SQL SESSION FACTORY
/*				if (sqlSessionFactory!=null){
					SqlSession session = null;
					try{
						session = sqlSessionFactory.openSession();
						System.out.println("ANTES DE ejecutar----");
						Configuration configuration = session.getConfiguration();
						
						if (!configuration.hasMapper(GenericMapperInfoMapper.class)) {
							try {
								configuration.addMapper(GenericMapperInfoMapper.class);
							} catch (Exception e) {
								System.out.println("Error while adding the mapper 'es.maltimor.genericRest.GenericMapperInfoMapper' to configuration.");
								throw new IllegalArgumentException(e);
							} finally {
								ErrorContext.instance().reset();
							}
						}
						
						if (!configuration.hasStatement("kk")) {
							System.out.println("Insertar KK");
							List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
							SqlSourceBuilder ss = new SqlSourceBuilder(configuration);
							MappedStatement.Builder builder = new Builder(configuration, "kk", ss.parse(sql, lst.getClass(), null), SqlCommandType.SELECT);
							configuration.addMappedStatement(builder.build());
							System.out.println("Insertar KK.FIN");
						}

						Collection<?> c = configuration.getMappedStatements();
						Iterator<?> i = c.iterator();
						while (i.hasNext()){
							Object o = i.next();
							if (o instanceof MappedStatement) System.out.println(((MappedStatement)o).getId());
							else System.out.println(i.next().getClass());
						}
						
						//System.out.println("antes de mp:");
						GenericMapperInfoMapper mp = configuration.getMapper(GenericMapperInfoMapper.class, session);
						List<Map<String,Object>> res2 = mp.getSQL(params);
						System.out.println("MP:"+res2);

						System.out.println("----------");
						
						//List<Map<String,Object>> res = session.selectList(sql);
						//System.out.println("FACTORY:"+res);
						
						ResultHandler handler = new GenericResultHandler2();
						System.out.println("antes de mp:");
						session.select("es.maltimor.genericRest.GenericMapperInfoMapper.getSQL",params,handler);
						System.out.println("+++++");
					} catch (Exception e2){
						e2.printStackTrace();
					} finally {
						session.close();
					}
				}*/
				
				if (md==null){
					//antigua forma de verificacion de la sentencia
					//System.out.println("---verificacion antigua-----");
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					md = rs.getMetaData();
				} //else System.out.println("!!!!!!!!!!!!!!!!! MD !!!!!!!!!!!!!!!");

				//String tablaOut = "["+table.getTable()+": ";
				
				//CASO DONDE HAY UNA DEFINICION DE LOS KEYS
				if (table.isHasFields()){
					for (GenericMapperInfoColumn columna : table.getFields()) {
						boolean encontrado = false;
						String columnas = "[";
						for (int k = 1; k <= md.getColumnCount(); k++) {
							String columnName = md.getColumnLabel(k);
							columnas+=columnName+",";
							//String columnName = md.getColumnName(k);
							if (columnName.equals(columna.getName())) {
								encontrado= true;
								//tablaOut+= "("+ columnName +"," + md.getColumnTypeName(k)+"," + md.getColumnDisplaySize(k)+")";
								//mapeo de tipos y tama�os:
								if(tipos.containsKey( md.getColumnTypeName(k) )) tipos.put( md.getColumnTypeName(k), tipos.get(md.getColumnTypeName(k))+1);
								else tipos.put(md.getColumnTypeName(k), 1);
								
								if( sizes.containsKey( md.getColumnDisplaySize(k) ) ) sizes.put( md.getColumnDisplaySize(k), sizes.get( md.getColumnDisplaySize(k) )+1);
								else sizes.put( md.getColumnDisplaySize(k), 1);
								//
								// getColumnLabel(k)>etiqueta
								// getColumnClassName(k)>java.lang.String etc...
								// getColumnTypeName(k)>varchar, DATE...
								// getColumnDisplaySize(k)>tamaño
								if (columna.getDescription().isEmpty()) columna.setDescription(columnName);
								String tipo = columna.getType();
								if (tipo.isEmpty()) {
									columna.setType(convertirTipo(md.getColumnTypeName(k)));
									table.getTypes().put(columna.getName().toLowerCase(), columna.getType());
								} else {
									//comprobamos el tipo si es el mismo que el dado o si es equivalente
									if (!collateTipo(tipo,convertirTipo(md.getColumnTypeName(k)))){
										lanzaExcepcion=true;
										mensajesExcepcion+="Error en la sintaxis de la tabla "+ table.getTable()+"," + columna.getName() + " es de tipo "+convertirTipo(md.getColumnTypeName(k))+"\n";
										continue;
									}
								}
								// TODO comprobar que el tamaño dado es valido
								if (columna.getSize().isEmpty()) columna.setSize(Integer.toString(md.getColumnDisplaySize(k)));
								else {
									//comprobamos el size si es el mismo que el dado.
									if (!columna.getSize().equals(Integer.toString(md.getColumnDisplaySize(k)))){
										lanzaExcepcion=true;
										mensajesExcepcion+="Error en la sintaxis de la tabla "+ table.getTable()+"," + columna.getName() + " es de tama�o "+Integer.toString(md.getColumnDisplaySize(k))+"\n";
										continue;
									}
								}
							}
							if (encontrado) break;
						}
						columnas+="]";
						if (!encontrado) {
							lanzaExcepcion=true;
							mensajesExcepcion+="Error en la sintaxis," +  columna.getName() + " no existe en " + table.getTable() + ":" +columnas +"\n";
							continue;
						}
					}
				}
				//CASO DONDE NO HAY FIELDS Y/O HAY UN FIELD * -> OBTENGO LOS FIELDS DE LA CONSULTA y lo A�ADO A LO QUE TENGA
				if (table.isHasSpecialColumn()) {
					//System.out.println("TABLA "+table.getTable());
					for (int k = 1; k <= md.getColumnCount(); k++) {
						String columnName = md.getColumnLabel(k);
						//tablaOut+= "("+ columnName +"," + md.getColumnTypeName(k)+"," + md.getColumnDisplaySize(k)+")";
						//mapeo de tipos y tama�os:
						if(tipos.containsKey( md.getColumnTypeName(k) )) tipos.put( md.getColumnTypeName(k), tipos.get(md.getColumnTypeName(k))+1);
						else tipos.put(md.getColumnTypeName(k), 1);
						
						if( sizes.containsKey( md.getColumnDisplaySize(k) ) ) sizes.put( md.getColumnDisplaySize(k), sizes.get( md.getColumnDisplaySize(k) )+1);
						else sizes.put( md.getColumnDisplaySize(k), 1);
						
						//SOLO INSERTO LA COLUMNA SI NO ESTABA DADA DE ALTA PREVIAMENTE
						//SI LO HUBIERA ESTADO DEBERIA HABER PASADO LA COMPROBACION
						boolean encontrado=false;
						for(GenericMapperInfoColumn col:table.getFields()) if (col.getName().equals(columnName)) { encontrado=true; break; }
						
						if (!encontrado){
							//System.out.println("INCLUYENDO "+columnName);
							GenericMapperInfoColumn columna = new GenericMapperInfoColumn();
							columna.setName(columnName);
							columna.setDescription(columnName);
							columna.setType(convertirTipo(md.getColumnTypeName(k)));
							columna.setSize(Integer.toString(md.getColumnDisplaySize(k)));
							columna.setFullText(true);
							table.getFields().add(columna);
							table.getTypes().put(columna.getName().toLowerCase(), columna.getType());
						} else {
							//System.out.println("OMITENDO "+columnName);
						}
					}
				}
				//System.out.println(tablaOut+"|"+table.getKeys().get(0)+"]"); //Para mostrar lo que viene de la BD
				//[TABLA: (col,tipo,tamaño),.... | pk]
			} catch (Exception e) {
				//Salta si la tabla no existe:
				e.printStackTrace();
				lanzaExcepcion=true;
				mensajesExcepcion+="Error en la sintaxis, la tabla " +  table.getTable() + " no existe\n";
				continue;
			} finally {
				if (rs!=null) rs.close();
				if (stmt!=null) stmt.close();
				rs=null;
				stmt=null;
			} 
		}
		//MOSTRAMOS TIPOS Y TAMAÑOS:
		//System.out.print("TIPOS: ");
		for(Entry<String, Integer> entry : tipos.entrySet()) {
			//System.out.print(entry.getKey()+"("+entry.getValue()+"), ");
		}
		//System.out.println();
		//System.out.print("TAMA�OS: ");
		for(Entry<Integer, Integer> entry : sizes.entrySet()) {
			//System.out.print(entry.getKey()+"("+entry.getValue()+"), ");
		}
		//System.out.println();
		
		
		/*String json = new ObjectMapper().writeValueAsString(data);
		System.out.println("=====================================================");
		System.out.println(json);
		System.out.println("=====================================================");*/
		
		
		if (lanzaExcepcion){
			throw new Exception("Error GLOBAL: "+mensajesExcepcion);
		}
		/////////////////
		
		long t = System.currentTimeMillis()-ini;
		System.out.println("Tiempo en comprobar: "+(t/1000)+" seg.");
	}

	private String convertirTipo(String tipo) {
		tipo=tipo.toUpperCase();
		if (tipo.equals("CHAR") || tipo.equals("VARCHAR2") || (tipo.equals("VARCHAR"))) tipo = "T";
		else if (tipo.equals("DATE")) tipo = "F";
		else if (tipo.equals("TIMESTAMP")) tipo = "F";
		else if (tipo.equals("DATETIME")) tipo = "F";
		else if (tipo.equals("LONG") || tipo.equals("LONG RAW") || tipo.equals("NUMBER")) tipo = "N";
		else if (tipo.equals("DECIMAL")) tipo = "N";
		else if (tipo.equals("NUMERIC")) tipo = "N";
		else if (tipo.equals("INT")) tipo = "N";
		else if (tipo.equals("DOUBLE")) tipo = "N";
		else if (tipo.equals("CLOB")) tipo = "C";			// TODO Crear un tipo C
		else if (tipo.equals("BLOB")) tipo = "B";			// TODO Crear un tipo B
		else System.out.println("WARNING: Convertir Tipo NO RECONOCIDO:"+tipo);
		return tipo;
	}
	
	private boolean collateTipo(String tipoManual,String tipoBBDD){
		//if (tipoManual.equals("T")) return true;		//todos los tipos aceptan Texto
		if (tipoManual.equals("D")) tipoManual="F";		//D es equivalente a F
		if (tipoManual.equals("S")) tipoManual=tipoBBDD;//S es equivalente a numero de secuencia, el valor lo debe aceptar siempre
		return tipoManual.equals(tipoBBDD);
	}

	// TODO opcional: usar * en la lista de campos y los coge todos
	// TODO opcional: usar - para eliminar campos de una lista
	// TODO opcional: mejorar la sintaxis para admintir:
	// TODO opcional: estudiar numeros con decimales
	// TABLA en vez de TABLA|*|KEY
	// TABLA||
	// TODO investigar claves forabneas y primaria:
	/* INTENTO DE OBTENER PRIMARY KEY:
	 * DatabaseMetaData meta = conn.getMetaData(); ResultSet keys =
	 * meta.getPrimaryKeys(null, null, "BARRIOS");
	 * //System.out.println("La clave primaria es"
	 * +keys.getString("COLUMN_NAME")); ResultSetMetaData mdkeys =
	 * keys.getMetaData(); System.out.println(mdkeys.getColumnCount()); for
	 * (int k = 1; k <= mdkeys.getColumnCount(); k++)
	 * System.out.println("la clave primaria: "+mdkeys.getColumnLabel(k));
	 */
	
	public String toString() {
		String res = data.size() + "[\n";
		for (String key : data.keySet()) {
			res += key + "(" + data.get(key).toString() + ")\n";
		}
		res += "]";
		return res;
	}

	public List<String> getFields(String table) {
		if (data.containsKey(table.toLowerCase())) {
			List<String> fields = new ArrayList<String>();
			List<GenericMapperInfoColumn> columns = data.get(table.toLowerCase()).getFields();
			for (GenericMapperInfoColumn column : columns) {
				fields.add(column.getName());
			}
			return fields;
		}
		return null;
	}

	public String getKey(String table) {
		// TODO: COGE SOLO LA PRIMERA KEY
		if (data.containsKey(table.toLowerCase()))
			return data.get(table.toLowerCase()).getKeys().get(0);
		return null;
	}

	public GenericMapperInfoTable getInfoTable(String table) {
		if (data.containsKey(table.toLowerCase()))
			return data.get(table.toLowerCase());
		return null;
	}

	public List<String> getListTable() {
		data.keySet();
		ArrayList<String> nomTabla = new ArrayList<String>();

		for (String s : data.keySet()) {
			nomTabla.add(s);
		}

		return nomTabla;
	}
	
	public void reload() throws Exception{
		//antes de borrar guardo el estado actual de info y de security
		String infoAct = info;
		String securityAct = security;
		try{
			data.clear();
			setInfo(_infoOrg);
			setSecurity(_securityOrg);
		} catch (Exception e){
			//restablezclo el estado anterior pero lanzo una excepcion
			try{
				data.clear();
				setInfoInternal(infoAct);
				setSecurityInternal(securityAct);
			} catch (Exception e2){
				//un error doble al intentar recuperarse del error
				throw new Exception("Error al intentar recuperase: "+e2.getMessage()+ " "+ e.getMessage());
			}
			throw e;
		}
	}
}
