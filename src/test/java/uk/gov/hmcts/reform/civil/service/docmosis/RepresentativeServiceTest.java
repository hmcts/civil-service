package uk.gov.hmcts.reform.civil.service.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.DxAddress;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@ExtendWith(MockitoExtension.class)
class RepresentativeServiceTest {

    @InjectMocks
    private RepresentativeService representativeService;

    @Mock
    private OrganisationService organisationService;

    private ContactInformation applicantContactInformation;
    private ContactInformation respondentContactInformation;
    private ContactInformation respondent2ContactInformation;
    private Address respondentSolicitorServiceAddress;
    private Address respondentSolicitor2ServiceAddress;
    private Address applicantSolicitorServiceAddress;
    private Organisation applicantOrganisation;
    private Organisation respondentOrganisation;
    private Organisation respondent2Organisation;

    @BeforeEach
    void setUp() {
        applicantContactInformation = createContactInformation("A");
        respondentContactInformation = createContactInformation("R");
        respondent2ContactInformation = createContactInformation("R2");
        respondentSolicitorServiceAddress = createAddress("RS");
        respondentSolicitor2ServiceAddress = createAddress("RS2");
        applicantSolicitorServiceAddress = createAddress("AS");
        applicantOrganisation = createOrganisation("test applicant org", applicantContactInformation);
        respondentOrganisation = createOrganisation("test respondent org", respondentContactInformation);
        respondent2Organisation = createOrganisation("test respondent org", respondent2ContactInformation);
    }

    private ContactInformation createContactInformation(String prefix) {
        return ContactInformation.builder()
            .addressLine1(prefix + " address line 1")
            .addressLine2(prefix + " address line 2")
            .addressLine3(prefix + " address line 3")
            .postCode("SW1 1AA " + prefix)
            .county("London " + prefix)
            .country("UK " + prefix)
            .dxAddress(List.of(DxAddress.builder().dxNumber("DX12345" + prefix).build()))
            .build();
    }

    private Address createAddress(String prefix) {
        return Address.builder()
            .addressLine1(prefix + " service address 1")
            .addressLine2(prefix + " service address 2")
            .addressLine3(prefix + " service address 3")
            .postCode("SW1 1AA " + prefix)
            .county("London " + prefix)
            .country("UK " + prefix)
            .build();
    }

    private Organisation createOrganisation(String name, ContactInformation contactInformation) {
        return Organisation.builder()
            .name(name)
            .contactInformation(List.of(contactInformation))
            .build();
    }

    @Nested
    class GetRespondent1Representative {

        @Test
        void shouldReturnValidOrganisationDetails_whenDefendantIsRepresented() {
            given(organisationService.findOrganisationById("QWERTY R")).willReturn(Optional.of(respondentOrganisation));

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
            given(organisationService.findOrganisationById("QWERTY R")).willReturn(Optional.of(respondentOrganisation));

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
        void shouldReturnValidOrganisationDetails_whenDefendantIsRepresentedAndHasProvidedServiceAddressSpec() {
            given(organisationService.findOrganisationById("QWERTY R")).willReturn(Optional.of(respondentOrganisation));

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .applicantSolicitor1ServiceAddress(applicantSolicitorServiceAddress)
                .build().toBuilder()
                .caseAccessCategory(SPEC_CLAIM)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .specRespondentCorrespondenceAddressdetails(respondentSolicitorServiceAddress)
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
            given(organisationService.findOrganisationById("QWERTY R")).willReturn(Optional.of(respondentOrganisation));

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
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();

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
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnregisteredDefendant().build();

            Representative representative = representativeService.getRespondent1Representative(caseData);

            verifyNoInteractions(organisationService);
            assertThat(representative).extracting(
                "organisationName", "phoneNumber", "dxAddress", "emailAddress").containsExactly(
                null, null, null, null
            );
            assertThat(representative).extracting("serviceAddress").isNull();

        }

        @Nested
        class ToBeRemovedAfterNOC {

            @Test
            void prod_shouldReturnValidOrganisationDetails_whenDefendantIsNotRepresented() {
                CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();

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
            void prod_shouldReturnEmptyRepresentative_whenDefendantSolicitorIsNotRegistered() {
                CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnregisteredDefendant().build();

                Representative representative = representativeService.getRespondent1Representative(caseData);

                verifyNoInteractions(organisationService);
                assertThat(representative).extracting(
                    "organisationName", "phoneNumber", "dxAddress", "emailAddress").containsExactly(
                    null, null, null, null
                );
                assertThat(representative).extracting("serviceAddress").isNull();

            }
        }
    }

    @Nested
    class GetRespondent2Representative {

        @Test
        void shouldReturnValidOrganisationDetails_whenDefendantIsRepresented() {
            given(organisationService.findOrganisationById("QWERTY R2")).willReturn(Optional.of(respondent2Organisation));

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
            given(organisationService.findOrganisationById("QWERTY R2")).willReturn(Optional.of(respondent2Organisation));

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
            given(organisationService.findOrganisationById("QWERTY R2")).willReturn(Optional.of(respondent2Organisation));

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
        void shouldReturnValidOrganisationDetails_whenOrganisationIDIsEmpty() {
            given(organisationService.findOrganisationById("QWERTY R2")).willReturn(Optional.of(respondent2Organisation));

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
    }

    @Nested
    class GetApplicantRepresentative {

        @Test
        void shouldReturnValidOrganisationDetails_whenApplicantHasProvidedServiceAddress() {
            given(organisationService.findOrganisationById("QWERTY A")).willReturn(Optional.of(applicantOrganisation));

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
            given(organisationService.findOrganisationById("QWERTY A")).willReturn(Optional.of(applicantOrganisation));

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

