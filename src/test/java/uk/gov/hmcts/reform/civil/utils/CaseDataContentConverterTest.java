package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.model.Address;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class CaseDataContentConverterTest {

    private static final String CIVIL_CASE_ID = "12341234";
    private static final String EVENT_ID = "1234";
    private static final String TOKEN = "test-token";

    @Test
    void constructor_ShouldBePrivate() throws NoSuchMethodException {
        Constructor<CaseDataContentConverter> constructor = CaseDataContentConverter.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

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
            .token(TOKEN)
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
        assertThat(converted.getEventToken()).isEqualTo(TOKEN);
        assertThat(converted.getEvent().getId()).isEqualTo(EVENT_ID);
    }

    @Test
    void shouldAddNewFieldsWhenNotPresent() {
        Map<String, Object> originalData = new HashMap<>(Map.of("caseId", CIVIL_CASE_ID));
        CaseDetails caseDetails = CaseDetails.builder().data(originalData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(EVENT_ID)
            .token(TOKEN)
            .caseDetails(caseDetails)
            .build();

        Map<String, Object> newData = Map.of("newField", "newValue", "anotherField", 123);

        CaseDataContent converted = CaseDataContentConverter.caseDataContentFromStartEventResponse(
            startEventResponse,
            newData
        );
        Map<String, Object> resultData = (Map<String, Object>) converted.getData();

        assertThat(resultData).containsEntry("caseId", CIVIL_CASE_ID)
            .containsEntry("newField", "newValue")
            .containsEntry("anotherField", 123);
    }

    @Test
    void shouldReplaceNonMapValues() {
        Map<String, Object> originalData = new HashMap<>(Map.of(
            "field1", "originalValue",
            "field2", 100
        ));
        CaseDetails caseDetails = CaseDetails.builder().data(originalData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(EVENT_ID)
            .token(TOKEN)
            .caseDetails(caseDetails)
            .build();

        Map<String, Object> updatedData = Map.of(
            "field1", "updatedValue",
            "field2", 200
        );

        CaseDataContent converted = CaseDataContentConverter.caseDataContentFromStartEventResponse(
            startEventResponse,
            updatedData
        );
        Map<String, Object> resultData = (Map<String, Object>) converted.getData();

        assertThat(resultData).containsEntry("field1", "updatedValue")
            .containsEntry("field2", 200);
    }

    @Test
    void shouldHandleEmptyModifiedContent() {
        Map<String, Object> originalData = new HashMap<>(Map.of("caseId", CIVIL_CASE_ID));
        CaseDetails caseDetails = CaseDetails.builder().data(originalData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(EVENT_ID)
            .token(TOKEN)
            .caseDetails(caseDetails)
            .build();

        Map<String, Object> emptyData = new HashMap<>();

        CaseDataContent converted = CaseDataContentConverter.caseDataContentFromStartEventResponse(
            startEventResponse,
            emptyData
        );
        Map<String, Object> resultData = (Map<String, Object>) converted.getData();

        assertThat(resultData).containsEntry("caseId", CIVIL_CASE_ID)
            .hasSize(1);
    }

    @Test
    void shouldHandleDeeplyNestedMaps() {
        Map<String, Object> level3Original = new HashMap<>();
        level3Original.put("value", "original");

        Map<String, Object> level2Original = new HashMap<>();
        level2Original.put("level3", level3Original);

        Map<String, Object> level1Original = new HashMap<>();
        level1Original.put("level2", level2Original);

        Map<String, Object> originalData = new HashMap<>();
        originalData.put("level1", level1Original);

        CaseDetails caseDetails = CaseDetails.builder().data(originalData).build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(EVENT_ID)
            .token(TOKEN)
            .caseDetails(caseDetails)
            .build();

        Map<String, Object> updatedData = Map.of(
            "level1", Map.of(
                "level2", Map.of(
                    "level3", Map.of("value", "updated")
                )
            )
        );

        CaseDataContent converted = CaseDataContentConverter.caseDataContentFromStartEventResponse(startEventResponse, updatedData);
        Map<String, Object> resultData = (Map<String, Object>) converted.getData();

        Map<String, Object> level1 = (Map<String, Object>) resultData.get("level1");
        Map<String, Object> level2 = (Map<String, Object>) level1.get("level2");
        Map<String, Object> level3 = (Map<String, Object>) level2.get("level3");

        assertThat(level3).containsEntry("value", "updated");
    }
}
