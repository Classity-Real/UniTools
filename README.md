# UniTools [OFFICIAL SOURCE]
The open-source tool made for rooted phones with a GUI.

> [!NOTE]
> This Tool is owned by Classity-Real and taking the tool without any credit isn't allowed. It uses MIT License and that allows you to modify, distribute, and
> make it a closed source but you have to keep the main credit for the tool. Also this project is VibeCoded, I don't give a hell if you roast and hate for this.

## How it works

The tool runs directly on an Android host device with root access (KernelSU, Magisk, or APatch) and communicates with Unisoc (Spreadtrum) devices over USB OTG.

1. **Root execution**: Executes bundled standalone Linux utilities (`spd_dump`, dynamic linkers, and `libusb-1.0`) with root privileges (`su`) to talk directly to USB devices.
2. **OTG connection**: Turn off the target Unisoc device, hold the boot key (usually Volume Down or Volume Up), and plug it into your phone using a USB OTG cable. UniTools detects the Spreadtrum Boot ROM (BROM) connection over USB.
3. **FDL loading**: UniTools sends FDL1 to device SRAM based on the selected chipset (UMS9230/T606/T616, SC9863A, UMS512/T610/T618) and brand profile (Infinix, Itel, Realme, Tecno). Once initialized, it uploads FDL2 to DDR memory to handle raw flash operations.
4. **Command execution**: Flashing, partition dumping, repartitioning, and maintenance commands run through the native binary, streaming full terminal logs to the app's console drawer in real time.

## Features

### Flashing
- Type any partition name manually or pick from standard suggestions (boot, init_boot, recovery, vbmeta, super, misc, dtbo, userdata).
- Add multiple partition images (.img, .bin) to a flash queue.
- Rename the target partition of any queued file before flashing.
- Choose what the target device does after flashing finishes: Normal reboot, Recovery, FastbootD, or Power off.

### Bootloader unlock
- Automated SPL unlock process:
  1. Backs up `splloader` and `uboot`.
  2. Generates the spl-unlock token.
  3. Flashes the modified uboot.
  4. Runs the unlock payload in BROM mode.
  5. Verifies unlock status in `miscdata`.
  6. Restores the original `splloader`.

### Backup and restore
- Dump raw partitions directly to your host device's internal storage.
- Quick backup presets:
  - IMEI / NVRAM (`miscdata`, `nvitem`, `persist`, `prodnv`, `sec_storage`, `sysdumpdb`)
  - All Lite (core boot and system images)
  - All Full (complete partition dump)
- Custom partition selection checklist.
- Flash previously dumped partition backups back to the device.

### Repartition by XML
- Repartition storage using an XML partition table file (`repartition <xml_file>`).
- Confirmation prompt before repartitioning to prevent accidental data loss.
- Dump current partition table directly from the connected device as an XML file (`partition_list <output.xml>`).

### Maintenance tools
- Wipe userdata: Format and wipe device data from BROM.
- Disable verity: Disable AVB / DM-Verity flags.
- Reset FRP: Erase persistent lock flags.
- Slot switcher: Switch active boot slot between Slot A and Slot B on A/B devices.
- Hardware reboots: Force reboot the connected device to Normal, Recovery, FastbootD, or Power off.

### Target configuration and chipset modes
- Supported chipsets: UMS9230 (T606 / T616), SC9863A, UMS512 (T610 / T618).
- OEM profiles: Infinix, Itel, Realme, Tecno.
- Auto-detect chipset folders: Automatically reads custom FDL folders added to device storage.
- 3 Hex modes: Standard, Mode 1 (`0x65015f08`), and Mode 2.

### File manager
- Built-in file browser for managing dumped images and FDL files.
- Import files using the system file picker, create folders, rename files, and toggle executable (`chmod +x`) permissions.

### UI
- Material You theme using your system wallpaper colors.
- Transparent status and navigation bars with edge-to-edge layout.
- Animated navigation bar pill indicator.
- Adaptive layout for both phones and tablets.

## Requirements

- Host phone running Android 7.0 or newer.
- Root access (KernelSU, Magisk, or APatch).
- USB OTG cable or USB-C to USB-C cable.
- Target device powered by a supported Unisoc chipset.

## Notes

Always back up your IMEI and NVRAM partitions before flashing or repartitioning. Incorrect partition tables or broken images can brick your device.

### Acknowledgements
- Classity-Real - Helping + Coding + Designer Plan
- Google Ai Studio - Coding + Designer
