#ifndef _DRAWER_MANAGER_H_
#define _DRAWER_MANAGER_H_

#include <jni.h>

#ifdef __cplusplus
extern "C"{
#endif

jint Java_org_mconf_android_core_video_VideoSurface_initDrawer(JNIEnv *env, jobject obj, jint screenW, jint screenH, jint displayAreaW, jint displayAreaH, jint displayPositionX, jint displayPositionY);
jint Java_org_mconf_android_core_video_VideoSurface_enqueueFrame(JNIEnv *env, jobject obj, jbyteArray data, jint length);
jint Java_org_mconf_android_core_video_VideoRenderer_nativeRender(JNIEnv *env, jobject obj);
jint Java_org_mconf_android_core_video_VideoSurface_nativeResize(JNIEnv *env, jobject obj, jint screenW, jint screenH, jint displayAreaW, jint displayAreaH, jint displayPositionX, jint displayPositionY);
jint Java_org_mconf_android_core_video_VideoRenderer_getVideoWidth(JNIEnv *env, jobject obj);
jint Java_org_mconf_android_core_video_VideoRenderer_getVideoHeight(JNIEnv *env, jobject obj);
jint Java_org_mconf_android_core_video_VideoSurface_endDrawer(JNIEnv *env, jobject obj);

#ifdef __cplusplus
}
#endif

#endif
