package com.rhseung.blueprint.registration.key

import com.rhseung.blueprint.tool.ToolType
import com.rhseung.blueprint.file.Loc
import com.rhseung.blueprint.registration.IBaseKey
import com.rhseung.blueprint.registration.Translation
import com.rhseung.blueprint.tool.Tier
import com.rhseung.blueprint.util.Functional.ifNotNull
import com.rhseung.blueprint.util.Languages
import com.rhseung.blueprint.util.Languages.LanguageTable
import com.rhseung.blueprint.tool.ToolLevel
import com.rhseung.blueprint.util.Utils.langcase
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.block.AbstractBlock.*
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.enums.NoteBlockInstrument
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.resource.featuretoggle.FeatureFlag
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import java.util.function.ToIntFunction

class BaseBlock(
    /**
     * The ID of the block. This is used to register the block.
     */
    override val id: Loc,

    /**
     * The settings of the block.
     */
    val properties: Properties,

    /**
     * The settings of the blockitem.
     */
    val itemProperties: BaseItem.Properties = BaseItem.Properties()
) : Block(properties.build()), IBaseKey {

    /**
     * The language table of the item. The key is the language code, and the value is the translated string.
     */
    override val langs = LanguageTable(Languages.EN_US to id.path.toString().langcase())

    init {
        properties.langs.forEach { lang, name ->
            name.ifNotNull { langs[lang] = it }
        }
    }

    /**
     * The blockitem of the block.
     */
    val item = BaseBlockItem(this, itemProperties)

    /**
     * Registers the block to the [Registry].
     */
    override fun register() {
        Registry.register(Registries.BLOCK, id.toIdentifier(), this)
        item.register()

        properties.itemGroup.ifNotNull {
            ItemGroupEvents.modifyEntriesEvent(it)
                .register(ItemGroupEvents.ModifyEntries { entries -> entries.add(this) })
        }
    }

    /**
     * Returns the string representation of the block.
     */
    override fun toString(): String {
        return "Block($id)"
    }

    class Properties {
        /**
         * The item group that the block will be in. If null, the block will not be in any item group.
         */
        internal var itemGroup: Group? = null

        fun vanillaGroup(value: () -> Group) {
            this.itemGroup = value()
        }

        fun itemGroup(value: () -> BaseItemGroup) {
            this.itemGroup = value().registrykey
        }

        /**
         * The sound group of the block. it contains break sound, step sound, place sound, hit sound, and fall sound.
         */
        internal var soundGroup: BlockSoundGroup = BlockSoundGroup.STONE

        fun soundGroup(value: () -> BlockSoundGroup) {
            this.soundGroup = value()
        }

        /**
         * Light level of the block.
         */
        internal var luminance: ToIntFunction<BlockState> = ToIntFunction { 0 }

        fun luminance(value: (state: BlockState) -> Int) {
            this.luminance = ToIntFunction { state -> value(state) }
        }

        /**
         * Map color of the block.
         */
        internal var mapColor: (BlockState) -> MapColor = { MapColor.CLEAR }

        @JvmName("mapColorMapColor")
        fun mapColor(value: () -> MapColor) {
            this.mapColor = { value() }
        }

        @JvmName("mapColorDyeColor")
        fun mapColor(value: () -> DyeColor) {
            this.mapColor = { value().mapColor }
        }

        fun mapColor(value: (state: BlockState) -> MapColor) {
            this.mapColor = value
        }

        /**
         * Collidable of the block. if it is false, entities can pass through the block.
         */
        internal var collidable: Boolean = true

        fun collidable(value: () -> Boolean) {
            this.collidable = value()
        }

        /**
         * Opaque of the block. If it is false, the block will be transparent.
         */
        internal var opaque: Boolean = true

        fun transparent(value: () -> Boolean) {
            this.opaque = !value()
        }

        /**
         * Resistance of the block. It is resistance to explosions.
         */
        internal var resistance: Float = 0.0f

        fun resistance(value: () -> Float) {
            this.resistance = value()
        }

        /**
         * Hardness of the block. It is how long it takes to break the block.
         */
        internal var hardness: Float = 0.0f

        fun hardness(value: () -> Float) {
            this.hardness = value()
        }

        /**
         * Least tool level and type to break the block.
         */
        internal var requiredToolLevel: ToolLevel = ToolType.NONE(Tier.HAND)

        fun requiredToolLevel(value: () -> ToolLevel) {
            this.requiredToolLevel = value()
        }

        /**
         * Random ticks of the block. If it is true, the block will be updated randomly.
         */
        internal var randomTicks: Boolean = false

        fun randomTicks(value: () -> Boolean) {
            this.randomTicks = value()
        }

        /**
         * Slipperiness of the block.
         */
        internal var slipperiness: Float = 0.6f

        fun slipperiness(value: () -> Float) {
            this.slipperiness = value()
        }

        /**
         * Velocity multiplier of the block.
         */
        internal var runVelocityMultiplier: Float = 1.0f

        fun runVelocityMultiplier(value: () -> Float) {
            this.runVelocityMultiplier = value()
        }

        /**
         * Jump velocity multiplier of the block.
         */
        internal var jumpVelocityMultiplier: Float = 1.0f

        fun jumpVelocityMultiplier(value: () -> Float) {
            this.jumpVelocityMultiplier = value()
        }

        /**
         * Block is air or not.
         */
        internal var isAir: Boolean = false

        fun air(value: () -> Boolean) {
            this.isAir = value()
        }

        /**
         * Burnable of the block. If it is true, the block can be burned.
         */
        internal var burnable: Boolean = false

        fun burnable(value: () -> Boolean) {
            this.burnable = value()
        }

        /**
         * Forces the block to be solid.
         */
        internal var forceSolid: Boolean = false

        fun solid(value: () -> Boolean) {
            this.forceSolid = value()
        }

        /**
         * Piston behavior of the block. It is how the block behaves when pushed by a piston.
         * [PistonBehavior.NORMAL] is the default behavior.
         */
        internal var pistonBehavior: PistonBehavior = PistonBehavior.NORMAL

        fun pistonBehavior(value: () -> PistonBehavior) {
            this.pistonBehavior = value()
        }

        /**
         * Shows the block breaking particles or not.
         */
        internal var showBlockBreakParticle: Boolean = true

        fun breakParticle(value: () -> Boolean) {
            this.showBlockBreakParticle = value()
        }

        /**
         * Noteblock instrument of the block.
         */
        internal var instrument: NoteBlockInstrument = NoteBlockInstrument.HARP

        fun instrument(value: () -> NoteBlockInstrument) {
            this.instrument = value()
        }

        /**
         * Replaceable of the block.
         */
        internal var replaceable: Boolean = false

        fun replaceable(value: () -> Boolean) {
            this.replaceable = value()
        }

        /**
         *
         */
        internal var allowsSpawningPredicate: TypedContextPredicate<EntityType<*>>
            = TypedContextPredicate { state, world, pos, _ -> state.isSideSolidFullSquare(world, pos, Direction.UP) && state.luminance < 14 }

        fun allowsSpawning(predicate: TypedContextPredicate<EntityType<*>>) {
            this.allowsSpawningPredicate = predicate
        }

        /**
         *
         */
        internal var solidBlockPredicate = ContextPredicate { state, world, pos -> state.isFullCube(world, pos) }

        fun solidBlock(value: (state: BlockState, world: BlockView, pos: BlockPos) -> Boolean) {
            this.solidBlockPredicate = ContextPredicate(value)
        }

        /**
         *
         */
        internal var suffocationPredicate = ContextPredicate { state, world, pos -> state.blocksMovement() && state.isFullCube(world, pos) }

        fun suffocates(value: (state: BlockState, world: BlockView, pos: BlockPos) -> Boolean) {
            this.suffocationPredicate = ContextPredicate(value)
        }

        /**
         *
         */
        internal var blockVisionPredicate = ContextPredicate { state, world, pos -> state.blocksMovement() && state.isFullCube(world, pos) }

        fun blocksVision(value: (state: BlockState, world: BlockView, pos: BlockPos) -> Boolean) {
            this.blockVisionPredicate = ContextPredicate(value)
        }

        /**
         *
         */
        internal var postProcessPredicate = ContextPredicate { state, world, pos -> false }

        fun postProcess(value: (state: BlockState, world: BlockView, pos: BlockPos) -> Boolean) {
            this.postProcessPredicate = ContextPredicate(value)
        }

        /**
         *
         */
        internal var emissiveLightingPredicate = ContextPredicate { state, world, pos -> false }

        fun emissiveLighting(value: (state: BlockState, world: BlockView, pos: BlockPos) -> Boolean) {
            this.emissiveLightingPredicate = ContextPredicate(value)
        }

        /**
         *
         */
        internal var dynamicBounds: Boolean = false

        fun dynamicBounds(value: () -> Boolean) {
            this.dynamicBounds = value()
        }

        /**
         * The required features for the block. default is [FeatureFlags.VANILLA_FEATURES].
         */
        internal var requiredFeatures: Array<FeatureFlag> = arrayOf()

        @JvmName("requiredFeaturesArray")
        fun requiredFeatures(value: () -> Array<FeatureFlag>) {
            this.requiredFeatures = value()
        }

        @JvmName("requiredFeaturesCollection")
        fun requiredFeatures(value: () -> Collection<FeatureFlag>) {
            this.requiredFeatures = value().toTypedArray()
        }

        /**
         *
         */
        internal var offsetType: OffsetType = OffsetType.NONE

        fun offset(value: () -> OffsetType) {
            this.offsetType = value()
        }

        /**
         * The language table of the item. The key is the language code, and the value is the translated string.
         */
        internal val langs = LanguageTable()

        fun lang(value: () -> Translation) {
            langs[value().language] = value().translation
        }

        /**
         * Builds the settings into an [AbstractBlock.Settings] object.
         */
        fun build(): Settings {
            return Settings.create()
                .sounds(soundGroup)
                .luminance(luminance)
                .strength(hardness, resistance)
                .mapColor(mapColor)
                .slipperiness(slipperiness)
                .velocityMultiplier(runVelocityMultiplier)
                .jumpVelocityMultiplier(jumpVelocityMultiplier)
                .pistonBehavior(pistonBehavior)
                .instrument(instrument)
                .allowsSpawning(allowsSpawningPredicate)
                .solidBlock(solidBlockPredicate)
                .suffocates(suffocationPredicate)
                .blockVision(blockVisionPredicate)
                .postProcess(postProcessPredicate)
                .emissiveLighting(emissiveLightingPredicate)
                .requires(*requiredFeatures)
                .offset(offsetType)
                .apply {
                    if (!collidable)
                        noCollision()
                    if (requiredToolLevel.hasType() && !requiredToolLevel.isHand())
                        requiresTool()
                    if (randomTicks)
                        ticksRandomly()
                    if (isAir)
                        air()
                    if (burnable)
                        burnable()
                    if (forceSolid)
                        solid()
                    if (!showBlockBreakParticle)
                        noBlockBreakParticles()
                    if (!opaque)
                        nonOpaque()
                    if (replaceable)
                        replaceable()
                    if (dynamicBounds)
                        dynamicBounds()
                }
        }
    }
}