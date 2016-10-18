package es.maltimor.genericRest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

//import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class GenericDatabaseInfo {//implements Runnable {
	private GenericDatabaseMapper mapper;
	private String resourceTable;
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
		for(Map<String,Object> doc:ldoc){
			String type=(String) doc.get("TYPE");
			String tableName=(String) doc.get("TABLE_NAME");
			String finalTable=(String) doc.get("FINAL_TABLE");
			String resolver=(String) doc.get("RESOLVER");
			String fields=(String) doc.get("FIELDS");
			String keys=(String) doc.get("KEYS");
			String sep=(String) doc.get("SEPARATOR");
			res+=type!=null?getType(type):"";
			res+=tableName;
			res+=finalTable!=null?":"+finalTable:"";
			res+=resolver!=null?"@"+resolver:defaultResolver!=null?"@"+defaultResolver:"";
			res+="|"+fields;
			res+="|"+keys;
			res+=(sep!=null?"|"+sep:"");
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
			String tableName=(String) doc.get("TABLE_NAME");
			String secResolver=(String) doc.get("SEC_RESOLVER");
			String secInfo=(String) doc.get("SEC_INFO");
			//TODO ver que hacer en caso de que sec_info==NULL
			if (secInfo!=null){
				res+=tableName;
				res+=secResolver!=null?"@"+secResolver:defaultSecResolver!=null?"@"+defaultSecResolver:"";
				res+="|"+secInfo+"\n";
			}
		}
		System.out.println("GET_SECURITY:"+res);
		return res;
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
}
