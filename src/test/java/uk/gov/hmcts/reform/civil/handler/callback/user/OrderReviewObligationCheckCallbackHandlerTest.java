package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ObligationReason;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ObligationData;
import uk.gov.hmcts.reform.civil.model.ObligationWAFlag;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class OrderReviewObligationCheckCallbackHandlerTest extends BaseCallbackHandlerTest {

    private OrderReviewObligationCheckCallbackHandler handler;
    @Mock
    private FeatureToggleService featureToggleService;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mapper.registerModules(new JavaTimeModule(), new Jdk8Module());
        handler = new OrderReviewObligationCheckCallbackHandler(mapper, featureToggleService);
    }

    @Nested
    class OrderReviewObligationCheckCallback {

        @ParameterizedTest
        @MethodSource("provideObligationData")
        void shouldHandleObligationWATaskRaised(LocalDate obligationDate, YesOrNo initialTaskRaised, YesOrNo expectedTaskRaised) {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build().builder()
                .storedObligationData(List.of(
                    Element.<ObligationData>builder()
                        .value(ObligationData.builder()
                                   .obligationDate(obligationDate)
                                   .obligationWATaskRaised(initialTaskRaised)
                                   .obligationReason(ObligationReason.UNLESS_ORDER)
                                   .build())
                        .build()))
                .build();
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            List<Element<ObligationData>> storedObligationData = mapper.convertValue(
                response.getData().get("storedObligationData"),
                new TypeReference<>() {}
            );

            assertThat(storedObligationData)
                .extracting(Element::getValue)
                .extracting(ObligationData::getObligationWATaskRaised)
                .contains(expectedTaskRaised);
        }

        private static Stream<Arguments> provideObligationData() {
            return Stream.of(
                arguments(LocalDate.now().minusDays(1), YesOrNo.NO, YesOrNo.YES),
                arguments(LocalDate.now().plusDays(2), YesOrNo.NO, YesOrNo.NO),
                arguments(LocalDate.now().minusDays(1), YesOrNo.YES, YesOrNo.YES)
            );
        }

        @Test
        void shouldHandleEmptyObligationDataList() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build()
                .builder().storedObligationData(List.of(
                Element.<ObligationData>builder()
                    .value(ObligationData.builder()
                               .obligationDate(LocalDate.now().minusDays(1))
                               .obligationWATaskRaised(YesOrNo.NO)
                               .obligationReason(ObligationReason.UNLESS_ORDER)
                               .build())
                    .build())).build();
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(false);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotSetObligationWAFlagWhenNoMatchingDataFound() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build().builder()
                .storedObligationData(List.of(
                    Element.<ObligationData>builder()
                        .value(ObligationData.builder()
                                   .obligationDate(LocalDate.now().plusDays(1))
                                   .obligationWATaskRaised(YesOrNo.NO)
                                   .build())
                        .build()))
                .build();
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            ObligationWAFlag obligationWAFlag = mapper.convertValue(
                response.getData().get("obligationWAFlag"),
                ObligationWAFlag.class
            );

            assertThat(obligationWAFlag).isNull();
        }

        @ParameterizedTest
        @MethodSource("provideObligationDataForAllScenarios")
        void shouldHandleAllIfScenarios(LocalDate obligationDate, YesOrNo initialTaskRaised, ObligationReason obligationReason,
                                        CaseState caseState, String manageStayOption, YesOrNo expectedTaskRaised, ObligationWAFlag expectedFlag) {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build().builder()
                .manageStayOption(manageStayOption)
                .storedObligationData(List.of(
                    Element.<ObligationData>builder()
                        .value(ObligationData.builder()
                                   .obligationDate(obligationDate)
                                   .obligationWATaskRaised(initialTaskRaised)
                                   .obligationReason(obligationReason)
                                   .build())
                        .build()))
                .ccdState(caseState)
                .build();
            when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            List<Element<ObligationData>> storedObligationData = mapper.convertValue(
                response.getData().get("storedObligationData"),
                new TypeReference<>() {}
            );

            assertThat(storedObligationData)
                .extracting(Element::getValue)
                .extracting(ObligationData::getObligationWATaskRaised)
                .contains(expectedTaskRaised);

            ObligationWAFlag obligationWAFlag = mapper.convertValue(
                response.getData().get("obligationWAFlag"),
                ObligationWAFlag.class
            );

            assertThat(obligationWAFlag).isEqualTo(expectedFlag);
        }

        private static Stream<Arguments> provideObligationDataForAllScenarios() {
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

            return Stream.of(
                // Case 1: Obligation date is before current date, task not raised, reason is LIFT_A_STAY, case state is CASE_STAYED
                arguments(currentDate.minusDays(1), YesOrNo.NO, ObligationReason.LIFT_A_STAY, CaseState.CASE_STAYED, null, YesOrNo.YES,
                          ObligationWAFlag.builder()
                              .currentDate(currentDate.format(formatter))
                              .obligationReason(ObligationReason.LIFT_A_STAY.name())
                              .obligationReasonDisplayValue(ObligationReason.LIFT_A_STAY.getDisplayedValue())
                              .build()),

                arguments(currentDate.minusDays(1), YesOrNo.NO, ObligationReason.LIFT_A_STAY, CaseState.CASE_STAYED, "REQUEST_UPDATE", YesOrNo.YES,
                          ObligationWAFlag.builder()
                              .currentDate(null)
                              .obligationReason(null)
                              .obligationReasonDisplayValue(null)
                              .build()),

                // Case 2: Obligation date is after current date, task not raised, reason is DISMISS_CASE, case state is CASE_DISMISSED
                arguments(currentDate.plusDays(1), YesOrNo.NO, ObligationReason.DISMISS_CASE, CaseState.CASE_DISMISSED, null, YesOrNo.NO, null),

                // Case 3: Obligation date is before current date, task already raised, reason is UNLESS_ORDER, case state is CASE_STAYED
                arguments(currentDate.minusDays(1), YesOrNo.YES, ObligationReason.UNLESS_ORDER, CaseState.CASE_STAYED, null, YesOrNo.YES, null),

                // Case 4: Obligation date is before current date, task not raised, reason is UNLESS_ORDER, case state is CASE_DISMISSED
                arguments(currentDate.minusDays(1), YesOrNo.NO, ObligationReason.UNLESS_ORDER, CaseState.CASE_DISMISSED, null, YesOrNo.YES,
                          ObligationWAFlag.builder()
                              .currentDate(currentDate.format(formatter))
                              .obligationReason(ObligationReason.UNLESS_ORDER.name())
                              .obligationReasonDisplayValue(ObligationReason.UNLESS_ORDER.getDisplayedValue())
                              .build()),
                arguments(currentDate.minusDays(1), YesOrNo.NO, ObligationReason.STAY_A_CASE, CaseState.CASE_STAYED, null, YesOrNo.YES,
                          ObligationWAFlag.builder()
                              .currentDate(null)
                              .obligationReason(null)
                              .obligationReasonDisplayValue(null)
                              .build()),
                arguments(currentDate.minusDays(1), YesOrNo.NO, ObligationReason.STAY_A_CASE, CaseState.JUDICIAL_REFERRAL, null, YesOrNo.YES,
                          ObligationWAFlag.builder()
                              .currentDate(currentDate.format(formatter))
                              .obligationReason(ObligationReason.STAY_A_CASE.name())
                              .obligationReasonDisplayValue(ObligationReason.STAY_A_CASE.getDisplayedValue())
                              .build())
            );
        }
    }
}
