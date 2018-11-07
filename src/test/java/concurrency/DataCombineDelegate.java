package concurrency;

import io.digitalstate.camunda.concurrency.concurrencyhelpers.ParallelUpdateMap;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.List;

public class DataCombineDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();

        // Save the array of values as a string into Camunda DB Variables:
        // In practice you would perform whatever merging you like
        // (such as merging a JSON objects into a Array of Json Objects)
        List<Object> values = ParallelUpdateMap.getValues(processInstanceId);
        execution.setVariable("finalResult", values.toString());

        // Reduce a sum of all the values:
        int sum  = (int)values.stream().reduce(0,(x,y) -> (int)x +(int)y);
        execution.setVariable("sum", sum);

        // Clear out the map of the old values
        ParallelUpdateMap.concurrentMap.entrySet()
                .removeIf(e-> e.getKey().contains(processInstanceId));
    }

}