const { chromium } = require('playwright');

// Known Rajasthan districts (all 33 districts)
const RAJASTHAN_DISTRICTS = [
  'Ajmer', 'Alwar', 'Banswara', 'Baran', 'Barmer',
  'Bharatpur', 'Bhilwara', 'Bikaner', 'Bundi', 'Chittorgarh',
  'Churu', 'Dausa', 'Dholpur', 'Dungarpur', 'Hanumangarh',
  'Jaipur', 'Jaisalmer', 'Jalore', 'Jhalawar', 'Jhunjhunu',
  'Jodhpur', 'Karauli', 'Kota', 'Nagaur', 'Pali',
  'Pratapgarh', 'Rajsamand', 'Sawai Madhopur', 'Sikar', 'Sirohi',
  'Sri Ganganagar', 'Tonk', 'Udaipur'
];

// Other states' districts that should NOT appear
const OTHER_STATE_INDICATORS = [
  'Mumbai', 'Pune', 'Chennai', 'Kolkata', 'Bengaluru',
  'Hyderabad', 'Lucknow', 'Patna', 'Bhopal', 'Chandigarh',
  'Thiruvananthapuram', 'Guwahati', 'Ranchi', 'Dehradun',
  'Shimla', 'Gandhinagar', 'Panaji', 'Imphal', 'Kohima',
  'Aizawl', 'Agartala', 'Itanagar', 'Shillong', 'Gangtok',
  'Raipur', 'Bhubaneswar', 'Amaravati', 'Srinagar'
];

const results = {
  passed: [],
  failed: [],
  warnings: []
};

function logPass(test, detail) {
  results.passed.push({ test, detail });
  console.log(`✅ PASS: ${test} — ${detail}`);
}

function logFail(test, detail) {
  results.failed.push({ test, detail });
  console.log(`❌ FAIL: ${test} — ${detail}`);
}

function logWarn(test, detail) {
  results.warnings.push({ test, detail });
  console.log(`⚠️  WARN: ${test} — ${detail}`);
}

async function main() {
  console.log('='.repeat(80));
  console.log('MY BHARAT - STATES PAGE COMPREHENSIVE TEST');
  console.log('URL: https://mybharat.gov.in/states');
  console.log('Date:', new Date().toISOString());
  console.log('='.repeat(80));
  console.log('');

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1920, height: 1080 },
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'
  });
  const page = await context.newPage();

  try {
    // ========== SECTION 1: PAGE LOAD ==========
    await testPageLoad(page);

    // ========== SECTION 2: RAJASTHAN VERIFICATION ==========
    await testRajasthanOnly(page);

    // ========== SECTION 3: DISTRICT DATA ==========
    await testDistrictData(page);

    // ========== SECTION 4: COUNTS & STATISTICS ==========
    await testCountsAndStatistics(page);

    // ========== SECTION 5: HYPERLINKS ==========
    await testHyperlinks(page, context);

  } catch (err) {
    logFail('EXECUTION', `Script error: ${err.message}`);
    console.error(err.stack);
  } finally {
    await browser.close();
  }

  // ========== FINAL REPORT ==========
  printReport();
}

async function testPageLoad(page) {
  console.log('\n' + '─'.repeat(80));
  console.log('SECTION 1: PAGE LOAD & ACCESSIBILITY');
  console.log('─'.repeat(80));

  // Test 1.1: Page loads successfully
  const response = await page.goto('https://mybharat.gov.in/states', {
    waitUntil: 'networkidle',
    timeout: 60000
  });

  if (response && response.status() === 200) {
    logPass('Page Load', `HTTP ${response.status()} — page loaded successfully`);
  } else {
    logFail('Page Load', `HTTP ${response ? response.status() : 'no response'}`);
    return;
  }

  // Wait for React to render
  await page.waitForTimeout(5000);

  // Test 1.2: Page title
  const title = await page.title();
  console.log(`   Page Title: "${title}"`);
  if (title && title.length > 0) {
    logPass('Page Title', `Title is "${title}"`);
  } else {
    logFail('Page Title', 'No title found');
  }

  // Test 1.3: Content rendered (not empty root div)
  const rootContent = await page.$eval('#root', el => el.innerHTML.length);
  if (rootContent > 100) {
    logPass('Content Rendered', `Root div has ${rootContent} chars of content`);
  } else {
    logFail('Content Rendered', `Root div only has ${rootContent} chars`);
  }

  // Take screenshot
  await page.screenshot({ path: 'states-page-full.png', fullPage: true });
  console.log('   📸 Screenshot saved: states-page-full.png');
}

async function testRajasthanOnly(page) {
  console.log('\n' + '─'.repeat(80));
  console.log('SECTION 2: RAJASTHAN STATE VERIFICATION');
  console.log('─'.repeat(80));

  const pageText = await page.textContent('body');
  const pageTextLower = pageText.toLowerCase();

  // Test 2.1: Page mentions Rajasthan
  if (pageTextLower.includes('rajasthan')) {
    logPass('Rajasthan Mentioned', 'Page contains "Rajasthan" text');
  } else {
    logFail('Rajasthan Mentioned', 'Page does NOT contain "Rajasthan" text');
  }

  // Test 2.2: Check for other state names that should NOT be present as data
  const otherStates = [
    'Maharashtra', 'Tamil Nadu', 'Karnataka', 'Andhra Pradesh',
    'Telangana', 'Kerala', 'West Bengal', 'Gujarat', 'Punjab',
    'Haryana', 'Madhya Pradesh', 'Bihar', 'Odisha', 'Jharkhand',
    'Chhattisgarh', 'Uttarakhand', 'Himachal Pradesh', 'Assam',
    'Goa', 'Tripura', 'Meghalaya', 'Manipur', 'Nagaland',
    'Mizoram', 'Arunachal Pradesh', 'Sikkim'
  ];

  const foundOtherStates = [];
  for (const state of otherStates) {
    if (pageTextLower.includes(state.toLowerCase())) {
      foundOtherStates.push(state);
    }
  }

  if (foundOtherStates.length === 0) {
    logPass('No Other States', 'No other state names found in page data');
  } else {
    logFail('No Other States', `Found other states: ${foundOtherStates.join(', ')}`);
  }

  // Test 2.3: Check other state cities NOT present
  const foundOtherCities = [];
  for (const city of OTHER_STATE_INDICATORS) {
    if (pageTextLower.includes(city.toLowerCase())) {
      foundOtherCities.push(city);
    }
  }

  if (foundOtherCities.length === 0) {
    logPass('No Other State Cities', 'No non-Rajasthan city names found');
  } else {
    logWarn('Other Cities Found', `Found: ${foundOtherCities.join(', ')} (may be in nav/footer)`);
  }
}

async function testDistrictData(page) {
  console.log('\n' + '─'.repeat(80));
  console.log('SECTION 3: DISTRICT DATA VERIFICATION');
  console.log('─'.repeat(80));

  const pageText = await page.textContent('body');
  const pageTextLower = pageText.toLowerCase();

  // Test 3.1: Check which Rajasthan districts are present
  const foundDistricts = [];
  const missingDistricts = [];

  for (const district of RAJASTHAN_DISTRICTS) {
    if (pageTextLower.includes(district.toLowerCase())) {
      foundDistricts.push(district);
    } else {
      missingDistricts.push(district);
    }
  }

  console.log(`   Found ${foundDistricts.length}/${RAJASTHAN_DISTRICTS.length} Rajasthan districts`);

  if (foundDistricts.length > 0) {
    logPass('Rajasthan Districts Present', `${foundDistricts.length} districts found on page`);
    console.log('   Districts found:', foundDistricts.join(', '));
  } else {
    logFail('Rajasthan Districts Present', 'No Rajasthan districts found');
  }

  if (missingDistricts.length > 0) {
    logWarn('Missing Districts', `${missingDistricts.length} districts not found: ${missingDistricts.join(', ')}`);
  }

  // Test 3.2: Extract table/card data if present
  const tables = await page.$$('table');
  console.log(`   Found ${tables.length} table(s) on page`);

  if (tables.length > 0) {
    for (let i = 0; i < tables.length; i++) {
      const rows = await tables[i].$$('tr');
      console.log(`   Table ${i + 1}: ${rows.length} rows`);
    }
  }

  // Test 3.3: Look for district cards/list items
  const cards = await page.$$('[class*="card"], [class*="district"], [class*="item"]');
  console.log(`   Found ${cards.length} card/district/item elements`);
}

async function testCountsAndStatistics(page) {
  console.log('\n' + '─'.repeat(80));
  console.log('SECTION 4: COUNTS & STATISTICS VERIFICATION');
  console.log('─'.repeat(80));

  // Extract all numbers with their labels
  const stats = await page.evaluate(() => {
    const results = [];

    // Look for stat/count sections
    const allElements = document.querySelectorAll('*');
    const numberPattern = /^[\d,]+$/;
    const largeNumberPattern = /[\d,]{2,}/;

    // Find elements with large numbers (likely statistics)
    for (const el of allElements) {
      const text = el.textContent.trim();
      if (numberPattern.test(text) && text.replace(/,/g, '').length >= 2) {
        const parent = el.parentElement;
        const label = parent ? parent.textContent.trim().replace(text, '').trim() : '';
        if (label.length < 100) {
          results.push({
            value: text,
            numericValue: parseInt(text.replace(/,/g, '')),
            label: label.substring(0, 80),
            tag: el.tagName,
            className: el.className ? el.className.substring(0, 50) : ''
          });
        }
      }
    }

    // Deduplicate
    const seen = new Set();
    return results.filter(r => {
      const key = `${r.value}-${r.label}`;
      if (seen.has(key)) return false;
      seen.add(key);
      return true;
    });
  });

  console.log(`   Found ${stats.length} numerical statistics on page`);
  console.log('');

  if (stats.length > 0) {
    logPass('Statistics Present', `${stats.length} numerical values found`);
    console.log('   Extracted Statistics:');
    console.log('   ' + '─'.repeat(60));

    let totalSum = 0;
    const statsByLabel = {};

    for (const stat of stats) {
      console.log(`   Value: ${stat.value.padStart(12)} | Label: ${stat.label || '(no label)'}`);
      totalSum += stat.numericValue;
      if (stat.label) {
        statsByLabel[stat.label] = stat.numericValue;
      }
    }

    console.log('   ' + '─'.repeat(60));
    console.log(`   Sum of all values: ${totalSum.toLocaleString()}`);
  } else {
    logWarn('Statistics', 'No standalone number elements found, checking text patterns');
  }

  // Also extract any visible counters/badges
  const counters = await page.evaluate(() => {
    const elements = document.querySelectorAll('[class*="count"], [class*="stat"], [class*="number"], [class*="total"], [class*="badge"]');
    return Array.from(elements).map(el => ({
      text: el.textContent.trim().substring(0, 100),
      className: el.className.substring(0, 80)
    })).filter(e => e.text.length > 0 && e.text.length < 100);
  });

  if (counters.length > 0) {
    console.log(`\n   Found ${counters.length} counter/stat elements:`);
    for (const c of counters) {
      console.log(`   → ${c.text}`);
    }
  }

  // Cross-verify: check if district count matches expected
  const districtCountOnPage = await page.evaluate(() => {
    const text = document.body.textContent;
    const match = text.match(/(\d+)\s*districts?/i);
    return match ? parseInt(match[1]) : null;
  });

  if (districtCountOnPage !== null) {
    if (districtCountOnPage === 33) {
      logPass('District Count', `Page shows ${districtCountOnPage} districts (correct for Rajasthan)`);
    } else {
      logWarn('District Count', `Page shows ${districtCountOnPage} districts (Rajasthan has 33)`);
    }
  }
}

async function testHyperlinks(page, context) {
  console.log('\n' + '─'.repeat(80));
  console.log('SECTION 5: HYPERLINK TESTING');
  console.log('─'.repeat(80));

  // Extract all links
  const links = await page.evaluate(() => {
    const anchors = document.querySelectorAll('a[href]');
    return Array.from(anchors).map(a => ({
      href: a.href,
      text: a.textContent.trim().substring(0, 60),
      target: a.target || '_self',
      isExternal: a.href.startsWith('http') && !a.href.includes('mybharat.gov.in')
    }));
  });

  console.log(`   Total links found: ${links.length}`);

  // Categorize links
  const internalLinks = links.filter(l => !l.isExternal && l.href.startsWith('http'));
  const externalLinks = links.filter(l => l.isExternal);
  const hashLinks = links.filter(l => l.href.includes('#'));
  const mailLinks = links.filter(l => l.href.startsWith('mailto:'));
  const telLinks = links.filter(l => l.href.startsWith('tel:'));

  console.log(`   Internal links: ${internalLinks.length}`);
  console.log(`   External links: ${externalLinks.length}`);
  console.log(`   Hash/anchor links: ${hashLinks.length}`);
  console.log(`   Email links: ${mailLinks.length}`);
  console.log(`   Phone links: ${telLinks.length}`);
  console.log('');

  // Test all HTTP links
  const httpLinks = links.filter(l => l.href.startsWith('http'));
  const uniqueUrls = [...new Set(httpLinks.map(l => l.href))];
  console.log(`   Unique HTTP URLs to test: ${uniqueUrls.length}`);
  console.log('');

  let working = 0;
  let broken = 0;
  let redirected = 0;
  const brokenLinks = [];

  // Test links in batches
  const batchSize = 5;
  for (let i = 0; i < uniqueUrls.length; i += batchSize) {
    const batch = uniqueUrls.slice(i, i + batchSize);
    const promises = batch.map(async (url) => {
      try {
        const testPage = await context.newPage();
        const resp = await testPage.goto(url, { timeout: 15000, waitUntil: 'domcontentloaded' });
        const status = resp ? resp.status() : 0;
        await testPage.close();

        if (status >= 200 && status < 300) {
          working++;
          return { url, status, ok: true };
        } else if (status >= 300 && status < 400) {
          redirected++;
          return { url, status, ok: true };
        } else {
          broken++;
          brokenLinks.push({ url, status });
          return { url, status, ok: false };
        }
      } catch (err) {
        broken++;
        brokenLinks.push({ url, status: err.message.substring(0, 50) });
        return { url, status: 'ERROR', ok: false };
      }
    });

    const results = await Promise.all(promises);
    for (const r of results) {
      const icon = r.ok ? '✓' : '✗';
      const statusStr = typeof r.status === 'number' ? `HTTP ${r.status}` : r.status;
      console.log(`   ${icon} [${statusStr}] ${r.url.substring(0, 80)}`);
    }
  }

  console.log('');
  console.log(`   Summary: ${working} working, ${redirected} redirected, ${broken} broken`);

  if (broken === 0) {
    logPass('All Links Valid', `${working + redirected} links tested, all working`);
  } else {
    logFail('Broken Links Found', `${broken} broken link(s) detected`);
    for (const bl of brokenLinks) {
      console.log(`   ❌ ${bl.url} → ${bl.status}`);
    }
  }

  // List all links for documentation
  console.log('\n   Complete Link Inventory:');
  console.log('   ' + '─'.repeat(60));
  for (const link of links) {
    const type = link.isExternal ? '[EXT]' : '[INT]';
    console.log(`   ${type} ${link.text || '(no text)'} → ${link.href.substring(0, 70)}`);
  }
}

function printReport() {
  console.log('\n' + '='.repeat(80));
  console.log('FINAL TEST REPORT');
  console.log('='.repeat(80));
  console.log(`\n  Total Tests: ${results.passed.length + results.failed.length + results.warnings.length}`);
  console.log(`  ✅ Passed:   ${results.passed.length}`);
  console.log(`  ❌ Failed:   ${results.failed.length}`);
  console.log(`  ⚠️  Warnings: ${results.warnings.length}`);
  console.log('');

  if (results.failed.length > 0) {
    console.log('  FAILURES:');
    for (const f of results.failed) {
      console.log(`    ❌ ${f.test}: ${f.detail}`);
    }
  }

  if (results.warnings.length > 0) {
    console.log('\n  WARNINGS:');
    for (const w of results.warnings) {
      console.log(`    ⚠️  ${w.test}: ${w.detail}`);
    }
  }

  console.log('\n' + '='.repeat(80));
  const verdict = results.failed.length === 0 ? '✅ ALL TESTS PASSED' : '❌ SOME TESTS FAILED';
  console.log(`  VERDICT: ${verdict}`);
  console.log('='.repeat(80));
}

main().catch(err => {
  console.error('Fatal error:', err);
  process.exit(1);
});
