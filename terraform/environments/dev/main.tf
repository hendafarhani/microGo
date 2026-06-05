module "cluster" {
  source = "../../modules/cluster"

  name        = var.project_name
  region      = var.region
  k8s_version = var.k8s_version
  node_size   = var.node_size
  node_count  = var.node_count
  tags        = ["microgo", "dev"]
}

locals {
  kubeconfig = yamldecode(module.cluster.kube_config_raw)
  k8s_host   = local.kubeconfig.clusters[0].cluster.server
  k8s_token  = local.kubeconfig.users[0].user.token
  k8s_ca     = base64decode(local.kubeconfig.clusters[0].cluster["certificate-authority-data"])
}

module "registry" {
  source = "../../modules/registry"

  name = var.registry_name
}

module "namespace" {
  source = "../../modules/namespace"
  providers = {
    kubernetes = kubernetes
  }

  name = var.namespace_name
}

module "dns" {
  source = "../../modules/dns"

  domain_name   = var.domain_name
  subdomain     = var.subdomain
  create_record = var.create_dns_record
  record_value  = var.dns_record_value
}
