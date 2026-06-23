# Backend Performance & JVM Diagnostics

This document tracks performance benchmarking, memory profiling, and database connection pool tuning conducted on **Project Workhub** (`com.gnl.workhub`).

---

## 1. Quick Reference: JVM & Connection Pool Settings

### IntelliJ VM Options for VisualVM Profiling

Add the following parameters to your Run Configuration to unlock real-time telemetry over port `9010`:

```text
-Xms128m -Xmx256m -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false

```

### Application Properties Defaults vs. Lab Limits

| Parameter | Default (16GB Mac) | Lab Settings | Configuration File |
| --- | --- | --- | --- |
| **Max Heap Size (`-Xmx`)** | ~4 GB ($\sim 25\%$ of RAM) | **`256 MB`** | IntelliJ VM Options |
| **Tomcat Max Threads** | `200` | **`100`** | `application.properties` |
| **HikariCP Pool Size** | `10` | **`2`** (Optimal) vs **`25`** | `application.properties` |

---

## 2. Core Performance Metrics (`ab` Output)

Evaluate system health using these three terminal data points from your ApacheBench summary:

* **Failed Requests:** Must be **0**. Non-zero values indicate application crashes.
* **Requests per second:** Higher is better. Measures raw system throughput capacity.
* **100% (Tail Latency):** Lower is better. The absolute slowest request execution time.

---

## 3. Core Insights & Diagnostic Patterns

### Memory Profiling & Leaks

* **Healthy Lifecycle:** Memory exhibits a clean **sawtooth pattern** in VisualVM. It climbs during requests and drops sharply during standard Garbage Collection (GC).
* **The Static Leak Bug:** Appending data chunks to a **`static` collection** prevents the GC from reclaiming memory. This causes the baseline heap floor to rise continuously until the server throws a fatal `java.lang.OutOfMemoryError` and freezes Tomcat's worker threads (`Acceptor`/`Poller`).

### The Resource Trade-Offs

#### 1. Memory Constraints (Small Heap)

Dropping max heap to `-Xmx256m` keeps the application stable under traffic but **doubles tail latency** (e.g., from $182\text{ ms}$ to $388\text{ ms}$).

* *Reason:* The JVM must execute frequent **Stop-The-World (STW) GC passes** to free up space, burning CPU cycles to save RAM.

#### 2. The Connection Pool Paradox

* **Choked Pool (`maximum-pool-size=2`):** **738.14 req/sec** | Max Latency: **247 ms**
* **Over-provisioned Pool (`maximum-pool-size=25`):** **684.48 req/sec** | Max Latency: **355 ms**
* *Reason:* Allocating more connections than the CPU can handle degrades performance due to **OS context switching overhead** and disk I/O contention. Limiting the pool size allows sequential, high-speed execution with minimal lock overhead.