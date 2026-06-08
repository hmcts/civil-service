package uk.gov.hmcts.reform.civil.model;

import java.util.List;

import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

@Data
public class CallbackErrorResponse {

    private String exception;
    private Integer status;
    private String error;
    private String message;
    private T details;
    private List<String> callbackErrors;
    private List<String> callbackWarnings;
}
