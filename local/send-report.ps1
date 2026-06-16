param(
    [string]$Suite = "Local Test"
)

# Read config
$configFile = Join-Path $PSScriptRoot "email-config.properties"
if (-not (Test-Path $configFile)) {
    Write-Host "ERROR: email-config.properties not found!" -ForegroundColor Red
    exit 1
}

$config = @{}
Get-Content $configFile | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.+)$') {
        $config[$matches[1].Trim()] = $matches[2].Trim()
    }
}

if ($config['smtp.password'] -eq 'YOUR_APP_PASSWORD_HERE') {
    Write-Host "ERROR: Set Gmail App Password in local/email-config.properties" -ForegroundColor Red
    exit 1
}

# Count module-level test cases (12 total)
$reportFile = "target\surefire-reports\testng-results.xml"

# Helper: get test method status from XML
function Get-TestStatus($methodName) {
    if (-not (Test-Path $reportFile)) { return "SKIP" }
    $content = Get-Content $reportFile -Raw
    if ($content -match "name=`"$methodName`"") {
        if ($content -match "name=`"$methodName`"[\s\S]*?status=`"FAIL`"") {
            return "FAIL"
        }
        return "PASS"
    }
    return "SKIP"
}

function Get-StatusIcon($status) {
    switch ($status) {
        "PASS" { return "<span style='color:#28a745;font-weight:bold;'>&#9989; PASS</span>" }
        "FAIL" { return "<span style='color:#dc3545;font-weight:bold;'>&#10060; FAIL</span>" }
        default { return "<span style='color:#ffc107;'>&#9889; SKIP</span>" }
    }
}

# Get individual test statuses
$s_register1 = Get-TestStatus "registerBatch1"
$s_register2 = Get-TestStatus "registerBatch2"
$s_login = Get-TestStatus "step1_login"
$s_navigate = Get-TestStatus "step2_navigateToCreateOrg"
$s_about = Get-TestStatus "step3_aboutSection"
$s_category = Get-TestStatus "step4_selectCategory"
$s_membership = Get-TestStatus "step12_membership"
$s_submit = Get-TestStatus "step15_submit"
$s_logout = Get-TestStatus "step18_logoutAfterCreate"
$s_approve = Get-TestStatus "step19_superAdminApprove"

# Main flow tests (all-modules)
$s_publicPages = Get-TestStatus "publicPages"
$s_registerYouth = Get-TestStatus "registerIndianYouth"
$s_verifyDB = Get-TestStatus "verifyUserInDatabase"
$s_logoutUser = Get-TestStatus "logoutUser"
$s_loginOTP = Get-TestStatus "loginWithOTP"
$s_profile = Get-TestStatus "completeYouthProfile"
$s_regCert = Get-TestStatus "verifyRegistrationCertificateDownload"
$s_basicInfo = Get-TestStatus "clickBasicInfoAndExtractEmail"
$s_quiz = Get-TestStatus "attemptCompetitiveQuiz"
$s_quizCert = Get-TestStatus "verifyQuizCertificateDownload"
$s_blog = Get-TestStatus "writeAndPublishBlog"

# Count module-level test cases (13 modules total)
$allStatuses = @($s_publicPages, $s_registerYouth, $s_verifyDB, $s_logoutUser, $s_loginOTP, $s_profile, $s_regCert, $s_basicInfo, $s_quiz, $s_quizCert, $s_blog, $s_submit, $s_approve)
$total = ($allStatuses | Where-Object { $_ -ne "SKIP" }).Count
$passed = ($allStatuses | Where-Object { $_ -eq "PASS" }).Count
$failed = ($allStatuses | Where-Object { $_ -eq "FAIL" }).Count
$skipped = ($allStatuses | Where-Object { $_ -eq "SKIP" }).Count

# Determine overall status
if ($failed -eq 0) {
    $status = "ALL TESTS PASSED"
    $emoji = "&#9989;"
    $color = "#28a745"
} else {
    $status = "SOME TESTS FAILED"
    $emoji = "&#10060;"
    $color = "#dc3545"
}

$timestamp = Get-Date -Format "dd-MMM-yyyy hh:mm tt IST"
$envName = if ($env:env) { $env:env.ToUpper() } else { "PROD" }
$baseUrl = if ($envName -eq "PROD") { "https://mybharat.gov.in" } else { "https://yuva-beta.mybharats.in" }

# Build HTML
$htmlBody = @"
<div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 700px; margin: 0 auto;">
  <div style="background: linear-gradient(135deg, #160363, #2d0a8f); padding: 25px; text-align: center; border-radius: 8px 8px 0 0;">
    <h1 style="color: #fff; margin: 0; font-size: 22px;">MY Bharat &#8212; QA Automation Report</h1>
    <p style="color: #c4b5fd; margin: 8px 0 0; font-size: 13px;">$envName Environment | Local E2E Test Execution</p>
  </div>
  <div style="background: $color; padding: 14px; text-align: center;">
    <h2 style="color: #fff; margin: 0; font-size: 20px;">$emoji $status</h2>
  </div>
  <div style="padding: 25px; border: 1px solid #e0e0e0; border-top: none;">
    <h3 style="color: #160363; margin: 0 0 12px; border-bottom: 2px solid #160363; padding-bottom: 5px;">Execution Details</h3>
    <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px;">
      <tr><td style="padding: 8px; font-weight: bold; width: 40%;">Execution Time:</td><td style="padding: 8px;">$timestamp</td></tr>
      <tr style="background:#f9f9f9;"><td style="padding: 8px; font-weight: bold;">Environment:</td><td style="padding: 8px;">$envName ($baseUrl)</td></tr>
      <tr><td style="padding: 8px; font-weight: bold;">Browser:</td><td style="padding: 8px;">Google Chrome (UI Mode)</td></tr>
      <tr style="background:#f9f9f9;"><td style="padding: 8px; font-weight: bold;">Test Suite:</td><td style="padding: 8px;">$Suite</td></tr>
      <tr><td style="padding: 8px; font-weight: bold;">Machine:</td><td style="padding: 8px;">$env:COMPUTERNAME (Local)</td></tr>
    </table>

    <h3 style="color: #160363; margin: 0 0 12px; border-bottom: 2px solid #160363; padding-bottom: 5px;">Test Results Summary</h3>
    <table style="width: 100%; border-collapse: collapse; text-align: center; margin-bottom: 20px;">
      <tr style="background: #160363; color: white;">
        <th style="padding: 10px;">Total Tests</th><th style="padding: 10px;">Passed</th><th style="padding: 10px;">Failed</th><th style="padding: 10px;">Skipped</th>
      </tr>
      <tr style="font-size: 20px; font-weight: bold;">
        <td style="padding: 12px; border: 1px solid #ddd;">$total</td>
        <td style="padding: 12px; border: 1px solid #ddd; color: #28a745;">$passed</td>
        <td style="padding: 12px; border: 1px solid #ddd; color: #dc3545;">$failed</td>
        <td style="padding: 12px; border: 1px solid #ddd; color: #ffc107;">$skipped</td>
      </tr>
    </table>

    <h3 style="color: #160363; margin: 0 0 12px; border-bottom: 2px solid #160363; padding-bottom: 5px;">Test Cases Executed</h3>
    <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 12px;">
      <tr style="background: #160363; color: white;">
        <th style="padding: 6px;">#</th>
        <th style="padding: 6px; text-align: left;">Module</th>
        <th style="padding: 6px; text-align: left;">Test Case</th>
        <th style="padding: 6px; text-align: left;">Description</th>
        <th style="padding: 6px;">Device</th>
        <th style="padding: 6px;">Status</th>
        <th style="padding: 6px; text-align: left;">Team Lead</th>
      </tr>
      <tr style="background: #f9f9f9;">
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">1</td>
        <td style="padding: 6px; border: 1px solid #eee;">Public Pages</td>
        <td style="padding: 6px; border: 1px solid #eee;">publicPages</td>
        <td style="padding: 6px; border: 1px solid #eee;">Header, Org, Footer navigation</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_publicPages)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Prashant/Hariom</td>
      </tr>
      <tr>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">2</td>
        <td style="padding: 6px; border: 1px solid #eee;">Registration</td>
        <td style="padding: 6px; border: 1px solid #eee;">registerIndianYouth</td>
        <td style="padding: 6px; border: 1px solid #eee;">Register new youth user with OTP</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_registerYouth)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Tejas</td>
      </tr>
      <tr style="background: #f9f9f9;">
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">3</td>
        <td style="padding: 6px; border: 1px solid #eee;">Registration</td>
        <td style="padding: 6px; border: 1px solid #eee;">verifyUserInDatabase</td>
        <td style="padding: 6px; border: 1px solid #eee;">Verify registered user exists in DB via Redash</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_verifyDB)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Tejas</td>
      </tr>
      <tr>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">4</td>
        <td style="padding: 6px; border: 1px solid #eee;">Logout</td>
        <td style="padding: 6px; border: 1px solid #eee;">logoutUser</td>
        <td style="padding: 6px; border: 1px solid #eee;">Logout after registration</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_logoutUser)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Tejas</td>
      </tr>
      <tr style="background: #f9f9f9;">
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">5</td>
        <td style="padding: 6px; border: 1px solid #eee;">Login</td>
        <td style="padding: 6px; border: 1px solid #eee;">loginWithOTP</td>
        <td style="padding: 6px; border: 1px solid #eee;">Login with OTP from Yopmail</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_loginOTP)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Tejas</td>
      </tr>
      <tr>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">6</td>
        <td style="padding: 6px; border: 1px solid #eee;">Profile</td>
        <td style="padding: 6px; border: 1px solid #eee;">completeYouthProfile</td>
        <td style="padding: 6px; border: 1px solid #eee;">Fill all profile sections</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_profile)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Alamgeer</td>
      </tr>
      <tr style="background: #f9f9f9;">
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">7</td>
        <td style="padding: 6px; border: 1px solid #eee;">Certificate</td>
        <td style="padding: 6px; border: 1px solid #eee;">verifyRegistrationCertificateDownload</td>
        <td style="padding: 6px; border: 1px solid #eee;">Download certificate PNG</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_regCert)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Alamgeer</td>
      </tr>
      <tr>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">8</td>
        <td style="padding: 6px; border: 1px solid #eee;">Basic Info</td>
        <td style="padding: 6px; border: 1px solid #eee;">clickBasicInfoAndExtractEmail</td>
        <td style="padding: 6px; border: 1px solid #eee;">Verify email in Basic Info</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_basicInfo)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Alamgeer</td>
      </tr>
      <tr style="background: #f9f9f9;">
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">9</td>
        <td style="padding: 6px; border: 1px solid #eee;">Quiz</td>
        <td style="padding: 6px; border: 1px solid #eee;">attemptCompetitiveQuiz</td>
        <td style="padding: 6px; border: 1px solid #eee;">Attempt competitive quiz</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_quiz)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Uvais</td>
      </tr>
      <tr>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">10</td>
        <td style="padding: 6px; border: 1px solid #eee;">Quiz Certificate</td>
        <td style="padding: 6px; border: 1px solid #eee;">verifyQuizCertificateDownload</td>
        <td style="padding: 6px; border: 1px solid #eee;">Download quiz certificate</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_quizCert)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Uvais</td>
      </tr>
      <tr style="background: #f9f9f9;">
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">11</td>
        <td style="padding: 6px; border: 1px solid #eee;">Blog</td>
        <td style="padding: 6px; border: 1px solid #eee;">writeAndPublishBlog</td>
        <td style="padding: 6px; border: 1px solid #eee;">Create blog and verify Pending status</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_blog)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Sonali</td>
      </tr>
      <tr style="background: #e8f5e9;">
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">12</td>
        <td style="padding: 6px; border: 1px solid #eee;"><b>Youth Club</b></td>
        <td style="padding: 6px; border: 1px solid #eee;">createYouthClub</td>
        <td style="padding: 6px; border: 1px solid #eee;">Register 6 members + Create Youth Club + Verify members</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_submit)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Tejas</td>
      </tr>
      <tr style="background: #e8f5e9;">
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">13</td>
        <td style="padding: 6px; border: 1px solid #eee;"><b>Youth Club Approve</b></td>
        <td style="padding: 6px; border: 1px solid #eee;">approveYouthClub</td>
        <td style="padding: 6px; border: 1px solid #eee;">Login SuperAdmin and approve Youth Club</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">Web</td>
        <td style="padding: 6px; border: 1px solid #eee; text-align: center;">$(Get-StatusIcon $s_approve)</td>
        <td style="padding: 6px; border: 1px solid #eee;">Tejas</td>
      </tr>
    </table>

    <h3 style="color: #160363; margin: 0 0 12px; border-bottom: 2px solid #160363; padding-bottom: 5px;">Test Flow</h3>
    <p style="font-size: 13px; color: #555; line-height: 1.8;">
      Public Pages &#8594; Registration &#8594; Logout &#8594; Login (OTP) &#8594; Profile &#8594; Certificate &#8594; Basic Info &#8594; Quiz &#8594; Quiz Certificate &#8594; Blog &#8594; <b>Create Youth Club &#8594; Approve Youth Club</b>
    </p>
  </div>
  <div style="background: #f8f9fa; padding: 12px; text-align: center; border-radius: 0 0 8px 8px; border: 1px solid #e0e0e0; border-top: none;">
    <p style="color: #888; font-size: 11px; margin: 0;">
      MY Bharat | QA Automation Team | Local Test Run | $timestamp
    </p>
  </div>
</div>
"@

# Send email
$subject = "$($config['report.subject.prefix']) $Suite | $status | $timestamp"

try {
    $securePassword = ConvertTo-SecureString $config['smtp.password'] -AsPlainText -Force
    $cred = New-Object System.Management.Automation.PSCredential($config['smtp.username'], $securePassword)

    $mailParams = @{
        From       = $config['email.from']
        To         = $config['email.to']
        Subject    = $subject
        Body       = $htmlBody
        BodyAsHtml = $true
        SmtpServer = $config['smtp.server']
        Port       = [int]$config['smtp.port']
        UseSsl     = $true
        Credential = $cred
    }

    $extentReport = "reports\index.html"
    if (Test-Path $extentReport) {
        $mailParams['Attachments'] = (Resolve-Path $extentReport).Path
    }

    Send-MailMessage @mailParams

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host " Email Report Sent Successfully!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  To:      $($config['email.to'])"
    Write-Host "  Subject: $subject"
    Write-Host "  Results: P=$passed F=$failed S=$skipped"
    Write-Host "========================================" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Failed to send email - $_" -ForegroundColor Red
    Write-Host "Fix: Set Gmail App Password in local/email-config.properties" -ForegroundColor Yellow
    Write-Host "URL: https://myaccount.google.com/apppasswords" -ForegroundColor Yellow
    exit 1
}
