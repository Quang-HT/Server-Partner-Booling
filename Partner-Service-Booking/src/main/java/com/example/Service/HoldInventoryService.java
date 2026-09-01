package com.example.Service;

import com.example.Enum.PartnerErrorCode;
import com.example.Exception.AppException;
import com.example.Model.BookingHold;
import com.example.Model.BookingHoldItem;
import com.example.Model.Inventory;
import com.example.Repository.BookingHoldItemRepository;
import com.example.Repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HoldInventoryService {

    private final BookingHoldItemRepository bookingHoldItemRepository;
    private final InventoryRepository inventoryRepository;

    public List<HeldInventory> loadForRead(BookingHold hold) {
        return load(hold, false);
    }

    public List<HeldInventory> loadForUpdate(BookingHold hold) {
        return load(hold, true);
    }

    public void restore(BookingHold hold) {
        List<HeldInventory> heldInventories = loadForUpdate(hold);
        for (HeldInventory heldInventory : heldInventories) {
            Inventory inventory = heldInventory.inventory();
            int currentAvailable = inventory.getAvailableQuantity() == null
                    ? 0
                    : inventory.getAvailableQuantity();
            int restoredAvailable = currentAvailable + heldInventory.quantity();

            if (inventory.getTotalQuantity() != null) {
                restoredAvailable = Math.min(restoredAvailable, inventory.getTotalQuantity());
            }

            inventory.setAvailableQuantity(restoredAvailable);
        }

        inventoryRepository.saveAll(
                heldInventories.stream().map(HeldInventory::inventory).toList()
        );
    }

    private List<HeldInventory> load(BookingHold hold, boolean forUpdate) {
        List<BookingHoldItem> items = bookingHoldItemRepository
                .findAllByHoldIdOrderByInventoryIdAsc(hold.getHoldId());

        Map<String, Integer> quantityByInventoryId = new LinkedHashMap<>();
        for (BookingHoldItem item : items) {
            quantityByInventoryId.put(item.getInventoryId(), item.getQuantity());
        }

        // Giữ tương thích với các hold cũ chỉ có một inventory_id.
        if (quantityByInventoryId.isEmpty()
                && hold.getInventoryId() != null
                && !hold.getInventoryId().isBlank()) {
            quantityByInventoryId.put(hold.getInventoryId(), hold.getQuantity());
        }

        if (quantityByInventoryId.isEmpty()) {
            throw new AppException(PartnerErrorCode.INVENTORY_NOT_FOUND);
        }

        List<String> inventoryIds = List.copyOf(quantityByInventoryId.keySet());
        List<Inventory> inventories = forUpdate
                ? inventoryRepository.findAllByInventoryIdInForUpdate(inventoryIds)
                : inventoryRepository.findAllByInventoryIdIn(inventoryIds);

        if (inventories.size() != inventoryIds.size()) {
            throw new AppException(PartnerErrorCode.INVENTORY_NOT_FOUND);
        }

        return inventories.stream()
                .map(inventory -> new HeldInventory(
                        inventory,
                        quantityByInventoryId.get(inventory.getInventoryId())
                ))
                .toList();
    }

    public record HeldInventory(Inventory inventory, Integer quantity) {
    }
}
