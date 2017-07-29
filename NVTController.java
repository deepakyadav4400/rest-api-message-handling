/**
 * Copyright 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 */
package com.emc.gs.network.validation.tool.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.emc.gs.network.validation.tool.constants.ApplicationConstants;
import com.emc.gs.network.validation.tool.dto.CLIOperationOutcome;
import com.emc.gs.network.validation.tool.dto.HostnameResponse;
import com.emc.gs.network.validation.tool.dto.JsonImportPath;
import com.emc.gs.network.validation.tool.dto.Location;
import com.emc.gs.network.validation.tool.dto.NetworkConfiguration;
import com.emc.gs.network.validation.tool.dto.VxRailProductConfiguration;
import com.emc.gs.network.validation.tool.exception.BaseResourcesExceptionHandler;
import com.emc.gs.network.validation.tool.exception.MapperException;
import com.emc.gs.network.validation.tool.services.NetworkServicesImpl;
import com.emc.gs.network.validation.tool.services.ReportService;
import com.emc.gs.network.validation.tool.utils.CommonUtil;
import com.emc.gs.network.validation.tool.utils.TimeZoneUtil;

/**
 * NVTController class for network services configuration
 */
@RestController
@RequestMapping("/nvt")
@ComponentScan(basePackages = "com.emc.gs.*")
public class NVTController extends BaseResourcesExceptionHandler {

	@Autowired
	VxRailProductConfiguration instance;

	@Autowired
	NetworkConfiguration networkConf;

	@Autowired
	NetworkServicesImpl networkServicesImpl;

	@Autowired
	ReportService reportSrv;

	
		/**
	 * Response includes list of CLIOperationOutcome
	 * 
	 * @param path
	 *            path of JSON file with prechecklist data.
	 * @return ResponseEntity with list of CLIOperationOutcome
	 * @throws IOException
	 */
	@RequestMapping(value = "/jsonfile", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> generateJsonFile(@RequestBody JsonImportPath jsonPath) throws IOException {

		NetworkServicesImpl impl = new NetworkServicesImpl();
		ResponseEntity<List<CLIOperationOutcome>> entity = null;

		try {

			entity = impl.importJsonDirectly(jsonPath);

		} catch (Exception e) {
			LOGGER.error("Exception thrown and handled ...\n", e);

			return handleException(ApplicationConstants.REST_API_CALL_INVALID_JSON_FORMAT, e);
		}

		return entity;

	}
	
	/**
	 * Response generates the version number.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(NVTController.class);

	public static final String ALLDOMAINS = "*";
	public static final String CORS = "Access-Control-Allow-Origin";
	public static final String VALIDATIONSTARTED_MSG = "Validate Network Configuration for VxRail...";
	public static final String VALIDATIONCOMPLETED_MSG = "Network Configuration validation for VxRail is complete";

	/**
	 * Response includes EXSi Host Names in a VxRail cluster.
	 * 
	 * @param prefix
	 *            prefix for EXSI host name
	 * @param separator
	 *            Separator
	 * @param it
	 *            iterator
	 * @param tld
	 *            top of domain
	 * @return ResponseEntity with list of EXSI hostnames
	 * @throws MapperException
	 * 
	 */
	@RequestMapping(value = "/management/Hostnames", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<HostnameResponse>> getEXSIHostNames(@RequestParam(value = "prefix") String prefix,
			@RequestParam(value = "separator") String sep, @RequestParam(value = "iterator") String it,
			@RequestParam(value = "offset", required = false) String offset,
			@RequestParam(value = "postfix", required = false) String postfix, @RequestParam(value = "tld") String tld,
			@RequestParam(value = "numberOfAppliance") int numberOfAppliance,
			@RequestParam(value = "numberOfNodes") int numberOfNodes) {

		NetworkServicesImpl networkServicesImpl = new NetworkServicesImpl();

		return networkServicesImpl.preview(prefix, sep, it, offset, postfix, tld, numberOfAppliance, numberOfNodes);
	}

	/**
	 * Response includes ESXi hostnames in a VxRail cluster.
	 * 
	 * @param networkconf
	 *            network configuration
	 * @return ResponseEntity with list of CLIOperationoutcomes
	 */
	@RequestMapping(value = "/management/validate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> validateNetworkConfiguration(@RequestBody String jsonBodyRequest) {

		LOGGER.info(VALIDATIONSTARTED_MSG);

		NetworkConfiguration networkconf = null;
		try {
			networkconf = CommonUtil.fromJsonString2Object(jsonBodyRequest, NetworkConfiguration.class);
		} catch (IOException e) {

			return handleException(ApplicationConstants.REST_API_CALL_INVALID_JSON_FORMAT,
					new IllegalArgumentException(e));
		}

		HttpHeaders headers = new HttpHeaders();

		headers.add(CORS, ALLDOMAINS);

		List<CLIOperationOutcome> outcomes = new ArrayList<>();

		ResponseEntity<List<CLIOperationOutcome>> entity = null;

		try {
			outcomes = networkServicesImpl.validateNetworkConfiguration(networkconf);

			entity = new ResponseEntity<List<CLIOperationOutcome>>(outcomes, headers, HttpStatus.OK);

		} catch (FileNotFoundException e) {
			LOGGER.debug("File Not Found Exception at spedified path", e);
			entity = new ResponseEntity<List<CLIOperationOutcome>>(outcomes, headers, HttpStatus.BAD_REQUEST);
		}

		return entity;

	}

	/**
	 * getNetworkConfigurationTemplate method - Get Network configuration
	 * template
	 * 
	 * @param no
	 *            parameters
	 * 
	 * @return networkConf networkConfiguration
	 */
	@RequestMapping(value = "/template", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody NetworkConfiguration getNetworkConfigurationTemplate() {
		return networkConf;
	}

	/**
	 * Response includes Path of Generated JSON
	 * 
	 * @param networkConf
	 *            network configuration
	 * @return ResponseBody with Location dto
	 */
	@RequestMapping(value = "/report", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Location generateReport(@RequestBody NetworkConfiguration networkConf) {

		Location location = null;
		try {
			location = reportSrv.generateJSONReport(networkConf);
		} catch (MapperException e) {
			LOGGER.debug("Report", e);
		}
		return location;
	}



	/**
	 * Response includes list of time zones
	 * 
	 * @return ResponseEntity with list time zones
	 * 
	 */
	@RequestMapping(value = "/timezone", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getTimeZone() {

		ResponseEntity<List<String>> responseEntity = null;

		TimeZoneUtil timezoneutil = new TimeZoneUtil();

		responseEntity = new ResponseEntity<>(timezoneutil.displayTimeZoneService(), HttpStatus.OK);

		return responseEntity;
	}

}
