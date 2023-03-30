package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.ZiqniAdminApiFactory;
import com.ziqni.admin.sdk.model.ActionTypeAdjustmentFactor;
import com.ziqni.admin.sdk.model.Member;
import com.ziqni.admin.sdk.model.Product;
import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformer.test.concurrent.ZiqniConcurrentHashMap;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicProduct;
import lombok.NonNull;
import scala.Option;
import scala.collection.Map;
import scala.collection.Seq;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductsStore implements AsyncCacheLoader<@NonNull String, @NonNull Product>, RemovalListener<@NonNull String, @NonNull Product> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();
    private static final ZiqniConcurrentHashMap<String, String> refIdCache = new ZiqniConcurrentHashMap<>();
    public final AsyncLoadingCache<@org.checkerframework.checker.nullness.qual.NonNull String, @org.checkerframework.checker.nullness.qual.NonNull Product> cache;

    public ProductsStore() {
        cache = Caffeine
                .newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .evictionListener(this)
                .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
                .buildAsync(this);
    }

    public CompletableFuture<Optional<String>> getIdByReferenceId(String productRefId) {
        return null;
    }

    public CompletableFuture<String> getRefIdByProductId(String productId) {
        return cache.get(productId).thenApply(Product::getProductRefId);
    }

    public CompletableFuture<Optional<BasicProduct>> findBasicProductModelById(String productId) {
        return cache.get(productId)
                .thenApply(Optional::ofNullable)
                .thenApply(x -> x.map(BasicProduct::apply));
    }

    public CompletableFuture<Optional<String>> create(String productRefId, String displayName, Seq<String> providers, String productType, Double defaultAdjustmentFactor, Option<Map<String, String>> metaData) {
        return null;
    }

    public CompletableFuture<Optional<Result>> update(String productId, Option<String> productRefId, Option<String> displayName, Option<Seq<String>> providers, Option<String> productType, Option<Double> defaultAdjustmentFactor, Option<scala.collection.Map<String, String>> metaData) {
        return null;
    }

    public CompletableFuture<Optional<Result>> delete(String productId) {
        return null;
    }

    public Product makeMock(){
        final var identifierCount = identifierCounter.incrementAndGet();
        return new Product()
                .id("memb-" + identifierCount)
                .spaceName("test-space-name")
                .created(OffsetDateTime.now())
                .customFields(java.util.Map.of("test-custom-field","test-val"))
                .addTagsItem("test-tag")
                .metadata(java.util.Map.of("test-metadata","test-val"))
                .name("Test-product-"+identifierCount)
                .description("Test-description")
                .adjustmentFactor(2.0)
                .productRefId("test-product-ref-id")
                .addActionTypeAdjustmentFactorsItem(new ActionTypeAdjustmentFactor().actionTypeId("test-action-type-id").adjustmentFactor(2.0));
    }

    @Override
    public CompletableFuture<? extends @NonNull Product> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return CompletableFuture.completedFuture(makeMock());
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable @NonNull Product value, RemovalCause cause) {

    }
}
