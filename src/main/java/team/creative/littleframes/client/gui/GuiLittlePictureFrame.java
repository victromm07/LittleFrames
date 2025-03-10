package team.creative.littleframes.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiColumn;
import team.creative.creativecore.common.gui.controls.parent.GuiRow;
import team.creative.creativecore.common.gui.controls.parent.GuiTable;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.common.structure.LittlePictureFrame;
import team.creative.littleframes.common.structure.LittlePictureFrame.FitMode;

public class GuiLittlePictureFrame extends GuiLayer {
    
    public LittlePictureFrame frame;
    
    public GuiTextfield url;
    
    public final GuiSyncLocal<EndTag> PLAY = getSyncHolder().register("play", x -> frame.play());
    public final GuiSyncLocal<EndTag> PAUSE = getSyncHolder().register("pause", x -> frame.pause());
    public final GuiSyncLocal<EndTag> STOP = getSyncHolder().register("stop", x -> frame.stop());
    
    public final GuiSyncLocal<CompoundTag> SET_DATA = getSyncHolder().register("set_data", nbt -> {
        String url = nbt.getString("url");
        if (LittleFrames.CONFIG.canUse(getPlayer(), url)) {
            frame.setURL(url);
            frame.renderDistance = Math.min(LittleFrames.CONFIG.maxRenderDistance, nbt.getInt("render"));
            frame.fitMode = LittlePictureFrame.FitMode.values()[nbt.getInt("fit")];
            frame.loop = nbt.getBoolean("loop");
            frame.volume = nbt.getFloat("volume");
            frame.minDistance = nbt.getFloat("min");
            frame.maxDistance = nbt.getFloat("max");
            frame.alpha = nbt.getFloat("transparency");
            frame.brightness = nbt.getFloat("brightness");
        }
        
        frame.updateStructure();
    });
    
    public GuiLittlePictureFrame(LittlePictureFrame frame) {
        super("little_frame");
        this.frame = frame;
    }
    
    @Override
    public void create() {
        GuiButton save = new GuiButton("save", x -> {
            CompoundTag nbt = new CompoundTag();
            GuiTextfield url = get("url");
            GuiSteppedSlider renderDistance = get("distance");
            
            GuiStateButtonMapped<FitMode> fit = get("fit");
            
            GuiSlider transparency = get("transparency");
            GuiSlider brightness = get("brightness");
            
            GuiCheckBox loop = get("loop");
            GuiSlider volume = get("volume");
            GuiSteppedSlider min = get("range_min");
            GuiSteppedSlider max = get("range_max");
            
            nbt.putInt("fit", fit.getState());
            
            nbt.putInt("render", (int) renderDistance.value);
            
            nbt.putFloat("transparency", (float) transparency.value);
            nbt.putFloat("brightness", (float) brightness.value);
            
            nbt.putBoolean("loop", loop.value);
            nbt.putFloat("volume", (float) volume.value);
            nbt.putFloat("min", min.getValue());
            nbt.putFloat("max", max.getValue());
            
            nbt.putString("url", url.getText());
            
            nbt.putInt("type", 0);
            SET_DATA.send(nbt);
        });
        
        save.setTranslate("gui.save");
        
        align = Align.STRETCH;
        flow = GuiFlow.STACK_Y;
        
        url = new GuiUrlTextfield(save, "url", frame.getRealURL());
        url.setMaxStringLength(512);
        add(url);
        GuiLabel error = new GuiLabel("error").setDefaultColor(ColorUtils.RED);
        if (frame.cache != null && frame.cache.getError() != null)
            error.setTranslate(frame.cache.getError());
        add(error);
        GuiStateButtonMapped<FitMode> button = new GuiStateButtonMapped<FitMode>("fit", new TextMapBuilder<FitMode>()
                .addComponent(FitMode.values(), x -> Component.translatable("gui.little_frame.fitmode." + x.name())));
        button.select(frame.fitMode);
        add(button);
        
        GuiTable table = new GuiTable();
        add(table);
        GuiColumn left;
        GuiColumn right;
        
        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("t_label").setTitle(Component.translatable("gui.creative_frame.transparency").append(":")));
        right.add(new GuiSlider("transparency", frame.alpha, 0, 1).setExpandableX());
        
        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("b_label").setTitle(Component.translatable("gui.creative_frame.brightness").append(":")));
        right.add(new GuiSlider("brightness", frame.brightness, 0, 1).setExpandableX());
        
        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("d_label").setTitle(Component.translatable("gui.creative_frame.distance").append(":")));
        right.add(new GuiSteppedSlider("distance", frame.renderDistance, 5, 1024).setExpandableX());
        
        GuiParent play = new GuiParent(GuiFlow.STACK_X);
        add(play);
        
        play.add(new GuiIconButton("play", GuiIcon.PLAY, x -> PLAY.send(EndTag.INSTANCE)));
        play.add(new GuiIconButton("pause", GuiIcon.PAUSE, x -> PAUSE.send(EndTag.INSTANCE)));
        play.add(new GuiIconButton("stop", GuiIcon.STOP, x -> STOP.send(EndTag.INSTANCE)));
        
        play.add(new GuiCheckBox("loop", frame.loop).setTranslate("gui.creative_frame.loop"));
        play.add(new GuiLabel("v_label").setTranslate("gui.creative_frame.volume"));
        play.add(new GuiSlider("volume", frame.volume, 0, 1));
        play.add(new GuiLabel("range_label").setTranslate("gui.creative_frame.range"));
        play.add(new GuiSteppedSlider("range_min", (int) frame.minDistance, 0, 512));
        play.add(new GuiSteppedSlider("range_max", (int) frame.maxDistance, 0, 512));
        
        GuiParent bottom = new GuiParent(GuiFlow.STACK_X);
        bottom.align = Align.RIGHT;
        add(bottom);
        save.setEnabled(LittleFrames.CONFIG.canUse(getPlayer(), url.getText()));
        bottom.add(save);
        bottom.add(new GuiButton("reload", x -> {
            if (Screen.hasShiftDown())
                TextureCache.reloadAll();
            else if (frame.cache != null)
                frame.cache.reload();
        }).setTranslate("gui.creative_frame.reload").setTooltip(new TextBuilder().translate("gui.creative_frame.reloadtooltip").build()));
    }
    
}
