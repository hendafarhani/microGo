# Set your token as an environment variable for safety:
# export TF_VAR_do_token="your_digitalocean_token"

project_name = "microgo-dev"
region       = "fra1"
k8s_version  = "1.34.5-do.1"

node_size  = "s-2vcpu-2gb"
node_count = 2

registry_name  = "hendafarhani-microgo-registry"
namespace_name = "microgo-dev"

domain_name = "hendafarhani.cloud"
subdomain   = "dev-microgo"

# Keep false on Day 2, then set true on Day 3 when you have a LB/ingress public IP.
create_dns_record = false
dns_record_value  = ""
