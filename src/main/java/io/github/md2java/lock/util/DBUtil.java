package io.github.md2java.lock.util;

import javax.sql.DataSource;

public class DBUtil {
	
	public static String getDriverClassName(DataSource dataSource) {
        if (dataSource != null) {
            try {
                return dataSource.getClass().getMethod("getDriverClassName").invoke(dataSource).toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
