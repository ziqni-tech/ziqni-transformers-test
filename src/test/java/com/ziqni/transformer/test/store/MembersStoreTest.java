package com.ziqni.transformer.test.store;

import com.ziqni.transformer.test.utils.ScalaUtils;
import org.junit.jupiter.api.Test;
import scala.Option;
import scala.Some;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class MembersStoreTest {

    private final MembersStore membersStore;

    public MembersStoreTest() {
        String accountId = "test-account";
        var ziqniStores = new ZiqniStores(StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.membersStore = ziqniStores.membersStore;
    }

    @Test
    void getIdByReferenceId() throws ExecutionException, InterruptedException {
        membersStore.create("test-member-ref-id-new-1", "test-member", ScalaUtils.emptySeqString, Option.empty()).join();
        final var contestModel = membersStore.getIdByReferenceId("test-member-ref-id-new-1");
        contestModel.join();
        assertNotNull(contestModel);
        assertNotNull(contestModel.get());
        assertTrue(contestModel.get().isPresent());
    }

    @Test
    void getRefIdByMemberId() throws ExecutionException, InterruptedException {
        final var contestModel = membersStore.getRefIdByMemberId("memb-1");
        contestModel.join();
        assertNotNull(contestModel);
        assertNotNull(contestModel.get());
        assertNotNull(contestModel.get());
    }

    @Test
    void create() throws ExecutionException, InterruptedException {
        final var member = membersStore.create("test-member-ref-id-1", "test-member", ScalaUtils.emptySeqString, Option.empty());
        member.join();
        assertNotNull(member);
        assertNotNull(member.get());
    }

    @Test
    void update() throws ExecutionException, InterruptedException {
        final var member = membersStore.update("memb-1" ,new Some<>("test-member-ref-id-1"), new Some<>("test-member"), Option.empty(), Option.empty());
        assertFalse(member.isCompletedExceptionally());
        member.join();
        assertNotNull(member);
        assertNotNull(member.get());
    }

    @Test
    void findBasicMemberModelById() throws ExecutionException, InterruptedException {
        final var contestModel = membersStore.findBasicMemberModelById("memb-1");
        contestModel.join();
        assertNotNull(contestModel);
        assertNotNull(contestModel.get());
        assertNotNull(contestModel.get().get());
    }

    @Test
    void findMemberById() throws ExecutionException, InterruptedException {
        final var member = membersStore.findMemberById("memb-1");
        member.join();
        assertNotNull(member);
        assertNotNull(member.get());
        assertNotNull(member.get().get());
    }

    @Test
    void makeMock() {
        final var member = membersStore.makeMock();
        assertNotNull(member);
        assertNotNull(member.getId());
    }
}