package com.airton.avoidminer.block.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

final class MachineInputBalancer {
    private MachineInputBalancer() {
    }

    static boolean balance(ItemStacksResourceHandler handler, int inputStart, int inputCount) {
        Map<ItemResource, Group> groups = new LinkedHashMap<>();
        List<Integer> emptySlots = new ArrayList<>();

        for (int offset = 0; offset < inputCount; offset++) {
            int slot = inputStart + offset;
            ItemResource resource = handler.getResource(slot);
            int amount = handler.getAmountAsInt(slot);
            if (resource.isEmpty() || amount <= 0) {
                emptySlots.add(slot);
                continue;
            }
            Group group = groups.computeIfAbsent(resource, ignored -> new Group(resource));
            group.total += amount;
            group.slots.add(slot);
        }

        while (!emptySlots.isEmpty()) {
            Group best = null;
            int highestLoad = 1;
            for (Group group : groups.values()) {
                int load = InputBalanceRules.loadPerSlot(group.total, group.slots.size());
                if (load > highestLoad) {
                    highestLoad = load;
                    best = group;
                }
            }
            if (best == null) break;
            best.slots.add(emptySlots.removeFirst());
        }

        boolean changed = false;
        for (Group group : groups.values()) {
            for (int index = 0; index < group.slots.size(); index++) {
                int slot = group.slots.get(index);
                int target = InputBalanceRules.amountForSlot(group.total, group.slots.size(), index);
                if (!handler.getResource(slot).equals(group.resource)
                        || handler.getAmountAsInt(slot) != target) {
                    changed = true;
                }
            }
        }
        if (!changed) return false;

        for (int offset = 0; offset < inputCount; offset++) {
            handler.set(inputStart + offset, ItemResource.EMPTY, 0);
        }
        for (Group group : groups.values()) {
            for (int index = 0; index < group.slots.size(); index++) {
                int amount = InputBalanceRules.amountForSlot(group.total, group.slots.size(), index);
                handler.set(group.slots.get(index), group.resource, amount);
            }
        }
        return true;
    }

    private static final class Group {
        private final ItemResource resource;
        private final List<Integer> slots = new ArrayList<>();
        private int total;

        private Group(ItemResource resource) {
            this.resource = resource;
        }
    }
}
