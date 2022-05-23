package com.github.minecraftschurlimods.multiblocklib.fabric.xplat;

import com.github.minecraftschurlimods.multiblocklib.init.RegistrationProvider;
import com.github.minecraftschurlimods.multiblocklib.init.RegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class FabricRegistryProviderFactory implements RegistrationProvider.Factory {
    @Override
    public <T> RegistrationProvider<T> create(final ResourceKey<? extends Registry<T>> registryKey, final String modId) {
        return new Provider<>(modId, registryKey);
    }

    private static class Provider<T> implements RegistrationProvider<T> {
        private final String modId;
        private final Registry<T> registry;

        private final Set<RegistryObject<T>> entries = new HashSet<>();
        private final Set<RegistryObject<T>> entriesView = Collections.unmodifiableSet(entries);

        private Provider(String modId, ResourceKey<? extends Registry<T>> key) {
            this.modId = modId;
            this.registry = RegistryAccess.BUILTIN.get().registryOrThrow(key);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
            final var rl = new ResourceLocation(modId, name);
            final var obj = Registry.register(registry, rl, supplier.get());
            final var ro = new FabricRegistryObject<>(registry, rl, obj);
            entries.add((RegistryObject<T>) ro);
            return ro;
        }

        @Override
        public Collection<RegistryObject<T>> getEntries() {
            return entriesView;
        }

        @Override
        public String getModId() {
            return modId;
        }
    }

    private static class FabricRegistryObject<T, I extends T> implements RegistryObject<I> {
        final ResourceKey<I> key;
        private final ResourceLocation rl;
        private final I obj;
        private final Registry<I> registry;

        @SuppressWarnings("unchecked")
        public FabricRegistryObject(final Registry<T> registry, final ResourceLocation rl, final I obj) {
            this.rl = rl;
            this.obj = obj;
            this.key = (ResourceKey<I>) ResourceKey.create(registry.key(), rl);
            this.registry = (Registry<I>) registry;
        }

        @Override
        public ResourceKey<I> getResourceKey() {
            return key;
        }

        @Override
        public ResourceLocation getId() {
            return rl;
        }

        @Override
        public I get() {
            return obj;
        }

        @Override
        public Holder<I> asHolder() {
            return registry.getOrCreateHolder(this.key);
        }

        @Override
        public boolean isPresent() {
            return true;
        }
    }
}
