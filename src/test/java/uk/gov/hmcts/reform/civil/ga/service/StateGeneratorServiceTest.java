package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
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

@ExtendWith(MockitoExtension.class)
public class StateGeneratorServiceTest {

    @InjectMocks
    StateGeneratorService stateGeneratorService;
    @Mock
    JudicialDecisionHelper judicialDecisionHelper;

    private static final String JUDGES_DECISION = "MAKE_DECISION";

    @Test
    public void shouldReturnAwaiting_Addition_InformationWhenMoreInfoSelected() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .judicialDecision(new GAJudicialDecision(REQUEST_MORE_INFO))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder().setOrderText("test"))
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo())
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(AWAITING_ADDITIONAL_INFORMATION);
    }

    @Test
    public void shouldReturn_Awaiting_Written_Representation_WhenMakeOrderForWrittenRepresentationsSelected() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .judicialDecision(new GAJudicialDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder().setOrderText("test"))
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);

        assertThat(caseState).isEqualTo(AWAITING_WRITTEN_REPRESENTATIONS);
    }

    @Test
    public void shouldReturn_Awaiting_Directions_Order_Docs_WhenMakeOrderSelectedAndTextProvided() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setDirectionsText("test")
                                           .setMakeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING))
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
            .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(YesOrNo.YES))
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YesOrNo.NO))
            .applicationIsCloaked(YesOrNo.NO)
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);

        assertThat(caseState).isEqualTo(AWAITING_DIRECTIONS_ORDER_DOCS);
    }

    @Test
    public void shouldReturnCurrentStateWhenMakeOrderAndDismissed() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setMakeAnOrder(DISMISS_THE_APPLICATION))
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);

        assertThat(caseState).isEqualTo(APPLICATION_DISMISSED);
    }

    @Test
    public void shouldReturnListingForHearingWhenTheDecisionHasBeenMade() {

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(LIST_FOR_A_HEARING))
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);

        assertThat(caseState).isEqualTo(LISTING_FOR_A_HEARING);
    }

    @Test
    public void shouldReturnOrderDateWhenCaseworkerApprovesApplication() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .approveConsentOrder(new GAApproveConsentOrder().setConsentOrderDescription("Test Order")
                                     )
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(ORDER_MADE);
    }

    @Test
    public void shouldReturnProceedsInHeritageSystemWhenTheDecisionHasBeenMade() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setMakeAnOrder(APPROVE_OR_EDIT))
            .parentClaimantIsApplicant(YesOrNo.YES)
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
            .generalAppType(new GAApplicationType()
                                .setTypes(applicationTypeJudgement()))
            .build();
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(PROCEEDS_IN_HERITAGE);
    }

    @Test
    public void shouldReturnOrderMadeWhenTheDecisionHasBeenMade() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setMakeAnOrder(APPROVE_OR_EDIT))
            .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(YesOrNo.NO))
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YesOrNo.NO))
            .applicationIsCloaked(YesOrNo.YES)
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
            .build();
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(ORDER_MADE);
    }

    @Test
    public void shouldReturnOrderMadeWhenJudgeDecidesFreeFormOrder() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .freeFormRecitalText("test")
            .freeFormOrderedText("test")
            .judicialDecision(new GAJudicialDecision(FREE_FORM_ORDER))
            .orderOnCourtInitiative(new FreeFormOrderValues().setOnInitiativeSelectionDate(LocalDate.now())
                                        .setOnInitiativeSelectionTextArea("test"))
            .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(YesOrNo.NO))
            .generalAppDetailsOfOrder("order test")
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
            .build();
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(ORDER_MADE);
    }

    @Test
     void shouldNotReturnOrderAdditionalAddPayment_WhenJudgeUncloakTheApplicationInOrderMake() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setMakeAnOrder(APPROVE_OR_EDIT))
            .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(YesOrNo.NO))
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YesOrNo.NO))
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
            .applicationIsCloaked(YesOrNo.NO)
            .build();
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(ORDER_MADE);
    }

    @Test
     void shouldNotReturnOrderAdditionalAddPayment_WhenJudgeUncloakTheApplicationAwaitingDocsOrderMake() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecision(new GAJudicialDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setDirectionsText("test")
                                           .setMakeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING))
            .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(YesOrNo.NO))
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YesOrNo.NO))
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
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
            .generalAppType(new GAApplicationType()
                    .setTypes(singletonList(SET_ASIDE_JUDGEMENT))).build();

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
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails()
                                                                    .setReference("123456")
                                                                    .setStatus(PaymentStatus.SUCCESS)
                                                                    )
                                      )
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
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails()
                                                                    .setReference("123456")
                                                                    .setStatus(PaymentStatus.SUCCESS)
                                                                    )
                                      )
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YesOrNo.NO))
            .generalAppUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency(YES))
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
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails()
                                                                    .setReference("123456")
                                                                    .setStatus(PaymentStatus.SUCCESS)
                                                                    )
                                      )
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
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setAdditionalPaymentDetails(new PaymentDetails()
                                                                    .setReference("123456")
                                                                    .setStatus(PaymentStatus.SUCCESS)
                                                                    )
                                      )
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .generalAppConsentOrder(NO)
            .build();

        when(judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(any())).thenReturn(true);
        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);
        assertThat(caseState).isEqualTo(AWAITING_RESPONDENT_RESPONSE);
    }

    @Test
    void shouldReturnCurrentStateWhenJudicialDecisionIsMissing() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .build();

        CaseState caseState = stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(caseData);

        assertThat(caseState).isEqualTo(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION);
    }

    private List<GeneralApplicationTypes> applicationTypeJudgement() {
        return List.of(
            GeneralApplicationTypes.STRIKE_OUT
        );
    }

    private List<Element<GASolicitorDetailsGAspec>> getRespondentSolicitors() {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = new GASolicitorDetailsGAspec().setId("id")
            .setEmail("test@gmail.com").setOrganisationIdentifier("org2");

        respondentSols.add(element(respondent1));

        return respondentSols;
    }

    private List<Element<GARespondentResponse>> getRespondentResponse() {
        List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
        respondentsResponses
            .add(element(new GARespondentResponse()
                             .setGaHearingDetails(new GAHearingDetails()
                                                   .setVulnerabilityQuestionsYesOrNo(YES)
                                                   .setVulnerabilityQuestion("dummy")
                                                   .setHearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                   .setHearingDuration(GAHearingDuration.HOUR_1))
                             ));

        return respondentsResponses;
    }
}
