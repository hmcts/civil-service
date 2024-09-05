package uk.gov.hmcts.reform.civil.controllers.testingsupport.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Data
@Builder
@Jacksonized
public class TestCamundaProcess {

    private String name;
    private Map<String, Object> variables;
}
