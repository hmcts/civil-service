package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class AmountIncludesTextParamsBuilderTest {

    private AmountIncludesTextParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new AmountIncludesTextParamsBuilder();
    }

    @Test
    void shouldAddParamsForFullAdmitPayImmediatelyClaimSpec() {
        // Arrange
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .build();
        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("amountIncludesTextEn",
            ". This amount includes interest if it has been claimed which will continue to accrue to the date of Judgment, settlement agreement or earlier payment");
        assertThat(params).containsEntry("amountIncludesTextCy",
            ". Mae'r swm hwn yn cynnwys llog os hawlir a fydd yn parhau i gronni hyd at ddyddiad y dyfarniad,"
                + " cytundeb setlo neu daliad cynharach");
    }

    @Test
    void shouldAddParamsForPartAdmitPayImmediatelyClaimSpec() {
        // Arrange
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .build();
        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("amountIncludesTextEn", " plus the claim fee and any fixed costs claimed");
        assertThat(params).containsEntry("amountIncludesTextCy", " ynghyd â ffi’r hawliad ac unrhyw gostau sefydlog a hawlir");
    }

    @Test
    void shouldNotAddParamsWhenConditionsAreNotMet() {
        // Arrange
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .build();
        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
