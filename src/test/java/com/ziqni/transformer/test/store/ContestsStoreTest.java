package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.CompetitionStatus;
import com.ziqni.admin.sdk.model.Contest;
import com.ziqni.admin.sdk.model.ContestStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class ContestsStoreTest {

    private final ContestsStore contestsStore;

    public ContestsStoreTest() {
        var ziqniStores = new ZiqniStores(StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.contestsStore = ziqniStores.contestsStore;
    }

    @Test
    void getZiqniContest() throws ExecutionException, InterruptedException {
        final var contest = contestsStore.getZiqniContest("cont-1");
        contest.join();
        assertNotNull(contest);
        assertNotNull(contest.get());
        assertNotNull(contest.get().getName());
    }

    @Test
    void getContest() throws ExecutionException, InterruptedException {
        final var contest = contestsStore.getContest("cont-1");
        contest.join();
        assertNotNull(contest);
        assertNotNull(contest.get());
        assertTrue(contest.get().isPresent());
        assertNotNull(contest.get().get().getName());
    }

    @Test
    void put() throws ExecutionException, InterruptedException {
        String contestId = "contest-test-1";
        contestsStore.put(new Contest().id(contestId).name("testName"));

        final var contest = contestsStore.getZiqniContest(contestId);
        contest.join();
        assertNotNull(contest);
        assertNotNull(contest.get());
        assertNotNull(contest.get().getName());
    }

    @Test
    void makeMock() {
        final var actionTypeEntry = contestsStore.makeMock(11, ContestStatus.ACTIVE, CompetitionStatus.ACTIVE);
        assertNotNull(actionTypeEntry);
        assertNotNull(actionTypeEntry.getId());
    }

}