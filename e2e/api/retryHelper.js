const MAX_RETRY_TIMEOUT = 30000;

const retry = (fn, remainingRetries = 5, retryTimeout = 2000, err = null) => {
  if (!remainingRetries) {
    return Promise.reject(err);
  }
  if (retryTimeout > MAX_RETRY_TIMEOUT) {
    retryTimeout = MAX_RETRY_TIMEOUT;
  }
  return fn().catch(async err => {
    console.log(`${err.message}, retrying in ${retryTimeout / 1000} seconds (Retries left: ${remainingRetries})`);
    await sleep(retryTimeout);
    return retry(fn, remainingRetries - 1, retryTimeout * 2, err);
  });
};

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

module.exports = {retry};

