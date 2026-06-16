#!/usr/bin/env bash
# run-baseline.sh
# Reproduceerbare baseline-meting voor onderhoudbaarheid (bulletpoint 2).
# Bron: docs/onderhoudbaarheid/02-teststrategie.md
#
# Gebruik:
#   ./scripts/run-baseline.sh                 # volledige run (T1 + T2 + T3)
#   SKIP_PIT=1 ./scripts/run-baseline.sh       # alleen unit + coverage (sneller)
#   PIT_ONLY=1 ./scripts/run-baseline.sh       # alleen mutation (na al gedraaide verify)
#
# Output:
#   - target/surefire-reports/        unit-test XML
#   - target/site/jacoco/             coverage HTML/XML/CSV
#   - target/pit-reports/             mutation HTML/XML/CSV
#   - docs/onderhoudbaarheid/raw/tests/baseline-<datum>.txt    samenvatting

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API_DIR="${REPO_ROOT}/openmrs-module-appointmentscheduling/openmrs-module-appointmentscheduling/api"
RAW_DIR="${REPO_ROOT}/docs/onderhoudbaarheid/raw/tests"
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT="${RAW_DIR}/baseline-${STAMP}.txt"

mkdir -p "${RAW_DIR}"
cd "${API_DIR}"

echo "[run-baseline] API_DIR=${API_DIR}"
echo "[run-baseline] OUT=${OUT}"
echo "[run-baseline] mvn $(mvn -v | head -1)"
{
  echo "# Baseline-meting onderhoudbaarheid"
  echo "Datum: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo "Host:  $(uname -srm)"
  echo "Java:  $(java -version 2>&1 | head -1)"
  echo "Mvn:   $(mvn -v | head -1)"
  echo
} > "${OUT}"

if [[ "${PIT_ONLY:-0}" != "1" ]]; then
  echo "[run-baseline] T1+T2: mvn clean verify"
  mvn -B clean verify | tee -a "${OUT}"
fi

if [[ "${SKIP_PIT:-0}" != "1" ]]; then
  echo "[run-baseline] T3: mvn pitest:mutationCoverage"
  mvn -B org.pitest:pitest-maven:mutationCoverage | tee -a "${OUT}"
fi

{
  echo
  echo "# --- Samenvatting (geparseerd) ---"
  echo
  echo "## Unit tests (Surefire)"
  if compgen -G "target/surefire-reports/TEST-*.xml" > /dev/null; then
    grep -hE "tests=|errors=|failures=|skipped=" target/surefire-reports/TEST-*.xml \
      | head -1 \
      || echo "(geen samenvattingsregel gevonden)"
  else
    echo "(geen Surefire-rapport gevonden — tests niet gedraaid)"
  fi

  echo
  echo "## Coverage (JaCoCo)"
  if [[ -f target/site/jacoco/jacoco.csv ]]; then
    echo "Per package (LINE_COVERED, LINE_MISSED, BRANCH_COVERED, BRANCH_MISSED):"
    awk -F, 'NR>1 {printf "  %-60s LINE %d/%d  BRANCH %d/%d\n", $2"/"$3, $9, $8+$9, $7, $6+$7}' target/site/jacoco/jacoco.csv
  else
    echo "(geen JaCoCo-CSV gevonden)"
  fi

  echo
  echo "## Mutation testing (PIT)"
  if [[ -f target/pit-reports/mutations.xml ]]; then
    KILLED=$(grep -c 'status="KILLED"'  target/pit-reports/mutations.xml || true)
    SURV=$(grep -c   'status="SURVIVED"' target/pit-reports/mutations.xml || true)
    NOTCOV=$(grep -c 'status="NO_COVERAGE"' target/pit-reports/mutations.xml || true)
    TOTAL=$((KILLED + SURV + NOTCOV))
    if (( TOTAL > 0 )); then
      SCORE=$(awk -v k="$KILLED" -v t="$TOTAL" 'BEGIN{printf "%.1f", (k*100)/t}')
      echo "  KILLED=$KILLED  SURVIVED=$SURV  NO_COVERAGE=$NOTCOV  TOTAL=$TOTAL  SCORE=${SCORE}%"
    else
      echo "(geen mutations geparseerd)"
    fi
  else
    echo "(geen PIT-rapport gevonden — mutation testing niet gedraaid of overgeslagen)"
  fi
} | tee -a "${OUT}"

echo
echo "[run-baseline] Baseline geschreven naar: ${OUT}"
