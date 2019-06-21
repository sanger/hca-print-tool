variable "user_name" {}
variable "password" {}
variable "tenant_name" {
  default = "cgap-prod"
}
variable "keypair" {
  default = "cgapprodkeypair"
}
variable "instance_image_id" {
  default = "c5c6a324-8aa7-409f-a658-8ee3c897620f"
}

provider "openstack" {
  user_name   = "${var.user_name}"
  tenant_name = "${var.tenant_name}"
  password    = "${var.password}"
  auth_url    = "https://eta.internal.sanger.ac.uk:13000"
  domain_name = "Default"
}

resource "openstack_networking_network_v2" "cgap_prod_hcaprint_network" {
  name           = "cgap_prod_hcaprint_network"
  admin_state_up = "true"
}

resource "openstack_networking_router_v2" "cgap_prod_hcaprint_router" {
  name = "cgap_prod_hcaprint_router"
  admin_state_up = "true"
  external_network_id = "eb31cc74-96ba-4394-aef4-0e94bec46d85" # public
}

resource "openstack_networking_subnet_v2" "cgap_prod_hcaprint_subnet" {
  name            = "cgap_prod_hcaprint_subnet"
  network_id      = "${openstack_networking_network_v2.cgap_prod_hcaprint_network.id}"
  cidr            = "192.168.0.0/24"
  ip_version      = 4
  dns_nameservers = ["172.18.255.1", "172.18.255.2", "172.18.255.3"]
}

resource "openstack_networking_router_interface_v2" "cgap_prod_hcaprint_router_interface" {
  router_id = "${openstack_networking_router_v2.cgap_prod_hcaprint_router.id}"
  subnet_id = "${openstack_networking_subnet_v2.cgap_prod_hcaprint_subnet.id}"
}


resource "openstack_networking_secgroup_v2" "cgap_prod_hcaprint_security_group" {
  name = "cgap_prod_hcaprint_security_group"
}

resource "openstack_networking_secgroup_rule_v2" "cgap_prod_hcaprint_security_group_rule_ssh" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 22
  port_range_max    = 22
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = "${openstack_networking_secgroup_v2.cgap_prod_hcaprint_security_group.id}"
}

resource "openstack_networking_secgroup_rule_v2" "cgap_prod_hcaprint_security_group_rule_tomcat" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 8080
  port_range_max    = 8080
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = "${openstack_networking_secgroup_v2.cgap_prod_hcaprint_security_group.id}"
}

resource "openstack_compute_instance_v2" "cgap_prod_hcaprint_instance" {
  name            = "cgap_prod_hcaprint_instance"
  image_id        = "${var.instance_image_id}"
  flavor_id       = "2000"
  key_pair        = "${var.keypair}"
  security_groups = ["default", "${openstack_networking_secgroup_v2.cgap_prod_hcaprint_security_group.name}"]

  network {
    name = "${openstack_networking_network_v2.cgap_prod_hcaprint_network.name}"
  }
}

resource "openstack_compute_floatingip_associate_v2" "cgap_prod_hcaprint_floating_ip_assoc" {
  floating_ip = "172.27.81.76"
  instance_id = "${openstack_compute_instance_v2.cgap_prod_hcaprint_instance.id}"
}
