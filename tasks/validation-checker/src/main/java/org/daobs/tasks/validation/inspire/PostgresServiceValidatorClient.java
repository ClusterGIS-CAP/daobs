package org.daobs.tasks.validation.inspire;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class PostgresServiceValidatorClient extends JdbcDaoSupport{
	
	/**
	 * JdbcTemplate to query database
	 */
	/**
	 * Valid INSPIRE rule result varchar in database 
	 */
	private String validRuleResult;
	/**
	 * SQL query to get INSPIRE rule result from a metadata uuid
	 */
	private String selectMetadataValidationResultQuery;
	
	/**
	 * Constuctor
	 * @param dataSource Database datasource
	 * @param validRuleResult Valid INSPIRE rule result varchar in database 
	 * @param selectMetadataValidationResultQuery SQL query to get metadata INSPIRE rule result from a metadata uuid
	 */
	public PostgresServiceValidatorClient(DataSource dataSource, String validRuleResult, String selectMetadataValidationResultQuery){
		setDataSource(dataSource);
		this.validRuleResult = validRuleResult;
		this.selectMetadataValidationResultQuery = selectMetadataValidationResultQuery;
	}
	
	/**
	 * Default constructor 
	 */
	public PostgresServiceValidatorClient(){}
	
	/**
	 * validRuleResult getter
	 * @return validRuleResult
	 */
	public String getValidRuleResult() {
		return validRuleResult;
	}
	/**
	 * validRuleResult setter
	 * @param validRuleResult
	 */
	public void setValidRuleResult(String validRuleResult) {
		validRuleResult = validRuleResult != null ? stripAccents(validRuleResult.trim()) : null;
		this.validRuleResult = validRuleResult;
	}
	/**
	 * selectMetadataValidationResultQuery getter
	 * @return
	 */
	public String getSelectMetadataValidationResultQuery() {
		return selectMetadataValidationResultQuery;
	}
	/**
	 * selectMetadataValidationResultQuery setter
	 * @param selectMetadataValidationResultQuery
	 */
	public void setSelectMetadataValidationResultQuery(
			String selectMetadataValidationResultQuery) {
		this.selectMetadataValidationResultQuery = selectMetadataValidationResultQuery;
	}

	/**
	 * Validate a metadata from its uuid
	 * @param metadataUuid metadata uuid
	 * @return metadata validation report
	 * @throws DataAccessException Datasource exception
	 * @throws SQLException SQL exception
	 */
	public ValidationReport validate(String metadataUuid) throws DataAccessException, SQLException {
		
		//validation report
		ValidationReport report = new ValidationReport(100);
		
		//query database to get the metadata INSPIRE rule result
		List<String> results  = getJdbcTemplate().query(selectMetadataValidationResultQuery, new String[]{metadataUuid}, new org.springframework.jdbc.core.RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}
		});  
		//non referenced INSPIRE metadata in database
		if(results.size() == 0){
			String resultStr = "Record with uuid=\"" +metadataUuid +"\" non referenced in validation database";
			report.setStatus(false);
			report.setInfo(resultStr);
			report.setReport(resultStr);
			report.setCompletenessIndicator(0);
		}
		//invalid INSPIRE metadata
		else if(stripAccents(results.get(0).trim()).compareTo(validRuleResult) != 0){
			//results contains list of inspire non conformities
			String resultStr = getResultsStr(results);
			report.setStatus(false);
			report.setInfo(resultStr);
			report.setReport(resultStr);
			report.setCompletenessIndicator(0);
		}
		//valid INSPIRE metadata
		else{
			report.setStatus(true);
			report.setInfo(results.get(0));
			report.setReport(results.get(0));
			report.setCompletenessIndicator(100);
		}
		return report;
	}
	
	/**
	 * Construct a result string from a list of inspire non conformities
	 * @param results
	 * @return
	 */
	private String getResultsStr(List<String> results){
		StringBuilder resultsStr = new StringBuilder();
		if(results != null && results.size() > 0){
			for(String result : results){
				resultsStr.append(result);
				resultsStr.append(" ");
			}
		}
		return resultsStr.toString();
	}
	
	/**
	 * Supprime les accents
	 * @param s
	 * @return
	 */
	public static String stripAccents(String s) {
	    s = Normalizer.normalize(s, Normalizer.Form.NFD);
	    s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
	    return s;
	}
}
