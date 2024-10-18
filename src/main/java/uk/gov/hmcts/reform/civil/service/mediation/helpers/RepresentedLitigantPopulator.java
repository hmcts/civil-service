package uk.gov.hmcts.reform.civil.service.mediation.helpers;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.model.mediation.MediationContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationLitigant;
import uk.gov.hmcts.reform.civil.service.mediation.MediationUnavailability;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Component
public class RepresentedLitigantPopulator {

    private final OrganisationService organisationService;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public RepresentedLitigantPopulator(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    public MediationLitigant.MediationLitigantBuilder populator(MediationLitigant.MediationLitigantBuilder builder,
                                                                MediationContactInformation mediationContactInformation,
                                                                MediationAvailability mediationAvailability,
                                                                OrganisationPolicy organisationPolicy, String solicitorEmail) {

        String solicitorOrgName = getSolicitorOrgName(organisationPolicy);
        String mediationContactName = getMediationContactName(mediationContactInformation);
        String mediationContactNumber = mediationContactInformation != null
            ? mediationContactInformation.getTelephoneNumber() : null;
        String mediationEmail = mediationContactInformation != null
            ? mediationContactInformation.getEmailAddress() : null;

        List<MediationUnavailability> dateRangeToAvoid = getDateRangeToAvoid(mediationAvailability);

        return builder.represented(true)
            .solicitorOrgName(solicitorOrgName)
            .litigantEmail(solicitorEmail)
            .litigantTelephone(null)
            .mediationContactName(mediationContactName)
            .mediationContactNumber(mediationContactNumber)
            .mediationContactEmail(mediationEmail)
            .dateRangeToAvoid(dateRangeToAvoid);
    }

    @Nullable
    private String getSolicitorOrgName(OrganisationPolicy organisationPolicy) {
        if (organisationPolicy.getOrganisation() == null) {
            return null;
        }
        String orgId = organisationPolicy.getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(orgId);
        Organisation solicitorOrgDetails = organisation.orElse(null);
        return solicitorOrgDetails != null ? solicitorOrgDetails.getName() : null;
    }

    @Nullable
    private String getMediationContactName(MediationContactInformation mediationContactInformation) {
        return mediationContactInformation != null
            ? String.format("%s %s", mediationContactInformation.getFirstName(), mediationContactInformation.getLastName())
            : null;
    }

    private List<MediationUnavailability> getDateRangeToAvoid(MediationAvailability mediationAvailability) {
        if (isMediationUnavailable(mediationAvailability)) {
            return toMediationUnavailableDates(mediationAvailability.getUnavailableDatesForMediation());
        }
        return getDefaultUnavailableList();
    }

    private boolean isMediationUnavailable(MediationAvailability mediationAvailability) {
        return mediationAvailability != null && YES.equals(mediationAvailability.getIsMediationUnavailablityExists());
    }

    private List<MediationUnavailability> toMediationUnavailableDates(List<Element<UnavailableDate>> unavailableDatesForMediation) {
        List<UnavailableDate> unavailableDates = unwrapElements(unavailableDatesForMediation);
        List<MediationUnavailability> toMediationUnavailability = new ArrayList<>();
        for (UnavailableDate unavailableDate : unavailableDates) {
            if (SINGLE_DATE.equals(unavailableDate.getUnavailableDateType())) {
                toMediationUnavailability.add(MediationUnavailability.builder()
                                                  .dateFrom(formatDate(unavailableDate.getDate()))
                                                  .dateTo(formatDate(unavailableDate.getDate()))
                                                  .build());
            }
            if (DATE_RANGE.equals(unavailableDate.getUnavailableDateType())) {
                toMediationUnavailability.add(MediationUnavailability.builder()
                                                  .dateFrom(formatDate(unavailableDate.getFromDate()))
                                                  .dateTo(formatDate(unavailableDate.getToDate()))
                                                  .build());
            }
        }
        return toMediationUnavailability;
    }

    private List<MediationUnavailability> getDefaultUnavailableList() {
        return List.of(MediationUnavailability.builder().build());
    }

    private String formatDate(LocalDate unavailableDate) {
        return unavailableDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK));
    }
}
