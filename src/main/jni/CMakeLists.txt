
cmake_minimum_required(VERSION 2.8)

project(jnidbus)

set(CMAKE_BUILD_TYPE Debug)

find_package(JNI REQUIRED 1.7)

if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif()

include_directories(${JNI_INCLUDE_DIRS})

find_package(PkgConfig)

pkg_check_modules(DBUS dbus-1)

# Use results of pkg_check_modules() call.
include_directories(${DBUS_INCLUDE_DIRS})
link_directories(${DBUS_LIBRARY_DIRS})

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

file (GLOB SOURCES src/headers/*.h src/*.cpp)


add_library(jnidbus SHARED ${SOURCES})
target_link_libraries(jnidbus ${DBUS_LIBRARIES})