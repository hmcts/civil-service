package uk.gov.hmcts.reform.civil.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The decentralised runtime's ccd-persistence endpoints answer only CCD data-store.
 */
@TestPropertySource(properties = "civil.decentralised.enabled=true")
public class CcdPersistenceAuthorisationTest extends BaseIntegrationTest {

    private static final String CASES = "/ccd-persistence/cases?case-refs=1111222233334444";

    @Autowired
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @Test
    @SneakyThrows
    public void answersDataStore() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("ccd_data");

        mockMvc.perform(get(CASES)
                            .header("Authorization", BEARER_TOKEN)
                            .header("ServiceAuthorization", "s2s-token"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    @SneakyThrows
    public void refusesAnotherService() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("civil-citizen-ui");

        mockMvc.perform(get(CASES)
                            .header("Authorization", BEARER_TOKEN)
                            .header("ServiceAuthorization", "s2s-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    public void refusesAUserWithoutAServiceToken() {
        mockMvc.perform(get(CASES).header("Authorization", BEARER_TOKEN))
            .andExpect(status().isForbidden());
    }
}
