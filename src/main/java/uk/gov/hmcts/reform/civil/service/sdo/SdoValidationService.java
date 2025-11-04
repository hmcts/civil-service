package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;
import java.util.List;

@Service
public class SdoValidationService {

    public List<String> validateSdo(CaseData caseData) {
        return Collections.emptyList();
    }
}
