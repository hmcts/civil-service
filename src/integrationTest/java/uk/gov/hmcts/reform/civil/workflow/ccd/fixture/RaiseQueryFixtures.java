package uk.gov.hmcts.reform.civil.workflow.ccd.fixture;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

public final class RaiseQueryFixtures {

    private static final String TEMPLATE = "query-management-start";

    private RaiseQueryFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(TEMPLATE);
    }

    public static CaseData caseDataInClosedState() {
        return CaseDataTemplates.load(TEMPLATE, template ->
            CaseDataTemplates.set(template, "ccdState", CaseState.CLOSED.name())
        );
    }

    public static CaseData caseDataLip() {
        return CaseDataTemplates.load(TEMPLATE, template -> {
            CaseDataTemplates.set(template, "respondent1Represented", YesOrNo.NO);
            CaseDataTemplates.set(template, "applicant1Represented", YesOrNo.YES);
        });
    }

    public static CaseData caseDataWithNewQuery(boolean isHearingRelated) {
        CaseData base = caseData();
        CaseMessage message = queryMessage("test-user-id", "Test query subject",
                                           "What is the status?", isHearingRelated);
        CaseQueriesCollection queries = new CaseQueriesCollection();
        queries.setPartyName("All queries");
        queries.setCaseMessages(new ArrayList<>(List.of(element(message))));
        base.setQueries(queries);
        return base;
    }

    public static CaseData caseDataWithQueryAndResponse() {
        
        String rootId = UUID.randomUUID().toString();

        CaseMessage rootQuery = queryMessage("party-user-id", "Query subject",
                                             "Original question", false);
        rootQuery.setId(rootId);
        rootQuery.setCreatedOn(OffsetDateTime.now().minusHours(2));

        CaseMessage response = queryMessage("caseworker-id", "Query subject",
                                            "Caseworker response", false);
        response.setParentId(rootId);
        response.setCreatedOn(OffsetDateTime.now().minusHours(1));

        CaseQueriesCollection queries = new CaseQueriesCollection();
        queries.setPartyName("All queries");
        queries.setCaseMessages(new ArrayList<>(List.of(element(rootQuery), element(response))));
        CaseData base = caseData();
        base.setQueries(queries);
        return base;
    }

    public static CaseData caseDataWithLegacyCollections() {
        CaseData base = caseData();

        CaseMessage oldMessage = queryMessage("old-user", "Legacy query",
                                              "Old question", false);

        CaseQueriesCollection legacyCollection = new CaseQueriesCollection();
        legacyCollection.setPartyName("Claimant");
        legacyCollection.setCaseMessages(new ArrayList<>(List.of(element(oldMessage))));

        base.setQmApplicantSolicitorQueries(legacyCollection);
        return base;
    }

    public static CaseMessage queryMessage(String createdBy, String subject,
                                           String body, boolean hearingRelated) {
        CaseMessage message = new CaseMessage();
        message.setId(UUID.randomUUID().toString());
        message.setSubject(subject);
        message.setName("Test User");
        message.setBody(body);
        message.setCreatedBy(createdBy);
        message.setCreatedOn(OffsetDateTime.now());
        message.setIsHearingRelated(hearingRelated ? YesOrNo.YES : YesOrNo.NO);

        Document doc = new Document();
        doc.setDocumentUrl("http://dm-store/documents/123");
        doc.setDocumentBinaryUrl("http://dm-store/documents/123/binary");
        doc.setDocumentFileName("test-attachment.pdf");
        message.setAttachments(List.of(element(doc)));

        return message;
    }
}
