package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.EventAddedEvents.CLAIMANT_INTENTION_EVENT;
import static uk.gov.hmcts.reform.civil.enums.EventAddedEvents.DEFENDANT_RESPONSE_EVENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class ExpertUtils {

    private ExpertUtils() {
        //NO-OP
    }

    private static Experts addEventAndDateToExperts(Experts experts, LocalDate dateAdded, String eventAdded) {
        List<Expert> expertList = unwrapElements(experts.getDetails());
        List<Element<Expert>> updatedExpertDetails = new ArrayList<>();
        for (Expert expert : expertList) {
            updatedExpertDetails.addAll(wrapElements(expert.toBuilder()
                .eventAdded(eventAdded)
                .dateAdded(dateAdded).build()));
        }
        return experts.toBuilder().details(updatedExpertDetails).build();
    }

    public static void addEventAndDateAddedToRespondentExperts(CaseData.CaseDataBuilder<?, ?> updatedData) {
        CaseData caseData = updatedData.build();
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1DQ().getRespondent1DQExperts() != null
            && caseData.getRespondent1DQ().getRespondent1DQExperts().getDetails() != null
            && !caseData.getRespondent1DQ().getRespondent1DQExperts().getDetails().isEmpty()) {
            Experts respondent1DQExperts = caseData.getRespondent1DQ().getRespondent1DQExperts();
            Experts updatedRespondent1Experts = addEventAndDateToExperts(
                respondent1DQExperts,
                caseData.getRespondent1ResponseDate().toLocalDate(),
                DEFENDANT_RESPONSE_EVENT.getValue()
            );
            updatedData.respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                          .respondent1DQExperts(updatedRespondent1Experts).build());

            // copy in respondent2 for 1v2SS single response
            if (caseData.getRespondent2() != null
                && YES.equals(caseData.getRespondent2SameLegalRepresentative())
                && YES.equals(caseData.getRespondentResponseIsSame())) {
                if (caseData.getRespondent2DQ() == null) {
                    updatedData.respondent2DQ(Respondent2DQ.builder()
                                                  .respondent2DQExperts(updatedRespondent1Experts)
                                                  .build());
                } else {
                    updatedData.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                                  .respondent2DQExperts(updatedRespondent1Experts).build());
                }
            }
        }

        if (caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getRespondent2DQExperts() != null
            && caseData.getRespondent2DQ().getRespondent2DQExperts().getDetails() != null
            && !caseData.getRespondent2DQ().getRespondent2DQExperts().getDetails().isEmpty()) {
            Experts respondent2DQExperts = caseData.getRespondent2DQ().getRespondent2DQExperts();
            Experts updatedRespondent2Experts = addEventAndDateToExperts(
                respondent2DQExperts,
                caseData.getRespondent2ResponseDate().toLocalDate(),
                DEFENDANT_RESPONSE_EVENT.getValue()
            );
            updatedData.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                          .respondent2DQExperts(updatedRespondent2Experts).build());
        }
    }

    public static void addEventAndDateAddedToApplicantExperts(CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData caseData = builder.build();
        if (caseData.getApplicant1DQ() != null
            && caseData.getApplicant1DQ().getApplicant1DQExperts() != null
            && caseData.getApplicant1DQ().getApplicant1DQExperts().getDetails() != null
            && !caseData.getApplicant1DQ().getApplicant1DQExperts().getDetails().isEmpty()) {
            Experts applicant1DQExperts = caseData.getApplicant1DQ().getApplicant1DQExperts();
            Experts updatedApplicant1Experts = addEventAndDateToExperts(
                applicant1DQExperts,
                caseData.getApplicant1ResponseDate().toLocalDate(),
                CLAIMANT_INTENTION_EVENT.getValue()
            );
            builder.applicant1DQ(caseData.getApplicant1DQ().toBuilder()
                                         .applicant1DQExperts(updatedApplicant1Experts).build());

            // copy in applicant 2 for single response
            if (caseData.getApplicant2() != null
                && ((YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1()))
                || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1()))) {
                if (caseData.getApplicant2DQ() == null) {
                    builder.applicant2DQ(Applicant2DQ.builder()
                                             .applicant2DQExperts(updatedApplicant1Experts)
                                             .build());
                } else {
                    builder.applicant2DQ(caseData.getApplicant2DQ().toBuilder()
                                             .applicant2DQExperts(updatedApplicant1Experts).build());
                }
            }
        }

        if (caseData.getApplicant2DQ() != null
            && caseData.getApplicant2DQ().getApplicant2DQExperts() != null
            && caseData.getApplicant2DQ().getApplicant2DQExperts().getDetails() != null
            && !caseData.getApplicant2DQ().getApplicant2DQExperts().getDetails().isEmpty()) {
            Experts applicant2DQExperts = caseData.getApplicant2DQ().getApplicant2DQExperts();
            Experts updatedApplicant2Experts = addEventAndDateToExperts(
                applicant2DQExperts,
                caseData.getApplicant2ResponseDate().toLocalDate(),
                CLAIMANT_INTENTION_EVENT.getValue()
            );
            builder.applicant2DQ(caseData.getApplicant2DQ().toBuilder()
                                         .applicant2DQExperts(updatedApplicant2Experts).build());
        }
    }
}
