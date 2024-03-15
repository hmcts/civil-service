package uk.gov.hmcts.reform.civil.controllers;

import feign.Request;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;

public class FeignRequestUtils {

    /**
     * Convert Feign {@link Request} headers collection to Spring {@link HttpHeaders} object
     *
     * @param request Feign request to convert
     * @return Converted HTTP headers (as a Spring object)
     */
    public static HttpHeaders convertRequestHeaders(final Request request) {
        final HttpHeaders springHeaders = new HttpHeaders();
        request.headers().forEach((header, vals) -> springHeaders.put(header, new ArrayList<String>(vals)));
        return springHeaders;
    }

    /**
     * Convert Feign {@link Request} request method to Spring {@link HttpMethod} object
     *
     * @param request Feign request to convert
     * @return Converted HTTP Method (as a Spring object)
     */
    public static HttpMethod convertRequestMethod(final Request request) {
        return HttpMethod.valueOf(request.method());
    }
}
