output "id" {
  value = digitalocean_kubernetes_cluster.this.id
}

output "name" {
  value = digitalocean_kubernetes_cluster.this.name
}

output "endpoint" {
  value = digitalocean_kubernetes_cluster.this.endpoint
}

output "kube_config_raw" {
  value     = digitalocean_kubernetes_cluster.this.kube_config[0].raw_config
  sensitive = true
}
