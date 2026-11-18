// Generate the downstream chart from the same source/version as the slim Flux chart.
def prepare(String destination) {
  dir(destination) {
    deleteDir()
  }
  withEnv(["PREVIEW_CHART_DIRECTORY=${destination}"]) {
    sh '''
      set -eu
      mkdir -p "$PREVIEW_CHART_DIRECTORY"
      cp -R charts/civil-service/. "$PREVIEW_CHART_DIRECTORY/"
      rm -f "$PREVIEW_CHART_DIRECTORY/Chart.lock"
    '''
  }
  def chart = readYaml(file: "${destination}/Chart.yaml")
  chart.name = 'civil-service-preview'
  chart.annotations?.remove('hmcts.github.io/skip-publish')
  writeYaml(file: "${destination}/Chart.yaml", data: chart, overwrite: true)
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
    def dependencyNames = expected.dependencies.collect { it.alias ?: it.name }
    def packagedNames = (packaged.dependencies ?: []).collect { it.alias ?: it.name }
    if (packaged.name != expected.name || packaged.version != expected.version
      || packaged.annotations?.get('hmcts.github.io/skip-publish') || packagedNames != dependencyNames) {
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
