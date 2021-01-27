package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.resimulators.simukraft.Network;
import com.resimulators.simukraft.Reference;
import com.resimulators.simukraft.common.building.BuildingTemplate;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.enums.Category;
import com.resimulators.simukraft.common.jobs.Profession;
import com.resimulators.simukraft.packets.StartBuildingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

public class GuiBuilder extends GuiBaseJob {
    private Button Build;
    private Button CustomBack;
    private BuildingTemplate selected;
    private boolean loaded = false;
    private Button confirmBuilding;
    private ArrayList<BuildingTemplate> structures;
    private HashMap<Category, ArrayList<StructureButton>> structureButtons = new HashMap<>();
    public GuiBuilder(ITextComponent component, ArrayList<Integer> ids, BlockPos pos, @Nullable int id) {
        super(component, ids, pos, id, Profession.BUILDER.getId());
    }


    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        if (structures != null)loaded = true;



            addButton(Build = new LargeButton(width / 2 - 55, height - 55, 110, 42, new StringTextComponent("Build"), (Build -> {
                super.hideAll();
                CustomBack.visible = true;
                controlStructures(true);
                state = State.SELECTBULDING;
            })));
            //Build.active=false;
            addButton(CustomBack = new Button(width - 120, height - 30, 110, 20, new StringTextComponent("Back"), (Back -> {
                super.Back.onPress();
                if (state == State.SELECTBULDING) {
                    state = State.MAIN;
                    showMainMenu();
                    controlStructures(false);

                }
                if (state == State.BUILDINGINFO) {
                    state = State.SELECTBULDING;
                    controlStructures(true);
                    confirmBuilding.visible = false;
                }



            })));
            addButton(confirmBuilding = new Button(20, height - 30, 110, 20, new StringTextComponent("Confirm"), Confirm -> startBuilding()));
            confirmBuilding.visible = false;


            if (structures != null){
                createStructureButtons();
                loaded = true;

            }
            if (!isHired()) {
                Build.active = false;
            }
        if (!loaded) {
            CustomBack.visible = false;
            Build.visible = false;
        }else {

            if (state != State.MAIN){
                Build.visible = false;

            if (state == State.SELECTBULDING){
               controlStructures(true);

            }
            if (state == State.BUILDINGINFO){
                confirmBuilding.visible = true;
            }
            }

        }
    }

    private void startBuilding() {
        Network.getNetwork().sendToServer(new StartBuildingPacket(pos,Minecraft.getInstance().player.getAdjustedHorizontalFacing(),selected.getName(),Minecraft.getInstance().player.getUniqueID()));
        Minecraft.getInstance().displayGuiScreen(null);
    }

    public void setStructures(ArrayList<BuildingTemplate> structures) {
        this.loaded = true;
        Build.visible = true;
        this.structures = structures;
        createStructureButtons();

    }

    public void createStructureButtons(){
        int xSpacing = 100;
        int xPadding = 20;
        int index = 0;
        for (BuildingTemplate template: structures) {
            StructureButton button = new StructureButton();
            button.createButtons(template,xSpacing *index + xPadding,50);
            index++;
            BuildingType type = BuildingType.getById(template.getTypeID());
            if (type != null){
            structureButtons.computeIfAbsent(type.category, k -> new ArrayList<>());
            ArrayList<StructureButton> list = structureButtons.get(type.category);
            list.add(button);
            structureButtons.put(type.category,list);
            }
        }


    }

    private void controlStructures(boolean visible){

        for (ArrayList<StructureButton> array: structureButtons.values()){
            for (StructureButton button: array){
                button.controlVisibility(visible);
            }
        }

    }

    @Override
    public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground(stack);
        if (loaded) {
            super.render(stack, p_render_1_, p_render_2_, p_render_3_);
            if (state == State.BUILDINGINFO){
                font.drawString(stack, "Building Name: " + selected.getName(), (float) width / 6, (float) height / 4, Color.WHITE.getRGB());
                font.drawString(stack, "Author: " + selected.getAuthor(), (float) width / 6, (float) height / 4+20, Color.WHITE.getRGB());
                font.drawString(stack, "Price: " + selected.getCost(), (float) width / 6, (float) height / 4+40, Color.WHITE.getRGB());
                font.drawString(stack, "Rent: " + selected.getRent(), (float) width / 6 , (float) height / 4+60, Color.WHITE.getRGB());

            }





        }
        else {
            font.drawString(stack, "Loading", (float) width / 2 - font.getStringWidth("Loading") / 2, (float) height / 2, Color.WHITE.getRGB());
        }

    }

    @Override
    public void showMainMenu() {
        super.showMainMenu();
        Build.visible = true;
    }

    static class State extends GuiBaseJob.State {
        private static final int SELECTBULDING = nextID();
        private static final int BUILDINGINFO = nextID();



    }

    private class StructureButton {
        ArrayList<Button> infoButtons = new ArrayList<>();
        Button name;
        Button price;
        Button author;
        Button rent;
        int width = 100;
        int height = 20;

        void createButtons(BuildingTemplate template, int x, int y){

            addButton(name = new Button(x,y,width,height,new StringTextComponent(template.getName()),button ->{
               state = State.BUILDINGINFO;
               CustomBack.visible = true;
               confirmBuilding.visible = true;
               controlStructures(false);
               selected = template;
            }));
            name.visible = false;
            addButton(author = new Button(x,y+height,width,height,new StringTextComponent("Author: " + template.getAuthor()),button->{}));
            author.active = false;
            author.visible = false;
            addButton(price = new Button(x,y+height*2,width,height, new StringTextComponent("Price: " + template.getCost()),button ->{}));
            price.active = false;
            price.visible = false;
            addButton(rent = new Button(x,y + height*3,width,height,new StringTextComponent("Rent: " +template.getRent()),button->{}));
            rent.active = false;
            rent.visible = false;
            infoButtons.add(name);
            infoButtons.add(author);
            infoButtons.add(price);
            infoButtons.add(rent);

        }


        void controlVisibility(boolean visible){
            name.visible = visible;
            author.visible = visible;
            price.visible = visible;
            rent.visible = visible;
        }
    }

    private class LargeButton extends Button {
        final ResourceLocation LARGE_BUTTON = new ResourceLocation(Reference.MODID, "textures/gui/large_button.png");

        public LargeButton(int widthIn, int heightIn, int width, int height, ITextComponent text, IPressable onPress) {
            super(widthIn, heightIn, width, height, text, onPress);
        }

        @Override
        public void renderButton(MatrixStack stack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
            Minecraft minecraft = Minecraft.getInstance();
            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.getTextureManager().bindTexture(LARGE_BUTTON);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
            int i = this.getYImage(this.isHovered);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            this.blit(stack, this.x, this.y, 0, 1 + i * 42, this.width / 2, this.height);
            this.blit(stack, this.x + this.width / 2, this.y, 200 - this.width / 2, 1 + i * 42, this.width / 2, this.height);
            this.renderBg(stack, minecraft, p_renderButton_1_, p_renderButton_2_);
            int j = getFGColor();
            this.drawCenteredString(stack, fontrenderer, this.getMessage().getString(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);

        }


        @Override
        public int getYImage(boolean hovered) {
            int i = 0;
            if (!this.active) {
                i = 1;
            } else if (hovered) {
                i = 2;
            }

            return i;
        }
    }
}
