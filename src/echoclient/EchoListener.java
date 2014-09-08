/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package echoclient;

/**
 * @author Lars Mortensen
 */
public interface EchoListener {
  void messageArrived(String data);
}
