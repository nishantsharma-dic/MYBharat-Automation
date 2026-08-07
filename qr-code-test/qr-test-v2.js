/**
 * QR Code Test - Quiz Beta Page (Dashboard)
 * 
 * FINDING: QR codes are on INDIVIDUAL quiz dashboard pages (e.g., /quiz/quiz_dashboard/{id})
 * Implementation: <div class="qr-code"> containing base64 PNG img (100x100) with download link
 * 
 * Tests:
 * 1. Chrome Desktop (1920x1080)
 * 2. Chrome Tablet (768x1024)
 * 3. Chrome Mobile (375x812)
 * 4. Firefox UA Desktop (1920x1080)
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

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const testResults = [];
let totalPass = 0, totalFail = 0, totalWarning = 0;

if (!fs.existsSync(SCREENSHOT_DIR)) fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });

const viewports = [
  { name: 'Chrome_Desktop_1920x1080', width: 1920, height: 1080, device: 'Desktop', browser: 'Chrome' },
  { name: 'Chrome_Tablet_768x1024', width: 768, height: 1024, device: 'Tablet', browser: 'Chrome' },
  { name: 'Chrome_Mobile_375x812', width: 375, height: 812, device: 'Mobile', browser: 'Chrome' },
  { name: 'Firefox_Desktop_1920x1080', width: 1920, height: 1080, device: 'Desktop', browser: 'Firefox',
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:128.0) Gecko/20100101 Firefox/128.0' }
];

function addResult(viewport, testCase, status, details, screenshot = null) {
  testResults.push({ viewport, testCase, status, details, screenshot, timestamp: new Date().toISOString() });
  if (status === 'PASS') totalPass++;
  else if (status === 'FAIL') totalFail++;
  else totalWarning++;
  const icon = status === 'PASS' ? '✅' : status === 'FAIL' ? '❌' : '⚠️';
  console.log(`  ${icon} [${status}] ${testCase}: ${details}`);
}

async function testViewport(vpConfig) {
  const { name, width, height, userAgent } = vpConfig;
  console.log(`\n${'═'.repeat(70)}`);
  console.log(`  TESTING: ${name} (${width}x${height})`);
  console.log('═'.repeat(70));

  let browser, page;
  try {
    browser = await puppeteer.launch({
      headless: 'new',
      args: ['--no-sandbox', '--disable-dev-shm-usage', '--disable-gpu']
    });
    page = await browser.newPage();
    await page.setViewport({ width, height });
    if (userAgent) await page.setUserAgent(userAgent);

    // ====== QUIZ LISTING PAGE TESTS ======
    console.log('\n  📋 QUIZ LISTING PAGE TESTS');
    console.log('  ' + '-'.repeat(40));

    // TC-01: Page Load
    const startTime = Date.now();
    const response = await page.goto(QUIZ_URL, { waitUntil: 'networkidle2', timeout: 30000 });
    const loadTime = Date.now() - startTime;

    if (response.status() === 200) {
      addResult(name, 'TC-01: Quiz Listing Page Load', 'PASS', `HTTP 200, Load: ${loadTime}ms`);
    } else {
      addResult(name, 'TC-01: Quiz Listing Page Load', 'FAIL', `HTTP ${response.status()}, Load: ${loadTime}ms`);
    }
    await delay(3000);

    // TC-02: Page title
    const title = await page.title();
    if (title.includes('Quiz') && title.includes('MYBharat')) {
      addResult(name, 'TC-02: Page Title Correct', 'PASS', `"${title}"`);
    } else {
      addResult(name, 'TC-02: Page Title Correct', 'FAIL', `Unexpected: "${title}"`);
    }

    // TC-03: Quiz Cards Present
    const cardCount = await page.evaluate(() => document.querySelectorAll('.col-md-4.mb-4').length);
    if (cardCount > 0) {
      addResult(name, 'TC-03: Quiz Cards Present', 'PASS', `Found ${cardCount} quiz cards`);
    } else {
      addResult(name, 'TC-03: Quiz Cards Present', 'FAIL', 'No quiz cards found');
    }

    // TC-04: Get quiz dashboard links
    const dashboardLinks = await page.evaluate(() => {
      return Array.from(document.querySelectorAll('a[href*="quiz_dashboard"]'))
        .map(a => a.href)
        .filter((v, i, arr) => arr.indexOf(v) === i) // unique
        .slice(0, 5);
    });
    addResult(name, 'TC-04: Dashboard Links Available', dashboardLinks.length > 0 ? 'PASS' : 'FAIL',
      `${dashboardLinks.length} unique dashboard links found`);

    // TC-05: No Horizontal Scroll on Listing
    const listingOverflow = await page.evaluate(() => document.body.scrollWidth > window.innerWidth);
    addResult(name, 'TC-05: Listing Page Responsive (No H-Scroll)', !listingOverflow ? 'PASS' : 'FAIL',
      `Body width: ${await page.evaluate(() => document.body.scrollWidth)}px vs Viewport: ${width}px`);

    // Screenshot - listing page
    const listingScreenshot = `${name}_listing_page.png`;
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, listingScreenshot), fullPage: false });

    // ====== QUIZ DASHBOARD PAGE TESTS (QR CODE) ======
    if (dashboardLinks.length > 0) {
      console.log('\n  🔲 QR CODE TESTS (Dashboard Page)');
      console.log('  ' + '-'.repeat(40));

      // Navigate to first quiz dashboard
      const dashURL = dashboardLinks[0];
      const dashStart = Date.now();
      const dashResponse = await page.goto(dashURL, { waitUntil: 'networkidle2', timeout: 30000 });
      const dashLoadTime = Date.now() - dashStart;
      await delay(5000); // Wait for QR generation via AJAX

      // TC-06: Dashboard Page Load
      if (dashResponse.status() === 200) {
        addResult(name, 'TC-06: Dashboard Page Load', 'PASS', `HTTP 200, Load: ${dashLoadTime}ms`);
      } else {
        addResult(name, 'TC-06: Dashboard Page Load', 'FAIL', `HTTP ${dashResponse.status()}`);
      }

      // TC-07: QR Code Container Present
      const qrContainerData = await page.evaluate(() => {
        const qrDiv = document.querySelector('.qr-code');
        if (!qrDiv) return null;
        return {
          displayed: qrDiv.offsetWidth > 0 && qrDiv.offsetHeight > 0,
          style: qrDiv.getAttribute('style'),
          dataQrUrl: qrDiv.getAttribute('data-qr-url'),
          width: qrDiv.offsetWidth,
          height: qrDiv.offsetHeight
        };
      });

      if (qrContainerData) {
        addResult(name, 'TC-07: QR Container (.qr-code) Present', 'PASS',
          `Displayed: ${qrContainerData.displayed}, Size: ${qrContainerData.width}x${qrContainerData.height}px`);
      } else {
        addResult(name, 'TC-07: QR Container (.qr-code) Present', 'FAIL', 'div.qr-code NOT found on dashboard page');
      }

      // TC-08: QR Code Image Present & Loaded
      const qrImageData = await page.evaluate(() => {
        const qrDiv = document.querySelector('.qr-code');
        if (!qrDiv) return null;
        const img = qrDiv.querySelector('img');
        if (!img) return { found: false };
        return {
          found: true,
          src: img.src.substring(0, 100),
          alt: img.alt,
          naturalWidth: img.naturalWidth,
          naturalHeight: img.naturalHeight,
          displayWidth: img.offsetWidth,
          displayHeight: img.offsetHeight,
          isBase64: img.src.startsWith('data:image'),
          isLoaded: img.complete && img.naturalWidth > 0
        };
      });

      if (qrImageData && qrImageData.found && qrImageData.isLoaded) {
        addResult(name, 'TC-08: QR Image Rendered', 'PASS',
          `${qrImageData.naturalWidth}x${qrImageData.naturalHeight}px, Display: ${qrImageData.displayWidth}x${qrImageData.displayHeight}px, Base64: ${qrImageData.isBase64}`);
      } else if (qrImageData && qrImageData.found && !qrImageData.isLoaded) {
        addResult(name, 'TC-08: QR Image Rendered', 'FAIL', 'QR img element found but NOT loaded/rendered');
      } else {
        addResult(name, 'TC-08: QR Image Rendered', 'FAIL', 'QR img element not found inside .qr-code');
      }

      // TC-09: QR Code Alt Text (Accessibility)
      if (qrImageData && qrImageData.alt) {
        addResult(name, 'TC-09: QR Image Accessibility (alt)', 'PASS', `alt="${qrImageData.alt}"`);
      } else {
        addResult(name, 'TC-09: QR Image Accessibility (alt)', 'FAIL', 'No alt text on QR image');
      }

      // TC-10: QR Code Visibility Check
      if (qrContainerData && qrContainerData.displayed) {
        addResult(name, 'TC-10: QR Code Visible to User', 'PASS',
          `Container visible at ${qrContainerData.width}x${qrContainerData.height}px`);
      } else if (qrContainerData && !qrContainerData.displayed) {
        addResult(name, 'TC-10: QR Code Visible to User', 'FAIL', 'QR container is HIDDEN (display:none or zero size)');
      } else {
        addResult(name, 'TC-10: QR Code Visible to User', 'FAIL', 'QR container not found');
      }

      // TC-11: QR Download Link
      const downloadLink = await page.evaluate(() => {
        const qrDiv = document.querySelector('.qr-code');
        if (!qrDiv) return null;
        const a = qrDiv.querySelector('a');
        if (!a) return null;
        return {
          href: a.href.substring(0, 80),
          text: a.textContent.trim(),
          hasDownloadData: a.href.startsWith('data:image') || a.href.includes('download')
        };
      });

      if (downloadLink && downloadLink.hasDownloadData) {
        addResult(name, 'TC-11: QR Download Link Functional', 'PASS',
          `Link text: "${downloadLink.text}", Has image data: ${downloadLink.hasDownloadData}`);
      } else if (downloadLink) {
        addResult(name, 'TC-11: QR Download Link Functional', 'WARNING',
          `Link found but href may not work: ${downloadLink.href}`);
      } else {
        addResult(name, 'TC-11: QR Download Link Functional', 'FAIL', 'No download link inside QR container');
      }

      // TC-12: QR Code Size Responsiveness
      const qrSizeCheck = await page.evaluate((viewportWidth) => {
        const qrDiv = document.querySelector('.qr-code');
        if (!qrDiv) return null;
        const img = qrDiv.querySelector('img');
        if (!img) return null;
        const rect = img.getBoundingClientRect();
        return {
          imgWidth: rect.width,
          imgHeight: rect.height,
          isSquare: Math.abs(rect.width - rect.height) < 5,
          withinViewport: rect.left >= 0 && rect.right <= viewportWidth,
          isMobileSize: viewportWidth <= 768 ? rect.width <= 80 : rect.width >= 80,
          isReasonableSize: rect.width >= 30 && rect.width <= 200
        };
      }, width);

      if (qrSizeCheck) {
        const sizePass = qrSizeCheck.isSquare && qrSizeCheck.withinViewport && qrSizeCheck.isReasonableSize;
        addResult(name, 'TC-12: QR Responsive Size', sizePass ? 'PASS' : 'WARNING',
          `Size: ${Math.round(qrSizeCheck.imgWidth)}x${Math.round(qrSizeCheck.imgHeight)}px, Square: ${qrSizeCheck.isSquare}, In viewport: ${qrSizeCheck.withinViewport}`);
      } else {
        addResult(name, 'TC-12: QR Responsive Size', 'FAIL', 'Cannot measure QR size');
      }

      // TC-13: Dashboard Page No Horizontal Overflow
      const dashOverflow = await page.evaluate(() => document.body.scrollWidth > window.innerWidth);
      addResult(name, 'TC-13: Dashboard Responsive (No H-Scroll)', !dashOverflow ? 'PASS' : 'FAIL',
        `Body: ${await page.evaluate(() => document.body.scrollWidth)}px vs Viewport: ${width}px`);

      // TC-14: "Click to Download QR" text present
      const downloadText = await page.evaluate(() => {
        const qrDiv = document.querySelector('.qr-code');
        if (!qrDiv) return null;
        const span = qrDiv.querySelector('span');
        return span ? span.textContent.trim() : null;
      });
      if (downloadText && downloadText.toLowerCase().includes('download')) {
        addResult(name, 'TC-14: Download CTA Text', 'PASS', `"${downloadText}"`);
      } else {
        addResult(name, 'TC-14: Download CTA Text', downloadText ? 'WARNING' : 'FAIL',
          downloadText ? `Text: "${downloadText}"` : 'No span with download text');
      }

      // Screenshot - dashboard with QR
      const dashScreenshot = `${name}_dashboard_qr.png`;
      await page.screenshot({ path: path.join(SCREENSHOT_DIR, dashScreenshot), fullPage: false });
      addResult(name, 'TC-15: Dashboard Screenshot', 'PASS', `Captured: ${dashScreenshot}`, dashScreenshot);

      // TC-16: Test multiple dashboard pages for QR consistency
      if (dashboardLinks.length > 1) {
        const secondDash = dashboardLinks[1];
        await page.goto(secondDash, { waitUntil: 'networkidle2', timeout: 30000 });
        await delay(5000);
        
        const secondQR = await page.evaluate(() => {
          const qrDiv = document.querySelector('.qr-code');
          if (!qrDiv) return { found: false };
          const img = qrDiv.querySelector('img');
          return { found: true, imgLoaded: img ? (img.complete && img.naturalWidth > 0) : false };
        });

        if (secondQR.found && secondQR.imgLoaded) {
          addResult(name, 'TC-16: QR on 2nd Quiz Dashboard', 'PASS', 'QR code also present and loaded on another quiz');
        } else if (secondQR.found) {
          addResult(name, 'TC-16: QR on 2nd Quiz Dashboard', 'WARNING', 'Container found but image not loaded');
        } else {
          addResult(name, 'TC-16: QR on 2nd Quiz Dashboard', 'FAIL', 'QR code not found on second quiz dashboard');
        }
      }

    } else {
      addResult(name, 'TC-06 to TC-16: QR Tests', 'FAIL', 'No dashboard links found - cannot test QR codes');
    }

  } catch (error) {
    addResult(name, 'EXECUTION ERROR', 'FAIL', error.message);
  } finally {
    if (browser) await browser.close();
  }
}

async function runAllTests() {
  console.log('╔══════════════════════════════════════════════════════════════════════╗');
  console.log('║  QR CODE TEST SUITE - MY Bharat Quiz Beta Page                      ║');
  console.log('║  URL: https://yuva-beta.mybharats.in/quiz                           ║');
  console.log('║  Scope: Cross-browser, Cross-device, Responsiveness                 ║');
  console.log('║  Auth: No Login Required (Public Page)                              ║');
  console.log('║  Date: ' + new Date().toLocaleString('en-IN') + '                                       ║');
  console.log('╚══════════════════════════════════════════════════════════════════════╝');

  for (const vp of viewports) {
    await testViewport(vp);
  }

  console.log('\n\n' + '═'.repeat(70));
  console.log('  TEST EXECUTION SUMMARY');
  console.log('═'.repeat(70));
  console.log(`  Total: ${testResults.length} | ✅ PASS: ${totalPass} | ❌ FAIL: ${totalFail} | ⚠️ WARNING: ${totalWarning}`);
  console.log(`  Pass Rate: ${((totalPass / testResults.length) * 100).toFixed(1)}%`);
  console.log('═'.repeat(70));

  // Save data for report
  fs.writeFileSync(path.join(__dirname, 'test-data.json'), JSON.stringify({
    testResults, totalPass, totalFail, totalWarning, viewports,
    executionDate: new Date().toISOString(),
    url: QUIZ_URL
  }, null, 2));
}

runAllTests().then(() => {
  console.log('\n📊 Generating HTML Report...');
  require('./generate-report-v2.js');
}).catch(err => {
  console.error('Fatal:', err);
  process.exit(1);
});
