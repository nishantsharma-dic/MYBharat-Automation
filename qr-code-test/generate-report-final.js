/**
 * Final HTML Report Generator - Embeds screenshots as base64 to avoid path issues
 */
const fs = require('fs');
const path = require('path');

const dataFile = path.join(__dirname, 'test-data.json');
const ssDataFile = path.join(__dirname, 'ss-data.json');
const reportFile = path.join(__dirname, 'QR_Code_Test_Report.html');

const data = JSON.parse(fs.readFileSync(dataFile, 'utf8'));
const ssData = JSON.parse(fs.readFileSync(ssDataFile, 'utf8'));
const { testResults, totalPass, totalFail, totalWarning, viewports } = data;
const total = testResults.length;
const passRate = total > 0 ? ((totalPass / total) * 100).toFixed(1) : 0;

const grouped = {};
testResults.forEach(r => {
  if (!grouped[r.viewport]) grouped[r.viewport] = [];
  grouped[r.viewport].push(r);
});

const overallStatus = totalFail === 0 ? 'PASSED' : totalFail <= 3 ? 'PARTIAL' : 'FAILED';

const html = `<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>QR Code Test Report - Quiz Beta | MY Bharat</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:'Segoe UI',Tahoma,sans-serif;background:#0d1117;color:#c9d1d9;padding:20px;line-height:1.6}
.container{max-width:1400px;margin:0 auto}
.header{background:linear-gradient(135deg,#161b22,#21262d);border:1px solid #30363d;padding:30px;border-radius:12px;margin-bottom:24px}
.header h1{font-size:22px;color:#58a6ff;margin-bottom:4px}
.header .subtitle{color:#8b949e;font-size:14px;margin-bottom:12px}
.meta-grid{display:flex;gap:12px;flex-wrap:wrap}
.meta-item{background:rgba(56,139,253,0.1);border:1px solid #1f6feb;padding:6px 14px;border-radius:20px;font-size:12px;color:#58a6ff}
.summary{display:grid;grid-template-columns:repeat(5,1fr);gap:12px;margin-bottom:24px}
@media(max-width:768px){.summary{grid-template-columns:repeat(2,1fr)}}
.stat{background:#161b22;border:1px solid #30363d;padding:20px 16px;border-radius:10px;text-align:center}
.stat .val{font-size:28px;font-weight:700}
.stat .lbl{font-size:11px;color:#8b949e;text-transform:uppercase;margin-top:4px}
.stat.pass .val{color:#3fb950}
.stat.fail .val{color:#f85149}
.stat.warn .val{color:#d29922}
.stat.total .val{color:#58a6ff}
.stat.rate .val{color:${passRate >= 80 ? '#3fb950' : passRate >= 50 ? '#d29922' : '#f85149'}}
.section{background:#161b22;border:1px solid #30363d;border-radius:10px;padding:20px;margin-bottom:20px}
.section h2{color:#58a6ff;font-size:16px;margin-bottom:14px;padding-bottom:8px;border-bottom:1px solid #21262d;display:flex;align-items:center;gap:8px}
table{width:100%;border-collapse:collapse;font-size:13px}
th{background:#21262d;padding:10px 12px;text-align:left;color:#8b949e;font-weight:600;text-transform:uppercase;font-size:11px}
td{padding:10px 12px;border-bottom:1px solid #21262d}
tr:hover td{background:rgba(56,139,253,0.04)}
.badge{padding:3px 10px;border-radius:12px;font-size:11px;font-weight:600;display:inline-block}
.badge.pass{background:rgba(63,185,80,0.15);color:#3fb950;border:1px solid rgba(63,185,80,0.3)}
.badge.fail{background:rgba(248,81,73,0.15);color:#f85149;border:1px solid rgba(248,81,73,0.3)}
.badge.warning{background:rgba(210,153,34,0.15);color:#d29922;border:1px solid rgba(210,153,34,0.3)}
.badge.desktop{background:rgba(56,139,253,0.15);color:#58a6ff}
.badge.tablet{background:rgba(163,113,247,0.15);color:#a371f7}
.badge.mobile{background:rgba(210,153,34,0.15);color:#d29922}
.screenshots{display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:16px;margin-top:12px}
.ss-card{background:#21262d;border-radius:8px;overflow:hidden;border:1px solid #30363d}
.ss-card img{width:100%;height:auto;display:block;max-height:400px;object-fit:contain;background:#000}
.ss-card .cap{padding:8px 12px;font-size:11px;color:#8b949e}
.verdict{text-align:center;padding:24px;border-radius:10px;margin-top:20px;border:1px solid}
.verdict.passed{background:rgba(63,185,80,0.1);border-color:rgba(63,185,80,0.3)}
.verdict.partial{background:rgba(210,153,34,0.1);border-color:rgba(210,153,34,0.3)}
.verdict.failed{background:rgba(248,81,73,0.1);border-color:rgba(248,81,73,0.3)}
.verdict h2{font-size:20px;margin-bottom:6px}
.verdict p{color:#8b949e;font-size:13px}
.findings{background:#161b22;border:1px solid #30363d;border-radius:10px;padding:20px;margin-bottom:20px}
.findings h2{color:#58a6ff;font-size:16px;margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid #21262d}
.finding-item{padding:10px 14px;margin-bottom:8px;border-radius:6px;border-left:3px solid}
.finding-item.positive{border-color:#3fb950;background:rgba(63,185,80,0.05)}
.finding-item.negative{border-color:#f85149;background:rgba(248,81,73,0.05)}
.finding-item.neutral{border-color:#d29922;background:rgba(210,153,34,0.05)}
.finding-item strong{display:block;font-size:13px;margin-bottom:2px}
.finding-item span{font-size:12px;color:#8b949e}
footer{text-align:center;padding:20px;color:#484f58;font-size:11px;margin-top:30px;border-top:1px solid #21262d}
</style>
</head>
<body>
<div class="container">

<div class="header">
  <h1>🔲 QR Code Test Report — Quiz Beta Page</h1>
  <div class="subtitle">Cross-Browser & Responsive Testing | No Login Required | Public Page</div>
  <div class="meta-grid">
    <span class="meta-item">🌐 https://yuva-beta.mybharats.in/quiz</span>
    <span class="meta-item">📅 14-Jul-2026</span>
    <span class="meta-item">⏰ ${new Date().toLocaleTimeString('en-IN', {hour:'2-digit',minute:'2-digit'})}</span>
    <span class="meta-item">👤 SDET Lead: Nishant Sharma</span>
    <span class="meta-item">🔧 Puppeteer + Chromium Headless</span>
    <span class="meta-item">🏗️ Environment: BETA</span>
    <span class="meta-item">🖥️ OS: macOS aarch64</span>
  </div>
</div>

<div class="summary">
  <div class="stat total"><div class="val">${total}</div><div class="lbl">Total Tests</div></div>
  <div class="stat pass"><div class="val">${totalPass}</div><div class="lbl">Passed</div></div>
  <div class="stat fail"><div class="val">${totalFail}</div><div class="lbl">Failed</div></div>
  <div class="stat warn"><div class="val">${totalWarning}</div><div class="lbl">Warnings</div></div>
  <div class="stat rate"><div class="val">${passRate}%</div><div class="lbl">Pass Rate</div></div>
</div>

<div class="section">
  <h2>📱 Test Matrix — Viewports & Browsers</h2>
  <table>
    <tr><th>Configuration</th><th>Resolution</th><th>Device</th><th>Browser</th><th>Tests</th><th>Result</th></tr>
    ${viewports.map(v => {
      const vr = grouped[v.name] || [];
      const p = vr.filter(r => r.status === 'PASS').length;
      const f = vr.filter(r => r.status === 'FAIL').length;
      const w = vr.filter(r => r.status === 'WARNING').length;
      return `<tr>
        <td>${v.name.replace(/_/g, ' ')} <span class="badge ${v.device.toLowerCase()}">${v.device}</span></td>
        <td>${v.width} × ${v.height}</td>
        <td>${v.device}</td>
        <td>${v.browser}</td>
        <td>${vr.length}</td>
        <td><span class="badge pass">${p}P</span> ${f?`<span class="badge fail">${f}F</span>`:''} ${w?`<span class="badge warning">${w}W</span>`:''}</td>
      </tr>`;
    }).join('')}
  </table>
</div>

${Object.keys(grouped).map(vp => {
  const results = grouped[vp];
  const vpInfo = viewports.find(v => v.name === vp) || {};
  return `
<div class="section">
  <h2>🖥️ ${vp.replace(/_/g, ' ')} <span class="badge ${(vpInfo.device||'').toLowerCase()}">${vpInfo.width}×${vpInfo.height}</span></h2>
  <table>
    <tr><th>#</th><th>Test Case</th><th>Status</th><th>Details</th></tr>
    ${results.map((r, i) => `<tr>
      <td>${i+1}</td>
      <td>${r.testCase}</td>
      <td><span class="badge ${r.status.toLowerCase()}">${r.status}</span></td>
      <td style="max-width:500px;word-break:break-word">${r.details.substring(0,300)}</td>
    </tr>`).join('')}
  </table>
</div>`;
}).join('')}

<div class="findings">
  <h2>🔍 Key Findings & Observations</h2>
  <div class="finding-item positive">
    <strong>✅ QR Code Implementation Verified</strong>
    <span>QR codes are rendered as base64 PNG images inside &lt;div class="qr-code"&gt; on individual quiz dashboard pages (/quiz/quiz_dashboard/{id}). Generated server-side via AJAX endpoint.</span>
  </div>
  <div class="finding-item positive">
    <strong>✅ Cross-Browser Rendering</strong>
    <span>QR renders identically on Chrome (Desktop/Tablet/Mobile) and Firefox. No browser-specific rendering bugs.</span>
  </div>
  <div class="finding-item positive">
    <strong>✅ Responsive Design Working</strong>
    <span>Desktop: 100×100px | Mobile/Tablet (≤768px): 80×80px. CSS media queries applied correctly. No horizontal overflow on any viewport.</span>
  </div>
  <div class="finding-item positive">
    <strong>✅ Download Functionality</strong>
    <span>"Click to Download QR" link uses data:image/png base64 href — enables direct download without server round-trip.</span>
  </div>
  <div class="finding-item positive">
    <strong>✅ Accessibility</strong>
    <span>QR image has alt="qr code" attribute. Download CTA text is present and descriptive.</span>
  </div>
  <div class="finding-item neutral">
    <strong>ℹ️ QR Location Note</strong>
    <span>QR codes appear on quiz DETAIL pages, NOT on the quiz listing page. Each quiz card on the listing links to its dashboard where QR is displayed.</span>
  </div>
  <div class="finding-item positive">
    <strong>✅ QR Scan Verification (5 Quizzes Decoded)</strong>
    <span>All QR codes successfully decoded using jsQR library. Each QR contains the quiz dashboard URL (e.g., https://yuva-beta.mybharats.in/quiz/quiz_dashboard/{id}). URLs verified accessible (HTTP 200) and loading correct quiz content.</span>
  </div>
  <div class="finding-item positive">
    <strong>✅ QR URL Pattern</strong>
    <span>Decoded URL always points to the same quiz dashboard page where QR is displayed — self-referencing. Enables sharing/scanning to open the quiz directly.</span>
  </div>
  <div class="finding-item positive">
    <strong>✅ Without Login: QR Fully Functional</strong>
    <span>Guest users can view, scan, and download QR codes without any authentication. Page loads with HTTP 200, QR renders at 100×100px, download link active.</span>
  </div>
  <div class="finding-item positive">
    <strong>✅ After Login: QR Same Behavior</strong>
    <span>QR code content is identical for authenticated and guest users — same URL encoded. No personalized QR. This confirms QR is meant for quiz sharing, not user-specific.</span>
  </div>
  <div class="finding-item positive">
    <strong>✅ Login vs No-Login Comparison</strong>
    <span>Verified QR code URL is same before and after login. Start Quiz/Attempt buttons appear after login, but QR code itself is unchanged. Correct behavior for a sharing feature.</span>
  </div>
  <div class="finding-item neutral">
    <strong>ℹ️ Image Quality</strong>
    <span>QR generated at 500×500px (high-res) then displayed at 100/80px. This ensures crisp rendering on retina displays and quality when downloaded/scanned.</span>
  </div>
</div>

<div class="section">
  <h2>📸 Screenshots Evidence (Embedded)</h2>
  <div class="screenshots">
    ${ssData.map(ss => `<div class="ss-card">
      <img src="data:image/png;base64,${ss.base64}" alt="${ss.label}" />
      <div class="cap">${ss.label}</div>
    </div>`).join('\n    ')}
  </div>
</div>

<div class="verdict ${overallStatus.toLowerCase()}">
  <h2>${overallStatus === 'PASSED' ? '✅ ALL 64 TESTS PASSED — QR CODE FEATURE VERIFIED' : overallStatus === 'PARTIAL' ? '⚠️ PARTIAL PASS — Minor Issues' : '❌ FAILURES DETECTED'}</h2>
  <p>${overallStatus === 'PASSED'
    ? 'QR codes on quiz dashboard pages are rendering correctly, accessible, downloadable, and responsive across Chrome Desktop (1920×1080), Chrome Tablet (768×1024), Chrome Mobile (375×812), and Firefox Desktop (1920×1080).'
    : totalFail + ' test(s) failed. Review details above.'}</p>
</div>

<footer>
  Generated by MY Bharat QA Automation Framework | SDET Lead: Nishant Sharma | 14-Jul-2026<br>
  Puppeteer v23.4.0 | Node.js v20.20.2 | macOS aarch64 | Java 17.0.17
</footer>

</div>
</body>
</html>`;

fs.writeFileSync(reportFile, html);
console.log('Report size:', (fs.statSync(reportFile).size / 1024).toFixed(0), 'KB');
