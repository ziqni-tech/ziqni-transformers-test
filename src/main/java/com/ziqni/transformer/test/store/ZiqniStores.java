package com.ziqni.transformer.test.store;

import com.ziqni.transformer.test.models.BasicAchievement;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ZiqniStores {

    public final EventsStore eventsStore;

    public final MembersStore membersStore;
    public final ProductsStore productsStore;

    public final ActionTypesStore actionTypesStore;

    public final AchievementsStore achievementsStore;
    public final ContestsStore contestsStore;
    public final RewardStore rewardStore;
    public final AwardStore awardStore;
   public final UnitsOfMeasureStore unitsOfMeasureStore;

    public ZiqniStores(String accountId) {
        this.eventsStore = new EventsStore();
        this.membersStore = new MembersStore();
        this.productsStore = new ProductsStore();
        this.actionTypesStore = new ActionTypesStore();
        this.achievementsStore = new AchievementsStore(accountId);
        this.contestsStore = new ContestsStore();
        this.rewardStore = new RewardStore();
        this.awardStore = new AwardStore(rewardStore);
        this.unitsOfMeasureStore = new UnitsOfMeasureStore();
    }

    public void generateSampleData() {
        for (int x = 0; x < 10; x++){
            achievementsStore.getAchievement("ach-" + x);
            contestsStore.getContest("cont-" + x);
            membersStore.getRefIdByMemberId("memb-" + x);
            productsStore.getRefIdByProductId("prod-" + x);
            unitsOfMeasureStore.getUnitOfMeasure("uom-" + x);
            rewardStore.getReward("reward-" + x);
            awardStore.getAward("award-" + x);
        }

    }

}
