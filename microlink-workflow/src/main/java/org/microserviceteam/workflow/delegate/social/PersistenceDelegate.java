package org.microserviceteam.workflow.delegate.social;

import org.activiti.engine.delegate.DelegateExecution;
import org.microserviceteam.common.Result;
import org.microserviceteam.common.dto.search.ContentDoc;
import org.microserviceteam.workflow.client.SearchClient;
import org.microserviceteam.workflow.client.SocialClient;
import org.microserviceteam.workflow.delegate.BaseWorkflowDelegate;
import org.microserviceteam.workflow.service.WorkflowService;
import org.microserviceteam.workflow.util.ProcessVariableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component("persistenceDelegate")
public class PersistenceDelegate extends BaseWorkflowDelegate {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceDelegate.class);

    @Autowired
    private SocialClient socialClient;

    @Autowired
    private SearchClient searchClient;

    @Autowired
    private WorkflowService workflowService;

    @Override
    protected String run(DelegateExecution execution) throws Exception {
        String action = ProcessVariableUtil.getString(execution, "action", "UNKNOWN");
        String userId = ProcessVariableUtil.getString(execution, "userId", "anonymous");
        String instanceId = execution.getProcessInstanceId();

        logger.info(">>> [Persistence Service] Action: {}, Instance: {}", action, instanceId);

        if ("USER_ONBOARDING".equals(action)) {
            logger.info(">>> [Persistence Service] Building user index (Mock)...");
            return "User index built";
        } else if ("CONTENT_PUBLISH".equals(action)) {
            Long contentId = null;
            Object contentIdObj = execution.getVariable("contentId");
            if (contentIdObj != null) {
                contentId = Long.valueOf(contentIdObj.toString());
            }
            
            logger.info(">>> [Persistence Service] Syncing search index for content {}...", contentId);
            
            ContentDoc doc = new ContentDoc();
            try {
                Result<String> result = searchClient.indexContent(doc);
                logger.info(">>> [Persistence Service] Index sync called.");
            } catch (Exception e) {
                logger.error(">>> [Persistence Service] Index sync error: {}", e.getMessage());
            }
            return "Content index synced";
        } else {
            // Social logic (V2 messages)
            Result<Void> repoResult = socialClient.saveInteractionRecord(userId, action, instanceId, "content");
            
            Map<String, Object> subVars = new HashMap<>();
            subVars.put("userId", userId);
            subVars.put("action", action);
            workflowService.startByMessage("RECORD_STATS_MSG_V2", subVars, execution);
            workflowService.startByMessage("SEND_PUSH_MSG_V2", subVars, execution);
            
            return "Social persistence success";
        }
    }
}
