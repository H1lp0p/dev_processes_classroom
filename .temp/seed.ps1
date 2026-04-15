param(
    [string]$BaseUrl = "http://37.21.130.4:5000",
    [string]$DataPath = (Join-Path $PSScriptRoot "seed.data.json"),
    [string]$StatePath = (Join-Path $PSScriptRoot "seed.state.json"),
    [int]$DelayMs = 500,
    [int]$MaxRetries = 3,
    [ValidateSet("auth", "courses", "posts", "comments", "replies", "solutions", "teamWorkflows")]
    [string]$StartFrom = "auth",
    [switch]$DryRun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "lib.ps1")

if (-not (Test-Path -LiteralPath $DataPath)) {
    throw "Data file not found: $DataPath"
}

$data = Get-Content -LiteralPath $DataPath -Raw | ConvertFrom-Json
$ctx = New-SeedContext -BaseUrl $BaseUrl -DelayMs $DelayMs -MaxRetries $MaxRetries -DryRun:$DryRun

Write-SeedLog -Context $ctx -Message ("Seeder started. DryRun={0}. BaseUrl={1}. StartFrom={2}" -f $ctx.DryRun, $ctx.BaseUrl, $StartFrom)

$state = [ordered]@{
    UsersByKey    = @{}
    UsersByEmail  = @{}
    CoursesByKey  = @{}
    PostsByKey    = @{}
    CommentIds    = New-Object System.Collections.Generic.List[string]
    SolutionByPost = @{}
}

function Get-UserByKey {
    param([string]$Key)
    if (-not $state.UsersByKey.ContainsKey($Key)) {
        throw "Unknown user key: $Key"
    }
    return $state.UsersByKey[$Key]
}

function Save-State {
    $snapshot = [ordered]@{
        users = @()
        coursesByKey = @{}
        postsByKey = @{}
        commentIds = @($state.CommentIds)
        solutionByPost = $state.SolutionByPost
    }
    foreach ($u in $state.UsersByKey.Values) {
        $snapshot.users += @{
            key = $u.key
            name = $u.name
            email = $u.email
            id = $u.id
        }
    }
    foreach ($key in $state.CoursesByKey.Keys) {
        $c = $state.CoursesByKey[$key]
        $snapshot.coursesByKey[$key] = @{
            key = $c.key
            id = $c.id
            title = $c.title
            inviteCode = $c.inviteCode
            ownerKey = $c.ownerKey
        }
    }
    foreach ($key in $state.PostsByKey.Keys) {
        $p = $state.PostsByKey[$key]
        $snapshot.postsByKey[$key] = @{
            key = $p.key
            id = $p.id
            type = $p.type
            courseId = $p.courseId
        }
    }
    $json = $snapshot | ConvertTo-Json -Depth 20
    Set-Content -LiteralPath $StatePath -Value $json -Encoding UTF8
}

function Load-State {
    if (-not (Test-Path -LiteralPath $StatePath)) {
        return
    }
    $saved = Get-Content -LiteralPath $StatePath -Raw | ConvertFrom-Json
    foreach ($u in @($saved.users)) {
        $state.UsersByKey[$u.key] = [pscustomobject]@{
            key = [string]$u.key
            name = [string]$u.name
            email = [string]$u.email
            token = $null
            id = [string]$u.id
        }
        $state.UsersByEmail[$u.email] = $state.UsersByKey[$u.key]
    }
    $savedCoursesProp = $saved.PSObject.Properties["coursesByKey"]
    if ($null -ne $savedCoursesProp) {
        foreach ($courseProp in $saved.coursesByKey.PSObject.Properties) {
            $c = $courseProp.Value
            $state.CoursesByKey[$courseProp.Name] = [pscustomobject]@{
                key = [string]$c.key
                id = [string]$c.id
                title = [string]$c.title
                inviteCode = [string]$c.inviteCode
                ownerKey = [string]$c.ownerKey
            }
        }
    }
    $savedPostsProp = $saved.PSObject.Properties["postsByKey"]
    if ($null -ne $savedPostsProp) {
        foreach ($postProp in $saved.postsByKey.PSObject.Properties) {
            $p = $postProp.Value
            $state.PostsByKey[$postProp.Name] = [pscustomobject]@{
                key = [string]$p.key
                id = [string]$p.id
                type = [string]$p.type
                courseId = [string]$p.courseId
            }
        }
    }
    foreach ($id in @($saved.commentIds)) {
        $state.CommentIds.Add([string]$id)
    }
    $savedSolutionsProp = $saved.PSObject.Properties["solutionByPost"]
    if ($null -ne $savedSolutionsProp) {
        foreach ($solProp in $saved.solutionByPost.PSObject.Properties) {
            $state.SolutionByPost[$solProp.Name] = [string]$solProp.Value
        }
    }
}

function Get-PhaseIndex {
    param([string]$Phase)
    $phases = @("auth", "courses", "posts", "comments", "replies", "solutions", "teamWorkflows")
    for ($i = 0; $i -lt $phases.Count; $i++) {
        if ($phases[$i] -eq $Phase) { return $i }
    }
    throw "Unknown phase: $Phase"
}

function Should-RunPhase {
    param([string]$Phase)
    return (Get-PhaseIndex -Phase $Phase) -ge (Get-PhaseIndex -Phase $StartFrom)
}

function Add-IfNotNull {
    param(
        [hashtable]$Map,
        [string]$Key,
        $Value
    )
    if ($null -ne $Value) {
        $Map[$Key] = $Value
    }
}

function Get-OptionalProp {
    param(
        [Parameter(Mandatory = $true)]$Object,
        [Parameter(Mandatory = $true)][string]$Name
    )
    $prop = $Object.PSObject.Properties[$Name]
    if ($null -eq $prop) { return $null }
    return $prop.Value
}

function Ensure-UserSession {
    param($User, [string]$SharedPassword)

    if ($state.UsersByKey.ContainsKey($User.key)) {
        return $state.UsersByKey[$User.key]
    }

    Write-SeedLog -Context $ctx -Message ("Auth bootstrap for {0}" -f $User.email)

    if ((Get-PhaseIndex -Phase $StartFrom) -le (Get-PhaseIndex -Phase "auth")) {
        $registerBody = @{
            email       = [string]$User.email
            password    = [string]$SharedPassword
            credentials = [string]$User.name
        }
        [void](Invoke-ApiRequest -Context $ctx -Method "POST" -Path "/api/auth/register" -Body $registerBody -AllowFailure)
    }

    $loginBody = @{
        email    = [string]$User.email
        password = [string]$SharedPassword
    }
    $loginResp = Invoke-ApiRequest -Context $ctx -Method "POST" -Path "/api/auth/login" -Body $loginBody
    $token = Get-TokenFromResponse -Response $loginResp
    if (-not $token) {
        throw "No token in login response for $($User.email). Raw body: $($loginResp.BodyText)"
    }

    $meResp = Invoke-ApiRequest -Context $ctx -Method "GET" -Path "/api/users" -Token $token -Body $null
    $meData = Get-ApiData -JsonObject $meResp.Json
    $userId = Get-FirstValue -Object $meData -Paths @("id")
    if (-not $userId) {
        $userId = [guid]::NewGuid().Guid
        Write-SeedLog -Context $ctx -Level "WARN" -Message ("Cannot resolve user id for {0}; generated local id {1}" -f $User.email, $userId)
    }

    $entry = [pscustomobject]@{
        key   = [string]$User.key
        name  = [string]$User.name
        email = [string]$User.email
        token = [string]$token
        id    = [string]$userId
    }
    $state.UsersByKey[$User.key] = $entry
    $state.UsersByEmail[$User.email] = $entry
    return $entry
}

function Resolve-CourseMembers {
    param([string]$CourseId, [string]$OwnerToken)
    $resp = Invoke-ApiRequest -Context $ctx -Method "GET" -Path ("/api/course/{0}/members?skip=0&take=200" -f $CourseId) -Token $OwnerToken -Body $null
    $dataObj = Get-ApiData -JsonObject $resp.Json
    $records = Get-FirstValue -Object $dataObj -Paths @("records")
    if ($records -is [System.Array]) { return $records }
    if ($records) { return @($records) }
    if ($dataObj -is [System.Array]) { return $dataObj }
    return @()
}

function Create-CourseAndMembers {
    param($Course, [string]$SharedPassword)
    $owner = Get-UserByKey -Key $Course.owner

    Write-SeedLog -Context $ctx -Message ("Create course [{0}] by {1}" -f $Course.title, $owner.email)
    $createResp = Invoke-ApiRequest -Context $ctx -Method "POST" -Path "/api/course" -Token $owner.token -Body @{ title = [string]$Course.title }
    $courseId = Get-IdFromResponse -Response $createResp
    if (-not $courseId) {
        throw "Course ID not found for $($Course.title)"
    }

    $courseInfoResp = Invoke-ApiRequest -Context $ctx -Method "GET" -Path ("/api/course/{0}" -f $courseId) -Token $owner.token -Body $null
    $courseInfo = Get-ApiData -JsonObject $courseInfoResp.Json
    $inviteCode = Get-FirstValue -Object $courseInfo -Paths @("inviteCode")
    if (-not $inviteCode) {
        throw "Invite code missing for course $($Course.title)"
    }

    foreach ($memberKey in @($Course.members)) {
        $member = Get-UserByKey -Key ([string]$memberKey)
        Write-SeedLog -Context $ctx -Message ("Join course [{0}] as {1}" -f $Course.title, $member.email)
        [void](Invoke-ApiRequest -Context $ctx -Method "POST" -Path "/api/course/join" -Token $member.token -Body @{ inviteCode = $inviteCode } -AllowFailure)
    }

    $members = Resolve-CourseMembers -CourseId $courseId -OwnerToken $owner.token
    foreach ($override in @($Course.roleOverrides)) {
        $targetUser = Get-UserByKey -Key ([string]$override.user)
        if ($targetUser.key -eq $Course.owner) {
            # Backend forbids changing own role; owner role is already authoritative.
            continue
        }
        $memberId = $null
        foreach ($m in $members) {
            $email = Get-FirstValue -Object $m -Paths @("email")
            if ($email -and $email -eq $targetUser.email) {
                $memberId = Get-FirstValue -Object $m -Paths @("id")
                break
            }
        }
        if (-not $memberId) {
            Write-SeedLog -Context $ctx -Level "WARN" -Message ("Cannot find member id for role override {0} in course {1}" -f $targetUser.email, $Course.title)
            continue
        }
        [void](Invoke-ApiRequest -Context $ctx -Method "PUT" -Path ("/api/course/{0}/members/{1}/role" -f $courseId, $memberId) -Token $owner.token -Body @{ role = [string]$override.role } -AllowFailure)
    }

    $state.CoursesByKey[$Course.key] = [pscustomobject]@{
        key       = [string]$Course.key
        id        = [string]$courseId
        title     = [string]$Course.title
        inviteCode = [string]$inviteCode
        ownerKey  = [string]$Course.owner
    }
}

function Create-Post {
    param($Post)
    if (-not $state.CoursesByKey.ContainsKey($Post.course)) {
        throw "Unknown course key in post: $($Post.key)"
    }

    $course = $state.CoursesByKey[$Post.course]
    $owner = Get-UserByKey -Key $course.ownerKey
    $deadlineDaysFromNow = Get-OptionalProp -Object $Post -Name "deadlineDaysFromNow"
    $deadline = $null
    if ($null -ne $deadlineDaysFromNow) {
        $deadline = (Get-Date).ToUniversalTime().AddDays([double]$deadlineDaysFromNow).ToString("o")
    }

    $body = @{}
    Add-IfNotNull -Map $body -Key "type" -Value ([string]$Post.type)
    Add-IfNotNull -Map $body -Key "title" -Value ([string]$Post.title)
    Add-IfNotNull -Map $body -Key "text" -Value ([string]$Post.text)
    Add-IfNotNull -Map $body -Key "deadline" -Value $deadline
    Add-IfNotNull -Map $body -Key "taskType" -Value (Get-OptionalProp -Object $Post -Name "taskType")
    Add-IfNotNull -Map $body -Key "maxScore" -Value (Get-OptionalProp -Object $Post -Name "maxScore")
    Add-IfNotNull -Map $body -Key "solvableAfterDeadline" -Value (Get-OptionalProp -Object $Post -Name "solvableAfterDeadline")
    Add-IfNotNull -Map $body -Key "minTeamSize" -Value (Get-OptionalProp -Object $Post -Name "minTeamSize")
    Add-IfNotNull -Map $body -Key "maxTeamSize" -Value (Get-OptionalProp -Object $Post -Name "maxTeamSize")
    Add-IfNotNull -Map $body -Key "captainMode" -Value (Get-OptionalProp -Object $Post -Name "captainMode")
    Add-IfNotNull -Map $body -Key "votingDurationHours" -Value (Get-OptionalProp -Object $Post -Name "votingDurationHours")
    Add-IfNotNull -Map $body -Key "predefinedTeamsCount" -Value (Get-OptionalProp -Object $Post -Name "predefinedTeamsCount")
    Add-IfNotNull -Map $body -Key "allowJoinTeam" -Value (Get-OptionalProp -Object $Post -Name "allowJoinTeam")
    Add-IfNotNull -Map $body -Key "allowLeaveTeam" -Value (Get-OptionalProp -Object $Post -Name "allowLeaveTeam")
    Add-IfNotNull -Map $body -Key "allowStudentTransferCaptain" -Value (Get-OptionalProp -Object $Post -Name "allowStudentTransferCaptain")

    Write-SeedLog -Context $ctx -Message ("Create post [{0}] in {1}" -f $Post.title, $course.title)
    $resp = Invoke-ApiRequest -Context $ctx -Method "POST" -Path ("/api/course/{0}/task" -f $course.id) -Token $owner.token -Body $body
    $postId = Get-IdFromResponse -Response $resp
    if (-not $postId) {
        throw "Post id not found for $($Post.key)"
    }

    $state.PostsByKey[$Post.key] = [pscustomobject]@{
        key      = [string]$Post.key
        id       = [string]$postId
        type     = [string]$Post.type
        courseId = [string]$course.id
    }
}

function Create-CommentAndSaveRef {
    param($Comment)
    $postRef = $state.PostsByKey[$Comment.post]
    $author = Get-UserByKey -Key $Comment.author
    $resp = Invoke-ApiRequest -Context $ctx -Method "POST" -Path ("/api/post/{0}/comment" -f $postRef.id) -Token $author.token -Body @{ text = [string]$Comment.text }
    $commentId = Get-IdFromResponse -Response $resp
    if (-not $commentId) {
        $commentId = [guid]::NewGuid().Guid
        Write-SeedLog -Context $ctx -Level "WARN" -Message ("Cannot read comment id for post {0}; generated local id" -f $Comment.post)
    }
    $state.CommentIds.Add([string]$commentId)
}

function Create-Reply {
    param($Reply)
    $author = Get-UserByKey -Key $Reply.author
    $index = [int]$Reply.commentRef
    if ($index -lt 0 -or $index -ge $state.CommentIds.Count) {
        Write-SeedLog -Context $ctx -Level "WARN" -Message ("Reply skipped: commentRef {0} is out of range" -f $index)
        return
    }
    $commentId = $state.CommentIds[$index]
    [void](Invoke-ApiRequest -Context $ctx -Method "POST" -Path ("/api/comment/{0}/reply" -f $commentId) -Token $author.token -Body @{ text = [string]$Reply.text } -AllowFailure)
}

function Create-SolutionAndReview {
    param($Solution)
    $post = $state.PostsByKey[$Solution.post]
    $author = Get-UserByKey -Key $Solution.author
    $submitResp = Invoke-ApiRequest -Context $ctx -Method "PUT" -Path ("/api/task/{0}/solution" -f $post.id) -Token $author.token -Body @{ text = [string]$Solution.text; files = @() } -AllowFailure
    $solutionId = Get-IdFromResponse -Response $submitResp
    if (-not $solutionId) {
        Write-SeedLog -Context $ctx -Level "WARN" -Message ("Submit solution did not return id for post {0}" -f $Solution.post)
        return
    }
    $state.SolutionByPost[$Solution.post] = [string]$solutionId

    if ($null -ne $Solution.review) {
        $reviewer = Get-UserByKey -Key $Solution.review.reviewer
        $reviewBody = @{
            status  = [string]$Solution.review.status
            score   = $Solution.review.score
            comment = [string]$Solution.review.comment
        }
        [void](Invoke-ApiRequest -Context $ctx -Method "POST" -Path ("/api/solution/{0}/review" -f $solutionId) -Token $reviewer.token -Body $reviewBody -AllowFailure)
    }
}

function Resolve-TeamByIndex {
    param(
        [string]$AssignmentId,
        [string]$Token,
        [int]$TeamIndex
    )
    $resp = Invoke-ApiRequest -Context $ctx -Method "GET" -Path ("/api/team-task/{0}/teams" -f $AssignmentId) -Token $Token -Body $null
    $dataObj = Get-ApiData -JsonObject $resp.Json
    $teams = @()
    if ($dataObj -is [System.Array]) {
        $teams = $dataObj
    } elseif ($dataObj) {
        $teams = @($dataObj)
    }
    if ($TeamIndex -lt 0 -or $TeamIndex -ge $teams.Count) {
        return $null
    }
    return $teams[$TeamIndex]
}

function Execute-TeamWorkflow {
    param($Flow)
    if (-not $state.PostsByKey.ContainsKey($Flow.post)) {
        Write-SeedLog -Context $ctx -Level "WARN" -Message ("Team flow skipped. Unknown post key: {0}" -f $Flow.post)
        return
    }

    $post = $state.PostsByKey[$Flow.post]
    foreach ($jp in @($Flow.joinPlan)) {
        $user = Get-UserByKey -Key $jp.user
        $team = Resolve-TeamByIndex -AssignmentId $post.id -Token $user.token -TeamIndex ([int]$jp.teamIndex)
        if ($null -eq $team) {
            Write-SeedLog -Context $ctx -Level "WARN" -Message ("Join skipped for {0}: team index {1} unavailable" -f $user.email, $jp.teamIndex)
            continue
        }
        $teamId = Get-FirstValue -Object $team -Paths @("id")
        if (-not $teamId) {
            Write-SeedLog -Context $ctx -Level "WARN" -Message ("Join skipped for {0}: team id missing" -f $user.email)
            continue
        }
        [void](Invoke-ApiRequest -Context $ctx -Method "POST" -Path ("/api/teams/{0}/join" -f $teamId) -Token $user.token -Body $null -AllowFailure)
    }

    $teamSolutionId = $null
    if ($null -ne $Flow.submit) {
        $submitUser = Get-UserByKey -Key $Flow.submit.author
        $submitResp = Invoke-ApiRequest -Context $ctx -Method "PUT" -Path ("/api/team-task/{0}/solution" -f $post.id) -Token $submitUser.token -Body @{ text = [string]$Flow.submit.text; files = @() } -AllowFailure
        $teamSolutionId = Get-IdFromResponse -Response $submitResp
    }

    if ($teamSolutionId -and $null -ne $Flow.review) {
        $reviewer = Get-UserByKey -Key $Flow.review.reviewer
        $reviewBody = @{
            status  = [string]$Flow.review.status
            score   = $Flow.review.score
            comment = [string]$Flow.review.comment
        }
        [void](Invoke-ApiRequest -Context $ctx -Method "POST" -Path ("/api/team-solution/{0}/review" -f $teamSolutionId) -Token $reviewer.token -Body $reviewBody -AllowFailure)
    }

    if ($null -eq $Flow.gradeDistribution) { return }

    $updateUser = Get-UserByKey -Key $Flow.gradeDistribution.updatedBy
    $myTeamResp = Invoke-ApiRequest -Context $ctx -Method "GET" -Path ("/api/team-task/{0}/my-team" -f $post.id) -Token $updateUser.token -Body $null -AllowFailure
    $myTeam = Get-ApiData -JsonObject $myTeamResp.Json
    if ($null -eq $myTeam) {
        Write-SeedLog -Context $ctx -Level "WARN" -Message "Grade distribution skipped: my team is empty."
        return
    }

    $teamId = Get-FirstValue -Object $myTeam -Paths @("id")
    $members = Get-FirstValue -Object $myTeam -Paths @("members")
    if (-not $teamId -or -not $members) {
        Write-SeedLog -Context $ctx -Level "WARN" -Message "Grade distribution skipped: team id or members missing."
        return
    }

    $entries = @()
    foreach ($m in @($members)) {
        $memberId = Get-FirstValue -Object $m -Paths @("id")
        $memberEmail = $null
        $user = $null
        if ($memberId) {
            foreach ($known in $state.UsersByKey.Values) {
                if ($known.id -eq $memberId) {
                    $user = $known
                    break
                }
            }
        }
        if ($null -ne $user) {
            $memberEmail = $user.email
        }
        if (-not $memberEmail) { continue }
        $pointsProp = $Flow.gradeDistribution.entriesByEmail.PSObject.Properties[$memberEmail]
        $points = if ($null -ne $pointsProp) { $pointsProp.Value } else { $null }
        if ($null -eq $points) { continue }
        $entries += @{
            userId = [string]$memberId
            points = [double]$points
        }
    }

    if ($entries.Count -gt 0) {
        [void](Invoke-ApiRequest -Context $ctx -Method "PUT" -Path ("/api/teams/{0}/assignments/{1}/grade-distribution" -f $teamId, $post.id) -Token $updateUser.token -Body @{ entries = $entries } -AllowFailure)
    }

    foreach ($vote in @($Flow.gradeDistribution.voteBy)) {
        $voter = Get-UserByKey -Key $vote.user
        [void](Invoke-ApiRequest -Context $ctx -Method "POST" -Path ("/api/teams/{0}/assignments/{1}/grade-distribution/vote" -f $teamId, $post.id) -Token $voter.token -Body @{ vote = [string]$vote.vote } -AllowFailure)
    }
}

try {
    Load-State

    foreach ($u in @($data.users)) {
        [void](Ensure-UserSession -User $u -SharedPassword ([string]$data.sharedPassword))
    }
    Save-State

    if (Should-RunPhase -Phase "courses") {
        foreach ($c in @($data.courses)) {
            Create-CourseAndMembers -Course $c -SharedPassword ([string]$data.sharedPassword)
        }
        Save-State
    } elseif ($state.CoursesByKey.Count -eq 0) {
        throw "StartFrom=$StartFrom requires existing course ids. Run once with -StartFrom auth/courses first."
    }

    if (Should-RunPhase -Phase "posts") {
        foreach ($p in @($data.posts)) {
            Create-Post -Post $p
        }
        Save-State
    } elseif ($state.PostsByKey.Count -eq 0) {
        throw "StartFrom=$StartFrom requires existing post ids. Run once with -StartFrom posts first."
    }

    if (Should-RunPhase -Phase "comments") {
        foreach ($comment in @($data.comments)) {
            Create-CommentAndSaveRef -Comment $comment
        }
        Save-State
    }

    if (Should-RunPhase -Phase "replies") {
        foreach ($reply in @($data.replies)) {
            Create-Reply -Reply $reply
        }
        Save-State
    }

    if (Should-RunPhase -Phase "solutions") {
        foreach ($solution in @($data.solutions)) {
            Create-SolutionAndReview -Solution $solution
        }
        Save-State
    }

    if (Should-RunPhase -Phase "teamWorkflows") {
        foreach ($flow in @($data.teamWorkflows)) {
            Execute-TeamWorkflow -Flow $flow
        }
        Save-State
    }

    Write-SeedLog -Context $ctx -Message "Seeder completed successfully."
    Write-Host ""
    Write-Host ("Log file: {0}" -f $ctx.LogFile)
}
catch {
    Write-SeedLog -Context $ctx -Level "ERROR" -Message $_.Exception.Message
    throw
}
