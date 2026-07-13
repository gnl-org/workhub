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
SSH_KEY=${SSH_KEY:-~/.ssh/workhub-key.pem}
FRONTEND_DIR="$(dirname "$0")/../workhub-frontend"

echo "Building frontend..."
cd "$FRONTEND_DIR"
npm run build

echo "Deploying to $HOST..."
rsync -avz -e "ssh -i $SSH_KEY" dist/ $SSH_USER@$HOST:/app/frontend/

echo "Reloading Nginx..."
ssh -i $SSH_KEY $SSH_USER@$HOST "sudo systemctl reload nginx"

echo "Frontend deployed!"
