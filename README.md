# ShipThis Go <a href="https://discord.gg/gPjn3S99k4"><img alt="discord" src="https://img.shields.io/discord/1304144717239554069?style=flat-square&label=%F0%9F%92%AC%20discord&color=00ACD7"></a><a href="https://shipth.is/?ref=github_readme"><img src="st.png" align="right" height="80" alt="ShipThis Go" /></a>

**ShipThis Go** is an Android companion app for the [ShipThis CLI](https://github.com/shipth-is/cli). It runs **Godot** games on real Android devices and streams runtime logs back to your terminal.

Builds are produced on the ShipThis cloud build servers, so no local Android SDK or Android build tools are required.

The app supports multiple Godot versions by downloading the required engine version on demand using **Play Feature Delivery**, rather than bundling all engines into a single install.

## Features

* Supports **Godot 3.x through 4.5** using namespaced engine versions
* Downloads the **required Godot engine version only when needed** via Play Feature Delivery
* Streams **verbose runtime logs** to the CLI in real time
* Builds are performed on **ShipThis cloud servers**
* Designed for **debugging and quick iteration** during development

## Demo

[shipthis-go-demo.webm](https://github.com/user-attachments/assets/7d6a6267-7069-4f1f-96c0-116ee7d6b44b)
