# terraform/eks.tf
# EKS = Elastic Kubernetes Service
# = Kubernetes-Cluster den AWS für uns verwaltet

# IAM Role für EKS
# IAM = Identity and Access Management
# Role = "Was darf der EKS-Cluster in AWS machen?"
resource "aws_iam_role" "eks_cluster" {
  name = "${var.cluster_name}-role"

  # Vertrauensrichtlinie: "EKS darf diese Rolle annehmen"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "eks.amazonaws.com" }
    }]
  })
}

# AWS-verwaltete Berechtigungen an die Rolle hängen
resource "aws_iam_role_policy_attachment" "eks_cluster_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.eks_cluster.name
}

# ══════════════════════════════════════════
# DER KUBERNETES CLUSTER (EKS)
# ══════════════════════════════════════════
resource "aws_eks_cluster" "main" {
  name     = var.cluster_name
  role_arn = aws_iam_role.eks_cluster.arn
  version  = "1.29"   # Kubernetes Version

  vpc_config {
    subnet_ids = concat(
      aws_subnet.public[*].id,
      aws_subnet.private[*].id
    )
    endpoint_public_access = true
    # ↑ Wir können kubectl von unserem PC aus nutzen
  }

  depends_on = [aws_iam_role_policy_attachment.eks_cluster_policy]
  # ↑ "Erstelle den Cluster ERST wenn die IAM-Rolle fertig ist"
}

# IAM Role für Worker Nodes (die eigentlichen Server)
resource "aws_iam_role" "eks_nodes" {
  name = "${var.cluster_name}-nodes-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

# Berechtigungen für Worker Nodes
resource "aws_iam_role_policy_attachment" "eks_worker_node" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.eks_nodes.name
}

resource "aws_iam_role_policy_attachment" "eks_cni" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.eks_nodes.name
}

resource "aws_iam_role_policy_attachment" "eks_container_registry" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.eks_nodes.name
}

# ══════════════════════════════════════════
# NODE GROUP = Die Server auf denen Container laufen
# ══════════════════════════════════════════
resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "taskmanager-nodes"
  node_role_arn   = aws_iam_role.eks_nodes.arn
  subnet_ids      = aws_subnet.private[*].id

  # Server-Typ (t3.medium = 2 CPUs, 4GB RAM, ~0.04$/Stunde)
  instance_types = ["t3.medium"]

  # Skalierung
  scaling_config {
    desired_size = 2   # Normalerweise: 2 Server
    min_size     = 1   # Mindestens: 1 Server
    max_size     = 4   # Höchstens: 4 Server (bei viel Traffic)
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_worker_node,
    aws_iam_role_policy_attachment.eks_cni,
    aws_iam_role_policy_attachment.eks_container_registry,
  ]
}