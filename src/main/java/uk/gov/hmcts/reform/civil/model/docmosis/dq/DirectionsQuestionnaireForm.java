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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
    @Builder.Default
    private String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR
    private String caseName;
    private String referenceNumber;
    private SolicitorReferences solicitorReferences;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate submittedOn;
    private Party applicant;
    private Party applicant2;
    private List<Party> respondents;
    private List<Party> applicants;
    private FileDirectionsQuestionnaire fileDirectionsQuestionnaire;
    private FixedRecoverableCostsSection fixedRecoverableCosts;
    private DisclosureOfElectronicDocuments disclosureOfElectronicDocuments;
    private DisclosureOfNonElectronicDocuments disclosureOfNonElectronicDocuments;
    private YesOrNo deterWithoutHearingYesNo;
    private String deterWithoutHearingWhyNot;
    private Experts experts;
    private Witnesses witnesses;
    private Integer witnessesIncludingDefendants;
    private Hearing hearing;
    private String hearingSupport;
    private HearingSupport support;
    private FurtherInformation furtherInformation;
    private WelshLanguageRequirements welshLanguageRequirements;
    private StatementOfTruth statementOfTruth;
    private String allocatedTrack;
    private DisclosureReport disclosureReport;
    private RequestedCourt requestedCourt;
    private VulnerabilityQuestions vulnerabilityQuestions;
    private String statementOfTruthText;
    private Address respondent1LiPCorrespondenceAddress;
    private Address applicant1LiPCorrespondenceAddress;
    private LipExperts lipExperts;
    private LipExtraDQ lipExtraDQ;
    private List<HearingLipSupportRequirements> hearingLipSupportRequirements;
    private String lipStatementOfTruthName;
    private DocumentsToBeConsideredSection documentsToBeConsidered;
    private String representativeOrganisationName;
}
