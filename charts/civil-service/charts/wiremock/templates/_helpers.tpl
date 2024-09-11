{{- define "wiremock.name" -}}
{{- .Chart.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "wiremock.fullname" -}}
{{- printf "%s-%s" (include "wiremock.name" .) .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
