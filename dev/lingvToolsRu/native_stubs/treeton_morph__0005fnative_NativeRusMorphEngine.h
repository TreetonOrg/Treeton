/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class treeton_morph__0005fnative_NativeRusMorphEngine */

#ifndef _Included_treeton_morph__0005fnative_NativeRusMorphEngine
#define _Included_treeton_morph__0005fnative_NativeRusMorphEngine
#ifdef __cplusplus
extern "C" {
#endif
/* Inaccessible static: undef */
/* Inaccessible static: tpsp */
/* Inaccessible static: tnmb */
/* Inaccessible static: tcas */
/* Inaccessible static: tanim */
/* Inaccessible static: tgen */
/* Inaccessible static: norm */
/* Inaccessible static: shrt */
/* Inaccessible static: cmp */
/* Inaccessible static: ta_repr */
/* Inaccessible static: tv_repr */
/* Inaccessible static: tv_asp */
/* Inaccessible static: tv_vox */
/* Inaccessible static: tv_md */
/* Inaccessible static: tv_tns */
/* Inaccessible static: tv_prs */
/*
 * Class:     treeton_morph__0005fnative_NativeRusMorphEngine
 * Method:    processWord
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_treeton_morph__1native_NativeRusMorphEngine_processWord
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     treeton_morph__0005fnative_NativeRusMorphEngine
 * Method:    loadDictionary
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_treeton_morph__1native_NativeRusMorphEngine_loadDictionary
  (JNIEnv *, jobject, jstring);

/*
 * Class:     treeton_morph__0005fnative_NativeRusMorphEngine
 * Method:    initEngine
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_treeton_morph__1native_NativeRusMorphEngine_initEngine
  (JNIEnv *, jobject);

/*
 * Class:     treeton_morph__0005fnative_NativeRusMorphEngine
 * Method:    deinitEngine
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_treeton_morph__1native_NativeRusMorphEngine_deinitEngine
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif