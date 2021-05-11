package uk.gov.hmcts.reform.unspec.service.docmosis;

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
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.OrganisationService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

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
        void shouldReturnValidOrganisationDetails_whenDefendantIsRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

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
        void shouldReturnValidOrganisationDetails_whenDefendantIsNotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnRepresentedDefendant().build();

            Representative representative = representativeService.getRespondentRepresentative(caseData);

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
        void shouldReturnEmptyRepresentative_whenDefendantSolicitorIsNotRegisteredInMyHmcts() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnRegisteredDefendant().build();

            Representative representative = representativeService.getRespondentRepresentative(caseData);

            verifyNoInteractions(organisationService);
            assertThat(representative).extracting(
                "organisationName", "phoneNumber", "dxAddress", "emailAddress").containsExactly(
                null, null, null, null
            );
            assertThat(representative).extracting("serviceAddress").isNull();

        }
    }
}

