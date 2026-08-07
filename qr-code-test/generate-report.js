/**
 * HTML Report Generator for QR Code Tests
 */
const fs = require('fs');
const path = require('path');

const dataFile = path.join(__dirname, 'test-data.json');
const reportFile = path.join(__dirname, 'QR_Code_Test_Report.html');

if (!fs.existsSync(dataFile)) {
  console.error('No test data found. Run qr-test.js first.');
  process.exit(1);
}

const { testResults, totalPass, totalFail, totalWarning, viewports } = JSON.parse(fs.readFileSync(dataFile, 'utf8'));
const total = testResults.length;
const passRate = total > 0 ? ((totalPass / total) * 100).toFixed(1) : 0;

// Group results by viewport
const grouped = {};
testResults.forEach(r => {
  if (!grouped[r.viewport]) grouped[r.viewport] = [];
  grouped[r.viewport].push(r);
});

// Get screenshots
const screenshotDir = path.join(__dirname, 'screenshots');
const screenshots = fs.existsSync(screenshotDir) ? fs.readdirSync(screenshotDir).filter(f => f.endsWith('.png')) : [];

const html = `<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>QR Code Test Report - Quiz Beta Page</title>
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: 'Segoe UI', Tahoma, sans-serif; background: #1a1a2e; color: #eee; padding: 20px; }
  .header { background: linear-gradient(135deg, #16213e, #0f3460); padding: 30px; border-radius: 12px; margin-bottom: 20px; }
  .header h1 { font-size: 24px; color: #fff; margin-bottom: 8px; }
  .header .subtitle { color: #a0c4ff; font-size: 14px; }
  .header .meta { display: flex; gap: 20px; margin-top: 15px; flex-wrap: wrap; }
  .header .meta span { background: rgba(255,255,255,0.1); padding: 5px 12px; border-radius: 6px; font-size: 13px; }
  .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 15px; margin-bottom: 25px; }
  .summary-card { background: #16213e; padding: 20px; border-radius: 10px; text-align: center; border: 1px solid #333; }
  .summary-card .value { font-size: 32px; font-weight: bold; }
  .summary-card .label { font-size: 12px; color: #aaa; margin-top: 5px; text-transform: uppercase; }
  .pass .value { color: #4caf50; }
  .fail .value { color: #f44336; }
  .warn .value { color: #ff9800; }
  .total .value { color: #2196f3; }
  .rate .value { color: ${passRate >= 80 ? '#4caf50' : passRate >= 50 ? '#ff9800' : '#f44336'}; }
  .section { background: #16213e; border-radius: 10px; padding: 20px; margin-bottom: 20px; border: 1px solid #333; }
  .section h2 { color: #64b5f6; font-size: 18px; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #333; }
  .viewport-badge { display: inline-block; padding: 3px 10px; border-radius: 15px; font-size: 11px; margin-left: 8px; }
  .viewport-badge.desktop { background: #1b5e20; color: #a5d6a7; }
  .viewport-badge.tablet { background: #e65100; color: #ffcc80; }
  .viewport-badge.mobile { background: #4a148c; color: #ce93d8; }
  table { width: 100%; border-collapse: collapse; font-size: 13px; }
  th { background: #0f3460; padding: 10px; text-align: left; color: #90caf9; }
  td { padding: 10px; border-bottom: 1px solid #2a2a4a; }
  tr:hover { background: rgba(255,255,255,0.03); }
  .status { padding: 3px 10px; border-radius: 4px; font-weight: bold; font-size: 11px; display: inline-block; }
  .status.pass { background: #1b5e20; color: #a5d6a7; }
  .status.fail { background: #b71c1c; color: #ef9a9a; }
  .status.warning { background: #e65100; color: #ffcc80; }
  .screenshots { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 15px; margin-top: 15px; }
  .screenshot-card { background: #0f3460; border-radius: 8px; overflow: hidden; }
  .screenshot-card img { width: 100%; height: auto; display: block; }
  .screenshot-card .caption { padding: 8px 12px; font-size: 12px; color: #90caf9; }
  .overall-verdict { text-align: center; padding: 20px; border-radius: 10px; margin-top: 20px; }
  .overall-verdict.passed { background: linear-gradient(135deg, #1b5e20, #2e7d32); }
  .overall-verdict.failed { background: linear-gradient(135deg, #b71c1c, #c62828); }
  .overall-verdict.mixed { background: linear-gradient(135deg, #e65100, #f57c00); }
  .overall-verdict h2 { font-size: 22px; margin-bottom: 8px; }
  .overall-verdict p { color: #ddd; }
  .test-env { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px; margin-top: 10px; }
  .env-item { background: rgba(255,255,255,0.05); padding: 8px 12px; border-radius: 6px; font-size: 12px; }
  .env-item strong { color: #64b5f6; }
  footer { text-align: center; margin-top: 30px; padding: 15px; color: #666; font-size: 12px; }
</style>
</head>
<body>

<div class="header">
  <h1>🔲 QR Code Test Report - Quiz Beta Page</h1>
  <div class="subtitle">Cross-Browser & Responsive Testing | Without Login</div>
  <div class="meta">
    <span>🌐 URL: https://yuva-beta.mybharats.in/quiz</span>
    <span>📅 ${new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}</span>
    <span>⏰ ${new Date().toLocaleTimeString('en-IN')}</span>
    <span>👤 SDET Lead: Nishant Sharma</span>
  </div>
</div>

<div class="summary">
  <div class="summary-card total"><div class="value">${total}</div><div class="label">Total Tests</div></div>
  <div class="summary-card pass"><div class="value">${totalPass}</div><div class="label">Passed</div></div>
  <div class="summary-card fail"><div class="value">${totalFail}</div><div class="label">Failed</div></div>
  <div class="summary-card warn"><div class="value">${totalWarning}</div><div class="label">Warnings</div></div>
  <div class="summary-card rate"><div class="value">${passRate}%</div><div class="label">Pass Rate</div></div>
</div>

<div class="section">
  <h2>🖥️ Test Environment</h2>
  <div class="test-env">
    <div class="env-item"><strong>Platform:</strong> macOS (aarch64)</div>
    <div class="env-item"><strong>Browser Engine:</strong> Chromium (Puppeteer)</div>
    <div class="env-item"><strong>Java:</strong> 17.0.17</div>
    <div class="env-item"><strong>Node.js:</strong> v20.20.2</div>
    <div class="env-item"><strong>Target:</strong> Beta Environment</div>
    <div class="env-item"><strong>Auth:</strong> No Login Required</div>
  </div>
</div>

<div class="section">
  <h2>📊 Viewports Tested</h2>
  <table>
    <tr><th>Configuration</th><th>Resolution</th><th>Device Type</th><th>Browser</th><th>Tests Run</th><th>Pass/Fail</th></tr>
    ${viewports.map(v => {
      const vpResults = grouped[v.name] || [];
      const vpPass = vpResults.filter(r => r.status === 'PASS').length;
      const vpFail = vpResults.filter(r => r.status === 'FAIL').length;
      const deviceClass = v.device.toLowerCase();
      return `<tr>
        <td>${v.name} <span class="viewport-badge ${deviceClass}">${v.device}</span></td>
        <td>${v.width}x${v.height}</td>
        <td>${v.device}</td>
        <td>${v.browser}</td>
        <td>${vpResults.length}</td>
        <td><span class="status pass">${vpPass} Pass</span> ${vpFail > 0 ? `<span class="status fail">${vpFail} Fail</span>` : ''}</td>
      </tr>`;
    }).join('')}
  </table>
</div>

${Object.keys(grouped).map(viewport => {
  const results = grouped[viewport];
  const device = viewports.find(v => v.name === viewport);
  const deviceClass = device ? device.device.toLowerCase() : 'desktop';
  return `
<div class="section">
  <h2>${viewport} <span class="viewport-badge ${deviceClass}">${device ? device.width + 'x' + device.height : ''}</span></h2>
  <table>
    <tr><th>#</th><th>Test Case</th><th>Status</th><th>Details</th></tr>
    ${results.map((r, i) => `
    <tr>
      <td>${i + 1}</td>
      <td>${r.testCase}</td>
      <td><span class="status ${r.status.toLowerCase()}">${r.status}</span></td>
      <td>${r.details}</td>
    </tr>`).join('')}
  </table>
</div>`;
}).join('')}

<div class="section">
  <h2>📸 Screenshots Evidence</h2>
  <div class="screenshots">
    ${screenshots.map(s => `
    <div class="screenshot-card">
      <img src="screenshots/${s}" alt="${s}" loading="lazy" />
      <div class="caption">${s.replace(/_/g, ' ').replace('.png', '')}</div>
    </div>`).join('')}
    ${screenshots.length === 0 ? '<p style="color:#888;">No screenshots captured</p>' : ''}
  </div>
</div>

<div class="overall-verdict ${totalFail === 0 ? 'passed' : totalFail > totalPass ? 'failed' : 'mixed'}">
  <h2>${totalFail === 0 ? '✅ ALL TESTS PASSED' : totalFail > totalPass ? '❌ CRITICAL FAILURES DETECTED' : '⚠️ PARTIAL PASS - ISSUES FOUND'}</h2>
  <p>${totalFail === 0 ? 'QR codes on quiz cards are rendering correctly across all tested viewports and browsers.' : 
     `${totalFail} test(s) failed. Review the detailed results above for specific issues.`}</p>
</div>

<footer>
  <p>Generated by MY Bharat QA Automation Framework | SDET Lead: Nishant Sharma | ${new Date().toISOString()}</p>
</footer>

</body>
</html>`;

fs.writeFileSync(reportFile, html);
console.log(`\n✅ HTML Report generated: ${reportFile}`);
console.log(`   Screenshots: ${screenshotDir} (${screenshots.length} files)`);
