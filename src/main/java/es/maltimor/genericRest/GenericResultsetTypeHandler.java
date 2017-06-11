package es.maltimor.genericRest;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

//@MappedJdbcTypes(JdbcType.CURSOR)
//@MappedTypes(List.class)
public class GenericResultsetTypeHandler extends BaseTypeHandler<Object> {
	
	@Override
	public Object getNullableResult(ResultSet rs, String colName) throws SQLException {
		Object value = rs.getObject(colName);
		try {
			return doRS(value);
		} catch (Exception e) { }
		return value;
	}

	@Override
	public Object getNullableResult(ResultSet rs, int colIndex) throws SQLException {
		Object value = rs.getObject(colIndex);
		try {
			return doRS(value);
		} catch (Exception e) { }
		return value;
	}

	@Override
	public Object getNullableResult(CallableStatement cs, int colIndex) throws SQLException {
		Object value = cs.getObject(colIndex);
		try {
			return doRS(value);
		} catch (Exception e) { }
		return value;
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
		ps.setObject(i, parameter);
	}
	
	private Object doRS(Object value) throws Exception{
		if (value!=null) {
			if (value instanceof ResultSet){
				ResultSet rs = (ResultSet) value;
				ResultSetMetaData rsmd = rs.getMetaData();
				int columns = rsmd.getColumnCount();
				List<Map<String,Object>> lmap = new ArrayList<Map<String,Object>>();
				while(rs.next()) {
					Map<String,Object> map = new HashMap<String,Object>();
					for(int i=1;i<=columns;i++){
						Object o = rs.getObject(i);
						o = doLOB(o);
						map.put(rsmd.getColumnName(i), o);
					}
					lmap.add(map);
				}
				return lmap;
			} else return value;
		} else return null;
	}

	private Object doLOB(Object value) throws Exception{
		if (value!=null) {
			if (value instanceof Blob){
				Blob blob = (Blob) value;
				byte[] buff= blob.getBytes(1,(int) blob.length());
				return buff;
			} else if (value instanceof Clob){
				Clob clob = (Clob) value;
				String buff= clob.getSubString(1,(int) clob.length());
				return buff;
			} else return value;
		} else return null;
	}
}
