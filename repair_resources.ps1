$ErrorActionPreference = "Stop"

$root = "c:\Users\Administrator\Pictures\HydroNyaSama"
$assetsDir = Join-Path $root "common\src\main\resources\assets\hydronyasama"
$refLangPath = Join-Path $root ".reference\NyaSamaBuilding\src\main\resources\assets\nyasamabuilding\lang\zh_cn.lang"
$outLangPath = Join-Path $assetsDir "lang\zh_cn.json"

function Write-Utf8NoBom {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Content
    )
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}

function Normalize-JsonFiles {
    param([string]$Dir)

    Get-ChildItem -Path $Dir -Recurse -Filter "*.json" | ForEach-Object {
        $path = $_.FullName
        $text = [System.IO.File]::ReadAllText($path)

        $text = $text.Replace("hydronyasama:blocks/", "hydronyasama:block/")
        $text = $text.Replace("hydronyasama:items/", "hydronyasama:item/")

        $text = $text -replace '"model"\s*:\s*"hydronyasama:([^"/]+)"', '"model": "hydronyasama:block/$1"'
        $text = $text -replace '"parent"\s*:\s*"hydronyasama:([^"/]+)"', '"parent": "hydronyasama:block/$1"'

        Write-Utf8NoBom -Path $path -Content $text
    }
}

function Convert-SnakeToCamel {
    param([string]$Snake)
    return ($Snake -split "_" | ForEach-Object { if ($_.Length -gt 0) { $_.Substring(0,1).ToUpper() + $_.Substring(1) } }) -join ""
}

function Resolve-TileName {
    param(
        [hashtable]$TileMap,
        [string]$BaseId
    )

    $candidates = New-Object System.Collections.Generic.List[string]

    $camel = Convert-SnakeToCamel $BaseId
    $candidates.Add($camel)

    $camel2 = $camel.Replace("Tvbg", "TVBg")
    $candidates.Add($camel2)

    if ($camel -match "^Nsb") { $candidates.Add(($camel -replace "^Nsb", "NSB")) }
    if ($camel -match "^Nsc") { $candidates.Add(($camel -replace "^Nsc", "NSC")) }
    if ($camel -match "^Nse") { $candidates.Add(($camel -replace "^Nse", "NSE")) }
    if ($camel -match "^Nso") { $candidates.Add(($camel -replace "^Nso", "NSO")) }
    if ($camel -match "^Nsr") { $candidates.Add(($camel -replace "^Nsr", "NSR")) }
    if ($camel -match "^Nst") { $candidates.Add(($camel -replace "^Nst", "NST")) }
    if ($camel -match "^Nsdn") { $candidates.Add(($camel -replace "^Nsdn", "NSDN")) }

    foreach ($k in $candidates) {
        if ($TileMap.ContainsKey($k)) { return $TileMap[$k] }
    }
    return $null
}

function Generate-ZhCnJson {
    param(
        [string]$RefLang,
        [string]$AssetsPath,
        [string]$OutputPath
    )

    $tileMap = @{}
    $suffixMap = @{}
    $probeName = $null

    foreach ($line in [System.IO.File]::ReadAllLines($RefLang)) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }

        if ($line.StartsWith("trans.") -and $line.Contains(".name=")) {
            $key = $line.Substring(6, $line.IndexOf(".name=") - 6)
            $value = $line.Substring($line.IndexOf("=") + 1)
            switch ($key) {
                "Carpet" { $suffixMap["carpet"] = $value }
                "Edge" { $suffixMap["edge"] = $value }
                "Railing" { $suffixMap["railing"] = $value }
                "Roof" { $suffixMap["roof"] = $value }
                "Fence" { $suffixMap["fence"] = $value }
                "FenceGate" { $suffixMap["fence_gate"] = $value }
                "Pane" { $suffixMap["pane"] = $value }
                "Slab" { $suffixMap["slab"] = $value }
                "Stairs" { $suffixMap["stairs"] = $value }
                "Strip" { $suffixMap["strip"] = $value }
                "VSlab" { $suffixMap["vslab"] = $value }
                "VStrip" { $suffixMap["vstrip"] = $value }
                "Wall" { $suffixMap["wall"] = $value }
            }
        } elseif ($line.StartsWith("tile.") -and $line.Contains(".name=")) {
            $key = $line.Substring(5, $line.IndexOf(".name=") - 5)
            $value = $line.Substring($line.IndexOf("=") + 1)
            $tileMap[$key] = $value
        } elseif ($line.StartsWith("item.Probe.name=")) {
            $probeName = $line.Substring($line.IndexOf("=") + 1)
        }
    }

    foreach ($k in @("carpet","edge","railing","roof","fence","fence_gate","pane","slab","stairs","strip","vslab","vstrip","wall")) {
        if (-not $suffixMap.ContainsKey($k) -or [string]::IsNullOrEmpty($suffixMap[$k])) {
            $suffixMap[$k] = $k
        }
    }

    $out = [ordered]@{}
    $out["itemGroup.hydronyasama.core"] = ([char]0x55B5) + ([char]0x7389) + ([char]0x6838) + ([char]0x5FC3)
    $out["itemGroup.hydronyasama.building"] = ([char]0x55B5) + ([char]0x7389) + ([char]0x571F) + ([char]0x6728)
    if ($probeName -ne $null) {
        $out["item.hydronyasama.probe"] = $probeName
    } else {
        $out["item.hydronyasama.probe"] = "Probe"
    }

    $blockstatesDir = Join-Path $AssetsPath "blockstates"
    $suffixes = @("fence_gate","carpet","edge","railing","roof","fence","pane","slab","stairs","strip","vslab","vstrip","wall","block")

    Get-ChildItem -Path $blockstatesDir -Filter "*.json" | ForEach-Object {
        $id = [System.IO.Path]::GetFileNameWithoutExtension($_.Name)
        if ($id -eq $null -or $id.Length -eq 0) { return }

        $baseId = $id
        $variantSuffix = $null

        foreach ($suf in $suffixes) {
            $suffixToken = "_" + $suf
            if ($id.EndsWith($suffixToken)) {
                $baseId = $id.Substring(0, $id.Length - $suffixToken.Length)
                $variantSuffix = $suf
                break
            }
        }

        $name = Resolve-TileName -TileMap $tileMap -BaseId $baseId
        if ($name -eq $null) {
            $name = $baseId
        }

        if ($variantSuffix -ne $null -and $variantSuffix -ne "block") {
            $suffixName = $suffixMap[$variantSuffix]
            if ($suffixName -ne $null) {
                $name = $name + $suffixName
            }
        }

        $out["block.hydronyasama.$id"] = $name
    }

    $json = ($out | ConvertTo-Json -Depth 4)
    Write-Utf8NoBom -Path $OutputPath -Content $json
}

Normalize-JsonFiles -Dir $assetsDir
Generate-ZhCnJson -RefLang $refLangPath -AssetsPath $assetsDir -OutputPath $outLangPath

Write-Host "Resource repair completed."
