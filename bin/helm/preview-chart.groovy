// Generate the downstream chart from the same source/version as the slim Flux chart.
def copyChart(String destination) {
  dir(destination) {
    deleteDir()
  }
  withEnv(["PREVIEW_CHART_DIRECTORY=${destination}"]) {
    sh '''
      set -eu
      mkdir -p "$PREVIEW_CHART_DIRECTORY"
      cp -R charts/civil-service/. "$PREVIEW_CHART_DIRECTORY/"
      rm -rf "$PREVIEW_CHART_DIRECTORY/charts"
      rm -f "$PREVIEW_CHART_DIRECTORY/Chart.lock"
    '''
  }
}

// Compare the whole PR, including earlier commits on a rebuilt branch.
def previewCheckRequired() {
  sh 'git fetch --no-tags origin "refs/heads/$CHANGE_TARGET"'
  def base = sh(script: 'git merge-base HEAD FETCH_HEAD', returnStdout: true).trim()
  def changed = sh(
    script: "git diff --name-only ${base} HEAD -- charts/civil-service bin/helm/preview-chart.groovy",
    returnStdout: true
  ).readLines()
  if (changed.contains('charts/civil-service/Chart.yaml')) {
    def previous = readYaml(text: sh(script: "git show ${base}:charts/civil-service/Chart.yaml", returnStdout: true))
    def current = readYaml(file: 'charts/civil-service/Chart.yaml')
    if ((previous.dependencies ?: []) != (current.dependencies ?: [])) {
      def target = readYaml(text: sh(script: 'git show FETCH_HEAD:charts/civil-service/Chart.yaml', returnStdout: true))
      if (compareVersions(v1: current.version, v2: target.version) <= 0) {
        error("Chart dependencies changed. Bump charts/civil-service/Chart.yaml above ${target.version} to publish them.")
      }
    }
  }
  return !changed.isEmpty()
}

def prepareSlim(String destination) {
  copyChart(destination)
  def chart = readYaml(file: "${destination}/Chart.yaml")
  def skipped = (chart.annotations?.get('hmcts.github.io/skip-publish') ?: '')
    .split(',').collect { it.trim() }.findAll { it }
  def dependencies = chart.dependencies ?: []
  def unknown = skipped - dependencies.collect { it.alias ?: it.name }
  if (unknown) {
    error("Unknown skip-publish dependencies: ${unknown.join(', ')}")
  }
  chart.dependencies = dependencies.findAll { !skipped.contains(it.alias ?: it.name) }
  writeYaml(file: "${destination}/Chart.yaml", data: chart, overwrite: true)
}

def prepare(String destination) {
  copyChart(destination)
  def chart = readYaml(file: "${destination}/Chart.yaml")
  chart.name = 'civil-service-preview'
  chart.annotations?.remove('hmcts.github.io/skip-publish')
  writeYaml(file: "${destination}/Chart.yaml", data: chart, overwrite: true)
  // helmPublish derives IMAGE_NAME from the chart name. Use the source chart's application image.
  def image = readYaml(file: "${destination}/values.yaml").java.image
  dir(destination) {
    findFiles(glob: 'values.*.template.yaml').each { template ->
      writeFile(file: template.path, text: readFile(template.path).replace('${IMAGE_NAME}', image))
    }
  }
}

// Exercise real dependency building and packaging on PRs without publishing anything.
def verify(String directory) {
  def expected = readYaml(file: "${directory}/Chart.yaml")
  withEnv(["PREVIEW_CHART_DIRECTORY=${directory}", "PREVIEW_CHART_OUTPUT=${directory}-package"]) {
    sh '''
      set -eu
      helm dependency build "$PREVIEW_CHART_DIRECTORY"
      helm package "$PREVIEW_CHART_DIRECTORY" --destination "$PREVIEW_CHART_OUTPUT"
    '''
  }
  def archive = "${directory}-package/civil-service-preview-${expected.version}.tgz"
  withEnv(["PREVIEW_CHART_ARCHIVE=${archive}"]) {
    def packaged = readYaml(text: sh(
      script: 'tar -xOzf "$PREVIEW_CHART_ARCHIVE" civil-service-preview/Chart.yaml',
      returnStdout: true
    ))
    def contents = sh(script: 'tar -tzf "$PREVIEW_CHART_ARCHIVE"', returnStdout: true).readLines()
    def dependencies = expected.dependencies ?: []
    def dependencyNames = dependencies.collect { it.name }.unique()
    if (packaged.name != expected.name || packaged.version != expected.version
      || packaged.annotations?.get('hmcts.github.io/skip-publish') || (packaged.dependencies ?: []) != dependencies) {
      error('Full preview chart metadata does not match the source chart')
    }
    dependencyNames.each { name ->
      if (!contents.contains("civil-service-preview/charts/${name}/Chart.yaml".toString())) {
        error("Full preview chart is missing dependency ${name}")
      }
    }
    echo "Verified civil-service-preview ${expected.version}: ${dependencyNames.join(', ')}"
  }
}

return this
