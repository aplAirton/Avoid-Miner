package com.airton.avoidminer.block.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XpVaultRulesTest {
    @Test
    public void withdrawsRequestedLevelsOrEverythingAvailable() {
        assertEquals(1, XpVaultRules.withdrawAmount(20, 1));
        assertEquals(5, XpVaultRules.withdrawAmount(20, 5));
        assertEquals(10, XpVaultRules.withdrawAmount(20, 10));
        assertEquals(3, XpVaultRules.withdrawAmount(3, 10));
        assertEquals(20, XpVaultRules.withdrawAmount(20, XpVaultRules.ALL));
        assertEquals(0, XpVaultRules.withdrawAmount(0, 10));
    }

    @Test
    public void depositsOnlyAvailableLevelsAndNeverOverflows() {
        assertEquals(10, XpVaultRules.depositAmount(0, 30, 10));
        assertEquals(3, XpVaultRules.depositAmount(0, 3, 10));
        assertEquals(30, XpVaultRules.depositAmount(0, 30, XpVaultRules.ALL));
        assertEquals(2, XpVaultRules.depositAmount(Integer.MAX_VALUE - 2, 30, 10));
        assertEquals(0, XpVaultRules.depositAmount(Integer.MAX_VALUE, 30, 10));
        assertEquals(0, XpVaultRules.depositAmount(0, 0, 10));
    }
}
