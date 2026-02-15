$assetsDir = "c:\Users\Administrator\Pictures\HydroNyaSama\common\src\main\resources\assets\hydronyasama"

# Function to fix paths in JSON files
function Fix-JsonFile {
    param (
        [string]$FilePath
    )
    
    $content = Get-Content -Path $FilePath -Raw
    $originalContent = $content

    # Fix model references in Blockstates
    # "model": "hydronyasama:foo" -> "model": "hydronyasama:block/foo"
    # Negative lookahead to ensure we don't double-prefix if already correct
    $content = $content -replace '"model"\s*:\s*"hydronyasama:(?!block/)([^"]+)"', '"model": "hydronyasama:block/$1"'

    # Fix parent references in Models
    # "parent": "hydronyasama:foo" -> "parent": "hydronyasama:block/foo"
    $content = $content -replace '"parent"\s*:\s*"hydronyasama:(?!block/)([^"]+)"', '"parent": "hydronyasama:block/$1"'
    
    # Fix texture references in Models (just in case some are missing block/)
    # "texture": "hydronyasama:foo" -> "texture": "hydronyasama:block/foo"
    # Note: textures usually use keys like "particle", "all", "top" etc, so we match the value
    $content = $content -replace '":\s*"hydronyasama:(?!block/)([^"]+)"', '": "hydronyasama:block/$1"'

    if ($content -ne $originalContent) {
        Write-Host "Fixed: $FilePath"
        Set-Content -Path $FilePath -Value $content
    }
}

# Process Blockstates
Write-Host "Processing Blockstates..."
Get-ChildItem -Path "$assetsDir\blockstates" -Filter "*.json" | ForEach-Object {
    Fix-JsonFile -FilePath $_.FullName
}

# Process Models (Block)
Write-Host "Processing Block Models..."
Get-ChildItem -Path "$assetsDir\models\block" -Recurse -Filter "*.json" | ForEach-Object {
    Fix-JsonFile -FilePath $_.FullName
}

# Process Models (Item)
Write-Host "Processing Item Models..."
Get-ChildItem -Path "$assetsDir\models\item" -Recurse -Filter "*.json" | ForEach-Object {
    Fix-JsonFile -FilePath $_.FullName
}

Write-Host "Asset path fix completed."