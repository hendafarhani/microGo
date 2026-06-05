{{- define "common.secret" -}}
{{- $root := .root -}}
{{- $component := .component -}}
apiVersion: v1
kind: Secret
metadata:
  name: {{ include "common.componentName" . }}-secret
  namespace: {{ include "common.namespace" $root }}
  labels:
    {{- include "common.labels" . | nindent 4 }}
type: Opaque
stringData:
  {{- range $key, $value := $component.secret.stringData }}
  {{ $key }}: {{ $value | quote }}
  {{- end }}
{{- end -}}
