package es.maltimor.genericRest.export;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExportExcel implements ExportableData{
	private List<String> fields;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	private int rownum;
	
	public ExportExcel(List<String> fields){
		this.fields = fields;
        this.workbook = new HSSFWorkbook();
        this.sheet = workbook.createSheet("pagina 1");
        this.rownum=0;
	}
	public void doHead(){
        HSSFRow row = sheet.createRow(rownum);
		for(int i=0;i<fields.size();i++) row.createCell(i).setCellValue(fields.get(i));
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
}
