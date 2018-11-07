package concurrency;

import io.digitalstate.camunda.concurrency.concurrencyhelpers.ParallelUpdateMap;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class DataGenDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String superProcessInstanceId = execution.getSuperExecution().getProcessInstanceId();
        String processInstanceId = execution.getProcessInstanceId();
        // For a unique Key, the Super Process and sub-process IDs are used. But you can add your own modifier based on your needs
        // This is important for when you have multiple sets of data doing updates in the same sub-process
        String keyName = String.join("--",superProcessInstanceId, processInstanceId);

        // Uses putIfAbsent to ensure atomic update
        ParallelUpdateMap.concurrentMap.putIfAbsent(keyName, 100);
    }

}