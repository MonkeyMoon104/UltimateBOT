package com.monkey.mcbot.wrapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.junit.jupiter.api.Test;

class WrapperTaskTest {

    @Test
    void cancelsFoliaTasksThroughTheirPublicInterface() {
        ScheduledTask scheduledTask = mock(ScheduledTask.class);

        WrapperTask.reflective("folia-test", scheduledTask).cancel();

        verify(scheduledTask).cancel();
    }
}
