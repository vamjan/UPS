/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.views;

import java.util.List;

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
    public void hideLobby();
    public void refuse();
    public void acknowledge();
}
