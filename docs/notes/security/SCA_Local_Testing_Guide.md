# 📦 Software Composition Analysis (SCA) Local Testing Guide

**Project Context:** `com.gnl.workhub` (Java 21 / Maven Dependency Auditing)

**Tooling Used:** Aqua Security Trivy via Docker, Zsh Terminal

---

## 1. What is SCA and Why Run It Locally?

While **DAST** (OWASP ZAP) attacks your application from the outside at runtime, **SCA** (Software Composition Analysis) inspects your application from the inside out before it runs.

Modern software relies heavily on open-source packages. SCA doesn't scan the code *you* wrote; it scans **the code written by other people** that your project imports via your `pom.xml`. It looks for known public vulnerabilities (Common Vulnerabilities and Exposures - CVEs) buried inside your direct and transitive dependency trees.

Running it locally allows you to audit your third-party libraries instantly and resolve dependency conflicts before pushing code to a shared repository.

---

## 2. Why the National Vulnerability Database (NVD) Failed Us

Initially, we attempted to use the industry-standard **OWASP Dependency-Check** tool, which relies directly on the **National Vulnerability Database (NVD)** hosted by NIST. This path failed for two technical reasons:

1. **Monolithic Data Synchronization:** The NVD architecture requires client tools to download a massive, historical database dump locally before running a scan.
2. **Aggressive Aggregated Rate Limiting:** Due to heavy traffic, the NVD API severely limits unauthenticated traffic. Without registering for an explicit NVD API Key, the synchronization process heavily throttles the download connection. This causes local container scans to freeze for up to an hour or completely time out over your network line.

### The Alternative: Aqua Security Trivy & The GitHub Advisory Database

To bypass the NVD bottlenecks, we pivoted to **Trivy**, a cloud-native security engine.

Instead of connecting to the monolithic NVD, Trivy pulls a highly compressed, light, document-based signature index from the **GitHub Advisory Database**. Because this database structure uses highly optimized key-value map diffs instead of long SQL relational tables, it bypasses heavy network throttling and executes the entire scan seamlessly without requiring an API key.

---

## 3. Handling the Maven Central `429 Too Many Requests` Error

During our first Trivy run, the scanner attempted to resolve your dependency tree by fetching package metadata directly from the internet, triggering a **429 Rate Limit** block from Central Maven:

```text
FATAL Error remote Maven repository returned 429 Too Many Requests... Retry-After: 1800

```

### The Fix: Mounting the Local `.m2` Cache

To solve this, we updated our Docker runtime arguments to mount your Mac's host machine Maven repository (`~/.m2`) right inside the container. This allowed Trivy to read the `.pom` files locally from your hard drive instead of generating outbound network requests.

### The Complete Local Zsh Execution Command:

```bash
docker run --rm \
  -v $(pwd):/root \
  -v "$HOME/.m2:/root/.m2" \
  -v /tmp/trivy-cache:/root/.cache/ \
  aquasec/trivy:latest fs /root/pom.xml

```

#### Flag Breakdown:

* `--rm`: Automatically destroys the container instance when the scan terminates to save system memory.
* `-v $(pwd):/root`: Binds your current working `workhub` project directory to the container workspace.
* `-v "$HOME/.m2:/root/.m2"`: Bridges your local Mac Maven cache folder to bypass internet fetches.
* `-v /tmp/trivy-cache:/root/.cache/`: Maps a 100-200MB directory on your Mac to store the compressed vulnerability definitions, speeding up subsequent runs to under 2 seconds.

---

## 4. How to Manage and Delete the Trivy Cache

The `-v /tmp/trivy-cache:/root/.cache/` flag maps a directory on your Mac to speed up subsequent scans. Over time, or if database tables get corrupted, you may want to check its disk footprint or clear it entirely.

### View the current disk space used:

```bash
du -sh /tmp/trivy-cache

```

### Safely delete the cache directory:

Because this data lives in your Mac's root volatile `/tmp` namespace, your operating system may naturally sweep it during hard system reboots. However, to drop it manually and force Trivy to pull a fresh set of security vulnerabilities next time, execute this command in your Zsh terminal:

```bash
rm -rf /tmp/trivy-cache

```

---

## 5. Analyzing the Scan Output & Remediation Strategy

A typical scan on a Spring Boot application will return vulnerabilities flagged across a spectrum from `LOW` to `CRITICAL`.

### Transitive vs. Direct Dependencies

If your report flags a library you didn't explicitly add to your `pom.xml` (like `com.fasterxml.jackson.core:jackson-databind`), it is a **transitive dependency**—a library that one of your primary Spring Boot Starters pulled in automatically under the hood.

### The Fixing Blueprint

#### Strategy A: Maven Property Overrides (The Surgical Fix)

Avoid explicitly declaring transitive libraries as direct dependencies to fix security bugs, as this can destabilize version management. Instead, override the specific version property recognized by the parent Spring Boot framework inside your `<properties>` block:

```xml
<properties>
    <java.version>21</java.version>
    <jackson.version>2.21.4</jackson.version>
</properties>

```

#### Strategy B: Parent Framework Bumping (The Global Fix)

If multiple core libraries (Tomcat, Jackson, Netty) show vulnerabilities, the optimal solution is to update the parent Spring Boot starter version to the latest stable patch version inside the release train:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.2</version> </parent>

```

---

## 6. Post-Remediation Verification Loop

Every time you modify your `pom.xml` to fix a vulnerability, run this verification loop in your terminal to confirm your changes successfully dropped the vulnerability count:

```bash
# 1. Clear old targets and resolve the newly declared versions
mvn clean dependency:resolve

# 2. Re-run Trivy to confirm the vulnerabilities are cleared
docker run --rm -v $(pwd):/root -v "$HOME/.m2:/root/.m2" -v /tmp/trivy-cache:/root/.cache/ aquasec/trivy:latest fs /root/pom.xml

```