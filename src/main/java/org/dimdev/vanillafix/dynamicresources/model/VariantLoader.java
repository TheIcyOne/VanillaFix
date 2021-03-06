package org.dimdev.vanillafix.dynamicresources.model;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class VariantLoader implements ICustomModelLoader {
    public static final VariantLoader INSTANCE = new VariantLoader();

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation instanceof ModelResourceLocation;
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        ModelResourceLocation variant = (ModelResourceLocation) modelLocation;
        ModelBlockDefinition definition = loadMultiPartModelBlockDefinition(variant);

        if (definition.hasVariant(variant.getVariant())) {
            return new WeightedRandomModel(definition.getVariant(variant.getVariant()));
        } else {
            if (definition.hasMultipartData()) {
                Block block = ModelLocationInformation.getBlockFromBlockstateLocation(new ResourceLocation(variant.getResourceDomain(), variant.getResourcePath()));
                if (block != null) {
                    definition.getMultipartData().setStateContainer(block.getBlockState());
                }
            }

            return new MultipartModel(new ResourceLocation(variant.getResourceDomain(), variant.getResourcePath()), definition.getMultipartData());
        }
    }

    private ModelBlockDefinition loadMultiPartModelBlockDefinition(ResourceLocation location) {
        ResourceLocation blockstateLocation = new ResourceLocation(location.getResourceDomain(), "blockstates/" + location.getResourcePath() + ".json");

        List<ModelBlockDefinition> list = Lists.newArrayList();
        try {
            for (IResource resource : Minecraft.getMinecraft().getResourceManager().getAllResources(blockstateLocation)) {
                list.add(loadModelBlockDefinition(location, resource));
            }
        } catch (IOException e) {
            throw new RuntimeException("Encountered an exception when loading model definition of model " + blockstateLocation, e);
        }

        return new ModelBlockDefinition(list);
    }

    private ModelBlockDefinition loadModelBlockDefinition(ResourceLocation location, IResource resource) {
        ModelBlockDefinition definition;

        try (InputStream is = resource.getInputStream()) {
            definition = ModelBlockDefinition.parseFromReader(new InputStreamReader(is, StandardCharsets.UTF_8), location);
        } catch (Exception exception) {
            throw new RuntimeException("Encountered an exception when loading model definition of '" + location + "' from: '" + resource.getResourceLocation() + "' in resourcepack: '" + resource.getResourcePackName() + "'", exception);
        }

        return definition;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}

    @Override
    public String toString() {
        return "VariantLoader";
    }
}
