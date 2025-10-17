package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.RespondentsResponsesUtil.isRespondentsResponseSatisfied;

class RespondentsResponsesUtilTest {

    @Test
    void shouldReturnFalseWhenNoSolicitorsOrResponsesPresent() {
        GeneralApplicationCaseData baseCaseData = GeneralApplicationCaseData.builder()
            .generalAppRespondentSolicitors(null)
            .respondentsResponses(null)
            .build();

        assertThat(isRespondentsResponseSatisfied(baseCaseData, baseCaseData))
            .isFalse();
    }

    @Test
    void shouldReturnTrueForOneVOneResponse() {
        UUID solicitorId = UUID.randomUUID();
        Element<GASolicitorDetailsGAspec> solicitor = Element.<GASolicitorDetailsGAspec>builder()
            .id(solicitorId)
            .value(GASolicitorDetailsGAspec.builder()
                       .id(solicitorId.toString())
                       .organisationIdentifier("ORG-1")
                       .build())
            .build();

        GeneralApplicationCaseData baseCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withIsMultiParty(YesOrNo.NO)
            .withGeneralAppRespondentSolicitors(List.of(solicitor))
            .build();

        Element<GARespondentResponse> response = Element.<GARespondentResponse>builder()
            .id(UUID.randomUUID())
            .value(GARespondentResponse.builder()
                       .gaRespondentDetails(solicitorId.toString())
                       .build())
            .build();

        GeneralApplicationCaseData updatedCaseData = baseCaseData.toBuilder()
            .respondentsResponses(List.of(response))
            .build();

        assertThat(isRespondentsResponseSatisfied(baseCaseData, updatedCaseData)).isTrue();
    }

    @Test
    void shouldReturnTrueForTwoSolicitorsWhenBothRespond() {
        UUID solicitor1Id = UUID.randomUUID();
        UUID solicitor2Id = UUID.randomUUID();

        Element<GASolicitorDetailsGAspec> solicitorOne = Element.<GASolicitorDetailsGAspec>builder()
            .id(solicitor1Id)
            .value(GASolicitorDetailsGAspec.builder()
                       .id(solicitor1Id.toString())
                       .organisationIdentifier("ORG-1")
                       .build())
            .build();

        Element<GASolicitorDetailsGAspec> solicitorTwo = Element.<GASolicitorDetailsGAspec>builder()
            .id(solicitor2Id)
            .value(GASolicitorDetailsGAspec.builder()
                       .id(solicitor2Id.toString())
                       .organisationIdentifier("ORG-2")
                       .build())
            .build();

        GeneralApplicationCaseData baseCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withIsMultiParty(YesOrNo.YES)
            .withGeneralAppRespondentSolicitors(List.of(solicitorOne, solicitorTwo))
            .build();

        Element<GARespondentResponse> responseOne = Element.<GARespondentResponse>builder()
            .id(UUID.randomUUID())
            .value(GARespondentResponse.builder()
                       .gaRespondentDetails(solicitor1Id.toString())
                       .build())
            .build();

        Element<GARespondentResponse> responseTwo = Element.<GARespondentResponse>builder()
            .id(UUID.randomUUID())
            .value(GARespondentResponse.builder()
                       .gaRespondentDetails(solicitor2Id.toString())
                       .build())
            .build();

        GeneralApplicationCaseData updatedCaseData = baseCaseData.toBuilder()
            .respondentsResponses(List.of(responseOne, responseTwo))
            .build();

        assertThat(isRespondentsResponseSatisfied(baseCaseData, updatedCaseData)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOnlyOneRespondentRepliesInMultiParty() {
        UUID solicitor1Id = UUID.randomUUID();
        UUID solicitor2Id = UUID.randomUUID();

        Element<GASolicitorDetailsGAspec> solicitorOne = Element.<GASolicitorDetailsGAspec>builder()
            .id(solicitor1Id)
            .value(GASolicitorDetailsGAspec.builder()
                       .id(solicitor1Id.toString())
                       .organisationIdentifier("ORG-1")
                       .build())
            .build();

        Element<GASolicitorDetailsGAspec> solicitorTwo = Element.<GASolicitorDetailsGAspec>builder()
            .id(solicitor2Id)
            .value(GASolicitorDetailsGAspec.builder()
                       .id(solicitor2Id.toString())
                       .organisationIdentifier("ORG-2")
                       .build())
            .build();

        GeneralApplicationCaseData baseCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withIsMultiParty(YesOrNo.YES)
            .withGeneralAppRespondentSolicitors(List.of(solicitorOne, solicitorTwo))
            .build();

        Element<GARespondentResponse> response = Element.<GARespondentResponse>builder()
            .id(UUID.randomUUID())
            .value(GARespondentResponse.builder()
                       .gaRespondentDetails(solicitor1Id.toString())
                       .build())
            .build();

        GeneralApplicationCaseData updatedCaseData = baseCaseData.toBuilder()
            .respondentsResponses(List.of(response))
            .build();

        assertThat(isRespondentsResponseSatisfied(baseCaseData, updatedCaseData)).isFalse();
    }
}
