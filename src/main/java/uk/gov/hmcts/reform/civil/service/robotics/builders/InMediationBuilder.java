package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;

import java.util.List;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.RPA_IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.isStayClaim;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventDetailsText;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.setApplicant1ResponseDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMediationBuilder extends BaseEventBuilder {

    private EventHistoryMapper mapper;

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {

        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildClaimInMediation(builder, caseData);
    }

    private void buildClaimInMediation(EventHistory.EventHistoryBuilder builder,
                                       CaseData caseData) {

        if (caseData.hasDefendantAgreedToFreeMediation() && caseData.hasClaimantAgreedToFreeMediation()) {

            buildClaimantDirectionQuestionnaireForSpec(builder, caseData);

            builder.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(setApplicant1ResponseDate(caseData))
                    .eventDetailsText(RPA_IN_MEDIATION)
                    .eventDetails(EventDetails.builder()
                        .miscText(RPA_IN_MEDIATION)
                        .build())
                    .build());
        }
    }

    private void buildClaimantDirectionQuestionnaireForSpec(EventHistory.EventHistoryBuilder builder,
                                                            CaseData caseData) {
        List<ClaimantResponseDetails> applicantDetails = prepareApplicantsDetails(caseData);

        CaseCategory claimType = caseData.getCaseAccessCategory();

        if (SPEC_CLAIM.equals(claimType)) {
            List<Event> dqForProceedingApplicantsSpec = IntStream.range(0, applicantDetails.size())
                .mapToObj(index ->
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
                        .dateReceived(applicantDetails.get(index).getResponseDate())
                        .litigiousPartyID(applicantDetails.get(index).getLitigiousPartyID())
                        .eventDetails(EventDetails.builder()
                            .stayClaim(isStayClaim(applicantDetails.get(index).getDq()))
                            .preferredCourtCode(
                                getPreferredCourtCode(caseData.getApplicant1DQ()))
                            .preferredCourtName("")
                            .build())
                        .eventDetailsText(prepareEventDetailsText(
                            applicantDetails.get(index).getDq(),
                            getPreferredCourtCode(caseData.getApplicant1DQ())
                        ))
                        .build())
                .toList();
            builder.directionsQuestionnaireFiled(dqForProceedingApplicantsSpec);
        }
    }
}
