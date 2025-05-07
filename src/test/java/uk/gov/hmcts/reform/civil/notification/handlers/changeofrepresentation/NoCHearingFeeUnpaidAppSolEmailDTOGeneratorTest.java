package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NoCHearingFeeUnpaidAppSolEmailDTOGeneratorTest {

    private static final String SOLICITOR_EMAIL = "solicitor@example.com";
    private static final String TEMPLATE_ID = "hearing-fee-template-id";
    private static final String CASE_REFERENCE = "000DC123";
    private static final Map<String, String> EMAIL_PROPS = Map.of("feeAmount", "£300");

    private NoCHelper noCHelper;
    private NotificationsProperties notificationsProperties;
    private NoCHearingFeeUnpaidAppSolEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        noCHelper = mock(NoCHelper.class);
        notificationsProperties = mock(NotificationsProperties.class);
        generator = new NoCHearingFeeUnpaidAppSolEmailDTOGenerator(noCHelper, notificationsProperties);

        caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1234567890123456L)
            .hearingReferenceNumber("REFERENCE_NUM")
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .legacyCaseReference(CASE_REFERENCE)
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(SOLICITOR_EMAIL).build())
            .hearingFee(Fee.builder()
                            .calculatedAmountInPence(new BigDecimal("23212"))
                            .build())
            .build();
    }

    @Test
    void shouldNotify_WhenInHearingReadinessAndFeeUnpaidAndFeePresent() {
        when(noCHelper.isHearingFeePaid(caseData)).thenReturn(false);

        boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isTrue();
    }

    @Test
    void shouldNotNotify_WhenFeeIsPaid() {
        when(noCHelper.isHearingFeePaid(caseData)).thenReturn(true);

        boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldNotNotify_WhenHearingFeeIsNull() {
        caseData = CaseDataBuilder.builder()
            .hearingReferenceNumber("REFERENCE_NUM")
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .legacyCaseReference(CASE_REFERENCE)
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(SOLICITOR_EMAIL).build())
            .hearingFee(null)
            .build();

        when(noCHelper.isHearingFeePaid(caseData)).thenReturn(false);

        boolean shouldNotify = generator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldBuildEmailDTO_WhenConditionsMet() {
        when(noCHelper.isHearingFeePaid(caseData)).thenReturn(false);
        when(notificationsProperties.getHearingFeeUnpaidNoc()).thenReturn(TEMPLATE_ID);
        when(noCHelper.getHearingFeeEmailProperties(caseData)).thenReturn(EMAIL_PROPS);

        EmailDTO result = generator.buildEmailDTO(caseData);

        assertThat(result.getTargetEmail()).isEqualTo(SOLICITOR_EMAIL);
        assertThat(result.getEmailTemplate()).isEqualTo(TEMPLATE_ID);
        assertThat(result.getReference()).isEqualTo("notice-of-change-" + CASE_REFERENCE);
        assertThat(result.getParameters()).containsAllEntriesOf(EMAIL_PROPS);
    }
}
