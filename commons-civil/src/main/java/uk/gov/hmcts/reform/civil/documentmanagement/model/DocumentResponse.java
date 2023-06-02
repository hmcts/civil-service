package uk.gov.hmcts.reform.civil.documentmanagement.model;


import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

public record DocumentResponse(Resource file, HttpHeaders headers) {

}
