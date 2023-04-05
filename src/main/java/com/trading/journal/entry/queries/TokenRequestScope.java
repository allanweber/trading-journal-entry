package com.trading.journal.entry.queries;

import com.allanweber.jwttoken.data.AccessTokenInfo;

public class TokenRequestScope {

    private static final InheritableThreadLocal<AccessTokenInfo> REQUEST_THREAD_LOCAL = new InheritableThreadLocal<>();

    public static void set(AccessTokenInfo person) {
        REQUEST_THREAD_LOCAL.set(person);
    }

    public static AccessTokenInfo get() {
        return REQUEST_THREAD_LOCAL.get();
    }

    public static void clear() {
        REQUEST_THREAD_LOCAL.remove();
    }
}
