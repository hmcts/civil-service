package uk.gov.hmcts.reform.civil.request;

import java.util.Set;

public interface RequestData {

    String authorisation();

    String userId();

    Set<String> userRoles();

    String caseId();
}
