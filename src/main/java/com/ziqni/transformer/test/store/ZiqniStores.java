package com.ziqni.transformer.test.store;

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

    public ZiqniStores() {
        this.eventsStore = new EventsStore();
        this.membersStore = new MembersStore();
        this.productsStore = new ProductsStore();
        this.actionTypesStore = new ActionTypesStore();
        this.achievementsStore = new AchievementsStore();
        this.contestsStore = new ContestsStore();
        this.rewardStore = new RewardStore();
        this.awardStore = new AwardStore();
        this.unitsOfMeasureStore = new UnitsOfMeasureStore();
    }
}
