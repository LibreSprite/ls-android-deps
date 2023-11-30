cmake_minimum_required(VERSION 3.4.1)
project(giflib)

find_library(
        z
        z
)

find_library(
        log
        log
)

include_directories(include)

add_library( # Sets the name of the library.
        giflib

        # Sets the library as a shared library.
        SHARED

        dgif_lib.c
        egif_lib.c
        getarg.c
        gif_err.c
        gif_font.c
        gif_hash.c
        gifalloc.c
        openbsd-reallocarray.c
        qprintf.c
        quantize.c

        )


target_link_libraries(
  giflib
)
