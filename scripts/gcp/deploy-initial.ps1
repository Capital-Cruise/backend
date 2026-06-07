Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ProjectId = "capital-cruise"
$Region = "us-east4"
$ServiceName = "capital-cruise-backend"
$Repository = "capital-cruise-backend"

$Image = "us-east4-docker.pkg.dev/$ProjectId/$Repository/${ServiceName}:manual"

function Assert-CommandExists {
    param([Parameter(Mandatory = $true)][string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command not found: $Name"
    }
}

function Invoke-Gcloud {
    param([Parameter(Mandatory = $true)][string[]]$Args)

    & gcloud @Args
    if ($LASTEXITCODE -ne 0) {
        throw "gcloud command failed: gcloud $($Args -join ' ')"
    }
}

function Test-DockerReady {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        return $false
    }

    try {
        & docker info *> $null
        return $LASTEXITCODE -eq 0
    }
    catch {
        return $false
    }
}

Assert-CommandExists -Name "gcloud"

$currentProject = (& gcloud config get-value project 2>$null | Out-String).Trim()
if ($currentProject -ne $ProjectId) {
    Invoke-Gcloud @("config", "set", "project", $ProjectId)
}

if (Test-DockerReady) {
    Write-Host "Building Docker image locally"
    Invoke-Gcloud @("auth", "configure-docker", "us-east4-docker.pkg.dev", "--quiet")
    & docker build -t $Image .
    if ($LASTEXITCODE -ne 0) {
        throw "docker build failed"
    }
    & docker push $Image
    if ($LASTEXITCODE -ne 0) {
        throw "docker push failed"
    }
}
else {
    Write-Host "Docker is not available. Using Cloud Build."
    Invoke-Gcloud @("auth", "configure-docker", "us-east4-docker.pkg.dev", "--quiet")
    Invoke-Gcloud @("builds", "submit", "--tag", $Image, ".")
}

Invoke-Gcloud @("run", "deploy", $ServiceName,
    "--project=$ProjectId",
    "--region=$Region",
    "--image=$Image",
    "--platform=managed",
    "--allow-unauthenticated",
    "--service-account=capital-cruise-backend-runner@$ProjectId.iam.gserviceaccount.com",
    "--memory=1Gi",
    "--cpu=1",
    "--min-instances=0",
    "--max-instances=2",
    "--port=8080",
    "--set-env-vars=SPRING_PROFILES_ACTIVE=prod",
    "--set-secrets=DATABASE_URL=capital-cruise-database-url:latest,SUPABASE_DB_USERNAME=capital-cruise-supabase-db-username:latest,SUPABASE_DB_PASSWORD=capital-cruise-supabase-db-password:latest,JWT_SECRET=capital-cruise-jwt-secret:latest,JWT_EXPIRATION_MILLIS=capital-cruise-jwt-expiration-millis:latest,CAPITAL_CRUISE_ADMIN_PASSWORD=capital-cruise-admin-password:latest,FRONTEND_ALLOWED_ORIGINS=capital-cruise-frontend-allowed-origins:latest")

$serviceUrl = (& gcloud run services describe $ServiceName --project=$ProjectId --region=$Region --format="value(status.url)" 2>$null | Out-String).Trim()
Write-Host "Service URL: $serviceUrl"

try {
    $healthUrl = "$serviceUrl/api/v1/health"
    Write-Host "Health check: $healthUrl"
    $response = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 30
    $response | ConvertTo-Json -Compress
}
catch {
    Write-Host "Health check failed. Reading recent Cloud Run logs."
    Invoke-Gcloud @("run", "services", "logs", "read", $ServiceName,
        "--region=$Region",
        "--project=$ProjectId",
        "--limit=50")
    throw
}
