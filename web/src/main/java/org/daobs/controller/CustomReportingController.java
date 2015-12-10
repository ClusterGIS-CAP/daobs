package org.daobs.controller;

import net.sf.saxon.FeatureKeys;
import org.daobs.index.SolrRequestBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Node;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by francois on 18/12/14.
 */
@Controller
public class CustomReportingController {

    // TODO: config should move to configuration file
    public static final String SPATIALDATASETS_QUERY_URL = "/data/select?" +
            "q=%%2BdocumentType%%3Ametadata+" +
                "%%2B(resourceType%%3Adataset+" +
                "resourceType%%3Aseries)&" +
            "fq=%s&" +
            "start=0&rows=%d&" +
            "fl=metadataIdentifier,resourceTitle,isAboveThreshold," +
            "inspireAnnex,inspireTheme,inspireConformResource," +
            "recordOperatedByType,recordOperatedByTypeview,recordOperatedByTypedownload," +
            "OrgForResource,custodianOrgForResource,ownerOrgForResource,pointOfContactOrgForResource,harvesterUuid";

    public static final String SPATIALDATASERVICE_QUERY_URL = "/data/select?" +
            "q=%%2BdocumentType%%3Ametadata+%%2BresourceType%%3Aservice&" +
            "fq=%s&" +
            "start=0&rows=%d&" +
            "fl=metadataIdentifier,resourceTitle,isAboveThreshold," +
            "inspireAnnex,inspireTheme,inspireConformResource," +
            "serviceType,linkUrl," +
            "OrgForResource,custodianOrgForResource,ownerOrgForResource,pointOfContactOrgForResource,harvesterUuid";

    public static final List<String> BOOLEAN_PARAMETERS = new ArrayList<>(
            Arrays.asList("withRowData")
    );


    /**
     * Generate report
     * TODO: provide custom XSLT as parameter
     *
     * @param request
     * @param reporting
     * @return
     * @throws java.io.IOException
     */
    @RequestMapping(value = "/reporting/custom/{reporting}",
            produces = {
                    MediaType.APPLICATION_XML_VALUE
            },
            method = RequestMethod.GET)
    public ModelAndView generateMonitoring(HttpServletRequest request,
                                           @RequestParam Map<String,String> allRequestParams,
                                           @PathVariable(value = "reporting") String reporting,
                                           @RequestParam(
                                                   value = "withRowData",
                                                   required = false) Boolean withRowData,
                                           @RequestParam(
                                                   value = "fq",
                                                   defaultValue = "",
                                                   required = false) String fq,
                                           @RequestParam(
                                                   value = "rows",
                                                   defaultValue = "70000",
                                                   required = false) int rows)
            throws IOException {
        IndicatorCalculatorImpl indicatorCalculator =
                ReportingController.generateReporting(request, reporting, fq, true);

        ModelAndView model = new ModelAndView("reporting-xslt-" + reporting);
        model.addObject("xmlSource", indicatorCalculator.toSource());

        addRequestParametersToModel(allRequestParams, model);

        addRowDataToModel(withRowData, rows, fq, model);

        return model;
    }



    /**
     * Generate a specific report for a specific area in
     * INSPIRE monitoring reporting format
     *
     * @param request
     * @param reporting
     * @param territory
     * @param withRowData Include the rowData section in the report.
     * @param rows Number of rows to return for spatial data sets and services
     *             Default value is 10000. When the number of records to return
     *             is too high, error may occurs. Only applies if withRowData
     *             is true.
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/reporting/custom/{reporting}/{territory}",
            produces = {
                    MediaType.APPLICATION_XML_VALUE
            },
            method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView generateMonitoring(HttpServletRequest request,
                                           @RequestParam Map<String,String> allRequestParams,
                                           @RequestParam(
                                                   value = "withRowData",
                                                   required = false) Boolean withRowData,
                                           @RequestParam(
                                                   value = "fq",
                                                   defaultValue = "",
                                                   required = false) String fq,
                                           @RequestParam(
                                                   value = "rows",
                                                   defaultValue = "70000",
                                                   required = false) int rows,
                                           @PathVariable(value = "reporting") String reporting,
                                           @PathVariable(value = "territory") String territory)
            throws IOException {
        String filter = fq + " +territory:" + territory;
        IndicatorCalculatorImpl indicatorCalculator =
                ReportingController.generateReporting(request, reporting, filter, true);

//        TransformerFactory.setFeature(FeatureKeys.LINE_NUMBERING, true);

        ModelAndView model = new ModelAndView("reporting-xslt-" + reporting);
        model.addObject("xmlSource", indicatorCalculator.toSource());
        // Add path parameters
        if (territory != null) {
            model.addObject("territory", territory);
        }
        if (filter != null) {
            model.addObject("filter", filter);
        }

        addRequestParametersToModel(allRequestParams, model);

        // TODO: URL encoding should be done when the HTTP request is made
        addRowDataToModel(withRowData, rows, URLEncoder.encode(filter, "UTF-8"), model);

        return model;
    }

    private void addRowDataToModel(Boolean withRowData, int rows, String fq, ModelAndView model) {
        // Handle defaults for boolean
        if (withRowData == null) {
            withRowData = false;
        }
        model.addObject("withRowData", withRowData);

        // Grab data sets and services to later
        // build the raw data section
        if (withRowData) {
            Node spatialDataSets = SolrRequestBean.query(
                    String.format(
                            SPATIALDATASETS_QUERY_URL,
                            fq, rows));
            model.addObject("spatialDataSets", spatialDataSets);


            Node spatialDataServices = SolrRequestBean.query(
                    String.format(
                            SPATIALDATASERVICE_QUERY_URL,
                            fq, rows));
            model.addObject("spatialDataServices", spatialDataServices);
        }
    }

    private void addRequestParametersToModel(Map<String, String> allRequestParams, ModelAndView model) {
        // Add all request parameters to the model
        // in order to have them as XSL parameters in the view.
        Iterator iterator = allRequestParams.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> parameter = (Map.Entry<String, String>) iterator.next();
            String parameterName = parameter.getKey();
            Object parameterValue = (String)parameter.getValue();
            if (BOOLEAN_PARAMETERS.contains(parameterName)) {
                parameterValue = Boolean.parseBoolean((String)parameterValue);
            }
            model.addObject(parameterName, parameterValue);
        }
    }

}
