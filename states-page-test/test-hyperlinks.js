const { chromium } = require('playwright');

async function testAllLinks() {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1920, height: 1080 } });
  const page = await context.newPage();

  await page.goto('https://mybharat.gov.in/states', { waitUntil: 'networkidle', timeout: 60000 });
  await page.waitForTimeout(5000);

  console.log('═══════════════════════════════════════════════════════════════════════════════');
  console.log('  COMPREHENSIVE HYPERLINK TEST REPORT');
  console.log('═══════════════════════════════════════════════════════════════════════════════\n');

  // 1. Extract ALL clickable elements (a, button with links, onclick)
  const allLinks = await page.evaluate(() => {
    const links = [];

    // All <a> tags
    document.querySelectorAll('a[href]').forEach(a => {
      links.push({
        type: 'anchor',
        href: a.href,
        text: a.textContent.trim().substring(0, 80),
        target: a.target || '_self',
        ariaLabel: a.getAttribute('aria-label') || '',
        isVisible: a.offsetParent !== null
      });
    });

    // All buttons/elements with onclick or routing
    document.querySelectorAll('button, [role="button"], [role="link"]').forEach(btn => {
      const text = btn.textContent.trim().substring(0, 80);
      if (text && !links.some(l => l.text === text)) {
        links.push({
          type: 'button',
          href: btn.getAttribute('data-href') || '',
          text: text,
          target: '_self',
          ariaLabel: btn.getAttribute('aria-label') || '',
          isVisible: btn.offsetParent !== null
        });
      }
    });

    return links;
  });

  console.log(`Total clickable elements found: ${allLinks.length}`);
  console.log(`  Anchors: ${allLinks.filter(l => l.type === 'anchor').length}`);
  console.log(`  Buttons: ${allLinks.filter(l => l.type === 'button').length}\n`);

  // 2. Categorize all links
  const httpLinks = allLinks.filter(l => l.href.startsWith('http'));
  const telLinks = allLinks.filter(l => l.href.startsWith('tel:'));
  const mailLinks = allLinks.filter(l => l.href.startsWith('mailto:'));
  const jsLinks = allLinks.filter(l => l.href.startsWith('javascript:'));

  console.log('─── LINK CATEGORIES ───');
  console.log(`  HTTP/HTTPS links: ${httpLinks.length}`);
  console.log(`  Tel links: ${telLinks.length}`);
  console.log(`  Mailto links: ${mailLinks.length}`);
  console.log(`  JavaScript links: ${jsLinks.length}`);

  // 3. Test all HTTP links
  console.log('\n─── HTTP LINK TESTING ───\n');
  const uniqueUrls = [...new Set(httpLinks.map(l => l.href))];
  console.log(`Unique URLs to test: ${uniqueUrls.length}\n`);

  const results = { working: [], broken: [], redirected: [], slow: [] };

  for (let i = 0; i < uniqueUrls.length; i++) {
    const url = uniqueUrls[i];
    const linkInfo = httpLinks.find(l => l.href === url);
    const startTime = Date.now();

    try {
      const testPage = await context.newPage();
      const resp = await testPage.goto(url, { timeout: 20000, waitUntil: 'domcontentloaded' });
      const elapsed = Date.now() - startTime;
      const status = resp ? resp.status() : 0;
      const finalUrl = testPage.url();
      await testPage.close();

      const entry = { url, status, elapsed, text: linkInfo?.text || '', finalUrl };

      if (status >= 200 && status < 300) {
        results.working.push(entry);
        const icon = elapsed > 5000 ? '🐌' : '✓';
        console.log(`  ${icon} [${status}] ${elapsed}ms │ ${linkInfo?.text || '(no text)'}`);
        console.log(`         └─ ${url.substring(0, 80)}`);
        if (elapsed > 5000) results.slow.push(entry);
      } else if (status >= 300 && status < 400) {
        results.redirected.push(entry);
        console.log(`  ↪ [${status}] ${elapsed}ms │ ${linkInfo?.text || '(no text)'}`);
        console.log(`         └─ ${url.substring(0, 80)}`);
        console.log(`         → Redirects to: ${finalUrl.substring(0, 80)}`);
      } else {
        results.broken.push(entry);
        console.log(`  ✗ [${status}] ${elapsed}ms │ ${linkInfo?.text || '(no text)'}`);
        console.log(`         └─ ${url}`);
      }
    } catch (err) {
      const elapsed = Date.now() - startTime;
      results.broken.push({ url, status: 'TIMEOUT/ERROR', elapsed, text: linkInfo?.text || '', error: err.message.substring(0, 80) });
      console.log(`  ✗ [ERROR] ${elapsed}ms │ ${linkInfo?.text || '(no text)'}`);
      console.log(`         └─ ${url}`);
      console.log(`         └─ Error: ${err.message.substring(0, 80)}`);
    }
  }

  // 4. Test telephone links format
  console.log('\n─── TELEPHONE LINK VALIDATION ───\n');
  for (const link of telLinks) {
    const phoneNum = link.href.replace('tel:', '');
    const isValid = /^[\d+\-\s]+$/.test(phoneNum) && phoneNum.length >= 5;
    const icon = isValid ? '✓' : '✗';
    console.log(`  ${icon} ${link.text} → ${link.href} (${isValid ? 'valid format' : 'INVALID format'})`);
    if (isValid) results.working.push({ url: link.href, status: 'VALID', text: link.text });
    else results.broken.push({ url: link.href, status: 'INVALID', text: link.text });
  }

  // 5. Test "View All" and navigation buttons
  console.log('\n─── INTERACTIVE BUTTON TESTING ───\n');
  const viewAllButtons = await page.$$('text=View All');
  console.log(`  "View All" buttons found: ${viewAllButtons.length}`);

  for (let i = 0; i < viewAllButtons.length; i++) {
    try {
      const btnText = await viewAllButtons[i].textContent();
      const parent = await viewAllButtons[i].evaluateHandle(el => el.closest('a'));
      const href = await parent.evaluate(el => el ? el.href : null).catch(() => null);
      if (href) {
        console.log(`    [${i + 1}] "${btnText.trim()}" → ${href}`);
      } else {
        console.log(`    [${i + 1}] "${btnText.trim()}" → (JS navigation/no href)`);
      }
    } catch (e) {
      console.log(`    [${i + 1}] Error: ${e.message.substring(0, 50)}`);
    }
  }

  // Also check "Apply Now" and "Join Now" buttons
  const actionButtons = await page.$$('text=/Apply Now|Join Now|Participate/');
  console.log(`\n  Action buttons (Apply Now/Join Now/Participate): ${actionButtons.length}`);
  for (let i = 0; i < actionButtons.length; i++) {
    try {
      const btnText = await actionButtons[i].textContent();
      const parent = await actionButtons[i].evaluateHandle(el => el.closest('a'));
      const href = await parent.evaluate(el => el ? el.href : null).catch(() => null);
      if (href) {
        console.log(`    [${i + 1}] "${btnText.trim().substring(0, 40)}" → ${href.substring(0, 80)}`);
      } else {
        console.log(`    [${i + 1}] "${btnText.trim().substring(0, 40)}" → (JS navigation)`);
      }
    } catch (e) {}
  }

  // 6. Final summary
  console.log('\n═══════════════════════════════════════════════════════════════════════════════');
  console.log('  HYPERLINK TEST SUMMARY');
  console.log('═══════════════════════════════════════════════════════════════════════════════');
  console.log(`  ✓ Working:     ${results.working.length}`);
  console.log(`  ↪ Redirected:  ${results.redirected.length}`);
  console.log(`  ✗ Broken:      ${results.broken.length}`);
  console.log(`  🐌 Slow (>5s): ${results.slow.length}`);
  console.log('');

  if (results.broken.length > 0) {
    console.log('  BROKEN LINKS:');
    for (const b of results.broken) {
      console.log(`    ❌ [${b.status}] ${b.text || '(no text)'} → ${b.url}`);
      if (b.error) console.log(`       Error: ${b.error}`);
    }
  }

  if (results.slow.length > 0) {
    console.log('\n  SLOW LINKS (>5 seconds):');
    for (const s of results.slow) {
      console.log(`    🐌 ${s.elapsed}ms ${s.text} → ${s.url.substring(0, 70)}`);
    }
  }

  const verdict = results.broken.length === 0 ? '✅ ALL LINKS VALID' :
    results.broken.length <= 1 ? '⚠️  MINOR ISSUES (1 broken link)' :
    '❌ BROKEN LINKS DETECTED';
  console.log(`\n  VERDICT: ${verdict}`);
  console.log('═══════════════════════════════════════════════════════════════════════════════');

  await browser.close();
}

testAllLinks().catch(console.error);
