package es.maltimor.genericRest.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExportHTML implements ExportableData{
	private StringBuilder res;
	private List<String> fields;
	private Map<String,String> params;
	//parametros seleccionados por params
	private List<String> alias;

	
	public ExportHTML(List<String> fields){
		this.fields = fields;
		this.alias=null;
		this.res = new StringBuilder();
	}
	public ExportHTML(List<String> fields,Map<String,String> params){
		this(fields);
		this.setParams(params);
	}
	public void doHead(){
		res.append("<table><thead><tr>");
        List<String> heads = alias!=null? alias : fields;
		for(int i=0;i<heads.size();i++) res.append("<th>").append(heads.get(i)).append("</th>");
		res.append("</tr></thead><tbody>");		
	}
	public void doBody(List<Map<String,Object>> data){
		for(Map<String,Object> map:data){
			res.append("<tr>");
			for(int i=0;i<fields.size();i++) {
				Object value=map.get(fields.get(i));
				if (value==null) value="";
				res.append("<td>").append(value).append("</td>");
			}
			res.append("</tr>");
		}
	}
	public void doFoot(){
		res.append("</tbody></table>");
	}
	
	public Object getResult(){
		return res.toString();
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
		if (params.containsKey("alias")) {
			this.alias=new ArrayList<String>();
			String aux[] = params.get("alias").split(",");
			for(String a:aux) alias.add(a);
		}
	}
}
