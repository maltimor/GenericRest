package es.maltimor.genericRest.export;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExportExcel implements ExportableData{
	private Map<String,String> params;
	private List<String> fields;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	private int rownum;
	//parametros añadidos a la funcionalidad de exportacion aexcel
	private List<String> alias;
	
	
	public ExportExcel(List<String> fields){
		this.fields = fields;
		this.alias = null;
        this.workbook = new HSSFWorkbook();
        this.sheet = workbook.createSheet("pagina 1");
        this.rownum=0;
	}
	public ExportExcel(List<String> fields,Map<String,String> params){
		this(fields);
		this.setParams(params);
	}
	public void doHead(){
        HSSFRow row = sheet.createRow(rownum);
        List<String> heads = alias!=null? alias : fields;
		for(int i=0;i<heads.size();i++) row.createCell(i).setCellValue(heads.get(i));
		rownum++;
	}
	public void doBody(List<Map<String,Object>> data){
		for(Map<String,Object> map:data){
            HSSFRow row = sheet.createRow(rownum);
			for(int i=0;i<fields.size();i++) {
				Object value=map.get(fields.get(i));
				if (value==null) value="";
				row.createCell(i).setCellValue(value.toString());
			}
			rownum++;
		}
	}
	public void doFoot(){
	}
	
	public Object getResult(){
        try{
        	ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        	workbook.write(bOut);
        	bOut.close();
            return bOut.toByteArray();
        } catch (Exception e){
        	return "Ocurrio un error: "+e.getMessage();
        }
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
