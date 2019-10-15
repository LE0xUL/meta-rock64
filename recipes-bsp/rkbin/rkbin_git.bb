inherit deploy

DESCRIPTION = "Rockchip binary tools (including WiFi/BT firmware)"

LICENSE = "BINARY"
LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=564e729dd65db6f65f911ce0cd340cf9"
NO_GENERIC_LICENSE[BINARY] = "LICENSE.TXT"

SRC_URI = "git://github.com/armbian/rkbin"
SRCREV = "51d55a46fa5a0736d12fae6e0164a6e79249abf8"

S = "${WORKDIR}/git"

do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install () {
    install -d ${D}/lib/firmware/brcm/
    cp -fr ${S}/firmware/wifi/fw_bcm4356a2_ag.bin ${D}/lib/firmware/brcm/brcmfmac4356-sdio.bin
    cp -fr ${S}/firmware/wifi/nvram_ap6356.txt ${D}/lib/firmware/brcm/brcmfmac4356-sdio.txt
}

PACKAGES =+ " \
    ${PN}-wifi \
    ${PN}-bt \
"

FILES_${PN}-wifi = "/lib/firmware/brcm/*"

do_deploy () {
    install -d ${DEPLOYDIR}/rkbin/tools
    install -m 755 ${S}/tools/loaderimage ${DEPLOYDIR}/rkbin/tools
    install -m 755 ${S}/rk33/rk3399_ddr_800MHz_v1.14.bin ${DEPLOYDIR}/rkbin
    install -m 755 ${S}/rk33/rk3399_miniloader_v1.15.bin ${DEPLOYDIR}/rkbin
    install -m 755 ${S}/img/rk3399/trust.img ${DEPLOYDIR}/rkbin
}

addtask deploy before do_build after do_compile
