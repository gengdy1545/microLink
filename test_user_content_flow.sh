#!/bin/bash

BASE_URL="http://localhost:8085"

echo "=== 1. 刷新并部署所有流程定义 (V2) ==="
# 注意：我们手动部署，因为自动部署可能被禁用或存在冲突
curl -X POST "$BASE_URL/workflow/manage/flush-all"
echo -e "\n"

echo "=== 2. 测试 User Onboarding 流程 (V2) ==="
echo "启动流程..."
START_USER_RESP=$(curl -s -X POST "$BASE_URL/workflow/start/user-onboarding-process-v2" \
  -H "Content-Type: application/json" \
  -d '{"userId": 1001, "username": "testuser"}')
echo "响应: $START_USER_RESP"

echo "查询 admin 的待办任务..."
TASKS_RESP=$(curl -s -X GET "$BASE_URL/workflow/tasks/admin")
echo "待办任务: $TASKS_RESP"

# 提取任务 ID (兼容 code/data 结构)
TASK_ID=$(echo $TASKS_RESP | python3 -c "import sys, json; data=json.load(sys.stdin).get('data', []); print(data[0]['taskId'] if data else '')")

if [ -n "$TASK_ID" ]; then
  echo "审批任务 (ID: $TASK_ID)..."
  curl -X POST "$BASE_URL/workflow/tasks/complete/$TASK_ID"
  echo -e "\n审批完成，流程将流转到 Activate User 并触发索引/推送消息。"
else
  echo "未找到待办任务，请检查流程部署状态。"
fi

echo -e "\n=== 3. 测试 Content Publish 流程 (V2) ==="
echo "启动流程 (模拟自动审核通过)..."
START_CONTENT_RESP=$(curl -s -X POST "$BASE_URL/workflow/start/content-publish-process-v2" \
  -H "Content-Type: application/json" \
  -d '{"contentId": 2001, "title": "Hello World", "content": "This is a test content", "autoCheckPassed": true}')
echo "响应: $START_CONTENT_RESP"
echo -e "\n流程将自动流转到 Publish Content 并触发索引/推送消息。"

echo -e "\n=== 测试完成 ==="
