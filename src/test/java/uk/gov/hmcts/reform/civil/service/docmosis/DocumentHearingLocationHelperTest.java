package uk.gov.hmcts.reform.civil.service.docmosis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DocumentHearingLocationHelperTest {

    @InjectMocks
    private DocumentHearingLocationHelper hearingLocationHelper;
    @Mock
    private LocationRefDataService locationRefDataService;

    @Test
    void whenFormDefined_thenReturnForm() {
        String fromForm = "label from form";
        CaseData caseData = CaseData.builder().build();
        String authorisation = "authorisation";

        LocationRefData location1 = LocationRefData.builder().build();
        Mockito.when(locationRefDataService.getLocationMatchingLabel(fromForm, authorisation))
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
        Mockito.when(locationRefDataService.getLocationMatchingLabel(fromForm, authorisation))
            .thenReturn(Optional.empty());
        Mockito.when(locationRefDataService
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
        Mockito.when(locationRefDataService.getLocationMatchingLabel(fromForm, authorisation))
            .thenReturn(Optional.empty());
        Mockito.when(locationRefDataService
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

}
