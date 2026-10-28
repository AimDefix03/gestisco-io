import assert from 'node:assert/strict';
import { mkdtempSync, mkdirSync, writeFileSync, readFileSync, existsSync, cpSync, rmSync, realpathSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, delimiter } from 'node:path';
import { spawnSync } from 'node:child_process';
import test from 'node:test';

const secret = 'valore-segreto-di-prova';

function runContainer({ version = '1.64.1', installed = true, project = 'gestionale-prodlike-abc123', runId = 'ci-42-1-abc123', exitCode = 0, command = ['npm', 'run', 'test:e2e'] } = {}) {
  const root = realpathSync(mkdtempSync(join(tmpdir(), 'playwright-container-contract-')));
  try {
    for (const directory of ['scripts/ci', 'web/frontend', 'bin']) {
      mkdirSync(join(root, directory), { recursive: true });
    }
    for (const script of ['run-playwright-container.sh', 'docker-run-safety.sh']) {
      cpSync(new URL(`./${script}`, import.meta.url), join(root, 'scripts/ci', script));
    }
    if (installed) {
      const packageDirectory = join(root, 'web/frontend/node_modules/@playwright/test');
      mkdirSync(packageDirectory, { recursive: true });
      writeFileSync(join(packageDirectory, 'package.json'), JSON.stringify({ name: '@playwright/test', version }));
    }
    const output = join(root, 'docker.json');
    writeFileSync(join(root, 'bin/docker'), `#!/bin/sh\nexec "${process.execPath}" "${join(root, 'mock.mjs')}" "$@"\n`, { mode: 0o700 });
    writeFileSync(join(root, 'mock.mjs'), `import { writeFileSync } from 'node:fs';
writeFileSync(process.env.CONTAINER_OUTPUT, JSON.stringify({ args: process.argv.slice(2) }));
process.exit(Number(process.env.CONTAINER_EXIT));
`);
    const env = {
      ...process.env,
      PATH: `${join(root, 'bin')}${delimiter}${process.env.PATH}`,
      CONTAINER_OUTPUT: output,
      CONTAINER_EXIT: String(exitCode),
      E2E_PASSWORD: secret,
      PLAYWRIGHT_BASE_URL: 'http://127.0.0.1:28081'
    };
    const cwd = join(root, 'web/frontend');
    const result = spawnSync('sh', [join(root, 'scripts/ci/run-playwright-container.sh'), project, runId, ...command], { cwd, env, encoding: 'utf8', timeout: 5000 });
    assert.equal(result.error, undefined);
    return { status: result.status, call: existsSync(output) ? JSON.parse(readFileSync(output, 'utf8')) : null, root, cwd };
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
}

function optionValues(args, option) {
  return args.flatMap((arg, index) => (arg === option ? [args[index + 1]] : []));
}

test('runs the command in the image matching the installed Playwright version', () => {
  const result = runContainer();
  assert.equal(result.status, 0);
  const { args } = result.call;
  assert.equal(args[0], 'run');
  for (const flag of ['--rm', '--init', '--ipc=host']) assert.ok(args.includes(flag), flag);
  assert.deepEqual(optionValues(args, '--network'), ['host']);
  assert.deepEqual(args.slice(-4), ['mcr.microsoft.com/playwright:v1.64.1-noble', 'npm', 'run', 'test:e2e']);
  assert.deepEqual(optionValues(args, '--workdir'), [result.cwd]);
  assert.deepEqual(optionValues(args, '--volume'), [`${result.root}:${result.root}`]);
});

test('labels the container with the run so cleanup can remove it', () => {
  const { args } = runContainer().call;
  assert.deepEqual(optionValues(args, '--label'), [
    'com.docker.compose.project=gestionale-prodlike-abc123',
    'it.giovannidefilippo.gestionale.run-id=ci-42-1-abc123'
  ]);
});

test('passes environment by name only, never secret values', () => {
  const { args } = runContainer().call;
  assert.deepEqual(optionValues(args, '--env'), [
    'HOME=/tmp',
    'CI',
    'PLAYWRIGHT_BASE_URL',
    'PLAYWRIGHT_TLS_SELF_SIGNED',
    'E2E_USERNAME',
    'E2E_PASSWORD',
    'GESTIONALE_BOOTSTRAP_SUPER_ADMIN_USERNAME',
    'GESTIONALE_BOOTSTRAP_SUPER_ADMIN_PASSWORD',
    'PLAYWRIGHT_FIREFOX_DIAGNOSTIC_REPEATS'
  ]);
  assert.ok(!args.join(' ').includes(secret));
  assert.ok(!args.join(' ').includes('127.0.0.1:28081'));
});

test('runs as the invoking user', () => {
  const { args } = runContainer().call;
  const uid = spawnSync('id', ['-u'], { encoding: 'utf8' }).stdout.trim();
  const gid = spawnSync('id', ['-g'], { encoding: 'utf8' }).stdout.trim();
  assert.deepEqual(optionValues(args, '--user'), [`${uid}:${gid}`]);
});

for (const version of ['latest', '1.64.1-beta', '1.64.1;rm', '.1.64', '1..64', '']) {
  test(`invalid Playwright version rejected (${JSON.stringify(version)})`, () => {
    const result = runContainer({ version });
    assert.equal(result.status, 1);
    assert.equal(result.call, null);
  });
}

test('missing Playwright installation rejected', () => {
  const result = runContainer({ installed: false });
  assert.equal(result.status, 1);
  assert.equal(result.call, null);
});

for (const [project, runId] of [['gestionale-step43-demo', 'ci-1'], ['gestionale-prodlike-', 'ci-1'], ['gestionale-prodlike-abc', 'CI 1'], ['gestionale-prodlike-abc', '']]) {
  test(`foreign or invalid identifiers rejected (${project}, ${JSON.stringify(runId)})`, () => {
    const result = runContainer({ project, runId });
    assert.equal(result.status, 64);
    assert.equal(result.call, null);
  });
}

test('missing command rejected', () => {
  const result = runContainer({ command: [] });
  assert.equal(result.status, 2);
  assert.equal(result.call, null);
});

test('container failure propagates unchanged', () => {
  assert.equal(runContainer({ exitCode: 7 }).status, 7);
});
