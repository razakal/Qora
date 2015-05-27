# https://github.com/kennethreitz/osx-gcc-installer
rm -f *.o *.so binary
gcc -fPIC -c add_scalar.c
gcc -fPIC -c key_exchange.c
gcc -fPIC -c sc.c
gcc -fPIC -c sign.c
gcc -fPIC -c ge.c
gcc -fPIC -c keypair.c
gcc -fPIC -c seed.c
gcc -fPIC -c verify.c
gcc -fPIC -c fe.c
gcc -fPIC -c sha512.c
gcc -shared -o libed25519.mac.dylib add_scalar.o key_exchange.o sc.o sign.o ge.o keypair.o seed.o verify.o fe.o sha512.o
