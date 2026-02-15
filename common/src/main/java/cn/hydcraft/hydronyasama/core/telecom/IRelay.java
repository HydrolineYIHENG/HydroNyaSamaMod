package cn.hydcraft.hydronyasama.core.telecom;

public interface IRelay<TTarget, TSender> extends IReceiver<TSender> {
  TTarget getTarget();

  void setTarget(TTarget target);
}
