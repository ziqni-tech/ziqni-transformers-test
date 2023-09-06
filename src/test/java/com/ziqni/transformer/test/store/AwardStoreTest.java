package com.ziqni.transformer.test.store;

import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;

class AwardStoreTest {

    private final RewardStore rewardStore;
    private final AwardStore awardStore;
    public AwardStoreTest() {
        this.rewardStore = new RewardStore(StoreContext.StandAlone());
        var ziqniStores = new ZiqniStores(StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.awardStore = ziqniStores.awardStore;
    }

    @Test
    void getZiqniAward() throws ExecutionException, InterruptedException {
        final var actionTypeByAction = awardStore.getZiqniAward("award-1");
        actionTypeByAction.join();
        assertNotNull(actionTypeByAction);
        assertNotNull(actionTypeByAction.get());
        assertNotNull(actionTypeByAction.get().getRewardId());
    }

    @Test
    void getAward() throws ExecutionException, InterruptedException {
        final var actionTypeByAction = awardStore.getAward("award-1");
        actionTypeByAction.join();
        assertNotNull(actionTypeByAction);
        assertNotNull(actionTypeByAction.get());
        assertNotNull(actionTypeByAction.get().getRewardId());
    }

    @Test
    void makeMock() {
        final var actionTypeEntry = awardStore.makeMock(rewardStore.makeMock());
        assertNotNull(actionTypeEntry);
        assertNotNull(actionTypeEntry.getId());
    }
}