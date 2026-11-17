package uk.gov.hmcts.reform.civil.notification.handlers;

/**
 * The minimum a case must expose to be notified through the Notifier framework.
 *
 * <p>{@link Notifier} needs the case reference for logging and for error tracking, and otherwise
 * passes the case straight through to a {@link PartiesEmailGenerator}. That is the whole of the
 * framework's dependency on case data, so this is deliberately a single accessor rather than a
 * shared supertype.
 *
 * <p>It exists because {@code CaseData} and {@code GeneralApplicationCaseData} have no common
 * ancestor that declares the reference: they share only {@code MappableObject}, which declares
 * {@code toMap} alone. Both are Lombok {@code @Data} classes that already generate this getter, so
 * implementing this interface is a declaration on each and adds no code.</p>
 */
public interface NotifiableCaseData {

    Long getCcdCaseReference();
}
