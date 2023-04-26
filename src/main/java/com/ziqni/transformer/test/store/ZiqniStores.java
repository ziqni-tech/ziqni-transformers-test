package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.CompetitionStatus;
import com.ziqni.admin.sdk.model.Contest;
import com.ziqni.admin.sdk.model.ContestStatus;
import com.ziqni.transformers.domain.BasicEventModel;
import scala.Some;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

    public ZiqniStores(StoreContext context) {
        this.membersStore = new MembersStore(context);
        this.productsStore = new ProductsStore(context);
        this.actionTypesStore = new ActionTypesStore(context);
        this.achievementsStore = new AchievementsStore(context);
        this.contestsStore = new ContestsStore(context);
        this.rewardStore = new RewardStore(context);
        this.awardStore = new AwardStore(rewardStore,context);
        this.unitsOfMeasureStore = new UnitsOfMeasureStore(context);
        this.eventsStore = new EventsStore(productsStore, membersStore, actionTypesStore,context);
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
            actionTypesStore.findActionTypeByAction("actionType-" + x);
            actionTypesStore.findActionTypeByAction("action-" + x);
            final var basicEventModel = new BasicEventModel(new Some<>("memb-1"), "test-member-ref-id-1", null, "test-event-" + x, new Some<>("1002"), "test-action", 2.0, null, null, null);
            eventsStore.pushEvent(basicEventModel);
        }
        generateActiveContsWithDifferentCompStatuses();
        generateActiveCompsWithDifferentContStatuses();

    }

    private void generateActiveContsWithDifferentCompStatuses() {
        //Finished Competition and Active Contest
        for (int x = 10; x < 20; x++){
            Contest contest = contestsStore.makeMock(x, ContestStatus.ACTIVE, CompetitionStatus.FINISHED);
            contestsStore.put(contest);
        }

        //Finalised Competition and Active Contest
        for (int x = 20; x < 30; x++){
            Contest contest = contestsStore.makeMock(x, ContestStatus.ACTIVE, CompetitionStatus.FINALISED);
            contestsStore.put(contest);
        }

        //Ready Competition and Active Contest
        for (int x = 30; x < 40; x++){
            Contest contest = contestsStore.makeMock(x, ContestStatus.ACTIVE, CompetitionStatus.READY);
            contestsStore.put(contest);
        }

        //Cancelled Competition and Active Contest
        for (int x = 40; x < 50; x++){
            Contest contest = contestsStore.makeMock(x, ContestStatus.ACTIVE, CompetitionStatus.CANCELLED);
            contestsStore.put(contest);
        }
    }

    private void generateActiveCompsWithDifferentContStatuses() {
        //Finished Contest and Active Competition
        for (int x = 50; x < 60; x++){
            Contest contest = contestsStore.makeMock(x, ContestStatus.FINISHED, CompetitionStatus.ACTIVE);
            contestsStore.put(contest);
        }

        //Finalised Contest and Active Competition
        for (int x = 60; x < 70; x++){
            Contest contest = contestsStore.makeMock(x, ContestStatus.FINALISED, CompetitionStatus.ACTIVE);
            contestsStore.put(contest);
        }

        //Ready Contest and Active Competition
        for (int x = 70; x < 80; x++){
            Contest contest = contestsStore.makeMock(x, ContestStatus.READY, CompetitionStatus.ACTIVE);
            contestsStore.put(contest);
        }

        //Cancelled Contest and Active Competition
        for (int x = 80; x < 90; x++){
            Contest contest = contestsStore.makeMock(x, ContestStatus.CANCELLED, CompetitionStatus.ACTIVE);
            contestsStore.put(contest);
        }
    }

}
