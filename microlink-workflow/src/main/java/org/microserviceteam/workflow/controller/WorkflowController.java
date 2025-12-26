package org.microserviceteam.workflow.controller;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.microserviceteam.common.Result;
import org.microserviceteam.workflow.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private TaskService taskService;

    @PostMapping("/start/{processKey}")
    public Result<Map<String, Object>> start(@PathVariable String processKey,
                                             @RequestBody Map<String, Object> variables) {
        Map<String, Object> processDeepInfo = workflowService.start(processKey, variables);
        return Result.success(processDeepInfo);
    }

    @PostMapping("/message/{messageName}")
    public Result<Map<String, Object>> startByMessage(@PathVariable String messageName,
                                                      @RequestBody Map<String, Object> variables) {
        Map<String, Object> result = workflowService.startByMessage(messageName, variables, null);
        return Result.success(result);
    }

    @GetMapping("/tasks/{assignee}")
    public Result<List<Map<String, String>>> getTasks(@PathVariable String assignee) {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).list();
        List<Map<String, String>> result = tasks.stream().map(t -> {
            Map<String, String> m = new HashMap<>();
            m.put("taskId", t.getId());
            m.put("taskName", t.getName());
            m.put("processInstanceId", t.getProcessInstanceId());
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @PostMapping("/tasks/complete/{taskId}")
    public Result<String> completeTask(@PathVariable String taskId, @RequestBody(required = false) Map<String, Object> variables) {
        if (variables == null) {
            taskService.complete(taskId);
        } else {
            taskService.complete(taskId, variables);
        }
        return Result.success("Task completed: " + taskId);
    }
}
