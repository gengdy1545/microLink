package org.microserviceteam.workflow.delegate.content;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("contentRejectDelegate")
public class ContentRejectDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Object contentId = execution.getVariable("contentId");
        System.out.println("Content rejected: " + contentId);
        // 这里可以更新内容状态为 REJECTED
    }
}
