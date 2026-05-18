package uk.gov.hmcts.reform.civil.model;

import java.util.List;

import lombok.Data;

@Data
public class CallbackErrorResponse {

    private List<String> callbackErrors;
    private List<String> callbackWarnings;
}
