package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;

import java.util.List;

public class CaseQueriesUtuil {


    private CaseQueriesUtuil() {
        //NO-OP
    }

    public static CaseQueriesCollection createCaseQueries(String flagsPartyName, String roleOnCase) {
        return CaseQueriesCollection.builder()
            .partyName(flagsPartyName)
            .roleOnCase(roleOnCase)
            .caseMessageCollection(List.of())
            .build();
    }

}
