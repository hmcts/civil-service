package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.io.IOException;
import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@PactTestFor(providerName = "referenceData_location")
@MockServerConfig(hostInterface = "localhost", port = "6669")
public class LocationReferenceDataApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/refdata/location/court-venues";

    @Autowired
    private LocationReferenceDataApiClient locationReferenceDataApiClient;

    @Pact(consumer = "civil_service")
    public RequestResponsePact getAllCivilCourtVenuesPact(PactDslWithProvider builder) throws IOException {
        return builder
            .given("Court locations exist")
            .uponReceiving("A request for all civil court venues")
            .path(ENDPOINT)
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.GET.toString())
            .matchQuery("court_type_id", "10", "10")
            .matchQuery("location_type", "Court", "Court")
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildLocationRefDataResponseBody())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getAllCivilCourtVenuesPact")
    public void verifyGetAllCivilCourtVenues() {
        List<LocationRefData> response = locationReferenceDataApiClient.getAllCivilCourtVenues(
            SERVICE_AUTH_TOKEN,
            AUTHORIZATION_TOKEN,
            "10",
            "Court"
        );

        assertThat(response.size(), is(1));
        assertThat(response.get(0).getRegion(), is(equalTo("regionTest123")));
        assertThat(response.get(0).getCourtVenueId(), is(equalTo("courtVenueId123")));
    }

    private static DslPart buildLocationRefDataResponseBody() {
        return newJsonArray(arr -> arr
            .object(locationRefData -> locationRefData
                .stringType("courtVenueId", "courtVenueId123")
                .stringType("epimmsId", "epimmsIdTest123")
                .stringType("siteName", "siteNameTest123")
                .stringType("regionId", "regionIdTest123")
                .stringType("region", "regionTest123")
                .stringType("courtType", "courtTypeTest123")
                .stringType("courtTypeId", "courtTypeIdTest123")
                .stringType("courtAddress", "courtAddressTest123")
                .stringType("postcode", "postcodeTest123")
                .stringType("phoneNumber", "phoneNumberTest123")
                .stringType("courtLocationCode", "courtLocationCodeTest123")
                .stringType("courtStatus", "courtStatusTest123")
                .stringType("courtName", "courtNameTest123")
                .stringType("venueName", "venueNameTest123")
                .stringType("locationType", "locationTypeTest123")
                .stringType("parentLocation", "parentLocationTest123")
            )
        ).build();
    }
}
