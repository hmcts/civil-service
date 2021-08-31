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
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.prd.model.DxAddress;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    private final ContactInformation applicantContactInformation = ContactInformation.builder()
        .addressLine1("A address line 1")
        .addressLine2("A address line 2")
        .addressLine3("A address line 3")
        .postCode("SW1 1AA A")
        .county("London A")
        .country("UK A")
        .dxAddress(List.of(DxAddress.builder().dxNumber("DX12345A").build()))
        .build();
    private final ContactInformation respondentContactInformation = ContactInformation.builder()
        .addressLine1("R address line 1")
        .addressLine2("R address line 2")
        .addressLine3("R address line 3")
        .postCode("SW1 1AA R")
        .county("London R")
        .country("UK A")
        .dxAddress(List.of(DxAddress.builder().dxNumber("DX12345R").build()))
        .build();
    private final Address respondentSolicitorServiceAddress = Address.builder()
        .addressLine1("RS service address 1")
        .addressLine2("RS service address 2")
        .addressLine3("RS service address 3")
        .postCode("SW1 1AA RS")
        .county("London RS")
        .country("UK RS")
        .build();
    private final Address applicantSolicitorServiceAddress = Address.builder()
        .addressLine1("AS service address 1")
        .addressLine2("AS service address 2")
        .addressLine3("AS service address 3")
        .postCode("SW1 1AA AS")
        .county("London AS")
        .country("UK AS")
        .build();
    private final Organisation applicantOrganisation = Organisation.builder()
        .name("test applicant org")
        .contactInformation(List.of(applicantContactInformation))
        .build();
    private final Organisation respondentOrganisation = Organisation.builder()
        .name("test respondent org")
        .contactInformation(List.of(respondentContactInformation))
        .build();

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private RepresentativeService representativeService;

    @BeforeEach
    void setup() {
        given(organisationService.findOrganisationById("QWERTY A")).willReturn(Optional.of(applicantOrganisation));
        given(organisationService.findOrganisationById("QWERTY R")).willReturn(Optional.of(respondentOrganisation));
    }

    @Nested
    class GetRespondentRepresentative {

        @Test
        void shouldReturnValidOrganisationDetails_whenDefendantIsRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

            Representative representative = representativeService.getRespondent1Representative(caseData);

            verify(organisationService).findOrganisationById(
                caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID());
            assertThat(representative).extracting("organisationName").isEqualTo(respondentOrganisation.getName());
            assertThat(representative).extracting("dxAddress").isEqualTo(
                respondentContactInformation.getDxAddress().get(0).getDxNumber());
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
                respondentContactInformation.getAddressLine1(),
                respondentContactInformation.getAddressLine2(),
                respondentContactInformation.getAddressLine3(),
                respondentContactInformation.getCounty(),
                respondentContactInformation.getCountry(),
                respondentContactInformation.getPostCode()
            );
        }

        @Test
        void shouldReturnValidOrganisationDetails_whenDefendantIsRepresentedAndHasProvidedServiceAddress() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .applicantSolicitor1ServiceAddress(applicantSolicitorServiceAddress)
                .respondentSolicitor1ServiceAddress(respondentSolicitorServiceAddress)
                .build();

            Representative representative = representativeService.getRespondent1Representative(caseData);

            verify(organisationService).findOrganisationById(
                caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID());
            assertThat(representative).extracting("organisationName").isEqualTo(respondentOrganisation.getName());
            assertThat(representative).extracting("dxAddress").isEqualTo(
                respondentContactInformation.getDxAddress().get(0).getDxNumber());
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
                respondentSolicitorServiceAddress.getAddressLine1(),
                respondentSolicitorServiceAddress.getAddressLine2(),
                respondentSolicitorServiceAddress.getAddressLine3(),
                respondentSolicitorServiceAddress.getCounty(),
                respondentSolicitorServiceAddress.getCountry(),
                respondentSolicitorServiceAddress.getPostCode()
            );
        }

        @Test
        void shouldReturnValidOrganisationDetails_whenDefendantIsRepresentedAndHasNotProvidedServiceAddress() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .applicantSolicitor1ServiceAddress(applicantSolicitorServiceAddress)
                .build();

            Representative representative = representativeService.getRespondent1Representative(caseData);

            verify(organisationService).findOrganisationById(
                caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID());
            assertThat(representative).extracting("organisationName").isEqualTo(respondentOrganisation.getName());
            assertThat(representative).extracting("dxAddress").isEqualTo(
                respondentContactInformation.getDxAddress().get(0).getDxNumber());
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
                respondentContactInformation.getAddressLine1(),
                respondentContactInformation.getAddressLine2(),
                respondentContactInformation.getAddressLine3(),
                respondentContactInformation.getCounty(),
                respondentContactInformation.getCountry(),
                respondentContactInformation.getPostCode()
            );
        }

        @Test
        void shouldReturnValidOrganisationDetails_whenDefendantIsNotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnRepresentedDefendant().build();

            Representative representative = representativeService.getRespondent1Representative(caseData);

            verifyNoInteractions(organisationService);
            assertThat(representative).extracting(
                "organisationName", "phoneNumber", "dxAddress", "emailAddress").containsExactly(
                null,
                null,
                null,
                null
            );
            assertThat(representative).extracting("serviceAddress").isNull();

        }

        @Test
        void shouldReturnEmptyRepresentative_whenDefendantSolicitorIsNotRegistered() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnRegisteredDefendant().build();

            Representative representative = representativeService.getRespondent1Representative(caseData);

            verifyNoInteractions(organisationService);
            assertThat(representative).extracting(
                "organisationName", "phoneNumber", "dxAddress", "emailAddress").containsExactly(
                null, null, null, null
            );
            assertThat(representative).extracting("serviceAddress").isNull();

        }
    }

    @Nested
    class GetApplicantRepresentative {

        @Test
        void shouldReturnValidOrganisationDetails_whenApplicantHasProvidedServiceAddress() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .applicantSolicitor1ServiceAddress(applicantSolicitorServiceAddress)
                .respondentSolicitor1ServiceAddress(respondentSolicitorServiceAddress)
                .build();

            Representative representative = representativeService.getApplicantRepresentative(caseData);

            verify(organisationService).findOrganisationById(
                caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID());
            assertThat(representative).extracting("organisationName").isEqualTo(applicantOrganisation.getName());
            assertThat(representative).extracting("dxAddress").isEqualTo(
                applicantContactInformation.getDxAddress().get(0).getDxNumber());
            assertThat(representative).extracting("emailAddress").isEqualTo(
                caseData.getApplicantSolicitor1UserDetails().getEmail());
            assertThat(representative).extracting("serviceAddress").extracting(
                "AddressLine1",
                "AddressLine2",
                "AddressLine3",
                "County",
                "Country",
                "PostCode"
            ).containsExactly(
                applicantSolicitorServiceAddress.getAddressLine1(),
                applicantSolicitorServiceAddress.getAddressLine2(),
                applicantSolicitorServiceAddress.getAddressLine3(),
                applicantSolicitorServiceAddress.getCounty(),
                applicantSolicitorServiceAddress.getCountry(),
                applicantSolicitorServiceAddress.getPostCode()
            );
        }

        @Test
        void shouldReturnValidOrganisationDetails_whenApplicantHasNotProvidedServiceAddress() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .respondentSolicitor1ServiceAddress(respondentSolicitorServiceAddress)
                .build();

            Representative representative = representativeService.getApplicantRepresentative(caseData);

            verify(organisationService).findOrganisationById(
                caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID());
            assertThat(representative).extracting("organisationName").isEqualTo(applicantOrganisation.getName());
            assertThat(representative).extracting("dxAddress").isEqualTo(
                applicantContactInformation.getDxAddress().get(0).getDxNumber());
            assertThat(representative).extracting("emailAddress").isEqualTo(
                caseData.getApplicantSolicitor1UserDetails().getEmail());
            assertThat(representative).extracting("serviceAddress").extracting(
                "AddressLine1",
                "AddressLine2",
                "AddressLine3",
                "County",
                "Country",
                "PostCode"
            ).containsExactly(
                applicantContactInformation.getAddressLine1(),
                applicantContactInformation.getAddressLine2(),
                applicantContactInformation.getAddressLine3(),
                applicantContactInformation.getCounty(),
                applicantContactInformation.getCountry(),
                applicantContactInformation.getPostCode()
            );
        }
    }
}

