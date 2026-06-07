Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ProjectId = "capital-cruise"
$ProjectNumber = "482841496133"
$Region = "us-east4"
$ServiceName = "capital-cruise-backend"
$Repository = "capital-cruise-backend"
$GitHubRepository = "Capital-Cruise/backend"
$RuntimeServiceAccount = "capital-cruise-backend-runner"
$DeployServiceAccount = "github-cloud-run-deployer"
$WorkloadIdentityPool = "github-actions-pool"
$WorkloadIdentityProvider = "github-actions-provider"

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

function Invoke-GcloudAllowExists {
    param([Parameter(Mandatory = $true)][string[]]$Args)

    try {
        Invoke-Gcloud -Args $Args
    }
    catch {
        if ($_.Exception.Message -notmatch "ALREADY_EXISTS") {
            throw
        }
    }
}

function Test-Gcloud {
    param([Parameter(Mandatory = $true)][string[]]$Args)

    try {
        & gcloud @Args *> $null
        return $LASTEXITCODE -eq 0
    }
    catch {
        return $false
    }
}

function Get-GcloudOutput {
    param([Parameter(Mandatory = $true)][string[]]$Args)

    try {
        $output = & gcloud @Args 2>$null
        if ($LASTEXITCODE -ne 0) {
            return $null
        }
        return (($output | Out-String).Trim())
    }
    catch {
        return $null
    }
}

function ConvertFrom-SecureStringToPlainText {
    param([Parameter(Mandatory = $true)][Security.SecureString]$SecureString)

    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureString)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    }
    finally {
        if ($bstr -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
        }
    }
}

function Read-Value {
    param(
        [Parameter(Mandatory = $true)][string]$Prompt,
        [string]$Default = "",
        [switch]$Secure,
        [int]$MinLength = 0
    )

    $envValue = [Environment]::GetEnvironmentVariable($Prompt.ToUpperInvariant().Replace(" ", "_"))
    if (-not [string]::IsNullOrWhiteSpace($envValue)) {
        return $envValue
    }

    if ($Secure) {
        $secureValue = Read-Host -AsSecureString $Prompt
        $plain = ConvertFrom-SecureStringToPlainText $secureValue
        if ([string]::IsNullOrWhiteSpace($plain)) {
            throw "$Prompt is required"
        }
        if ($MinLength -gt 0 -and $plain.Length -lt $MinLength) {
            throw "$Prompt must be at least $MinLength characters"
        }
        return $plain
    }

    if ([string]::IsNullOrWhiteSpace($Default)) {
        $value = Read-Host $Prompt
        if ([string]::IsNullOrWhiteSpace($value)) {
            throw "$Prompt is required"
        }
        return $value
    }

    $value = Read-Host "$Prompt [$Default]"
    if ([string]::IsNullOrWhiteSpace($value)) {
        return $Default
    }
    return $value
}

function Ensure-Secret {
    param(
        [Parameter(Mandatory = $true)][string]$SecretName,
        [Parameter(Mandatory = $true)][string]$Value
    )

    if (-not (Test-Gcloud @("secrets", "describe", $SecretName, "--project=$ProjectId"))) {
        Write-Host "Creating secret $SecretName"
        Invoke-Gcloud @("secrets", "create", $SecretName, "--project=$ProjectId", "--replication-policy=automatic")
    }

    Write-Host "Adding latest version for $SecretName"
    $Value | & gcloud secrets versions add $SecretName --project=$ProjectId --data-file=- --quiet
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to add secret version for $SecretName"
    }
}

Assert-CommandExists -Name "gcloud"

$activeAccount = Get-GcloudOutput @("auth", "list", "--filter=status:ACTIVE", "--format=value(account)")
if ([string]::IsNullOrWhiteSpace($activeAccount)) {
    throw "No active gcloud account found. Run 'gcloud auth login' first."
}

$currentProject = Get-GcloudOutput @("config", "get-value", "project")
if ($currentProject -ne $ProjectId) {
    Write-Host "Setting active project to $ProjectId"
    Invoke-Gcloud @("config", "set", "project", $ProjectId)
}

Write-Host "Enabling required APIs"
Invoke-Gcloud @("services", "enable",
    "run.googleapis.com",
    "artifactregistry.googleapis.com",
    "iam.googleapis.com",
    "iamcredentials.googleapis.com",
    "secretmanager.googleapis.com",
    "cloudbuild.googleapis.com",
    "--project=$ProjectId")

$repoExists = Test-Gcloud @("artifacts", "repositories", "describe", $Repository, "--location=$Region", "--project=$ProjectId")
if (-not $repoExists) {
    Write-Host "Creating Artifact Registry repository $Repository"
    Invoke-GcloudAllowExists @("artifacts", "repositories", "create", $Repository,
        "--repository-format=docker",
        "--location=$Region",
        "--description=Capital Cruise backend container images",
        "--project=$ProjectId")
}

function Ensure-ServiceAccount {
    param([Parameter(Mandatory = $true)][string]$AccountId)

    if (-not (Test-Gcloud @("iam", "service-accounts", "describe", "$AccountId@$ProjectId.iam.gserviceaccount.com", "--project=$ProjectId"))) {
        Write-Host "Creating service account $AccountId"
        Invoke-GcloudAllowExists @("iam", "service-accounts", "create", $AccountId,
            "--display-name=$AccountId",
            "--project=$ProjectId")
    }
}

Ensure-ServiceAccount -AccountId $RuntimeServiceAccount
Ensure-ServiceAccount -AccountId $DeployServiceAccount

$runtimeEmail = "$RuntimeServiceAccount@$ProjectId.iam.gserviceaccount.com"
$deployEmail = "$DeployServiceAccount@$ProjectId.iam.gserviceaccount.com"

Invoke-Gcloud @("projects", "add-iam-policy-binding", $ProjectId,
    "--member=serviceAccount:$deployEmail",
    "--role=roles/run.admin")
Invoke-Gcloud @("projects", "add-iam-policy-binding", $ProjectId,
    "--member=serviceAccount:$deployEmail",
    "--role=roles/artifactregistry.writer")
Invoke-Gcloud @("projects", "add-iam-policy-binding", $ProjectId,
    "--member=serviceAccount:$deployEmail",
    "--role=roles/viewer")
Invoke-Gcloud @("iam", "service-accounts", "add-iam-policy-binding", $runtimeEmail,
    "--member=serviceAccount:$deployEmail",
    "--role=roles/iam.serviceAccountUser",
    "--project=$ProjectId")
Invoke-Gcloud @("projects", "add-iam-policy-binding", $ProjectId,
    "--member=serviceAccount:$runtimeEmail",
    "--role=roles/secretmanager.secretAccessor")

$poolResource = "projects/$ProjectNumber/locations/global/workloadIdentityPools/$WorkloadIdentityPool"
if (-not (Test-Gcloud @("iam", "workload-identity-pools", "describe", $WorkloadIdentityPool, "--location=global", "--project=$ProjectId"))) {
    Write-Host "Creating workload identity pool $WorkloadIdentityPool"
    Invoke-GcloudAllowExists @("iam", "workload-identity-pools", "create", $WorkloadIdentityPool,
        "--location=global",
        "--project=$ProjectId",
        "--display-name=GitHub Actions Pool")
}

if (-not (Test-Gcloud @("iam", "workload-identity-pools", "providers", "describe", $WorkloadIdentityProvider, "--workload-identity-pool=$WorkloadIdentityPool", "--location=global", "--project=$ProjectId"))) {
    Write-Host "Creating workload identity provider $WorkloadIdentityProvider"
    Invoke-GcloudAllowExists @("iam", "workload-identity-pools", "providers", "create-oidc", $WorkloadIdentityProvider,
        "--workload-identity-pool=$WorkloadIdentityPool",
        "--location=global",
        "--project=$ProjectId",
        "--display-name=GitHub Actions Provider",
        "--issuer-uri=https://token.actions.githubusercontent.com",
        "--attribute-mapping=google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository,attribute.ref=assertion.ref",
        "--attribute-condition=attribute.repository=='$GitHubRepository' && attribute.ref=='refs/heads/main-app'")
}

Invoke-Gcloud @("iam", "service-accounts", "add-iam-policy-binding", $deployEmail,
    "--member=principalSet://iam.googleapis.com/$poolResource/attribute.repository/$GitHubRepository",
    "--role=roles/iam.workloadIdentityUser",
    "--project=$ProjectId")

$databaseUrl = Read-Value -Prompt "DATABASE_URL" -Default "jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require"
$dbUsername = Read-Value -Prompt "SUPABASE_DB_USERNAME" -Default "postgres.cbkoepkuqcxiepeqasvr"
$dbPassword = Read-Value -Prompt "SUPABASE_DB_PASSWORD" -Secure
$jwtSecret = Read-Value -Prompt "JWT_SECRET" -Secure -MinLength 32
$jwtExpirationMillis = Read-Value -Prompt "JWT_EXPIRATION_MILLIS" -Default "3600000"
$adminPassword = Read-Value -Prompt "CAPITAL_CRUISE_ADMIN_PASSWORD" -Secure
$frontendAllowedOrigins = Read-Value -Prompt "FRONTEND_ALLOWED_ORIGINS" -Default "http://localhost:5173,http://localhost:5174,http://localhost:3000"

if ($jwtSecret.Length -lt 32) {
    throw "JWT_SECRET must be at least 32 characters"
}

Ensure-Secret -SecretName "capital-cruise-database-url" -Value $databaseUrl
Ensure-Secret -SecretName "capital-cruise-supabase-db-username" -Value $dbUsername
Ensure-Secret -SecretName "capital-cruise-supabase-db-password" -Value $dbPassword
Ensure-Secret -SecretName "capital-cruise-jwt-secret" -Value $jwtSecret
Ensure-Secret -SecretName "capital-cruise-jwt-expiration-millis" -Value $jwtExpirationMillis
Ensure-Secret -SecretName "capital-cruise-admin-password" -Value $adminPassword
Ensure-Secret -SecretName "capital-cruise-frontend-allowed-origins" -Value $frontendAllowedOrigins

Write-Host ""
Write-Host "WORKLOAD_IDENTITY_PROVIDER: projects/$ProjectNumber/locations/global/workloadIdentityPools/$WorkloadIdentityPool/providers/$WorkloadIdentityProvider"
Write-Host "GCP_SERVICE_ACCOUNT: $deployEmail"
Write-Host "CLOUD_RUN_SERVICE: $ServiceName"
Write-Host "CLOUD_RUN_REGION: $Region"
