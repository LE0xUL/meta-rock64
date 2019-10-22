SUMMARY = "ARM Trusted Firmware"
DESCRIPTION = "ARM Trusted Firmware provides a reference implementation of \
Secure World software for ARMv8-A, including Exception Level 3 (EL3) software. \
It provides implementations of various ARM interface standards such as the \
Power State Coordination Interface (PSCI), Trusted Board Boot Requirements \
(TBBR) and Secure monitor code."
HOMEPAGE = "http://infocenter.arm.com/help/topic/com.arm.doc.dui0928e/CJHIDGJF.html"

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://license.rst;md5=90153916317c204fade8b8df15739cde"

inherit deploy

DEPENDS += "dtc-native openssl-native"

PACKAGE_ARCH = "${MACHINE_ARCH}"

S = "${WORKDIR}/git"

PV_append = "+git${SRCPV}"

BRANCH = "master"
SRC_URI = "git://github.com/ARM-software/arm-trusted-firmware.git;protocol=https;branch=${BRANCH}"

SRCREV ?= "3441952f61a62948ccf84c2e3eada9b340c3560d"

COMPATIBLE_MACHINE = "(rock64)"
ATFPLATFORM = "rk3328"

CFLAGS[unexport] = "1"
LDFLAGS[unexport] = "1"
AS[unexport] = "1"
LD[unexport] = "1"

do_configure[noexec] = "1"

EXTRA_OEMAKE = 'CROSS_COMPILE="${TARGET_PREFIX}" PLAT="${ATFPLATFORM}"'

do_compile() {
	oe_runmake all
}

do_install() {
	install -d ${D}/boot
	install -m 0644 ${S}/build/${ATFPLATFORM}/release/bl31/bl31.elf ${D}/boot/
}

do_deploy() {
	install -d ${DEPLOYDIR}
	install -m 0644 ${S}/build/${ATFPLATFORM}/release/bl31/bl31.elf ${DEPLOYDIR}/
}
addtask deploy before do_build after do_compile

FILES_${PN} = "/boot"
SYSROOT_DIRS += "/boot"
