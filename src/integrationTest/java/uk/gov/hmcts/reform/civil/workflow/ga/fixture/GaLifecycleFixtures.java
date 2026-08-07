package uk.gov.hmcts.reform.civil.workflow.ga.fixture;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

public final class GaLifecycleFixtures {

    public static final long CASE_ID = 1644495739087775L;
    public static final String RESPONDENT_ID = "respondent-user-id";
    public static final String RESPONDENT_ORGANISATION = "respondent-organisation";
    public static final String RESPONDENT_EMAIL = "respondent@example.com";
    public static final String SECOND_RESPONDENT_ID = "second-respondent-user-id";
    public static final String SECOND_RESPONDENT_ORGANISATION = "second-respondent-organisation";
    public static final String PARENT_CASE_REFERENCE = "1234567890123456";
    private static final String APPLICATION_SUBMITTED = "ga/application-submitted";

    private GaLifecycleFixtures() {
    }

    public static GeneralApplicationCaseData paidWithNotice() {
        return CaseDataTemplates.load(APPLICATION_SUBMITTED, GeneralApplicationCaseData.class).copy()
            .ccdState(CaseState.AWAITING_RESPONDENT_RESPONSE)
            .isMultiParty(YesOrNo.NO)
            .generalAppRespondentAgreement(null)
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YesOrNo.YES))
            .generalAppUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency(YesOrNo.NO))
            .generalAppRespondentSolicitors(List.of(element(respondentSolicitor())))
            .build();
    }

    public static GeneralApplicationCaseData paidWithoutNotice() {
        return paidWithNotice().copy()
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YesOrNo.NO))
            .build();
    }

    public static GeneralApplicationCaseData awaitingPayment() {
        return paidWithNotice().copy()
            .ccdState(CaseState.PENDING_APPLICATION_ISSUED)
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setFee(new Fee().setCode("PAY")))
            .build();
    }

    public static GeneralApplicationCaseData successfulPayment() {
        return paidWithNotice().copy()
            .ccdState(CaseState.AWAITING_APPLICATION_PAYMENT)
            .businessProcess(null)
            .generalAppNotificationDeadlineDate(LocalDateTime.of(2026, 8, 10, 16, 0))
            .respondentResponseDeadlineChecked(YesOrNo.YES)
            .build();
    }

    public static GeneralApplicationCaseData respondentVaryJudgmentWithResponse() {
        return paidWithResponse().copy()
            .parentClaimantIsApplicant(YesOrNo.NO)
            .generalAppType(new GAApplicationType().setTypes(List.of(
                GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)))
            .build();
    }

    public static GeneralApplicationCaseData paidWithResponse() {
        return paidWithNotice().copy()
            .respondentsResponses(List.of(element(
                new GARespondentResponse().setGaRespondentDetails(RESPONDENT_ID))))
            .build();
    }

    public static GeneralApplicationCaseData multiPartyWithFirstOrganisationResponse() {
        return paidWithNotice().copy()
            .ccdState(CaseState.PENDING_APPLICATION_ISSUED)
            .isMultiParty(YesOrNo.YES)
            .generalAppRespondentSolicitors(List.of(
                element(respondentSolicitor()),
                element(respondentSolicitor(SECOND_RESPONDENT_ID, SECOND_RESPONDENT_ORGANISATION))
            ))
            .respondentsResponses(List.of(element(
                new GARespondentResponse().setGaRespondentDetails(RESPONDENT_ID))))
            .build();
    }

    public static GeneralApplicationCaseData multiPartyWithBothOrganisationsResponses() {
        return multiPartyWithFirstOrganisationResponse().copy()
            .respondentsResponses(List.of(
                element(new GARespondentResponse().setGaRespondentDetails(RESPONDENT_ID)),
                element(new GARespondentResponse().setGaRespondentDetails(SECOND_RESPONDENT_ID))
            ))
            .build();
    }

    public static GeneralApplicationCaseData responseInput() {
        DynamicList locations = fromList(List.of("Central London County Court - Thomas More Building - EC4A 3TR"));
        locations.setValue(locations.getListItems().getFirst());

        return paidWithNotice().copy()
            .businessProcess(null)
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.SUMMARY_JUDGEMENT)))
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_REFERENCE))
            .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                          .setId("applicant-user-id")
                                          .setEmail("applicant@example.com"))
            .generalAppRespondentSolicitors(List.of(element(respondentSolicitor().setEmail(RESPONDENT_EMAIL))))
            .defendant1PartyName("Respondent One")
            .claimant1PartyName("Applicant One")
            .parentClaimantIsApplicant(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.NO)
            .generalAppRespondent1Representative(new GARespondentRepresentative()
                                                      .setGeneralAppRespondent1Representative(YesOrNo.NO))
            .generalAppRespondReason("The application should not be granted")
            .hearingDetailsResp(new GAHearingDetails()
                                    .setHearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .setHearingPreferredLocation(locations))
            .generalAppRespondDocument(List.of(element(new Document()
                                                            .setDocumentUrl("http://dm-store/response")
                                                            .setDocumentBinaryUrl("http://dm-store/response/binary")
                                                            .setDocumentFileName("response-evidence.pdf"))))
            .build();
    }

    public static GeneralApplicationCaseData decisionStartInput() {
        return paidWithNotice().copy()
            .respondentsResponses(null)
            .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(YesOrNo.NO))
            .generalAppHearingDetails(new GAHearingDetails()
                                          .setHearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .setHearingDuration(GAHearingDuration.HOUR_1))
            .generalAppDetailsOfOrder("Extend the deadline")
            .applicantPartyName("Applicant One")
            .claimant1PartyName("Applicant One")
            .defendant1PartyName("Respondent One")
            .createdDate(LocalDateTime.of(2026, 8, 1, 9, 0))
            .build();
    }

    public static GeneralApplicationCaseData responseParentCase() {
        return new GeneralApplicationCaseData()
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL)
            .build();
    }

    public static GeneralApplicationCaseData decision(GAJudgeDecisionOption decision) {
        return paidWithResponse().copy()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision().setDecision(decision))
            .build();
    }

    public static GeneralApplicationCaseData makeOrder(GAJudgeMakeAnOrderOption order) {
        return decision(GAJudgeDecisionOption.MAKE_AN_ORDER).copy()
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder().setMakeAnOrder(order))
            .build();
    }

    public static GeneralApplicationCaseData requestMoreInformation() {
        return decision(GAJudgeDecisionOption.REQUEST_MORE_INFO).copy()
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                 .setJudgeRequestMoreInfoByDate(LocalDate.of(2026, 8, 14)))
            .build();
    }

    public static GeneralApplicationCaseData directionsOrder() {
        return makeOrder(GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING);
    }

    public static GeneralApplicationCaseData dismissedApplication() {
        return makeOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION);
    }

    public static GeneralApplicationCaseData writtenRepresentations() {
        return decision(GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS);
    }

    public static GeneralApplicationCaseData listForHearing() {
        return decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING);
    }

    public static GeneralApplicationCaseData strikeOutOrder() {
        return makeOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).copy()
            .parentClaimantIsApplicant(YesOrNo.YES)
            .generalAppType(new GAApplicationType().setTypes(List.of(GeneralApplicationTypes.STRIKE_OUT)))
            .build();
    }

    private static GASolicitorDetailsGAspec respondentSolicitor() {
        return respondentSolicitor(RESPONDENT_ID, RESPONDENT_ORGANISATION);
    }

    private static GASolicitorDetailsGAspec respondentSolicitor(String id, String organisation) {
        return new GASolicitorDetailsGAspec()
            .setId(id)
            .setEmail(RESPONDENT_EMAIL)
            .setOrganisationIdentifier(organisation);
    }
}
