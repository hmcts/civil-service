package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * There are different texts generated for different intentions (claim created, claim responded, proceed with claim...)
 * and different kind of cases (repayment plan, full defence...) Cases are classified for each intention using a
 * different subset of its fields and possibly a complex classification.
 * <p>
 * For each intention, a different sub-interface or this one should be created. That allows to declare a
 * List&lt;ThatInterface> dependency in the user class so that Spring can populate it with the generators relevant
 * to that intention.
 * <p>
 * Within each intention, each kind of case is addressed by a different subclass of ThatInterface.
 */
public interface CaseDataToTextGenerator {

    /**
     * Checks for suitability of case data should be made as soon as possible to avoid unnecessary
     * processing. Sometimes those checks will need calculated elements that are then used in the text,
     * and that's why everything can be in one method.
     *
     * @param caseData case data to generate the text for
     * @return empty if case data is not applicable for this generator, the confirmation text if it is
     */
    Optional<String> generateTextFor(CaseData caseData);

    /**
     * When choosing a suitable text generator usually we are going to have the same kind of code. To ease those
     * calls, this method contains what's probably going to be that code.
     *
     * @param generators  a stream of generators to try
     * @param defaultText if no generator is suitable for the case, use this default text
     * @param caseData    case information
     * @return either the result of defaultText or the text of the first suitable generator returning one
     */
    static <T extends CaseDataToTextGenerator> String getTextFor(Stream<T> generators,
                             Supplier<String> defaultText,
                             CaseData caseData) {
        return generators
            .map(generator -> generator.generateTextFor(caseData))
            .filter(Optional::isPresent)
            .map(Optional::get).findFirst()
            .orElse(defaultText.get());
    }
}
