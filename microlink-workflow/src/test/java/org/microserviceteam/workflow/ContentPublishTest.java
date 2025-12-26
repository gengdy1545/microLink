package org.microserviceteam.workflow;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.microserviceteam.workflow.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class ContentPublishTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private WorkflowService workflowService;

    @Test
    public void testContentPublishFlowSuccess() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("contentId", 456L);
        variables.put("autoCheckPassed", true); // 模拟审核通过

        // 1. 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("content-publish-process-v2", variables);
        assertNotNull(processInstance);
        System.out.println("Started process instance: " + processInstance.getId());

        // 2. 验证流程是否结束，以及子流程是否启动
        Map<String, Object> info = workflowService.collectDeepInfo(processInstance.getId());
        System.out.println("Flow execution info: " + info);
        
        // 验证执行路径中包含发布节点
        boolean published = ((java.util.List<Map<String, Object>>) info.get("executionPath")).stream()
                .anyMatch(m -> "publishTask".equals(m.get("node")));
        assertTrue(published, "Should have reached publishTask");
        
        assertNotNull(info.get("subProcessResults"), "Should have sub-processes started by message");
    }

    @Test
    public void testContentPublishFlowRejected() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("contentId", 789L);
        variables.put("autoCheckPassed", false); // 模拟审核失败

        // 1. 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("content-publish-process-v2", variables);
        assertNotNull(processInstance);

        // 2. 验证流程是否走向拒绝分支
        Map<String, Object> info = workflowService.collectDeepInfo(processInstance.getId());
        
        boolean rejected = ((java.util.List<Map<String, Object>>) info.get("executionPath")).stream()
                .anyMatch(m -> "endEventRejected".equals(m.get("node")));
        assertTrue(rejected, "Should have reached endEventRejected");
    }
}
