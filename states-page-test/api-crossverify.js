const { chromium } = require('playwright');

async function apiCrossVerify() {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({ viewport: { width: 1920, height: 1080 } });

  // Intercept ALL API responses
  const apiData = {};
  page.on('response', async (response) => {
    const url = response.url();
    try {
      if (url.includes('search-api-prod') || url.includes('api-prod') || url.includes('cdn-prod')) {
        const contentType = response.headers()['content-type'] || '';
        if (contentType.includes('json') || url.endsWith('.json')) {
          const body = await response.json().catch(() => null);
          if (body) {
            if (!apiData[url]) apiData[url] = [];
            apiData[url].push(body);
          }
        }
      }
    } catch (e) {}
  });

  await page.goto('https://mybharat.gov.in/states', { waitUntil: 'networkidle', timeout: 60000 });
  await page.waitForTimeout(8000);

  console.log('═══════════════════════════════════════════════════════════════════════════════');
  console.log('  API-LEVEL CROSS-VERIFICATION REPORT');
  console.log('═══════════════════════════════════════════════════════════════════════════════\n');

  // 1. States JSON analysis
  console.log('━━━ 1. STATES CONFIG JSON ━━━');
  const statesData = apiData[Object.keys(apiData).find(k => k.includes('states.json'))];
  if (statesData && statesData[0]) {
    const states = statesData[0].states;
    console.log(`  Total partner states: ${states.count}`);
    console.log(`  State data entries: ${states.data.length}`);
    for (const s of states.data) {
      console.log(`    → Org ID: ${s.org_id}, Name: ${s.org_name}, Activity: ${s.activitytype}`);
    }
  }

  // 2. Verify all org API calls use state_id=28
  console.log('\n━━━ 2. STATE ID VERIFICATION IN API CALLS ━━━');
  const orgUrls = Object.keys(apiData).filter(k => k.includes('/organizations'));
  console.log(`  Organization API calls: ${orgUrls.length}`);

  let allStateId28 = true;
  const orgTotals = {};
  for (const url of orgUrls) {
    const responses = apiData[url];
    for (const resp of responses) {
      if (resp.records) {
        for (const rec of resp.records) {
          const source = rec._source || rec;
          if (source.org_state_id && source.org_state_id !== 28) {
            allStateId28 = false;
            console.log(`  ❌ Found non-Rajasthan org: state_id=${source.org_state_id}`);
          }
          // Collect org types
          const orgType = source.org_type || 'Unknown';
          if (!orgTotals[orgType]) orgTotals[orgType] = new Set();
          orgTotals[orgType].add(source.org_tagging || 'none');
        }
      }
    }
  }

  if (allStateId28) {
    console.log('  ✅ ALL organization records have org_state_id=28 (Rajasthan)');
  }

  // 3. Cross-verify event counts
  console.log('\n━━━ 3. EVENT/VOLUNTEER COUNT VERIFICATION ━━━');
  const eventUrls = Object.keys(apiData).filter(k => k.includes('/events'));
  for (const url of eventUrls) {
    const resp = apiData[url][0];
    console.log(`  Events API total: ${resp.total}`);
    console.log(`  Page displays: 12,261`);
    if (resp.total === 12261) {
      console.log('  ✅ MATCH: Volunteer for Bharat count matches API');
    } else {
      console.log(`  ❌ MISMATCH: API says ${resp.total}, page shows 12,261`);
    }
  }

  // 4. Cross-verify ELP counts
  console.log('\n━━━ 4. ELP COUNT VERIFICATION ━━━');
  const elpUrls = Object.keys(apiData).filter(k => k.includes('/elos_list'));
  for (const url of elpUrls) {
    const resp = apiData[url][0];
    console.log(`  ELP API total: ${resp.total}`);
    console.log(`  Page displays: 1,025`);
    if (resp.total === 1025) {
      console.log('  ✅ MATCH: ELP count matches API');
    } else {
      console.log(`  ❌ MISMATCH: API says ${resp.total}, page shows 1,025`);
    }
  }

  // 5. Cross-verify Quiz data
  console.log('\n━━━ 5. QUIZ DATA VERIFICATION ━━━');
  const quizUrls = Object.keys(apiData).filter(k => k.includes('/quiz'));
  for (const url of quizUrls) {
    const resp = apiData[url][0];
    if (resp.data) {
      const ongoing = resp.data.ongoing ? resp.data.ongoing.length : 0;
      const upcoming = resp.data.upcoming ? resp.data.upcoming.length : 0;
      const past = resp.data.past ? resp.data.past.length : 0;
      const total = ongoing + upcoming + past;
      console.log(`  Quiz API: ongoing=${ongoing}, upcoming=${upcoming}, past=${past}, total=${total}`);
      console.log(`  Page displays: 1`);
      if (total === 1) {
        console.log('  ✅ MATCH: Quiz count matches API');
      } else {
        console.log(`  ⚠️  API total=${total}, page shows 1 (may count only active)`);
      }
      // Show quiz details
      const allQuizzes = [...(resp.data.ongoing || []), ...(resp.data.upcoming || []), ...(resp.data.past || [])];
      for (const q of allQuizzes) {
        console.log(`    Quiz: "${q.name}" (org: ${q.org_id}, status: ${q.status || 'N/A'})`);
      }
    }
  }

  // 6. Organization sub-type breakdown from API
  console.log('\n━━━ 6. ORGANIZATION BREAKDOWN FROM API ━━━');
  const orgBreakdown = { Government: 0, 'Knowledge Institution': 0, 'Not For Profit': 0, 'For Profit': 0 };
  for (const url of orgUrls) {
    const responses = apiData[url];
    for (const resp of responses) {
      if (resp.records && resp.records.length > 0) {
        const source = resp.records[0]._source;
        if (source) {
          const type = source.org_type;
          // Use the total from API response for proper counting
          if (type && resp.total) {
            // Only count unique API calls per type
          }
        }
      }
    }
  }

  // Get totals from specific API calls
  console.log('  Checking individual org type totals from API responses...');
  const typeTotals = {};
  for (const url of orgUrls) {
    const responses = apiData[url];
    for (const resp of responses) {
      if (resp.records && resp.records.length > 0) {
        const source = resp.records[0]._source;
        if (source && source.org_type) {
          const key = `${source.org_type}|${source.org_tagging || 'all'}`;
          if (!typeTotals[key] || resp.total > typeTotals[key]) {
            typeTotals[key] = resp.total;
          }
        }
      }
    }
  }
  console.log('  Type totals from API:');
  for (const [key, total] of Object.entries(typeTotals)) {
    console.log(`    ${key}: ${total}`);
  }

  // 7. ELP records state verification
  console.log('\n━━━ 7. ELP RECORDS STATE VERIFICATION ━━━');
  for (const url of elpUrls) {
    const resp = apiData[url][0];
    if (resp.records) {
      let rajCount = 0;
      let otherCount = 0;
      const otherStates = [];
      for (const rec of resp.records) {
        const source = rec._source || rec;
        if (source.state_id === 28 || source.partner_state_id === 28) {
          rajCount++;
        } else {
          otherCount++;
          otherStates.push(source.state_id || source.partner_state_id);
        }
      }
      console.log(`  ELP records checked: ${resp.records.length}`);
      console.log(`  Rajasthan ELPs: ${rajCount}`);
      console.log(`  Other state ELPs: ${otherCount}`);
      if (otherCount > 0) {
        console.log(`  Other state IDs: ${[...new Set(otherStates)].join(', ')}`);
      }
      if (otherCount === 0) {
        console.log('  ✅ ALL ELP records belong to Rajasthan');
      } else {
        console.log('  ⚠️  Some ELPs may be from other states (checking if state-filtered)');
      }
    }
  }

  // 8. Event records state verification
  console.log('\n━━━ 8. EVENT RECORDS STATE VERIFICATION ━━━');
  for (const url of eventUrls) {
    const resp = apiData[url][0];
    if (resp.records) {
      let rajCount = 0;
      let otherCount = 0;
      for (const rec of resp.records) {
        const source = rec._source || rec;
        if (source.state_id === 28 || source.partner_state_id === 28) {
          rajCount++;
        } else {
          otherCount++;
        }
      }
      console.log(`  Event records checked: ${resp.records.length}`);
      console.log(`  Rajasthan events: ${rajCount}`);
      console.log(`  Other state events: ${otherCount}`);
      if (otherCount === 0) {
        console.log('  ✅ ALL event records belong to Rajasthan');
      }
    }
  }

  console.log('\n═══════════════════════════════════════════════════════════════════════════════');
  console.log('  API VERIFICATION COMPLETE');
  console.log('═══════════════════════════════════════════════════════════════════════════════');

  await browser.close();
}

apiCrossVerify().catch(console.error);
