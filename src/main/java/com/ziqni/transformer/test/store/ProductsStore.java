package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.ApiException;
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
import scala.collection.JavaConverters;

import scala.collection.Seq;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductsStore implements AsyncCacheLoader<@NonNull String, @NonNull Product>, RemovalListener<@NonNull String, @NonNull Product> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();
    private final ZiqniConcurrentHashMap<String, String> refIdCache = new ZiqniConcurrentHashMap<>();
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
        return cache.get(productRefId).thenApply(x -> Optional.of(x.getProductRefId()));
    }

    public CompletableFuture<String> getRefIdByProductId(String productId) {
        return cache.get(productId).thenApply(Product::getProductRefId);
    }

    public CompletableFuture<Optional<BasicProduct>> findBasicProductModelById(String productId) {
        return cache.get(productId)
                .thenApply(Optional::ofNullable)
                .thenApply(x -> x.map(BasicProduct::apply));
    }

    public CompletableFuture<Optional<String>> create(String productRefId, String displayName, Seq<String> providers, String productType, Double defaultAdjustmentFactor, Option<scala.collection.Map<String, String>> metaData) {
        final var out = new CompletableFuture<Optional<String>>();
        if(this.refIdCache.containsKey(productRefId))
            out.completeExceptionally(new ApiException("product_ref_id_already_exists")); // or whatever we throw
        else {
            final var providersToCreate = JavaConverters.seqAsJavaList(providers);
            final Map<String,String> metadata = metaData.isEmpty() ? Map.of() : JavaConverters.mapAsJavaMap(metaData.get());
            final var product = makeMock()
                    .name(displayName)
                    .productRefId(productRefId)
//                    .actionTypeAdjustmentFactors(defaultAdjustmentFactor)
                    .metadata(metadata);
            this.cache.put(product.getId(), CompletableFuture.completedFuture(product));
            this.refIdCache.put(product.getProductRefId(), product.getId());
            out.complete(Optional.of(product.getId()));
        }

        return out;
    }

    public CompletableFuture<Optional<Result>> update(String productId, Option<String> productRefId, Option<String> displayName, Option<Seq<String>> providers, Option<String> productType, Option<Double> defaultAdjustmentFactor, Option<scala.collection.Map<String, String>> metaData) {
        final var out = new CompletableFuture<Optional<Result>>();
        var isNotInCache = Objects.isNull(this.cache.getIfPresent(productId));
        if (isNotInCache)
            out.completeExceptionally(new ApiException("product_with_id_[" + productId + "]_does_not_exist")); // or whatever we throw
        else {
            this.cache.get(productId)
                    .thenAccept(x -> {
                        if (!productRefId.isEmpty())
                            x.productRefId(productRefId.get());
                        if (!displayName.isEmpty())
                            x.name(displayName.get());
//                        if (!productType.isEmpty())
//                            x.productType(productType);
                        if (Objects.nonNull(metaData) && !metaData.isEmpty())
                            x.metadata(JavaConverters.mapAsJavaMap(metaData.get()));

                        out.complete(Optional.of(new Result()
                                .id(x.getId())
                                .result("UPDATED")
                                .externalReference(x.getProductRefId())));
                    });

        }

        return out;
    }

    public CompletableFuture<Optional<Result>> delete(String productId) {
        removeFromCache(productId);
        return CompletableFuture.completedFuture(Optional.of(
                new Result()
                .id(productId)
                .result("DELETED")));
    }

    public Product makeMock(){
        final var identifierCount = identifierCounter.incrementAndGet();
        return new Product()
                .id("prod-" + identifierCount)
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

    public void removeFromCache(String key) {
        this.cache.put(key, CompletableFuture.completedFuture(null));
    }
}
