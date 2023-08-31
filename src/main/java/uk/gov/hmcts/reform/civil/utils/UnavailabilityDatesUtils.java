package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingDates;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class UnavailabilityDatesUtils {

    private static final String DEFENDANT_RESPONSE_EVENT = "Defendant Response Event";
    private static final String CLAIMANT_INTENTION_EVENT = "Claimant Intention Event";
    private static final String DJ_EVENT = "Request DJ Event";

    private UnavailabilityDatesUtils() {
                //NO-OP
    }

    public static void rollUpUnavailabilityDatesForRespondent(CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData caseData = builder.build();
        if (caseData.getRespondent1DQ() != null && caseData.getRespondent1DQ().getHearing() != null) {

            Party.PartyBuilder resp1 = caseData.getRespondent1().toBuilder()
                .unavailableDates(caseData.getRespondent1DQ().getHearing()
                                      .getUnavailableDates());
            builder.respondent1(resp1.build());

            if ((getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP)) {
                Party.PartyBuilder resp2 = caseData.getRespondent2().toBuilder()
                    .unavailableDates(caseData.getRespondent1DQ().getHearing()
                                          .getUnavailableDates());
                builder.respondent2(resp2.build());
            }

        }
        if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getHearing() != null) {
            Party.PartyBuilder resp2 = caseData.getRespondent2().toBuilder()
                .unavailableDates(caseData.getRespondent2DQ().getHearing()
                                      .getUnavailableDates());
            builder.respondent2(resp2.build());
        }
    }

    public static void rollUpUnavailabilityDatesForApplicant(CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData caseData = builder.build();
        if (caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getHearing() != null) {

            Party.PartyBuilder appl1 = caseData.getApplicant1().toBuilder()
                .unavailableDates(caseData.getApplicant1DQ().getHearing()
                                      .getUnavailableDates());
            builder.applicant1(appl1.build());
        }
        if (caseData.getApplicant2() != null && caseData.getApplicant1DQ().getHearing() != null) {
            Party.PartyBuilder appl2 = caseData.getApplicant2().toBuilder()
                .unavailableDates(caseData.getApplicant1DQ().getHearing()
                                      .getUnavailableDates());
            builder.applicant2(appl2.build());
        }
    }

    public static void rollUpUnavailabilityDatesForApplicantDJ(CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData caseData = builder.build();
        if (caseData.getHearingSupportRequirementsDJ() != null
            && YES.equals(caseData.getHearingSupportRequirementsDJ().getHearingUnavailableDates())) {
            List<UnavailableDate> unavailableDates = new ArrayList<>();
            List<HearingDates> unavailableDatesDJ = unwrapElements(caseData.getHearingSupportRequirementsDJ().getHearingDates());

            for (HearingDates unavailableDate : unavailableDatesDJ) {
                LocalDate fromDate = unavailableDate.getHearingUnavailableFrom();
                LocalDate toDate = unavailableDate.getHearingUnavailableUntil();
                UnavailableDateType type = fromDate.isEqual(toDate) ? SINGLE_DATE : DATE_RANGE;

                if (SINGLE_DATE.equals(type)) {
                    unavailableDates.add(UnavailableDate.builder()
                                             .date(fromDate)
                                             .unavailableDateType(type)
                                             .build());
                } else {
                    unavailableDates.add(UnavailableDate.builder()
                                             .fromDate(fromDate)
                                             .toDate(toDate)
                                             .unavailableDateType(type)
                                             .build());
                }
            }

            builder.applicant1(caseData.getApplicant1().toBuilder()
                                   .unavailableDates(wrapElements(unavailableDates))
                                   .build());

            if (caseData.getApplicant2() != null) {
                builder.applicant2(caseData.getApplicant2().toBuilder()
                                       .unavailableDates(wrapElements(unavailableDates))
                                       .build());
            }
        }
    }

    public static void copyDatesIntoListingTabFields(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        // if unavailable dates exist but listingTab fields are null
        if (caseData.getApplicant1().getUnavailableDates() != null
            && caseData.getApplicant1UnavailableDatesForTab() == null) {
            copyDatesIntoListingTabFieldsForApplicant(caseData, updatedData, true);
        }

        if (caseData.getApplicant2() != null
            && caseData.getApplicant2().getUnavailableDates() != null
            && caseData.getApplicant1UnavailableDatesForTab() == null) {
            // applicant 1 and 2 share the same dates in 2v1 scenario
            copyDatesIntoListingTabFieldsForApplicant(caseData, updatedData, false);
        }

        if (caseData.getRespondent1().getUnavailableDates() != null
            && caseData.getRespondent1UnavailableDatesForTab() == null) {
            copyDatesIntoListingTabFieldsForRespondent1(caseData, updatedData);
        }

        if (caseData.getRespondent2() != null
            && caseData.getRespondent2().getUnavailableDates() != null
            && caseData.getRespondent2UnavailableDatesForTab() == null) {
            copyDatesIntoListingTabFieldsForRespondent2(caseData, updatedData);
        }
    }

    private static List<Element<UnavailableDate>> getExistingDates(List<Element<UnavailableDate>> partyDates, String event, LocalDate date) {
        List<Element<UnavailableDate>> existingUnavailableDates = ofNullable(partyDates).orElse(newArrayList());
        List<Element<UnavailableDate>> updatedUnavailableDates = new ArrayList<>();

        // if top level party has existing unavailableDates
        if (!existingUnavailableDates.isEmpty()) {
            for (Element<UnavailableDate> existingDate : existingUnavailableDates) {
                // only add dateAdded and eventAdded if they do not already exist.
                if (existingDate.getValue() != null
                    && existingDate.getValue().getEventAdded() == null
                    && existingDate.getValue().getDateAdded() == null) {
                    updatedUnavailableDates.addAll(wrapElements(existingDate.getValue().toBuilder()
                                                                    .dateAdded(date)
                                                                    .eventAdded(event).build()));
                } else {
                    updatedUnavailableDates.add(existingDate);
                }
            }
        }

        return updatedUnavailableDates;
    }

    private static Boolean isClaimantIntentionEvent(CaseData caseData) {
        // what about spec?
        return caseData.getApplicant1DQ() != null
            && caseData.getApplicant1DQ().getHearing() != null
            && YES.equals(caseData.getApplicant1DQ().getHearing().getUnavailableDatesRequired());
    }

    private static void copyDatesIntoListingTabFieldsForApplicant(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, Boolean isApplicant1) {
        String eventAdded = null;
        LocalDate dateAdded = null;

        // Claimant event
        if (isClaimantIntentionEvent(caseData)) {
            eventAdded = CLAIMANT_INTENTION_EVENT;
            dateAdded = caseData.getApplicant1ResponseDate().toLocalDate();
        // DJ event
        } else if (caseData.getHearingSupportRequirementsDJ() != null
            && YES.equals(caseData.getHearingSupportRequirementsDJ().getHearingUnavailableDates())) {
            eventAdded = DJ_EVENT;
            dateAdded = caseData.getDefaultJudgmentDocuments().get(0).getValue().getCreatedDatetime().toLocalDate();
        }

        List<Element<UnavailableDate>> dates = getExistingDates(
            caseData.getApplicant1().getUnavailableDates(),
            eventAdded,
            dateAdded
        );

        if (isApplicant1) {
            updatedData
                .applicant1(caseData.getApplicant1().toBuilder().unavailableDates(dates).build())
                .applicant1UnavailableDatesForTab(dates);
        } else {
            updatedData
                .applicant2(caseData.getApplicant2().toBuilder().unavailableDates(dates).build())
                .applicant2UnavailableDatesForTab(dates);
        }
    }

    private static void copyDatesIntoListingTabFieldsForRespondent1(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        String eventAdded = null;
        LocalDate dateAdded = null;

        // No DJ check because only claimant solicitor can run DJ
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1DQ().getHearing() != null
            && YES.equals(caseData.getRespondent1DQ().getHearing().getUnavailableDatesRequired())) {
            eventAdded = DEFENDANT_RESPONSE_EVENT;
            dateAdded = caseData.getRespondent1ResponseDate().toLocalDate();
        }

        List<Element<UnavailableDate>> dates = getExistingDates(
            caseData.getRespondent1().getUnavailableDates(),
            eventAdded,
            dateAdded
        );

        updatedData
            .respondent1(caseData.getRespondent1().toBuilder().unavailableDates(dates).build())
            .respondent1UnavailableDatesForTab(dates);
    }

    private static void copyDatesIntoListingTabFieldsForRespondent2(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        String eventAdded = null;
        LocalDate dateAdded = null;

        // No DJ check because only claimant solicitor can run DJ
        if (caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getHearing() != null
            && YES.equals(caseData.getRespondent2DQ().getHearing().getUnavailableDatesRequired())) {
            eventAdded = DEFENDANT_RESPONSE_EVENT;
            dateAdded = caseData.getRespondent2ResponseDate().toLocalDate();
        }

        List<Element<UnavailableDate>> dates = getExistingDates(
            caseData.getRespondent2().getUnavailableDates(),
            eventAdded,
            dateAdded
        );

        updatedData
            .respondent2(caseData.getRespondent2().toBuilder().unavailableDates(dates).build())
            .respondent2UnavailableDatesForTab(dates);
    }
}
