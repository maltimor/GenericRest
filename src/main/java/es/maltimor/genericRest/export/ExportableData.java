package es.maltimor.genericRest.export;

import java.util.List;
import java.util.Map;

public interface ExportableData {
	public void doHead();
	public void doBody(List<Map<String,Object>> data);
	public void doFoot();
	public Object getResult();
}
