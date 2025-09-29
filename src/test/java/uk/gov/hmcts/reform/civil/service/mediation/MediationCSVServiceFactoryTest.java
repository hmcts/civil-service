package uk.gov.hmcts.reform.civil.service.mediation;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {
    MediationCSVLrvLipService.class,
    MediationCSVLrvLrService.class,
    MediationCSVLipVLipService.class,
    MediationCsvServiceFactory.class
})
public class MediationCSVServiceFactoryTest {

    @Mock
    private CaseData caseData;

    @MockitoBean
    private OrganisationService organisationService;

    @MockitoBean
    private FeatureToggleService toggleService;

    @Autowired
    private MediationCsvServiceFactory mediationCsvServiceFactory;

    @Test
    void shouldReturnMediationCSVLrvLrService_whenDefendantRepresented() {
        //Given
        given(caseData.isRespondent1LiP()).willReturn(false);
        //When
        MediationCSVService mediationCSVService = mediationCsvServiceFactory.getMediationCSVService(caseData);
        //Then
        assertThat(mediationCSVService).isInstanceOf(MediationCSVLrvLrService.class);
    }

    @Test
    void shouldReturnMediationCSVLrvLip_whenDefendantLip() {
        //Given
        given(caseData.isRespondent1LiP()).willReturn(true);
        //When
        MediationCSVService mediationCSVService = mediationCsvServiceFactory.getMediationCSVService(caseData);
        //Then
        assertThat(mediationCSVService).isInstanceOf(MediationCSVLrvLipService.class);
    }

    @Test
    void shouldReturnMediationCSVLipVLip_whenApplicantLip() {
        //Given
        given(toggleService.isLipVLipEnabled()).willReturn(true);
        given(caseData.isApplicantLiP()).willReturn(true);
        //When
        MediationCSVService mediationCSVService = mediationCsvServiceFactory.getMediationCSVService(caseData);
        //Then
        assertThat(mediationCSVService).isInstanceOf(MediationCSVLipVLipService.class);
    }
}

