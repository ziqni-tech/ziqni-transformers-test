package com.ziqni.transformer.test.store;

import com.ziqni.transformers.domain.BasicEventModel;
import org.junit.jupiter.api.Test;
import scala.None;
import scala.Option;
import scala.Some;
import scala.collection.JavaConverters;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EventsStoreTest {

    private final EventsStore eventsStore;

    public EventsStoreTest() {
        String accountId = "test-account";
        var ziqniStores = new ZiqniStores(accountId);
        ziqniStores.generateSampleData();
        this.eventsStore = ziqniStores.eventsStore;
    }

    @Test
    void pushEvent() {
        final var basicEventModel = new BasicEventModel(new Some<>("memb-2"), "test-member-ref-id-1", null, "test-event-1", new Some<>("1002"), "test-action", 2.0, null, null, null, null);
        final var productFuture = eventsStore.pushEvent(basicEventModel);
        final var eventResponse = productFuture.join();
        assertNotNull(eventResponse);
        assertTrue(eventResponse.getResults().size() > 0);
    }

    @Test
    void pushEventEmpty() {
        final var basicEventModel = new BasicEventModel(Option.empty(), "test-member-ref-id-1", null, "test-event-1", new Some<>("1002"), "test-action", 2.0, null, null, null, null);
        final var productFuture = eventsStore.pushEvent(basicEventModel);
        final var eventResponse = productFuture.join();
        assertNotNull(eventResponse);
        assertTrue(eventResponse.getResults().size() > 0);
    }

    @Test
    void pushEventTransaction() {
        final var basicEventModel = new BasicEventModel(new Some<>("memb-2"), "test-member-ref-id-1", null, "test-event-1", new Some<>("1002"), "test-action", 2.0, null, null, null, null);
        final var productFuture = eventsStore.pushEventTransaction(basicEventModel);
        final var eventResponse = productFuture.join();
        assertNotNull(eventResponse);
        assertTrue(eventResponse.getResults().size() > 0);
    }

    @Test
    void findByBatchId() {
        final var eventsStoreByBatchId = eventsStore.findByBatchId("1002");
        List<BasicEventModel> basicEventModels = eventsStoreByBatchId.join();
        assertNotNull(basicEventModels);
        assertTrue(basicEventModels.size() > 0);
    }

    @Test
    void makeMock() {
        final var eventTransaction = eventsStore.makeMock();
        assertNotNull(eventTransaction);
        assertNotNull(eventTransaction.getEvents());
    }
}