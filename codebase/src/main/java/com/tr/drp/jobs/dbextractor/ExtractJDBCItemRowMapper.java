package com.tr.drp.jobs.dbextractor;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for mapping rows of ResultSet on a per-row basis.
 *
 * @author Ramesh Sugunan
 */
public class ExtractJDBCItemRowMapper implements RowMapper<Map<String, String>> {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy");

    private static final int COUNTER_START_VALUE = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> mapRow(ResultSet result, int rowNum) throws SQLException {
        Map<String, String> details = new HashMap<>();
        ResultSetMetaData resultSetMetaData = result.getMetaData();
        int numberOfCoulmns = resultSetMetaData.getColumnCount();
        for (int index = COUNTER_START_VALUE; index <= numberOfCoulmns; index++) {
            String coulmnName = resultSetMetaData.getColumnName(index);


            Object value = result.getObject(coulmnName);
            String sValue = null;
            if (value != null) {
                sValue = value.toString();
                if (value instanceof String) {
                    sValue = '"'+sValue+'"';
                }
                if (value instanceof Date) {
                    sValue = dateFormat.format(value);
                }
            }

            details.put(coulmnName, sValue);
        }
        return details;
    }
}
