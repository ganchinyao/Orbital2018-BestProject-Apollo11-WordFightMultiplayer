package com.ganwl.multiplayerwordgame.packs;

/**
 * A Pack is a cardview containing the name, and an image id
 */
public class Pack {
    private String packName;
    private int packImage;
    private boolean isLocked;
    private int levelDifficulty;

    public Pack(String packName, int packImage, boolean isLocked, int levelDifficulty) {
        this.packName = packName;
        this.packImage = packImage;
        this.isLocked = isLocked;
        this.levelDifficulty = levelDifficulty;
    }

    public String getPackName() {
        return packName;
    }

    public int getPackImage() {
        return packImage;
    }

    public boolean getIsLocked() {
        return this.isLocked;
    }

    public int getLevelDifficulty() {
        return this.levelDifficulty;
    }

    public void setUnlock() {
        this.isLocked = false;
    }
}
