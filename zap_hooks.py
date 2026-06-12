def zap_started(zap, target):
    # Rules suppressed with documented justification (see security.md for full rationale)
    # 90033 - Loosely Scoped Cookie: cookies set by Azure App Gateway / IDAM, not civil-service (stateless)
    # 10010 - Cookie No HttpOnly Flag: same upstream cookie origin as 90033
    # 10054 - Cookie without SameSite Attribute: same upstream cookie origin as 90033
    # 10023 - Information Disclosure - Debug Error Messages: reduced by sanitised @ControllerAdvice handlers
    # 10096 - Timestamp Disclosure - Unix: false positives from numeric case IDs and epoch-based identifiers
    # 100000 - Client Error response code: 4xx expected from ZAP probing endpoints that require valid payloads/auth
    # 100001 - Unexpected Content-Type: binary document download endpoints return non-JSON content types
    rules_to_ignore = (
        '90033', '10010', '10054', '10023',
        '10096', '100000', '100001',
    )
    for rule in rules_to_ignore:
        zap.pscan.set_scanner_alert_threshold(id=rule, alertthreshold='OFF')
