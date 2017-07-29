/**
 * Copyright 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 */
package com.emc.gs.network.validation.tool.exception;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class EndPointApiError is a generic error to capture and to display in UI.
 * @author Debadatta Mishra
 */
@XmlRootElement(name = "Error")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "status","errorCode","errorMessage","details" })
@JsonPropertyOrder({"status","errorCode","errorMessage","details"})
public class EndPointApiError {
 
	/** The status. */
	@XmlElement(name = "status")
	@JsonProperty("status")
    private HttpStatus status;
	
	/** The error message. */
	@XmlElement(name = "errorMessage")
	@JsonProperty("errorMessage")
    private String errorMessage = "";
	
	/** The ex. */
	@XmlTransient
	@JsonIgnore
    private Exception ex;
    
	/** The error code. */
	@XmlElement(name = "errorCode")
    @JsonProperty("errorCode")
    private int errorCode;
    
	/** The more details. */
	@XmlElement(name = "moreDetails")
    @JsonProperty("moreDetails")
    private String moreDetails = null;
    
 
    /**
     * Instantiates a new end point api error.
     *
     * @param status the status
     * @param errorMessage the error message
     * @param ex the ex
     */
    public EndPointApiError(HttpStatus status, String errorMessage, Exception ex) {
        super();
        this.status = status;
        this.errorMessage = errorMessage;
        this.ex = ex;
        this.errorCode = status.value();
        this.moreDetails = ex.getMessage();
    }


	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public HttpStatus getStatus() {
		return status;
	}


	/**
	 * Gets the error message.
	 *
	 * @return the error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}


	/**
	 * Gets the ex.
	 *
	 * @return the ex
	 */
	public Exception getEx() {
		return ex;
	}


	/**
	 * Gets the error code.
	 *
	 * @return the error code
	 */
	public int getErrorCode() {
		return errorCode;
	}


	/**
	 * Gets the more details.
	 *
	 * @return the more details
	 */
	public String getMoreDetails() {
		return moreDetails;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EndPointApiError [status=" + status + ", errorMessage=" + errorMessage + ", errorCode=" + errorCode
				+ ", moreDetails=" + moreDetails + "]";
	}
    
	/**
	 * Converts to JSON.
	 *
	 * @return the string
	 */
	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		String toJson = null;
		try {
			toJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return toJson;
	}

	/**
	 * Converts to XML.
	 *
	 * @return the string
	 */
	public String toXML() {
		String xmlString = "";
		try {
			JAXBContext context = JAXBContext.newInstance(this.getClass());
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); 
			StringWriter sw = new StringWriter();
			m.marshal(this, sw);
			xmlString = sw.toString();

		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return xmlString;
	}
    

    
}