package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.ziqni.admin.sdk.model.EntityType;
import com.ziqni.admin.sdk.model.ModelApiResponse;
import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformers.domain.BasicEventModel;
import lombok.NonNull;
import scala.Option;
import scala.Some;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class EventsStore implements CacheLoader<@NonNull String, EventsStore.EventTransaction> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();

    private final ProductsStore productsStore;

    private final MembersStore membersStore;

    @Override
    public @Nullable EventsStore.EventTransaction load(@NonNull String key) throws Exception {
        return makeMock();
    }

    public EventsStore(ProductsStore productsStore, MembersStore membersStore) {
        this.productsStore = productsStore;
        this.membersStore = membersStore;
    }

    public CompletableFuture<ModelApiResponse> pushEvent(BasicEventModel basicEventModel) {
        return pushEvent(List.of(basicEventModel));
    }

    public CompletableFuture<ModelApiResponse> pushEvent(List<BasicEventModel> basicEventModels) {
        var response = new ModelApiResponse();
        basicEventModels.forEach(x -> {
            pushEvent(x, response);
        });

        return CompletableFuture.completedFuture(response);
    }

    public CompletableFuture<ModelApiResponse> pushEventTransaction(BasicEventModel basicEventModel) {
        var response = new ModelApiResponse();
        pushEvent(basicEventModel, response);

        return CompletableFuture.completedFuture(response);
    }

    public CompletableFuture<List<BasicEventModel>> findByBatchId(String batchId) {
        return null;
    }

    private void pushEvent(BasicEventModel basicEventModel, ModelApiResponse response) {
        if (basicEventModel.action().equalsIgnoreCase(EntityType.MEMBER.getValue())){
            CompletableFuture<Optional<String>> createdMember = membersStore.create(basicEventModel.memberRefId(), "member-" + identifierCounter, basicEventModel.tags(), null);
            createdMember.thenAccept(y -> {
                y.ifPresent(z -> {
                    response.addResultsItem(new Result().id(z));
                });
            });

        } else if (basicEventModel.action().equalsIgnoreCase(EntityType.PRODUCT.getValue())) {
            CompletableFuture<Optional<String>> createdProduct = productsStore.create(basicEventModel.entityRefId(), "product-" + identifierCounter, null, null, null, null);
            createdProduct.thenAccept(y -> {
                y.ifPresent(z -> {
                    response.addResultsItem(new Result().id(z));
                });
            });
        }
    }

    public EventTransaction makeMock(){
        final var eventTrans = new EventTransaction();
        String memberRefId = "member-ref-" + identifierCounter;
        AtomicReference<String> memberId = new AtomicReference<>();
        final var createdMember = membersStore.create(memberRefId, "member-" + identifierCounter, null, null);
        createdMember.thenAccept(y -> {
            y.ifPresent(memberId::set);
        });
        var memberIdOption = new Some<>(memberId.get());
        eventTrans.addBasicEvent(new BasicEventModel(memberIdOption, memberRefId, null, null, null, null, 2.0, null, null, null, null));
        return eventTrans;
    }

    public static class EventTransaction {
        private final List<BasicEventModel> buffer = new ArrayList<>();

        public boolean addBasicEvent(BasicEventModel e) {
            return buffer.add(e);
        }

        public List<BasicEventModel> getEvents() {
            return buffer;
        }
    }
}
