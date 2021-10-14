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
        .country("UK R")
        .dxAddress(List.of(DxAddress.builder().dxNumber("DX12345R").build()))
        .build();
    private final ContactInformation respondent2ContactInformation = ContactInformation.builder()
        .addressLine1("R2 address line 1")
        .addressLine2("R2 address line 2")
        .addressLine3("R2 address line 3")
        .postCode("SW1 1AA R2")
        .county("London R2")
        .country("UK R2")
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
    private final Address respondentSolicitor2ServiceAddress = Address.builder()
        .addressLine1("RS2 service address 1")
        .addressLine2("RS2 service address 2")
        .addressLine3("RS2 service address 3")
        .postCode("SW1 1AA RS2")
        .county("London RS2")
        .country("UK RS2")
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
    private final Organisation respondent2Organisation = Organisation.builder()
        .name("test respondent org")
        .contactInformation(List.of(respondent2ContactInformation))
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
        given(organisationService.findOrganisationById("QWERTY R2")).willReturn(Optional.of(respondent2Organisation));
    }

    @Nested
    class GetRespondent1Representative {

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
    class GetRespondent2Representative {

        @Test
        void shouldReturnValidOrganisationDetails_whenDefendantIsRepresented() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .multiPartyClaimTwoDefendantSolicitors().build();

            Representative representative = representativeService.getRespondent2Representative(caseData);

            verify(organisationService).findOrganisationById(
                caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID());
            assertThat(representative).extracting("organisationName").isEqualTo(respondent2Organisation.getName());
            assertThat(representative).extracting("dxAddress").isEqualTo(
                respondent2ContactInformation.getDxAddress().get(0).getDxNumber());
            assertThat(representative).extracting("emailAddress").isEqualTo(
                caseData.getRespondentSolicitor2EmailAddress());
            assertThat(representative).extracting("serviceAddress").extracting(
                "AddressLine1",
                "AddressLine2",
                "AddressLine3",
                "County",
                "Country",
                "PostCode"
            ).containsExactly(
                respondent2ContactInformation.getAddressLine1(),
                respondent2ContactInformation.getAddressLine2(),
                respondent2ContactInformation.getAddressLine3(),
                respondent2ContactInformation.getCounty(),
                respondent2ContactInformation.getCountry(),
                respondent2ContactInformation.getPostCode()
            );
        }

        @Test
        void shouldReturnValidOrganisationDetails_whenDefendantIsRepresentedAndHasProvidedServiceAddress() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .multiPartyClaimTwoDefendantSolicitors()
                .applicantSolicitor1ServiceAddress(applicantSolicitorServiceAddress)
                .respondentSolicitor1ServiceAddress(respondentSolicitorServiceAddress)
                .respondentSolicitor2ServiceAddress(respondentSolicitor2ServiceAddress)
                .build();

            Representative representative = representativeService.getRespondent2Representative(caseData);

            verify(organisationService).findOrganisationById(
                caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID());
            assertThat(representative).extracting("organisationName").isEqualTo(respondent2Organisation.getName());
            assertThat(representative).extracting("dxAddress").isEqualTo(
                respondent2ContactInformation.getDxAddress().get(0).getDxNumber());
            assertThat(representative).extracting("emailAddress").isEqualTo(
                caseData.getRespondentSolicitor2EmailAddress());
            assertThat(representative).extracting("serviceAddress").extracting(
                "AddressLine1",
                "AddressLine2",
                "AddressLine3",
                "County",
                "Country",
                "PostCode"
            ).containsExactly(
                respondentSolicitor2ServiceAddress.getAddressLine1(),
                respondentSolicitor2ServiceAddress.getAddressLine2(),
                respondentSolicitor2ServiceAddress.getAddressLine3(),
                respondentSolicitor2ServiceAddress.getCounty(),
                respondentSolicitor2ServiceAddress.getCountry(),
                respondentSolicitor2ServiceAddress.getPostCode()
            );
        }

        @Test
        void shouldReturnValidOrganisationDetails_whenDefendantIsRepresentedAndHasNotProvidedServiceAddress() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .multiPartyClaimTwoDefendantSolicitors()
                .applicantSolicitor1ServiceAddress(applicantSolicitorServiceAddress)
                .build();

            Representative representative = representativeService.getRespondent2Representative(caseData);

            verify(organisationService).findOrganisationById(
                caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID());
            assertThat(representative).extracting("organisationName").isEqualTo(respondent2Organisation.getName());
            assertThat(representative).extracting("dxAddress").isEqualTo(
                respondent2ContactInformation.getDxAddress().get(0).getDxNumber());
            assertThat(representative).extracting("emailAddress").isEqualTo(
                caseData.getRespondentSolicitor2EmailAddress());
            assertThat(representative).extracting("serviceAddress").extracting(
                "AddressLine1",
                "AddressLine2",
                "AddressLine3",
                "County",
                "Country",
                "PostCode"
            ).containsExactly(
                respondent2ContactInformation.getAddressLine1(),
                respondent2ContactInformation.getAddressLine2(),
                respondent2ContactInformation.getAddressLine3(),
                respondent2ContactInformation.getCounty(),
                respondent2ContactInformation.getCountry(),
                respondent2ContactInformation.getPostCode()
            );
        }

        @Test
        void shouldReturnValidOrganisationDetails_whenDefendantIsNotRepresented() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssuedUnRepresentedDefendant()
                .multiPartyClaimTwoDefendantSolicitors().build();

            Representative representative = representativeService.getRespondent2Representative(caseData);

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
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssuedUnRegisteredDefendant()
                .multiPartyClaimTwoDefendantSolicitors().build();

            Representative representative = representativeService.getRespondent2Representative(caseData);

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

