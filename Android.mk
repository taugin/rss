# ============================================================
# This is generated based on build.xml. Please don't edit it.
# Author: Frank Liu <prnq63@motorola.com>
# ============================================================
LOCAL_PATH:= $(call my-dir)

#############hdpi
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := rss 
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_TAGS := eng

LOCAL_SRC_FILES := $(call all-java-files-under, src)

include $(BUILD_PACKAGE)
