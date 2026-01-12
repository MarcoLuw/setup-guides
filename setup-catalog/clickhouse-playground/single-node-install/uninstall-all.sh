#!/bin/bash
set -euo pipefail

BACKUP_TOOL_DIR="/engn001/install/clickhouse-backup"

echo "Stopping ClickHouse service (if running)..."
sudo systemctl stop clickhouse-server || true
sudo systemctl disable clickhouse-server || true

echo "Removing ClickHouse packages..."
sudo yum remove -y \
  clickhouse-server-25.10.2.65 \
  clickhouse-client-25.10.2.65 || true

echo "Removing ClickHouse data, config, and logs..."
sudo rm -rf \
  /var/lib/clickhouse \
  /etc/clickhouse-server \
  /var/log/clickhouse-server

echo "Removing clickhouse-backup binary..."
sudo rm -f /usr/local/bin/clickhouse-backup

echo "Removing clickhouse-backup install directory..."
sudo rm -rf "$BACKUP_TOOL_DIR"

echo "Uninstall completed successfully."

# Validate removal
command -v clickhouse-backup || echo "clickhouse-backup removed"
rpm -qa | grep clickhouse || echo "ClickHouse RPMs removed"
systemctl list-unit-files | grep clickhouse || echo "ClickHouse service removed"
echo "All ClickHouse components have been uninstalled."