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
        String accountId = "test-account";
        var ziqniStores = new ZiqniStores(accountId);
        ziqniStores.generateSampleData();
        this.contestsStore = ziqniStores.contestsStore;
    }

    @Test
    void getBasicContestModel() throws ExecutionException, InterruptedException {
        final var contestModel = contestsStore.getBasicContestModel("cont-1");
        contestModel.join();
        assertNotNull(contestModel);
        assertNotNull(contestModel.get());
        assertNotNull(contestModel.get().get());
        assertNotNull(contestModel.get().get().getName());
    }

    @Test
    void getContest() throws ExecutionException, InterruptedException {
        final var contestModel = contestsStore.getContest("cont-1");
        contestModel.join();
        assertNotNull(contestModel);
        assertNotNull(contestModel.get());
        assertNotNull(contestModel.get().get());
        assertNotNull(contestModel.get().get().getName());
    }

    @Test
    void put() throws ExecutionException, InterruptedException {
        String contestId = "contest-test-1";
        contestsStore.put(new Contest().id(contestId).name("testName"));

        final var contestModel = contestsStore.getBasicContestModel(contestId);
        contestModel.join();
        assertNotNull(contestModel);
        assertNotNull(contestModel.get());
        assertNotNull(contestModel.get().get());
        assertNotNull(contestModel.get().get().getName());
    }

    @Test
    void makeMock() {
        final var actionTypeEntry = contestsStore.makeMock(11, ContestStatus.ACTIVE, CompetitionStatus.ACTIVE);
        assertNotNull(actionTypeEntry);
        assertNotNull(actionTypeEntry.getId());
    }

}