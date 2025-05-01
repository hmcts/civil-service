package uk.gov.hmcts.reform.civil.service.mediation.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.model.mediation.MediationContactInformation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationLitigant;
import uk.gov.hmcts.reform.civil.service.mediation.MediationUnavailability;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;

@ExtendWith(MockitoExtension.class)
public class RepresentedLitigantPopulatorTest {

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private RepresentedLitigantPopulator representedLitigantPopulator;

    private static final String MEDIATION_CONTACT_NAME = "Contact person";
    private static final String MEDIATION_CONTACT_EMAIL = "Contact.person@mediation.com";
    private static final String MEDIATION_CONTACT_NUMBER = "07888888888";
    private static final String PAPER_RESPONSE = "N";
    private static final String APPLICANT_LR_NAME = "Applicant LR Org";
    private static final String APPLICANT_LR_EMAIL = "applicantsolicitor@example.com";

    @Test
    void shouldPopulateLitigantWithCorrectInfo_whenAllFieldsProvided() {
        MediationContactInformation contactInfo = MediationContactInformation.builder()
            .firstName("John")
            .lastName("Doe")
            .telephoneNumber("0123456789")
            .emailAddress("john.doe@example.com")
            .build();

        addMediationInfoRepresented(buildClaimant1(YES));

        Organisation organisation = Organisation.builder().build();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(organisation)
            .build();

        MediationAvailability mediationAvailability = MediationAvailability.builder()
            .isMediationUnavailablityExists(YES)
            .unavailableDatesForMediation(Collections.emptyList())
            .build();

        String solicitorEmail = "solicitor@example.com";

        MediationLitigant.MediationLitigantBuilder builder = MediationLitigant.builder();
        MediationLitigant litigant = representedLitigantPopulator.populator(builder, contactInfo, mediationAvailability, organisationPolicy, solicitorEmail).build();

        assertThat(litigant.isRepresented()).isTrue();
        assertThat(litigant.getLitigantEmail()).isEqualTo(solicitorEmail);
        assertThat(litigant.getLitigantTelephone()).isNull();
        assertThat(litigant.getMediationContactName()).isEqualTo("John Doe");
        assertThat(litigant.getMediationContactNumber()).isEqualTo("0123456789");
        assertThat(litigant.getMediationContactEmail()).isEqualTo("john.doe@example.com");
        assertThat(litigant.getDateRangeToAvoid()).isEmpty();
    }

    @Test
    void shouldHandleNullableContactInformation() {
        Organisation organisation = new Organisation();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(organisation)
            .build();

        MediationAvailability mediationAvailability = MediationAvailability.builder()
            .isMediationUnavailablityExists(YES)
            .unavailableDatesForMediation(Collections.emptyList())
            .build();

        String solicitorEmail = "solicitor@example.com";

        MediationLitigant.MediationLitigantBuilder builder = MediationLitigant.builder();
        MediationLitigant litigant = representedLitigantPopulator.populator(builder, null, mediationAvailability, organisationPolicy, solicitorEmail).build();

        assertThat(litigant.isRepresented()).isTrue();
        assertThat(litigant.getLitigantEmail()).isEqualTo(solicitorEmail);
        assertThat(litigant.getMediationContactName()).isNull();
        assertThat(litigant.getMediationContactNumber()).isNull();
        assertThat(litigant.getMediationContactEmail()).isNull();
        assertThat(litigant.getDateRangeToAvoid()).isEmpty();
    }

    @Test
    void shouldHandleUnavailableDatesCorrectly() {
        MediationContactInformation contactInfo = MediationContactInformation.builder()
            .firstName("John")
            .lastName("Doe")
            .telephoneNumber("0123456789")
            .emailAddress("john.doe@example.com")
            .build();

        Organisation organisation = new Organisation();
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(organisation)
            .build();

        LocalDate fixedDate = LocalDate.of(2024, 6, 10);
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .date(fixedDate)
            .unavailableDateType(SINGLE_DATE)
            .build();
        Element<UnavailableDate> elementUnavailableDate = ElementUtils.element(unavailableDate);

        MediationAvailability mediationAvailability = MediationAvailability.builder()
            .isMediationUnavailablityExists(YES)
            .unavailableDatesForMediation(List.of(elementUnavailableDate))
            .build();

        String solicitorEmail = "solicitor@example.com";

        MediationLitigant.MediationLitigantBuilder builder = MediationLitigant.builder();
        MediationLitigant litigant = representedLitigantPopulator.populator(builder, contactInfo, mediationAvailability, organisationPolicy, solicitorEmail).build();

        assertThat(litigant.getDateRangeToAvoid()).hasSize(1);
        assertThat(litigant.getDateRangeToAvoid().get(0).getDateFrom()).isEqualTo("2024-06-10");
        assertThat(litigant.getDateRangeToAvoid().get(0).getDateTo()).isEqualTo("2024-06-10");
    }

    private MediationLitigant addMediationInfoRepresented(MediationLitigant litigant) {
        return litigant.toBuilder()
            .mediationContactName(MEDIATION_CONTACT_NAME)
            .mediationContactNumber(MEDIATION_CONTACT_NUMBER)
            .mediationContactEmail(MEDIATION_CONTACT_EMAIL)
            .dateRangeToAvoid(List.of(MediationUnavailability.builder()
                                          .dateFrom("2024-06-01")
                                          .dateTo("2024-06-01")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-07")
                                          .dateTo("2024-06-07")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-10")
                                          .dateTo("2024-06-15")
                                          .build(),
                                      MediationUnavailability.builder()
                                          .dateFrom("2024-06-20")
                                          .dateTo("2024-06-25")
                                          .build())).build();
    }

    private MediationLitigant buildClaimant1(YesOrNo represented) {
        if (YES.equals(represented)) {
            return MediationLitigant.builder()
                .partyID("app-1-party-id")
                .partyRole("Claimant 1")
                .partyName("Mr. John Rambo")
                .partyType(INDIVIDUAL)
                .paperResponse(PAPER_RESPONSE)
                .represented(true)
                .solicitorOrgName(APPLICANT_LR_NAME)
                .litigantTelephone(null)
                .litigantEmail(APPLICANT_LR_EMAIL)
                .mediationContactName(null)
                .mediationContactNumber(null)
                .mediationContactEmail(null)
                .dateRangeToAvoid(List.of(MediationUnavailability.builder().build()))
                .build();
        } else {
            return MediationLitigant.builder()
                .partyID("app-1-party-id")
                .partyRole("Claimant 1")
                .partyName("Mr. John Rambo")
                .partyType(INDIVIDUAL)
                .paperResponse(PAPER_RESPONSE)
                .represented(false)
                .solicitorOrgName(null)
                .litigantTelephone("0123456789")
                .litigantEmail("rambo@email.com")
                .mediationContactName(null)
                .mediationContactNumber(null)
                .mediationContactEmail(null)
                .dateRangeToAvoid(List.of(MediationUnavailability.builder().build()))
                .build();
        }
    }

}
