#!/bin/bash

rm -rf release/*

cd src/SILK_SDK_SRC_FLP_v1.0.9
./make_so.sh

cp -rf interface ../../release
cp -f libsilk.so ../../release
