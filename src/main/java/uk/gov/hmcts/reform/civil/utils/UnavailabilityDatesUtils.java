package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.hearingvalues.UnavailabilityRangeModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.enums.hearing.UnavailabilityType.ALL_DAY;

    public class UnavailabilityDatesUtils {

        private UnavailabilityDatesUtils() {
                //NO-OP
                }

        public static ArrayList<UnavailabilityRangeModel> rollUpExpertUnavailabilityDates(CaseData.CaseDataBuilder<?, ?> builder, boolean defendantResponse) {
                CaseData caseData = builder.build();
                ArrayList<UnavailabilityRangeModel> unavailabilityDates = new ArrayList<>();

                    // todo split between res 1 and 2 since it goes to each party
                        if (defendantResponse) {
                        if (caseData.getRespondent1DQ() != null && caseData.getRespondent1DQ().getHearing() != null
                                && caseData.getRespondent1DQ().getHearing().getUnavailableDates() != null) {
                                List<Element<UnavailableDate>> respondent1DQUnavailableDates = caseData.getRespondent1DQ().getHearing().getUnavailableDates();
                                getUnavailableDatesForParty(unavailabilityDates, respondent1DQUnavailableDates);
                            }
                        if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getHearing() != null
                                && caseData.getRespondent2DQ().getHearing().getUnavailableDates() != null) {
                                List<Element<UnavailableDate>> respondent2DQUnavailableDates = caseData.getRespondent2DQ().getHearing().getUnavailableDates();
                                getUnavailableDatesForParty(unavailabilityDates, respondent2DQUnavailableDates);
                            }
                    } else {
                        if (caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getHearing() != null
                                && caseData.getApplicant1DQ().getHearing().getUnavailableDates() != null) {
                                List<Element<UnavailableDate>> applicant1DQUnavailableDates = caseData.getApplicant1DQ().getHearing().getUnavailableDates();
                                getUnavailableDatesForParty(unavailabilityDates, applicant1DQUnavailableDates);
                            }
                        if (caseData.getApplicant2DQ() != null && caseData.getApplicant2DQ().getHearing() != null
                                && caseData.getApplicant2DQ().getHearing().getUnavailableDates() != null) {
                                List<Element<UnavailableDate>> applicant2DQUnavailableDates = caseData.getApplicant2DQ().getHearing().getUnavailableDates();
                                getUnavailableDatesForParty(unavailabilityDates, applicant2DQUnavailableDates);
                            }
                    }
                return unavailabilityDates;
            }

        private static void getUnavailableDatesForParty(ArrayList<UnavailabilityRangeModel> unavailabilityDates,
                                                    List<Element<UnavailableDate>> unavailableDates) {
                for (Element<UnavailableDate> dateElement : unavailableDates) {
                        if (dateElement.getValue().getUnavailableDateType() != null) {
                                if (SINGLE_DATE.equals(dateElement.getValue().getUnavailableDateType())) {
                                        unavailabilityDates.add(buildUnavailabilityDateObject(dateElement.getValue().getDate(),
                                                                                              dateElement.getValue().getDate()));
                                    } else if (DATE_RANGE.equals(dateElement.getValue().getUnavailableDateType())) {
                                        unavailabilityDates.add(buildUnavailabilityDateObject(dateElement.getValue().getFromDate(),
                                                                                              dateElement.getValue().getToDate()));
                                    }
                            } else {
                                unavailabilityDates.add(buildUnavailabilityDateObject(dateElement.getValue().getDate(),
                                                                                                 dateElement.getValue().getDate()));
                            }
                    }
            }

        private static UnavailabilityRangeModel buildUnavailabilityDateObject(LocalDate fromDate, LocalDate toDate) {
                return UnavailabilityRangeModel.builder()
                        .unavailableFromDate(fromDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .unavailableToDate(toDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .unavailabilityType(ALL_DAY)
                        .build();
            }
}
