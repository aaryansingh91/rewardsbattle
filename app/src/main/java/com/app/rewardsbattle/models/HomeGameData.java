package com.app.rewardsbattle.models;

public class HomeGameData {

    String gameId;
    String gameName;
    String gameImage;
    String gameStatus;

    String gameType;

    public HomeGameData(String gameId, String gameName, String gameImage, String gameStatus, String gameType) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.gameImage = gameImage;
        this.gameStatus = gameStatus;
        this.gameType = gameType;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameImage() {
        return gameImage;
    }

    public void setGameImage(String gameImage) {
        this.gameImage = gameImage;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

}
