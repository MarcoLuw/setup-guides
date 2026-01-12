#!/bin/bash
set -eu

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_TOOL_DIR="/engn001/install/clickhouse-backup"

sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://packages.clickhouse.com/rpm/clickhouse.repo
sudo yum install -y clickhouse-server-25.10.2.65 clickhouse-client-25.10.2.65

sudo systemctl enable clickhouse-server
sudo systemctl start clickhouse-server
# sudo systemctl status clickhouse-server

clickhouse-client --multiquery < "$ROOT_DIR/schema.sql"
clickhouse-client --multiquery < "$ROOT_DIR/data.sql"

# install clickhouse-backup
sudo mkdir -p $BACKUP_TOOL_DIR
sudo wget https://github.com/Altinity/clickhouse-backup/releases/download/v2.6.41/clickhouse-backup-linux-amd64.tar.gz -O $BACKUP_TOOL_DIR/clickhouse-backup.tar.gz
sudo tar -xvf $BACKUP_TOOL_DIR/clickhouse-backup.tar.gz -C $BACKUP_TOOL_DIR
sudo rm -f $BACKUP_TOOL_DIR/clickhouse-backup.tar.gz
sudo cp $BACKUP_TOOL_DIR/build/linux/amd64/clickhouse-backup /usr/local/bin/
sudo chmod +x /usr/local/bin/clickhouse-backup
if command -v clickhouse-backup >/dev/null 2>&1; then
    echo "clickhouse-backup installed successfully"
else
    echo "clickhouse-backup installation failed"
    exit 1
fi