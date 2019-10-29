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
#    install -d ${D}/lib/firmware/brcm/
#    cp -fr ${S}/firmware/wifi/fw_bcm4356a2_ag.bin ${D}/lib/firmware/brcm/brcmfmac4356-sdio.bin
#    cp -fr ${S}/firmware/wifi/nvram_ap6356.txt ${D}/lib/firmware/brcm/brcmfmac4356-sdio.txt
#    cp -fr ${S}/firmware/wifi/nvram_ap6212a.txt ${D}/lib/firmware/brcm/nvram_ap6212a.txt
#    cp -fr ${S}/firmware/wifi/fw_bcm43438* ${D}/lib/firmware/brcm/
    install -d ${D}/vendor/etc/firmware/
    cp -fr ${S}/firmware/wifi/nvram_ap6212a.txt ${D}/vendor/etc/firmware/
    cp -fr ${S}/firmware/wifi/fw_bcm43438a1.bin ${D}/vendor/etc/firmware/
}

PACKAGES =+ " \
    ${PN}-wifi \
    ${PN}-bt \
"

# FILES_${PN}-wifi = "/lib/firmware/brcm/*"
FILES_${PN}-wifi = "/vendor/etc/firmware/*"

do_deploy () {
    install -d ${DEPLOYDIR}/rkbin/tools
    install -m 755 ${S}/tools/loaderimage ${DEPLOYDIR}/rkbin/tools
    install -m 755 ${S}/rk33/rk3328_ddr_786MHz_v1.13.bin ${DEPLOYDIR}/rkbin
    install -m 755 ${S}/rk33/rk3328_miniloader_v2.46.bin ${DEPLOYDIR}/rkbin
    install -m 755 ${S}/img/rk3328/trust.img ${DEPLOYDIR}/rkbin
}

addtask deploy before do_build after do_compile
