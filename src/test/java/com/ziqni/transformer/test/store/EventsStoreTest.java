package com.ziqni.transformer.test.store;

import com.ziqni.transformer.test.utils.ScalaUtils;
import com.ziqni.transformers.domain.ZiqniEvent;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import scala.Option;
import scala.Some;
import scala.collection.immutable.Map$;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventsStoreTest {

    private final EventsStore eventsStore;

    public EventsStoreTest() {
        var ziqniStores = new ZiqniStores(StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.eventsStore = ziqniStores.eventsStore;
    }

    @Test
    void pushEvent() {
        final var basicEvent = new ZiqniEvent(new Some<>("memb-2"), "test-member-ref-id-1", "1", "test-event-1", new Some<>("1002"), "test-action", 2.0, DateTime.now(), ScalaUtils.emptySeqString, Map$.MODULE$.empty(), null);
        final var future = eventsStore.pushEvent(basicEvent);
        final var eventResponse = future.join();
        assertNotNull(eventResponse);
        assertNotNull(eventResponse.getResults());
        assertFalse(eventResponse.getResults().isEmpty());
    }

    @Test
    void pushEventEmpty() {
        final var basicEvent = new ZiqniEvent(Option.empty(), "test-member-ref-id-1", "2", "test-event-1", new Some<>("1002"), "test-action", 2.0, DateTime.now(), ScalaUtils.emptySeqString, Map$.MODULE$.empty(), null);
        final var future = eventsStore.pushEvent(basicEvent);
        final var eventResponse = future.join();
        assertNotNull(eventResponse);
        assertNotNull(eventResponse.getResults());
        assertFalse(eventResponse.getResults().isEmpty());
    }

    @Test
    void pushEventTransaction() {
        final var basicEvent = new ZiqniEvent(new Some<>("memb-2"), "test-member-ref-id-1", "3", "test-event-1", new Some<>("1002"), "test-action", 2.0, DateTime.now(), ScalaUtils.emptySeqString, Map$.MODULE$.empty(), null);
        final var future = eventsStore.pushEventTransaction(basicEvent);
        final var eventResponse = future.join();
        assertNotNull(eventResponse);
        assertNotNull(eventResponse.getResults());
        assertFalse(eventResponse.getResults().isEmpty());
    }

    @Test
    void findByBatchId() {
        final var eventsStoreByBatchId = eventsStore.findByBatchId("1002");
        List<ZiqniEvent> basicEvents = eventsStoreByBatchId.join();
        assertNotNull(basicEvents);
        assertFalse(basicEvents.isEmpty());
    }

    @Test
    void makeMock() {
        final var eventTransaction = eventsStore.makeMock();
        assertNotNull(eventTransaction);
        assertNotNull(eventTransaction.getEvents());
    }
}