package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondentLiPResponse {
    private ResponseOptionsLiP responseDeadlineMoreTimeRequest;
    private AdditionalTimeOptionsLip responseDeadlineAdditionalTime;
    private LocalDate responseDeadlineAgreedResponseDeadline;
    private YesOrNo partialAdmissionAlreadyPaid;
    private String timelineComment;
    private String evidenceComment;
    private String mediationContactPerson;
    private String mediationContactPhone;
    private String determinationWithoutHearing;
    private String determinationReasonForHearing;
    private YesOrNo defendantYourSelfEvidence;
    private YesOrNo expertReportsAvailable;
    private List<ReportsDetailsLiP> expertReports;
    private String permissionToUseAnExpert;
    private ExpertCanStillExamineLiP expertCanStillExamine;
}
