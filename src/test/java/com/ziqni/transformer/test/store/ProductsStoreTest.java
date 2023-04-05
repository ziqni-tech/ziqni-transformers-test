package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformer.test.models.BasicProduct;
import org.junit.jupiter.api.Test;
import scala.Some;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductsStoreTest {

    private final ProductsStore productsStore;

    public ProductsStoreTest() {
        final var accountId = "test-account";
        this.productsStore = new ProductsStore();
        final var ziqniStores = new ZiqniStores(accountId);
        ziqniStores.generateSampleData();
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
        final var productFuture = productsStore.create("test-product-ref-id-1", "test-product", null, null, 2.0, null);
        Optional<String> productIdOptional = productFuture.join();
        assertNotNull(productIdOptional);
        assertNotNull(productIdOptional.get());
    }

    @Test
    void update() {
        final var productFuture = productsStore.update("prod-1" ,new Some<>("test-product-ref-id-1"), new Some<>("updated-test-product"), null, null, null, null);
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