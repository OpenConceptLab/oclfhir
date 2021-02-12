package org.openconceptlab.fhir.controller;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.coyote.Response;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.openconceptlab.fhir.util.OclFhirUtil.badRequestRawMsg;

@ControllerAdvice
public class FhirExceptionHandler {

    private static final Log log = LogFactory.getLog(FhirExceptionHandler.class);

    @ExceptionHandler(value = {BaseServerResponseException.class})
    public ResponseEntity<Object> handleBaseServerResponseException(BaseServerResponseException bsre) {
        log.error("BaseServerResponseException - " + bsre.getMessage());
        return ResponseEntity.status(bsre.getStatusCode()).body(bsre.getResponseBody());
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Exception - " + e.getMessage());
        return badRequestRawMsg(e.getMessage());
    }

}
