# Beacon Plugins 更新指南

为了与上述 Beacon Provider 的“分片+限长”传输策略一致，Beacon Gateway/插件端需要做以下准备与校验：

## 1. 兼容分片协议
- 支持 Provider 发送的 chunk metadata（例如 `sessionId`、`chunkIndex`、`chunkCount`、每片压缩标志）。
- 每次收到 chunk 后检查顺序与 repeat，按 `chunkIndex` 排序后在内存或临时文件中拼接；完成整包后再进行原有的 NBT/JSON 解析流程。
- 记录每个 session 的状态日志，便于排查丢包或重发情况。
- `mtr:get_railway_snapshot` 的响应里会用 `payloadChunks` 把 Base64 MessagePack 拆成多个字符串片段，Gateway 需要按 `chunks[index]` 升序串联 `data` 字段，拼出完整的 Base64，再交给 MessagePack/JSON 解码器。

## 2. 校验每片大小与字符串长度
- Gateway 仍然遵守 `StreamReadConstraints.getMaxStringLength()`，要确保所有 chunk 的解码后字符串仍在限制内；若 Provider 压缩，请在 Gateway 解压后再校验。
- 如有必要，可以在接收端增加 `chunkSize` 上限验证并在超限时主动拒绝，让 Provider 调整分片尺寸。

## 3. 简单 Ack/重试协作
- 每收到 chunk 发出简单 ack（可以是整页确认、也可以是单片），让 Provider 继续发送下一片。若 ack 超时，要支持重发同一 `chunkIndex`。
- 与 Provider 约定可重试次数与超时逻辑，避免 Gateway/Provider 双向都无限等待。

## 4. 集中日志与监控点
- 把 chunk 级别日志（收到了第几片、大小、解码耗时）输出到与 Provider 对应的 log source，方便排查 100MB+ 传输过程。
- 若后续 Gateway 可以调高 `StreamReadConstraints`，建议先在配置里以 feature flag 暴露，配合上述 chunk 机制逐步测试。

## 5. 验证与联调建议
1. 在本地用 Playwright/其他自动化方式模拟 Provider 端发送 50MB+ 的数据（可构造 chunk 阶段），确保 Gateway 能稳定 ack/拼接。 
2. 验证 Gateway 端在 chunk 失败、重连、断开后的恢复策略；确认不会因为缺片而把整个 session 标记成失败。
3. 与 Provider 团队同步 chunk 格式与压缩方式版本号（如新增字段必须兼容旧版本）。

## 6. 后续维护
- 保留 chunk 版本号 field，在协议升级时可以向后兼容。
- 如果某次日志反复出现 `StreamReadConstraints` 报错，先检查 chunk 实际大小是否异常，再考虑提升网关限制。
