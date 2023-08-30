package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.EventAddedEvents.CLAIMANT_INTENTION_EVENT;
import static uk.gov.hmcts.reform.civil.enums.EventAddedEvents.DEFENDANT_RESPONSE_EVENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class WitnessUtils {

    private WitnessUtils() {
        //NO-OP
    }

    private static Witnesses addEventAndDateToWitnesses(Witnesses witnesses, LocalDate dateAdded, String eventAdded) {
        List<Witness> witnessList = unwrapElements(witnesses.getDetails());
        List<Element<Witness>> updatedWitnessDetails = new ArrayList<>();
        for (Witness witness : witnessList) {
            updatedWitnessDetails.addAll(wrapElements(witness.toBuilder()
                                                          .eventAdded(eventAdded)
                                                          .dateAdded(dateAdded).build()));
        }
        return witnesses.toBuilder().details(updatedWitnessDetails).build();
    }

    public static void addEventAndDateAddedToRespondentWitnesses(CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData caseData = builder.build();
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1DQ().getRespondent1DQWitnesses() != null
            && caseData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails() != null
            && !caseData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails().isEmpty()) {
            Witnesses respondent1DQWitnesses = caseData.getRespondent1DQ().getRespondent1DQWitnesses();
            Witnesses updatedRespondent1Witnesses = addEventAndDateToWitnesses(
                respondent1DQWitnesses,
                caseData.getRespondent1ResponseDate().toLocalDate(),
                DEFENDANT_RESPONSE_EVENT.getValue()
            );
            builder.respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                      .respondent1DQWitnesses(updatedRespondent1Witnesses).build());

            // copy in respondent2 for 1v2SS single response
            if (caseData.getRespondent2() != null
                && YES.equals(caseData.getRespondent2SameLegalRepresentative())
                && YES.equals(caseData.getRespondentResponseIsSame())) {
                if (caseData.getRespondent2DQ() == null) {
                    builder.respondent2DQ(Respondent2DQ.builder()
                                              .respondent2DQWitnesses(updatedRespondent1Witnesses)
                                              .build());
                } else {
                    builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                              .respondent2DQWitnesses(updatedRespondent1Witnesses).build());
                }
            }
        }

        if (caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null
            && caseData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails() != null
            && !caseData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails().isEmpty()) {
            Witnesses respondent2DQWitnesses = caseData.getRespondent2DQ().getRespondent2DQWitnesses();
            Witnesses updatedRespondent2Witnesses = addEventAndDateToWitnesses(
                respondent2DQWitnesses,
                caseData.getRespondent2ResponseDate().toLocalDate(),
                DEFENDANT_RESPONSE_EVENT.getValue()
            );
            builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                          .respondent2DQWitnesses(updatedRespondent2Witnesses).build());
        }
    }

    public static void addEventAndDateAddedToApplicantWitnesses(CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData caseData = builder.build();
        if (caseData.getApplicant1DQ() != null
            && caseData.getApplicant1DQ().getApplicant1DQWitnesses() != null
            && caseData.getApplicant1DQ().getApplicant1DQWitnesses().getDetails() != null
            && !caseData.getApplicant1DQ().getApplicant1DQWitnesses().getDetails().isEmpty()) {
            Witnesses applicant1DQWitnesses = caseData.getApplicant1DQ().getApplicant1DQWitnesses();
            Witnesses updatedApplicant1Witnesses = addEventAndDateToWitnesses(
                applicant1DQWitnesses,
                caseData.getApplicant1ResponseDate().toLocalDate(),
                CLAIMANT_INTENTION_EVENT.getValue()
            );
            builder.applicant1DQ(caseData.getApplicant1DQ().toBuilder()
                                         .applicant1DQWitnesses(updatedApplicant1Witnesses).build());

            // copy in applicant 2 for single response
            if (caseData.getApplicant2() != null
                && ((YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1()))
                || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1()))) {
                if (caseData.getApplicant2DQ() == null) {
                    builder.applicant2DQ(Applicant2DQ.builder()
                                             .applicant2DQWitnesses(updatedApplicant1Witnesses)
                                             .build());
                } else {
                    builder.applicant2DQ(caseData.getApplicant2DQ().toBuilder()
                                             .applicant2DQWitnesses(updatedApplicant1Witnesses).build());
                }
            }
        }

        if (caseData.getApplicant2DQ() != null
            && caseData.getApplicant2DQ().getApplicant2DQWitnesses() != null
            && caseData.getApplicant2DQ().getApplicant2DQWitnesses().getDetails() != null
            && !caseData.getApplicant2DQ().getApplicant2DQWitnesses().getDetails().isEmpty()) {
            Witnesses applicant2DQWitnesses = caseData.getApplicant2DQ().getApplicant2DQWitnesses();
            Witnesses updatedApplicant2Witnesses = addEventAndDateToWitnesses(
                applicant2DQWitnesses,
                caseData.getApplicant2ResponseDate().toLocalDate(),
                CLAIMANT_INTENTION_EVENT.getValue()
            );
            builder.applicant2DQ(caseData.getApplicant2DQ().toBuilder()
                                         .applicant2DQWitnesses(updatedApplicant2Witnesses).build());
        }
    }
}
