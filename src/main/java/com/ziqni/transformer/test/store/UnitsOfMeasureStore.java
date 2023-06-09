package com.ziqni.transformer.test.store;

import com.github.benmanes.caffeine.cache.*;
import com.ziqni.admin.sdk.ApiException;
import com.ziqni.admin.sdk.model.Result;
import com.ziqni.admin.sdk.model.Reward;
import com.ziqni.admin.sdk.model.UnitOfMeasure;
import com.ziqni.admin.sdk.model.UnitOfMeasureType;
import com.ziqni.transformer.test.concurrent.ZiqniExecutors;
import com.ziqni.transformer.test.models.BasicUnitOfMeasure;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.collection.JavaConverters;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class UnitsOfMeasureStore implements AsyncCacheLoader<@NonNull String, @NonNull UnitOfMeasure>, RemovalListener<@NonNull String, @NonNull UnitOfMeasure> {

    private final static AtomicInteger identifierCounter = new AtomicInteger();
    private static final Logger logger = LoggerFactory.getLogger(UnitsOfMeasureStore.class);

    public final AsyncLoadingCache<@NonNull String, @NonNull UnitOfMeasure> cache = Caffeine
            .newBuilder()
            .maximumSize(1_000)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .evictionListener(this)
            .executor(ZiqniExecutors.GlobalZiqniCachesExecutor)
            .buildAsync(this);

    public UnitsOfMeasureStore(StoreContext context) {
    }

    public CompletableFuture<Optional<BasicUnitOfMeasure>> getBasicUnitOfMeasure(String uom) {
        return getUnitOfMeasure(uom).thenApply(x->x.map(BasicUnitOfMeasure::apply));
    }

    public CompletableFuture<Optional<UnitOfMeasure>> getUnitOfMeasure(String uom) {
        return cache.get(uom).thenApply(Optional::ofNullable);
    }

    public CompletableFuture<Optional<Double>> getUnitOfMeasureMultiplier(String uom) {
        return cache.get(uom).thenApply(Optional::ofNullable).thenApply(unitOfMeasure -> unitOfMeasure.map(UnitOfMeasure::getMultiplier));
    }

    public CompletableFuture<Optional<Result>> create(final String key, Option<String> name, Option<String> isoCode, Double multiplier, UnitOfMeasureType unitOfMeasureType){
        final var out = new CompletableFuture<Optional<Result>>();
        var look = this.cache.getIfPresent(key);
        var isInCache = Objects.nonNull(look) &&
                look.thenApply(Objects::nonNull)
                .join();
        if(isInCache)
            out.completeExceptionally(new ApiException("unit_of_measure_with_key_[" + key + "]_already_exists")); // or whatever we throw
        else {
            final var uom = makeMock();
            if (!name.isEmpty()){
                uom.name(name.get());
            }
            if (!isoCode.isEmpty()){
                uom.isoCode(isoCode.get());
            }
            uom.multiplier(multiplier);
            uom.unitOfMeasureType(unitOfMeasureType);
            out.complete(Optional.of(new Result()
                    .id(uom.getId())
                    .result("CREATED")
                    .externalReference(key)));
        }

        return out;
    }

    public UnitOfMeasure makeMock(){
        final var identifierCount = identifierCounter.incrementAndGet();
        return new UnitOfMeasure()
                .id("uom-" + identifierCount)
                .spaceName("test-space-name")
                .created(OffsetDateTime.now())
                .customFields(java.util.Map.of("test-custom-field","test-val"))
                .addTagsItem("test-tag")
                .metadata(java.util.Map.of("test-metadata","test-val"))
                .name("Test-oum-"+identifierCount)
                .description("Test-description")
                .key("test-key" + identifierCount)
                .isoCode("test-iso-code")
                .multiplier(2.0)
                .symbol("test-symbol");
    }

    @Override
    public CompletableFuture<? extends @NonNull UnitOfMeasure> asyncLoad(@NonNull String key, Executor executor) throws Exception {
        return CompletableFuture.completedFuture(makeMock());
    }

    @Override
    public void onRemoval(@Nullable @NonNull String key, @Nullable @NonNull UnitOfMeasure value, RemovalCause cause) {

    }
}
