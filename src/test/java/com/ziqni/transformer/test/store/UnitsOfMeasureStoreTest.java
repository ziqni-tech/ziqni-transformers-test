package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.Result;
import com.ziqni.admin.sdk.model.Reward;
import com.ziqni.admin.sdk.model.UnitOfMeasure;
import com.ziqni.admin.sdk.model.UnitOfMeasureType;
import com.ziqni.transformer.test.models.BasicReward;
import com.ziqni.transformer.test.models.BasicUnitOfMeasure;
import org.junit.jupiter.api.Test;
import scala.None;
import scala.Option;
import scala.Some;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UnitsOfMeasureStoreTest {

    private final UnitsOfMeasureStore unitsOfMeasureStore;

    public UnitsOfMeasureStoreTest() {
        final var accountId = "test-account";
        var ziqniStores = new ZiqniStores(accountId);
        ziqniStores.generateSampleData();
        this.unitsOfMeasureStore = ziqniStores.unitsOfMeasureStore;
    }

    @Test
    void getBasicUnitOfMeasure() {
        final var unitOfMeasure = unitsOfMeasureStore.getBasicUnitOfMeasure("prod-1");
        Optional<BasicUnitOfMeasure> basicUnitOfMeasure = unitOfMeasure.join();
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