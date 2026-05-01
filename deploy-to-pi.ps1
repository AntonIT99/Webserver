$ConfigPath = Join-Path $PSScriptRoot "deploy-to-pi.local.ps1"

if (-not (Test-Path $ConfigPath)) {
    Write-Error "Missing deploy-to-pi.local.ps1. Create it from deploy-to-pi.local.example.ps1."
    exit 1
}

. $ConfigPath

function Escape-EnvValue([string] $Value) {
    return $Value.Replace("\", "\\").Replace("`"", "\`"")
}

Write-Host "Building Spring Boot JAR..."
.\gradlew.bat clean bootJar

if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed."
    exit 1
}

$Jar = Get-ChildItem ".\build\libs\*.jar" |
        Where-Object { $_.Name -notlike "*plain*.jar" } |
        Select-Object -First 1

if (-not $Jar) {
    Write-Error "No boot JAR found."
    exit 1
}

Write-Host "Uploading $($Jar.Name) to Raspberry Pi..."
scp -i $SshKey $Jar.FullName "${PiUser}@${PiHost}:${TargetPath}"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Upload failed."
    exit 1
}

$RemoteDirectory = $TargetPath -replace "/[^/]+$", ""
$RemoteEnvPath = "$RemoteDirectory/webserver.env"
$LocalEnvPath = Join-Path $env:TEMP "webserver.env"

if (-not $BootstrapAdminUsername -or -not $BootstrapAdminPassword) {
    Write-Error "BootstrapAdminUsername and BootstrapAdminPassword must be set in deploy-to-pi.local.ps1."
    exit 1
}

@(
    "BOOTSTRAP_ADMIN_USERNAME=""$(Escape-EnvValue $BootstrapAdminUsername)"""
    "BOOTSTRAP_ADMIN_PASSWORD=""$(Escape-EnvValue $BootstrapAdminPassword)"""
) | Set-Content -Path $LocalEnvPath -Encoding ascii

Write-Host "Uploading environment file to Raspberry Pi..."
scp -i $SshKey $LocalEnvPath "${PiUser}@${PiHost}:${RemoteEnvPath}"

if ($LASTEXITCODE -ne 0) {
    Remove-Item $LocalEnvPath -ErrorAction SilentlyContinue
    Write-Error "Environment upload failed."
    exit 1
}

ssh -i $SshKey "${PiUser}@${PiHost}" "chmod 600 '$RemoteEnvPath'"

if ($LASTEXITCODE -ne 0) {
    Remove-Item $LocalEnvPath -ErrorAction SilentlyContinue
    Write-Error "Could not set permissions on remote environment file."
    exit 1
}

Remove-Item $LocalEnvPath -ErrorAction SilentlyContinue

Write-Host "Done."
