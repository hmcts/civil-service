package uk.gov.hmcts.reform.civil.service.ga;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Service
@RequiredArgsConstructor
public class GaInitiateGeneralApplicationService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final InitiateGeneralApplicationService delegate;
    private final GaInitiateGeneralApplicationHelper helper;
    private final ObjectMapper objectMapper;

    public boolean respondentAssigned(GeneralApplicationCaseData gaCaseData) {
        return helper.respondentAssigned(gaCaseData);
    }

    public boolean caseContainsLip(GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData == null) {
            return false;
        }

        if (helper.hasExplicitLipFlag(gaCaseData)) {
            return true;
        }

        return delegate.caseContainsLiP(asCaseData(gaCaseData));
    }

    public boolean isGaApplicantSameAsParentCaseClaimant(GeneralApplicationCaseData gaCaseData, String authToken) {
        return helper.isGaApplicantSameAsParentCaseClaimant(gaCaseData, authToken);
    }

    public GeneralApplicationCaseData buildCaseData(GeneralApplicationCaseData gaCaseData,
                                                    UserDetails userDetails,
                                                    String authToken) {
        GeneralApplicationCaseData defaults = helper.ensureDefaults(gaCaseData);
        CaseData mapped = asCaseData(defaults);
        CaseData updated = delegate.buildCaseData(mapped.toBuilder(), mapped, userDetails, authToken);
        GeneralApplicationCaseData overlay = helper.ensureDefaults(overlay(defaults, updated));
        List<Element<GeneralApplication>> applications = helper.buildApplications(overlay);
        return overlay.toBuilder()
            .generalApplications(applications)
            .build();
    }

    public GeneralApplicationCaseData ensureDefaults(GeneralApplicationCaseData gaCaseData) {
        return helper.ensureDefaults(gaCaseData);
    }

    public CaseData asCaseData(GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData == null) {
            return null;
        }

        Map<String, Object> map = objectMapper.convertValue(gaCaseData, MAP_TYPE);
        helper.applyLipFlags(map, gaCaseData);
        return objectMapper.convertValue(map, CaseData.class);
    }

    public GeneralApplicationCaseData merge(GeneralApplicationCaseData original, CaseData updatedCaseData) {
        return overlay(original, updatedCaseData);
    }

    private GeneralApplicationCaseData overlay(GeneralApplicationCaseData original, CaseData updatedCaseData) {
        Map<String, Object> base = objectMapper.convertValue(original, MAP_TYPE);
        Map<String, Object> updated = objectMapper.convertValue(updatedCaseData, MAP_TYPE);
        base.putAll(updated);
        return objectMapper.convertValue(base, GeneralApplicationCaseData.class);
    }
}
