const load = require;
const fs = require('fs');
const path = require('path');

const definitionsRoot = path.resolve(__dirname, '../../../../ccd-definition');
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

// Please update this map whenever exclusions are updated in build-release-ccd-definition.sh
// without the *
const exclusions = new Map([
  ['preview', ['-prod.json']],
  ['demo', ['UserProfile.json', '-prod.json']],
  ['aat', ['UserProfile.json', '-prod.json']],
  ['prod', ['UserProfile.json', '-nonprod.json', '-testing.json']],
  ['staging', ['UserProfile.json', '-nonprod.json', '-testing.json']],
]);

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

processDir = (filePath, withConfig) => {
  const fileNames = fs.readdirSync(path.resolve(__dirname, filePath));
  if (!Object.prototype.toString.call(fileNames) === '[object Array]') {
    const currentObject = path.resolve(__dirname, `${filePath}/${fileNames}`);
    const stat = fs.statSync(currentObject);
    if (stat.isFile()) {
      if (!(exclusions.get(withConfig).filter(ext => fileNames.includes(ext)).length > 0)) {
        const content = Object.assign(load(currentObject), []);
        fieldsArray = [...fieldsArray, ...content];
      }
    } else if (stat.isDirectory()) {
      processDir(currentObject, withConfig);
    }
  } else {
    fileNames.forEach(filename => {
      const currentObject = path.resolve(__dirname, `${filePath}/${filename}`);
      const stat = fs.statSync(currentObject);
      if (stat.isFile()) {
        if (!(exclusions.get(withConfig).filter(ext => filename.includes(ext)).length > 0)) {
          const content = Object.assign(load(currentObject), []);
          if (Object.prototype.toString.call(content) === '[object Array]') {
            fieldsArray = [...fieldsArray, ...content];
          } else {
            fieldsArray.push(content);
          }
        }
      } else if (stat.isDirectory()) {
        processDir(currentObject, withConfig);
      }
    });
  }
};

module.exports = {
  getConfig,
  exclusions,
  ccdData
};
