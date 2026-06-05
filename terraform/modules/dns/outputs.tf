output "fqdn" {
  value = "${var.subdomain}.${var.domain_name}"
}

output "record_id" {
  value = var.create_record ? digitalocean_record.app[0].id : null
}
