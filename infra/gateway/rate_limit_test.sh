#!/bin/bash
# rate_limit_test.sh

echo "🚀 Rate Limit 테스트 시작"

for i in {1..30}; do
    echo "요청 #$i: $(date)"
    curl -w "Status: %{http_code}, Time: %{time_total}s\n" \
         -o /dev/null -s \
         http://localhost:8000/api/member-service/auth/login \
         -H "Content-Type: application/json" \
         -d '{"email":"test@test.com","password":"password"}'
    sleep 0.1
done