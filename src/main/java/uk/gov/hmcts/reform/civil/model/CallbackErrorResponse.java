package uk.gov.hmcts.reform.civil.model;

import java.util.List;

import lombok.Data;

@Data
public class CallbackErrorResponse {

    private String exception;
    private Integer status;
    private String error;
    private String message;
    private Object details;
    private List<String> callbackErrors;
    private List<String> callbackWarnings;
}
