# PQC Readiness Audit Action

Estimates how much effort a post-quantum cryptography migration will cost for a Java codebase, directly in your CI. The action runs the [pqc-readiness-auditor](https://github.com/Arpan0995/pqc-migration-readiness) against your source tree, flags quantum-vulnerable JCA/JCE usage and structural fragility (fixed-size buffers, concrete key-type coupling, protocol pinning), and uploads the results to GitHub code scanning so findings appear as pull-request annotations and Security-tab alerts.

Unlike crypto inventory tools or misuse detectors, the auditor answers a different question: not "where is the crypto?" but "what will replacing it cost?". Findings are effort estimates under the NIST IR 8547 deprecation timeline, not exploitable vulnerabilities, and are reported at severities that reflect that.

## Usage

```yaml
name: PQC readiness
on: [push, pull_request]

jobs:
  audit:
    runs-on: ubuntu-latest
    permissions:
      security-events: write   # needed to upload SARIF
      contents: read
    steps:
      - uses: actions/checkout@v4
      - uses: Arpan0995/pqc-readiness-action@v1
```

That is the whole setup. Reports (JSON, Markdown, SARIF) land in `audit-out/`, a ranked-hotspot summary is written to the job summary page, and the SARIF report is uploaded to code scanning.

## Inputs

| Input | Default | Description |
|---|---|---|
| `path` | `.` | Source root to scan, relative to the workspace. Scan the repository root so code-scanning alerts resolve to files. |
| `name` | repository name | Codebase label used in the reports. |
| `output-dir` | `audit-out` | Directory the reports are written to. |
| `auditor-version` | `1.2.0` | Auditor version to download from Maven Central. 1.1.0 is the earliest version with SARIF output. |
| `upload-sarif` | `true` | Upload the SARIF report to code scanning. Set to `false` on runners without the `security-events: write` permission (for example fork PRs). |

## Outputs

| Output | Description |
|---|---|
| `findings` | Total number of findings across all modules. |
| `sarif-file` | Workspace-relative path of the SARIF report. |
| `report-dir` | Workspace-relative path of the report directory. |

Use the `findings` output to gate a job yourself if you want a tripwire for newly introduced quantum-vulnerable crypto:

```yaml
      - uses: Arpan0995/pqc-readiness-action@v1
        id: audit
      - if: steps.audit.outputs.findings != '0'
        run: echo "::notice::${{ steps.audit.outputs.findings }} PQC migration findings; see the Security tab"
```

## How it works

The action downloads the self-contained auditor jar from Maven Central, verifies its SHA-256 checksum, and runs it with a Temurin JDK 21. The auditor is a syntactic, classpath-free scanner: it parses your source with JavaParser and never executes or builds your code. Severity mapping and detection rules are documented in the [detection rule catalog](https://github.com/Arpan0995/pqc-migration-readiness/blob/main/docs/research/02-detection-rule-catalog.md).

## License

Apache-2.0. The auditor itself is also Apache-2.0.
