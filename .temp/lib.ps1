Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function New-SeedContext {
    param(
        [string]$BaseUrl,
        [int]$DelayMs = 450,
        [int]$MaxRetries = 3,
        [switch]$DryRun
    )

    $logDir = Join-Path $PSScriptRoot "logs"
    if (-not (Test-Path -LiteralPath $logDir)) {
        New-Item -ItemType Directory -Path $logDir | Out-Null
    }

    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $logFile = Join-Path $logDir ("seed-{0}.log" -f $stamp)

    [pscustomobject]@{
        BaseUrl    = $BaseUrl.TrimEnd("/")
        DelayMs    = $DelayMs
        MaxRetries = $MaxRetries
        DryRun     = [bool]$DryRun
        LogFile    = $logFile
    }
}

function Write-SeedLog {
    param(
        [Parameter(Mandatory = $true)]$Context,
        [Parameter(Mandatory = $true)][string]$Message,
        [ValidateSet("INFO", "WARN", "ERROR", "DEBUG")][string]$Level = "INFO"
    )

    $line = "[{0}] [{1}] {2}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $Level, $Message
    Add-Content -LiteralPath $Context.LogFile -Value $line
    Write-Host $line
}

function ConvertTo-JsonBody {
    param($Value)
    if ($null -eq $Value) {
        return "{}"
    }
    return ($Value | ConvertTo-Json -Depth 20 -Compress)
}

function Get-JsonPathValue {
    param(
        [Parameter(Mandatory = $true)]$Object,
        [Parameter(Mandatory = $true)][string]$Path
    )

    $segments = $Path.Split(".")
    $current = $Object
    foreach ($seg in $segments) {
        if ($null -eq $current) { return $null }
        $prop = $current.PSObject.Properties[$seg]
        if ($null -eq $prop) { return $null }
        $current = $prop.Value
    }
    return $current
}

function Get-FirstValue {
    param(
        [Parameter(Mandatory = $true)]$Object,
        [Parameter(Mandatory = $true)][string[]]$Paths
    )
    foreach ($path in $Paths) {
        $value = Get-JsonPathValue -Object $Object -Path $path
        if ($null -ne $value -and "$value" -ne "") {
            return $value
        }
    }
    return $null
}

function Get-ApiData {
    param([Parameter(Mandatory = $true)]$JsonObject)
    if ($null -eq $JsonObject) { return $null }
    $dataProp = $JsonObject.PSObject.Properties["data"]
    if ($null -ne $dataProp) {
        return $dataProp.Value
    }
    return $JsonObject
}

function Invoke-ApiRequest {
    param(
        [Parameter(Mandatory = $true)]$Context,
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Path,
        $Body,
        [string]$Token,
        [switch]$AllowFailure
    )

    $url = "{0}{1}" -f $Context.BaseUrl, $Path
    $jsonBody = ConvertTo-JsonBody -Value $Body

    function Test-RequiresDtoWrapper {
        param($JsonError)
        if ($null -eq $JsonError) { return $false }
        $errors = $JsonError.PSObject.Properties["errors"]
        if ($null -eq $errors) { return $false }
        $dtoError = $errors.Value.PSObject.Properties["dto"]
        if ($null -eq $dtoError) { return $false }
        foreach ($item in @($dtoError.Value)) {
            if ("$item" -match "required") {
                return $true
            }
        }
        return $false
    }

    if ($Context.DryRun) {
        Write-SeedLog -Context $Context -Level "DEBUG" -Message ("[DRY-RUN] {0} {1} body={2}" -f $Method, $url, $jsonBody)
        return [pscustomobject]@{
            StatusCode = 200
            BodyText   = '{"type":"success","data":{"id":"00000000-0000-0000-0000-000000000000","accessToken":"dry-run-token"}}'
            Json       = [pscustomobject]@{
                type = "success"
                data = [pscustomobject]@{
                    id          = [guid]::NewGuid().Guid
                    accessToken = "dry-run-token"
                    token       = "dry-run-token"
                }
            }
        }
    }

    $attempt = 0
    $dtoWrappedTried = $false
    while ($attempt -lt $Context.MaxRetries) {
        $attempt++
        $headers = @("-H", "Content-Type: application/json")
        if ($Token) {
            $headers += @("-H", ("Authorization: Bearer {0}" -f $Token))
        }

        $tmpBodyFile = [System.IO.Path]::GetTempFileName()
        try {
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($tmpBodyFile, $jsonBody, $utf8NoBom)
            $responseRaw = & curl.exe -sS -X $Method $url @headers --data-binary ("@{0}" -f $tmpBodyFile) -w "`n__HTTP_STATUS__:%{http_code}"
        }
        finally {
            if (Test-Path -LiteralPath $tmpBodyFile) {
                Remove-Item -LiteralPath $tmpBodyFile -Force -ErrorAction SilentlyContinue
            }
        }
        $responseText = [string]$responseRaw
        $statusMatch = [regex]::Match($responseText, "__HTTP_STATUS__:(\d{3})\s*$")
        if (-not $statusMatch.Success) {
            throw "Cannot parse HTTP status for $Method $url"
        }

        $statusCode = [int]$statusMatch.Groups[1].Value
        $bodyText = $responseText.Substring(0, $statusMatch.Index).Trim()
        $json = $null
        if ($bodyText) {
            try { $json = $bodyText | ConvertFrom-Json } catch { $json = $null }
        }

        if ($statusCode -ge 200 -and $statusCode -lt 300) {
            if ($Context.DelayMs -gt 0) {
                Start-Sleep -Milliseconds $Context.DelayMs
            }
            return [pscustomobject]@{
                StatusCode = $statusCode
                BodyText   = $bodyText
                Json       = $json
            }
        }

        if (
            -not $dtoWrappedTried -and
            $statusCode -eq 400 -and
            $null -ne $Body -and
            (Test-RequiresDtoWrapper -JsonError $json)
        ) {
            $dtoWrappedTried = $true
            $jsonBody = ConvertTo-JsonBody -Value @{ dto = $Body }
            Write-SeedLog -Context $Context -Level "WARN" -Message ("{0} {1}: backend expects dto wrapper, retrying with wrapped body." -f $Method, $Path)
            continue
        }

        $retryable = ($statusCode -eq 429 -or $statusCode -ge 500)
        $msg = ("HTTP {0} for {1} {2}. Attempt {3}/{4}. Body: {5}" -f $statusCode, $Method, $Path, $attempt, $Context.MaxRetries, $bodyText)
        Write-SeedLog -Context $Context -Level "WARN" -Message $msg
        if (-not $retryable -or $attempt -ge $Context.MaxRetries) {
            if ($AllowFailure) {
                return [pscustomobject]@{
                    StatusCode = $statusCode
                    BodyText   = $bodyText
                    Json       = $json
                }
            }
            throw $msg
        }

        $sleepMs = [Math]::Min(4000, $Context.DelayMs * [Math]::Pow(2, $attempt))
        Start-Sleep -Milliseconds ([int]$sleepMs)
    }
}

function Get-IdFromResponse {
    param([Parameter(Mandatory = $true)]$Response)

    $json = $Response.Json
    if ($null -eq $json) { return $null }
    $candidate = Get-FirstValue -Object $json -Paths @("data.id", "id", "data.value", "value")
    if ($null -eq $candidate) { return $null }
    return [string]$candidate
}

function Get-TokenFromResponse {
    param([Parameter(Mandatory = $true)]$Response)
    $json = $Response.Json
    if ($null -eq $json) { return $null }
    $candidate = Get-FirstValue -Object $json -Paths @(
        "data.accessToken",
        "data.token",
        "accessToken",
        "token",
        "data.jwt",
        "jwt"
    )
    if ($null -eq $candidate) { return $null }
    return [string]$candidate
}
