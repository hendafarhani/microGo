terraform {
  required_providers {
    digitalocean = {
      source = "digitalocean/digitalocean"
    }
  }
}

resource "digitalocean_container_registry" "this" {
  name                   = var.name
  subscription_tier_slug = var.subscription_tier
}
