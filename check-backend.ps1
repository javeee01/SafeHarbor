$ProgressPreference = 'SilentlyContinue'

Write-Host "=== Testing POST with Origin header ==="
$body = '{"username":"demouser2","password":"Demo@1234","fullName":"Demo User","role":"FIELD_RESPONDER","contactNumber":"1234567890","assignedRegion":"Delhi"}'
try {
    $r = Invoke-WebRequest -Uri 'https://safeharbor-backend.onrender.com/api/auth/register' -Method POST -Body $body -ContentType 'application/json' -Headers @{
        'Origin' = 'https://javeee01.github.io'
    } -UseBasicParsing -TimeoutSec 30
    Write-Host "Status:" $r.StatusCode
    Write-Host "Headers:"
    $r.Headers.Keys | ForEach-Object { Write-Host "  $_`:" $r.Headers[$_] }
    Write-Host "Body:" $r.Content
} catch {
    if ($_.Exception.Response) {
        $statusCode = [int]$_.Exception.Response.StatusCode
        Write-Host "Status: $statusCode"
        Write-Host "Headers:"
        foreach ($key in $_.Exception.Response.Headers.AllKeys) {
            Write-Host "  ${key}:" $_.Exception.Response.Headers[$key]
        }
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Host "Body:" $reader.ReadToEnd()
    } else {
        Write-Host "Error: $_"
    }
}
