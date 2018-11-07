package io.digitalstate.camunda.concurrency.concurrencyhelpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ParallelUpdateMap {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelUpdateMap.class);

    public static ConcurrentHashMap<String, Object> concurrentMap = new ConcurrentHashMap<>();

    public static synchronized List<Object> getValues(String keyContains){
        List<Object> result = concurrentMap.entrySet()
                .stream()
                .filter(e -> e.getKey().contains(keyContains))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        return result;
    }
}
