package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.UnitOfMeasureType;
import org.junit.jupiter.api.*;
import scala.Option;
import scala.Some;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class ActionTypesStoreTest {

    private final ActionTypesStore actionTypesStore;

    public ActionTypesStoreTest() {
        String accountId = "test-account";
        var ziqniStores = new ZiqniStores(accountId);
        ziqniStores.generateSampleData();
        this.actionTypesStore = ziqniStores.actionTypesStore;
    }


    @Test
    void actionTypeExists() throws ExecutionException, InterruptedException {
        actionTypesStore.findActionTypeByAction("action-1");
        final var actionType = actionTypesStore.actionTypeExists("action-1");
        actionType.join();
        assertNotNull(actionType);
        assertNotNull(actionType.get());
        assertTrue(actionType.get());
    }

    @Test
    void create() throws ExecutionException, InterruptedException {

        final var actionType = actionTypesStore.create("action-1-new", new Some<>("test-action-11621628"), null, "test-unit-of-measure-11");
        actionType.join();
        assertNotNull(actionType);
        assertNotNull(actionType.get());
        assertNotNull(actionType.get().get());
        assertEquals(actionType.get().get().getExternalReference(), "action-1-new");
    }

    @Test
    void update() throws ExecutionException, InterruptedException {
        String action = "test-action-MMMT";
        final var actionType = actionTypesStore.update(action, new Some<>("test-action-11621628"), Option.empty(), Option.empty());
        actionType.join();
        assertNotNull(actionType);
        assertNotNull(actionType.get());
        assertNotNull(actionType.get());
        assertEquals(actionType.get().getResults().size(), 1);
    }

    @Test
    void findActionTypeById() throws ExecutionException, InterruptedException {
        final var actionTypeByAction = actionTypesStore.findActionTypeByAction("action-1");
        actionTypeByAction.join();
        assertNotNull(actionTypeByAction.get());
        assertNotNull(actionTypeByAction.get().get());
        assertNotNull(actionTypeByAction.get().get().getName());
    }

    @Test
    void makeMock() {
        final var actionTypeEntry = actionTypesStore.makeMock(null);
        assertNotNull(actionTypeEntry);
        assertNotNull(actionTypeEntry.getName());
    }
}