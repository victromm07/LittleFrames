package team.creative.littleframes.client.display;

import me.srrapero720.watermedia.api.WaterMediaAPI;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.littleframes.client.texture.TextureCache;

public class FramePictureDisplay extends FrameDisplay {
    
    public final TextureCache texture;
    private int textureId;
    
    public FramePictureDisplay(TextureCache texture) {
        this.texture = texture;
    }
    
    @Override
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        long time = WaterMediaAPI.gameTicksToMs(tick) + (playing ? (long) (CreativeCoreClient.getFrameTime() * 50) : 0);
        if (texture.getDuration() > 0 && time > texture.getDuration())
            if (loop)
                time %= texture.getDuration();
        textureId = texture.getTexture(time);
    }
    
    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public int texture() {
        return textureId;
    }
    
    @Override
    public void release() {
        texture.unuse();
    }
    
    @Override
    public int getWidth() {
        return texture.getWidth();
    }
    
    @Override
    public int getHeight() {
        return texture.getHeight();
    }
    
}
