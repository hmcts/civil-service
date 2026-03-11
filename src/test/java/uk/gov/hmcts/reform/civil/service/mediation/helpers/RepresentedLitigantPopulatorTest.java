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
        addMediationInfoRepresented(buildClaimant1(YES));

        Organisation organisation = new Organisation();
        OrganisationPolicy organisationPolicy = new OrganisationPolicy()
            .setOrganisation(organisation);

        MediationContactInformation contactInfo = new MediationContactInformation()
            .setFirstName("John")
            .setLastName("Doe")
            .setTelephoneNumber("0123456789")
            .setEmailAddress("john.doe@example.com");

        MediationAvailability mediationAvailability = new MediationAvailability();
        mediationAvailability.setIsMediationUnavailablityExists(YES);
        mediationAvailability.setUnavailableDatesForMediation(Collections.emptyList());

        String solicitorEmail = "solicitor@example.com";

        MediationLitigant litigant = representedLitigantPopulator.populator(
            new MediationLitigant(),
            contactInfo,
            mediationAvailability,
            organisationPolicy,
            solicitorEmail
        );

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
        OrganisationPolicy organisationPolicy = new OrganisationPolicy()
            .setOrganisation(organisation);

        MediationAvailability mediationAvailability = new MediationAvailability();
        mediationAvailability.setIsMediationUnavailablityExists(YES);
        mediationAvailability.setUnavailableDatesForMediation(Collections.emptyList());

        String solicitorEmail = "solicitor@example.com";

        MediationLitigant litigant = representedLitigantPopulator.populator(
            new MediationLitigant(),
            null,
            mediationAvailability,
            organisationPolicy,
            solicitorEmail
        );

        assertThat(litigant.isRepresented()).isTrue();
        assertThat(litigant.getLitigantEmail()).isEqualTo(solicitorEmail);
        assertThat(litigant.getMediationContactName()).isNull();
        assertThat(litigant.getMediationContactNumber()).isNull();
        assertThat(litigant.getMediationContactEmail()).isNull();
        assertThat(litigant.getDateRangeToAvoid()).isEmpty();
    }

    @Test
    void shouldHandleUnavailableDatesCorrectly() {
        MediationContactInformation contactInfo = new MediationContactInformation()
            .setFirstName("John")
            .setLastName("Doe")
            .setTelephoneNumber("0123456789")
            .setEmailAddress("john.doe@example.com");

        Organisation organisation = new Organisation();
        OrganisationPolicy organisationPolicy = new OrganisationPolicy()
            .setOrganisation(organisation);

        LocalDate fixedDate = LocalDate.of(2024, 6, 10);
        UnavailableDate unavailableDate = new UnavailableDate()
            .setDate(fixedDate)
            .setUnavailableDateType(SINGLE_DATE);
        Element<UnavailableDate> elementUnavailableDate = ElementUtils.element(unavailableDate);

        MediationAvailability mediationAvailability = new MediationAvailability();
        mediationAvailability.setIsMediationUnavailablityExists(YES);
        mediationAvailability.setUnavailableDatesForMediation(List.of(elementUnavailableDate));

        String solicitorEmail = "solicitor@example.com";

        MediationLitigant litigant = representedLitigantPopulator.populator(
            new MediationLitigant(),
            contactInfo,
            mediationAvailability,
            organisationPolicy,
            solicitorEmail
        );

        assertThat(litigant.getDateRangeToAvoid()).hasSize(1);
        assertThat(litigant.getDateRangeToAvoid().get(0).getDateFrom()).isEqualTo("2024-06-10");
        assertThat(litigant.getDateRangeToAvoid().get(0).getDateTo()).isEqualTo("2024-06-10");
    }

    private MediationLitigant addMediationInfoRepresented(MediationLitigant litigant) {
        return litigant
            .setMediationContactName(MEDIATION_CONTACT_NAME)
            .setMediationContactNumber(MEDIATION_CONTACT_NUMBER)
            .setMediationContactEmail(MEDIATION_CONTACT_EMAIL)
            .setDateRangeToAvoid(List.of(
                new MediationUnavailability()
                    .setDateFrom("2024-06-01")
                    .setDateTo("2024-06-01"),
                new MediationUnavailability()
                    .setDateFrom("2024-06-07")
                    .setDateTo("2024-06-07"),
                new MediationUnavailability()
                    .setDateFrom("2024-06-10")
                    .setDateTo("2024-06-15"),
                new MediationUnavailability()
                    .setDateFrom("2024-06-20")
                    .setDateTo("2024-06-25")
            ));
    }

    private MediationLitigant buildClaimant1(YesOrNo represented) {
        if (YES.equals(represented)) {
            return new MediationLitigant()
                .setPartyID("app-1-party-id")
                .setPartyRole("Claimant 1")
                .setPartyName("Mr. John Rambo")
                .setPartyType(INDIVIDUAL)
                .setPaperResponse(PAPER_RESPONSE)
                .setRepresented(true)
                .setSolicitorOrgName(APPLICANT_LR_NAME)
                .setLitigantTelephone(null)
                .setLitigantEmail(APPLICANT_LR_EMAIL)
                .setMediationContactName(null)
                .setMediationContactNumber(null)
                .setMediationContactEmail(null)
                .setDateRangeToAvoid(List.of(new MediationUnavailability()));
        } else {
            return new MediationLitigant()
                .setPartyID("app-1-party-id")
                .setPartyRole("Claimant 1")
                .setPartyName("Mr. John Rambo")
                .setPartyType(INDIVIDUAL)
                .setPaperResponse(PAPER_RESPONSE)
                .setRepresented(false)
                .setSolicitorOrgName(null)
                .setLitigantTelephone("0123456789")
                .setLitigantEmail("rambo@email.com")
                .setMediationContactName(null)
                .setMediationContactNumber(null)
                .setMediationContactEmail(null)
                .setDateRangeToAvoid(List.of(new MediationUnavailability()));
        }
    }

}
