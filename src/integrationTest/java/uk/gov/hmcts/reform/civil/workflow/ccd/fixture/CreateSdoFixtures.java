package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

public final class CreateSdoFixtures {

    private static final String TEMPLATE = "judicial-referral";

    private CreateSdoFixtures() {
    }

    public static CaseData unspecFastTrack() {
        return CaseDataTemplates.load(TEMPLATE, template -> {
            CaseDataTemplates.set(template, "drawDirectionsOrderRequired", "Yes");
            CaseDataTemplates.set(template, "drawDirectionsOrderSmallClaims", "No");
            CaseDataTemplates.set(template, "orderType", "DECIDE_DAMAGES");
        }).toBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .caseAccessCategory(UNSPEC_CLAIM)
            .sdoOrderDocument(sdoDocument())
            .build();
    }

    public static CaseData unspecSmallClaims() {
        return CaseDataTemplates.load(TEMPLATE, template -> {
            CaseDataTemplates.set(template, "drawDirectionsOrderRequired", "Yes");
            CaseDataTemplates.set(template, "drawDirectionsOrderSmallClaims", "Yes");
        }).toBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .caseAccessCategory(UNSPEC_CLAIM)
            .sdoOrderDocument(sdoDocument())
            .build();
    }

    public static CaseData specSmallClaims() {
        return CaseDataTemplates.load(TEMPLATE, template -> {
            CaseDataTemplates.set(template, "drawDirectionsOrderRequired", "Yes");
            CaseDataTemplates.set(template, "drawDirectionsOrderSmallClaims", "Yes");
        }).toBuilder()
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .caseAccessCategory(SPEC_CLAIM)
            .sdoOrderDocument(sdoDocument())
            .build();
    }

    private static CaseDocument sdoDocument() {
        return new CaseDocument()
            .setDocumentType(DocumentType.SDO_ORDER)
            .setCreatedDatetime(LocalDateTime.now())
            .setDocumentLink(new Document(
                "http://dm-store/documents/sdo-doc",
                "http://dm-store/documents/sdo-doc/binary",
                "sdo-order.pdf",
                null, null, null
            ));
    }
}
