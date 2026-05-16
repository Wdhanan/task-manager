# terraform/main.tf
# Hauptkonfiguration

# Provider = "Mit welchem Cloud-Anbieter arbeiten wir?"
terraform {
  required_version = ">= 1.7.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Backend = "Wo speichern wir den Terraform-Status?"
  # Status = Terraform merkt sich was es erstellt hat
  backend "s3" {
    bucket = "taskmanager-terraform-state-123456"
    # ↑ S3 = AWS Datei-Speicher (wie eine Festplatte in der Cloud)
    key    = "terraform.tfstate"
    region = "eu-central-1"
  }
}

provider "aws" {
  region = var.aws_region
  # ↑ var. = "Nimm den Wert aus variables.tf"
}

# ══════════════════════════════════════════
# VPC = Virtuelles privates Netzwerk
# Wie ein abgeschlossenes Bürogebäude in der Cloud
# ══════════════════════════════════════════
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
  # ↑ IP-Adressbereich für unser Netzwerk
  #   10.0.0.0 bis 10.0.255.255 = 65536 mögliche Adressen

  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "${var.cluster_name}-vpc"
    Environment = var.environment
  }
}

# Subnets = Unterabteilungen des Netzwerks
# Wie Stockwerke im Bürogebäude
# Public Subnets = Mit Internet-Zugang (für Load Balancer)
resource "aws_subnet" "public" {
  count             = 2
  # ↑ Erstelle 2 davon (in verschiedenen Verfügbarkeitszonen)
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]

  map_public_ip_on_launch = true
  # ↑ Jedes Ding hier bekommt automatisch eine öffentliche IP

  tags = {
    Name = "${var.cluster_name}-public-${count.index}"
    "kubernetes.io/role/elb" = "1"
    # ↑ Kubernetes-Tag: "Hier darf ein Load Balancer erstellt werden"
  }
}

# Private Subnets = KEIN direkter Internet-Zugang
# Sicherer für Datenbanken und Backend
resource "aws_subnet" "private" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 10}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = "${var.cluster_name}-private-${count.index}"
    "kubernetes.io/role/internal-elb" = "1"
  }
}

# Internet Gateway = Die Haustür zum Internet
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "${var.cluster_name}-igw" }
}

# Routing Tabelle = Wegweiser im Netzwerk
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"     # Alle Internet-Adressen
    gateway_id = aws_internet_gateway.main.id  # → Durch die Haustür
  }
}

# Routing Tabelle mit Subnets verbinden
resource "aws_route_table_association" "public" {
  count          = 2
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# Verfügbare Zonen abfragen (automatisch)
data "aws_availability_zones" "available" {
  state = "available"
}