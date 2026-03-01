$sourceDir = "c:\Users\MSI-THIN\Desktop\WinGo\workshop3A9\src\main"
$targetDir = "c:\Users\MSI-THIN\Desktop\WinGo\WinGo\src\main"

Write-Host "Migrating files from $sourceDir to $targetDir"

Get-ChildItem -Path $sourceDir -Recurse -File | ForEach-Object {
    $relativePath = $_.FullName.Substring($sourceDir.Length + 1)
    $destination = Join-Path $targetDir $relativePath
    
    $destDir = Split-Path $destination -Parent
    if (-not (Test-Path $destDir)) {
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    }
    
    if (Test-Path $destination) {
        Write-Host "Overwriting existing file: $relativePath"
    } else {
        Write-Host "Copying new file: $relativePath"
    }
    
    Copy-Item -Path $_.FullName -Destination $destination -Force
}

Write-Host "Migration complete!"
