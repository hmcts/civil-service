package uk.gov.hmcts.reform.civil.model.docmosis.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceType.CONTRACTS_AND_AGREEMENTS;

class EvidenceTemplateDataTest {

    @Test
    void shouldDisplayStringValueOfTypeSuccessfullyWhenItExists() {
        //Given
        String enumValue = "CONTRACTS_AND_AGREEMENTS";
        String expectedValue = CONTRACTS_AND_AGREEMENTS.getDisplayValue();
        EvidenceTemplateData evidenceTemplateData = new EvidenceTemplateData(enumValue, "some explanation");
        //When
        String resultValue = evidenceTemplateData.getDisplayTypeValue();
        //Then
        assertThat(resultValue).isEqualTo(expectedValue);
    }

    @Test
    void shouldDisplayEmptyStringWhenTypeIsNull() {
        //Given
        EvidenceTemplateData evidenceTemplateData = new EvidenceTemplateData(null, "some explanation");
        //When
        String resultValue = evidenceTemplateData.getDisplayTypeValue();
        //Then
        assertThat(resultValue).isEmpty();
    }

    @Test
    void shouldDisplayActualTypeValueWhenNoEnumTypeMatched() {
        //Given
        String enumValue = "ABRA_CADABRA";
        EvidenceTemplateData evidenceTemplateData = new EvidenceTemplateData(enumValue, "some explanation");
        //When
        String resultValue = evidenceTemplateData.getDisplayTypeValue();
        //Then
        assertThat(resultValue).isEqualTo(enumValue);
    }

}
