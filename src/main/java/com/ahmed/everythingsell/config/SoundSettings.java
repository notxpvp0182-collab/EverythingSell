package com.ahmed.everythingsell.config;

public final class SoundSettings {

    private final boolean enabled;
    private final String soundName;
    private final float volume;
    private final float pitch;

    public SoundSettings(boolean enabled, String soundName, float volume, float pitch) {
        this.enabled = enabled;
        this.soundName = soundName == null ? "" : soundName.trim();
        this.volume = Math.max(0.0F, volume);
        this.pitch = Math.max(0.1F, Math.min(2.0F, pitch));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSoundName() {
        return soundName;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
