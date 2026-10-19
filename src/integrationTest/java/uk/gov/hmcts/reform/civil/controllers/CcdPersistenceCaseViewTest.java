package uk.gov.hmcts.reform.civil.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Data-store loads decentralised CIVIL and GENERALAPPLICATION cases as this service stored them.
 */
@TestPropertySource(properties = "civil.decentralised.enabled=true")
public class CcdPersistenceCaseViewTest extends BaseIntegrationTest {

    private static final long CIVIL_CASE = 1700000000000001L;
    private static final long GA_CASE = 1700000000000002L;
    private static final String CIVIL_DATA = """
        {"legacyCaseReference": "000MC001", "notInCaseData": {"kept": ["as", "stored"]}, "totalClaimAmount": 1500.50}
        """;
    private static final String GA_DATA = """
        {"generalAppType": {"types": ["STRIKE_OUT"]}, "notInCaseData": true}
        """;

    @Autowired
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void storeCases() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("ccd_data");
        jdbcTemplate.update("delete from ccd.case_data where reference in (?, ?)", CIVIL_CASE, GA_CASE);
        store(1, CIVIL_CASE, "CIVIL", "CASE_ISSUED", CIVIL_DATA);
        store(2, GA_CASE, "GENERALAPPLICATION", "AWAITING_APPLICATION_PAYMENT", GA_DATA);
    }

    @Test
    @SneakyThrows
    public void returnsCasesAsStored() {
        String body = mockMvc.perform(get("/ccd-persistence/cases")
                                          .param("case-refs", String.valueOf(CIVIL_CASE), String.valueOf(GA_CASE))
                                          .header("Authorization", BEARER_TOKEN)
                                          .header("ServiceAuthorization", "s2s-token"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode cases = objectMapper.readTree(body);
        assertThat(cases).hasSize(2);
        assertThat(caseData(cases, CIVIL_CASE)).isEqualTo(objectMapper.readTree(CIVIL_DATA));
        assertThat(caseData(cases, GA_CASE)).isEqualTo(objectMapper.readTree(GA_DATA));
    }

    private void store(long id, long reference, String caseType, String state, String data) {
        // Data-store assigns the id it keeps for a decentralised case; the runtime stores it alongside.
        jdbcTemplate.update(
            """
                insert into ccd.case_data (id, reference, security_classification, jurisdiction, case_type_id, state,
                                           data)
                values (?, ?, 'PUBLIC'::ccd.securityclassification, 'CIVIL', ?, ?, ?::jsonb)
                """,
            id, reference, caseType, state, data
        );
    }

    private static JsonNode caseData(JsonNode cases, long reference) {
        for (JsonNode decentralisedCase : cases) {
            JsonNode caseDetails = decentralisedCase.path("case_details");
            if (caseDetails.path("id").asLong() == reference || caseDetails.path("reference").asLong() == reference) {
                return caseDetails.path("case_data");
            }
        }
        throw new AssertionError("No case " + reference + " in " + cases);
    }
}
