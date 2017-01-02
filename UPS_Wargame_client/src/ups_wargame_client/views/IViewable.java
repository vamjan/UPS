/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.views;

import java.util.List;
import ups_wargame_client.control.Lobby;
import ups_wargame_client.data_model.IGameData;

/**
 *
 * @author sini
 */
public interface IViewable {
    public void showServerMessage(String data, String msg);
    public void showLobbyMessage(String data, String msg);
    public void backToStart();
    public void setLobbyList(List list);
    public void showLobby();
    public void updateLobby(Lobby lobby);
    public void hideLobby();
    public void toggleConnected();
    public void refuse();
    public void acknowledge();
    public void startGame(IGameData data);
    public void endGame(String winner);
    public void redraw(IGameData gd);
}
