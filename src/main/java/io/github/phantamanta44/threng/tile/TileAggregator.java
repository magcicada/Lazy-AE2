package io.github.phantamanta44.threng.tile;

import io.github.phantamanta44.libnine.capability.impl.L9AspectInventory;
import io.github.phantamanta44.libnine.capability.impl.L9AspectSlot;
import io.github.phantamanta44.libnine.capability.provider.CapabilityBrokerDirPredicated;
import io.github.phantamanta44.libnine.recipe.output.ItemStackOutput;
import io.github.phantamanta44.libnine.tile.RegisterTile;
import io.github.phantamanta44.libnine.util.collection.Accrue;
import io.github.phantamanta44.libnine.util.data.serialization.AutoSerialize;
import io.github.phantamanta44.libnine.util.tuple.ITriple;
import io.github.phantamanta44.libnine.util.world.IAllocableSides;
import io.github.phantamanta44.libnine.util.world.SideAlloc;
import io.github.phantamanta44.threng.constant.ThrEngConst;
import io.github.phantamanta44.threng.recipe.AggRecipe;
import io.github.phantamanta44.threng.recipe.component.TriItemInput;
import io.github.phantamanta44.threng.tile.base.TileSimpleProcessor;
import io.github.phantamanta44.threng.util.InvUtils;
import io.github.phantamanta44.threng.util.SlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@RegisterTile(ThrEngConst.MOD_ID)
public class TileAggregator
        extends TileSimpleProcessor<ITriple<ItemStack, ItemStack, ItemStack>, ItemStack, TriItemInput, ItemStackOutput, AggRecipe> {

    private static final int ENERGY_MAX = 100000;

    @AutoSerialize
    private final L9AspectInventory invInput = new L9AspectInventory.Observable(3, (s, o, n) -> markWorkStateDirty());
    @AutoSerialize
    private final L9AspectSlot slotOutput = new L9AspectSlot.Observable(is -> false, (s, o, n) -> markWorkStateDirty());
    @AutoSerialize
    private final SideAlloc<SlotType.BasicIO> sides = new SideAlloc<>(SlotType.BasicIO.NONE, this::getFrontFace);

    public TileAggregator() {
        super(AggRecipe.class, ENERGY_MAX);
        setInitialized();
    }

    @Override
    protected CapabilityBrokerDirPredicated initCapabilities() {
        return super.initCapabilities()
                .with(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, invInput, sides.getPredicate(SlotType.BasicIO.INPUT))
                .with(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, slotOutput, sides.getPredicate(SlotType.BasicIO.OUTPUT));
    }

    public IAllocableSides<SlotType.BasicIO> getSidedIo() {
        return sides;
    }

    @Override
    protected ITriple<ItemStack, ItemStack, ItemStack> getInput() {
        return ITriple.of(invInput.getStackInSlot(0), invInput.getStackInSlot(1), invInput.getStackInSlot(2));
    }

    @Override
    protected ItemStack getOutputEnvironment() {
        return slotOutput.getStackInSlot();
    }

    @Override
    protected void acceptOutput(ITriple<ItemStack, ItemStack, ItemStack> newInputs, ItemStackOutput output) {
        invInput.setStackInSlot(0, newInputs.getA());
        invInput.setStackInSlot(1, newInputs.getB());
        invInput.setStackInSlot(2, newInputs.getC());
        if (slotOutput.getStackInSlot().isEmpty()) {
            slotOutput.setStackInSlot(output.getOutput().copy());
        } else {
            slotOutput.getStackInSlot().grow(output.getOutput().getCount());
        }
    }

    public IItemHandler getInputSlots() {
        return invInput;
    }

    public IItemHandler getOutputSlot() {
        return slotOutput;
    }

    @Override
    public void collectDrops(Accrue<ItemStack> drops) {
        super.collectDrops(drops);
        InvUtils.accrue(drops, invInput, slotOutput);
    }

}
