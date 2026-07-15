package com.airton.avoidminer.block.entity;

public final class InputBalanceRules {
    private InputBalanceRules() {
    }

    public static int loadPerSlot(int totalItems, int slotCount) {
        if (totalItems <= 0 || slotCount <= 0) return 0;
        return Math.ceilDiv(totalItems, slotCount);
    }

    public static int amountForSlot(int totalItems, int slotCount, int slotIndex) {
        if (totalItems <= 0 || slotCount <= 0 || slotIndex < 0 || slotIndex >= slotCount) return 0;
        int base = totalItems / slotCount;
        return base + (slotIndex < totalItems % slotCount ? 1 : 0);
    }
}
