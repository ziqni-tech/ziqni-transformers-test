package com.ziqni.transformer.test.store;

import com.ziqni.admin.sdk.model.Result;
import com.ziqni.transformer.test.utils.ScalaUtils;
import org.junit.jupiter.api.Test;
import scala.Option;
import scala.Some;
import scala.collection.immutable.Map$;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductsStoreTest {

    private final ProductsStore productsStore;

    public ProductsStoreTest() {
        var ziqniStores = new ZiqniStores(StoreContext.StandAlone());
        ziqniStores.generateSampleData();
        this.productsStore = ziqniStores.productsStore;
    }

    @Test
    void getIdByReferenceId() {
        final var productCompletableFuture = productsStore.getByReferenceId("test-product-ref-id1");
        final var product = productCompletableFuture.join();
        assertNotNull(product);
        assertNotNull(product.getProductId());
        assertEquals(product.getProductReferenceId(), "test-product-ref-id1");
    }

    @Test
    void getRefIdByProductId() {
        final var productIdFuture = productsStore.getRefIdByProductId("prod-1");
        final var productId = productIdFuture.join();
        assertNotNull(productId);

    }

    @Test
    void findZiqniProductById() {
        final var productById = productsStore.findZiqniProductById("prod-1");
        final var basicProductOptional = productById.join();
        assertNotNull(basicProductOptional);
        assertNotNull(basicProductOptional.getMetaData());
    }

    @Test
    void create() {
        final var productFuture = productsStore.create("test-product-ref-id-1", "test-product", ScalaUtils.emptySeqString, "test", 2.0, Map$.MODULE$.empty());
        final var productIdOptional = productFuture.join();
        assertNotNull(productIdOptional);
        assertNotNull(productIdOptional.getProductId());
    }

    @Test
    void update() {
        final var productFuture = productsStore.update("prod-1" ,new Some<>("test-product-ref-id-1"), new Some<>("updated-test-product"), Option.empty(), Option.apply("test"), Option.apply(1.0), Option.empty());
        assertFalse(productFuture.isCompletedExceptionally());
        final var productIdOptional = productFuture.join();
        assertNotNull(productIdOptional);
        assertNotNull(productIdOptional.getProductId());
    }

    @Test
    void delete() {
        final var productFuture = productsStore.delete("prod-1");
        Optional<Result> productIdOptional = productFuture.join();
        assertNotNull(productIdOptional);
        assertTrue(productIdOptional.isPresent());
        assertNotNull(productIdOptional.get().getId());
    }

    @Test
    void makeMock() {
        final var member = productsStore.makeMock();
        assertNotNull(member);
        assertNotNull(member.getId());
    }
}