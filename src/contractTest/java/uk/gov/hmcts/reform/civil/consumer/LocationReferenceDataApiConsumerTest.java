package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "referenceData_location")
@MockServerConfig(hostInterface = "localhost", port = "6669")
public class LocationReferenceDataApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/refdata/location/court-venues";
    private static final String CIVIL_SPEC_SERVICE_ID = "AAA6";
    private static final String CIVIL_COURT_TYPE_ID = "10";
    private static final String CIVIL_UN_SPEC_SERVICE_ID = "AAA7";
    private static final String SERVICE_ID_REGEX = "AAA6|AAA7";
    private static final String LOCATION_TYPE = "Court";

    @Autowired
    private LocationReferenceDataApiClient locationReferenceDataApiClient;

    @Pact(consumer = "civil_service")
    public RequestResponsePact getAllCivilCourtVenuesForSpec(PactDslWithProvider builder)
        throws JSONException {
        return buildCourtVenueResponsePact(builder, CIVIL_SPEC_SERVICE_ID);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getAllCivilCourtVenuesForUnSpec(PactDslWithProvider builder)
        throws JSONException {
        return buildCourtVenueResponsePact(builder, CIVIL_UN_SPEC_SERVICE_ID);
    }

    @Test
    @PactTestFor(pactMethod = "getAllCivilCourtVenuesForSpec")
    public void verifyCivilSpecCourtVenue() {
        verifyCourtVenue(CIVIL_SPEC_SERVICE_ID);
    }

    @Test
    @PactTestFor(pactMethod = "getAllCivilCourtVenuesForUnSpec")
    public void verifyCivilUnSpecCourtVenue() {
        verifyCourtVenue(CIVIL_UN_SPEC_SERVICE_ID);
    }

    private void verifyCourtVenue(String serviceId) {
        List<LocationRefData> response = locationReferenceDataApiClient.getAllCivilCourtVenuesByServiceId(
            SERVICE_AUTH_TOKEN,
            AUTHORIZATION_TOKEN,
            CIVIL_COURT_TYPE_ID,
            LOCATION_TYPE,
            serviceId
        );
        assertThat(
            response.getFirst().getRegion(),
            is(equalTo("regionTest123"))
        );
    }

    private RequestResponsePact buildCourtVenueResponsePact(PactDslWithProvider builder, String serviceId)  {
        return builder
            .given("There are court locations to be returned")
            .uponReceiving("a location request")
            .path(ENDPOINT)
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.GET.toString())
            .matchQuery("court_type_id", "\\d+", CIVIL_COURT_TYPE_ID)
            .matchQuery("location_type", ".+", LOCATION_TYPE)
            .matchQuery("service_code", SERVICE_ID_REGEX, serviceId)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildLocationRefDataResponseBody(serviceId))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    static DslPart buildLocationRefDataResponseBody(String serviceId) {
        return newJsonArray(response ->
                                response
                                    .object(locationRefData -> locationRefData
                                        .stringType("court_venue_id", "12345")
                                        .stringType("service_code", serviceId)
                                        .stringType("epimms_id", "epimmsIdTest123")
                                        .stringType("site_name", "siteNameTest123")
                                        .stringType("region_id", "regionIdTest123")
                                        .stringType("region", "regionTest123")
                                        .stringType("court_type", "courtTypeTest123")
                                        .stringType("court_type_id", "courtTypeIdTest123")
                                        .stringType("court_address", "courtAddressTest123")
                                        .stringType("postcode", "postcodeTest123")
                                        .stringType("phone_number", "phoneNumberTest123")
                                        .stringType("court_location_code", "courtLocationCodeTest123")
                                        .stringType("court_status", "courtStatusTest123")
                                        .stringType("court_name", "courtNameTest123")
                                        .stringType("venue_name", "venueNameTest123")
                                        .stringType("location_type", "locationTypeTest123")
                                        .stringType("parent_location", "parentLocationTest123"))
        ).build();
    }
}
