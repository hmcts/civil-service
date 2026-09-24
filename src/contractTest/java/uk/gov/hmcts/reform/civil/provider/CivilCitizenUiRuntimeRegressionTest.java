package uk.gov.hmcts.reform.civil.provider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CivilCitizenUiRuntimeRegressionTest {

    private final CivilCitizenUiProviderSupport target = new CivilCitizenUiProviderSupport() { };

    @BeforeEach
    void setUp() {
        target.beforeEach(null);
    }

    @AfterEach
    void tearDown() throws Exception {
        target.tearDown();
    }

    @Test
    void claimPaymentUsesRuntimeIsoDate() throws Exception {
        target.claimIssuePaymentExists();
        target.mockMvc.perform(post("/fees/CLAIMISSUED/case/1234567890123456/payment")
                                   .header("Authorization", "Bearer some-access-token")
                                   .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.dateCreated").value("2023-11-27T13:15:06.313Z"));
    }

    @Test
    void gaPaymentUsesRuntimeIsoDate() throws Exception {
        target.claimIssuePaymentExistsForGeneralApplication();
        target.mockMvc.perform(post("/fees/case/1234567890123456/ga/payment")
                                   .header("Authorization", "Bearer some-access-token")
                                   .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dateCreated").value("2023-11-27T13:15:06.313Z"));
    }

    @Test
    void missingAuthorizationUsesControllerAdvice() throws Exception {
        target.mockMvc.perform(get("/fees/CLAIMISSUED/case/1234567890123456/payment/RC-1701-0909-0602-0418/status"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void incompatibleAcceptHeaderIsNotOverwritten() throws Exception {
        target.mockMvc.perform(get("/fees/CLAIMISSUED/case/1234567890123456/payment/RC-1701-0909-0602-0418/status")
                                   .header("Authorization", "Bearer some-access-token")
                                   .accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isNotAcceptable());
    }
}
