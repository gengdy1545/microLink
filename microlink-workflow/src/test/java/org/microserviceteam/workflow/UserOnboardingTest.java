package org.microserviceteam.workflow;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.microserviceteam.workflow.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class UserOnboardingTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private WorkflowService workflowService;

    @Test
    public void testUserOnboardingFlow() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userId", 123L);

        // 1. 启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("user-onboarding-process-v2", variables);
        assertNotNull(processInstance);
        System.out.println("Started process instance: " + processInstance.getId());

        // 2. 办理人工审批任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskAssignee("admin")
                .singleResult();
        assertNotNull(task, "Admin approval task should exist");
        System.out.println("Completing task: " + task.getName());
        taskService.complete(task.getId());

        // 3. 验证流程是否结束，以及子流程是否启动 (通过 collectDeepInfo 验证)
        Map<String, Object> info = workflowService.collectDeepInfo(processInstance.getId());
        System.out.println("Flow execution info: " + info);
        
        assertNotNull(info.get("subProcessResults"), "Should have sub-processes started by message");
    }
}
