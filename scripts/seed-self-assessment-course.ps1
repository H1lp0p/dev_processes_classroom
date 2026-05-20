# Seed: course + task with self-assessment rubric (user@example.com).
# Russian strings: scripts/seed-self-assessment-course.data.json (UTF-8).
param(
    [string]$BaseUrl = "http://176.209.147.7:5000",
    [string]$Email = "user@example.com",
    [string]$Password = "string1",
    [string]$DataPath = (Join-Path $PSScriptRoot "seed-self-assessment-course.data.json"),
    [int]$DeadlineDaysFromNow = 14,
    [switch]$DryRun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$libPath = Join-Path $PSScriptRoot "..\.temp\lib.ps1"
if (-not (Test-Path -LiteralPath $libPath)) {
    throw "Shared API helpers not found: $libPath"
}
if (-not (Test-Path -LiteralPath $DataPath)) {
    throw "Data file not found: $DataPath"
}

. $libPath

$data = Get-Content -LiteralPath $DataPath -Raw -Encoding UTF8 | ConvertFrom-Json
$courseTitle = [string]$data.courseTitle
$taskTitle = [string]$data.taskTitle
$taskText = [string]$data.taskText
$registerDisplayName = [string]$data.registerDisplayName

$ctx = New-SeedContext -BaseUrl $BaseUrl -DelayMs 300 -MaxRetries 3 -DryRun:$DryRun
Write-SeedLog -Context $ctx -Message ("Self-assessment course seed. User={0}, BaseUrl={1}" -f $Email, $ctx.BaseUrl)

function Get-AuthToken {
    $loginBody = @{ email = $Email; password = $Password }
    $loginResp = Invoke-ApiRequest -Context $ctx -Method "POST" -Path "/api/auth/login" -Body $loginBody -AllowFailure
    if ($loginResp.StatusCode -ge 200 -and $loginResp.StatusCode -lt 300) {
        $token = Get-TokenFromResponse -Response $loginResp
        if ($token) { return $token }
    }

    Write-SeedLog -Context $ctx -Level "WARN" -Message "Login failed; trying register then login again."
    $registerBody = @{
        email       = $Email
        password    = $Password
        credentials = $registerDisplayName
    }
    [void](Invoke-ApiRequest -Context $ctx -Method "POST" -Path "/api/auth/register" -Body $registerBody -AllowFailure)

    $loginResp2 = Invoke-ApiRequest -Context $ctx -Method "POST" -Path "/api/auth/login" -Body $loginBody
    $token2 = Get-TokenFromResponse -Response $loginResp2
    if (-not $token2) {
        throw "Cannot obtain token for $Email. Last body: $($loginResp2.BodyText)"
    }
    return $token2
}

$token = Get-AuthToken

Write-SeedLog -Context $ctx -Message ("Create course: {0}" -f $courseTitle)
$createCourseResp = Invoke-ApiRequest -Context $ctx -Method "POST" -Path "/api/course" -Token $token -Body @{ title = $courseTitle }
$courseId = Get-IdFromResponse -Response $createCourseResp
if (-not $courseId) {
    throw "Course id missing in response: $($createCourseResp.BodyText)"
}

$deadline = (Get-Date).ToUniversalTime().AddDays($DeadlineDaysFromNow).ToString("o")

$criteria = @()
foreach ($c in @($data.criteria)) {
    $item = [ordered]@{
        type       = [string]$c.type
        title      = [string]$c.title
        orderIndex = [int]$c.orderIndex
    }
    if ($null -ne $c.maxScore) { $item["maxScore"] = [double]$c.maxScore }
    if ($null -ne $c.weight) { $item["weight"] = [double]$c.weight }
    if ($null -ne $c.score) { $item["score"] = [double]$c.score }
    if ($null -ne $c.direction) { $item["direction"] = [string]$c.direction }
    $criteria += $item
}

$taskBody = @{
    type                  = "task"
    title                 = $taskTitle
    text                  = $taskText
    deadline              = $deadline
    maxScore              = 5
    taskType              = "mandatory"
    solvableAfterDeadline = $false
    studentScoreWeight    = 0.25
    penaltyPerDay         = 0.1
    maxDays               = 7
    failThreshold         = 0.2
    successThreshold      = 0.9
    criteria              = $criteria
}

Write-SeedLog -Context $ctx -Message ("Create task with self-assessment rubric in course {0}" -f $courseId)
$createTaskResp = Invoke-ApiRequest -Context $ctx -Method "POST" -Path ("/api/course/{0}/task" -f $courseId) -Token $token -Body $taskBody
$postId = Get-IdFromResponse -Response $createTaskResp
if (-not $postId) {
    throw "Task/post id missing in response: $($createTaskResp.BodyText)"
}

$courseInfoResp = Invoke-ApiRequest -Context $ctx -Method "GET" -Path ("/api/course/{0}" -f $courseId) -Token $token -Body $null
$courseInfo = Get-ApiData -JsonObject $courseInfoResp.Json
$inviteCode = Get-FirstValue -Object $courseInfo -Paths @("inviteCode")

Write-SeedLog -Context $ctx -Message "Done."
Write-Host ""
Write-Host "=== Self-assessment test course ==="
Write-Host ("Course id:    {0}" -f $courseId)
Write-Host ("Course title: {0}" -f $courseTitle)
Write-Host ("Invite code:  {0}" -f $inviteCode)
Write-Host ("Task/post id: {0}" -f $postId)
Write-Host ("Task title:   {0}" -f $taskTitle)
Write-Host ("Deadline:     {0}" -f $deadline)
Write-Host ("Teacher:      {0}" -f $Email)
Write-Host ("Log file:     {0}" -f $ctx.LogFile)
