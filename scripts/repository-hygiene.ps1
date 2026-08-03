$ErrorActionPreference = "Stop"

$tracked = git ls-files
$forbidden = @(".env", ".db", ".sqlite", ".sqlite3", ".apk", ".aab", "token", "secret", "credential")
$violations = @($tracked | Where-Object {
    $path = $_.ToLowerInvariant()
    $forbidden | Where-Object { $path.Contains($_) } | Select-Object -First 1
})
$violations = @($violations | Where-Object { $_ -notlike "*.env.example" })

if ($violations.Count -gt 0) {
    Write-Error ("Potential private files tracked:`n" + ($violations -join "`n"))
}

Write-Output "Repository hygiene check passed."
