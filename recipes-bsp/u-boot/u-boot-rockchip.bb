# Rock64 Board u-boot-rockchip

require recipes-bsp/u-boot/u-boot.inc

DESCRIPTION = "u-boot which includes support for the Asus Tinker Board."
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/gpl-2.0.txt;md5=b234ee4d69f5fce4486a80fdaf4a4263"

COMPATIBLE_MACHINE = "(rock64)"

HOMEPAGE = "http://www.denx.de/wiki/U-Boot/WebHome"                                                                                
SECTION = "bootloaders"
DEPENDS += "flex-native arm-trusted-firmware bc-native dtc-native bison-native"
# DEPENDS += "rk-binary-native"
DEPENDS_append = " bc-native dtc-native arm-trusted-firmware"

PE = "1"

SRC_URI = " \
    git://gitlab.denx.de/u-boot/custodians/u-boot-rockchip.git;protocol=https;branch=master \
    "

SRCREV = "u-boot-rockchip-20190920"

S = "${WORKDIR}/git"

PACKAGE_ARCH = "${MACHINE_ARCH}"

# u-boot will build native python module
# inherit pythonnative

# Generate rockchip style u-boot binary
# UBOOT_BINARY = "uboot.img"
IDBLOADER = "idbloader.img"
TPL_BINARY ?= "u-boot-tpl.bin"
SPL_BINARY ?= "u-boot-spl.bin"

# EXTRA_OEMAKE += " u-boot.itb"
# EXTRA_OEMAKE = 'CROSS_COMPILE="${TARGET_PREFIX}" ARCH=arm64'
# EXTRA_OEMAKE += 'HOSTCC="${CC}" HOSTCFLAGS="${CFLAGS} HOSTLDFLAGS="${LDFLAGS}"'
EXTRA_OEMAKE += " BL31=${DEPLOY_DIR_IMAGE}/bl31.elf"
UBOOT_MAKE_TARGET += " u-boot.itb"

do_compile_append () {
    cp ${B}/tpl/${TPL_BINARY} ${B}/${TPL_BINARY}
    cp ${B}/spl/${SPL_BINARY} ${B}/${SPL_BINARY}
    cp ${B}/${TPL_BINARY} ${DEPLOYDIR}/${TPL_BINARY}
    cp ${B}/u-boot.itb ${DEPLOYDIR}/u-boot.itb

    # UBOOT_TEXT_BASE=`grep -w "CONFIG_SYS_TEXT_BASE" ${B}/include/autoconf.mk`
    # loaderimage --pack --uboot ${B}/u-boot.bin ${B}/${UBOOT_BINARY} ${UBOOT_TEXT_BASE#*=} --size "${RK_LOADER_SIZE}" "${RK_LOADER_BACKUP_NUM}"

    #With rkbin
    # tools/mkimage -n rk3399 -T rksd -d ${DEPLOY_DIR_IMAGE}/rkbin/rk3399_ddr_800MHz_v1.14.bin idbloader.bin
    # cat ${DEPLOY_DIR_IMAGE}/rkbin/rk3399_miniloader_v1.15.bin >> idbloader.bin
    # ${DEPLOY_DIR_IMAGE}/rkbin/tools/loaderimage --pack --uboot ./u-boot-dtb.bin uboot.img 0x200000
}

#With rkbin
# do_install_append() {
#     install -d ${D}/boot
#     install -c -m 0644 ${B}/idbloader.bin ${B}/uboot.img ${D}/boot
# }

do_deploy_append () {
    # Create bootloader image
    ${B}/tools/mkimage -n ${SOC_FAMILY} -T rksd -d ${B}/tpl/${TPL_BINARY} ${DEPLOYDIR}/${IDBLOADER}
    cat ${B}/spl/${SPL_BINARY} >> ${DEPLOYDIR}/${IDBLOADER}

    #With rkbin
    # install ${B}/idbloader.bin ${DEPLOYDIR}
    # install ${B}/uboot.img ${DEPLOYDIR}
}
