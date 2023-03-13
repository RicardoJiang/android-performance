#include "jni.h"
#include <string>

#ifndef ANDROID_PERFORMANCE_THREADSTACKSHINK_H
#define ANDROID_PERFORMANCE_THREADSTACKSHINK_H
namespace thread_stack_shink {
    extern void OnPThreadCreate(pthread_t* pthread, pthread_attr_t const* attr, void* (*start_routine)(void*), void* args);
}
#endif //ANDROID_PERFORMANCE_THREADSTACKSHINK_H
