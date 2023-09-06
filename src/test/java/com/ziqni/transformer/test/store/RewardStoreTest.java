package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.Reward;
import com.ziqni.transformer.test.models.ZiqniReward;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RewardStoreTest {

    private final RewardStore rewardStore;

    public RewardStoreTest() {
        var ziqniStores = new ZiqniStores(StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.rewardStore = ziqniStores.rewardStore;
    }

    @Test
    void getZiqniReward() {
        final var rewardById = rewardStore.getZiqniReward("prod-1");
        ZiqniReward basicReward = rewardById.join();
        assertNotNull(basicReward);
        assertNotNull(basicReward.getMetaData());
    }

    @Test
    void getReward() {
        final var rewardById = rewardStore.getReward("prod-1");
        Optional<Reward> basicRewardOptional = rewardById.join();
        assertNotNull(basicRewardOptional);
        assertTrue(basicRewardOptional.isPresent());
        assertNotNull(basicRewardOptional.get().getId());
    }

    @Test
    void makeMock() {
        final var reward = rewardStore.makeMock();
        assertNotNull(reward);
        assertNotNull(reward.getId());
    }
}