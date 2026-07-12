# ADR-007: Manual Deployment (No CI/CD Deploy Step)

## Status

Accepted

## Context

The CI pipeline (`.github/workflows/ci.yml`) builds, scans, and pushes Docker images to ECR on every push to main. A separate deploy workflow (`.github/workflows/deploy.yml`) exists but only retags ECR images for the target environment — it stops short of actually deploying to the server.

The original design envisioned a fully automated deploy step: SSH into EC2, pull new images, and restart containers. This was left as a TODO.

## Decision

Deployment remains manual via a local shell script (`infra/deploy.sh`). The GitHub Actions deploy workflow is not extended with an SSH deploy step.

### Reasons

1. **Ephemeral infrastructure.** The EC2 instance is stopped when not in use. Its public IP changes on every start (no Elastic IP). A CI/CD deploy step would need updating the target host on every session, negating the automation benefit.

2. **Secrets management overhead.** Automating SSH from CI requires storing the private key and host IP as GitHub Actions secrets. For a single-instance, occasional-use deployment, the security review and secret rotation burden outweighs the convenience.

3. **Low deployment frequency.** The application is in active development with infrequent production deployments. The manual `deploy.sh` script is a single command and takes under a minute.

4. **Simple rollback.** Manual deployment naturally gates changes — the developer verifies the build locally before running deploy.sh. Automated deployment increases the risk of pushing a bad build without review.

## Consequences

### Positive

- No CI secrets to manage (SSH keys, host IPs)
- No risk of automated deployment of a broken build
- Deploy script works regardless of instance IP changes
- Developer consciously reviews what is being deployed

### Negative

- Deployment is not reproducible from CI — requires a developer with the deploy script
- No deployment audit trail in GitHub Actions
- Cannot trigger production deployments from a mobile device or web interface
- Manual step can be forgotten

## Future Considerations

If deployment frequency increases or the team grows, the deploy step can be added to the GitHub Actions workflow with:

- An Elastic IP for the EC2 instance (static host)
- `EC2_SSH_KEY` and `EC2_HOST` GitHub secrets
- A workflow step running `docker-compose pull && docker-compose up -d` via SSH
