//
// Created by Wangne.Wei on 2019/3/20.
//

#ifndef TESTAPPLICATION_ENHANCED_DLCLOSE_H
#define TESTAPPLICATION_ENHANCED_DLCLOSE_H

#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#ifdef __cplusplus
extern "C" {
#endif
    int enhanced_dlclose(void *handle);
    void *enhanced_dlopen(const char *libpath, int flags);
    void *enhanced_dlsym(void *handle, const char *name);
#ifdef __cplusplus
}
#endif
#endif //TESTAPPLICATION_ENHANCED_DLCLOSE_H
