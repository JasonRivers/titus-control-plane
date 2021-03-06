package com.netflix.titus.supplementary.relocation.startup;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.netflix.governator.guice.jersey.GovernatorJerseySupportModule;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.titus.common.runtime.TitusRuntime;
import com.netflix.titus.common.runtime.internal.DefaultTitusRuntime;
import com.netflix.titus.common.runtime.internal.LoggingSystemAbortListener;
import com.netflix.titus.common.util.archaius2.Archaius2ConfigurationLogger;
import com.netflix.titus.common.util.code.CodeInvariants;
import com.netflix.titus.common.util.code.CompositeCodeInvariants;
import com.netflix.titus.common.util.code.LoggingCodeInvariants;
import com.netflix.titus.common.util.code.SpectatorCodeInvariants;
import com.netflix.titus.runtime.TitusEntitySanitizerModule;
import com.netflix.titus.runtime.connector.agent.AgentDataReplicator;
import com.netflix.titus.runtime.connector.agent.replicator.DefaultAgentDataReplicator;
import com.netflix.titus.runtime.connector.eviction.EvictionDataReplicator;
import com.netflix.titus.runtime.connector.eviction.replicator.DefaultEvictionDataReplicator;
import com.netflix.titus.runtime.connector.jobmanager.JobDataReplicator;
import com.netflix.titus.runtime.connector.jobmanager.replicator.DefaultJobDataReplicator;
import com.netflix.titus.supplementary.relocation.evacuation.AgentInstanceEvacuator;

public class TaskRelocationMainModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Archaius2ConfigurationLogger.class).asEagerSingleton();
        bind(Registry.class).toInstance(new DefaultRegistry());

        install(new GovernatorJerseySupportModule());

        install(new TitusEntitySanitizerModule());
        install(new MasterConnectorModule());
        install(new TaskRelocationModule());

        bind(AgentInstanceEvacuator.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    public TitusRuntime getTitusRuntime(Registry registry) {
        CodeInvariants codeInvariants = new CompositeCodeInvariants(
                LoggingCodeInvariants.getDefault(),
                new SpectatorCodeInvariants(registry.createId("titus.runtime.invariant.violations"), registry)
        );
        return new DefaultTitusRuntime(codeInvariants, LoggingSystemAbortListener.getDefault(), registry);
    }
}
