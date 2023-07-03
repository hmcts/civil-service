package uk.gov.hmcts.reform.civil.utils;

import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
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
            List<UnavailableDate> newUd = addEventAndDate(caseData.getApplicant1DQ().getHearing()
                                                              .getUnavailableDates(), "Claimant Response");

            builder.applicant1(caseData.getApplicant1().toBuilder()
                .unavailableDates(wrapElements(newUd)).build());
        }
        if (caseData.getApplicant2() != null && caseData.getApplicant1DQ().getHearing() != null) {

            Party.PartyBuilder appl2 = caseData.getApplicant2().toBuilder()
                .unavailableDates(caseData.getApplicant1DQ().getHearing()
                                      .getUnavailableDates());
            builder.applicant2(appl2.build());
        }
    }

    @NotNull
    public static List<UnavailableDate> addEventAndDate(List<Element<UnavailableDate>> unavailableDates, String event) {
        List<UnavailableDate> newUd = new ArrayList<>();

        for (Element<UnavailableDate> unavailableDate : unavailableDates) {
            if (unavailableDate.getValue().getWhereAdded() == null && unavailableDate.getValue().getDateAdded() == null) {
                newUd.add(unavailableDate.getValue().toBuilder().dateAdded(LocalDate.now()).whereAdded(event).build());
            } else {
                newUd.add(unavailableDate.getValue());
            }
        }
        if (!("Claimant Response").equals(event)) {
            newUd.add(UnavailableDate.builder()
                          .date(LocalDate.of(2023,9,2))
                          .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                          .dateAdded(LocalDate.now())
                          .whereAdded(event)
                          .build());
        }
        return newUd;
    }
}
