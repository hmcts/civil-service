package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_DIRECTIONS_ORDER_DOCS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.LISTING_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.LIST_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_AN_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.REQUEST_MORE_INFO;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SET_ASIDE_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SpringBootTest(classes =
    StateGeneratorService.class
)
public class StateGeneratorServiceTest {

    @Autowired
    StateGeneratorService stateGeneratorService;
    @MockBean
    JudicialDecisionHelper judicialDecisionHelper;
    private static final String JUDGES_DECISION = "MAKE_DECISION";

    @Test
    public void shouldReturnAwaiting_Addition_InformationWhenMoreInfoSelected() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .judicialDecision(new GAJudicialDecision(REQUEST_MORE_INFO))
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().orderText("test").build())
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder().build())
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(any())).thenReturn(false);
        assertThat(caseState).isEqualTo(AWAITING_ADDITIONAL_INFORMATION);
    }

    @Test
    public void shouldReturn_Awaiting_Written_Representation_WhenMakeOrderForWrittenRepresentationsSelected() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .judicialDecision(new GAJudicialDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS))
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().orderText("test").build())
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);

        assertThat(caseState).isEqualTo(AWAITING_WRITTEN_REPRESENTATIONS);
    }

    @Test
    public void shouldReturn_Awaiting_Directions_Order_Docs_WhenMakeOrderSelectedAndTextProvided() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .directionsText("test")
                                           .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
            .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.YES).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .applicationIsCloaked(YesOrNo.NO)
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);

        assertThat(caseState).isEqualTo(AWAITING_DIRECTIONS_ORDER_DOCS);
    }

    @Test
    public void shouldReturnCurrentStateWhenMakeOrderAndDismissed() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(DISMISS_THE_APPLICATION)
                                           .build())
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);

        assertThat(caseState).isEqualTo(APPLICATION_DISMISSED);
    }

    @Test
    public void shouldReturnListingForHearingWhenTheDecisionHasBeenMade() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(LIST_FOR_A_HEARING))
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);

        assertThat(caseState).isEqualTo(LISTING_FOR_A_HEARING);
    }

    @Test
    public void shouldReturnOrderDateWhenCaseworkerApprovesApplication() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .approveConsentOrder(GAApproveConsentOrder.builder().consentOrderDescription("Test Order")
                                     .build())
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(ORDER_MADE);
    }

    @Test
    public void shouldReturnProceedsInHeritageSystemWhenTheDecisionHasBeenMade() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(APPROVE_OR_EDIT)
                                           .build())
            .parentClaimantIsApplicant(YesOrNo.YES)
            .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
            .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeJudgement()).build())
            .build();
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(PROCEEDS_IN_HERITAGE);
    }

    @Test
    public void shouldReturnOrderMadeWhenTheDecisionHasBeenMade() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(APPROVE_OR_EDIT)
                                           .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.NO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .applicationIsCloaked(YesOrNo.YES)
            .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
            .build();
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(ORDER_MADE);
    }

    @Test
    public void shouldReturnOrderMadeWhenJudgeDecidesFreeFormOrder() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .freeFormRecitalText("test")
            .freeFormOrderedText("test")
            .judicialDecision(new GAJudicialDecision(FREE_FORM_ORDER))
            .orderOnCourtInitiative(FreeFormOrderValues.builder().onInitiativeSelectionDate(LocalDate.now())
                                        .onInitiativeSelectionTextArea("test").build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.NO).build())
            .generalAppDetailsOfOrder("order test")
            .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
            .build();
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(ORDER_MADE);
    }

    @Test
     void shouldNotReturnOrderAdditionalAddPayment_WhenJudgeUncloakTheApplicationInOrderMake() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(APPROVE_OR_EDIT)
                                           .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.NO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
            .applicationIsCloaked(YesOrNo.NO)
            .build();
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(ORDER_MADE);
    }

    @Test
     void shouldNotReturnOrderAdditionalAddPayment_WhenJudgeUncloakTheApplicationAwaitingDocsOrderMake() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .directionsText("test")
                                           .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.NO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
            .applicationIsCloaked(YesOrNo.NO)
            .build();
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(AWAITING_DIRECTIONS_ORDER_DOCS);
    }

    @Test
    void shouldNotReturnAdditionalAddPayment_WhenJudgeUncloakTheApplication_RequestMoreInformation() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY,
                                                                         NO, YesOrNo.NO)
            .generalAppType(GAApplicationType.builder()
                    .types(singletonList(SET_ASIDE_JUDGEMENT))
                    .build()).build();

        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(any())).thenReturn(true);

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(APPLICATION_ADD_PAYMENT);
    }

    @Test
    void shouldReturnAdditionalAddPayment_WhenJudgeUncloakTheApplication_RequestMoreInformation() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY,
                                                                         NO, YesOrNo.NO).build();

        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(any())).thenReturn(true);

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(APPLICATION_ADD_PAYMENT);
    }

    @Test
     void shouldRequestMoreInformation_WhenJudgeUncloakTheApplication_AdditionalPaymentIsMadeSuccessfully() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY, NO, YesOrNo.NO)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .additionalPaymentDetails(PaymentDetails.builder()
                                                                    .reference("123456")
                                                                    .status(PaymentStatus.SUCCESS)
                                                                    .build())
                                      .build())
            .build();

        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(any())).thenReturn(true);
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(AWAITING_RESPONDENT_RESPONSE);
    }

    @Test
    void shouldAwaitingJudicialDecision__WhenAdditionalPaymentReceived_RequestMoreInformation_UrgentWithoutNotice() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY, NO, YesOrNo.NO)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .additionalPaymentDetails(PaymentDetails.builder()
                                                                    .reference("123456")
                                                                    .status(PaymentStatus.SUCCESS)
                                                                    .build())
                                      .build())
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
            .build();

        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(any())).thenReturn(true);
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION);
    }

    @Test
    void shouldAwaitingJudicialDecision__WhenAdditionalPaymentReceived_RequestMoreInformation_ConsentOrder() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY, NO, YesOrNo.NO)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .additionalPaymentDetails(PaymentDetails.builder()
                                                                    .reference("123456")
                                                                    .status(PaymentStatus.SUCCESS)
                                                                    .build())
                                      .build())
            .generalAppConsentOrder(NO)
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .respondentsResponses(getRespondentResponse())
            .build();

        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(any())).thenReturn(true);
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION);
    }

    @Test
    void shouldRespondentResponse_WhenAdditionalPaymentReceived_ConsentOrder_withoutRespondentResponse() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY, NO, YesOrNo.NO)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .additionalPaymentDetails(PaymentDetails.builder()
                                                                    .reference("123456")
                                                                    .status(PaymentStatus.SUCCESS)
                                                                    .build())
                                      .build())
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppConsentOrder(NO)
            .build();

        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(any())).thenReturn(true);
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(AWAITING_RESPONDENT_RESPONSE);
    }

    private List<GeneralApplicationTypes> applicationTypeJudgement() {
        return List.of(
            GeneralApplicationTypes.STRIKE_OUT
        );
    }

    private List<Element<GASolicitorDetailsGAspec>> getRespondentSolicitors() {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email("test@gmail.com").organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));

        return respondentSols;
    }

    private List<Element<GARespondentResponse>> getRespondentResponse() {
        List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
        respondentsResponses
            .add(element(GARespondentResponse.builder()
                             .gaHearingDetails(GAHearingDetails.builder()
                                                   .vulnerabilityQuestionsYesOrNo(YES)
                                                   .vulnerabilityQuestion("dummy")
                                                   .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                   .hearingDuration(GAHearingDuration.HOUR_1)
                                                   .build())
                             .build()));

        return respondentsResponses;
    }
}

