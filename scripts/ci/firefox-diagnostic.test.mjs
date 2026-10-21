import assert from 'node:assert/strict';
import { mkdtempSync, mkdirSync, writeFileSync, readFileSync, existsSync, cpSync, rmSync, realpathSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, delimiter } from 'node:path';
import { spawnSync } from 'node:child_process';
import test from 'node:test';

function runDiagnostic(repeats, exitCode = 0) {
  const root = realpathSync(mkdtempSync(join(tmpdir(), 'firefox-diagnostic-contract-')));
  try {
    for (const directory of ['scripts/ci', 'web/frontend', 'bin']) {
      mkdirSync(join(root, directory), { recursive: true });
    }
    cpSync(new URL('./run-firefox-diagnostic.sh', import.meta.url), join(root, 'scripts/ci/run-firefox-diagnostic.sh'));
    const output = join(root, 'arguments.json');
    writeFileSync(join(root, 'bin/npx'), `#!/bin/sh\nexec "${process.execPath}" "${join(root, 'mock.mjs')}" "$@"\n`, { mode: 0o700 });
    writeFileSync(join(root, 'mock.mjs'), `import { writeFileSync } from 'node:fs';
writeFileSync(process.env.DIAGNOSTIC_OUTPUT, JSON.stringify({ args: process.argv.slice(2), debug: process.env.DEBUG, cwd: process.cwd() }));
process.exit(Number(process.env.DIAGNOSTIC_EXIT));
`);
    const env = { ...process.env, PATH: `${join(root, 'bin')}${delimiter}${process.env.PATH}`, DIAGNOSTIC_OUTPUT: output, DIAGNOSTIC_EXIT: String(exitCode) };
    delete env.PLAYWRIGHT_FIREFOX_DIAGNOSTIC_REPEATS;
    if (repeats !== undefined) env.PLAYWRIGHT_FIREFOX_DIAGNOSTIC_REPEATS = repeats;
    const result = spawnSync('sh', [join(root, 'scripts/ci/run-firefox-diagnostic.sh')], { env, encoding: 'utf8', timeout: 5000 });
    assert.equal(result.error, undefined);
    return { status: result.status, call: existsSync(output) ? JSON.parse(readFileSync(output, 'utf8')) : null, frontend: join(root, 'web/frontend') };
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
}

for (const repeats of [undefined, '0']) {
  test(`disabled diagnostic (${repeats}) never starts a browser`, () => {
    const result = runDiagnostic(repeats);
    assert.deepEqual(result.call, null);
    assert.equal(result.status, 0);
  });
}

for (const repeats of ['30', '100']) {
  test(`diagnostic ${repeats} preserves strict execution and separate artifacts`, () => {
    const result = runDiagnostic(repeats);
    assert.equal(result.status, 0);
    assert.equal(result.call.debug, 'pw:browser');
    assert.equal(result.call.cwd, result.frontend);
    assert.deepEqual(result.call.args, ['--no-install', 'playwright', 'test', 'e2e/accessibility.spec.ts', '--project=firefox', '--grep=^.*la pagina di accesso non presenta violazioni axe critiche o serie$', `--repeat-each=${repeats}`, '--retries=0', '--max-failures=1', '--reporter=line', '--trace=on', '--output=test-results/firefox-diagnostic']);
  });
}

for (const repeats of ['-1', '1', '30; exit 0', '1000']) {
  test(`invalid repetitions rejected (${repeats})`, () => {
    const result = runDiagnostic(repeats);
    assert.equal(result.status, 2);
    assert.equal(result.call, null);
  });
}

test('browser failure propagates unchanged', () => {
  assert.equal(runDiagnostic('30', 7).status, 7);
});
