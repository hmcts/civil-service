const LATEST_MAC = 'macOS 10.15';
const LATEST_WINDOWS = 'Windows 10';

const supportedBrowsers = {
  // microsoft: {
  //   ie11_win: {
  //     browserName: 'internet explorer',
  //     platformName: LATEST_WINDOWS,
  //     browserVersion: 'latest',
  //     'sauce:options': {
  //       name: 'Civil: IE11',
  //       screenResolution: '1400x1050',
  //     },
  //   },
  // },

  // passing:  safari, chrome_win_latest, chrome_mac_latest, firefox_win
  //failing: ie11-win, firefox_mac
  safari: {
    safari_mac_latest: {
      browserName: 'safari',
      platformName: 'macOS 11.00',
      browserVersion: 'latest',
      'sauce:options': {
        name: 'Civil: MAC_SAFARI',
        seleniumVersion: '3.141.59',
        screenResolution: '1376x1032',
      },
    },
  },
  chrome: {
    chrome_win_latest: {
      browserName: 'chrome',
      platformName: LATEST_WINDOWS,
      browserVersion: 'latest',
      'sauce:options': {
        name: 'Civil: WIN_CHROME_LATEST',
      },
    },
    chrome_mac_latest: {
      browserName: 'chrome',
      platformName: LATEST_MAC,
      browserVersion: 'latest',
      'sauce:options': {
        name: 'Civil: MAC_CHROME_LATEST',
      },
    },
  },
  firefox: {
    firefox_win_latest: {
      browserName: 'firefox',
      platformName: LATEST_WINDOWS,
      browserVersion: 'latest',
      'sauce:options': {
        name: 'Civil: WIN_FIREFOX_LATEST',
      },
    },
  },
  // firefox_mac_latest: {
  //   browserName: 'firefox',
  //   platformName: LATEST_MAC,
  //   browserVersion: 'latest',
  //   'sauce:options': {
  //     name: 'Civil: MAC_FIREFOX_LATEST',
  //   },
  // },
  //},
};


module.exports = supportedBrowsers;
