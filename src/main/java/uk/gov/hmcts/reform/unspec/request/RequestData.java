package uk.gov.hmcts.reform.unspec.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
//NOTE: shouldn't be used for async methods as spring cleans up the request when controller method is finished
public class RequestData {

    private final HttpServletRequest httpServletRequest;

    @Autowired
    public RequestData(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public String authorisation() {
        return httpServletRequest.getHeader("authorization");
    }

    public String userId() {
        return httpServletRequest.getHeader("user-id");
    }
}
