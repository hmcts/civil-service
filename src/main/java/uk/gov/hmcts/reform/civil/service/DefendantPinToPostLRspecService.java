package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.AccessCodeGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class DefendantPinToPostLRspecService {
    public DefendantPinToPostLRspec buildDefendantPinToPost() {
        LocalDate expiryDate = LocalDate.now().plusDays(180);
        return DefendantPinToPostLRspec.builder()
            .accessCode(AccessCodeGenerator.generateAccessCode())
            .respondentCaseRole(
                CaseRole.RESPONDENTSOLICITORONESPEC.getFormattedName())
            .expiryDate(expiryDate)
            .pinUsed(NO)
            .build();
    }

}
