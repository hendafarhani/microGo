{{- define "common.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "common.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name (include "common.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{- define "common.componentName" -}}
{{- $root := .root -}}
{{- $component := .component -}}
{{- if $component.fullnameOverride -}}
{{- $component.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else if $root.Values.fullnameOverride -}}
{{- $root.Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" (include "common.fullname" $root) $component.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{- define "common.namespace" -}}
{{- .Values.namespace.name -}}
{{- end -}}

{{- define "common.labels" -}}
{{- $root := .root -}}
{{- $component := .component -}}
app.kubernetes.io/name: {{ include "common.name" $root }}
app.kubernetes.io/instance: {{ $root.Release.Name }}
app.kubernetes.io/managed-by: {{ $root.Release.Service }}
app.kubernetes.io/component: {{ $component.name }}
helm.sh/chart: {{ printf "%s-%s" $root.Chart.Name $root.Chart.Version | replace "+" "_" }}
{{- end -}}

{{- define "common.selectorLabels" -}}
{{- $root := .root -}}
{{- $component := .component -}}
app.kubernetes.io/name: {{ include "common.name" $root }}
app.kubernetes.io/instance: {{ $root.Release.Name }}
app.kubernetes.io/component: {{ $component.name }}
{{- end -}}

{{- define "common.renderEnv" -}}
{{- range . }}
- name: {{ .name }}
  value: {{ .value | quote }}
{{- end -}}
{{- end -}}
