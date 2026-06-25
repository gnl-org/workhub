#### 1. What is K6?

Grafana K6 is a developer-centric, open-source load testing tool used to simulate concurrent users (VUs) hitting your application endpoints to measure performance, throughput, and reliability under stress. Scripts are written in **JavaScript**.

#### 2. Core Concepts & Terminology

* **Virtual Users (VUs):** Discrete concurrent execution loops simulating real-world users.
* **Duration:** The total timeframe a specific test scenario runs.
* **Throughput (`http_reqs`):** The total number of HTTP requests processed per second.
* **Latencies (`http_req_duration`):** * `avg`: The mathematical mean (can hide spikes).
* `med`: The middle metric value.
* `p(90)` / `p(95)`: The **Percentiles**. A `p(95) = 10.78ms` means 95% of all users received their responses in under 11 milliseconds. **Always track percentiles rather than averages to identify spikes.**


* **Thresholds:** Strict PASS/FAIL criteria applied to metrics (e.g., `'p(95)<150'` guarantees that if 5% of responses exceed 150ms, the overall test run is marked as failed).

---

#### 3. Standard Running Workflow

Before validating your load tests, remember to clear out any state layers (like local Redis cache data blocks) that could distort performance results:

```bash
# Wipes local test container state cleanly
docker exec -it projecthub-cache redis-cli flushall

```

To execute a test script pipeline locally from your terminal, run:

```bash
k6 run stress_test.js

```

To run a rapid, low-impact sanity test (e.g., 1 Virtual User for 5 seconds) before scaling up your virtual load:

```bash
k6 run --vus 1 --duration 5s stress_test.js

```

---

#### 4. Reading a K6 Summary Report

When reviewing console execution feedback, scan for these three primary verification metrics:

* **`http_req_failed`**: Should sit perfectly at **`0.00%`**. Any higher indicates the application layer is dropping requests or returning internal server errors (HTTP 500s) under pressure.
* **`checks_succeeded`**: Verifies that custom assertions inside the script (like checking for an HTTP status `200`) passed completely.
* **`max` / `p(95)**`: Confirms that response times are stable and flat even at peak usage numbers, proving your optimization layer (e.g., caching database payloads directly in RAM) is scaling predictably.