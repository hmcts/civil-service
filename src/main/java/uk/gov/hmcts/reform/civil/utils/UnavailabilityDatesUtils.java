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

    public static void rollUpUnavailabilityDatesForRespondent(CaseData.CaseDataBuilder<?, ?> builder, boolean updateContactDetailsEnabled) {
        CaseData caseData = builder.build();
        if (caseData.getRespondent1DQ() != null && caseData.getRespondent1DQ().getHearing() != null
            && caseData.getRespondent1DQ().getHearing().getUnavailableDates() != null) {

            if (updateContactDetailsEnabled) {
                List<Element<UnavailableDate>> respondent1DQUnavailableDates = caseData.getRespondent1DQ().getHearing()
                    .getUnavailableDates();

                List<Element<UnavailableDate>> updatedUnavailableDates = addEventAndDate(
                    caseData.getRespondent1ResponseDate().toLocalDate(),
                    DEFENDANT_RESPONSE_EVENT,
                    respondent1DQUnavailableDates
                );

                Party.PartyBuilder resp1 = caseData.getRespondent1().toBuilder()
                    .unavailableDates(updatedUnavailableDates);
                builder.respondent1(resp1.build())
                    .respondent1UnavailableDatesForTab(updatedUnavailableDates);

                if ((getMultiPartyScenario(caseData) == ONE_V_TWO_ONE_LEGAL_REP)) {
                    Party.PartyBuilder resp2 = caseData.getRespondent2().toBuilder()
                        .unavailableDates(updatedUnavailableDates);
                    builder.respondent2(resp2.build())
                        .respondent2UnavailableDatesForTab(updatedUnavailableDates);
                }
            } else {
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
        }
        if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getHearing() != null
            && caseData.getRespondent2DQ().getHearing().getUnavailableDates() != null) {
            if (updateContactDetailsEnabled) {
                List<Element<UnavailableDate>> respondent2DQUnavailableDates = caseData.getRespondent2DQ().getHearing()
                    .getUnavailableDates();

                List<Element<UnavailableDate>> updatedUnavailableDates = addEventAndDate(
                    caseData.getRespondent1ResponseDate().toLocalDate(),
                    DEFENDANT_RESPONSE_EVENT,
                    respondent2DQUnavailableDates
                );
                Party.PartyBuilder resp2 = caseData.getRespondent2().toBuilder()
                    .unavailableDates(updatedUnavailableDates);
                builder.respondent2(resp2.build())
                    .respondent2UnavailableDatesForTab(updatedUnavailableDates);
            } else {
                Party.PartyBuilder resp2 = caseData.getRespondent2().toBuilder()
                    .unavailableDates(caseData.getRespondent2DQ().getHearing()
                                          .getUnavailableDates());
                builder.respondent2(resp2.build());
            }
        }
    }

    public static void rollUpUnavailabilityDatesForApplicant(CaseData.CaseDataBuilder<?, ?> builder, boolean updateContactDetailsEnabled) {
        CaseData caseData = builder.build();
        if (caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getHearing() != null
            && caseData.getApplicant1DQ().getHearing().getUnavailableDates() != null) {
            if (updateContactDetailsEnabled) {
                List<Element<UnavailableDate>> applicant1DQUnavailableDates = caseData.getApplicant1DQ().getHearing()
                    .getUnavailableDates();

                List<Element<UnavailableDate>> updatedUnavailableDates = addEventAndDate(
                    caseData.getApplicant1ResponseDate().toLocalDate(),
                    CLAIMANT_INTENTION_EVENT,
                    applicant1DQUnavailableDates
                );

                Party.PartyBuilder appl1 = caseData.getApplicant1().toBuilder()
                    .unavailableDates(updatedUnavailableDates);
                builder.applicant1(appl1.build())
                    .applicant1UnavailableDatesForTab(updatedUnavailableDates);

                if (caseData.getApplicant2() != null) {
                    Party.PartyBuilder appl2 = caseData.getApplicant2().toBuilder()
                        .unavailableDates(updatedUnavailableDates);
                    builder.applicant2(appl2.build())
                        .applicant2UnavailableDatesForTab(updatedUnavailableDates);
                }
            } else {
                Party.PartyBuilder appl1 = caseData.getApplicant1().toBuilder()
                    .unavailableDates(caseData.getApplicant1DQ().getHearing()
                                          .getUnavailableDates());
                builder.applicant1(appl1.build());
                if (caseData.getApplicant2() != null) {
                    Party.PartyBuilder appl2 = caseData.getApplicant2().toBuilder()
                        .unavailableDates(caseData.getApplicant1DQ().getHearing()
                                              .getUnavailableDates());
                    builder.applicant2(appl2.build());
                }
            }
        }
    }

    public static void rollUpUnavailabilityDatesForApplicantDJ(CaseData.CaseDataBuilder<?, ?> builder, boolean updateContactDetailsEnabled) {
        CaseData caseData = builder.build();
        if (caseData.getHearingSupportRequirementsDJ() != null
            && YES.equals(caseData.getHearingSupportRequirementsDJ().getHearingUnavailableDates())) {
            List<UnavailableDate> unavailableDates = new ArrayList<>();
            List<HearingDates> unavailableDatesDJ = unwrapElements(caseData.getHearingSupportRequirementsDJ().getHearingDates());

            // Date added will be the date documents were added during DJ journey
            LocalDate dateAdded = caseData.getDefaultJudgmentDocuments().get(0).getValue().getCreatedDatetime().toLocalDate();

            for (HearingDates unavailableDate : unavailableDatesDJ) {
                LocalDate fromDate = unavailableDate.getHearingUnavailableFrom();
                LocalDate toDate = unavailableDate.getHearingUnavailableUntil();
                UnavailableDateType type = fromDate.isEqual(toDate) ? SINGLE_DATE : DATE_RANGE;

                if (updateContactDetailsEnabled) {
                    if (SINGLE_DATE.equals(type)) {
                        unavailableDates.add(UnavailableDate.builder()
                                                 .eventAdded(DJ_EVENT)
                                                 .dateAdded(dateAdded)
                                                 .date(fromDate)
                                                 .unavailableDateType(type)
                                                 .build());
                    } else {
                        unavailableDates.add(UnavailableDate.builder()
                                                 .eventAdded(DJ_EVENT)
                                                 .dateAdded(dateAdded)
                                                 .fromDate(fromDate)
                                                 .toDate(toDate)
                                                 .unavailableDateType(type)
                                                 .build());
                    }
                } else {
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
            }

            builder.applicant1(caseData.getApplicant1().toBuilder()
                                   .unavailableDates(wrapElements(unavailableDates))
                                   .build());

            if (updateContactDetailsEnabled) {
                builder.applicant1UnavailableDatesForTab(wrapElements(unavailableDates));
            }

            if (caseData.getApplicant2() != null) {
                builder.applicant2(caseData.getApplicant2().toBuilder()
                                       .unavailableDates(wrapElements(unavailableDates))
                                       .build());
                if (updateContactDetailsEnabled) {
                    builder.applicant2UnavailableDatesForTab(wrapElements(unavailableDates));
                }
            }
        }
    }

    private static List<Element<UnavailableDate>> addEventAndDate(LocalDate dateAdded,
                                                                  String eventAdded,
                                                                  List<Element<UnavailableDate>> unavailableDates) {
        List<Element<UnavailableDate>> updatedUnavailableDates = new ArrayList<>();
        for (Element<UnavailableDate> date : unavailableDates) {
            updatedUnavailableDates.addAll(wrapElements(date.getValue().toBuilder()
                                                            .dateAdded(dateAdded)
                                                            .eventAdded(eventAdded).build()));
        }
        return updatedUnavailableDates;
    }
}
