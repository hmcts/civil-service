package uk.gov.hmcts.reform.civil.ga.stateflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.LIST_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_AN_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.REQUEST_MORE_INFO;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.APPLICATION_SUBMITTED_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.JUDGE_DIRECTIONS;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.JUDGE_WRITTEN_REPRESENTATION;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.LISTED_FOR_HEARING;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState.Main.PROCEED_GENERAL_APPLICATION;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    GaStateFlowEngine.class
})

public class StateFlowEngineTest {

    @Autowired
    private GaStateFlowEngine stateFlowEngine;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldReturnApplicationSubmittedWhenPBAPaymentIsFailed() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentFailureCaseData();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(APPLICATION_SUBMITTED.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(2)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName());
    }

    @Test
    void shouldReturnApplicationSubmittedWhenPBAPaymentIsSuccess() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().toBuilder().parentClaimantIsApplicant(NO).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(PROCEED_GENERAL_APPLICATION.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(3)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName());
    }

    @Test
    void shouldReturn_ApplicationSubmitted_JudicialDecision_WhenJudgeMadeDecision() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .generalOrderApplication()
            .generalAppPBADetails(
                GeneralApplicationPbaDetails.builder()
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.SUCCESS)
                                        .build()).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(NO)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(ORDER_MADE.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             ORDER_MADE.fullName());
    }

    @Test
    void shouldReturn_ApplicationSubmitted_JudicialDecision_WhenJudgeMadeDecisionFreeformOrder() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .generalOrderFreeFormApplication()
            .generalAppPBADetails(
                GeneralApplicationPbaDetails.builder()
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.SUCCESS)
                                        .build())
                    .fee(new Fee().setCode("FEE23")).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(ORDER_MADE.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             ORDER_MADE.fullName());
    }

    @Test
    void shouldReturn_WhenJudgeMadeFinalOrder() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .judgeFinalOrderApplication()
            .generalAppPBADetails(
                GeneralApplicationPbaDetails.builder()
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.SUCCESS)
                                        .build())
                    .fee(new Fee().setCode("FEE23")).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(ORDER_MADE.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             ORDER_MADE.fullName());
    }

    @Test
    void shouldReturn_Judge_Written_Rep_WhenJudgeMadeDecision() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .writtenRepresentationSequentialApplication()
            .generalAppPBADetails(
                GeneralApplicationPbaDetails.builder()
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.SUCCESS)
                                        .build()).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(NO)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(JUDGE_WRITTEN_REPRESENTATION.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             JUDGE_WRITTEN_REPRESENTATION.fullName());
    }

    @Test
    void shouldReturn_Judge_Order_Made_WhenJudgeMadeDecision() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .approveApplication()
            .generalAppPBADetails(
                GeneralApplicationPbaDetails.builder()
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.SUCCESS)
                                        .build()).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(ORDER_MADE.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             ORDER_MADE.fullName());
    }

    @Test
    void shouldReturn_Listed_For_Hearing_WhenJudgeMadeDecision() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().toBuilder()
            .judicialDecision(GAJudicialDecision.builder().decision(LIST_FOR_A_HEARING).build())
            .judicialListForHearing(GAJudgesHearingListGAspec.builder().build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(LISTED_FOR_HEARING.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             LISTED_FOR_HEARING.fullName());
    }

    @Test
    void shouldReturn_Additional_Info_WhenJudgeMadeDecision() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().toBuilder()
            .judicialDecision(GAJudicialDecision.builder().decision(REQUEST_MORE_INFO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(ADDITIONAL_INFO.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             ADDITIONAL_INFO.fullName());
    }

    @Test
    void shouldReturn_Judge_Directions_WhenJudgeMadeDecision() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().toBuilder()
            .judicialDecision(GAJudicialDecision.builder().decision(MAKE_AN_ORDER).build())
            .judicialDecisionMakeOrder(
                GAJudicialMakeAnOrder.builder().makeAnOrder(
                    GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(JUDGE_DIRECTIONS.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             JUDGE_DIRECTIONS.fullName());
    }

    @Test
    void shouldReturnApplicationSubmittedWhenPBAPaymentIsSuccess_SetWelshFlowFlag() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().toBuilder()
                .isGaApplicantLip(YES)
                .applicantBilingualLanguagePreference(YES)
                .parentClaimantIsApplicant(NO).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(PROCEED_GENERAL_APPLICATION.fullName());
        assertThat(stateFlow.getFlags()).hasSize(5);
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED")).isTrue();
        assertThat(stateFlow.getStateHistory()).hasSize(3)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName());
    }

    @Test
    void shouldReturnApplicationSubmittedWhenPBAPaymentIsSuccess_SetWelshFlowFlagForRespondentLip() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().toBuilder()
                .isGaApplicantLip(NO)
                .isGaRespondentOneLip(YES)
                .parentClaimantIsApplicant(NO)
                .respondentBilingualLanguagePreference(YES)
                .build();

        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(PROCEED_GENERAL_APPLICATION.fullName());
        assertThat(stateFlow.getFlags()).hasSize(5);
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED")).isTrue();
        assertThat(stateFlow.getStateHistory()).hasSize(3)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName());
    }

    @Test
    void shouldReturn_Judge_Written_Rep_WhenJudgeMadeDecisionForWelshApplicant() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .writtenRepresentationSequentialApplication()
            .generalAppPBADetails(
                GeneralApplicationPbaDetails.builder()
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.SUCCESS)
                                        .build())
                    .fee(new Fee().setCode("FEE23")).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .isGaApplicantLip(YES)
            .applicantBilingualLanguagePreference(YES)
            .parentClaimantIsApplicant(NO)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(JUDGE_WRITTEN_REPRESENTATION.fullName());

        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             JUDGE_WRITTEN_REPRESENTATION.fullName());
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED_FOR_JUDGE_DECISION")).isTrue();
    }

    @Test
    void shouldReturnApplicationSubmittedWhenPBAPaymentIsSuccess_DontSetWelshFlowFlagForRespondentLip() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().toBuilder()
                .isGaApplicantLip(NO)
                .isGaRespondentOneLip(YES)
                .parentClaimantIsApplicant(NO)
                .respondentBilingualLanguagePreference(NO)
                .build();

        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(PROCEED_GENERAL_APPLICATION.fullName());
        assertThat(stateFlow.getFlags()).hasSize(5);
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED")).isFalse();
        assertThat(stateFlow.getStateHistory()).hasSize(3)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName());
    }

    @Test
    void shouldSetWelshFlag_Judge_Directions_WhenJudgeMadeDecision() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().toBuilder()
            .judicialDecision(GAJudicialDecision.builder().decision(MAKE_AN_ORDER).build())
            .isGaApplicantLip(YES)
            .applicantBilingualLanguagePreference(YES)
            .isGaRespondentOneLip(YES)
            .judicialDecisionMakeOrder(
                GAJudicialMakeAnOrder.builder().makeAnOrder(
                    GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(JUDGE_DIRECTIONS.fullName());
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED_FOR_JUDGE_DECISION")).isTrue();
        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             JUDGE_DIRECTIONS.fullName());
    }

    @Test
    void shouldSetWelshFlagRespondentBilingual_Judge_Directions_WhenJudgeMadeDecision() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().toBuilder()
            .judicialDecision(GAJudicialDecision.builder().decision(MAKE_AN_ORDER).build())
            .isGaApplicantLip(YES)
            .respondentBilingualLanguagePreference(YES)
            .isGaRespondentOneLip(YES)
            .judicialDecisionMakeOrder(
                GAJudicialMakeAnOrder.builder().makeAnOrder(
                    GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(JUDGE_DIRECTIONS.fullName());
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED_FOR_JUDGE_DECISION")).isTrue();
        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             JUDGE_DIRECTIONS.fullName());
    }

    @Test
    void shouldReturn_Judge_Order_Made_WhenJudgeMadeDecisionForWelshLip() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .approveApplication()
            .isGaApplicantLip(YES)
            .isGaRespondentOneLip(YES)
            .generalAppPBADetails(
                GeneralApplicationPbaDetails.builder()
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.SUCCESS)
                                        .build())
                    .fee(new Fee().setCode("FEE23")).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .applicantBilingualLanguagePreference(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(ORDER_MADE.fullName());
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED_FOR_JUDGE_DECISION")).isTrue();
        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             ORDER_MADE.fullName());
    }

    @Test
    void shouldReturn_Judge_Order_Made_WhenJudgeMadeDecisionForRespondentWelshLip() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .approveApplication()
            .isGaApplicantLip(YES)
            .isGaRespondentOneLip(YES)
            .generalAppPBADetails(
                GeneralApplicationPbaDetails.builder()
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.SUCCESS)
                                        .build())
                    .fee(new Fee().setCode("FEE23")).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .applicantBilingualLanguagePreference(YES)
            .respondentBilingualLanguagePreference(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(ORDER_MADE.fullName());
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED_FOR_JUDGE_DECISION")).isTrue();
        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             ORDER_MADE.fullName());
    }

    @Test
    void shouldReturn_Judge_Order_Made_WhenJudgeMadeDecisionForNonWelshLip() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .approveApplication()
            .isGaApplicantLip(YES)
            .isGaRespondentOneLip(YES)
            .generalAppPBADetails(
                GeneralApplicationPbaDetails.builder()
                    .paymentDetails(PaymentDetails.builder()
                                        .status(PaymentStatus.SUCCESS)
                                        .build()).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .parentClaimantIsApplicant(YES)
            .applicantBilingualLanguagePreference(NO)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(ORDER_MADE.fullName());
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED_FOR_JUDGE_DECISION")).isFalse();
        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             ORDER_MADE.fullName());
    }

    @Test
    void shouldReturn_Additional_Info_WhenJudgeMadeDecisionForApplicantWelsh() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().toBuilder()
            .judicialDecision(GAJudicialDecision.builder().decision(REQUEST_MORE_INFO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder().requestMoreInfoOption(
            GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION).build())
            .parentClaimantIsApplicant(YES)
            .isGaApplicantLip(YES)
            .isGaRespondentOneLip(YES)
            .applicantBilingualLanguagePreference(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(ADDITIONAL_INFO.fullName());
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED_FOR_JUDGE_DECISION")).isTrue();
        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             ADDITIONAL_INFO.fullName());
    }

    @Test
    void shouldReturn_Additional_Info_WhenJudgeMadeDecisionForRespondentWelsh() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().toBuilder()
            .judicialDecision(GAJudicialDecision.builder().decision(REQUEST_MORE_INFO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder().requestMoreInfoOption(
                GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION).build())
            .parentClaimantIsApplicant(YES)
            .isGaApplicantLip(YES)
            .isGaRespondentOneLip(YES)
            .respondentBilingualLanguagePreference(YES)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build()).build();
        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(ADDITIONAL_INFO.fullName());
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED_FOR_JUDGE_DECISION")).isTrue();
        assertThat(stateFlow.getStateHistory()).hasSize(5)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName(),
                             APPLICATION_SUBMITTED_JUDICIAL_DECISION.fullName(),
                             ADDITIONAL_INFO.fullName());
    }

    @Test
    void shouldReturnApplicationSubmittedWhenPBAPaymentIsSuccess_DontSetWelshFlowFlagForApplicantLip() {
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().toBuilder()
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .parentClaimantIsApplicant(YES)
                .applicantBilingualLanguagePreference(NO)
                .build();

        GaStateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        assertThat(stateFlow.getState()).extracting(State::getName).isNotNull()
            .isEqualTo(PROCEED_GENERAL_APPLICATION.fullName());
        assertThat(stateFlow.getFlags()).hasSize(5);
        assertThat(stateFlow.getFlags().get("WELSH_ENABLED")).isFalse();
        assertThat(stateFlow.getStateHistory()).hasSize(3)
            .extracting(State::getName)
            .containsExactly(DRAFT.fullName(), APPLICATION_SUBMITTED.fullName(),
                             PROCEED_GENERAL_APPLICATION.fullName());
    }
}
