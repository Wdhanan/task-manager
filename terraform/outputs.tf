# terraform/outputs.tf
# Was soll nach dem Erstellen angezeigt werden?

output "cluster_name" {
  value       = aws_eks_cluster.main.name
  description = "Name des EKS Clusters"
}

output "cluster_endpoint" {
  value       = aws_eks_cluster.main.endpoint
  description = "URL des Kubernetes API Servers"
}

output "kubectl_command" {
  value       = "aws eks update-kubeconfig --name ${aws_eks_cluster.main.name} --region ${var.aws_region}"
  description = "Diesen Befehl ausführen um kubectl zu konfigurieren"
}