package uk.gov.hmcts.reform.civil.controllers.testingsupport.model;

import lombok.Data;

import java.util.Map;

@Data
public class TestCamundaProcess {

    private String name;
    private Map<String, Object> variables;
}
