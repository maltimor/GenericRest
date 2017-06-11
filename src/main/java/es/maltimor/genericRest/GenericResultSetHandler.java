package es.maltimor.genericRest;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

@Intercepts({ @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = { Statement.class }) })
public class GenericResultSetHandler implements Interceptor {
	public static ResultSetMetaData md;
	public static ResultSetMetaData md2;

	public Object intercept(Invocation invocation) throws Throwable {
		Object[] args = invocation.getArgs();
		Statement statement = (Statement) args[0];
	    ResultSet rs = getFirstResultSet(statement);
		if (rs != null) {
			md = new ResultSetMetaDataWrapper(rs.getMetaData());
		}
		Object results = invocation.proceed();
		return results;
	}
	
	  private ResultSet getFirstResultSet(Statement stmt) throws SQLException {
		    ResultSet rs = stmt.getResultSet();
		    while (rs == null) {
		      // move forward to get the first resultset in case the driver
		      // doesn't return the resultset as the first result (HSQLDB 2.1)
		      if (stmt.getMoreResults()) {
		        rs = stmt.getResultSet();
		      } else {
		        if (stmt.getUpdateCount() == -1) {
		          // no more results. Must be no resultset
		          break;
		        }
		      }
		    }
		    return rs;
		  }

		  private ResultSet getNextResultSet(Statement stmt) throws SQLException {
		    // Making this method tolerant of bad JDBC drivers
		    try {
		      if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
		        // Crazy Standard JDBC way of determining if there are more results
		        if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
		          ResultSet rs = stmt.getResultSet();
		          return rs;
		        }
		      }
		    } catch (Exception e) {
		      // Intentionally ignored.
		    }
		    return null;
		  }

		  private void closeResultSet(ResultSet rs) {
		    try {
		      if (rs != null) {
		        rs.close();
		      }
		    } catch (SQLException e) {
		      // ignore
		    }
		  }

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	public class ResultSetMetaDataWrapper implements ResultSetMetaData {
		private int columnCount;
		private String[] columnLabel;
		private String[] columnName;
		private String[] columnTypeName;
		private int[] columnDisplaySize;

		public ResultSetMetaDataWrapper(ResultSetMetaData md) throws Exception{
			this.columnCount= md.getColumnCount();
			this.columnLabel = new String[this.columnCount+1];
			this.columnName = new String[this.columnCount+1];
			this.columnTypeName = new String[this.columnCount+1];
			this.columnDisplaySize = new int[this.columnCount+1];
			for (int i=1;i<=this.columnCount;i++){
				this.columnLabel[i]=md.getColumnLabel(i);
				this.columnName[i]=md.getColumnName(i);
				this.columnTypeName[i]=md.getColumnTypeName(i);
				this.columnDisplaySize[i]=md.getColumnDisplaySize(i);
			}
    	}
		
		public String toString(){
			return "colCount:"+columnCount+" label:"+columnLabel.length+" Name:"+columnName.length;
		}

		public int getColumnCount() throws SQLException {
			return columnCount;
		}
		public String getColumnLabel(int column) throws SQLException {
			return this.columnLabel[column];
		}
		public String getColumnName(int column) throws SQLException {
			return this.columnName[column];
		}
		public String getColumnTypeName(int column) throws SQLException {
			return this.columnTypeName[column];
		}
		public int getColumnDisplaySize(int column) throws SQLException {
			return this.columnDisplaySize[column];
		}


		/* METODOS SIN IMPLEMENTAR */
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return null;
		}
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return false;
		}
		public boolean isAutoIncrement(int column) throws SQLException {
			return false;
		}
		public boolean isCaseSensitive(int column) throws SQLException {
			return false;
		}
		public boolean isSearchable(int column) throws SQLException {
			return false;
		}
		public boolean isCurrency(int column) throws SQLException {
			return false;
		}
		public int isNullable(int column) throws SQLException {
			return 0;
		}
		public boolean isSigned(int column) throws SQLException {
			return false;
		}
		public String getSchemaName(int column) throws SQLException {
			return null;
		}
		public int getPrecision(int column) throws SQLException {
			return 0;
		}
		public int getScale(int column) throws SQLException {
			return 0;
		}
		public String getTableName(int column) throws SQLException {
			return null;
		}
		public String getCatalogName(int column) throws SQLException {
			return null;
		}
		public int getColumnType(int column) throws SQLException {
			return 0;
		}
		public boolean isReadOnly(int column) throws SQLException {
			return false;
		}
		public boolean isWritable(int column) throws SQLException {
			return false;
		}
		public boolean isDefinitelyWritable(int column) throws SQLException {
			return false;
		}
		public String getColumnClassName(int column) throws SQLException {
			return null;
		}
	}
}