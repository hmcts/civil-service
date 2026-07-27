const nonRetryableError = Symbol('nonRetryableError');

export default class NonRetryableError {
  static mark<T extends Error>(error: T): T {
    Object.defineProperty(error, nonRetryableError, {
      value: true,
      enumerable: false,
      configurable: true,
    });
    return error;
  }

  static is(error: unknown): boolean {
    return Boolean(
      error &&
      typeof error === 'object' &&
      (error as Record<PropertyKey, unknown>)[nonRetryableError],
    );
  }
}
