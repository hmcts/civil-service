package uk.gov.hmcts.reform.unspec.launchdarkly;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.unspec.service.OrganisationService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.launchdarkly.OnBoardingOrganisationControlService.ORG_NOT_ONBOARDED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    OnBoardingOrganisationControlService.class,
    JacksonAutoConfiguration.class
})
class OnBoardingOrganisationControlServiceTest {

    public static final String USER_TOKEN = "bearer:userToken";
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private OnBoardingOrganisationControlService onBoardingOrganisationControlService;

    @Test
    void shouldNotReturnError_whenOrganisationInAllowedList() {
        when(organisationService.findOrganisation(USER_TOKEN))
            .thenReturn(Optional.of(Organisation.builder().organisationIdentifier("0FA7S8S").build()));

        when(featureToggleService.isOrganisationOnboarded("0FA7S8S")).thenReturn(true);

        assertThat(onBoardingOrganisationControlService.validateOrganisation(USER_TOKEN)).isEmpty();
    }

    @Test
    void shouldReturnError_whenOrganisationNotInAllowedList() {
        String firm = "Solicitor tribunal ltd";
        when(organisationService.findOrganisation(USER_TOKEN))
            .thenReturn(Optional.of(Organisation.builder().name(firm).organisationIdentifier("0F99S99").build()));

        when(featureToggleService.isOrganisationOnboarded("0F99S99")).thenReturn(false);

        assertThat(onBoardingOrganisationControlService.validateOrganisation(USER_TOKEN))
            .contains(ORG_NOT_ONBOARDED);
    }
}
