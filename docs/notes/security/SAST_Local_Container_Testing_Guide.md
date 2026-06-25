# 🔍 Static Application Security Testing (SAST) Local Container Guide

**Project Context:** `com.gnl.workhub` (Java 21 Source Code Analysis)

**Tooling Used:** Semgrep OSS Engine via Docker, Zsh Terminal

---

## 1. What is SAST and Why Run It in an Isolated Container?

**SAST (Static Application Security Testing)** evaluates your application's custom source code line-by-line before compilation. It aims to catch vulnerabilities, logical security flaws, or secrets written directly by developers.

### The Storage Dilemma

Installing SAST engines natively (e.g., via Homebrew) often introduces complex runtime dependencies (Python environments, system C-compilers, or Abstract Syntax Tree parsing tools) that can occupy anywhere from **200MB to 500MB** of your local space.

### The Container Solution

By running Semgrep directly inside an ephemeral Docker container, you can analyze your code using world-class security rules while maintaining a completely clean host filesystem.

---

## 2. Zero-Footprint Execution Blueprint

Open your Zsh terminal, navigate to your root `workhub` backend project folder, and run this targeted container wrapper command:

```bash
docker run --rm \
  --dns 8.8.8.8 \
  -v "$(pwd):/src" \
  semgrep/semgrep:latest \
  semgrep scan --config auto
```

Sample resulst
┌─────────────┐
│ Scan Status │
└─────────────┘
  Scanning 115 files tracked by git with 1059 Code rules:
                                                                                                                        
  Language      Rules   Files          Origin      Rules                                                                
 ─────────────────────────────        ───────────────────                                                               
  <multilang>      48     115          Community    1059                                                                
  java            118      71                                                                                           
  dockerfile        6       1                                                                                           
  bash              4       1                                                                                           
                                                                                                                        
                
                
┌──────────────┐
│ Scan Summary │
└──────────────┘
✅ Scan completed successfully.
 • Findings: 0 (0 blocking)
 • Rules run: 175
 • Targets scanned: 115
 • Parsed lines: ~99.9%
 • Scan skipped: 
   ◦ Files matching .semgrepignore patterns: 2
 • For a detailed list of skipped files and lines, run semgrep with the --verbose flag
Ran 175 rules on 115 files: 0 findings.
(need more rules? `semgrep login` for additional free Semgrep Registry rules)

### Technical Parameter Breakdown:

* `--rm`: **The Storage Cleaner.** The moment the security scan finishes printing logs to your Zsh terminal, Docker instantly destroys the live container runtime instance, clearing out all active system memory allocations.
* `-v "$(pwd):/src"`: Safely maps your local source folder into the container's isolated workspace (`/src`) as a read-only volume bridge without copying files into the Docker storage sub-layer.
* `semgrep/semgrep:latest`: Pulls the official, pre-packaged open-source static analysis execution image.

---

## 3. Saving Clean Markdown Reports For Later

If you want to bypass terminal screen scrolling constraints and keep a historical log of your findings to track code changes, redirect the output directly into a standard markdown document on your Mac using this pattern:

```bash
docker run --rm \
  -v "$(pwd):/src" \
  semgrep/semgrep:latest \
  semgrep scan --config auto --text -o /src/semgrep_report.md

```



---

## 4. Security Target Checklist (What It Hunts For)

When the Semgrep engine inspects your Spring Boot layer patterns, it executes structural regex checks to flag these four high-severity vulnerabilities:

| Vulnerability Target | Code Risk | Corrective Action |
| --- | --- | --- |
| **Hardcoded Secrets** | Exposing raw JWT keys or database passwords inside strings. | Extract string literals to `application.properties` and inject using environment variables. |
| **SQL Injection (SQLi)** | Concatenating unvalidated text data straight into dynamic queries. | Enforce parameterized queries or bind methods using Spring Data JPA frameworks natively. |
| **Weak Crypto Implementations** | Utilizing compromised hashing targets (like `MD5` or `SHA-1`) for storage. | Upgrade cryptographic security implementations to use strong options like `BCryptPasswordEncoder`. |
| **Loose Endpoint Controls** | Overly permissive routing rules (e.g., `.permitAll()`) exposing private actions. | Explicitly narrow resource paths down inside your master `SecurityFilterChain` bean. |

---

## 5. Total Disk Cleanup (Reclaiming Every Byte)

When your security auditing phase wraps up and you want to leave your Mac perfectly pristine, wipe the cached base engine image layers entirely off your computer with these commands:

```bash
# 1. Purge the Semgrep image layer
docker rmi semgrep/semgrep:latest

# 3. Clean up any lingering network bridges or intermediate layer fragments
docker system prune -f

```

Executing this routine clears 100% of the security footprint from your hard drive, leaving your development machine optimized and completely clean!