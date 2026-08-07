/**
 * QR Code Test - Quiz Beta Page
 * URL: https://yuva-beta.mybharats.in/quiz
 * 
 * Tests QR codes on quiz cards across:
 * - Chrome Desktop (1920x1080)
 * - Chrome Tablet (768x1024)
 * - Chrome Mobile (375x812)
 * - Firefox Desktop (1920x1080) [via Chromium with mobile UA as proxy]
 * 
 * No login required - public page testing
 * Author: SDET Lead - Nishant Sharma
 * Date: 14-Jul-2026
 */

const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

const QUIZ_URL = 'https://yuva-beta.mybharats.in/quiz';
const SCREENSHOT_DIR = path.join(__dirname, 'screenshots');
const REPORT_FILE = path.join(__dirname, 'QR_Code_Test_Report.html');

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// Test results collector
const testResults = [];
let totalPass = 0, totalFail = 0, totalWarning = 0;

// Ensure screenshot directory
if (!fs.existsSync(SCREENSHOT_DIR)) fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });

// Viewports to test
const viewports = [
  { name: 'Chrome Desktop', width: 1920, height: 1080, device: 'Desktop', browser: 'Chrome' },
  { name: 'Chrome Tablet (iPad)', width: 768, height: 1024, device: 'Tablet', browser: 'Chrome' },
  { name: 'Chrome Mobile (iPhone 13)', width: 375, height: 812, device: 'Mobile', browser: 'Chrome' },
  { name: 'Firefox Desktop (simulated)', width: 1920, height: 1080, device: 'Desktop', browser: 'Firefox',
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:128.0) Gecko/20100101 Firefox/128.0' }
];

function addResult(viewport, testCase, status, details, screenshot = null) {
  const result = { viewport, testCase, status, details, screenshot, timestamp: new Date().toISOString() };
  testResults.push(result);
  if (status === 'PASS') totalPass++;
  else if (status === 'FAIL') totalFail++;
  else totalWarning++;
  console.log(`  [${status}] ${testCase}: ${details}`);
}

async function runTestForViewport(viewportConfig) {
  const { name, width, height, userAgent } = viewportConfig;
  console.log(`\n${'='.repeat(60)}`);
  console.log(`Testing: ${name} (${width}x${height})`);
  console.log('='.repeat(60));

  let browser, page;
  try {
    browser = await puppeteer.launch({
      headless: 'new',
      args: ['--no-sandbox', '--disable-dev-shm-usage', '--disable-gpu', `--window-size=${width},${height}`]
    });
    page = await browser.newPage();
    await page.setViewport({ width, height });
    if (userAgent) await page.setUserAgent(userAgent);

    // TC-01: Page Load
    console.log('\n  --- TC-01: Page Load ---');
    const startTime = Date.now();
    const response = await page.goto(QUIZ_URL, { waitUntil: 'networkidle2', timeout: 30000 });
    const loadTime = Date.now() - startTime;
    const statusCode = response.status();

    if (statusCode === 200) {
      addResult(name, 'TC-01: Page Load', 'PASS', `Status ${statusCode}, Load time: ${loadTime}ms`);
    } else {
      addResult(name, 'TC-01: Page Load', 'FAIL', `Status ${statusCode}, Load time: ${loadTime}ms`);
    }

    // Wait for content to render
    await delay(3000);

    // Take full page screenshot
    const screenshotName = `${name.replace(/[^a-zA-Z0-9]/g, '_')}_full_page.png`;
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, screenshotName), fullPage: true });
    addResult(name, 'TC-02: Full Page Screenshot', 'PASS', `Captured: ${screenshotName}`, screenshotName);

    // TC-03: Find Quiz Cards
    console.log('\n  --- TC-03: Quiz Cards Detection ---');
    const cardSelectors = [
      '[class*="card"]', '[class*="quiz"]', '[class*="Card"]', '[class*="Quiz"]',
      'div[class*="swiper"]', '[class*="slide"]', '[class*="item"]'
    ];

    let cards = [];
    for (const selector of cardSelectors) {
      const found = await page.$$(selector);
      if (found.length > 0) {
        cards = found;
        addResult(name, 'TC-03: Quiz Cards Found', 'PASS', `Found ${found.length} elements with selector: ${selector}`);
        break;
      }
    }
    if (cards.length === 0) {
      // Try broader search
      const allDivs = await page.evaluate(() => {
        const elements = document.querySelectorAll('div');
        const cardLike = [];
        elements.forEach(el => {
          const cls = el.className || '';
          if (cls.toLowerCase().includes('card') || cls.toLowerCase().includes('quiz') || cls.toLowerCase().includes('slide')) {
            cardLike.push({ tag: el.tagName, class: cls, childCount: el.children.length });
          }
        });
        return cardLike.slice(0, 20);
      });
      addResult(name, 'TC-03: Quiz Cards Found', 'WARNING', `Card-like elements: ${JSON.stringify(allDivs).substring(0, 200)}`);
    }

    // TC-04: QR Code Detection
    console.log('\n  --- TC-04: QR Code Detection ---');
    const qrData = await page.evaluate(() => {
      const results = { images: [], svgs: [], canvases: [], qrContainers: [] };

      // Check for QR code images (img tags with qr in src/alt/class)
      document.querySelectorAll('img').forEach(img => {
        const src = img.src || '';
        const alt = img.alt || '';
        const cls = img.className || '';
        if (src.toLowerCase().includes('qr') || alt.toLowerCase().includes('qr') || cls.toLowerCase().includes('qr')) {
          results.images.push({
            src: src.substring(0, 150), alt, class: cls,
            width: img.naturalWidth, height: img.naturalHeight,
            displayed: img.offsetWidth > 0 && img.offsetHeight > 0,
            displayWidth: img.offsetWidth, displayHeight: img.offsetHeight
          });
        }
      });

      // Check for SVG-based QR codes
      document.querySelectorAll('svg').forEach(svg => {
        const cls = svg.className?.baseVal || '';
        const parent = svg.parentElement;
        const parentCls = parent ? (parent.className || '') : '';
        if (cls.toLowerCase().includes('qr') || parentCls.toLowerCase().includes('qr')) {
          results.svgs.push({
            class: cls, parentClass: parentCls,
            width: svg.getAttribute('width'), height: svg.getAttribute('height'),
            viewBox: svg.getAttribute('viewBox')
          });
        }
      });

      // Check for canvas-based QR codes
      document.querySelectorAll('canvas').forEach(canvas => {
        const cls = canvas.className || '';
        const parent = canvas.parentElement;
        const parentCls = parent ? (parent.className || '') : '';
        if (cls.toLowerCase().includes('qr') || parentCls.toLowerCase().includes('qr') || 
            canvas.width > 50 && canvas.width < 500) {
          results.canvases.push({
            class: cls, parentClass: parentCls,
            width: canvas.width, height: canvas.height
          });
        }
      });

      // Check for containers with 'qr' in class/id
      document.querySelectorAll('[class*="qr"], [class*="QR"], [class*="Qr"], [id*="qr"], [id*="QR"]').forEach(el => {
        results.qrContainers.push({
          tag: el.tagName, class: el.className || '', id: el.id || '',
          childCount: el.children.length, innerHTML: el.innerHTML.substring(0, 200),
          displayed: el.offsetWidth > 0 && el.offsetHeight > 0,
          width: el.offsetWidth, height: el.offsetHeight
        });
      });

      // Also check all images on the page for any QR-like images
      const allImages = [];
      document.querySelectorAll('img').forEach(img => {
        if (img.naturalWidth > 0) {
          allImages.push({ src: img.src.substring(0, 100), w: img.naturalWidth, h: img.naturalHeight, displayed: img.offsetWidth > 0 });
        }
      });
      results.allImages = allImages;

      return results;
    });

    // Report QR findings
    const totalQR = qrData.images.length + qrData.svgs.length + qrData.canvases.length + qrData.qrContainers.length;
    if (totalQR > 0) {
      addResult(name, 'TC-04: QR Codes Detected', 'PASS',
        `Found ${qrData.images.length} QR images, ${qrData.svgs.length} SVG QRs, ${qrData.canvases.length} Canvas QRs, ${qrData.qrContainers.length} QR containers`);
    } else {
      addResult(name, 'TC-04: QR Codes Detected', 'FAIL',
        `No QR codes found. Total images on page: ${qrData.allImages.length}. Images: ${JSON.stringify(qrData.allImages).substring(0, 300)}`);
    }

    // TC-05: QR Code Visibility & Size Check
    console.log('\n  --- TC-05: QR Code Visibility ---');
    if (qrData.images.length > 0) {
      let allVisible = true;
      qrData.images.forEach((img, i) => {
        if (img.displayed && img.displayWidth >= 30 && img.displayHeight >= 30) {
          addResult(name, `TC-05: QR Image #${i + 1} Visibility`, 'PASS',
            `Displayed at ${img.displayWidth}x${img.displayHeight}px, natural: ${img.width}x${img.height}`);
        } else if (!img.displayed) {
          allVisible = false;
          addResult(name, `TC-05: QR Image #${i + 1} Visibility`, 'FAIL', `QR image NOT displayed (hidden/zero-size)`);
        } else {
          allVisible = false;
          addResult(name, `TC-05: QR Image #${i + 1} Visibility`, 'WARNING',
            `QR too small: ${img.displayWidth}x${img.displayHeight}px`);
        }
      });
    } else if (qrData.qrContainers.length > 0) {
      qrData.qrContainers.forEach((container, i) => {
        if (container.displayed && container.width >= 30) {
          addResult(name, `TC-05: QR Container #${i + 1} Visibility`, 'PASS',
            `Displayed at ${container.width}x${container.height}px, tag: ${container.tag}`);
        } else {
          addResult(name, `TC-05: QR Container #${i + 1} Visibility`, 'FAIL',
            `Not visible. Tag: ${container.tag}, class: ${container.class}`);
        }
      });
    } else {
      addResult(name, 'TC-05: QR Visibility', 'WARNING', 'No QR elements to check visibility');
    }

    // TC-06: Responsiveness Check
    console.log('\n  --- TC-06: Responsiveness ---');
    const layoutData = await page.evaluate(() => {
      const body = document.body;
      const html = document.documentElement;
      const hasHorizontalScroll = body.scrollWidth > window.innerWidth;
      const overflowElements = [];
      document.querySelectorAll('*').forEach(el => {
        const rect = el.getBoundingClientRect();
        if (rect.right > window.innerWidth + 5 && el.tagName !== 'HTML' && el.tagName !== 'BODY') {
          overflowElements.push({ tag: el.tagName, class: (el.className || '').substring(0, 50), right: Math.round(rect.right) });
        }
      });
      return {
        viewportWidth: window.innerWidth,
        viewportHeight: window.innerHeight,
        bodyScrollWidth: body.scrollWidth,
        hasHorizontalScroll,
        overflowCount: overflowElements.length,
        overflowSamples: overflowElements.slice(0, 5)
      };
    });

    if (!layoutData.hasHorizontalScroll) {
      addResult(name, 'TC-06: No Horizontal Overflow', 'PASS',
        `Viewport: ${layoutData.viewportWidth}px, Body scroll width: ${layoutData.bodyScrollWidth}px`);
    } else {
      addResult(name, 'TC-06: No Horizontal Overflow', 'FAIL',
        `Horizontal scroll detected! Body: ${layoutData.bodyScrollWidth}px > Viewport: ${layoutData.viewportWidth}px. Overflow elements: ${layoutData.overflowCount}`);
    }

    // TC-07: QR Code Responsiveness (size relative to container)
    console.log('\n  --- TC-07: QR Responsive Sizing ---');
    const qrSizing = await page.evaluate(() => {
      const qrElements = document.querySelectorAll('[class*="qr"], [class*="QR"], [class*="Qr"], img[src*="qr"], img[alt*="qr"]');
      const results = [];
      qrElements.forEach(el => {
        const rect = el.getBoundingClientRect();
        const parent = el.parentElement;
        const parentRect = parent ? parent.getBoundingClientRect() : null;
        results.push({
          tag: el.tagName,
          width: Math.round(rect.width),
          height: Math.round(rect.height),
          parentWidth: parentRect ? Math.round(parentRect.width) : 0,
          isWithinViewport: rect.left >= 0 && rect.right <= window.innerWidth,
          ratioToParent: parentRect ? Math.round((rect.width / parentRect.width) * 100) : 0
        });
      });
      return results;
    });

    if (qrSizing.length > 0) {
      qrSizing.forEach((qr, i) => {
        if (qr.isWithinViewport && qr.width > 0) {
          addResult(name, `TC-07: QR #${i + 1} Responsive`, 'PASS',
            `Size: ${qr.width}x${qr.height}px, ${qr.ratioToParent}% of parent (${qr.parentWidth}px), within viewport`);
        } else if (!qr.isWithinViewport) {
          addResult(name, `TC-07: QR #${i + 1} Responsive`, 'FAIL', `QR code overflows viewport!`);
        }
      });
    } else {
      addResult(name, 'TC-07: QR Responsive Sizing', 'WARNING', 'No QR elements found for responsive check');
    }

    // TC-08: Page title and meta verification
    console.log('\n  --- TC-08: Page Meta ---');
    const pageTitle = await page.title();
    addResult(name, 'TC-08: Page Title', pageTitle ? 'PASS' : 'WARNING', `Title: "${pageTitle}"`);

    // TC-09: Console errors check
    const consoleErrors = [];
    page.on('console', msg => { if (msg.type() === 'error') consoleErrors.push(msg.text()); });
    await page.reload({ waitUntil: 'networkidle2' });
    await delay(2000);

    if (consoleErrors.length === 0) {
      addResult(name, 'TC-09: No Console Errors', 'PASS', 'No JavaScript errors detected');
    } else {
      addResult(name, 'TC-09: No Console Errors', 'WARNING', `${consoleErrors.length} errors: ${consoleErrors.slice(0, 3).join('; ').substring(0, 200)}`);
    }

    // Take viewport-specific screenshot
    const vpScreenshot = `${name.replace(/[^a-zA-Z0-9]/g, '_')}_viewport.png`;
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, vpScreenshot) });

  } catch (error) {
    addResult(name, 'TEST EXECUTION', 'FAIL', `Error: ${error.message}`);
    console.error(`  ERROR: ${error.message}`);
  } finally {
    if (browser) await browser.close();
  }
}

async function runAllTests() {
  console.log('╔══════════════════════════════════════════════════════════╗');
  console.log('║  QR CODE TEST - Quiz Beta Page                          ║');
  console.log('║  URL: https://yuva-beta.mybharats.in/quiz               ║');
  console.log('║  Date: ' + new Date().toLocaleString() + '                    ║');
  console.log('╚══════════════════════════════════════════════════════════╝\n');

  for (const viewport of viewports) {
    await runTestForViewport(viewport);
  }

  // Print summary
  console.log('\n\n' + '='.repeat(60));
  console.log('TEST EXECUTION SUMMARY');
  console.log('='.repeat(60));
  console.log(`Total Tests: ${testResults.length}`);
  console.log(`PASS: ${totalPass} | FAIL: ${totalFail} | WARNING: ${totalWarning}`);
  console.log('='.repeat(60));

  // Generate HTML report
  generateReport();
}

function generateReport() {
  // Delegate to report generator
  const reportData = { testResults, totalPass, totalFail, totalWarning, viewports };
  fs.writeFileSync(path.join(__dirname, 'test-data.json'), JSON.stringify(reportData, null, 2));
  console.log('\nTest data saved. Generating HTML report...');
}

runAllTests().then(() => {
  console.log('\n✅ All tests completed. Generating report...');
  require('./generate-report.js');
}).catch(err => {
  console.error('Fatal error:', err);
  process.exit(1);
});
