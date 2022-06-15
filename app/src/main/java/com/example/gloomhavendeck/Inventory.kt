package com.example.gloomhavendeck
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable

@RequiresApi(Build.VERSION_CODES.N)
@Serializable
open class Inventory {
    var usableItems = mutableListOf(
        Item.CLOAK_OF_POCKETS,
        Item.MAJOR_CURE_POTION,
        Item.MAJOR_POWER_POTION,
        Item.MAJOR_STAMINA_POTION,
        Item.MINOR_STAMINA_POTION,
        Item.PENDANT_OF_DARK_PACTS,
        Item.RING_OF_BRUTALITY,
        Item.RING_OF_SKULLS,
        Item.ROCKET_BOOTS,
        Item.SPIKED_SHIELD,
        Item.SUPER_HEALING_POTION,
        Item.TOWER_SHIELD,
    )
    var activeItems = mutableListOf<Item>()
    var unusableItems = mutableListOf<Item>()

    // Manage directly
    fun loseItem(item: Item) {
        if (!usableItems.contains(item)) {
            throw Exception("Don't have a $item in usable!")
        }
        usableItems.remove(item)
        unusableItems.add(item)
    }

    fun regainItem(item: Item) {
        if (!unusableItems.contains(item)) {
            throw Exception("Don't have a $item in unusable!")
        }
        unusableItems.remove(item)
        usableItems.add(item)
    }

    open fun useItem(player: Player, deck: Deck, item: Item) {
        if (!usableItems.contains(item)) {
            throw Exception("You don't HAVE a $item, dumbass")
        }
        item.getUsed(player, deck)
        loseItem(item)
    }

    // Analyze
    class RecoveryCandidate(var item: Item, var useImmediately: Boolean)
    fun getRecoveryCandidates(player: Player, inventory: Inventory) = sequence {
        while (true) {
            if (unusableItems.contains(Item.PENDANT_OF_DARK_PACTS)
                && inventory.unusableItems.size >= 3 // 3, not 2, because of itself
            ) {
                yield(RecoveryCandidate(Item.PENDANT_OF_DARK_PACTS, true))
                continue
            }
            if (unusableItems.contains(Item.PENDANT_OF_DARK_PACTS)) {
                yield(RecoveryCandidate(Item.PENDANT_OF_DARK_PACTS, false))
                continue
            }
            if (unusableItems.contains(Item.MAJOR_STAMINA_POTION)) {
                yield(RecoveryCandidate(Item.MAJOR_STAMINA_POTION, false))
                continue
            }
            if (unusableItems.contains(Item.RING_OF_BRUTALITY)) {
                yield(RecoveryCandidate(Item.RING_OF_BRUTALITY, false))
                continue
            }
            if (unusableItems.contains(Item.MAJOR_POWER_POTION)) {
                yield(RecoveryCandidate(Item.MAJOR_POWER_POTION, false))
                continue
            }
            if (unusableItems.contains(Item.MAJOR_POWER_POTION)) {
                yield(RecoveryCandidate(Item.MAJOR_POWER_POTION, false))
                continue
            }
            if (unusableItems.contains(Item.RING_OF_BRUTALITY)) {
                yield(RecoveryCandidate(Item.RING_OF_BRUTALITY, false))
                continue
            }
            if (unusableItems.contains(Item.MAJOR_CURE_POTION)
                && (
                        player.statuses.contains(Status.MUDDLE)
                                || player.statuses.contains(Status.POISON)
                        )
            ) {
                yield(RecoveryCandidate(Item.MAJOR_CURE_POTION, true))
                continue
            }
            if (unusableItems.contains(Item.SUPER_HEALING_POTION)
                && player.hp <= player.hpDangerThreshold) {
                yield(RecoveryCandidate(Item.SUPER_HEALING_POTION, true))
                continue
            }
            if (unusableItems.contains(Item.MAJOR_POWER_POTION)) {
                yield(RecoveryCandidate(Item.MAJOR_CURE_POTION, false))
                continue
            }
            if (unusableItems.contains(Item.MAJOR_CURE_POTION)) {
                yield(RecoveryCandidate(Item.MAJOR_CURE_POTION, false))
                continue
            }
            if (unusableItems.contains(Item.SUPER_HEALING_POTION)) {
                yield(RecoveryCandidate(Item.SUPER_HEALING_POTION, player.hp <= 19))
                continue
            }
            if (unusableItems.contains(Item.RING_OF_SKULLS)) {
                yield(RecoveryCandidate(Item.RING_OF_SKULLS, false))
                continue
            }
            if (unusableItems.contains(Item.MINOR_STAMINA_POTION)) {
                yield(RecoveryCandidate(Item.MINOR_STAMINA_POTION, false))
                continue
            }
            if (unusableItems.contains(Item.TOWER_SHIELD)) {
                yield(RecoveryCandidate(Item.TOWER_SHIELD, false))
                continue
            }
            if (unusableItems.contains(Item.SPIKED_SHIELD)) {
                yield(RecoveryCandidate(Item.SPIKED_SHIELD, false))
                continue
            }
            break
        }
    }

    fun getRoomMakingItems(player: Player, begrudgingPowerPot: Boolean = false, reallyBegrudgingPowerPot: Boolean = false) = sequence{
        while (true) {
            if (usableItems.contains(Item.MAJOR_CURE_POTION)
                && (
                        player.statuses.contains(Status.MUDDLE)
                                || player.statuses.contains(Status.POISON)
                        )
            ) {
                yield(Item.MAJOR_CURE_POTION)
                continue
            }
            if (usableItems.contains(Item.SUPER_HEALING_POTION)
                && player.hp <= player.maxHp-7) {
                yield(Item.SUPER_HEALING_POTION)
                continue
            }
            if (usableItems.contains(Item.MAJOR_CURE_POTION)
                && player.statuses.any{it.negative}
            ) {
                yield(Item.MAJOR_CURE_POTION)
                continue
            }
            // Johnson should go here
            if (usableItems.contains(Item.MAJOR_POWER_POTION)
                && begrudgingPowerPot
            ) {
                yield(Item.MAJOR_POWER_POTION)
                continue
            }
            if (usableItems.contains(Item.SUPER_HEALING_POTION)
                && player.hp < player.maxHp
            ) {
                yield(Item.SUPER_HEALING_POTION)
                continue
            }
            if (usableItems.contains(Item.MINOR_STAMINA_POTION)
                && player.discardedCards >= 2
            ) {
                yield(Item.MINOR_STAMINA_POTION)
                continue
            }
            if (usableItems.contains(Item.MAJOR_POWER_POTION)
                && reallyBegrudgingPowerPot
            ) {
                yield(Item.MAJOR_POWER_POTION)
                continue
            }
            break
        }
    }

    fun recover(player: Player, deck: Deck, howMany: Int = 1): MutableList<RecoveryCandidate> {
        // Pendant logic is kept separate because otherwise it could try to revive itself
        // and other such nonsense
        // Separate from makeRoom because the items you'd use if forced to in order to make room
        // generally aren't the same as the items you'd want to replenish ASAP if already used

        // viaPendant is for when it wasn't recovered with a card, but rather is going "I need
        // to stop having a pendant", in which case exactly two items must be used
        val itemsRecovered = mutableListOf<RecoveryCandidate>()
        for (recoveryCandidate in getRecoveryCandidates(player, this)) {
            itemsRecovered.add(recoveryCandidate)
            regainItem(recoveryCandidate.item)
            if (itemsRecovered.size >= howMany) {
                break
            }
        }
        for (recoveryCandidate in itemsRecovered) {
            if (recoveryCandidate.useImmediately) {
                player.useItem(recoveryCandidate.item, deck)
                // log("Recovered and immediately used ${recoveryCandidate.item}")
            } else {
                // log("Recovered ${recoveryCandidate.item}")
            }
        }
        if (itemsRecovered.size == 0) {
            // log("Nothing to recover :c")
        }
        return itemsRecovered
    }

    fun makeRoom(player: Player, deck: Deck, howMuchRoom: Int =2): MutableList<Item> {
        // Attempting to, in pendant is recovered at the start of an attack, make there be two
        // used items to use the pendant on
        // Separate from recover because the items you'd use if forced to in order to make room
        // generally aren't the same as the items you'd want to replenish ASAP if already used
        val itemsConsumed = mutableListOf<Item>()
        for (roomMakingItem in getRoomMakingItems(player)) {
            // Put at the start so that if it's already fine it just bails
            if (unusableItems.size >= howMuchRoom) {
                break
            }
            player.useItem(roomMakingItem, deck)
            //log("Used $roomMakingItem in order to free up some room.")
            itemsConsumed.add(roomMakingItem)
        }
        return itemsConsumed
    }
}