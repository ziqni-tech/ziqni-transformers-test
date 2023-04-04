package com.ziqni.transformer.test.store;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class AwardStoreTest {

    private RewardStore rewardStore;
    private final AwardStore awardStore;
    public AwardStoreTest() {
        String accountId = "test-account";
        this.rewardStore = new RewardStore();
        this.awardStore = new AwardStore(rewardStore);
        final var ziqniStores = new ZiqniStores(accountId);
        ziqniStores.generateSampleData();
    }

    @Test
    void getBasicAward() throws ExecutionException, InterruptedException {
        final var actionTypeByAction = awardStore.getBasicAward("award-1");
        assertNotNull(actionTypeByAction);
        assertNotNull(actionTypeByAction.get());
        assertNotNull(actionTypeByAction.get().get());
        assertNotNull(actionTypeByAction.get().get().getRewardId());
    }

    @Test
    void getAward() throws ExecutionException, InterruptedException {
        final var actionTypeByAction = awardStore.getAward("award-1");
        assertNotNull(actionTypeByAction);
        assertNotNull(actionTypeByAction.get());
        assertNotNull(actionTypeByAction.get().get());
        assertNotNull(actionTypeByAction.get().get().getRewardId());
    }

    @Test
    void makeMock() {
        final var actionTypeEntry = awardStore.makeMock(rewardStore.makeMock());
        assertNotNull(actionTypeEntry);
        assertNotNull(actionTypeEntry.getId());
    }
}