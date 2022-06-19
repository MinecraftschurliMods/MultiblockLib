package com.github.minecraftschurlimods.multiblocklib.fabric.xplat;

import com.github.minecraftschurlimods.multiblocklib.init.RegistrationProvider;
import com.github.minecraftschurlimods.multiblocklib.init.RegistryObject;
import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
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

    static class Provider<T> implements RegistrationProvider<T> {
        private final String modId;

        private final Set<RegistryObject<T>> entries = new HashSet<>();
        private final Set<RegistryObject<T>> entriesView = Collections.unmodifiableSet(entries);
        private final ResourceKey<? extends Registry<T>> registryKey;

        private Provider(String modId, ResourceKey<? extends Registry<T>> key) {
            this.modId = modId;
            this.registryKey = key;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
            final var rl = new ResourceLocation(modId, name);
            final var ro = new FabricRegistryObject<T, I>(registryKey, rl, supplier);
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

        void register() {
            entries.forEach(ro -> ((FabricRegistryObject<T, ?>) ro).register());
        }
    }

    private static class FabricRegistryObject<T, I extends T> implements RegistryObject<I> {
        private final ResourceKey<T> key;
        private final Supplier<Registry<T>> registry;
        private final Supplier<? extends I> sup;
        private final ResourceLocation rl;
        private I obj;

        public FabricRegistryObject(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation rl, Supplier<? extends I> sup) {
            this.rl = rl;
            this.sup = sup;
            this.key = ResourceKey.create(registryKey, rl);
            this.registry = Suppliers.memoize(() -> {
                Registry<T> registry = (Registry<T>) Registry.REGISTRY.get(registryKey.location());
                if (registry == null)
                    registry = (Registry<T>) BuiltinRegistries.REGISTRY.get(registryKey.location());
                return registry;
            });
        }

        @Override
        public ResourceKey<I> getResourceKey() {
            return (ResourceKey<I>) key;
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
            return (Holder<I>) registry.get().getOrCreateHolder(key).result().get();
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        private void register() {
            if (obj != null) return;
            obj = Registry.register(registry.get(), rl, sup.get());
        }
    }
}
