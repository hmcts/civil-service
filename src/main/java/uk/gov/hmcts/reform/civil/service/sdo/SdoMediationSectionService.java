package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * Small helper that centralises the "should show" and "what text to display" logic for mediation sections
 * so templates can treat standard and DRH journeys consistently.
 */
@Service
public class SdoMediationSectionService {

    public <T> MediationSection resolve(T mediation,
                                        boolean carmEnabled,
                                        Function<T, String> textExtractor) {
        String text = mediation == null ? null : textExtractor.apply(mediation);
        boolean show = carmEnabled && mediation != null && text != null;
        return new MediationSection(show, text);
    }

    public record MediationSection(boolean show, String text) {
    }
}

