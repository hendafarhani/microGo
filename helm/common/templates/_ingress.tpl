{{- define "common.ingress" -}}
{{- $root := .root -}}
{{- $component := .component -}}
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: {{ include "common.componentName" . }}-ingress
  namespace: {{ include "common.namespace" $root }}
  labels:
    {{- include "common.labels" . | nindent 4 }}
  {{- with $component.ingress.annotations }}
  annotations:
    {{- toYaml . | nindent 4 }}
  {{- end }}
spec:
  {{- with $component.ingress.className }}
  ingressClassName: {{ . }}
  {{- end }}
  rules:
    {{- range $component.ingress.hosts }}
    - host: {{ .host | quote }}
      http:
        paths:
          {{- range .paths }}
          - path: {{ .path }}
            pathType: {{ .pathType }}
            backend:
              service:
                name: {{ include "common.componentName" (dict "root" $root "component" $component) }}
                port:
                  number: {{ $component.service.port }}
          {{- end }}
    {{- end }}
  {{- with $component.ingress.tls }}
  tls:
    {{- toYaml . | nindent 4 }}
  {{- end }}
{{- end -}}
