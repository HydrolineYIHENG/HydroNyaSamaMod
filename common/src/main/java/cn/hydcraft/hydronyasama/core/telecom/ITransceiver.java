package cn.hydcraft.hydronyasama.core.telecom;

public interface ITransceiver<TTransceiver> extends IInitiative {
  TTransceiver getTransceiver();

  void setTransceiver(TTransceiver transceiver);

  boolean transceiverIsPowered();
}
