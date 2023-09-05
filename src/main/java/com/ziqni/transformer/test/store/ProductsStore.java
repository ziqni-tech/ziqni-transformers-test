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
import com.ziqni.transformer.test.models.ZiqniProduct;
import com.ziqni.transformers.ZiqniNotFoundException;
import com.ziqni.transformers.domain.CreateProductRequest;
import lombok.NonNull;
import scala.Option;
import scala.collection.JavaConverters;

import scala.collection.Seq;
import scala.concurrent.Future;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProductsStore implements AsyncCacheLoader<@NonNull String, @NonNull Product>, RemovalListener<@NonNull String, @NonNull Product> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();
    private final ZiqniConcurrentHashMap<String, String> refIdCache = new ZiqniConcurrentHashMap<>();
    public final AsyncLoadingCache<@org.checkerframework.checker.nullness.qual.NonNull String, @org.checkerframework.checker.nullness.qual.NonNull Product> cache;

    public ProductsStore(StoreContext context) {
        cache = Caffeine
                .newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .evictionListener(this)
                .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
                .buildAsync(this);
    }

    public CompletableFuture<ZiqniProduct> getByReferenceId(String productRefId) {
        return Optional.of(refIdCache.get(productRefId)).map(s -> findZiqniProductById(s)).orElseThrow(() -> new ZiqniNotFoundException("ZiqniProduct",productRefId,true));
    }

    public CompletableFuture<String> getRefIdByProductId(String productId) {
        return cache.get(productId).thenApply(Product::getProductRefId);
    }

    public CompletableFuture<ZiqniProduct> findZiqniProductById(String productId) {
        return cache.get(productId)
                .thenApply(Optional::ofNullable)
                .thenApply(x -> x.map(ZiqniProduct::apply).orElseThrow( () -> new ZiqniNotFoundException("ZiqniProduct",productId,false) ))
                ;
    }

    public CompletableFuture<com.ziqni.transformers.domain.ZiqniProduct> create(String productRefId, String displayName, Seq<String> tags, String productType, Double defaultAdjustmentFactor, scala.collection.Map<String, String> metaData) {
        final var out = new CompletableFuture<com.ziqni.transformers.domain.ZiqniProduct>();
        if(this.refIdCache.containsKey(productRefId))
            out.completeExceptionally(new ApiException("product_ref_id_already_exists")); // or whatever we throw
        else {
            final var providersToCreate = JavaConverters.seqAsJavaList(tags);
            final Map<String,String> metadataOut = metaData.isEmpty() ? Map.of() : JavaConverters.mapAsJavaMap(metaData);
            final var product = makeMock()
                    .name(displayName)
                    .productRefId(productRefId)
//                    .actionTypeAdjustmentFactors(defaultAdjustmentFactor)
                    .metadata(metadataOut);
            this.cache.put(product.getId(), CompletableFuture.completedFuture(product));
            this.refIdCache.put(product.getProductRefId(), product.getId());
            out.complete(new ZiqniProduct(product));
        }

        return out;

    }

    public CompletableFuture<com.ziqni.transformers.domain.ZiqniProduct> getOrCreateProduct(String productRefId, Supplier<CreateProductRequest> createAs, Function<com.ziqni.transformers.domain.ZiqniProduct, Future<com.ziqni.transformers.domain.ZiqniProduct>> onExist) {
        final var out = new CompletableFuture<com.ziqni.transformers.domain.ZiqniProduct>();
        if(this.refIdCache.containsKey(productRefId))
            out.completeExceptionally(new ApiException("product_ref_id_already_exists")); // or whatever we throw
        else {
            final var toCreate = createAs.get();
            final var providersToCreate = JavaConverters.seqAsJavaList(toCreate.tags());
            final Map<String,String> metadata = toCreate.metadata().isEmpty() ? Map.of() : JavaConverters.mapAsJavaMap(toCreate.metadata());
            final var product = makeMock()
                    .name(toCreate.displayName())
                    .productRefId(productRefId)
//                    .actionTypeAdjustmentFactors(defaultAdjustmentFactor)
                    .metadata(metadata);
            this.cache.put(product.getId(), CompletableFuture.completedFuture(product));
            this.refIdCache.put(product.getProductRefId(), product.getId());
            out.complete(new ZiqniProduct(product));
        }

        return out;
    }

    public CompletableFuture<ZiqniProduct> update(String productId, Option<String> productRefId, Option<String> displayName, Option<Seq<String>> providers, Option<String> productType, Option<Double> defaultAdjustmentFactor, Option<scala.collection.Map<String, String>> metaData) {
        final var out = new CompletableFuture<ZiqniProduct>();
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

                        out.complete(new ZiqniProduct(x));
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
    public void onRemoval(@NonNull String key, @NonNull Product value, RemovalCause cause) {

    }

    public void removeFromCache(String key) {
        this.cache.put(key, CompletableFuture.completedFuture(null));
    }
}
