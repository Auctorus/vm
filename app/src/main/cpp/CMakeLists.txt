cmake_minimum_required(VERSION 3.10)

add_library(
        verusminer
        SHARED
        verus_jni.c
)

find_library(
        log-lib
        log
)

target_link_libraries(
        verusminer
        ${log-lib}
)
