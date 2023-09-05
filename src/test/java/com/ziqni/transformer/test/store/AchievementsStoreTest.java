package com.ziqni.transformer.test.store;

import com.ziqni.transformer.test.models.ZiqniAchievement;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;


class AchievementsStoreTest {

    private final AchievementsStore achievementsStore;

    public AchievementsStoreTest() {
        String accountId = "test-account";
        var ziqniStores = new ZiqniStores(StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.achievementsStore = ziqniStores.achievementsStore;
    }



    @Test
    void getAchievement() throws ExecutionException, InterruptedException {
        final var achievement = achievementsStore.getAchievement("ach-1");
        achievement.join();
        assertNotNull(achievement);
        assertNotNull(achievement.get());
        assertNotNull(achievement.get().get());
        assertNotNull(achievement.get().get().getName());

    }

    @Test
    void findAchievementById() throws ExecutionException, InterruptedException {
        final var achievement = achievementsStore.findAchievementById("ach-1");
        achievement.join();
        assertNotNull(achievement);
        assertNotNull(achievement.get());
        assertNotNull(achievement.get().get());
        assertNotNull(achievement.get().get().getName());
    }

    @Test
    void makeMock() {
        final var achievement = achievementsStore.makeMock();
        assertNotNull(achievement);
        assertNotNull(achievement.getName());
    }
}