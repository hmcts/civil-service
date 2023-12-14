package uk.gov.hmcts.reform.civil.controllers.locations;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocationControllerTest extends BaseIntegrationTest {

    @MockBean
    private CourtLocationUtils courtLocationUtils;

    @Test
    @SneakyThrows
    void shouldReturnCourtLocationsMVC() {
        DynamicListElement dynamicListElement = DynamicListElement.builder().label("test_label").build();
        DynamicList dynamicList = DynamicList.builder().listItems(Collections.singletonList(dynamicListElement))
            .build();
        when(courtLocationUtils.getLocationsFromList(any())).thenReturn(dynamicList);

        doGet(BEARER_TOKEN, "/locations/courtLocations")
            .andExpect(content().json(toJson(dynamicList.getListItems())))
            .andExpect(status().isOk());
    }
}
