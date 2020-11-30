package uk.gov.hmcts.reform.unspec.model.dq;

import uk.gov.hmcts.reform.unspec.model.StatementOfTruth;
import uk.gov.hmcts.reform.unspec.model.documents.Document;

public interface DQ {

    FileDirectionsQuestionnaire getFileDirectionQuestionnaire();

    DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments();

    DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments();

    DisclosureReport getDisclosureReport();

    Experts getExperts();

    Witnesses getWitnesses();

    Hearing getHearing();

    Document getDraftDirections();

    RequestedCourt getRequestedCourt();

    HearingSupport getHearingSupport();

    FurtherInformation getFurtherInformation();

    WelshLanguageRequirements getWelshLanguageRequirements();

    StatementOfTruth getStatementOfTruth();
}
