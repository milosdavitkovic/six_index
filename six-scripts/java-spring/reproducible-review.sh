#!/usr/bin/env bash
set -Eeuo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repository_root="$(cd -- "$script_dir/../.." && pwd)"

base_url="${BASE_URL:-http://localhost:8080}"
index_code="${INDEX_CODE:-SMI}"
review_period="${REVIEW_PERIOD:-Q3-2026}"
output_directory="${OUTPUT_DIRECTORY:-target/reproducible-review}"

if [[ "$output_directory" = /* ]]; then
    output_path="$output_directory"
else
    output_path="$repository_root/$output_directory"
fi
mkdir -p "$output_path"

app_pid=""
cleanup() {
    if [[ -n "$app_pid" ]] && kill -0 "$app_pid" 2>/dev/null; then
        kill "$app_pid" 2>/dev/null || true
    fi
}
trap cleanup EXIT INT TERM

service_is_ready() {
    curl --silent --fail "$base_url/api/health" \
        --output /dev/null
}

if ! service_is_ready; then
    echo "No service detected at $base_url; starting the application..."
    bash "$repository_root/mvnw" spring-boot:run \
        >"$output_path/application.log" 2>&1 &
    app_pid=$!

    for attempt in {1..60}; do
        if service_is_ready; then
            break
        fi
        if ! kill -0 "$app_pid" 2>/dev/null; then
            echo "The application stopped before becoming ready. See $output_path/application.log" >&2
            exit 1
        fi
        sleep 1
    done

    if ! service_is_ready; then
        echo "The application did not become ready within 60 seconds. See $output_path/application.log" >&2
        exit 1
    fi
fi

curl --fail-with-body "$base_url/api/health" \
    --output "$output_path/health.json"

curl --fail-with-body -X POST "$base_url/api/import/all" \
    -F "spiUniverse=@$repository_root/data/spi_universe.csv" \
    -F "securityData=@$repository_root/data/sec_data.csv" \
    -F "composition=@$repository_root/data/composition.csv" \
    --output "$output_path/import.json"

curl --fail-with-body -X POST \
    "$base_url/api/index-reviews/$index_code/$review_period/run" \
    --output "$output_path/review-report.json"

echo "Reproducible review completed."
echo "Report: $output_path/review-report.json"
