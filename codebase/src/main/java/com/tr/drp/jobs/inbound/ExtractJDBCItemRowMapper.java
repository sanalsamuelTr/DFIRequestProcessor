package com.tr.drp.jobs.inbound;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for mapping rows of ResultSet on a per-row basis.
 * 
 * @author Ramesh Sugunan
 *
 */
public class ExtractJDBCItemRowMapper implements RowMapper<Map<String, String>> {

	private static final int COUNTER_START_VALUE = 1;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

			details.put(coulmnName, value == null ? null : value.toString());
		}
		return details;
	}
}