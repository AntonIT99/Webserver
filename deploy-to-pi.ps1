$ConfigPath = Join-Path $PSScriptRoot "deploy-to-pi.local.ps1"

if (-not (Test-Path $ConfigPath)) {
    Write-Error "Missing deploy-to-pi.local.ps1. Create it from deploy-to-pi.local.example.ps1."
    exit 1
}

. $ConfigPath

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
scp $Jar.FullName "${PiUser}@${PiHost}:${TargetPath}"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Upload failed."
    exit 1
}

Write-Host "Done."