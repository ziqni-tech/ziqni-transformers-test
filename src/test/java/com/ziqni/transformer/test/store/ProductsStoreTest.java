package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformer.test.models.BasicProduct;
import com.ziqni.transformer.test.utils.ScalaUtils;
import org.junit.jupiter.api.Test;
import scala.Option;
import scala.Some;
import scala.collection.JavaConverters;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductsStoreTest {

    private final ProductsStore productsStore;

    public ProductsStoreTest() {
        final var accountId = "test-account";
        var ziqniStores = new ZiqniStores(accountId);
        ziqniStores.generateSampleData();
        this.productsStore = ziqniStores.productsStore;
    }

    @Test
    void getIdByReferenceId() {
        final var productsFuture = productsStore.getIdByReferenceId("test-product-ref-id1");
        final var productOptional = productsFuture.join();
        assertNotNull(productOptional);
        assertNotNull(productOptional);
        assertTrue(productOptional.isPresent());
    }

    @Test
    void getRefIdByProductId() {
        final var productIdFuture = productsStore.getRefIdByProductId("prod-1");
        final var productId = productIdFuture.join();
        assertNotNull(productId);

    }

    @Test
    void findBasicProductModelById() {
        final var productModelById = productsStore.findBasicProductModelById("prod-1");
        Optional<BasicProduct> basicProductOptional = productModelById.join();
        assertNotNull(basicProductOptional);
        assertNotNull(basicProductOptional.get());
        assertNotNull(basicProductOptional.get().getMetaData());
    }

    @Test
    void create() {
        final var productFuture = productsStore.create("test-product-ref-id-1", "test-product", ScalaUtils.emptySeqString, "test", 2.0, Option.empty());
        Optional<String> productIdOptional = productFuture.join();
        assertNotNull(productIdOptional);
        assertNotNull(productIdOptional.get());
    }

    @Test
    void update() {
        final var productFuture = productsStore.update("prod-1" ,new Some<>("test-product-ref-id-1"), new Some<>("updated-test-product"), Option.empty(), Option.apply("test"), Option.apply(1.0), Option.empty());
        assertFalse(productFuture.isCompletedExceptionally());
        Optional<Result> productIdOptional = productFuture.join();
        assertNotNull(productIdOptional);
        assertNotNull(productIdOptional.get());
        assertNotNull(productIdOptional.get().getId());
    }

    @Test
    void delete() {
        final var productFuture = productsStore.delete("prod-1");
        Optional<Result> productIdOptional = productFuture.join();
        assertNotNull(productIdOptional);
        assertNotNull(productIdOptional.get());
        assertNotNull(productIdOptional.get().getId());
    }

    @Test
    void makeMock() {
        final var member = productsStore.makeMock();
        assertNotNull(member);
        assertNotNull(member.getId());
    }
}