# 最快实现的修复方案

## 问题背景
Beacon Provider 每次通过内网向 Beacon Gateway 发送的 payload 都可能达到 100MB~200MB，导致 Gateway 侧 `StreamReadConstraints.getMaxStringLength()`（默认约 20MB）被触发，连接与 action 因为单条字符串过长而频繁断开。

## 目标
快速恢复稳定传输：保证每次送往 Gateway 的字符串/JSON payload 都在约束内，同时尽量在 Provider 端完成分片、压缩与可靠传输，保持现有 Gateway 协议的协作方式。

## 优选方案（MVP 快速实现）
1. **分析现有序列化节点**：定位 Provider 端哪里把整个数据集一次性序列化成字符串（例如打包 NBT/JSON、Base64 等），记录构造流程与上下文数据结构。重点是找到那段可以拆 chunk 且控制粒度的位置。
2. **引入 chunk 分片逻辑**：在 Provider 发送前，把要传输的内容切成固定大小的片段（例如 5~10MB 以下），每片附带唯一的会话 ID、索引、总片数等 metadata；每次通过 `Beacon Provider action` 向 Gateway 发送单个 chunk，Gateway 按顺序接收并在内存中重组。
3. **提供 Ack/重试机制（可选简单实现）**：调整 Provider 等待每片发送的返回（或在失败时重传），防止某片因网络波动丢失；Gateway 一侧可以暂存 chunk，或者直接按插入顺序拼回原始结构。
4. **压缩/裁剪 payload 内容**：在_chunk_逻辑内部过滤掉非必要字段、使用更密的序列化（比如 json 小写，GZIP）来减少每片长度，配合差分/增量传输思想（只传新变更）进一步降低负载。
5. **整合 & 回归验证**：实现 Provider 端 chunk 机制后，确认 Gateway 能顺利 re-assemble 并保持原有功能；若可能用 Playwright/其它工具模拟 100MB 级数据传输，确保连接稳定。
6. **优化与监控建议**：记录 chunk/session 成功与否、日志级别，以便后续在实际运营中调优 `StreamReadConstraints` 或 chunk 尺寸。若 Gateway 允许，可在后续逐步提升 `getMaxStringLength` 作为补充。

## 下一步
- 写文档说明新分片协议，方便 Beacon Gateway 端/后续开发理解变化。
- 等 Plan 经确认后再启动代码改造，并在分片实现完成后做联调与真实 payload 测试。
