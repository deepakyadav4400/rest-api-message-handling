/**
 * Copyright 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 */
package com.emc.gs.network.validation.tool.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * The Class BaseResourcesExceptionHandler.
 * @author Debadatta Mishra
 */
public abstract class BaseResourcesExceptionHandler {

	/** The exception map. */
	protected static Map<Class<?>, HttpStatus> exceptionMap = new HashMap<>();
	static {
		exceptionMap.put(SecurityException.class, HttpStatus.FORBIDDEN);
		exceptionMap.put(AccessDeniedException.class, HttpStatus.FORBIDDEN);
		exceptionMap.put(IllegalArgumentException.class, HttpStatus.BAD_REQUEST);
		exceptionMap.put(Exception.class, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Handle exception and return {@link ResponseEntity} by passing @link {@link EndPointApiError}.
	 *
	 * @param apiError the api error
	 * @return the response entity
	 */
	protected ResponseEntity<EndPointApiError> handleException(EndPointApiError apiError) {
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

	/**
	 * Handle exception and return {@link ResponseEntity} by passing @link {@link EndPointApiError}.
	 *
	 * @param erroMessage the erro message
	 * @param incomingException the incoming exception
	 * @return the response entity
	 */
	protected ResponseEntity<EndPointApiError> handleException(String erroMessage, Exception incomingException) {
		EndPointApiError apiError = new EndPointApiError(getStatus(incomingException), erroMessage, incomingException);
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

	/**
	 * Handle exception and return {@link ResponseEntity} by passing @link {@link EndPointApiError}.
	 *
	 * @param incomingException the incoming exception
	 * @return the response entity
	 */
	protected ResponseEntity<EndPointApiError> handleException(Exception incomingException) {
		EndPointApiError apiError = getError(incomingException);
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

	/**
	 * Gets the error by passing {@link Exception}.
	 *
	 * @param incomingException the incoming exception
	 * @return the error
	 */
	protected EndPointApiError getError(Exception incomingException) {
		return new EndPointApiError(getStatus(incomingException), getPreciseMessage(incomingException, 1),
				incomingException);
	}

	/**
	 * Gets the status by passing {@link Exception}..
	 *
	 * @param incomingException the incoming exception
	 * @return the status
	 */
	protected HttpStatus getStatus(Exception incomingException) {
		HttpStatus status = exceptionMap.get(incomingException.getClass());
		return (status == null) ? HttpStatus.INTERNAL_SERVER_ERROR : status;
	}

	/**
	 * Gets the precise message of the whole exception stackTrace.
	 *
	 * @param e the e
	 * @param maxLines the max lines
	 * @return the precise message
	 */
	public static String getPreciseMessage(Exception e, int maxLines) {
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		String[] lines = writer.toString().split("\n");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
			sb.append(lines[i]).append("\n");
		}
		return sb.toString();
	}

}
