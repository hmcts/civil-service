package uk.gov.hmcts.reform.civil.config;

import feign.Request;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;

public class FeignRequestUtils {

    private FeignRequestUtils() {
        //Utility class
    }

    public static HttpHeaders convertRequestHeaders(final Request request) {
        final HttpHeaders springHeaders = new HttpHeaders();
        request.headers().forEach((header, vals) -> springHeaders.put(header, new ArrayList<String>(vals)));
        return springHeaders;
    }

    public static HttpMethod convertRequestMethod(final Request request) {
        return HttpMethod.valueOf(request.method());
    }
}
