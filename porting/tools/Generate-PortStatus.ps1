param(
    [string]$UpstreamRoot = (Join-Path $PSScriptRoot "..\..\upstream\create-mc1.20.1-6.0.8"),
    [string]$OutputPath = (Join-Path $PSScriptRoot "..\status\CREATE_6.0.8.md")
)

$ErrorActionPreference = "Stop"
$registryRoot = Join-Path $UpstreamRoot "src\main\java\com\simibubi\create"

$specs = @(
    @{ Kind = "block"; File = "AllBlocks.java"; DefaultPriority = "P1" },
    @{ Kind = "item"; File = "AllItems.java"; DefaultPriority = "P1" },
    @{ Kind = "block_entity"; File = "AllBlockEntityTypes.java"; DefaultPriority = "P1" },
    @{ Kind = "fluid"; File = "AllFluids.java"; DefaultPriority = "P2" },
    @{ Kind = "menu"; File = "AllMenuTypes.java"; DefaultPriority = "P1" },
    @{ Kind = "entity"; File = "AllEntityTypes.java"; DefaultPriority = "P3" },
    @{ Kind = "enchantment"; File = "AllEnchantments.java"; DefaultPriority = "P1" },
    @{ Kind = "particle"; File = "AllParticleTypes.java"; DefaultPriority = "P1" },
    @{ Kind = "sound"; File = "AllSoundEvents.java"; DefaultPriority = "P1" },
    @{ Kind = "structure_processor"; File = "AllStructureProcessorTypes.java"; DefaultPriority = "P3" },
    @{ Kind = "mounted_storage"; File = "AllMountedStorageTypes.java"; DefaultPriority = "P3" },
    @{ Kind = "contraption_type"; File = "AllContraptionTypes.java"; DefaultPriority = "P3"; Exclude = @("BY_LEGACY_NAME") },
    @{ Kind = "display_source"; File = "AllDisplaySources.java"; DefaultPriority = "P2" },
    @{ Kind = "display_target"; File = "AllDisplayTargets.java"; DefaultPriority = "P2" },
    @{ Kind = "bogey_style"; File = "AllBogeyStyles.java"; DefaultPriority = "P5"; Exclude = @("BOGEY_STYLES", "CYCLE_GROUPS", "STANDARD_CYCLE_GROUP") },
    @{ Kind = "damage_type"; File = "AllDamageTypes.java"; DefaultPriority = "P1" },
    @{ Kind = "entity_data_serializer"; File = "AllEntityDataSerializers.java"; DefaultPriority = "P5"; Exclude = @("CARRIAGE_DATA") }
)

$overridePath = Join-Path $PSScriptRoot "..\status-overrides.csv"
$overrides = @{}
Import-Csv $overridePath | ForEach-Object {
    $overrides["$($_.kind):$($_.symbol)"] = $_
}

function Get-Priority([string]$kind, [string]$symbol, [string]$defaultPriority) {
    if ($symbol -match "TRAIN|TRACK|BOGEY|STATION|SIGNAL|SCHEDULE|RAILWAY") { return "P5" }
    if ($symbol -match "DECOR|ORNATE|PILLAR|BRICK|TILE|SHINGLE|WINDOW|LADDER|DOOR|TRAPDOOR|FENCE|WALL") { return "P6" }
    if ($symbol -match "CONTRAPTION|CHASSIS|BEARING|MECHANICAL_PISTON|ROPE_PULLEY|SUPER_GLUE") { return "P3" }
    if ($kind -eq "fluid") { return "P2" }
    return $defaultPriority
}

function Add-Row([System.Collections.Generic.List[object]]$rows, [hashtable]$spec, [string]$symbol, [int]$line) {
    $key = "$($spec.Kind):$symbol"
    $priority = Get-Priority $spec.Kind $symbol $spec.DefaultPriority
    $status = if ($priority -in @("P5", "P6")) { "planned-late" } else { "planned-core" }
    $automated = "pending"
    $manual = "pending"
    $notes = ""

    if ($overrides.ContainsKey($key)) {
        $override = $overrides[$key]
        $priority = $override.priority
        $status = $override.status
        $automated = $override.automated
        $manual = $override.manual
        $notes = $override.notes
    }

    $rows.Add([pscustomobject]@{
        Kind = $spec.Kind
        Symbol = $symbol
        Status = $status
        Priority = $priority
        Automated = $automated
        Manual = $manual
        Source = "$($spec.File):$line"
        Notes = $notes
    })
}

$rows = [System.Collections.Generic.List[object]]::new()
foreach ($spec in $specs) {
    $path = Join-Path $registryRoot $spec.File
    if (-not (Test-Path $path)) { throw "Missing upstream registry: $path" }
    $content = Get-Content -Raw -Encoding UTF8 $path

    foreach ($statement in [regex]::Matches($content, "(?ms)public\s+static\s+final\s+.*?;")) {
        foreach ($assignment in [regex]::Matches($statement.Value, "\b(?<symbol>[A-Z][A-Z0-9_]*)\s*=")) {
            if ($spec.Exclude -and $assignment.Groups["symbol"].Value -in $spec.Exclude) { continue }
            $prefix = $content.Substring(0, $statement.Index + $assignment.Index)
            $line = ([regex]::Matches($prefix, "\n")).Count + 1
            Add-Row $rows $spec $assignment.Groups["symbol"].Value $line
        }
    }
}

$enumSpecs = @(
    @{ Kind = "recipe"; File = "AllRecipeTypes.java"; Type = "AllRecipeTypes"; DefaultPriority = "P1" },
    @{ Kind = "particle"; File = "AllParticleTypes.java"; Type = "AllParticleTypes"; DefaultPriority = "P1" }
)
foreach ($spec in $enumSpecs) {
    $path = Join-Path $registryRoot $spec.File
    $content = Get-Content -Raw -Encoding UTF8 $path
    $enumBody = [regex]::Match($content, "(?ms)public\s+enum\s+$($spec.Type).*?\{(?<body>.*?);")
    foreach ($entry in [regex]::Matches($enumBody.Groups["body"].Value, "(?m)^\s*(?<symbol>[A-Z][A-Z0-9_]*)\s*\(")) {
        $absoluteIndex = $enumBody.Groups["body"].Index + $entry.Index
        $line = ([regex]::Matches($content.Substring(0, $absoluteIndex), "\n")).Count + 1
        Add-Row $rows $spec $entry.Groups["symbol"].Value $line
    }
}

$output = [System.Collections.Generic.List[string]]::new()
$output.Add("# Create 6.0.8 port status")
$output.Add("")
$output.Add("Generated from the vendored upstream registry declarations. Do not edit this file directly; edit ``status-overrides.csv`` and rerun the generator.")
$output.Add("")
$output.Add("Total registry entries: $($rows.Count)")

foreach ($group in ($rows | Sort-Object Kind, Symbol | Group-Object Kind)) {
    $output.Add("")
    $output.Add("## $($group.Name)")
    $output.Add("")
    $output.Add("| Symbol | Status | Priority | Automated | Manual | Upstream source | Notes |")
    $output.Add("| --- | --- | --- | --- | --- | --- | --- |")
    foreach ($row in $group.Group) {
        $output.Add("| ``$($row.Symbol)`` | $($row.Status) | $($row.Priority) | $($row.Automated) | $($row.Manual) | ``$($row.Source)`` | $($row.Notes) |")
    }
}

$outputDirectory = Split-Path -Parent $OutputPath
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
Set-Content -LiteralPath $OutputPath -Value $output -Encoding UTF8
Write-Output "Generated $($rows.Count) registry entries at $OutputPath"
