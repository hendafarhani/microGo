{{- define "common.configmap" -}}
{{- $root := .root -}}
{{- $component := .component -}}
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ include "common.componentName" . }}-config
  namespace: {{ include "common.namespace" $root }}
  labels:
    {{- include "common.labels" . | nindent 4 }}
data:
  {{- range $key, $value := $component.configMap.data }}
  {{ $key }}: {{ $value | quote }}
  {{- end }}
{{- end -}}
