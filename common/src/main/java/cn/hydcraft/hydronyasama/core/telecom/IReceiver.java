package cn.hydcraft.hydronyasama.core.telecom;

public interface IReceiver<TSender> extends IPassive {
  TSender getSender();

  void setSender(TSender sender);
}
