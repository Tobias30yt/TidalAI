param()

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$projectDir = Split-Path -Parent $scriptDir

$sourcePath = Join-Path -Path $projectDir -ChildPath "src\main\resources\iconig.png"
$outputPath = Join-Path -Path $scriptDir -ChildPath "TidalAI.ico"

Write-Host ""
Write-Host "Creating Windows icon..."
Write-Host "Source: $sourcePath"
Write-Host "Output: $outputPath"

if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) {
throw "Source icon not found: $sourcePath"
}

$png = [System.IO.File]::ReadAllBytes($sourcePath)

if ($png.Length -lt 24) {
throw "PNG file is too small to be a valid PNG."
}

# PNG signature

$pngSignature = [byte[]]@(137, 80, 78, 71, 13, 10, 26, 10)

for ($i = 0; $i -lt 8; $i++) {
if ($png[$i] -ne $pngSignature[$i]) {
throw "iconig.png is not a valid PNG file."
}
}

# PNG dimensions are stored as big-endian UInt32 at byte offsets 16 and 20.

function Read-BigEndianUInt32 {
param(
[byte[]]$Data,
[int]$Offset
)
$b0 = [uint32]$Data[$Offset]
$b1 = [uint32]$Data[$Offset + 1]
$b2 = [uint32]$Data[$Offset + 2]
$b3 = [uint32]$Data[$Offset + 3]

return (($b0 -shl 24) -bor
        ($b1 -shl 16) -bor
        ($b2 -shl 8) -bor
        $b3)


}

$width = Read-BigEndianUInt32 -Data $png -Offset 16
$height = Read-BigEndianUInt32 -Data $png -Offset 20

if (($width -le 0) -or ($height -le 0)) {
throw "PNG has invalid dimensions: ${width}x${height}"
}

Write-Host "PNG size: ${width}x${height}"

# ICO directory entries can represent dimensions up to 256.

# 0 means 256 in ICO format.

if ($width -eq 256) {
$iconWidth = 0
}
elseif ($width -lt 256) {
$iconWidth = [byte]$width
}
else {
$iconWidth = 0
}

if ($height -eq 256) {
$iconHeight = 0
}
elseif ($height -lt 256) {
$iconHeight = [byte]$height
}
else {
$iconHeight = 0
}

$imageOffset = 22
$imageSize = $png.Length

$stream = $null
$writer = $null

try {
$stream = New-Object System.IO.FileStream(
$outputPath,
[System.IO.FileMode]::Create,
[System.IO.FileAccess]::Write,
[System.IO.FileShare]::None
)

$writer = New-Object System.IO.BinaryWriter($stream)

# ICONDIR
$writer.Write([uint16]0) # Reserved
$writer.Write([uint16]1) # Type = icon
$writer.Write([uint16]1) # Number of images

# ICONDIRENTRY
$writer.Write([byte]$iconWidth)     # Width
$writer.Write([byte]$iconHeight)    # Height
$writer.Write([byte]0)              # Color count
$writer.Write([byte]0)              # Reserved
$writer.Write([uint16]1)            # Color planes
$writer.Write([uint16]32)           # Bits per pixel
$writer.Write([uint32]$imageSize)   # PNG size
$writer.Write([uint32]$imageOffset) # PNG offset

# Embedded PNG
$writer.Write($png)

$writer.Flush()

}
finally {
if ($writer -ne $null) {
$writer.Dispose()
}
elseif ($stream -ne $null) {
$stream.Dispose()
}
}

if (-not (Test-Path -LiteralPath $outputPath -PathType Leaf)) {
throw "ICO creation failed: output file was not created."
}

$outputInfo = Get-Item -LiteralPath $outputPath

Write-Host ""
Write-Host "Windows icon created successfully."
Write-Host "ICO: $($outputInfo.FullName)"
Write-Host "Size: $($outputInfo.Length) bytes"
Write-Host ""