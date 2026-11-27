package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchOverviewScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("System & ML", "Database & API", "Cloud & DevOps", "Strategy & Costs")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Architect & ML Desk", fontWeight = FontWeight.Bold, color = NeonCyan) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightBackground,
                    titleContentColor = NeonCyan
                )
            )
        },
        containerColor = MidnightBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurfaceGray,
                contentColor = NeonCyan,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Scrollable Content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> SystemAndMLTab()
                    1 -> DatabaseAndApiTab()
                    2 -> CloudAndDevOpsTab()
                    3 -> StrategyAndCostsTab()
                }
            }
        }
    }
}

@Composable
fun SystemAndMLTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionHeader(title = "1. Enterprise System Architecture", icon = Icons.Default.Schema)
            CodeBlock(
                """
Camera Input (Front/Side/Back 2K Images)
  ↓ [HTTPS Post / Encrypted JWT Auth]
Nginx Reverse Proxy & Rate Limiter
  ↓
FastAPI Backend Gateway (Python 3.11)
  ├─ Redis (Inference Task Queue & Session Cache)
  ├─ PostgreSQL (User Profile & Historical Scan DB)
  └─ Background Celery Workers (Inference Orchestration)
       ↓ [Distributed Message Broker]
       GPU Inference Cluster (PyTorch / ONNX Runtime)
         ├─ YOLOv11 (Fast Person Detection & Frame Alignment)
         ├─ Segment Anything 2 (SAM 2) (Body Silhouette Masking)
         ├─ MediaPipe Pose (3D Skeleton Joint Landmark Extraction)
         └─ Vision Transformer (ViT) & Multi-Stage Regression
               ↓ [Output Vectors]
             Confidence Score & Body Fat Estimation
               ↓ [JSON Payload]
Nginx WebSockets / Push Notifications
  ↓ [Encrypted JSON Response]
Mobile App Dashboard (Jetpack Compose / React Native client)
                """.trimIndent()
            )
        }

        item {
            SectionHeader(title = "2. Advanced Machine Learning Pipeline", icon = Icons.Default.Psychology)
            InfoCard(
                title = "Model Quantization & Mobile Optimization",
                description = "For mobile client inference, models are optimized using TensorRT for backend execution and ONNX Runtime/TFLite with INT8 Post-Training Quantization (PTQ) to reduce weights size from 120MB to under 28MB, ensuring latency under 320ms on mobile NPU chips."
            )
        }

        item {
            CodeBlock(
                """
# PyTorch INT8 Quantization Workflow
import torch

model = MyBodyFatRegressionModel()
model.eval()

# Set up quantization configuration
model.qconfig = torch.quantization.get_default_qconfig('fbgemm')
prepared_model = torch.quantization.prepare(model, inplace=True)

# Calibrate with representative dataset
calibrate(prepared_model, calibration_loader)

# Convert to quantized INT8 model
quantized_model = torch.quantization.convert(prepared_model, inplace=True)
torch.onnx.export(quantized_model, dummy_input, "body_fat_int8.onnx", 
                  opset_version=17, input_names=['input'], output_names=['body_fat'])
                """.trimIndent()
            )
        }

        item {
            InfoCard(
                title = "Dataset & Preprocessing Strategy",
                description = "Our model utilizes a combined anthropometric and 3D mesh database. Features undergo random rotation, scale jittering, brightness adjustments, and 3D silhouette projections during training to guarantee high robustness across different backgrounds and lighting conditions."
            )
        }
    }
}

@Composable
fun DatabaseAndApiTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionHeader(title = "1. Database Schema & ER Diagrams", icon = Icons.Default.Storage)
            Text(
                "Our production backend uses PostgreSQL for relational profile and history tracking, while utilizing local Android Room Database for secure offline-first client storage.",
                color = TextGray,
                fontSize = 14.sp
            )
        }

        item {
            CodeBlock(
                """
-- PostgreSQL Production Schema
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    height_cm NUMERIC(5,2) NOT NULL,
    weight_kg NUMERIC(5,2) NOT NULL,
    age INT NOT NULL,
    sex VARCHAR(10) NOT NULL,
    activity_level VARCHAR(50) NOT NULL,
    waist_cm NUMERIC(5,2),
    neck_cm NUMERIC(5,2),
    hip_cm NUMERIC(5,2),
    target_body_fat NUMERIC(4,2),
    is_premium BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE scan_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    scanned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    body_fat_percentage NUMERIC(4,2) NOT NULL,
    confidence_score NUMERIC(3,2) NOT NULL,
    lean_body_mass_kg NUMERIC(5,2) NOT NULL,
    fat_mass_kg NUMERIC(5,2) NOT NULL,
    bmi NUMERIC(4,2) NOT NULL,
    waist_to_height_ratio NUMERIC(3,2) NOT NULL,
    muscle_mass_kg NUMERIC(5,2) NOT NULL,
    body_shape VARCHAR(30) NOT NULL,
    fitness_category VARCHAR(30) NOT NULL,
    front_img_s3_url TEXT,
    side_img_s3_url TEXT,
    back_img_s3_url TEXT
);
                """.trimIndent()
            )
        }

        item {
            SectionHeader(title = "2. Production REST API Endpoints", icon = Icons.Default.Api)
        }

        item {
            CodeBlock(
                """
FastAPI Server Endpoints:

[POST] /api/v1/auth/register
  Request: { email, password, name }
  Response: { token_type, access_token }

[POST] /api/v1/auth/login
  Request: { username, password }
  Response: { token_type, access_token }

[GET]  /api/v1/profile
  Headers: Authorization: Bearer <JWT>
  Response: UserProfile JSON

[POST] /api/v1/profile/update
  Request: { height_cm, weight_kg, age, sex, waist_cm, neck_cm, hip_cm }
  Response: Updated UserProfile

[POST] /api/v1/scans/analyze
  Headers: Authorization: Bearer <JWT>
  Multipart Request:
    - front_image: binary
    - side_image: binary (optional)
    - back_image: binary (optional)
  Response: BodyFatEstimation Result JSON

[GET]  /api/v1/scans/history
  Headers: Authorization: Bearer <JWT>
  Response: List[ScanRecord]

[POST] /api/v1/coach/recommendations
  Request: { user_query }
  Response: { response_text }
                """.trimIndent()
            )
        }
    }
}

@Composable
fun CloudAndDevOpsTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionHeader(title = "1. AWS Infrastructure Deployment", icon = Icons.Default.CloudQueue)
            InfoCard(
                title = "Scalable Enterprise Infrastructure",
                description = "Our cloud architecture deploys a highly available microservice stack on AWS Elastic Kubernetes Service (EKS). GPU auto-scaling groups on Amazon EC2 (g5.xlarge instances with NVIDIA A10G) launch dynamically based on queue depth in Redis."
            )
        }

        item {
            SectionHeader(title = "2. Docker Container Configuration", icon = Icons.Default.Code)
        }

        item {
            CodeBlock(
                """
# Backend Production Dockerfile
FROM nvidia/cuda:11.8.0-runtime-ubuntu22.04

WORKDIR /app

RUN apt-get update && apt-get install -y \
    python3-pip python3-dev git libgl1-mesa-glx libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt

# Download model checkpoints (YOLO, SAM, MediaPipe, ViT)
RUN python3 -c "import urllib.request; \
    urllib.request.urlretrieve('https://models.aibodyfat.com/checkpoint.pth', '/app/models/checkpoint.pth')"

COPY . .

EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "4"]
                """.trimIndent()
            )
        }

        item {
            SectionHeader(title = "3. Kubernetes Deployment Manifest", icon = Icons.Default.Grid3x3)
        }

        item {
            CodeBlock(
                """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: aibodyfat-backend
  namespace: production
spec:
  replicas: 3
  selector:
    matchLabels:
      app: aibodyfat-backend
  template:
    metadata:
      labels:
        app: aibodyfat-backend
    spec:
      containers:
      - name: backend
        image: 123456789.dkr.ecr.us-east-1.amazonaws.com/aibodyfat-backend:v1.2.0
        resources:
          limits:
            nvidia.com/gpu: 1
            memory: 8Gi
            cpu: "4"
          requests:
            memory: 4Gi
            cpu: "2"
        ports:
        - containerPort: 8000
        envFrom:
        - secretRef:
            name: aibodyfat-secrets
---
apiVersion: v1
kind: Service
metadata:
  name: aibodyfat-service
  namespace: production
spec:
  selector:
    app: aibodyfat-backend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8000
  type: ClusterIP
                """.trimIndent()
            )
        }

        item {
            SectionHeader(title = "4. GitHub Actions CI/CD Pipeline", icon = Icons.Default.Autorenew)
        }

        item {
            CodeBlock(
                """
name: Production Deployment

on:
  push:
    branches: [ main ]

jobs:
  test-and-build:
    runs-on: ubuntu-latest
    steps:
    - name: Checkout Code
      uses: actions/checkout@v3

    - name: Set up Python
      uses: actions/setup-python@v4
      with:
        python-type: '3.11'

    - name: Run Unit & Model Tests
      run: |
        pip install -r requirements-dev.txt
        pytest tests/

    - name: Configure AWS Credentials
      uses: aws-actions/configure-aws-credentials@v2
      with:
        aws-access-key-id: ${{"$"}}{{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{"$"}}{{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: us-east-1

    - name: Build and Push Docker Image
      run: |
        aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 123456789.dkr.ecr.us-east-1.amazonaws.com
        docker build -t aibodyfat-backend:latest .
        docker tag aibodyfat-backend:latest 123456789.dkr.ecr.us-east-1.amazonaws.com/aibodyfat-backend:v1.2.0
        docker push 123456789.dkr.ecr.us-east-1.amazonaws.com/aibodyfat-backend:v1.2.0

    - name: Rollout Kubernetes Update
      run: |
        aws eks update-kubeconfig --name aibodyfat-cluster --region us-east-1
        kubectl set image deployment/aibodyfat-backend backend=123456789.dkr.ecr.us-east-1.amazonaws.com/aibodyfat-backend:v1.2.0 -n production
                """.trimIndent()
            )
        }
    }
}

@Composable
fun StrategyAndCostsTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionHeader(title = "1. Multi-Stage Development Roadmap", icon = Icons.Default.Map)
            InfoCard(
                title = "Roadmap - 12 Month Execution Plan",
                description = "• Q1 (Months 1-3): Focus on anthropometric model verification, UX/UI prototyping, local Room DB setup and core landmark algorithms.\n• Q2 (Months 4-6): Deploy FastAPI GPU backend on AWS, fine-tune YOLOv11 & SAM 2 segmentations, start private beta testing.\n• Q3 (Months 7-9): Enhance model quantization, launch iOS & Android app stores with basic subscriptions, implement real-time AI Coach.\n• Q4 (Months 10-12): Scale backend GPU clusters, integrate wearable device APIs (Apple Health, Fitbit, Google Fit), optimize retention pipelines."
            )
        }

        item {
            SectionHeader(title = "2. Monthly Cloud Cost Estimations", icon = Icons.Default.AttachMoney)
            InfoCard(
                title = "Post-launch Budget Blueprint (100k MAU)",
                description = "• AWS Elastic Kubernetes Service (3x EC2 general nodes): ${'$'}220/mo\n• GPU EC2 Instances (2x g5.xlarge autoscaling cluster for peak): ${'$'}1,450/mo\n• RDS PostgreSQL Multi-AZ (Medium-tier database instance): ${'$'}180/mo\n• AWS S3 & CloudFront CDN (Asset storage and global delivery): ${'$'}95/mo\n• Redis Managed Instance (ElastiCache queue and sessions): ${'$'}75/mo\n• Firebase Services (Auth, FCM Notification routing): ${'$'}0/mo (Free tier)\n• Total Estimated Base Monthly Runway: ${'$'}2,020"
            )
        }

        item {
            SectionHeader(title = "3. Performance Optimization Strategy", icon = Icons.Default.Speed)
            InfoCard(
                title = "Latency, Caching & Data Efficiency",
                description = "To ensure a seamless native experience, full-resolution client photos are compressed locally to under 800KB before upload. FastAPI implements asynchronous non-blocking handling using Gunicorn with Uvicorn worker threads. Inference results are immediately cached in Redis, decreasing consecutive analysis delays by 92%."
            )
        }

        item {
            SectionHeader(title = "4. Future Scalability Plan", icon = Icons.Default.TrendingUp)
            InfoCard(
                title = "B2B Gym Portals & Medical Integrations",
                description = "We plan to scale our computer vision platform by packaging the analysis engine as an enterprise SDK for commercial fitness centers (gym kiosks, personal trainer tablets). Future model development includes deep validation against dual-energy X-ray absorptiometry (DEXA) scans to seek regulatory health-tech certifications."
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = title, tint = NeonCyan, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
        )
    }
}

@Composable
fun InfoCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                description,
                fontSize = 13.sp,
                color = TextGray,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, OutlineGray, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFF38BDF8),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
