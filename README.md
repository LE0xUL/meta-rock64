# meta-rock64

OpenEmbedded/Yocto BSP layer for the Rock64 boards - <https://wiki.pine64.org/index.php/ROCK64_Main_Page>.

## Quick links

* Git repository web frontend:
  <https://github.com/trecetp/meta-rock64>
* Mailing list (yocto mailing list): <yocto@yoctoproject.org>
* Issues management (Github Issues):
  <https://github.com/trecetp/meta-rock64/issues>

## Description

This is the general hardware specific BSP overlay for the Rock64 device.

The core BSP part of meta-rock64 should work with different
OpenEmbedded/Yocto distributions and layer stacks, such as:

* Distro-less (only with OE-Core).
* Angstrom.
* Yocto/Poky (main focus of testing).

## Dependencies

This layer depends on:

* URI: git://git.yoctoproject.org/poky
  * branch: master
  * revision: HEAD

* URI: git://git.openembedded.org/meta-openembedded
  * layers: meta-oe, meta-multimedia, meta-networking, meta-python
  * branch: master
  * revision: HEAD

## Quick Start

1. source poky/oe-init-build-env rockchip-build
2. Add this layer to bblayers.conf and the dependencies above
3. Set `MACHINE=rock64` in local.conf
4. bitbake core-image-base
5. dd to a SD card the generated sdimg file (use xzcat if rock64-sdimg.xz is used)
6. Boot your rock64.

## Maintainers

* Leonardo Urrego `<leonardo.urrego at admobilize.com>`
