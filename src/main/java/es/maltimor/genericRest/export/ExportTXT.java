package es.maltimor.genericRest.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExportTXT implements ExportableData {
	private StringBuilder res;
	private List<String> fields;
	private Map<String,String> params;
	private String separator;
	private List<String> alias;
	
	public ExportTXT(List<String> fields){
		this.fields = fields;
		this.alias=null;
		this.res = new StringBuilder();
		this.separator = ",";
	}
	public ExportTXT(List<String> fields,Map<String,String> params){
		this(fields);
		this.params = params;
		setParams(params);
	}
	
	public void doHead(){
        List<String> heads = alias!=null? alias : fields;
		for(int i=0;i<heads.size();i++) {
			res.append(heads.get(i));
			if (i<heads.size()-1) res.append(separator);
		}
		res.append("\n");
	}
	
	public void doBody(List<Map<String,Object>> data){
		for(Map<String,Object> map:data){
			for(int i=0;i<fields.size();i++) {
				Object value=map.get(fields.get(i));
				if (value==null) value="";
				res.append(value);
				if (i<fields.size()-1) res.append(separator);
			}
			res.append("\n");
		}
	}
	
	public void doFoot(){
	}
	
	public Object getResult(){
		return res.toString();
	}

	public String getSeparator() {
		return separator;
	}
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
		if (params.containsKey("separator")) this.setSeparator(params.get("separator"));
		if (params.containsKey("alias")) {
			this.alias=new ArrayList<String>();
			String aux[] = params.get("alias").split(",");
			for(String a:aux) alias.add(a);
		}
	}
}
