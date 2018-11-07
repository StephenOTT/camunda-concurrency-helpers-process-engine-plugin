package io.digitalstate.camunda.concurrency;

import io.digitalstate.camunda.concurrency.concurrencyhelpers.ParallelUpdateMap;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.camunda.bpm.engine.ProcessEngine;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrencyHelpersProcessEnginePlugin extends AbstractProcessEnginePlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrencyHelpersProcessEnginePlugin.class);

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        try {
            new ParallelUpdateMap();
            LOGGER.info("ParallelUpdateMap Concurrency Helper has been initialized.");
        } catch (Exception e){
            LOGGER.error("ParallelUpdateMap Concurrency Helper Failed to initialize.", e);
        }
    }

    @Override
    public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    }

    @Override
    public void postProcessEngineBuild(ProcessEngine processEngine) {
    }
}