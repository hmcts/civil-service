package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.model.Address;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDataContentConverterTest {

    private static final String CIVIL_CASE_ID = "12341234";
    private static final String EVENT_ID = "1234";

    @Test
    void shouldConvertToUpdatedCaseWithoutDeletingNestedData() {
        Address app1Address = Address.builder()
            .addressLine1("123 Street")
            .postTown("City")
            .postCode("12345")
            .country("Country")
            .build();
        Address resp1Address = Address.builder()
            .addressLine1("456 Street")
            .postTown("City")
            .postCode("54321")
            .country("Country")
            .build();
        Map<String, Object> applicant1 =
            new HashMap<>(Map.of("firstName", "app1firstname", "lastName", "app1lastname", "email", "app1@test.com", "address", app1Address));
        Map<String, Object> respondent1 =
            new HashMap<>(Map.of("firstName", "resp1firstname", "lastName", "resp1lastname", "email", "resp1@test.com", "address", resp1Address));
        Map<String, Object> originalData =
            new HashMap<>(Map.of("caseId", CIVIL_CASE_ID, "applicant1", applicant1, "respondent1", respondent1));
        CaseDetails caseDetails = CaseDetails.builder().data(originalData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(EVENT_ID)
            .caseDetails(caseDetails)
            .build();

        Map<String, Object> updatedData = Map.of("respondent1", Map.of("firstName", "updatedfirstname", "lastName", "updatedlastname"));

        CaseDataContent converted = CaseDataContentConverter.caseDataContentFromStartEventResponse(startEventResponse, updatedData);
        Object convertData = converted.getData();
        assertThat(converted).isNotNull();

        Map<String, Object> expectedData =
            Map.of(
                "caseId", CIVIL_CASE_ID,
                "applicant1", applicant1,
                "respondent1",
                    Map.of(
                        "firstName", "updatedfirstname",
                        "lastName", "updatedlastname",
                        "email", "resp1@test.com",
                        "address", resp1Address));
        assertThat(convertData).isEqualTo(expectedData);
    }
}
