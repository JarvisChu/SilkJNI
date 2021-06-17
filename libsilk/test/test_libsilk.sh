# generate encoder and decoder
make

cp ../release/libsilk.so .
rm *.pcm *.silk

# test encoder and decoder
./encoder ../../audio/8000_16bit_1channel.pcm test_encoder_out.silk -Fs_API 8000
./decoder ../../audio/8000_16bit_1channel_20ms.silk test_decoder_out.pcm -Fs_API 8000

rm libsilk.so