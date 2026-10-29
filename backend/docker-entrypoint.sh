#!/bin/sh
set -eu

mkdir -p /data
chown cronos:cronos /data

exec su-exec cronos java -XX:MaxRAMPercentage=75 -jar /app/app.jar
