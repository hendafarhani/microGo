{{- define "common.deployment" -}}
{{- $root := .root -}}
{{- $component := .component -}}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "common.componentName" . }}
  namespace: {{ include "common.namespace" $root }}
  labels:
    {{- include "common.labels" . | nindent 4 }}
spec:
  replicas: {{ default 1 $component.replicaCount }}
  selector:
    matchLabels:
      {{- include "common.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "common.selectorLabels" . | nindent 8 }}
    spec:
      {{- with $root.Values.global.imagePullSecrets }}
      imagePullSecrets:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- if or $component.waitFor $component.initContainers }}
      initContainers:
        {{- range $component.waitFor }}
        {{- include "common.waitFor" (dict "wait" . "root" $root) | nindent 8 }}
        {{- end }}
        {{- with $component.initContainers }}
        {{- toYaml . | nindent 8 }}
        {{- end }}
      {{- end }}
      containers:
        - name: {{ $component.name }}
          {{- if $component.image.digest }}
          image: "{{ $component.image.repository }}@{{ $component.image.digest }}"
          {{- else }}
          image: "{{ $component.image.repository }}:{{ $component.image.tag }}"
          {{- end }}
          imagePullPolicy: {{ default "IfNotPresent" $root.Values.global.imagePullPolicy }}
          ports:
            - containerPort: {{ $component.containerPort }}
          {{- if or (and $component.configMap $component.configMap.enabled) (and $component.secret $component.secret.enabled) $component.extraEnvFrom }}
          envFrom:
            {{- if and $component.configMap $component.configMap.enabled }}
            - configMapRef:
                name: {{ include "common.componentName" . }}-config
            {{- end }}
            {{- if and $component.secret $component.secret.enabled }}
            - secretRef:
                name: {{ include "common.componentName" . }}-secret
            {{- end }}
            {{- with $component.extraEnvFrom }}
            {{- toYaml . | nindent 12 }}
            {{- end }}
          {{- end }}
          {{- with $component.env }}
          env:
            {{- include "common.renderEnv" . | nindent 12 }}
          {{- end }}
          {{- with $component.readinessProbe }}
          readinessProbe:
            {{- toYaml . | nindent 12 }}
          {{- end }}
          {{- with $component.livenessProbe }}
          livenessProbe:
            {{- toYaml . | nindent 12 }}
          {{- end }}
          {{- with $component.resources }}
          resources:
            {{- toYaml . | nindent 12 }}
          {{- end }}
{{- end -}}
