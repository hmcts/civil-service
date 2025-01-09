package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CaseQueriesUtuil {

    private CaseQueriesUtuil() {
        //NO-OP
    }

    public static CaseQueriesCollection createCaseQueries(String partyName, String roleOnCase) {
        List<Element<CaseMessage>> caseMessageCollection = new ArrayList<>();
        return CaseQueriesCollection.builder()
            .build();
    }

    public static CaseMessage createCaseMessage() {
        return CaseMessage.builder()
                .id(UUID.randomUUID().toString())
                .createdOn(LocalDateTime.now())
                .createdBy("120b3665-0b8a-4e80-ace0-01d8d63c1005")
                .isHearingRelated(YesOrNo.NO)
                .name("Piran Sam")
                .body("testing by olu")
                .subject("Review attached document")
                .build();

    }

}
