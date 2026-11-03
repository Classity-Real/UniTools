# UniTools - Files Management & USB Auto-Detection Update

This plan introduces real-time USB auto-detection, a dedicated 4th **Files Tab** with hierarchical FDL directory browsing and internal tool files management, removes top-bar icons and setup banners for a cleaner look, and compiles the final APK into the project root directory.

## User Review & Critical Decisions

> [!IMPORTANT]
> The top app bar icon and setup banners are removed for a minimalist title-only header. The new Files Tab provides full file management (import, add, rename, export/share, and permissions check) alongside an interactive breadcrumb tree for FDL folders (`FDLs > UMS9230/SC9863A/UMS512 > Infinix/Itel/Realme/Tecno > FDL binaries`).

- **Confirmed Decision (FDL Navigation)**: Interactive breadcrumb tree with expandable folders allowing seamless drill-down through chipsets, OEM brands, and FDL loader files.
- **Confirmed Decision (File Operations)**: Full-featured file manager with Import, Add Files, Rename, Export/Share, Delete, and Linux Permissions (`rwxr-xr-x`) viewer/checker.
- **Confirmed Decision (USB Auto-Detection)**: Live status pill powered by Android's `UsbManager` that monitors connected OTG/USB devices, identifies Unisoc BROM / VCOM interfaces (VID `0x1782` and standard serial endpoints), and auto-configures the SoC profile.
- **Confirmed Decision (APK Export)**: Automated Gradle build packaging the final release/debug APK directly to the root project workspace folder.

---

## 1. Overview & Core Concept

- **What It Does**:
  1. **USB Auto-Detect**: Continually tracks USB OTG / device attachments. When a Unisoc device is connected in BROM/VCOM mode, a live status pill illuminates with vendor/product IDs and auto-selects the appropriate SoC configuration.
  2. **Files Tab**: Added as the 4th destination on the bottom navigation bar (`Flashing`, `Backup`, `Tools`, `Files`). Features "Import Files" and "Add Files" circular actions, followed by "Which directory?" selection:
     - **FDLs**: Navigates through the hierarchical structure (`UMS9230`, `SC9863A`, `UMS512` -> `Infinix`, `Itel`, `Realme`, `Tecno` -> FDL binary files).
     - **Internal Tool Files**: Directly explores internal storage (`filesDir/binaries`), displaying binaries (`spd_dump`, `gen_spl-unlock`, `chsize`, `pacextractor`), configs, and logs.
  3. **Visual & Header Cleanup**:
     - Removed the hamburger icon and actions from the title bar; the top app bar is now clean and minimalistic.
     - Removed the "Binaries setup completed successfully" banner message.
  4. **Root APK Compilation**: Runs `gradle assembleDebug` and copies the generated APK to the root workspace folder for easy download and export.

---

## 2. User Experience & Visual Design

### Files Tab Structure & Flow

```
┌─────────────────────────────────────────────────────────────┐
│                          UniTools                           │
├─────────────────────────────────────────────────────────────┤
│  [● USB Auto-Detect: Spreadtrum BROM (VID: 0x1782)]         │
├─────────────────────────────────────────────────────────────┤
│  Quick Actions:   [ + Import Files ]     [ + Add Files ]    │
├─────────────────────────────────────────────────────────────┤
│  Which directory?                                           │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ 📁 FDLs                                               │  │
│  │   Drill down into UMS9230, SC9863A, UMS512 > Brands   │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ 📄 Internal Tool Files                                │  │
│  │   Browse binaries, configs, inputs, and backup files   │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

- **Interactive Breadcrumb Navigation**:
  - Tapping **FDLs** displays breadcrumb trail `FDLs / [SoC] / [Brand]` with back navigation.
  - Tapping an FDL file brings up a file details dialog with size, SHA-256 hash, Linux permissions (`755` executable), rename, export, or replace options.
- **Internal Tool Files Browser**:
  - Live file list of `filesDir/binaries`, `filesDir/backup`, and `filesDir/input`.
  - Supports adding files directly via system SAF picker and viewing file permissions.

---

## 3. Technical Implementation Plan

1. **USB Auto-Detection Service / State (`UsbDetector.kt`)**:
   - Register a `BroadcastReceiver` for `ACTION_USB_DEVICE_ATTACHED` and `ACTION_USB_DEVICE_DETACHED`.
   - Poll `UsbManager.deviceList` on lifecycle resume.
   - Match Unisoc Vendor ID (`0x1782` Spreadtrum) or generic USB serial/CDC endpoints, displaying a live green indicator pill.
2. **ViewModel Updates (`UniToolsViewModel.kt`)**:
   - Add `usbConnectionState` StateFlow (`connected: Boolean`, `deviceName: String`, `vendorId: Int`, `productId: Int`).
   - Remove `statusBanner` ("Binaries setup completed successfully").
   - Add state for Files navigation: current directory path, active breadcrumbs, file list, file details dialog, and rename dialog.
   - Implement `importFile(uri: Uri, targetDir: File)` and `addFiles(uris: List<Uri>, targetDir: File)`.
3. **UI Updates**:
   - Update `MainActivity.kt`: Remove navigation icon from `TopAppBar`, add 4th tab `Files` (`Icons.Default.Folder`), wire Files screen.
   - Create `FilesScreen.kt`:
     - Quick action buttons: "Import Files" and "Add Files".
     - "Which directory?" choice cards: "FDLs" and "Internal Tool Files".
     - Interactive breadcrumb tree for FDLs (`FDLs` -> `SocType` -> `DeviceBrand` -> `files`).
     - File items list with rename, export (share intent), delete, and file permissions badge (`chmod 755 / 644`).
4. **Build & APK Packaging**:
   - Execute Gradle build task to generate the debug APK.
   - Copy `app/build/outputs/apk/debug/app-debug.apk` directly to the workspace root as `UniTools.apk`.

---

## 4. Verification Plan

1. **Build Verification**: Run `compile_applet` to ensure syntax, imports, and Compose layouts compile cleanly.
2. **Tab & Navigation Verification**: Verify that clicking the new "Files" tab displays the "Which directory?" cards and navigates seamlessly through the FDL tree.
3. **USB Manager Verification**: Verify that `UsbDetector` correctly checks `UsbManager.deviceList` without crashes or missing permissions.
4. **Root APK Check**: Verify that `UniTools.apk` is generated and located in the workspace root directory.
