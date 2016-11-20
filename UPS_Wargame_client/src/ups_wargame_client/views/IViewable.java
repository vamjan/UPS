/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.views;

/**
 *
 * @author sini
 */
public interface IViewable {
    public void showServerMessage(String data, String msg);
    public void showLobbyMessage(String data, String msg);
    
}
