LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_USE_AAPT2 := true
LOCAL_MODULE := settingscards-res
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/src/main/res
LOCAL_FULL_MANIFEST_FILE := $(LOCAL_PATH)/src/main/AndroidManifest.xml
LOCAL_JAVA_LANGUAGE_VERSION := 1.7
include $(BUILD_STATIC_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_USE_AAPT2 := true
LOCAL_MODULE := settingscards
LOCAL_SRC_FILES := $(call all-java-files-under,src/main/java)
LOCAL_FULL_MANIFEST_FILE := $(LOCAL_PATH)/src/main/AndroidManifest.xml
LOCAL_STATIC_ANDROID_LIBRARIES := settingscards-res
LOCAL_SHARED_ANDROID_LIBRARIES := android-support-v7-appcompat android-support-v7-cardview android-support-annotations android-support-v4
LOCAL_JAR_EXCLUDE_FILES := none
LOCAL_JAVA_LANGUAGE_VERSION := 1.7
include $(BUILD_STATIC_JAVA_LIBRARY)
