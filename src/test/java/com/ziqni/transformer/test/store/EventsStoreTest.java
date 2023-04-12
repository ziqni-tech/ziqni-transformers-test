package com.ziqni.transformer.test.store;

import com.ziqni.transformers.domain.BasicEventModel;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import scala.Option;
import scala.Some;
import scala.collection.JavaConverters;
import scala.collection.immutable.Seq;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        Seq<String> tags = JavaConverters.asScalaIteratorConverter(List.of("test-tags").iterator()).asScala().toSeq();
//        var metadata = JavaConverters.asScalaIteratorConverter(Collections.emptyIterator()).asScala().toSeq();
        final var basicEventModel = new BasicEventModel(new Some<>("memb-2"), "test-member-ref-id-1", "test-entity-ref-id", "test-event-1", new Some<>("1002"), "test-action", 2.0, DateTime.now(), tags, null, null);
        final var productFuture = eventsStore.pushEvent(basicEventModel);
        final var eventResponse = productFuture.join();
        assertNotNull(eventResponse);
        assertTrue(eventResponse.getResults().size() > 0);
    }

    @Test
    void pushEventTransaction() {
        Seq<String> tags = JavaConverters.asScalaIteratorConverter(List.of("test-tags").iterator()).asScala().toSeq();
        final var basicEventModel = new BasicEventModel(new Some<>("memb-2"), "test-member-ref-id-1", "test-entity-ref-id", "test-event-1", new Some<>("1002"), "test-action", 2.0, DateTime.now(), tags, null, null);
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