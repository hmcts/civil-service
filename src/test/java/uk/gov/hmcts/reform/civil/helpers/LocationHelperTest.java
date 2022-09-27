package uk.gov.hmcts.reform.civil.helpers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;

import java.util.List;

public class LocationHelperTest {

    private final LocationHelper helper = new LocationHelper();

    @Test
    public void thereIsAMatchingLocation() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        List<LocationRefData> locations = List.of(LocationRefData.builder()
                                                      .courtLocationCode("123")
                                                      .regionId("regionId")
                                                      .region("region name")
                                                      .epimmsId("epimms")
                                                      .build());
        RequestedCourt requestedCourt = RequestedCourt.builder()
            .responseCourtCode("123")
            .requestHearingAtSpecificCourt(YesOrNo.YES)
            .build();
        helper.updateCaseManagementLocation(updatedData, requestedCourt, () -> locations);
        Assertions.assertThat(updatedData.build().getCaseManagementLocation())
            .isNotNull()
            .isEqualTo(CaseLocation.builder()
                           .region("regionId")
                           .baseLocation("epimms")
                           .build());
    }
}
