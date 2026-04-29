package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_BUNDLE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_DOCUMENTS_HEADING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_NOTES_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLINICAL_PARTIES_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_ANSWERS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_PERMISSION_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_QUESTIONS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.PERSONAL_INJURY_UPLOAD;

@ExtendWith(MockitoExtension.class)
class DjClinicalDirectionsServiceTest {

    private DjClinicalDirectionsService service;

    @BeforeEach
    void setUp() {
        service = new DjClinicalDirectionsService();
    }

    @Test
    void shouldBuildClinicalNegligenceUsingSharedText() {
        TrialClinicalNegligence result = service.buildTrialClinicalNegligence();

        assertThat(result.getInput1()).isEqualTo(CLINICAL_DOCUMENTS_HEADING);
        assertThat(result.getInput2()).isEqualTo(CLINICAL_PARTIES_DJ);
        assertThat(result.getInput3()).isEqualTo(CLINICAL_NOTES_DJ);
        assertThat(result.getInput4()).isEqualTo(CLINICAL_BUNDLE_DJ);
    }

    @Test
    void shouldBuildPersonalInjuryWithExpectedDates() {
        TrialPersonalInjury injury = service.buildTrialPersonalInjury();

        assertNull(injury.getDate1());
        assertThat(injury.getDate2()).isEqualTo(LocalDate.now().plusWeeks(7));
        assertThat(injury.getInput1()).isEqualTo(PERSONAL_INJURY_PERMISSION_DJ);
        assertThat(injury.getInput2()).isEqualTo(PERSONAL_INJURY_QUESTIONS);
        assertThat(injury.getInput3()).isEqualTo(PERSONAL_INJURY_ANSWERS);
        assertThat(injury.getInput4()).isEqualTo(PERSONAL_INJURY_UPLOAD);
    }
}
