package com.ziqni.transformer.test.store;

import com.ziqni.transformer.test.utils.ScalaUtils;
import org.junit.jupiter.api.Test;
import scala.Option;
import scala.Some;
import scala.collection.immutable.Map$;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class MembersStoreTest {

    private final MembersStore membersStore;

    public MembersStoreTest() {
        var ziqniStores = new ZiqniStores(StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.membersStore = ziqniStores.membersStore;
    }

    @Test
    void getIdByReferenceId() throws ExecutionException, InterruptedException {
        membersStore.create("test-member-ref-id-new-1", "test-member", ScalaUtils.emptySeqString, Map$.MODULE$.empty()).join();
        final var member = membersStore.getMemberByReferenceId("test-member-ref-id-new-1");
        member.join();
        assertNotNull(member);
        assertNotNull(member.get());
    }

    @Test
    void getRefIdByMemberId() throws ExecutionException, InterruptedException {
        final var contest = membersStore.getRefIdByMemberId("memb-1");
        contest.join();
        assertNotNull(contest);
        assertNotNull(contest.get());
        assertNotNull(contest.get());
    }

    @Test
    void create() throws ExecutionException, InterruptedException {
        final var member = membersStore.create("test-member-ref-id-1", "test-member", ScalaUtils.emptySeqString, Map$.MODULE$.empty());
        member.join();
        assertNotNull(member);
        assertNotNull(member.get());
    }

    @Test
    void update() throws ExecutionException, InterruptedException {
        final var member = membersStore.update("memb-1" ,new Some<>("test-member-ref-id-1"), new Some<>("test-member"), Option.empty());
        assertFalse(member.isCompletedExceptionally());
        member.join();
        assertNotNull(member);
        assertNotNull(member.get());
    }

    @Test
    void findZiqniMemberById() throws ExecutionException, InterruptedException {
        final var contest = membersStore.findZiqniMemberById("memb-1");
        contest.join();
        assertNotNull(contest);
        assertNotNull(contest.get());
    }

    @Test
    void findMemberById() throws ExecutionException, InterruptedException {
        final var member = membersStore.findMemberById("memb-1");
        member.join();
        assertNotNull(member);
        assertTrue(member.get().isPresent());
        assertNotNull(member.get().get());
    }

    @Test
    void makeMock() {
        final var member = membersStore.makeMock();
        assertNotNull(member);
        assertNotNull(member.getId());
    }
}