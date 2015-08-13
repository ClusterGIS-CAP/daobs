package org.daobs.tasks.validation.inspire;

import java.sql.SQLException;

import org.apache.camel.Exchange;
import org.springframework.dao.DataAccessException;

/**
 * 
 * @author csachot
 *
 */
public class PostgresServiceValidatorBean {
	
	/**
	 * PostgresServiceValidatorClient that queries the database to get the metadata's validation report
	 */
	private PostgresServiceValidatorClient postgresServiceValidatorClient;
	
	/**
	 * Metatadata id key in the exchange header
	 */
	private static final String METADATA_ID_KEY = "documentIdentifier";
	
	/**
	 * Constructor 
	 * @param postgresServiceValidatorClient PostgresServiceValidatorClient that queries the database to get the metadata's validation report
	 */
	public PostgresServiceValidatorBean(PostgresServiceValidatorClient postgresServiceValidatorClient){
		this.postgresServiceValidatorClient = postgresServiceValidatorClient;
	}

	/**
     * Get the input message body and validate
     * it against the INSPIRE validation service.
     * The output body contains the validation report.
     *
     * Headers are propagated.
     *
     * @param exchange
     */
    public void validateBody(Exchange exchange) {
    	
    	String metadataId = (String) exchange.getIn().getHeader(METADATA_ID_KEY);
    	
    	ValidationReport report = null;
    	try {
			report = postgresServiceValidatorClient.validate(metadataId);
		} catch (DataAccessException | SQLException e) {
			e.printStackTrace();
		}
    	
    	exchange.getOut().setBody(report);
    	exchange.getOut().setHeaders(exchange.getIn().getHeaders());
    }
}
