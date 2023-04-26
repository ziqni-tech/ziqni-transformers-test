package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.Reward;
import com.ziqni.transformer.test.models.BasicProduct;
import com.ziqni.transformer.test.models.BasicReward;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RewardStoreTest {

    private final RewardStore rewardStore;

    public RewardStoreTest() {
        final var accountId = "test-account";
        var ziqniStores = new ZiqniStores(accountId,StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.rewardStore = ziqniStores.rewardStore;
    }

    @Test
    void getBasicReward() {
        final var rewardModelById = rewardStore.getBasicReward("prod-1");
        Optional<BasicReward> basicRewardOptional = rewardModelById.join();
        assertNotNull(basicRewardOptional);
        assertNotNull(basicRewardOptional.get());
        assertNotNull(basicRewardOptional.get().getMetaData());
    }

    @Test
    void getReward() {
        final var rewardModelById = rewardStore.getReward("prod-1");
        Optional<Reward> basicRewardOptional = rewardModelById.join();
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