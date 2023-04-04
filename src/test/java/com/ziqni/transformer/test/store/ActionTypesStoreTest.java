package com.ziqni.transformer.test.store;

import org.junit.jupiter.api.Test;
import scala.Some;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class ActionTypesStoreTest {

    private final ActionTypesStore actionTypesStore;

    public ActionTypesStoreTest() {
        String accountId = "test-account";
        this.actionTypesStore = new ActionTypesStore();
        ZiqniStores ziqniStores = new ZiqniStores(accountId);
        ziqniStores.generateSampleData();
    }


    @Test
    void actionTypeExists() throws ExecutionException, InterruptedException {
        final var actionType = actionTypesStore.actionTypeExists("action-1");
        assertNotNull(actionType);
        assertNotNull(actionType.get());
        assertTrue(actionType.get());
    }

    @Test
    void create() throws ExecutionException, InterruptedException {
        String action = "test-action-MMMT";
        final var actionType = actionTypesStore.create(action, new Some<>("test-action-11621628"), null, "test-unit-of-measure-11");
        assertNotNull(actionType);
        assertNotNull(actionType.get());
        assertNotNull(actionType.get().get());
        assertEquals(actionType.get().get().getExternalReference(), action);
    }

    @Test
    void update() throws ExecutionException, InterruptedException {
        String action = "test-action-MMMT";
        final var actionType = actionTypesStore.create(action, new Some<>("test-action-11621628"), null, "test-unit-of-measure-11");
        assertNotNull(actionType);
        assertNotNull(actionType.get());
        assertNotNull(actionType.get().get());
        assertEquals(actionType.get().get().getExternalReference(), action);
    }

    @Test
    void findActionTypeById() throws ExecutionException, InterruptedException {
        final var actionTypeByAction = actionTypesStore.findActionTypeByAction("action-1");
        assertNotNull(actionTypeByAction);
        assertNotNull(actionTypeByAction.get());
        assertNotNull(actionTypeByAction.get().get());
        assertNotNull(actionTypeByAction.get().get().getName());
    }

    @Test
    void makeMock() {
        final var actionTypeEntry = actionTypesStore.makeMock();
        assertNotNull(actionTypeEntry);
        assertNotNull(actionTypeEntry.getName());
    }
}