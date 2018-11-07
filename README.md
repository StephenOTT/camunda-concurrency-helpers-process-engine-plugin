# Camunda Concurrency Helpers Process Engine Plugin

A plugin providing helpers for in-flight processes to perform common concurrency actions.

# Install the Plugin

## As a dependency

Add JitPack as a repository source in your build file.

If you are using Maven, then add the following to your pom.xml

```xml
<project>
...
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
...
```

This snippet will enable Maven dependency download directly from Github.com

Then add the following dependency:

```xml
...
 <dependency>
    <groupId>com.github.StephenOTT</groupId>
    <artifactId>camunda-concurrency-helpers-process-engine-plugin</artifactId>
    <version>v0.0.0</version>
 </dependency>
 ...
 ```
❗️ See the Releases for the latest version number ❗️

# Concurrency Data Aggregation (Concurrent HashMap)

This helper is a simple static ConcurrentHashMap that provides a in-memory storage space outside of the process execution.

The helper is found in `io.digitalstate.camunda.concurrency.concurrencyhelpers.ParallelUpdateMap`

## example usage for Java Delegate

### Adding data into the Map

```java
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
```

### Aggregating Data from the Map

```java
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
```

See the ConcurrencyMap1.java unit test for further details.

### getValues() Method

The `ParallelUpdateMap.getValues("someString")` static method is provided which has a String input for providing a "key" value which will be used for a "Contains" search.

The function is as follows:

```java
    public static synchronized List<Object> getValues(String keyContains){
        List<Object> result = concurrentMap.entrySet()
                .stream()
                .filter(e -> e.getKey().contains(keyContains))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        return result;
    }
```

The result is a List of objects based on the `keyContains` string value