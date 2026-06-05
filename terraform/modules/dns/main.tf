terraform {
  required_providers {
    digitalocean = {
      source = "digitalocean/digitalocean"
    }
  }
}

resource "digitalocean_record" "app" {
  count = var.create_record ? 1 : 0

  domain = var.domain_name
  type   = "A"
  name   = var.subdomain
  value  = var.record_value
  ttl    = var.ttl
}
