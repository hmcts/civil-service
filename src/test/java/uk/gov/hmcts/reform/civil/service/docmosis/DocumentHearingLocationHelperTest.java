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

        LocationRefData location1 = LocationRefData.builder().build();
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
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation("base location")
                                        .region("region")
                                        .build())
            .build();
        String authorisation = "authorisation";

        LocationRefData location1 = LocationRefData.builder()
            .regionId(caseData.getCaseManagementLocation().getRegion())
            .epimmsId(caseData.getCaseManagementLocation().getBaseLocation())
            .build();
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
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation("base location")
                                        .region("region")
                                        .build())
            .build();
        String authorisation = "authorisation";

        LocationRefData location1 = LocationRefData.builder()
            .regionId(caseData.getCaseManagementLocation().getRegion())
            .epimmsId(caseData.getCaseManagementLocation().getBaseLocation())
            .build();
        when(locationRefDataService.getLocationMatchingLabel(fromForm, authorisation))
            .thenReturn(Optional.empty());
        when(locationRefDataService
                         .getCourtLocationsByEpimmsIdAndCourtType(
                             authorisation,
                             caseData.getCaseManagementLocation().getBaseLocation()
                         )).thenReturn(List.of(location1,
                                               LocationRefData.builder()
                                                   .regionId("region 2")
                                                   .epimmsId("location 2")
                                                   .build()));
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
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation(ccmcEpimmId)
                                        .region("CCMCC region")
                                        .build())
            .build();

        LocationRefData location1 = LocationRefData.builder()
            .regionId(caseData.getCaseManagementLocation().getRegion())
            .epimmsId(caseData.getCaseManagementLocation().getBaseLocation())
            .build();

        when(locationRefDataService.getCcmccLocation(authorisation)).thenReturn(LocationRefData
                                                                                            .builder()
                                                                                            .epimmsId(ccmcEpimmId)
                                                                                            .regionId("CCMCC region")
                                                                                            .build());
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
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation(cnbcEpimmId)
                                        .region("CNBC region")
                                        .build())
            .build();

        LocationRefData location1 = LocationRefData.builder()
            .regionId(caseData.getCaseManagementLocation().getRegion())
            .epimmsId(caseData.getCaseManagementLocation().getBaseLocation())
            .build();

        when(locationRefDataService.getCnbcLocation(authorisation)).thenReturn(LocationRefData
                                                                                    .builder()
                                                                                    .epimmsId(cnbcEpimmId)
                                                                                    .regionId("CNBC region")
                                                                                    .build());
        LocationRefData returnedLocation = hearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);
        Assertions.assertEquals(returnedLocation, location1);
    }

    @Test
    void whenNotCnbcOrCcmccLocation_thenReturnListOfHearingCourts() {
        List<LocationRefData> locations = List.of(
            LocationRefData.builder().epimmsId("00001").courtLocationCode("00001")
                .siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
            LocationRefData.builder().epimmsId("00002").courtLocationCode("00002")
                .siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
            LocationRefData.builder().epimmsId("00003").courtLocationCode("00003")
                .siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
        );
        String authorisation = "authorisation";
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation("00002")
                                        .region("random region")
                                        .build())
            .build();

        LocationRefData location1 = LocationRefData.builder()
            .regionId(caseData.getCaseManagementLocation().getRegion())
            .epimmsId(caseData.getCaseManagementLocation().getBaseLocation())
            .build();

        when(locationRefDataService.getHearingCourtLocations(authorisation)).thenReturn(locations);
        LocationRefData returnedLocation = hearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);
        Assertions.assertEquals(returnedLocation.getEpimmsId(), location1.getEpimmsId());
    }

    @Test
    void whenNoCaseLocationFound_thenReturnException() {
        List<LocationRefData> locations = List.of(
            LocationRefData.builder().epimmsId("00001").courtLocationCode("00001")
                .siteName("court 1").courtAddress("1 address").postcode("Y01 7RB").build(),
            LocationRefData.builder().epimmsId("00002").courtLocationCode("00002")
                .siteName("court 2").courtAddress("2 address").postcode("Y02 7RB").build(),
            LocationRefData.builder().epimmsId("00003").courtLocationCode("00003")
                .siteName("court 3").courtAddress("3 address").postcode("Y03 7RB").build()
        );
        String authorisation = "authorisation";
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation("00009")
                                        .region("random region")
                                        .build())
            .build();

        when(locationRefDataService.getHearingCourtLocations(authorisation)).thenReturn(locations);
        assertThrows(IllegalArgumentException.class, () -> hearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation));
    }

}
