package es.maltimor.genericRest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

//import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class GenericDatabaseInfo {//implements Runnable {
	private GenericDatabaseMapper mapper;
	private String resourceTable;
	private boolean columnNamesInUpperCase=true;

//	private ThreadPoolTaskScheduler threadPoolTaskScheduler;
	
/*	public void run() {
		System.out.println("·········································································· ");
	}*/
	
	private String defaultResolver;
	private String defaultSecResolver;

	//recupera las filas del resourceTable
	public String getInfo(){
//		this.threadPoolTaskScheduler.scheduleWithFixedDelay(this, 5000);
		String res ="";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("resourceTable", resourceTable);
		List<Map<String,Object>> ldoc = mapper.getRows(map);
		System.out.println("getInfo:"+ldoc);
		for(Map<String,Object> doc:ldoc){
			String type,tableName,finalTable,resolver,fields,keys,sep;
			if (columnNamesInUpperCase){
				type=(String) doc.get("TYPE");
				tableName=(String) doc.get("TABLE_NAME");
				finalTable=(String) doc.get("FINAL_TABLE");
				resolver=(String) doc.get("RESOLVER");
				fields=(String) doc.get("FIELDS");
				keys=(String) doc.get("KEYS");
				sep=(String) doc.get("SEPARATOR");
			} else {
				type=(String) doc.get("type");
				tableName=(String) doc.get("table_name");
				finalTable=(String) doc.get("final_table");
				resolver=(String) doc.get("resolver");
				fields=(String) doc.get("fields");
				keys=(String) doc.get("keys");
				sep=(String) doc.get("separator");
			}
	
			res+=isNotNull(type)?getType(type):"";
			res+=tableName;
			res+=isNotNull(finalTable)?":"+finalTable:"";
			res+=isNotNull(resolver)?"@"+resolver:isNotNull(defaultResolver)?"@"+defaultResolver:"";
			res+="|"+fields;
			res+="|"+keys;
			res+=isNotNull(sep)?"|"+sep:"";
			res+="\n";
		}
		System.out.println("GET_INFO:"+res);
		return res;
	}
	
	public String getSecurity(){
		String res ="";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("resourceTable", resourceTable);
		List<Map<String,Object>> ldoc = mapper.getRows(map);
		for(Map<String,Object> doc:ldoc){
			String tableName,secResolver,secInfo;
			if (columnNamesInUpperCase){
				tableName=(String) doc.get("TABLE_NAME");
				secResolver=(String) doc.get("SEC_RESOLVER");
				secInfo=(String) doc.get("SEC_INFO");				
			} else {
				tableName=(String) doc.get("table_name");
				secResolver=(String) doc.get("sec_resolver");
				secInfo=(String) doc.get("sec_info");
			}
			//TODO ver que hacer en caso de que sec_info==NULL
			if (isNotNull(secInfo)){
				res+=tableName;
				res+=isNotNull(secResolver)?"@"+secResolver:isNotNull(defaultSecResolver)?"@"+defaultSecResolver:"";
				res+="|"+secInfo+"\n";
			}
		}
		System.out.println("GET_SECURITY:"+res);
		return res;
	}
	
	private boolean isNotNull(Object aux){
		return (aux!=null && !aux.equals(""));
	}

	private String getType(String type){
		if (type.equals("FUNCTION")) return "=";
		else if (type.equals("PROCEDURE")) return "^";
		else return type;
	}
	
	public GenericDatabaseMapper getMapper() {
		return mapper;
	}
	public void setMapper(GenericDatabaseMapper mapper) {
		this.mapper = mapper;
	}
	public String getResourceTable() {
		return resourceTable;
	}
	public void setResourceTable(String resourceTable) {
		this.resourceTable = resourceTable;
	}
/*	public ThreadPoolTaskScheduler getThreadPoolTaskScheduler() {
		return threadPoolTaskScheduler;
	}
	public void setThreadPoolTaskScheduler(ThreadPoolTaskScheduler threadPoolTaskScheduler) {
		this.threadPoolTaskScheduler = threadPoolTaskScheduler;
	}*/
	public String getDefaultResolver() {
		return defaultResolver;
	}
	public void setDefaultResolver(String defaultResolver) {
		this.defaultResolver = defaultResolver;
	}
	public String getDefaultSecResolver() {
		return defaultSecResolver;
	}
	public void setDefaultSecResolver(String defaultSecResolver) {
		this.defaultSecResolver = defaultSecResolver;
	}
	public boolean isColumnNamesInUpperCase() {
		return columnNamesInUpperCase;
	}
	public void setColumnNamesInUpperCase(boolean columnNamesInUpperCase) {
		this.columnNamesInUpperCase = columnNamesInUpperCase;
	}
}
