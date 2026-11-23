#!/usr/bin/env bash
set -euo pipefail

if ! awslocal s3api head-bucket --bucket "$STORAGE_BUCKET" 2>/dev/null; then
  awslocal s3 mb "s3://$STORAGE_BUCKET"
fi
