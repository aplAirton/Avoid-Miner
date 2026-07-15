package com.airton.avoidminer.block.entity;

final class XpVaultRules {
    static final int ALL = -1;

    private XpVaultRules() {
    }

    static int withdrawAmount(int storedLevels, int requestedLevels) {
        if (storedLevels <= 0) return 0;
        return requestedLevels == ALL
                ? storedLevels
                : Math.min(storedLevels, Math.max(0, requestedLevels));
    }

    static int depositAmount(int storedLevels, int playerLevels, int requestedLevels) {
        if (playerLevels <= 0 || storedLevels == Integer.MAX_VALUE) return 0;
        int request = requestedLevels == ALL ? playerLevels : Math.max(0, requestedLevels);
        return Math.min(Math.min(playerLevels, request), Integer.MAX_VALUE - storedLevels);
    }
}
