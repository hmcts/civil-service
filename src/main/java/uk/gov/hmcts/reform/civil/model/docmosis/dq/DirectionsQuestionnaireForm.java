package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureReport;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.FurtherInformation;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.VulnerabilityQuestions;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DirectionsQuestionnaireForm implements MappableObject {

    @JsonProperty("courtseal")
    private final String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR
    private final String caseName;
    private final String referenceNumber;
    private final SolicitorReferences solicitorReferences;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate submittedOn;
    private final Party applicant;
    private final Party applicant2;
    private final List<Party> respondents;
    private final List<Party> applicants;
    private final FileDirectionsQuestionnaire fileDirectionsQuestionnaire;
    private final FixedRecoverableCostsSection fixedRecoverableCosts;
    private final DisclosureOfElectronicDocuments disclosureOfElectronicDocuments;
    private final DisclosureOfNonElectronicDocuments disclosureOfNonElectronicDocuments;
    private final Experts experts;
    private final Witnesses witnesses;
    private final Integer witnessesIncludingDefendants;
    private final Hearing hearing;
    private final String hearingSupport;
    private final HearingSupport support;
    private final FurtherInformation furtherInformation;
    private final WelshLanguageRequirements welshLanguageRequirements;
    private final StatementOfTruth statementOfTruth;
    private final String allocatedTrack;
    private final DisclosureReport disclosureReport;
    private final RequestedCourt requestedCourt;
    private final VulnerabilityQuestions vulnerabilityQuestions;
    private final String statementOfTruthText;
    private final Address respondent1LiPCorrespondenceAddress;
    private final Address applicant1LiPCorrespondenceAddress;
    private final LipExperts lipExperts;
    private final LipExtraDQ lipExtraDQ;
    private final List<HearingLipSupportRequirements> hearingLipSupportRequirements;
    private final String lipStatementOfTruthName;
    private final DocumentsToBeConsideredSection documentsToBeConsidered;

}
