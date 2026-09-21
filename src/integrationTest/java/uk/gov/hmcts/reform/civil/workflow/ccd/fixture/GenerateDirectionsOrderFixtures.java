package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

public final class GenerateDirectionsOrderFixtures {

    private GenerateDirectionsOrderFixtures() {
    }

    public static CaseData intermediateFromJudicialReferral() {
        DynamicListElement templateOption = DynamicListElement.dynamicElementFromCode(
            "FIX_DATE_CMC", "Fix a date for CMC");
        CaseData caseData = CaseDataTemplates.load("judicial-referral").toBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .caseAccessCategory(UNSPEC_CLAIM)
            .allocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM)
            .build();
        caseData.setUploadOrderDocumentFromTemplate(orderDocumentLink());
        caseData.setFinalOrderDownloadTemplateOptions(new DynamicList()
                                                          .setValue(templateOption)
                                                          .setListItems(List.of(templateOption)));
        return caseData;
    }

    public static CaseData freeFormFromCaseProgression() {
        return CaseDataTemplates.load("case-progression").toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .caseAccessCategory(UNSPEC_CLAIM)
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .finalOrderDocument(orderCaseDocument())
            .build();
    }

    private static Document orderDocumentLink() {
        return new Document(
            "http://dm-store/documents/final-order-doc",
            "http://dm-store/documents/final-order-doc/binary",
            "final-order.pdf",
            null, null, null
        );
    }

    private static CaseDocument orderCaseDocument() {
        return new CaseDocument()
            .setDocumentType(DocumentType.JUDGE_FINAL_ORDER)
            .setCreatedDatetime(LocalDateTime.now())
            .setDocumentLink(orderDocumentLink());
    }
}
