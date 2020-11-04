const retry = (fn, remainingRetries = 3, err = null) => {
  if (!remainingRetries) {
    return Promise.reject(err);
  }

  return fn().catch(async err => {
    await sleep(3000);
    console.log('Retrying due to an error: ' + err);
    return retry(fn, remainingRetries - 1, err);
  });
};

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

module.exports = {retry};

