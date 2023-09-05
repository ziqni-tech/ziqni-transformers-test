package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.Result;
import com.ziqni.admin.sdk.model.Reward;
import com.ziqni.admin.sdk.model.UnitOfMeasure;
import com.ziqni.admin.sdk.model.UnitOfMeasureType;
import com.ziqni.transformer.test.models.ZiqniReward;
import com.ziqni.transformer.test.models.ZiqniUnitOfMeasure;
import org.junit.jupiter.api.Test;
import scala.None;
import scala.Option;
import scala.Some;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UnitsOfMeasureStoreTest {

    private final UnitsOfMeasureStore unitsOfMeasureStore;

    public UnitsOfMeasureStoreTest() {
        var ziqniStores = new ZiqniStores(StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.unitsOfMeasureStore = ziqniStores.unitsOfMeasureStore;
    }

    @Test
    void getZiqniUnitOfMeasure() {
        final var unitOfMeasure = unitsOfMeasureStore.getZiqniUnitOfMeasure("prod-1");
        Optional<ZiqniUnitOfMeasure> basicUnitOfMeasure = unitOfMeasure.join();
        assertNotNull(basicUnitOfMeasure);
        assertNotNull(basicUnitOfMeasure.get());
        assertNotNull(basicUnitOfMeasure.get().getUnitOfMeasureKey());
    }

    @Test
    void getUnitOfMeasure() {
        final var unitOfMeasureById = unitsOfMeasureStore.getUnitOfMeasure("prod-1");
        Optional<UnitOfMeasure> basicRewardOptional = unitOfMeasureById.join();
        assertNotNull(basicRewardOptional);
        assertNotNull(basicRewardOptional.get());
        assertNotNull(basicRewardOptional.get().getId());
    }

    @Test
    void getUnitOfMeasureMultiplier() {
        final var unitOfMeasureById = unitsOfMeasureStore.getUnitOfMeasure("prod-1");
        Optional<UnitOfMeasure> basicRewardOptional = unitOfMeasureById.join();
        assertNotNull(basicRewardOptional);
        assertNotNull(basicRewardOptional.get());
        assertNotNull(basicRewardOptional.get().getId());
    }

    @Test
    void create() {
        final var unitOfMeasureFuture = unitsOfMeasureStore.create("test-product-ref-id-new-1", new Some<>("test-product"), Option.empty(), 2.0, UnitOfMeasureType.OTHER);
        Optional<Result> productIdOptional = unitOfMeasureFuture.join();
        assertNotNull(productIdOptional);
        assertNotNull(productIdOptional.get());
    }

    @Test
    void makeMock() {
        final var unitOfMeasure = unitsOfMeasureStore.makeMock();
        assertNotNull(unitOfMeasure);
        assertNotNull(unitOfMeasure.getId());
    }
}