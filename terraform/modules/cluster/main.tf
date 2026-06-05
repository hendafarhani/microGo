terraform {
  required_providers {
    digitalocean = {
      source = "digitalocean/digitalocean"
    }
  }
}

resource "digitalocean_kubernetes_cluster" "this" {
  name    = var.name
  region  = var.region
  version = var.k8s_version

  node_pool {
    name       = "${var.name}-pool"
    size       = var.node_size
    node_count = var.node_count
    tags       = var.tags
  }

  tags = var.tags
}
