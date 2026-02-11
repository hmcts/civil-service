package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EvidenceDetailsTest {

    private static final String PHOTO_EVIDENCE = "photo evidence";
    private static final String WITNESS_EVIDENCE = "witness evidence";

    @Test
    void shouldReturnPhotoEvidenceWhenItsNotNull() {
        //Given
        EvidenceDetails evidenceDetails = new EvidenceDetails().setPhotoEvidence(PHOTO_EVIDENCE);
        //When
        String description = evidenceDetails.getEvidenceDescription();
        //Then
        assertThat(description).isEqualTo(PHOTO_EVIDENCE);
    }

    @Test
    void shouldReturnExpertWitnessEvidenceWhenItsNotNull() {
        //Given
        EvidenceDetails evidenceDetails = new EvidenceDetails().setExpertWitnessEvidence(WITNESS_EVIDENCE);
        //When
        String description = evidenceDetails.getEvidenceDescription();
        //Then
        assertThat(description).isEqualTo(WITNESS_EVIDENCE);
    }

    @Test
    void shouldREturnEmptyStringWhenNoEvidenceFieldsArePresent() {
        //Given
        EvidenceDetails evidenceDetails = new EvidenceDetails();
        //When
        String description = evidenceDetails.getEvidenceDescription();
        //Then
        assertThat(description).isEmpty();
    }

}
