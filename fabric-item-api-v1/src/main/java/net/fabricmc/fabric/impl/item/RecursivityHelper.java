package net.fabricmc.fabric.impl.item;

import java.util.function.Supplier;

public final class RecursivityHelper {
    public static final ThreadLocal<Boolean> FORGE_CALL = ThreadLocal.withInitial(() -> false);

    public static <T> T nonRecursiveApiCall(Supplier<T> supplier) {
        FORGE_CALL.set(true);
        T result = supplier.get();
        FORGE_CALL.set(false);
        return result;
    }

    public static boolean allowForgeCall() {
        return !FORGE_CALL.get();
    }

    private RecursivityHelper() {
    }
}
