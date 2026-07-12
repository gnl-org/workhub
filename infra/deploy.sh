#!/bin/bash
set -e

usage() {
    echo "Usage: $0 <ec2-host>"
    echo "Example: $0 ec2-xx-xx-xx-xx.ap-south-2.compute.amazonaws.com"
    exit 1
}

if [ $# -ne 1 ]; then
    usage
fi

HOST=$1
SSH_USER=${SSH_USER:-ec2-user}
SSH_KEY=${SSH_KEY:-~/.ssh/workhub.pem}

echo "Deploying to $HOST..."

rsync -avz -e "ssh -i $SSH_KEY" \
    --exclude '.env' \
    --exclude 'core-uploads' \
    infra/ \
    $SSH_USER@$HOST:/app/infra/

ssh -i $SSH_KEY $SSH_USER@$HOST << 'EOF'
    set -e
    aws ecr get-login-password --region ap-south-2 | docker login --username AWS --password-stdin 258227418027.dkr.ecr.ap-south-2.amazonaws.com
    cd /app/infra

    if [ ! -f .env ]; then
        echo "ERROR: /app/infra/.env not found on EC2."
        echo "SSH in and set it up manually:"
        echo "  vi /app/infra/.env"
        echo "  (Use the .env.example file as a template)"
        echo "Then re-run this deploy script."
        exit 1
    fi

    docker-compose pull
    docker-compose up -d --remove-orphans

    echo "Deploy complete!"
    docker ps
EOF
