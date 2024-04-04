package uk.gov.hmcts.reform.civil.sampledata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;

@SuppressWarnings("unchecked")
public class CaseDetailsBuilder {

    private final ObjectMapper mapper = new ObjectMapper()
        .registerModules(new Jdk8Module(), new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private String state;
    private Map<String, Object> data;
    private Long id;

    public static CaseDetailsBuilder builder() {
        return new CaseDetailsBuilder();
    }

    public CaseDetailsBuilder state(CaseState state) {
        this.state = state.name();
        return this;
    }

    public CaseDetailsBuilder data(CaseData caseData) {
        this.data = mapper.convertValue(caseData, Map.class);
        return this;
    }

    public CaseDetailsBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public CaseDetailsBuilder atStatePendingClaimIssued() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = PENDING_CASE_ISSUED.name();
        return this;
    }

    public CaseDetailsBuilder atStateCaseIssued() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = CASE_ISSUED.name();
        return this;
    }

    public CaseDetailsBuilder atStateAwaitingCaseDetailsNotification() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = AWAITING_CASE_DETAILS_NOTIFICATION.name();
        return this;
    }

    public CaseDetailsBuilder atStateAwaitingRespondentAcknowledgement() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name();
        return this;
    }

    public CaseDetailsBuilder atStateAwaitingRespondentAcknowledgement1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified1v1().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name();
        return this;
    }

    public CaseDetailsBuilder atStateClaimAcknowledge() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name();
        return this;
    }

    public CaseDetailsBuilder atStateExtensionRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name();
        return this;
    }

    public CaseDetailsBuilder atStateRespondedToClaim() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = AWAITING_APPLICANT_INTENTION.name();
        return this;
    }

    public CaseDetailsBuilder atStateFullDefence() {
        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = AWAITING_APPLICANT_INTENTION.name();
        return this;
    }

    public CaseDetailsBuilder atStateFullDefenceSpec() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .ccdState(AWAITING_APPLICANT_INTENTION)
            .build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = AWAITING_APPLICANT_INTENTION.name();
        return this;
    }

    public CaseDetailsBuilder atStateFullAdmitSpec() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .ccdState(AWAITING_APPLICANT_INTENTION)
            .build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = AWAITING_APPLICANT_INTENTION.name();
        return this;
    }

    public CaseDetailsBuilder atStatePartAdmitSpec() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .ccdState(AWAITING_APPLICANT_INTENTION)
            .build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = AWAITING_APPLICANT_INTENTION.name();
        return this;
    }

    public CaseDetailsBuilder atStateDecisionOutcome() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build().toBuilder()
            .hearingDate(LocalDate.now())
            .ccdState(PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = PREPARE_FOR_HEARING_CONDUCT_HEARING.name();
        return this;
    }

    public CaseDetailsBuilder atStateProceedsOffline() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedUnrepresentedDefendants().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = PROCEEDS_IN_HERITAGE_SYSTEM.name();
        return this;
    }

    public CaseDetailsBuilder atStateProceedsOffline1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1UnrepresentedDefendant().build();
        this.data = mapper.convertValue(caseData, Map.class);
        this.state = PROCEEDS_IN_HERITAGE_SYSTEM.name();
        return this;
    }

    public CaseDetails build() {
        return CaseDetails.builder()
            .data(data)
            .state(state)
            .id(id)
            .build();
    }
}
