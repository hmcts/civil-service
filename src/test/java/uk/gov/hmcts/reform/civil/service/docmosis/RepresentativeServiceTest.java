package uk.gov.hmcts.reform.civil.service.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.prd.model.DxAddress;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    RepresentativeService.class})
@ExtendWith(SpringExtension.class)
class RepresentativeServiceTest {

    private final ContactInformation contactInformation = ContactInformation.builder()
        .addressLine1("address line 1")
        .addressLine2("address line 2")
        .addressLine3("address line 3")
        .postCode("SW1 1AA")
        .county("London")
        .country("UK")
        .dxAddress(List.of(DxAddress.builder().dxNumber("DX12345").build()))
        .build();
    private final Organisation organisation = Organisation.builder()
        .name("test org")
        .contactInformation(List.of(contactInformation))
        .build();

    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private RepresentativeService representativeService;

    @BeforeEach
    void setup() {
        given(organisationService.findOrganisationById(any())).willReturn(Optional.of(organisation));
    }

    @Nested
    class GetRespondentRepresentative {

        @Test
        void shouldReturnValidOrganisationDetails_whenStateFlowIsNotProceedsOfflineUnrepresentedDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseNotification().build();

            Representative representative = representativeService.getRespondentRepresentative(caseData);

            verify(organisationService).findOrganisationById(
                caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID());
            assertThat(representative).extracting("organisationName").isEqualTo(organisation.getName());
            assertThat(representative).extracting("dxAddress").isEqualTo(
                contactInformation.getDxAddress().get(0).getDxNumber());
            assertThat(representative).extracting("emailAddress").isEqualTo(
                caseData.getRespondentSolicitor1EmailAddress());
            assertThat(representative).extracting("serviceAddress").extracting(
                "AddressLine1",
                "AddressLine2",
                "AddressLine3",
                "County",
                "Country",
                "PostCode"
            ).containsExactly(
                contactInformation.getAddressLine1(),
                contactInformation.getAddressLine2(),
                contactInformation.getAddressLine3(),
                contactInformation.getCounty(),
                contactInformation.getCountry(),
                contactInformation.getPostCode()
            );
        }

        @Test
        void shouldReturnValidOrganisationDetails_whenStateFlowIsProceedsOfflineUnrepresentedDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant().build();
            var organisationDetails = caseData.getRespondentSolicitor1OrganisationDetails();
            var organisationAddress = organisationDetails.getAddress();

            Representative representative = representativeService.getRespondentRepresentative(caseData);

            verifyNoInteractions(organisationService);
            assertThat(representative).extracting(
                "organisationName", "phoneNumber", "dxAddress", "emailAddress").containsExactly(
                organisationDetails.getOrganisationName(),
                organisationDetails.getPhoneNumber(),
                organisationDetails.getDx(),
                organisationDetails.getEmail()
            );
            assertThat(representative).extracting("serviceAddress").extracting(
                "AddressLine1",
                "AddressLine2",
                "AddressLine3",
                "County",
                "Country",
                "PostCode"
            ).containsExactly(
                organisationAddress.getAddressLine1(),
                organisationAddress.getAddressLine2(),
                organisationAddress.getAddressLine3(),
                organisationAddress.getCounty(),
                organisationAddress.getCountry(),
                organisationAddress.getPostCode()
            );

        }

        @Test
        void shouldReturnEmptyRepresentative_whenNoRespondentSolicitor1OrganisationDetailsProvided() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant().build();
            var organisationDetails = caseData.getRespondentSolicitor1OrganisationDetails();
            var organisationAddress = organisationDetails.getAddress();

            Representative representative = representativeService.getRespondentRepresentative(caseData);

            verifyNoInteractions(organisationService);
            assertThat(representative).extracting(
                "organisationName", "phoneNumber", "dxAddress", "emailAddress").containsExactly(
                organisationDetails.getOrganisationName(),
                organisationDetails.getPhoneNumber(),
                organisationDetails.getDx(),
                organisationDetails.getEmail()
            );
            assertThat(representative).extracting("serviceAddress").extracting(
                "AddressLine1",
                "AddressLine2",
                "AddressLine3",
                "County",
                "Country",
                "PostCode"
            ).containsExactly(
                organisationAddress.getAddressLine1(),
                organisationAddress.getAddressLine2(),
                organisationAddress.getAddressLine3(),
                organisationAddress.getCounty(),
                organisationAddress.getCountry(),
                organisationAddress.getPostCode()
            );

        }
    }
}

