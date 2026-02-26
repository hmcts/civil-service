package uk.gov.hmcts.reform.civil.service.mediation.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.mediation.MediationLitigant;
import uk.gov.hmcts.reform.civil.service.mediation.MediationUnavailability;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Component
public class UnrepresentedLitigantPopulator {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public MediationLitigant populator(MediationLitigant litigant,
                                       Party party,
                                       String originalMediationContactPerson,
                                       MediationLiPCarm mediationLiPCarm) {

        List<MediationUnavailability> dateRangeToAvoid = getDateRangeToAvoid(mediationLiPCarm);

        String mediationContactName = getUnrepresentedLitigantMediationContactName(
            party, originalMediationContactPerson, mediationLiPCarm);

        String mediationEmail = getMediationEmail(party, mediationLiPCarm);

        String mediationPhone = getMediationPhone(party, mediationLiPCarm);

        return litigant.setRepresented(false)
            .setSolicitorOrgName(null)
            .setLitigantEmail(party.getPartyEmail())
            .setLitigantTelephone(party.getPartyPhone())
            .setMediationContactName(mediationContactName)
            .setMediationContactNumber(mediationPhone)
            .setMediationContactEmail(mediationEmail)
            .setDateRangeToAvoid(dateRangeToAvoid);
    }

    private static String getMediationPhone(Party party, MediationLiPCarm mediationLiPCarm) {
        if (mediationLiPCarm == null) {
            return party.getPartyPhone();
        }
        return YES.equals(mediationLiPCarm.getIsMediationPhoneCorrect())
            ? party.getPartyPhone() : mediationLiPCarm.getAlternativeMediationTelephone();
    }

    private static String getMediationEmail(Party party, MediationLiPCarm mediationLiPCarm) {
        if (mediationLiPCarm == null) {
            return party.getPartyEmail();
        }
        return YES.equals(mediationLiPCarm.getIsMediationEmailCorrect())
            ? party.getPartyEmail() : mediationLiPCarm.getAlternativeMediationEmail();
    }

    private List<MediationUnavailability> getDateRangeToAvoid(MediationLiPCarm mediationLiPCarm) {
        if (mediationLiPCarm != null && YES.equals(mediationLiPCarm.getHasUnavailabilityNextThreeMonths())) {
            return toMediationUnavailableDates(mediationLiPCarm.getUnavailableDatesForMediation());
        }
        return List.of(new MediationUnavailability());
    }

    private String getUnrepresentedLitigantMediationContactName(Party party,
                                                                String originalMediationContactPerson,
                                                                MediationLiPCarm mediationLiPCarm) {
        return isIndividualOrSoleTrader(party)
            ? party.getPartyName()
            : getContactName(mediationLiPCarm, originalMediationContactPerson);
    }

    private List<MediationUnavailability> toMediationUnavailableDates(List<Element<UnavailableDate>> unavailableDatesForMediation) {
        List<UnavailableDate> unavailableDates = unwrapElements(unavailableDatesForMediation);
        List<MediationUnavailability> toMediationUnavailability = new ArrayList<>();
        for (UnavailableDate unavailableDate : unavailableDates) {
            if (SINGLE_DATE.equals(unavailableDate.getUnavailableDateType())) {
                toMediationUnavailability.add(new MediationUnavailability()
                                                  .setDateFrom(formatDate(unavailableDate.getDate()))
                                                  .setDateTo(formatDate(unavailableDate.getDate())));
            }
            if (DATE_RANGE.equals(unavailableDate.getUnavailableDateType())) {
                toMediationUnavailability.add(new MediationUnavailability()
                                                  .setDateFrom(formatDate(unavailableDate.getFromDate()))
                                                  .setDateTo(formatDate(unavailableDate.getToDate())));
            }
        }
        return toMediationUnavailability;
    }

    private boolean isIndividualOrSoleTrader(Party party) {
        return Party.Type.INDIVIDUAL.equals(party.getType())
            || Party.Type.SOLE_TRADER.equals(party.getType());
    }

    private String getContactName(MediationLiPCarm mediationLiPCarm, String originalContactName) {
        if (mediationLiPCarm == null) {
            return originalContactName;
        }
        return YES.equals(mediationLiPCarm.getIsMediationContactNameCorrect())
            ? originalContactName
            : mediationLiPCarm.getAlternativeMediationContactPerson();
    }

    private String formatDate(LocalDate unavailableDate) {
        return unavailableDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK));
    }
}
