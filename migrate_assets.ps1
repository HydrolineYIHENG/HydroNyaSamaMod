$srcDir = "c:\Users\Administrator\Pictures\HydroNyaSama\.reference\NyaSamaBuilding\src\main\resources\assets\nyasamabuilding"
$destDir = "c:\Users\Administrator\Pictures\HydroNyaSama\common\src\main\resources\assets\hydronyasama"

Write-Host "Copying blockstates..."
Copy-Item -Path "$srcDir\blockstates\*" -Destination "$destDir\blockstates" -Force

Write-Host "Copying models..."
Copy-Item -Path "$srcDir\models\*" -Destination "$destDir\models" -Recurse -Force

Write-Host "Replacing namespaces..."
$files = Get-ChildItem -Path "$destDir" -Recurse -Filter "*.json"
foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Raw
    # Replace texture paths first (blocks/ -> block/)
    $content = $content -replace "nyasamabuilding:blocks/", "hydronyasama:block/"
    # Replace remaining namespaces
    $content = $content -replace "nyasamabuilding:", "hydronyasama:"
    
    Set-Content -Path $file.FullName -Value $content
}
Write-Host "Done."