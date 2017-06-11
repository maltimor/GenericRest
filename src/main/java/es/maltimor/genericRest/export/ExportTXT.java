package es.maltimor.genericRest.export;

import java.util.List;
import java.util.Map;

public class ExportTXT implements ExportableData {
	private StringBuilder res;
	private List<String> fields;
	private String separator;
	
	public ExportTXT(List<String> fields){
		this.fields = fields;
		this.res = new StringBuilder();
		this.separator = ",";
	}
	
	public void doHead(){
		for(int i=0;i<fields.size();i++) {
			res.append(fields.get(i));
			if (i<fields.size()-1) res.append(separator);
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
}
