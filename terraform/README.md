# Terraform Infrastructure for microGo

This folder defines the cloud infrastructure for the `microGo` platform using Terraform.

The goal is to provision and manage the deployment foundation (Kubernetes, registry, DNS) in a repeatable and versioned way.

## What this Terraform project manages

- `modules/cluster`: creates a DigitalOcean Kubernetes cluster (DOKS).
- `modules/registry`: creates a DigitalOcean Container Registry for microservice images.
- `modules/namespace`: creates a real Kubernetes namespace using the Kubernetes provider.
- `modules/dns`: manages DNS records for exposing services through a domain/subdomain.
- `environments/dev`: composes all modules for the `dev` environment.

## Project structure

- `modules/`: reusable building blocks.
- `environments/`: environment-specific assembly (variables, module wiring, state scope).

## Typical workflow

1. Define or update infrastructure in module/environment code.
2. Validate and preview changes (`terraform validate`, `terraform plan`).
3. Apply changes (`terraform apply`).
4. Deploy application workloads (Helm/manifests) into the created cluster/namespace.

## Quick start (dev)

From `terraform/environments/dev`:

```bash
export TF_VAR_do_token="your_digitalocean_token"
terraform init
terraform fmt -recursive
terraform validate
terraform plan
```

To apply:

```bash
terraform apply
```

## Notes

- `create_dns_record` is `false` by default because you may not have a public LB/Ingress IP yet.
- Set `create_dns_record=true` and provide `dns_record_value` after your public endpoint exists.
- Keep sensitive values out of Git (use environment variables, secret stores, or CI secrets).
- Use separate state/workspaces/backends per environment (`dev`, `staging`, `prod`).
