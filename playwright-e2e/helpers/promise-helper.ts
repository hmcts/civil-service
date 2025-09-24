import PromiseTimeoutError from '../errors/promise-error';

type SettledPromise<T> = {
  status: 'fulfilled' | 'rejected';
  value?: T;
  reason?: any;
};

export default class PromiseHelper {
  static async someSettled<T>(
    promises: Promise<T>[],
    count: number,
    timeout: number,
  ): Promise<SettledPromise<T>[]> {
    return new Promise((resolve) => {
      const settled: SettledPromise<T>[] = [];
      let fulfilledCount = 0;
      const remainingPromises = new Set(promises);
      // eslint-disable-next-line no-undef
      let timeoutId: NodeJS.Timeout | null = null;

      promises.forEach((promise, index) => {
        promise
          .then((value) => {
            settled[index] = { status: 'fulfilled', value };
            fulfilledCount++;
          })
          .catch((reason) => {
            settled[index] = { status: 'rejected', reason };
          })
          .finally(() => {
            remainingPromises.delete(promise);

            if (fulfilledCount >= count && timeoutId === null) {
              timeoutId = setTimeout(() => {
                for (const pendingPromise of remainingPromises) {
                  const pendingIndex = promises.indexOf(pendingPromise);
                  settled[pendingIndex] = {
                    status: 'rejected',
                    reason: new PromiseTimeoutError('Promise auto-rejected due to timeout'),
                  };
                }
                resolve(settled);
              }, timeout);
            }

            if (settled.length === promises.length) {
              if (timeoutId) clearTimeout(timeoutId);
              resolve(settled);
            }
          });
      });
    });
  }
}
