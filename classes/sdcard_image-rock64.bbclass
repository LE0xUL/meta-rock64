inherit image_types

#
# Create a ROCK64 image that can be written onto a SD card using dd.
# Based on https://github.com/agherzan/meta-raspberrypi/blob/master/classes/sdcard_image-rpi.bbclass
#
# ROCK64 Boot flow:
#+--------+----------------+----------+-------------+---------+
#| Boot   | Terminology #1 | Actual   | Rockchip    | Image   |
#| stage  |                | program  |  Image      | Location|
#| number |                | name     |   Name      | (sector)|
#+--------+----------------+----------+-------------+---------+
#| 1      |  Primary       | ROM code | BootRom     |         |
#|        |  Program       |          |             |         |
#|        |  Loader        |          |             |         |
#|        |                |          |             |         |
#| 2      |  Secondary     | U-Boot   |idbloader.img| 0x40    | pre-loader
#|        |  Program       | TPL/SPL  |             |         |
#|        |  Loader (SPL)  |          |             |         |
#|        |                |          |             |         |
#| 3      |  -             | U-Boot   | u-boot.itb  | 0x4000  | including u-boot and atf
#|        |                |          | uboot.img   |         | only used with miniloader
#|        |                |          |             |         |
#|        |                | ATF/TEE  | trust.img   | 0x6000  | only used with miniloader
#|        |                |          |             |         |
#| 4      |  -             | kernel   | boot.img    | 0x8000  |
#|        |                |          |             |         |
#| 5      |  -             | rootfs   | rootfs.img  | 0x40000 |
#+--------+----------------+----------+-------------+---------+

# The disk layout used is:
#
#    0                      -> IDBLOADER                      - empty
#    IDBLOADER              -> UBOOT                          - pre-loader
#    UBOOT                  -> TRUST                          - UBoot
#    TRUST                  -> BOOT                           - ARM trusted firmware
#    BOOT                   -> ROOTFS                         - Kernel + Device Tree Blob
#    ROOTFS                 -> SDIMG_SIZE                     - RootFS

#                                                     Default Free space = 1.3x
#                                                     Use IMAGE_OVERHEAD_FACTOR to add more space
#
#  32KiB     ~8MiB      4MiB    4MiB     ~112MiB   SDIMG_ROOTFS
# <------><----------> <-----> <-----> <--------> <------------>
# -------- ----------- ------- ------- ---------- -------------
# | EMPTY | IDBLOADER | UBOOT | TRUST |   BOOT   |   ROOTFS    |
#  ------- ----------- ------- ------- ---------- -------------
# ^       ^           ^       ^       ^          ^             ^
# |       |           |       |       |          |             |
# 0      32KiB       8MiB   12MiB   16MiB      128MiB  128MiB+SDIMG_SIZE

# Kernel image name
SDIMG_KERNELIMAGE  ?= "Image"

# Boot partition volume id
BOOTDD_VOLUME_ID ?= "${MACHINE}"

# Use an uncompressed ext4 by default as rootfs
SDIMG_ROOTFS_TYPE ?= "ext4"
SDIMG_ROOTFS = "${IMGDEPLOYDIR}/${IMAGE_LINK_NAME}.${SDIMG_ROOTFS_TYPE}"

# This image depends on the rootfs image
IMAGE_TYPEDEP_rock64-sdimg = "${SDIMG_ROOTFS_TYPE}"

# For the names of kernel artifacts
inherit kernel-artifact-names

do_image_rock64_sdimg[depends] = " \
    parted-native:do_populate_sysroot \
    mtools-native:do_populate_sysroot \
    dosfstools-native:do_populate_sysroot \
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    rkbin:do_deploy \
"

do_image_rock64_sdimg[recrdeps] = "do_build"

# Indexes (blocks, bs=512 bytes)
START_IDBLOADER = "64"
START_UBOOT = "16384"
START_TRUST = "24576"
START_BOOT = "32768"
START_ROOTFS = "262144"

# SD card image name
SDIMG = "${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.rock64-sdimg"

IMAGE_CMD_rock64-sdimg () {

	# create boot.img
	rm -f ${WORKDIR}/boot.img
	dd if=/dev/zero of=${WORKDIR}/boot.img count=0 seek=229376
	mkfs.fat ${WORKDIR}/boot.img -n "rock64 boot"
	mcopy -v -i ${WORKDIR}/boot.img -s ${DEPLOY_DIR_IMAGE}/Image ::/ || bbfatal "mcopy cannot copy ${DEPLOY_DIR_IMAGE}/Image into boot.img"
	mcopy -v -i ${WORKDIR}/boot.img -s ${DEPLOY_DIR_IMAGE}/rk3328-rock64.dtb ::/ || bbfatal "mcopy cannot copy ${DEPLOY_DIR_IMAGE}/rk3328-rock64.dtb into boot.img"

	rm -rf ${WORKDIR}/extlinux; mkdir ${WORKDIR}/extlinux
	echo "label rockchip-kernel4.4" >> ${WORKDIR}/extlinux/extlinux.conf
	echo "        kernel /Image" >> ${WORKDIR}/extlinux/extlinux.conf
	echo "        fdt /rk3328-rock64.dtb" >> ${WORKDIR}/extlinux/extlinux.conf
	echo "        append earlyprintk console=ttyS2,1500000n8 rw root=/dev/mmcblk1p2 rootwait rootfstype=ext4 init=/sbin/init" >> ${WORKDIR}/extlinux/extlinux.conf

	mcopy -v -i ${WORKDIR}/boot.img -s ${WORKDIR}/extlinux :: || bbfatal "mcopy cannot copy ${WORKDIR}/extlinux into boot.img"


	# convert $ROOTFS_SIZE from bs=1024 to bs=512
	ROOTFS_SIZE_2=$(expr $ROOTFS_SIZE \* 2)

	# calculate total image size
	SDIMG_SIZE=$(expr ${START_IDBLOADER} + ${START_UBOOT} + ${START_TRUST} + ${START_BOOT} + ${START_ROOTFS} + ${ROOTFS_SIZE_2})

	# Initialize sdcard image file
	rm -f ${SDIMG}
	dd if=/dev/zero of=${SDIMG} count=0 seek=${SDIMG_SIZE}

	# Create partition table
	parted -s ${SDIMG} mklabel msdos
	# Create boot partition and mark it as bootable
	parted -s ${SDIMG} unit s mkpart primary fat32 ${START_BOOT} $(expr ${START_ROOTFS} - 1)
	parted -s ${SDIMG} set 1 boot on
	# Create rootfs partition to the end of disk
	parted -s ${SDIMG} -- unit s mkpart primary ext4 ${START_ROOTFS} -1s
	parted ${SDIMG} print

	# Flash contents
	dd if=${DEPLOY_DIR_IMAGE}/idbloader.bin of=${SDIMG} seek=${START_IDBLOADER}
	dd if=${DEPLOY_DIR_IMAGE}/uboot.img of=${SDIMG} seek=${START_UBOOT}
	dd if=${DEPLOY_DIR_IMAGE}/rkbin/trust.img of=${SDIMG} seek=${START_TRUST}
	dd if=${WORKDIR}/boot.img of=${SDIMG} seek=${START_BOOT}
	dd if=${SDIMG_ROOTFS} of=${SDIMG} conv=notrunc seek=${START_ROOTFS}

}
