{{- define "common.service" -}}
{{- $root := .root -}}
{{- $component := .component -}}
apiVersion: v1
kind: Service
metadata:
  name: {{ include "common.componentName" . }}
  namespace: {{ include "common.namespace" $root }}
  labels:
    {{- include "common.labels" . | nindent 4 }}
spec:
  type: {{ default "ClusterIP" $component.service.type }}
  selector:
    {{- include "common.selectorLabels" . | nindent 4 }}
  ports:
    - name: http
      port: {{ $component.service.port }}
      targetPort: {{ default $component.containerPort $component.service.targetPort }}
{{- end -}}
