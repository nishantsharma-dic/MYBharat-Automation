const { chromium } = require('playwright');

async function verifyAndCrossCheck() {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({ viewport: { width: 1920, height: 1080 } });

  await page.goto('https://mybharat.gov.in/states', { waitUntil: 'networkidle', timeout: 60000 });
  await page.waitForTimeout(5000);

  console.log('=== VERIFICATION & CROSS-CHECK ===\n');

  // 1. Verify "Goa" is false positive (part of "goals")
  console.log('--- TEST: "Goa" FALSE POSITIVE VERIFICATION ---');
  const goaCheck = await page.evaluate(() => {
    const html = document.body.innerHTML;
    const textContent = document.body.innerText;
    // Check if "Goa" appears as standalone word (state name) vs part of "goals"
    const standaloneGoa = textContent.match(/\bGoa\b/g);
    const goalsOccurrence = textContent.match(/\bgoals?\b/gi);
    return { standaloneGoa, goalsOccurrence };
  });
  console.log(`  Standalone "Goa": ${JSON.stringify(goaCheck.standaloneGoa)}`);
  console.log(`  "goals" occurrences: ${JSON.stringify(goaCheck.goalsOccurrence)}`);
  if (!goaCheck.standaloneGoa || goaCheck.standaloneGoa.length === 0) {
    console.log('  ✅ CONFIRMED: "Goa" is NOT present as state name. It was part of "goals"');
  } else {
    console.log('  ❌ "Goa" found as standalone word');
  }

  // 2. Cross-verify Organizations count
  console.log('\n--- TEST: ORGANIZATIONS COUNT CROSS-VERIFICATION ---');
  const orgCounts = await page.evaluate(() => {
    const text = document.body.innerText;
    const total = text.match(/Organizations\s*\((\d[\d,]*)\)/);
    const govt = text.match(/Government Organizations\s*\((\d[\d,]*)\)/);
    const knowledge = text.match(/Knowledge Institution\s*\((\d[\d,]*)\)/);
    const nonprofit = text.match(/Not-for-Profit Organizations\s*\((\d[\d,]*)\)/);
    const forprofit = text.match(/For-Profit Organizations\s*\((\d[\d,]*)\)/);
    return {
      total: total ? total[1] : null,
      govt: govt ? govt[1] : null,
      knowledge: knowledge ? knowledge[1] : null,
      nonprofit: nonprofit ? nonprofit[1] : null,
      forprofit: forprofit ? forprofit[1] : null
    };
  });

  console.log(`  Total Organizations: ${orgCounts.total}`);
  console.log(`  Government Organizations: ${orgCounts.govt}`);
  console.log(`  Knowledge Institutions: ${orgCounts.knowledge}`);
  console.log(`  Not-for-Profit: ${orgCounts.nonprofit}`);
  console.log(`  For-Profit: ${orgCounts.forprofit}`);

  // Cross-verify sum
  const totalNum = parseInt((orgCounts.total || '0').replace(/,/g, ''));
  const govtNum = parseInt((orgCounts.govt || '0').replace(/,/g, ''));
  const knowledgeNum = parseInt((orgCounts.knowledge || '0').replace(/,/g, ''));
  const nonprofitNum = parseInt((orgCounts.nonprofit || '0').replace(/,/g, ''));
  const forprofitNum = parseInt((orgCounts.forprofit || '0').replace(/,/g, ''));
  const calculatedSum = govtNum + knowledgeNum + nonprofitNum + forprofitNum;

  console.log(`\n  Sum of subcategories: ${govtNum} + ${knowledgeNum} + ${nonprofitNum} + ${forprofitNum} = ${calculatedSum}`);
  console.log(`  Displayed total: ${totalNum}`);

  if (calculatedSum === totalNum) {
    console.log('  ✅ PASS: Organization subcategory sum MATCHES total');
  } else {
    console.log(`  ❌ FAIL: Sum (${calculatedSum}) ≠ Total (${totalNum}) — Difference: ${Math.abs(calculatedSum - totalNum)}`);
  }

  // 3. Verify ELP count
  console.log('\n--- TEST: ELP COUNT ---');
  const elpCount = await page.evaluate(() => {
    const text = document.body.innerText;
    const match = text.match(/ELP.*?\((\d[\d,]*)\)/);
    return match ? match[1] : null;
  });
  console.log(`  ELP Programs: ${elpCount}`);

  // 4. Verify Volunteer for Bharat count
  console.log('\n--- TEST: VOLUNTEER FOR BHARAT COUNT ---');
  const vfbCount = await page.evaluate(() => {
    const text = document.body.innerText;
    const match = text.match(/Volunteer for Bharat\s*\((\d[\d,]*)\)/);
    return match ? match[1] : null;
  });
  console.log(`  Volunteer for Bharat events: ${vfbCount}`);

  // 5. Verify Quiz count
  console.log('\n--- TEST: QUIZ COUNT ---');
  const quizCount = await page.evaluate(() => {
    const text = document.body.innerText;
    const match = text.match(/Quiz\s*\((\d[\d,]*)\)/);
    return match ? match[1] : null;
  });
  console.log(`  Quizzes: ${quizCount}`);

  // 6. Verify Youth Registered count format
  console.log('\n--- TEST: YOUTH REGISTERED COUNT ---');
  const youthCount = await page.evaluate(() => {
    const text = document.body.innerText;
    const match = text.match(/Total Youth Registered\s*([\d,]+)/);
    return match ? match[1] : null;
  });
  console.log(`  Total Youth Registered: ${youthCount}`);
  const youthNum = parseInt((youthCount || '0').replace(/,/g, ''));
  console.log(`  Numeric value: ${youthNum.toLocaleString('en-IN')}`);
  if (youthNum > 0) {
    console.log('  ✅ PASS: Youth count is a positive number');
  }

  // 7. Cross-verify via API (if available)
  console.log('\n--- TEST: API CROSS-VERIFICATION ---');
  // Intercept API calls to see what data the page fetches
  const apiPage = await browser.newPage();
  const apiCalls = [];
  apiPage.on('response', async (response) => {
    const url = response.url();
    if (url.includes('api') || url.includes('graphql') || url.includes('state')) {
      try {
        const body = await response.text();
        apiCalls.push({ url, status: response.status(), bodyLength: body.length, bodyPreview: body.substring(0, 500) });
      } catch (e) {}
    }
  });
  await apiPage.goto('https://mybharat.gov.in/states', { waitUntil: 'networkidle', timeout: 60000 });
  await apiPage.waitForTimeout(5000);

  console.log(`  API calls intercepted: ${apiCalls.length}`);
  for (const call of apiCalls) {
    console.log(`  URL: ${call.url.substring(0, 100)}`);
    console.log(`  Status: ${call.status}, Body size: ${call.bodyLength}`);
    console.log(`  Preview: ${call.bodyPreview.substring(0, 200)}`);
    console.log('');
  }
  await apiPage.close();

  // 8. Verify page shows Rajasthan-specific content only
  console.log('\n--- TEST: RAJASTHAN-SPECIFIC CONTENT ---');
  const rajContent = await page.evaluate(() => {
    const text = document.body.innerText;
    const rajMentions = (text.match(/rajasthan/gi) || []).length;
    const h1 = document.querySelector('h1');
    return {
      rajMentions,
      h1Text: h1 ? h1.textContent.trim() : null,
      hasRajInTitle: h1 ? h1.textContent.toLowerCase().includes('rajasthan') : false
    };
  });
  console.log(`  "Rajasthan" mentioned ${rajContent.rajMentions} times`);
  console.log(`  H1 text: "${rajContent.h1Text}"`);
  console.log(`  H1 contains Rajasthan: ${rajContent.hasRajInTitle}`);
  if (rajContent.hasRajInTitle) {
    console.log('  ✅ PASS: Main heading confirms this is Rajasthan page');
  }

  // 9. Check "About" section for Rajasthan government description
  console.log('\n--- TEST: ABOUT SECTION ---');
  const aboutText = await page.evaluate(() => {
    const h2s = document.querySelectorAll('h2');
    for (const h of h2s) {
      if (h.textContent.trim() === 'About') {
        const parent = h.closest('section') || h.parentElement;
        return parent ? parent.textContent.trim().substring(0, 500) : null;
      }
    }
    return null;
  });
  console.log(`  About section: ${aboutText || 'Not found'}`);

  // 10. Verify ELP programs show Rajasthan location
  console.log('\n--- TEST: ELP PROGRAMS LOCATION ---');
  const elpLocations = await page.evaluate(() => {
    const locationEls = document.querySelectorAll('[class*="location"], [class*="items-center gap-1 text-xs"]');
    return Array.from(locationEls).map(el => el.textContent.trim()).filter(t => t.length > 0 && t.length < 100);
  });
  console.log(`  ELP Locations found: ${elpLocations.length}`);
  for (const loc of elpLocations) {
    console.log(`    → ${loc}`);
  }
  const allRajasthan = elpLocations.every(l => l.toUpperCase().includes('RAJASTHAN'));
  if (allRajasthan && elpLocations.length > 0) {
    console.log('  ✅ PASS: All ELP programs are from Rajasthan');
  } else if (elpLocations.length === 0) {
    console.log('  ⚠️  No location elements found');
  } else {
    console.log('  ❌ FAIL: Some ELPs are NOT from Rajasthan');
  }

  await browser.close();
}

verifyAndCrossCheck().catch(console.error);
