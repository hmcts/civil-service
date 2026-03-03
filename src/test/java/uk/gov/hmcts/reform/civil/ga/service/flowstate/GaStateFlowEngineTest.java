package uk.gov.hmcts.reform.civil.ga.service.flowstate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.civil.ga.stateflow.GaStateFlow;
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

@ExtendWith(MockitoExtension.class)
public class GaStateFlowEngineTest {

    @InjectMocks
    private GaStateFlowEngine stateFlowEngine;

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;

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
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().copy().parentClaimantIsApplicant(NO).build();
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
                new GeneralApplicationPbaDetails()
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
                                        ))
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
                new GeneralApplicationPbaDetails()
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
                                        )
                    .setFee(new Fee().setCode("FEE23")))
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
                new GeneralApplicationPbaDetails()
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
                                        )
                    .setFee(new Fee().setCode("FEE23")))
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
                new GeneralApplicationPbaDetails()
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
                                        ))
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
                new GeneralApplicationPbaDetails()
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
                                        ))
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
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().copy()
            .judicialDecision(new GAJudicialDecision().setDecision(LIST_FOR_A_HEARING))
            .judicialListForHearing(new GAJudgesHearingListGAspec())
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
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().copy()
            .judicialDecision(new GAJudicialDecision().setDecision(REQUEST_MORE_INFO))
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
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().copy()
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(
                new GAJudicialMakeAnOrder().setMakeAnOrder(
                    GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING))
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
            GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().copy()
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
            GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().copy()
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
                new GeneralApplicationPbaDetails()
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
                                        )
                    .setFee(new Fee().setCode("FEE23")))
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
            GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().copy()
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
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().copy()
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER))
            .isGaApplicantLip(YES)
            .applicantBilingualLanguagePreference(YES)
            .isGaRespondentOneLip(YES)
            .judicialDecisionMakeOrder(
                new GAJudicialMakeAnOrder().setMakeAnOrder(
                    GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING))
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
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().copy()
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER))
            .isGaApplicantLip(YES)
            .respondentBilingualLanguagePreference(YES)
            .isGaRespondentOneLip(YES)
            .judicialDecisionMakeOrder(
                new GAJudicialMakeAnOrder().setMakeAnOrder(
                    GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING))
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
                new GeneralApplicationPbaDetails()
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
                                        )
                    .setFee(new Fee().setCode("FEE23")))
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
                new GeneralApplicationPbaDetails()
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
                                        )
                    .setFee(new Fee().setCode("FEE23")))
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
                new GeneralApplicationPbaDetails()
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
                                        ))
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
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().copy()
            .judicialDecision(new GAJudicialDecision().setDecision(REQUEST_MORE_INFO))
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(
            GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION))
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
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildPaymentSuccessfulCaseData().copy()
            .judicialDecision(new GAJudicialDecision().setDecision(REQUEST_MORE_INFO))
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(
                GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION))
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
            GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData().copy()
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
