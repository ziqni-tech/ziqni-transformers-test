package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.Reward;
import com.ziqni.transformer.test.models.ZiqniProduct;
import com.ziqni.transformer.test.models.ZiqniReward;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RewardStoreTest {

    private final RewardStore rewardStore;

    public RewardStoreTest() {
        final var accountId = "test-account";
        var ziqniStores = new ZiqniStores(StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.rewardStore = ziqniStores.rewardStore;
    }

    @Test
    void getZiqniReward() {
        final var rewardById = rewardStore.getZiqniReward("prod-1");
        Optional<ZiqniReward> basicRewardOptional = rewardById.join();
        assertNotNull(basicRewardOptional);
        assertNotNull(basicRewardOptional.get());
        assertNotNull(basicRewardOptional.get().getMetaData());
    }

    @Test
    void getReward() {
        final var rewardById = rewardStore.getReward("prod-1");
        Optional<Reward> basicRewardOptional = rewardById.join();
        assertNotNull(basicRewardOptional);
        assertNotNull(basicRewardOptional.get());
        assertNotNull(basicRewardOptional.get().getId());
    }

    @Test
    void makeMock() {
        final var reward = rewardStore.makeMock();
        assertNotNull(reward);
        assertNotNull(reward.getId());
    }
}