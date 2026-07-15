package com.airton.avoidminer.block.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InputBalanceRulesTest {
    @Test
    public void dividesStacksEvenlyWithoutLosingItems() {
        assertEquals(22, InputBalanceRules.amountForSlot(64, 3, 0));
        assertEquals(21, InputBalanceRules.amountForSlot(64, 3, 1));
        assertEquals(21, InputBalanceRules.amountForSlot(64, 3, 2));
    }

    @Test
    public void reportsTheCurrentMaximumLoad() {
        assertEquals(64, InputBalanceRules.loadPerSlot(64, 1));
        assertEquals(32, InputBalanceRules.loadPerSlot(64, 2));
        assertEquals(22, InputBalanceRules.loadPerSlot(64, 3));
    }
}
