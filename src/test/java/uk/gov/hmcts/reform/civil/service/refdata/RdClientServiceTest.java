package uk.gov.hmcts.reform.civil.service.refdata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private final String serviceAuth = "serviceAuth";
    private final String auth = "auth";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        court1 = LocationRefData.builder()
            .epimmsId("111")
            .courtName("London Court")
            .build();

        court2 = LocationRefData.builder()
            .epimmsId("222")
            .courtName("Bristol Court")
            .build();

        when(locationRefDataApiClient.getAllCivilCourtVenues(any(), any(), any(), any()))
            .thenReturn(List.of(court1, court2));
    }

    @Test
    void shouldReturnAllCivilCourts() {
        List<LocationRefData> result = rdClientService.fetchAllCivilCourts(serviceAuth, auth);
        assertThat(result).containsExactlyInAnyOrder(court1, court2);

        verify(locationRefDataApiClient, times(1))
            .getAllCivilCourtVenues(serviceAuth, auth, "10", "Court");
    }

    @Test
    void shouldCallApiEachTimeWithoutSpringCaching() {
        // In plain Mockito test, caching is not active
        rdClientService.fetchAllCivilCourts(serviceAuth, auth);
        rdClientService.fetchAllCivilCourts(serviceAuth, auth);

        // API is called twice because @Cacheable is not active without Spring context
        verify(locationRefDataApiClient, times(2))
            .getAllCivilCourtVenues(serviceAuth, auth, "10", "Court");
    }
}
