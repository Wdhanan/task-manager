# terraform/variables.tf
# Variablen = Einstellungen die du anpassen kannst

variable "aws_region" {
  description = "AWS Region wo alles deployed wird"
  type        = string
  default     = "eu-central-1"  # Frankfurt
}

variable "cluster_name" {
  description = "Name des Kubernetes Clusters"
  type        = string
  default     = "taskmanager-cluster"
}

variable "environment" {
  description = "Umgebung (dev/staging/prod)"
  type        = string
  default     = "production"
}