package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.hmc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.utils.HearingFeeUtils;
import uk.gov.hmcts.reform.civil.utils.HearingUtils;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator.HEARING_TIME;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateAndApplyFee;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateHearingDueDate;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.hearingFeeRequired;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeHMCAppSolEmailDTOGeneratorTest {

    private static final String REFERENCE_TEMPLATE = "notification-of-hearing-%s";
    private static final String PROCESS_ID = "pid";
    private static final String TYPE_ANY = "ANY";
    private static final String TYPE_FAST = "FAST";
    private static final String NO_FEE_TEMPLATE = "noFeeTemplate";
    private static final String NO_FEE_ON_SUCCESS = "noFeeOnSuccess";
    private static final String FEE_TEMPLATE = "feeTemplate";
    private static final LocalDateTime START_DATE_TIME_1 = LocalDateTime.of(2025, 6, 30, 14, 45);
    private static final LocalDate DUE_DATE_1 = LocalDate.of(2025, 7, 15);
    private static final LocalDateTime START_DATE_TIME_2 = LocalDateTime.of(2025, 1, 5, 9, 0);
    private static final LocalDate DUE_DATE_2 = LocalDate.of(2025, 1, 20);

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private HearingNoticeCamundaService camundaService;

    @Mock
    private HearingFeesService hearingFeesService;

    @InjectMocks
    private GenerateHearingNoticeHMCAppSolEmailDTOGenerator generator;

    @Test
    void getEmailTemplateId_feeNotRequired_returnsNoFeeTemplate() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .businessProcess(new BusinessProcess().setProcessInstanceId(PROCESS_ID))
                .build();
        when(camundaService.getProcessVariables(PROCESS_ID))
                .thenReturn(new HearingNoticeVariables().setHearingType(TYPE_ANY));

        try (MockedStatic<HearingUtils> mocked = mockStatic(HearingUtils.class)) {
            mocked.when(() -> hearingFeeRequired(TYPE_ANY)).thenReturn(false);
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplateHMC())
                    .thenReturn(NO_FEE_TEMPLATE);

            String actual = generator.getEmailTemplateId(caseData);

            assertEquals(NO_FEE_TEMPLATE, actual);
        }
    }

    @Test
    void getEmailTemplateId_paymentSuccess_returnsNoFeeOnSuccess() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .businessProcess(new BusinessProcess().setProcessInstanceId(PROCESS_ID))
                .hearingFeePaymentDetails(new PaymentDetails().setStatus(SUCCESS))
                .build();
        when(camundaService.getProcessVariables(PROCESS_ID))
                .thenReturn(new HearingNoticeVariables().setHearingType(TYPE_FAST));

        try (MockedStatic<HearingUtils> mocked = mockStatic(HearingUtils.class)) {
            mocked.when(() -> hearingFeeRequired(TYPE_FAST)).thenReturn(true);
            when(notificationsProperties.getHearingListedNoFeeClaimantLrTemplateHMC())
                    .thenReturn(NO_FEE_ON_SUCCESS);

            String actual = generator.getEmailTemplateId(caseData);

            assertEquals(NO_FEE_ON_SUCCESS, actual);
        }
    }

    @Test
    void getEmailTemplateId_requiresAndNoPayment_returnsFeeTemplate() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .businessProcess(new BusinessProcess().setProcessInstanceId(PROCESS_ID))
                .hearingFeePaymentDetails(null)
                .build();
        when(camundaService.getProcessVariables(PROCESS_ID))
                .thenReturn(new HearingNoticeVariables().setHearingType(TYPE_FAST));

        try (MockedStatic<HearingUtils> mocked = mockStatic(HearingUtils.class)) {
            mocked.when(() -> hearingFeeRequired(TYPE_FAST)).thenReturn(true);
            when(notificationsProperties.getHearingListedFeeClaimantLrTemplateHMC())
                    .thenReturn(FEE_TEMPLATE);

            String actual = generator.getEmailTemplateId(caseData);

            assertEquals(FEE_TEMPLATE, actual);
        }
    }

    @Test
    void getReferenceTemplate_returnsStaticFormat() {
        assertEquals(REFERENCE_TEMPLATE, generator.getReferenceTemplate());
    }

    @Test
    void addCustomProperties_feeNonNull_populatesAllFields() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .businessProcess(new BusinessProcess().setProcessInstanceId(PROCESS_ID))
                .build();
        when(camundaService.getProcessVariables(PROCESS_ID))
                .thenReturn(new HearingNoticeVariables()
                        .setHearingStartDateTime(START_DATE_TIME_1));
        Fee fakeFee = mock(Fee.class);
        when(fakeFee.formData()).thenReturn("123.45");

        try (MockedStatic<HearingFeeUtils> mocked = mockStatic(HearingFeeUtils.class);
             MockedStatic<NotificationUtils> mockedUtils = mockStatic(NotificationUtils.class)) {
            mocked.when(() -> calculateAndApplyFee(hearingFeesService, caseData, caseData.getAssignedTrack()))
                    .thenReturn(fakeFee);
            mocked.when(() -> calculateHearingDueDate(any(LocalDate.class), eq(START_DATE_TIME_1.toLocalDate())))
                    .thenReturn(DUE_DATE_1);

            mockedUtils.when(() -> getApplicantLegalOrganizationName(eq(caseData), any()))
                .thenReturn("Applicant Org Ltd");

            Map<String, String> props = generator.addCustomProperties(new HashMap<>(), caseData);

            assertAll("custom properties",
                    () -> assertEquals("123.45", props.get(HEARING_FEE)),
                    () -> assertEquals("30-06-2025", props.get(HEARING_DATE)),
                    () -> assertEquals("02:45pm", props.get(HEARING_TIME)),
                    () -> assertEquals("15-07-2025", props.get(HEARING_DUE_DATE)),
                    () -> assertEquals("Applicant Org Ltd", props.get(CLAIM_LEGAL_ORG_NAME_SPEC))
            );
        }
    }

    @Test
    void addCustomProperties_feeNull_defaultsToZero() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .businessProcess(new BusinessProcess().setProcessInstanceId(PROCESS_ID))
                .build();
        when(camundaService.getProcessVariables(PROCESS_ID))
                .thenReturn(new HearingNoticeVariables()
                        .setHearingStartDateTime(START_DATE_TIME_2));

        try (MockedStatic<HearingFeeUtils> mocked = mockStatic(HearingFeeUtils.class);
             MockedStatic<NotificationUtils> mockedUtils = mockStatic(NotificationUtils.class)) {
            mocked.when(() -> calculateAndApplyFee(hearingFeesService, caseData, caseData.getAssignedTrack()))
                    .thenReturn(null);
            mocked.when(() -> calculateHearingDueDate(any(LocalDate.class), eq(START_DATE_TIME_2.toLocalDate())))
                    .thenReturn(DUE_DATE_2);

            mockedUtils.when(() -> getApplicantLegalOrganizationName(eq(caseData), any()))
                .thenReturn("Applicant Org Ltd");

            Map<String, String> props = generator.addCustomProperties(new HashMap<>(), caseData);

            assertAll("default zero-fee properties",
                    () -> assertEquals("£0.00", props.get(HEARING_FEE)),
                    () -> assertEquals("05-01-2025", props.get(HEARING_DATE)),
                    () -> assertEquals("09:00am", props.get(HEARING_TIME)),
                    () -> assertEquals("20-01-2025", props.get(HEARING_DUE_DATE)),
                      () -> assertEquals("Applicant Org Ltd", props.get(CLAIM_LEGAL_ORG_NAME_SPEC))
            );
        }
    }
}
