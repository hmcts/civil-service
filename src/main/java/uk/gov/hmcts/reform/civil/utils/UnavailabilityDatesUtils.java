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

    public static void rollUpUnavailabilityDatesForRespondent(CaseData caseData) {
        if (caseData.getRespondent1DQ() != null && caseData.getRespondent1DQ().getHearing() != null
            && caseData.getRespondent1DQ().getHearing().getUnavailableDates() != null
            && caseData.getRespondent1ResponseDate() != null) {

            List<Element<UnavailableDate>> respondent1DQUnavailableDates = caseData.getRespondent1DQ().getHearing()
                .getUnavailableDates();

            List<Element<UnavailableDate>> updatedUnavailableDates = addEventAndDate(
                caseData.getRespondent1ResponseDate().toLocalDate(),
                DEFENDANT_RESPONSE_EVENT,
                respondent1DQUnavailableDates
            );

            caseData.getRespondent1().setUnavailableDates(updatedUnavailableDates);
            caseData.setRespondent1UnavailableDatesForTab(updatedUnavailableDates);

            if ((getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP)) {
                caseData.getRespondent2().setUnavailableDates(updatedUnavailableDates);
                caseData.setRespondent2UnavailableDatesForTab(updatedUnavailableDates);
            }
        }
        if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getHearing() != null
            && caseData.getRespondent2DQ().getHearing().getUnavailableDates() != null
            && caseData.getRespondent2ResponseDate() != null) {

            List<Element<UnavailableDate>> respondent2DQUnavailableDates =
                caseData.getRespondent2DQ().getHearing().getUnavailableDates();
            List<Element<UnavailableDate>> updatedUnavailableDates = addEventAndDate(
                caseData.getRespondent2ResponseDate().toLocalDate(),
                DEFENDANT_RESPONSE_EVENT,
                respondent2DQUnavailableDates
            );

            caseData.getRespondent2().setUnavailableDates(updatedUnavailableDates);
            caseData.setRespondent2UnavailableDatesForTab(updatedUnavailableDates);
        }
    }

    public static void rollUpUnavailabilityDatesForApplicant(CaseData caseData) {
        if (caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getHearing() != null
            && caseData.getApplicant1DQ().getHearing().getUnavailableDates() != null
            && caseData.getApplicant1ResponseDate() != null) {

            List<Element<UnavailableDate>> applicant1DQUnavailableDates =
                caseData.getApplicant1DQ().getHearing().getUnavailableDates();

            List<Element<UnavailableDate>> updatedUnavailableDates = addEventAndDate(
                caseData.getApplicant1ResponseDate().toLocalDate(),
                CLAIMANT_INTENTION_EVENT,
                applicant1DQUnavailableDates
            );

            caseData.getApplicant1().setUnavailableDates(updatedUnavailableDates);
            caseData.setApplicant1UnavailableDatesForTab(updatedUnavailableDates);

            if (caseData.getApplicant2() != null) {
                caseData.getApplicant2().setUnavailableDates(updatedUnavailableDates);
                caseData.setApplicant2UnavailableDatesForTab(updatedUnavailableDates);
            }
        }
    }

    public static void rollUpUnavailabilityDatesForApplicantDJ(CaseData caseData) {
        if (caseData.getHearingSupportRequirementsDJ() != null
            && YES.equals(caseData.getHearingSupportRequirementsDJ().getHearingUnavailableDates())) {
            List<UnavailableDate> unavailableDates = new ArrayList<>();
            List<HearingDates> unavailableDatesDJ = unwrapElements(caseData.getHearingSupportRequirementsDJ().getHearingDates());
            LocalDate dateAdded = LocalDate.now();

            for (HearingDates unavailableDate : unavailableDatesDJ) {
                LocalDate fromDate = unavailableDate.getHearingUnavailableFrom();
                LocalDate toDate = unavailableDate.getHearingUnavailableUntil();
                UnavailableDateType type = fromDate.isEqual(toDate) ? SINGLE_DATE : DATE_RANGE;

                if (SINGLE_DATE.equals(type)) {
                    UnavailableDate unavailableDate1 =  new UnavailableDate();
                    unavailableDate1.setEventAdded(DJ_EVENT);
                    unavailableDate1.setDateAdded(dateAdded);
                    unavailableDate1.setDate(fromDate);
                    unavailableDate1.setUnavailableDateType(type);
                    unavailableDates.add(unavailableDate1);
                } else {
                    UnavailableDate unavailableDate1 =  new UnavailableDate();
                    unavailableDate1.setEventAdded(DJ_EVENT);
                    unavailableDate1.setDateAdded(dateAdded);
                    unavailableDate1.setFromDate(fromDate);
                    unavailableDate1.setToDate(toDate);
                    unavailableDate1.setUnavailableDateType(type);
                    unavailableDates.add(unavailableDate1);
                }
            }

            Party applicant1 = caseData.getApplicant1();
            applicant1.setUnavailableDates(wrapElements(unavailableDates));
            caseData.setApplicant1(applicant1);

            caseData.setApplicant1UnavailableDatesForTab(wrapElements(unavailableDates));

            if (caseData.getApplicant2() != null) {
                Party applicant2 = caseData.getApplicant1();
                applicant2.setUnavailableDates(wrapElements(unavailableDates));
                caseData.setApplicant2(applicant2);
                caseData.setApplicant2UnavailableDatesForTab(wrapElements(unavailableDates));
            }
        }
    }

    private static List<Element<UnavailableDate>> addEventAndDate(LocalDate dateAdded,
                                                                  String eventAdded,
                                                                  List<Element<UnavailableDate>> unavailableDates) {
        List<Element<UnavailableDate>> updatedUnavailableDates = new ArrayList<>();
        for (Element<UnavailableDate> date : unavailableDates) {
            UnavailableDate updated = new UnavailableDate();
            updated.setDate(date.getValue().getDate());
            updated.setDateAdded(dateAdded);
            updated.setEventAdded(eventAdded);
            updated.setUnavailableDateType(date.getValue().getUnavailableDateType());
            updated.setFromDate(date.getValue().getFromDate());
            updated.setToDate(date.getValue().getToDate());
            updatedUnavailableDates.addAll(wrapElements(updated));
        }
        return updatedUnavailableDates;
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

    public static CaseData copyDatesIntoListingTabFields(CaseData caseData) {
        if (caseData.getApplicant1() != null
            && caseData.getApplicant1().getUnavailableDates() != null) {
            copyDatesIntoListingTabFieldsForApplicant(caseData, true);
        }

        if (caseData.getApplicant2() != null
            && caseData.getApplicant2().getUnavailableDates() != null) {
            copyDatesIntoListingTabFieldsForApplicant(caseData, false);
        }

        if (caseData.getRespondent1() != null
            && caseData.getRespondent1().getUnavailableDates() != null) {
            copyDatesIntoListingTabFieldsForRespondent1(caseData);
        }

        if (caseData.getRespondent2() != null
            && caseData.getRespondent2().getUnavailableDates() != null) {
            copyDatesIntoListingTabFieldsForRespondent2(caseData);
        }
        return caseData;
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
                    UnavailableDate updatedDate = new UnavailableDate(
                        existingDate.getValue().getWho(),
                        existingDate.getValue().getDate(),
                        existingDate.getValue().getFromDate(),
                        existingDate.getValue().getToDate(),
                        existingDate.getValue().getUnavailableDateType(),
                        event,
                        date
                    );
                    updatedUnavailableDates.addAll(wrapElements(updatedDate));
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
        if (isClaimantIntentionEvent(caseData) && caseData.getApplicant1ResponseDate() != null) {
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
                .applicant1(caseData.getApplicant1().setUnavailableDates(dates))
                .applicant1UnavailableDatesForTab(dates);
        } else {
            updatedData
                .applicant2(caseData.getApplicant2().setUnavailableDates(dates))
                .applicant2UnavailableDatesForTab(dates);
        }
    }

    private static void copyDatesIntoListingTabFieldsForApplicant(CaseData caseData, boolean isApplicant1) {
        String eventAdded = null;
        LocalDate dateAdded = null;

        if (isClaimantIntentionEvent(caseData)) {
            eventAdded = CLAIMANT_INTENTION_EVENT;
            dateAdded = caseData.getApplicant1ResponseDate().toLocalDate();
        } else if (caseData.getHearingSupportRequirementsDJ() != null
            && YES.equals(caseData.getHearingSupportRequirementsDJ().getHearingUnavailableDates())) {
            eventAdded = DJ_EVENT;
            dateAdded = caseData.getDefaultJudgmentDocuments().get(0).getValue().getCreatedDatetime().toLocalDate();
        }
        // Todo: dates only considered from applicant 1. is it correct?
        List<Element<UnavailableDate>> dates = getExistingDates(
                caseData.getApplicant1().getUnavailableDates(),
                eventAdded,
                dateAdded
            );

        if (isApplicant1) {
            caseData.setApplicant1(caseData.getApplicant1().setUnavailableDates(dates));
            caseData.setApplicant1UnavailableDatesForTab(dates);
        } else {
            caseData.setApplicant2(caseData.getApplicant2().setUnavailableDates(dates));
            caseData.setApplicant2UnavailableDatesForTab(dates);
        }
    }

    private static void copyDatesIntoListingTabFieldsForRespondent1(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        String eventAdded = null;
        LocalDate dateAdded = null;

        // No DJ check because only claimant solicitor can run DJ
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1DQ().getHearing() != null
            && YES.equals(caseData.getRespondent1DQ().getHearing().getUnavailableDatesRequired())
            && caseData.getRespondent1ResponseDate() != null) {
            eventAdded = DEFENDANT_RESPONSE_EVENT;
            dateAdded = caseData.getRespondent1ResponseDate().toLocalDate();
        }

        List<Element<UnavailableDate>> dates = getExistingDates(
            caseData.getRespondent1().getUnavailableDates(),
            eventAdded,
            dateAdded
        );

        updatedData
            .respondent1(caseData.getRespondent1().setUnavailableDates(dates))
            .respondent1UnavailableDatesForTab(dates);
    }

    private static void copyDatesIntoListingTabFieldsForRespondent1(CaseData caseData) {
        String eventAdded = null;
        LocalDate dateAdded = null;

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

        caseData.setRespondent1(caseData.getRespondent1().setUnavailableDates(dates));
        caseData.setRespondent1UnavailableDatesForTab(dates);
    }

    private static void copyDatesIntoListingTabFieldsForRespondent2(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        String eventAdded = null;
        LocalDate dateAdded = null;

        // No DJ check because only claimant solicitor can run DJ
        if (caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getHearing() != null
            && YES.equals(caseData.getRespondent2DQ().getHearing().getUnavailableDatesRequired())
            && caseData.getRespondent2ResponseDate() != null) {
            eventAdded = DEFENDANT_RESPONSE_EVENT;
            dateAdded = caseData.getRespondent2ResponseDate().toLocalDate();
        }

        List<Element<UnavailableDate>> dates = getExistingDates(
            caseData.getRespondent2().getUnavailableDates(),
            eventAdded,
            dateAdded
        );

        updatedData
            .respondent2(caseData.getRespondent2().setUnavailableDates(dates))
            .respondent2UnavailableDatesForTab(dates);
    }

    private static void copyDatesIntoListingTabFieldsForRespondent2(CaseData caseData) {
        String eventAdded = null;
        LocalDate dateAdded = null;

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

        caseData.setRespondent2(caseData.getRespondent2().setUnavailableDates(dates));
        caseData.setRespondent2UnavailableDatesForTab(dates);
    }

    public static void updateMissingUnavailableDatesForApplicants(CaseData caseData) {
        if (isClaimantIntentionEvent(caseData)) {
            rollUpUnavailabilityDatesForApplicant(caseData);
        } else {
            rollUpUnavailabilityDatesForApplicantDJ(caseData);
        }
    }

    public static boolean shouldUpdateApplicant1UnavailableDates(CaseData caseData) {
        return caseData.getApplicant1().getUnavailableDates() != null
            && caseData.getApplicant1().getUnavailableDates().get(0).getValue().getDateAdded() == null;
    }

    public static boolean shouldUpdateApplicant2UnavailableDates(CaseData caseData) {
        return caseData.getApplicant2() != null
            && caseData.getApplicant2().getUnavailableDates() != null
            && caseData.getApplicant2().getUnavailableDates().get(0).getValue().getDateAdded() == null;
    }

    public static boolean shouldUpdateRespondent1UnavailableDates(CaseData caseData) {
        return caseData.getRespondent1().getUnavailableDates() != null
            && caseData.getRespondent1().getUnavailableDates().get(0).getValue().getDateAdded() == null;
    }

    public static boolean shouldUpdateRespondent2UnavailableDates(CaseData caseData) {
        return caseData.getRespondent2() != null
            && caseData.getRespondent2().getUnavailableDates() != null
            && caseData.getRespondent2().getUnavailableDates().get(0).getValue().getDateAdded() == null;
    }
}
