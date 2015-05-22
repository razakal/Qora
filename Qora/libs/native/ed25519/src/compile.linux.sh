#sudo apt-get install gcc-multilib 
rm -f *.o *.so binary
gcc -m32 -fPIC -c add_scalar.c
gcc -m32 -fPIC -c key_exchange.c
gcc -m32 -fPIC -c sc.c
gcc -m32 -fPIC -c sign.c
gcc -m32 -fPIC -c ge.c
gcc -m32 -fPIC -c keypair.c
gcc -m32 -fPIC -c seed.c
gcc -m32 -fPIC -c verify.c
gcc -m32 -fPIC -c fe.c
gcc -m32 -fPIC -c sha512.c
gcc -m32 -shared -o ed25519.linux.32.so add_scalar.o key_exchange.o sc.o sign.o ge.o keypair.o seed.o verify.o fe.o sha512.o
rm -f *.o
gcc -m64 -fPIC -c add_scalar.c
gcc -m64 -fPIC -c key_exchange.c
gcc -m64 -fPIC -c sc.c
gcc -m64 -fPIC -c sign.c
gcc -m64 -fPIC -c ge.c
gcc -m64 -fPIC -c keypair.c
gcc -m64 -fPIC -c seed.c
gcc -m64 -fPIC -c verify.c
gcc -m64 -fPIC -c fe.c
gcc -m64 -fPIC -c sha512.c
gcc -m64 -shared -o ed25519.linux.64.so add_scalar.o key_exchange.o sc.o sign.o ge.o keypair.o seed.o verify.o fe.o sha512.o