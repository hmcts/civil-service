package uk.gov.hmcts.reform.civil.service.refdata;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.client.LocationReferenceDataApiClient;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RdClientServiceTest {

    @Mock
    private LocationReferenceDataApiClient locationRefDataApiClient;

    @InjectMocks
    private RdClientService rdClientService;

    private LocationRefData court1;
    private LocationRefData court2;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        court1 = new LocationRefData()
            .setEpimmsId("111")
            .setCourtName("London Court");

        court2 = new LocationRefData()
            .setEpimmsId("222")
            .setCourtName("Bristol Court");

        when(locationRefDataApiClient.getAllCivilCourtVenuesByServiceId(any(), any(), any(), any(), any()))
            .thenReturn(List.of(court1, court2));
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @ParameterizedTest()
    @ValueSource(strings = {"AAA6", "AAA7"})
    void shouldReturnAllCivilCourtsByServiceId(String serviceId) {
        final String serviceAuth = "serviceAuth";
        final String auth = "auth";

        List<LocationRefData> result = rdClientService.fetchAllCivilCourtsByServiceId(serviceAuth, auth, serviceId);
        assertThat(result).containsExactlyInAnyOrder(court1, court2);

        verify(locationRefDataApiClient, times(1))
            .getAllCivilCourtVenuesByServiceId(serviceAuth, auth, "10", "Court", serviceId);
    }
}
