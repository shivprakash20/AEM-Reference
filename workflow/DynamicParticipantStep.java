package com.adobe.core.workflows;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.ParticipantStepChooser;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

/**
 * @author Shiv Prakash
 */

@Component(service = ParticipantStepChooser.class,  property = {Constants.SERVICE_DESCRIPTION + "= Venture Wave Participant Chooser Step",
        Constants.SERVICE_VENDOR + "= venturewave.com",
        "chooser.label" + "= Venture Participant Chooser Step"})
public class VentureParticipantChooserStep  implements ParticipantStepChooser{

    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {

        return workItem.getWorkflow().getInitiator();
    }
}
