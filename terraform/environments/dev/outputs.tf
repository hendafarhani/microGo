output "cluster_name" {
  value = module.cluster.name
}

output "cluster_id" {
  value = module.cluster.id
}

output "registry_endpoint" {
  value = module.registry.endpoint
}

output "namespace_name" {
  value = module.namespace.name
}

output "app_fqdn" {
  value = module.dns.fqdn
}
