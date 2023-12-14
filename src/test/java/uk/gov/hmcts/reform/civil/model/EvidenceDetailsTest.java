package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EvidenceDetailsTest {

    private static final String PHOTO_EVIDENCE = "photo evidence";
    private static final String WITNESS_EVIDENCE = "witness evidence";

    @Test
    void shouldReturnPhotoEvidenceWhenItsNotNull() {
        //Given
        EvidenceDetails evidenceDetails = EvidenceDetails.builder().photoEvidence(PHOTO_EVIDENCE).build();
        //When
        String description = evidenceDetails.getEvidenceDescription();
        //Then
        assertThat(description).isEqualTo(PHOTO_EVIDENCE);
    }

    @Test
    void shouldReturnExpertWitnessEvidenceWhenItsNotNull() {
        //Given
        EvidenceDetails evidenceDetails = EvidenceDetails.builder().expertWitnessEvidence(WITNESS_EVIDENCE).build();
        //When
        String description = evidenceDetails.getEvidenceDescription();
        //Then
        assertThat(description).isEqualTo(WITNESS_EVIDENCE);
    }

    @Test
    void shouldREturnEmptyStringWhenNoEvidenceFieldsArePresent() {
        //Given
        EvidenceDetails evidenceDetails = EvidenceDetails.builder().build();
        //When
        String description = evidenceDetails.getEvidenceDescription();
        //Then
        assertThat(description).isEmpty();
    }

}
