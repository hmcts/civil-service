const MAX_RETRY_TIMEOUT = 30000;

const retry = (fn, retryTimeout = 5000, remainingRetries = 10, err = null) => {
  if (!remainingRetries) {
    return Promise.reject(err);
  }
  if (retryTimeout > MAX_RETRY_TIMEOUT) {
    retryTimeout = MAX_RETRY_TIMEOUT;
  }
  return fn().catch(async err => {
    console.log(`Failed due to an error: ${err}, will try again in ${retryTimeout / 1000} seconds (Retries left: ${remainingRetries})`);
    await sleep(retryTimeout);
    return retry(fn, 2 * retryTimeout, remainingRetries - 1, err);
  });
};

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

module.exports = {retry};

