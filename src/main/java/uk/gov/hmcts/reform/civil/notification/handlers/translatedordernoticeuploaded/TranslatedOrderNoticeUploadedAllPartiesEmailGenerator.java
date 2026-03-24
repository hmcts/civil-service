package uk.gov.hmcts.reform.civil.notification.handlers.translatedordernoticeuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class TranslatedOrderNoticeUploadedAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public TranslatedOrderNoticeUploadedAllPartiesEmailGenerator(
        TranslatedOrderNoticeUploadedClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        TranslatedOrderNoticeUploadedDefendantEmailDTOGenerator defendantEmailDTOGenerator) {

        super(List.of(claimantEmailDTOGenerator,
                      defendantEmailDTOGenerator));
    }
}
