/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.control;

import ups_wargame_client.data_model.IGameData;
import ups_wargame_client.views.IViewable;

/**
 *
 * @author sini
 */
public interface IController {
    public void setupView(IViewable view);
    public void sendCommand(Command command);
    public void addToInputQueue(Command command);
    public void addToOutputQueue(Command command);
    public int getClientID();
    public IGameData getGameData();
}
