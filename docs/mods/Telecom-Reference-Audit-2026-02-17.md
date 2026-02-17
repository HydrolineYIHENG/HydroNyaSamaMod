# Telecom Reference Audit - 2026-02-17

## Scope
- Reference: `.reference/NyaSamaTelecom/src/main/java/club/nsdn/nyasamatelecom`
- Current: `common` + `fabric-*` + `forge-*`

## Class Name Match Summary
- Reference classes: 84
- Name-matched classes in current tree: 1
- Name-missing classes: 83

## Name-Matched
- TelecomProcessor

## Name-Missing (Needs feature-level check/migration)
- AbsFastTESR
- AbsTileEntitySpecialRenderer
- AdvancedBoxRenderer
- BlockDelayer
- BlockLoader
- BlockLogo
- BlockNSASMBox
- BlockNSDNLogo
- BlockNSPGA
- BlockNSPGAFlex
- BlockRedInput
- BlockRedOutput
- BlockRSLatch
- BlockSign
- BlockSignalBox
- BlockSignalBoxGetter
- BlockSignalBoxSender
- BlockTimer
- BlockTriStateSignalBox
- BlockWirelessRx
- BlockWirelessTx
- ChunkLoaderHandler
- ClientProxy
- ClientTickHandler
- CommonProxy
- Connector
- CreativeTabLoader
- CryptManager
- DevEditor
- DeviceBase
- EventRegister
- GuiNGTablet
- ITelecom
- ItemConnector
- ItemDevEditor
- ItemLoader
- ItemNGTablet
- ITileAnchor
- ITriStateReceiver
- NetworkRegister
- NetworkWrapper
- NGTablet
- NGTCommand
- NGTEditor
- NGTPacket
- NGTPacketHandler
- NSASM
- NSPGAEditorHandler
- NSPGAFlexRenderer
- NSPGAPacket
- NSPGAPacketHandler
- NSPGARenderer
- NyaGameMR
- NyaSamaTelecom
- PackagePrivate
- ParticlePacket
- ParticlePacketHandler
- RendererHelper
- RenderNyaGameMR
- ServerProxy
- ServerTickHandler
- SignalBox
- SignalBoxGetter
- SignalBoxRenderer
- SignalBoxSender
- TelecomHandler
- TelecomImpl
- TileEntityActuator
- TileEntityBase
- TileEntityLoader
- TileEntityModelBinder
- TileEntityMultiSender
- TileEntityPassiveReceiver
- TileEntityReceiver
- TileEntitySingleSender
- TileEntityTransceiver
- TileEntityTriStateReceiver
- TileEntityTriStateTransmitter
- ToolBase
- ToolHandler
- TriStateSignalBox
- TriStateSignalBoxRenderer
- Util

## Notes
- This is class-name level diff only; architecture changed to cross-loader runtime, so some features may be migrated under different class names.
- Next step is capability-level mapping per missing class.
