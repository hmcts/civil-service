const load = require;
const fs = require('fs');
const path = require('path');

const definitionsRoot = path.resolve(__dirname, '../..');
const definitionVariants = ['civil', 'generalapplication'];

const loadFileFromDefinitions = file => {
  const results = [];
  definitionVariants.forEach(variant => {
    const fullPath = path.join(definitionsRoot, variant, `${file}.json`);
    if (fs.existsSync(fullPath)) {
      const content = Object.assign(load(fullPath), []);
      if (Object.prototype.toString.call(content) === '[object Array]') {
        results.push(...content);
      } else {
        results.push(content);
      }
    }
  });
  return results;
};

const loadFilesFromDefinitions = filePrefix => {
  const results = [];
  const loadedFiles = new Set();
  definitionVariants.forEach(variant => {
    const candidateDirs = [
      path.join(definitionsRoot, variant),
      path.join(definitionsRoot, variant, filePrefix)
    ];

    candidateDirs.forEach(definitionDir => {
      if (!fs.existsSync(definitionDir)) {
        return;
      }
      fs.readdirSync(definitionDir)
        .filter(filename => filename === `${filePrefix}.json` || filename.startsWith(`${filePrefix}-`))
        .forEach(filename => {
          const fullPath = path.join(definitionDir, filename);
          if (loadedFiles.has(fullPath)) {
            return;
          }
          loadedFiles.add(fullPath);
          const content = Object.assign(load(fullPath), []);
          if (Object.prototype.toString.call(content) === '[object Array]') {
            results.push(...content);
          } else {
            results.push(content);
          }
        });
    });
  });
  return results;
};

// The files each environment leaves out, as passed to json2xlsx -e by bin/build-civil-xlsx-definitions.sh.
// The specs run once per environment against exactly the files that environment imports. The build always
// leaves out one of the two AuthorisationCaseType shutter files; this checks the unshuttered build, which
// is what every environment runs outside a shutter.
const exclusions = new Map(
  Object.entries(load(path.join(definitionsRoot, 'exclusions.json')))
    .map(([env, patterns]) => [env, [...patterns, 'AuthorisationCaseType-shuttered.json']])
);

// json2xlsx (ccd-definition-processor) leaves out a file when any segment of its path matches a pattern
// with matcher's isMatch: '*' matches any run of characters and the match ignores case.
const toRegExp = pattern => new RegExp(
  `^${pattern.split('*').map(part => part.replace(/[.+?^${}()|[\]\\]/g, '\\$&')).join('.*')}$`, 'i'
);
const isExcluded = (name, env) => {
  if (!exclusions.has(env)) {
    throw new Error(`No exclusions for environment '${env}' in exclusions.json`);
  }
  return exclusions.get(env).some(pattern => toRegExp(pattern).test(name));
};

const ccdData = {
    Banner: loadFileFromDefinitions('Banner'),
    CaseRoles: loadFileFromDefinitions('CaseRoles'),
    CaseType: loadFileFromDefinitions('CaseType'),
    Jurisdiction: loadFileFromDefinitions('Jurisdiction'),
    SearchCasesResultFields: loadFileFromDefinitions('SearchCasesResultFields'),
    SearchInputFields: loadFileFromDefinitions('SearchInputFields'),
    SearchResultFields: loadFileFromDefinitions('SearchResultFields'),
    State: loadFilesFromDefinitions('State'),
    UserProfile: loadFileFromDefinitions('UserProfile'),
    WorkBasketInputFields: loadFileFromDefinitions('WorkBasketInputFields'),
    WorkBasketResultFields: loadFileFromDefinitions('WorkBasketResultFields')
};

function getConfig(filePath, env) {
  return getFileData(filePath, env);
}

let getFileData = [];
let processDir = [];

let fieldsArray = [];
function resolveDefinitionDirs(filePath) {
  const resolved = path.resolve(__dirname, filePath);
  if (fs.existsSync(resolved)) {
    return [resolved];
  }
  if (resolved.startsWith(`${definitionsRoot}${path.sep}`)) {
    const subPath = path.relative(definitionsRoot, resolved);
    return definitionVariants
      .map(variant => path.join(definitionsRoot, variant, subPath))
      .filter(candidate => fs.existsSync(candidate));
  }
  return [];
}

getFileData = (filePath, env) => {
  fieldsArray = [];
  const definitionDirs = resolveDefinitionDirs(filePath);
  if (!definitionDirs.length) {
    return fieldsArray;
  }
  definitionDirs.forEach(definitionDir => {
    processDir(definitionDir, env);
  });
  return fieldsArray;
};

processDir = (filePath, env) => {
  fs.readdirSync(path.resolve(__dirname, filePath)).forEach(filename => {
    if (isExcluded(filename, env)) {
      return;
    }
    const currentObject = path.resolve(__dirname, `${filePath}/${filename}`);
    const stat = fs.statSync(currentObject);
    if (stat.isFile()) {
      const content = Object.assign(load(currentObject), []);
      if (Object.prototype.toString.call(content) === '[object Array]') {
        fieldsArray = [...fieldsArray, ...content];
      } else {
        fieldsArray.push(content);
      }
    } else if (stat.isDirectory()) {
      processDir(currentObject, env);
    }
  });
};

module.exports = {
  getConfig,
  exclusions,
  ccdData
};
