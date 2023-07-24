package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingDates;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;

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
}
