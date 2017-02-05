#include <jni.h>
#include <string>

extern "C"
{

jstring
Java_com_easy_wtool_sdk_test_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
}
