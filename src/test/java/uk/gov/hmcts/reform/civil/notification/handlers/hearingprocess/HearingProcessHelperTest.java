package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.getHearingFeePropertiesIfNotPaid;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.getRespSolOneReference;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.getRespSolTwoReference;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.isNoFeeDue;

public class HearingProcessHelperTest {

    @Test
    void shouldReturnNoFeeDueTrueWhenPaymentStatusIsSuccess() {
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.SUCCESS).build())
            .build();

        assertTrue(isNoFeeDue(caseData));
    }

    @Test
    void shouldReturnNoFeeDueTrueWhenHearingNoticeListIsOther() {
        CaseData caseData = CaseData.builder()
            .hearingNoticeList(HearingNoticeList.OTHER)
            .build();

        assertTrue(isNoFeeDue(caseData));
    }

    @Test
    void shouldReturnNoFeeDueTrueWhenListingIsRelisting() {
        CaseData caseData = CaseData.builder()
            .listingOrRelisting(ListingOrRelisting.RELISTING)
            .build();

        assertTrue(isNoFeeDue(caseData));
    }

    @Test
    void shouldReturnNoFeeDueFalseWhenNoConditionsMet() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(isNoFeeDue(caseData));
    }

    @Test
    void shouldReturnEmptyMapWhenFeeIsPaid() {
        CaseData caseData = CaseData.builder()
            .hearingFeePaymentDetails(PaymentDetails.builder().status(PaymentStatus.SUCCESS).build())
            .build();

        assertTrue(getHearingFeePropertiesIfNotPaid(caseData).isEmpty());
    }

    @Test
    void shouldReturnPropertiesWhenFeeNotPaid() {
        LocalDate dueDate = LocalDate.of(2025, 7, 10);
        Fee fee = Fee.builder().calculatedAmountInPence(new BigDecimal("1000")).build();

        MockedStatic<NotificationUtils> notificationUtilsMockedStatic = Mockito.mockStatic(NotificationUtils.class);
        notificationUtilsMockedStatic.when(() -> NotificationUtils.getFormattedHearingDate(dueDate)).thenReturn(
            "10 July 2025");

        CaseData caseData = CaseData.builder()
            .hearingDueDate(dueDate)
            .hearingFee(fee)
            .hearingFeePaymentDetails(null)
            .build();

        Map<String, String> props = getHearingFeePropertiesIfNotPaid(caseData);

        assertEquals("£10.00", props.get(HEARING_FEE));
        assertEquals("10 July 2025", props.get(HEARING_DUE_DATE));
    }

    @Test
    void shouldReturnZeroFeeWhenFeeIsNull() {
        CaseData caseData = CaseData.builder().hearingFeePaymentDetails(null).build();

        Map<String, String> props = getHearingFeePropertiesIfNotPaid(caseData);

        assertEquals("£0.00", props.get(HEARING_FEE));
    }

    @Test
    void shouldReturnEmptyAppSolReferenceWhenNull() {
        CaseData caseData = CaseData.builder().build();

        assertEquals("", HearingProcessHelper.getAppSolReference(caseData));
    }

    @Test
    void shouldReturnAppSolReferenceWhenPresent() {
        SolicitorReferences refs = SolicitorReferences.builder()
            .applicantSolicitor1Reference("APP-REF")
            .build();
        CaseData caseData = CaseData.builder()
            .solicitorReferences(refs)
            .build();

        assertEquals("APP-REF", HearingProcessHelper.getAppSolReference(caseData));
    }

    @Test
    void shouldReturnEmptyRespSolOneReferenceWhenNull() {
        CaseData caseData = CaseData.builder().build();

        assertEquals("", getRespSolOneReference(caseData));
    }

    @Test
    void shouldReturnRespSolOneReferenceWhenPresent() {
        SolicitorReferences refs = SolicitorReferences.builder()
            .respondentSolicitor1Reference("RESP-REF")
            .build();
        CaseData caseData = CaseData.builder()
            .solicitorReferences(refs)
            .build();

        assertEquals("RESP-REF", getRespSolOneReference(caseData));
    }

    @Test
    void shouldReturnEmptyRespSolTwoReferenceWhenNull() {
        CaseData caseData = CaseData.builder().build();

        assertEquals("", getRespSolTwoReference(caseData));
    }

    @Test
    void shouldReturnRespSolTwoReferenceWhenPresent() {
        CaseData caseData = CaseData.builder()
            .respondentSolicitor2Reference("RESP2-REF")
            .build();

        assertEquals("RESP2-REF", getRespSolTwoReference(caseData));
    }
}
