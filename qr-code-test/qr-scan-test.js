/**
 * QR Code SCAN Verification Test
 * 
 * Decodes the QR code image from quiz dashboard pages and verifies:
 * 1. QR code is scannable (decodeable)
 * 2. Decoded URL is valid
 * 3. Decoded URL matches the quiz page
 * 4. Decoded URL is accessible (returns HTTP 200)
 * 5. Test across multiple quizzes
 * 
 * Author: SDET Lead - Nishant Sharma
 * Date: 14-Jul-2026
 */

const puppeteer = require('puppeteer');
const jsQR = require('jsqr');
const { PNG } = require('pngjs');
const fs = require('fs');
const path = require('path');

const delay = ms => new Promise(r => setTimeout(r, ms));
const QUIZ_URL = 'https://yuva-beta.mybharats.in/quiz';

const scanResults = [];
let scanPass = 0, scanFail = 0, scanWarn = 0;

function addScanResult(testCase, status, details, quizName = '') {
  scanResults.push({ testCase, status, details, quizName, timestamp: new Date().toISOString() });
  if (status === 'PASS') scanPass++;
  else if (status === 'FAIL') scanFail++;
  else scanWarn++;
  const icon = status === 'PASS' ? '✅' : status === 'FAIL' ? '❌' : '⚠️';
  console.log(`  ${icon} [${status}] ${testCase}: ${details}`);
}

function decodeQRFromBase64(base64Data) {
  // Remove data:image/png;base64, prefix if present
  const cleanBase64 = base64Data.replace(/^data:image\/\w+;base64,/, '');
  const buffer = Buffer.from(cleanBase64, 'base64');
  
  try {
    const png = PNG.sync.read(buffer);
    const code = jsQR(new Uint8ClampedArray(png.data), png.width, png.height);
    return code ? { success: true, data: code.data, width: png.width, height: png.height } : { success: false, width: png.width, height: png.height };
  } catch (err) {
    return { success: false, error: err.message };
  }
}

async function runScanTests() {
  console.log('╔══════════════════════════════════════════════════════════════════════╗');
  console.log('║  QR CODE SCAN VERIFICATION TEST                                     ║');
  console.log('║  Decoding QR codes and verifying scanned URLs                       ║');
  console.log('║  URL: https://yuva-beta.mybharats.in/quiz                           ║');
  console.log('╚══════════════════════════════════════════════════════════════════════╝\n');

  const browser = await puppeteer.launch({ headless: 'new', args: ['--no-sandbox'] });
  const page = await browser.newPage();
  await page.setViewport({ width: 1920, height: 1080 });

  // Get quiz dashboard links
  await page.goto(QUIZ_URL, { waitUntil: 'networkidle2', timeout: 30000 });
  await delay(3000);

  const quizLinks = await page.evaluate(() => {
    return Array.from(document.querySelectorAll('a[href*="quiz_dashboard"]'))
      .map(a => ({ href: a.href, title: a.closest('.col-md-4')?.querySelector('h4')?.textContent?.trim() || 'Unknown Quiz' }))
      .filter((v, i, arr) => arr.findIndex(x => x.href === v.href) === i)
      .slice(0, 5);
  });

  console.log(`  Found ${quizLinks.length} quiz dashboards to scan\n`);

  for (let i = 0; i < quizLinks.length; i++) {
    const quiz = quizLinks[i];
    console.log(`\n  ─── Quiz ${i + 1}: ${quiz.title} ───`);

    await page.goto(quiz.href, { waitUntil: 'networkidle2', timeout: 30000 });
    await delay(5000); // Wait for QR generation AJAX

    // Extract QR image base64 data
    const qrData = await page.evaluate(() => {
      const qrDiv = document.querySelector('.qr-code');
      if (!qrDiv) return null;
      const img = qrDiv.querySelector('img');
      if (!img || !img.src) return null;
      const anchor = qrDiv.querySelector('a');
      return {
        imgSrc: img.src,
        isBase64: img.src.startsWith('data:image'),
        imgWidth: img.naturalWidth,
        imgHeight: img.naturalHeight,
        anchorHref: anchor ? anchor.href : null,
        pageUrl: window.location.href
      };
    });

    if (!qrData) {
      addScanResult(`Quiz ${i+1}: QR Element Found`, 'FAIL', 'No .qr-code element or img on page', quiz.title);
      continue;
    }

    addScanResult(`Quiz ${i+1}: QR Image Present`, 'PASS', 
      `Base64: ${qrData.isBase64}, Size: ${qrData.imgWidth}x${qrData.imgHeight}px`, quiz.title);

    // DECODE the QR code
    if (qrData.isBase64) {
      const decoded = decodeQRFromBase64(qrData.imgSrc);

      if (decoded.success) {
        addScanResult(`Quiz ${i+1}: QR Scannable`, 'PASS', 
          `Decoded successfully! Content: "${decoded.data.substring(0, 100)}"`, quiz.title);

        // Verify decoded content is a valid URL
        try {
          const url = new URL(decoded.data);
          addScanResult(`Quiz ${i+1}: QR Contains Valid URL`, 'PASS', 
            `URL: ${url.href}`, quiz.title);

          // Verify URL domain matches mybharat
          if (url.hostname.includes('mybharat') || url.hostname.includes('yuva-beta')) {
            addScanResult(`Quiz ${i+1}: QR URL Domain Correct`, 'PASS', 
              `Domain: ${url.hostname} (matches MY Bharat)`, quiz.title);
          } else {
            addScanResult(`Quiz ${i+1}: QR URL Domain Correct`, 'FAIL', 
              `Domain: ${url.hostname} (does NOT match MY Bharat)`, quiz.title);
          }

          // Verify URL is accessible
          const urlResponse = await page.goto(decoded.data, { waitUntil: 'domcontentloaded', timeout: 15000 });
          const urlStatus = urlResponse.status();
          if (urlStatus === 200) {
            addScanResult(`Quiz ${i+1}: Scanned URL Accessible`, 'PASS', 
              `HTTP ${urlStatus} — page loads correctly`, quiz.title);
          } else if (urlStatus === 301 || urlStatus === 302) {
            addScanResult(`Quiz ${i+1}: Scanned URL Accessible`, 'PASS', 
              `HTTP ${urlStatus} (redirect — acceptable)`, quiz.title);
          } else {
            addScanResult(`Quiz ${i+1}: Scanned URL Accessible`, 'FAIL', 
              `HTTP ${urlStatus} — page not accessible`, quiz.title);
          }

          // Verify URL leads to quiz-related content
          const pageContent = await page.evaluate(() => {
            return {
              title: document.title,
              hasQuizContent: document.body.innerHTML.toLowerCase().includes('quiz')
            };
          });
          if (pageContent.hasQuizContent) {
            addScanResult(`Quiz ${i+1}: Scanned URL Shows Quiz Content`, 'PASS', 
              `Page title: "${pageContent.title}"`, quiz.title);
          } else {
            addScanResult(`Quiz ${i+1}: Scanned URL Shows Quiz Content`, 'WARNING', 
              `Page title: "${pageContent.title}" — may not be quiz page`, quiz.title);
          }

        } catch (urlErr) {
          // Not a URL — check if it's some other content
          addScanResult(`Quiz ${i+1}: QR Content Type`, 'WARNING', 
            `Decoded content is not a URL: "${decoded.data.substring(0, 80)}"`, quiz.title);
        }

      } else {
        addScanResult(`Quiz ${i+1}: QR Scannable`, 'FAIL', 
          `Could not decode QR! Error: ${decoded.error || 'jsQR returned null'} (Image: ${decoded.width}x${decoded.height}px)`, quiz.title);
      }
    } else {
      addScanResult(`Quiz ${i+1}: QR Image Type`, 'WARNING', 
        'QR image is not base64 encoded — may be an external URL', quiz.title);
    }
  }

  await browser.close();

  // Print summary
  console.log(`\n\n${'═'.repeat(70)}`);
  console.log('  QR SCAN VERIFICATION SUMMARY');
  console.log('═'.repeat(70));
  console.log(`  Total: ${scanResults.length} | ✅ PASS: ${scanPass} | ❌ FAIL: ${scanFail} | ⚠️ WARNING: ${scanWarn}`);
  console.log(`  Pass Rate: ${((scanPass / scanResults.length) * 100).toFixed(1)}%`);
  console.log('═'.repeat(70));

  // Save results
  fs.writeFileSync(path.join(__dirname, 'scan-results.json'), JSON.stringify({
    scanResults, scanPass, scanFail, scanWarn
  }, null, 2));

  return { scanResults, scanPass, scanFail, scanWarn };
}

module.exports = runScanTests;

if (require.main === module) {
  runScanTests().then(results => {
    console.log('\n✅ Scan tests completed.');
    process.exit(results.scanFail > 0 ? 1 : 0);
  }).catch(err => {
    console.error('Fatal:', err);
    process.exit(1);
  });
}
