package es.maltimor.genericRest.export;

import java.util.List;
import java.util.Map;

public class ExportHTML implements ExportableData{
	private StringBuilder res;
	private List<String> fields;
	
	public ExportHTML(List<String> fields){
		this.fields = fields;
		this.res = new StringBuilder();
	}
	public void doHead(){
		res.append("<table><thead><tr>");
		for(int i=0;i<fields.size();i++) res.append("<th>").append(fields.get(i)).append("</th>");
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
}
