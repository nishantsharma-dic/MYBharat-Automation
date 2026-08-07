const { chromium } = require('playwright');

async function deepAnalysis() {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({ viewport: { width: 1920, height: 1080 } });

  await page.goto('https://mybharat.gov.in/states', { waitUntil: 'networkidle', timeout: 60000 });
  await page.waitForTimeout(5000);

  console.log('=== DEEP PAGE ANALYSIS ===\n');

  // 1. Get full body text to understand content
  const bodyText = await page.textContent('body');
  console.log('--- FULL PAGE TEXT (cleaned) ---');
  const cleanText = bodyText.replace(/\s+/g, ' ').trim();
  console.log(cleanText.substring(0, 5000));
  console.log('\n... (truncated)\n');

  // 2. Check where "Goa" appears
  console.log('--- WHERE "Goa" APPEARS ---');
  const goaContext = await page.evaluate(() => {
    const text = document.body.innerText;
    const idx = text.toLowerCase().indexOf('goa');
    if (idx >= 0) {
      return text.substring(Math.max(0, idx - 50), idx + 50);
    }
    return 'Not found in innerText';
  });
  console.log(`Context: "${goaContext}"`);

  // 3. Get all card/item content
  console.log('\n--- CARD/ITEM ELEMENTS CONTENT ---');
  const cardData = await page.evaluate(() => {
    const cards = document.querySelectorAll('[class*="card"], [class*="district"], [class*="item"]');
    return Array.from(cards).slice(0, 60).map((el, i) => ({
      index: i,
      text: el.textContent.trim().substring(0, 200),
      className: el.className.substring(0, 100),
      tag: el.tagName
    }));
  });

  for (const card of cardData) {
    if (card.text.length > 0) {
      console.log(`[${card.index}] (${card.tag}.${card.className.substring(0, 40)}) → ${card.text.substring(0, 150)}`);
    }
  }

  // 4. Look for district-specific selectors
  console.log('\n--- DISTRICT-SPECIFIC ELEMENTS ---');
  const districtElements = await page.evaluate(() => {
    // Look for common patterns
    const selectors = [
      '[class*="district"]', '[class*="District"]',
      '[data-district]', '[class*="location"]',
      '[class*="region"]', '[class*="area"]',
      'select option', '[class*="dropdown"]',
      '[class*="filter"]', '[class*="select"]'
    ];
    const results = {};
    for (const sel of selectors) {
      const els = document.querySelectorAll(sel);
      if (els.length > 0) {
        results[sel] = Array.from(els).slice(0, 10).map(el => el.textContent.trim().substring(0, 100));
      }
    }
    return results;
  });
  console.log(JSON.stringify(districtElements, null, 2));

  // 5. Check all headings
  console.log('\n--- ALL HEADINGS ---');
  const headings = await page.evaluate(() => {
    const hs = document.querySelectorAll('h1, h2, h3, h4, h5, h6');
    return Array.from(hs).map(h => ({ tag: h.tagName, text: h.textContent.trim() }));
  });
  for (const h of headings) {
    console.log(`${h.tag}: ${h.text}`);
  }

  // 6. Check all select/dropdown elements for district lists
  console.log('\n--- SELECT/DROPDOWN ELEMENTS ---');
  const selects = await page.evaluate(() => {
    const selectEls = document.querySelectorAll('select');
    return Array.from(selectEls).map(sel => ({
      name: sel.name || sel.id || sel.className,
      options: Array.from(sel.options).map(o => o.textContent.trim())
    }));
  });
  console.log(JSON.stringify(selects, null, 2));

  // 7. Check for API calls / network data
  console.log('\n--- NUMBERS AND STATS DETAIL ---');
  const allStats = await page.evaluate(() => {
    const text = document.body.innerText;
    // Find all numbers with surrounding context
    const matches = [];
    const regex = /([\w\s]{0,30})([\d,]+(?:\.\d+)?)([\w\s]{0,30})/g;
    let match;
    while ((match = regex.exec(text)) !== null) {
      const num = match[2].replace(/,/g, '');
      if (num.length >= 3) { // Only numbers with 3+ digits
        matches.push({
          before: match[1].trim(),
          number: match[2],
          after: match[3].trim(),
          full: `${match[1].trim()} [${match[2]}] ${match[3].trim()}`
        });
      }
    }
    return matches;
  });
  for (const s of allStats) {
    console.log(`  ${s.full}`);
  }

  // 8. Get page structure overview
  console.log('\n--- MAIN SECTIONS ---');
  const sections = await page.evaluate(() => {
    const mainChildren = document.querySelector('#root')?.children;
    if (!mainChildren) return [];
    return Array.from(mainChildren).map(child => ({
      tag: child.tagName,
      className: child.className.substring(0, 80),
      childCount: child.children.length,
      textPreview: child.textContent.trim().substring(0, 200)
    }));
  });
  for (const s of sections) {
    console.log(`<${s.tag} class="${s.className}"> (${s.childCount} children)`);
    console.log(`  Preview: ${s.textPreview.substring(0, 150)}`);
  }

  await browser.close();
}

deepAnalysis().catch(console.error);
