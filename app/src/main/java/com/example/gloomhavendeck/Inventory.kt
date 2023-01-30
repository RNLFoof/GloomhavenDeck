package com.example.gloomhavendeck
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RequiresApi(Build.VERSION_CODES.O)
@Serializable
class Inventory(@Transient override var controller: Controller = Controller(destroyTheUniverseUponInitiation = true)): Controllable(controller) {
    init {
        controller.inventory = this
    }

    var usableItems = mutableListOf<Item>()
    var activeItems = mutableListOf<Item>()
    var unusableItems = mutableListOf<Item>()

    // Starutup
    fun initializeThreeSpears() {
        usableItems = mutableListOf(
            Item.CLOAK_OF_POCKETS,
            Item.SECOND_CHANCE_RING,
            Item.LUCKY_EYE,
            Item.MAJOR_STAMINA_POTION,
            Item.PENDANT_OF_DARK_PACTS,
            Item.RING_OF_BRUTALITY,
            Item.MAJOR_CURE_POTION,
            //Item.RING_OF_SKULLS,
            Item.ROCKET_BOOTS,
            Item.SUPER_HEALING_POTION,
            Item.WALL_SHIELD,
            Item.UTILITY_BELT,
        )
    }

    fun initializeEye() {
        usableItems = mutableListOf(
            Item.CLOAK_OF_PHASING,
            Item.MAJOR_CURE_POTION,
            Item.MAJOR_STAMINA_POTION,
            Item.PENDANT_OF_DARK_PACTS,
            Item.RING_OF_DUALITY,
            Item.ROCKET_BOOTS,
            Item.SUPER_HEALING_POTION,
            Item.UTILITY_BELT,
            Item.WAR_HAMMER,
        )
    }

    fun allItemsSorted(): List<Item> {
        return (usableItems + unusableItems + activeItems).sortedBy { it.name }
    }

    // Manage directly
    fun loseItem(item: Item) {
        if (usableItems.contains(item)) {
            usableItems.remove(item)
        } else if (activeItems.contains(item)) {
            activeItems.remove(item)
        } else {
            throw Exception("Don't have a $item in usable or active!")
        }
        unusableItems.add(item)
        displayChangedInventory()
    }

    fun activateItem(item: Item) {
        if (!usableItems.contains(item)) {
            throw Exception("Don't have a $item in usable!")
        }
        usableItems.remove(item)
        activeItems.add(item)
        displayChangedInventory()
    }

    fun regainItem(item: Item) {
        if (!unusableItems.contains(item)) {
            throw Exception("Don't have a $item in unusable!")
        }
        unusableItems.remove(item)
        usableItems.add(item)
        displayChangedInventory()
    }

    fun useItem(item: Item, fullAutoBehavior: Boolean) {
        controller.logger?.log("Using a $item...")
        controller.logger?.let {it.logIndent += 1}

        if (!usableItems.contains(item)) {
            throw Exception("You don't HAVE a $item, dumbass")
        }

        try {
            item.getUsed(controller, fullAutoBehavior)
            controller.activityConnector?.effectQueue?.add(Effect(controller, card = item.graphic, sound = item.sound, selectTopRow = true))

            if (item.getsActivated) {
                activateItem(item)
            }
            else {
                loseItem(item)
            }
            displayChangedInventory()

        } catch (e: ItemUnusableException) {
            controller.activityConnector?.effectQueue?.add(Effect(controller, sound = SoundBundle.ITEMUNUSABLE))
        }
        controller.logger?.let {it.logIndent -= 1}
    }

    fun deactivateItem(item: Item, fullAutoBehavior: Boolean) {
        controller.activityConnector?.effectQueue?.add(Effect(controller, card = item.graphic, sound = item.sound, selectTopRow = true))
        controller.logger?.log("Deactivating $item...")
        controller.logger?.let {it.logIndent += 1}

        if (!activeItems.contains(item)) {
            throw Exception("You don't HAVE an active $item, dumbass")
        }
        item.getDeactivated(controller, fullAutoBehavior)
        loseItem(item)
        displayChangedInventory()

        controller.logger?.let {it.logIndent -= 1}
    }

    // Analyze
    class RecoveryCandidate(var item: Item, var useImmediately: Boolean)
    fun getRecoveryCandidates(cantRecover: List<Item> = listOf()) = sequence {
        if (unusableItems.contains(Item.UTILITY_BELT)) {
            yield(RecoveryCandidate(Item.UTILITY_BELT, false))
        }
        if (unusableItems.contains(Item.PENDANT_OF_DARK_PACTS)
            && unusableItems.size >= 3 // 3, not 2, because of itself
        ) {
            yield(RecoveryCandidate(Item.PENDANT_OF_DARK_PACTS, true))
        }
        if (unusableItems.contains(Item.PENDANT_OF_DARK_PACTS)) {
            yield(RecoveryCandidate(Item.PENDANT_OF_DARK_PACTS, false))
        }
        if (unusableItems.contains(Item.LUCKY_EYE)
        ) {
            yield(RecoveryCandidate(Item.LUCKY_EYE, !controller.player!!.statuses.contains(Status.STRENGTHEN)))
        }
        if (unusableItems.contains(Item.MAJOR_STAMINA_POTION)) {
            yield(RecoveryCandidate(Item.MAJOR_STAMINA_POTION, false))
        }
        if (unusableItems.contains(Item.RING_OF_BRUTALITY)) {
            yield(RecoveryCandidate(Item.RING_OF_BRUTALITY, false))
        }
        if (unusableItems.contains(Item.MAJOR_POWER_POTION)) {
            yield(RecoveryCandidate(Item.MAJOR_POWER_POTION, false))
        }
        if (unusableItems.contains(Item.RING_OF_BRUTALITY)) {
            yield(RecoveryCandidate(Item.RING_OF_BRUTALITY, false))
        }
        if (unusableItems.contains(Item.MAJOR_CURE_POTION)
            && (
                    controller.player!!.statuses.contains(Status.MUDDLE)
                            || controller.player!!.statuses.contains(Status.POISON)
                    )
        ) {
            yield(RecoveryCandidate(Item.MAJOR_CURE_POTION, true))
        }
        if (unusableItems.contains(Item.SECOND_CHANCE_RING)) {
            yield(RecoveryCandidate(Item.SECOND_CHANCE_RING, false))
        }
        if (unusableItems.contains(Item.SUPER_HEALING_POTION)
            && controller.player!!.hp <= controller.player!!.hpDangerThreshold) {
            yield(RecoveryCandidate(Item.SUPER_HEALING_POTION, true))
        }
        if (unusableItems.contains(Item.MAJOR_CURE_POTION)) {
            yield(RecoveryCandidate(Item.MAJOR_CURE_POTION, false))
        }
        if (unusableItems.contains(Item.SUPER_HEALING_POTION)) {
            yield(RecoveryCandidate(Item.SUPER_HEALING_POTION, controller.player!!.hp <= 19))
        }
        if (unusableItems.contains(Item.RING_OF_SKULLS)) {
            yield(RecoveryCandidate(Item.RING_OF_SKULLS, false))
        }
        if (unusableItems.contains(Item.MINOR_STAMINA_POTION)) {
            yield(RecoveryCandidate(Item.MINOR_STAMINA_POTION, false))
        }
        if (unusableItems.contains(Item.TOWER_SHIELD)) {
            yield(RecoveryCandidate(Item.TOWER_SHIELD, false))
        }
        if (unusableItems.contains(Item.SPIKED_SHIELD)) {
            yield(RecoveryCandidate(Item.SPIKED_SHIELD, false))
        }
        if (unusableItems.contains(Item.WALL_SHIELD)) {
            yield(RecoveryCandidate(Item.WALL_SHIELD, false))
        }
    }

    fun getRoomMakingItems(begrudgingPowerPot: Boolean = false, reallyBegrudgingPowerPot: Boolean = false) = sequence{
        while (true) {
            // Just gonna assume this all exists since you only use this in pipis
            if (usableItems.contains(Item.LUCKY_EYE)
                && !controller.player!!.statuses.contains(Status.STRENGTHEN) ?: true
            ) {
                yield(Item.LUCKY_EYE)
                continue
            }
            if (usableItems.contains(Item.MAJOR_CURE_POTION)
                && (
                        controller.player!!.statuses.contains(Status.MUDDLE)
                                || controller.player!!.statuses.contains(Status.POISON)
                        )
            ) {
                yield(Item.MAJOR_CURE_POTION)
                continue
            }
            if (usableItems.contains(Item.SUPER_HEALING_POTION)
                && controller.player!!.hp <= controller.player!!.maxHp-7) {
                yield(Item.SUPER_HEALING_POTION)
                continue
            }
            if (usableItems.contains(Item.MAJOR_CURE_POTION)
                && controller.player!!.statuses.any{it.negative}
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
                && controller.player!!.hp < controller.player!!.maxHp
            ) {
                yield(Item.SUPER_HEALING_POTION)
                continue
            }
            if (usableItems.contains(Item.MINOR_STAMINA_POTION)
                && controller.player!!.discardedCards >= 2
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

    fun recover(howMany: Int = 1, cantRecover: List<Item> = listOf()): MutableList<RecoveryCandidate> {
        // Pendant logic is kept separate because otherwise it could try to revive itself
        // and other such nonsense
        // Separate from makeRoom because the items you'd use if forced to in order to make room
        // generally aren't the same as the items you'd want to replenish ASAP if already used

        // viaPendant is for when it wasn't recovered with a card, but rather is going "I need
        // to stop having a pendant", in which case exactly two items must be used

        // Just gonna assume this all exists since you only use this in pipis
        val itemsRecovered = mutableListOf<RecoveryCandidate>()
        for (recoveryCandidate in getRecoveryCandidates()) {
            if (cantRecover.contains(recoveryCandidate.item)) {
                continue
            }
            itemsRecovered.add(recoveryCandidate)
            regainItem(recoveryCandidate.item)
            if (itemsRecovered.size >= howMany) {
                break
            }
        }
        for (recoveryCandidate in itemsRecovered) {
            if (recoveryCandidate.useImmediately) {
                useItem(recoveryCandidate.item, true)
                controller.logger?.log("Recovered and immediately used ${recoveryCandidate.item}")
            } else {
                controller.logger?.log("Recovered ${recoveryCandidate.item}")
            }
        }
        if (itemsRecovered.size == 0) {
            controller.logger?.log("Nothing to recover :c")
        }
        return itemsRecovered
    }

    fun makeRoom(howMuchRoom: Int =2,
                 begrudgingPowerPot: Boolean = false, reallyBegrudgingPowerPot: Boolean = false
    ): MutableList<Item> {
        // Attempt to, if pendant is recovered at the start of an attack, make there be two
        // used items to use the pendant on
        // Separate from recover because the items you'd use if forced to in order to make room
        // generally aren't the same as the items you'd want to replenish ASAP if already used
        val itemsConsumed = mutableListOf<Item>()
        for (roomMakingItem in getRoomMakingItems(begrudgingPowerPot, reallyBegrudgingPowerPot)) {
            // Put at the start so that if it's already fine it just bails
            if (itemsConsumed.size >= howMuchRoom) {
                break
            }
            useItem(roomMakingItem, true)
            //log("Used $roomMakingItem in order to free up some room.")
            itemsConsumed.add(roomMakingItem)
        }
        return itemsConsumed
    }

    fun displayChangedInventory() {
        controller.activityConnector?.let {
            if (it.llItemRow.isVisible) {
                val newItemRowDisplay = mutableListOf<Boolean>()
                for (item in controller.inventory!!.allItemsSorted()) {
                    newItemRowDisplay.add(item in controller.inventory!!.usableItems)
                }
                it.effectQueue.add(Effect(controller, newItemRowDisplay = newItemRowDisplay))
            }
        }
    }
}