package concurrency;

import io.digitalstate.camunda.concurrency.concurrencyhelpers.ParallelUpdateMap;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import org.camunda.bpm.engine.test.assertions.JobAssert;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class ConcurrencyMap1 {

    @Rule
    public ProcessEngineRule rule = new ProcessEngineRule("camunda_config/camunda.cfg.xml");

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void shouldExecuteProcess() {
        // Given we create a new process instance
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcessParent");

        // Ensure there are 10 jobs that were created
        List<Job> jobs = managementService().createJobQuery()
                .processDefinitionKey("testProcessChild")
                .list();
        assertThat(jobs.size() == 10);

        // Ensure that there were 10 sub-process instances created
        List<ProcessInstance> callActInstance = runtimeService().createProcessInstanceQuery()
                .processDefinitionKey("testProcessChild")
                .superProcessInstanceId(processInstance.getProcessInstanceId())
                .list();
        assertThat(callActInstance.size() == 10);

        // Execute the jobs for each sub-process instance
        callActInstance.forEach(instance -> {
            execute(job(instance));
        });

        // Print the value of finalResult
        String variableValue =  historyService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("finalResult")
                .singleResult()
                .getValue()
                .toString();
        System.out.println("finalResult Value: " + variableValue);

        // Print the "sum" value
        String sumValue =  historyService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("sum")
                .singleResult()
                .getValue()
                .toString();
        System.out.println("Sum Value: " + sumValue);

        // Print the current ParallelUpdateMap value: It should be empty
        System.out.println("ParallelUpdateMap Contains: " + ParallelUpdateMap.concurrentMap.toString());
        assertThat(ParallelUpdateMap.concurrentMap.isEmpty());

        // The process should have completed
        assertThat(processInstance).isEnded();
    }

}