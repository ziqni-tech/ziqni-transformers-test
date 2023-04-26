package com.ziqni.transformer.test.store;

public class StoreContext {

    private final Boolean liveIntegrationMode;
    private final String accountId;

    public StoreContext(String accountId, Boolean liveIntegrationMode) {
        this.liveIntegrationMode = liveIntegrationMode;
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }

    public Boolean isLiveIntegrationMode() {
        return liveIntegrationMode;
    }

    public static StoreContext StandAlone(){ return new StoreContext("test-account",false); }
}
