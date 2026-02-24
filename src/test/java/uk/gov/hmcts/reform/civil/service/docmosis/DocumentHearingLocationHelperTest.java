package uk.gov.hmcts.reform.civil.service.docmosis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {
    DocumentHearingLocationHelper.class})
public class DocumentHearingLocationHelperTest {

    @Autowired
    private DocumentHearingLocationHelper hearingLocationHelper;
    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @Test
    void whenFormDefined_thenReturnForm() {
        String fromForm = "label from form";
        CaseData caseData = CaseData.builder().build();
        String authorisation = "authorisation";

        LocationRefData location1 = new LocationRefData();
        when(locationRefDataService.getLocationMatchingLabel(fromForm, authorisation))
            .thenReturn(Optional.of(location1));
        LocationRefData location = hearingLocationHelper.getHearingLocation(
            fromForm,
            caseData,
            authorisation
        );

        Assertions.assertEquals(location, location1);
    }

    @Test
    void whenNotMatchingForm_thenDefaultToCaseLocation() {
        String fromForm = "label from form";
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation("base location")
                                        .setRegion("region")
                                        )
            .build();
        String authorisation = "authorisation";

        LocationRefData location1 = new LocationRefData()
            .setRegionId(caseData.getCaseManagementLocation().getRegion())
            .setEpimmsId(caseData.getCaseManagementLocation().getBaseLocation());
        when(locationRefDataService.getLocationMatchingLabel(fromForm, authorisation))
            .thenReturn(Optional.empty());
        when(locationRefDataService
                         .getCourtLocationsByEpimmsIdAndCourtType(
                             authorisation,
                             caseData.getCaseManagementLocation().getBaseLocation()
                         )).thenReturn(Collections.singletonList(location1));
        LocationRefData location = hearingLocationHelper.getHearingLocation(
            fromForm,
            caseData,
            authorisation
        );

        Assertions.assertEquals(location, location1);
    }

    @Test
    void whenSeveralLocations_thenDefaultToFirst() {
        String fromForm = "label from form";
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation("base location")
                                        .setRegion("region")
                                        )
            .build();
        String authorisation = "authorisation";

        LocationRefData location1 = new LocationRefData()
            .setRegionId(caseData.getCaseManagementLocation().getRegion())
            .setEpimmsId(caseData.getCaseManagementLocation().getBaseLocation());
        when(locationRefDataService.getLocationMatchingLabel(fromForm, authorisation))
            .thenReturn(Optional.empty());
        when(locationRefDataService
                         .getCourtLocationsByEpimmsIdAndCourtType(
                             authorisation,
                             caseData.getCaseManagementLocation().getBaseLocation()
                         )).thenReturn(List.of(location1,
                                               new LocationRefData()
                                                   .setRegionId("region 2")
                                                   .setEpimmsId("location 2")
                                                   ));
        LocationRefData location = hearingLocationHelper.getHearingLocation(
            fromForm,
            caseData,
            authorisation
        );

        Assertions.assertEquals(location, location1);
    }

    @Test
    void whenCcmccLocation_thenReturnCcmcDetails() {
        String authorisation = "authorisation";
        String ccmcEpimmId = hearingLocationHelper.ccmccEpimmId;
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(ccmcEpimmId)
                                        .setRegion("CCMCC region")
                                        )
            .build();

        LocationRefData location1 = new LocationRefData()
            .setRegionId(caseData.getCaseManagementLocation().getRegion())
            .setEpimmsId(caseData.getCaseManagementLocation().getBaseLocation());

        when(locationRefDataService.getCcmccLocation(authorisation)).thenReturn(new LocationRefData()
                                                                                            .setEpimmsId(ccmcEpimmId)
                                                                                            .setRegionId("CCMCC region"));
        LocationRefData returnedLocation = hearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);
        Assertions.assertEquals(returnedLocation, location1);
    }

    @Test
    void whenCnbcLocation_thenReturnCnbcDetails() {
        String authorisation = "authorisation";
        String cnbcEpimmId = hearingLocationHelper.cnbcEpimmId;
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(cnbcEpimmId)
                                        .setRegion("CNBC region")
                                        )
            .build();

        LocationRefData location1 = new LocationRefData()
            .setRegionId(caseData.getCaseManagementLocation().getRegion())
            .setEpimmsId(caseData.getCaseManagementLocation().getBaseLocation());

        when(locationRefDataService.getCnbcLocation(authorisation)).thenReturn(new LocationRefData()
                                                                                    .setEpimmsId(cnbcEpimmId)
                                                                                    .setRegionId("CNBC region"));
        LocationRefData returnedLocation = hearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);
        Assertions.assertEquals(returnedLocation, location1);
    }

    @Test
    void whenNotCnbcOrCcmccLocation_thenReturnListOfHearingCourts() {
        List<LocationRefData> locations = List.of(
            new LocationRefData().setEpimmsId("00001").setCourtLocationCode("00001")
                .setSiteName("court 1").setCourtAddress("1 address").setPostcode("Y01 7RB"),
            new LocationRefData().setEpimmsId("00002").setCourtLocationCode("00002")
                .setSiteName("court 2").setCourtAddress("2 address").setPostcode("Y02 7RB"),
            new LocationRefData().setEpimmsId("00003").setCourtLocationCode("00003")
                .setSiteName("court 3").setCourtAddress("3 address").setPostcode("Y03 7RB")
        );
        String authorisation = "authorisation";
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation("00002")
                                        .setRegion("random region")
                                        )
            .build();

        LocationRefData location1 = new LocationRefData()
            .setRegionId(caseData.getCaseManagementLocation().getRegion())
            .setEpimmsId(caseData.getCaseManagementLocation().getBaseLocation());

        when(locationRefDataService.getHearingCourtLocations(authorisation)).thenReturn(locations);
        LocationRefData returnedLocation = hearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);
        Assertions.assertEquals(returnedLocation.getEpimmsId(), location1.getEpimmsId());
    }

    @Test
    void whenNoCaseLocationFound_thenReturnException() {
        List<LocationRefData> locations = List.of(
            new LocationRefData().setEpimmsId("00001").setCourtLocationCode("00001")
                .setSiteName("court 1").setCourtAddress("1 address").setPostcode("Y01 7RB"),
            new LocationRefData().setEpimmsId("00002").setCourtLocationCode("00002")
                .setSiteName("court 2").setCourtAddress("2 address").setPostcode("Y02 7RB"),
            new LocationRefData().setEpimmsId("00003").setCourtLocationCode("00003")
                .setSiteName("court 3").setCourtAddress("3 address").setPostcode("Y03 7RB")
        );
        String authorisation = "authorisation";
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation("00009")
                                        .setRegion("random region")
                                        )
            .build();

        when(locationRefDataService.getHearingCourtLocations(authorisation)).thenReturn(locations);
        assertThrows(IllegalArgumentException.class, () -> hearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation));
    }

}
