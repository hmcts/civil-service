package uk.gov.hmcts.reform.civil.service.robotics.builders;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.CONSENT_EXTENSION_FILING_DEFENCE;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.STATES_PAID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.isStayClaim;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondentSpec;

public abstract class BaseEventBuilder implements EventBuilder {

    @Autowired
    protected Time time;

    protected BaseEventBuilder(Time time) {
        this.time = time;
    }

    protected BaseEventBuilder() {
    }

    public String prepareRespondentResponseText(CaseData caseData, Party respondent, boolean isRespondent1) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);

        if (scenario.equals(ONE_V_ONE) || scenario.equals(TWO_V_ONE)) {
            return handleSinglePartyScenario(caseData, scenario);
        } else {
            return handleMultiPartyScenario(caseData, respondent, isRespondent1, scenario);
        }
    }

    private String handleSinglePartyScenario(CaseData caseData, MultiPartyScenario scenario) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            RespondentResponseTypeSpec responseTypeForSpec = getResponseTypeForSpec(caseData, scenario);
            return getSpecClaimResponseText(responseTypeForSpec);
        } else {
            return getNonSpecClaimResponseText(caseData.getRespondent1ClaimResponseType());
        }
    }

    private RespondentResponseTypeSpec getResponseTypeForSpec(CaseData caseData, MultiPartyScenario scenario) {
        if (scenario.equals(TWO_V_ONE) && YES.equals(caseData.getDefendantSingleResponseToBothClaimants())) {
            return caseData.getRespondent1ClaimResponseTypeForSpec();
        } else {
            return scenario.equals(TWO_V_ONE)
                ? caseData.getClaimant1ClaimResponseTypeForSpec()
                : caseData.getRespondent1ClaimResponseTypeForSpec();
        }
    }

    private String getSpecClaimResponseText(RespondentResponseTypeSpec responseTypeForSpec) {
        switch (responseTypeForSpec) {
            case COUNTER_CLAIM:
                return "RPA Reason: Defendant rejects and counter claims.";
            case FULL_ADMISSION:
                return "RPA Reason: Defendant fully admits.";
            case PART_ADMISSION:
                return "RPA Reason: Defendant partial admission.";
            default:
                return "";
        }
    }

    private String getNonSpecClaimResponseText(RespondentResponseType responseType) {
        switch (responseType) {
            case COUNTER_CLAIM:
                return "RPA Reason: Defendant rejects and counter claims.";
            case FULL_ADMISSION:
                return "RPA Reason: Defendant fully admits.";
            case PART_ADMISSION:
                return "RPA Reason: Defendant partial admission.";
            default:
                return "";
        }
    }

    private String handleMultiPartyScenario(CaseData caseData, Party respondent, boolean isRespondent1, MultiPartyScenario scenario) {
        String paginatedMessage = "";

        if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
            paginatedMessage = getPaginatedMessageFor1v2SameSolicitor(caseData, isRespondent1);
        }

        return String.format(
            "RPA Reason: %sDefendant: %s has responded: %s",
            paginatedMessage,
            respondent.getPartyName(),
            SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                ? getResponseTypeForRespondentSpec(caseData, respondent)
                : getResponseTypeForRespondent(caseData, respondent)
        );
    }

    protected String getPaginatedMessageFor1v2SameSolicitor(CaseData caseData, boolean isRespondent1) {
        int index = 1;
        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
        LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();
        if (respondent1ResponseDate != null && respondent2ResponseDate != null) {
            index = isRespondent1 ? 1 : 2;
        }
        return format(
            "[%d of 2 - %s] ",
            index,
            time.now().toLocalDate().toString()
        );
    }

    protected void buildRespondentResponseEvent(EventHistory.EventHistoryBuilder builder,
                                                CaseData caseData,
                                                RespondentResponseType respondentResponseType,
                                                LocalDateTime respondentResponseDate,
                                                String respondentID) {
        switch (respondentResponseType) {
            case FULL_DEFENCE:
                buildDefenceFiled(builder, caseData, respondentResponseDate, respondentID);
                break;
            case PART_ADMISSION:
                buildReceiptOfPartAdmission(builder, respondentResponseDate, respondentID);
                break;
            case FULL_ADMISSION:
                buildReceiptOfAdmission(builder, respondentResponseDate, respondentID);
                break;
            default:
                break;
        }
    }

    protected void buildRespondentResponseEventForSpec(EventHistory.EventHistoryBuilder builder,
                                                       CaseData caseData,
                                                       RespondentResponseTypeSpec respondentResponseTypeSpec,
                                                       LocalDateTime respondentResponseDate,
                                                       String respondentID) {
        switch (respondentResponseTypeSpec) {
            case FULL_DEFENCE:
                buildDefenceFiled(builder, caseData, respondentResponseDate, respondentID);
                break;
            case PART_ADMISSION:
                buildReceiptOfPartAdmission(builder, respondentResponseDate, respondentID);
                break;
            case FULL_ADMISSION:
                buildReceiptOfAdmission(builder, respondentResponseDate, respondentID);
                break;
            default:
                break;
        }
    }

    private void buildDefenceFiled(EventHistory.EventHistoryBuilder builder,
                                   CaseData caseData,
                                   LocalDateTime respondentResponseDate,
                                   String respondentID) {
        if (respondentID.equals(RESPONDENT_ID)) {
            RespondToClaim respondToClaim = caseData.getRespondToClaim();
            if (isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)) {
                builder.statesPaid(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)
                ));
            } else {
                builder.defenceFiled(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    false
                ));
            }
            builder.directionsQuestionnaire(buildDirectionsQuestionnaireFiledEvent(
                builder, caseData, respondentResponseDate, respondentID,
                caseData.getRespondent1DQ(), caseData.getRespondent1(), true
            ));
        } else {
            RespondToClaim respondToClaim;
            if (ONE_V_TWO_ONE_LEGAL_REP.equals(MultiPartyScenario.getMultiPartyScenario(caseData))
                && caseData.getSameSolicitorSameResponse() == YES) {
                respondToClaim = caseData.getRespondToClaim();
            } else {
                respondToClaim = caseData.getRespondToClaim2();
            }
            if (isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)) {
                builder.statesPaid(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)
                ));
            } else {
                builder.defenceFiled(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    false
                ));
            }
            builder.directionsQuestionnaire(buildDirectionsQuestionnaireFiledEvent(
                builder, caseData, respondentResponseDate, respondentID,
                caseData.getRespondent2DQ(), caseData.getRespondent2(), false
            ));
        }

    }

    protected void buildReceiptOfPartAdmission(EventHistory.EventHistoryBuilder builder,
                                               LocalDateTime respondentResponseDate,
                                               String respondentID) {
        builder.receiptOfPartAdmission(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                .dateReceived(respondentResponseDate)
                .litigiousPartyID(respondentID)
                .build());
    }

    protected void buildReceiptOfAdmission(EventHistory.EventHistoryBuilder builder,
                                           LocalDateTime respondentResponseDate,
                                           String respondentID) {
        builder.receiptOfAdmission(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(RECEIPT_OF_ADMISSION.getCode())
                .dateReceived(respondentResponseDate)
                .litigiousPartyID(respondentID)
                .build());
    }

    protected boolean isAllPaid(BigDecimal totalClaimAmount, RespondToClaim claimResponse) {
        return totalClaimAmount != null
            && Optional.ofNullable(claimResponse).map(RespondToClaim::getHowMuchWasPaid)
            .map(paid -> MonetaryConversions.penniesToPounds(paid).compareTo(totalClaimAmount) >= 0).orElse(false);
    }

    protected Event buildDefenceFiledEvent(EventHistory.EventHistoryBuilder builder,
                                           LocalDateTime respondentResponseDate,
                                           String litigiousPartyID,
                                           boolean statesPaid) {
        return Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(statesPaid ? STATES_PAID.getCode() : DEFENCE_FILED.getCode())
            .dateReceived(respondentResponseDate)
            .litigiousPartyID(litigiousPartyID)
            .build();
    }

    protected Event buildDirectionsQuestionnaireFiledEvent(EventHistory.EventHistoryBuilder builder,
                                                           CaseData caseData,
                                                           LocalDateTime respondentResponseDate,
                                                           String litigiousPartyID,
                                                           DQ respondentDQ,
                                                           Party respondent,
                                                           boolean isRespondent1) {
        return Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
            .dateReceived(respondentResponseDate)
            .litigiousPartyID(litigiousPartyID)
            .eventDetailsText(prepareFullDefenceEventText(
                respondentDQ,
                caseData,
                isRespondent1,
                respondent
            ))
            .eventDetails(EventDetails.builder()
                .stayClaim(isStayClaim(respondentDQ))
                .preferredCourtCode(getPreferredCourtCode(respondentDQ))
                .preferredCourtName("")
                .build())
            .build();
    }

    public String prepareFullDefenceEventText(DQ dq, CaseData caseData, boolean isRespondent1, Party respondent) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        String paginatedMessage = "";
        if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
            paginatedMessage = getPaginatedMessageFor1v2SameSolicitor(caseData, isRespondent1);
        }
        return (format(
            "%sDefendant: %s has responded: %s; "
                + "preferredCourtCode: %s; stayClaim: %s",
            paginatedMessage,
            respondent.getPartyName(),
            getResponseTypeForRespondent(caseData, respondent),
            getPreferredCourtCode(dq),
            isStayClaim(dq)
        ));
    }

    protected List<ClaimantResponseDetails> prepareApplicantsDetails(CaseData caseData) {
        List<ClaimantResponseDetails> applicantsDetails = new ArrayList<>();
        if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            if (YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
                applicantsDetails.add(ClaimantResponseDetails.builder()
                    .dq(caseData.getApplicant1DQ())
                    .litigiousPartyID(APPLICANT_ID)
                    .responseDate(caseData.getApplicant1ResponseDate())
                    .build());
            }
            if (YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
                || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
                applicantsDetails.add(ClaimantResponseDetails.builder()
                    .dq(caseData.getApplicant2DQ())
                    .litigiousPartyID(APPLICANT2_ID)
                    .responseDate(
                        SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                            ? caseData.getApplicant1ResponseDate()
                            : caseData.getApplicant2ResponseDate())
                    .build());
            }
        } else {
            applicantsDetails.add(ClaimantResponseDetails.builder()
                .dq(caseData.getApplicant1DQ())
                .litigiousPartyID(APPLICANT_ID)
                .responseDate(caseData.getApplicant1ResponseDate())
                .build());
        }
        return applicantsDetails;
    }

    protected List<Event> prepareMiscEventList(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                               List<String> miscEventText, LocalDateTime... eventDate) {
        return IntStream.range(0, miscEventText.size())
            .mapToObj(index ->
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(eventDate.length > 0
                        && eventDate[0] != null
                        ? eventDate[0] : caseData.getApplicant1ResponseDate())
                    .eventDetailsText(miscEventText.get(index))
                    .eventDetails(EventDetails.builder()
                        .miscText(miscEventText.get(index))
                        .build())
                    .build())
            .toList();
    }

    protected Event buildConsentExtensionFilingDefenceEvent(
        PartyData party, MultiPartyScenario scenario, int eventNumber) {
        return Event.builder()
            .eventSequence(eventNumber)
            .eventCode(CONSENT_EXTENSION_FILING_DEFENCE.getCode())
            .dateReceived(party.getTimeExtensionDate())
            .litigiousPartyID(party.getRole().equals(RESPONDENT_ONE) ? RESPONDENT_ID : RESPONDENT2_ID)
            .eventDetailsText(getExtensionEventText(scenario, party))
            .eventDetails(EventDetails.builder()
                .agreedExtensionDate(party.getSolicitorAgreedDeadlineExtension().format(ISO_DATE))
                .build())
            .build();
    }

    protected String getExtensionEventText(MultiPartyScenario scenario, PartyData party) {
        String extensionDate = party.getSolicitorAgreedDeadlineExtension()
            .format(DateTimeFormatter.ofPattern("dd MM yyyy"));
        switch (scenario) {
            case ONE_V_TWO_ONE_LEGAL_REP:
                return format("Defendant(s) have agreed extension: %s", extensionDate);
            case ONE_V_TWO_TWO_LEGAL_REP:
                return format("Defendant: %s has agreed extension: %s", party.getDetails().getPartyName(),
                    extensionDate
                );
            default:
                return format("agreed extension date: %s", extensionDate);
        }
    }
}
