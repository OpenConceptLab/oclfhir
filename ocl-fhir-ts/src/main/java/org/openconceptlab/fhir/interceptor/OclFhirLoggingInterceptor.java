package org.openconceptlab.fhir.interceptor;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * The OclFhirLoggingInterceptor class.
 * @author harpatel1
 */
@Component
public class OclFhirLoggingInterceptor {
    private static final Logger ourLog = LoggerFactory.getLogger(OclFhirLoggingInterceptor.class);

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
    public boolean logRequestDetails(RequestDetails theRequest) {
        ourLog.info("{} Handling {} client operation for url {}", new Date(), theRequest.getRequestType(), theRequest.getCompleteUrl());
        return true;
    }
}
