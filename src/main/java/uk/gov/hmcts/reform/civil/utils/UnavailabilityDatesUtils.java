package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

public class UnavailabilityDatesUtils {

    private UnavailabilityDatesUtils() {
                //NO-OP
    }

    public static void rollUpExpertUnavailabilityDates(CaseData.CaseDataBuilder<?, ?> builder, boolean defendantResponse) {
        CaseData caseData = builder.build();
           if (defendantResponse) {
            if (caseData.getRespondent1DQ() != null && caseData.getRespondent1DQ().getHearing() != null) {

                Party.PartyBuilder resp1 = caseData.getRespondent1().toBuilder()
                    .unavailableDates(caseData.getRespondent1DQ().getHearing()
                                          .getUnavailableDates());
                builder.respondent1(resp1.build());

            }
            if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getHearing() != null) {
                  Party.PartyBuilder resp2 = caseData.getRespondent2().toBuilder()
                    .unavailableDates(caseData.getRespondent2DQ().getHearing()
                                          .getUnavailableDates());
                builder.respondent2(resp2.build());
            }
        } else {
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
    }
}
