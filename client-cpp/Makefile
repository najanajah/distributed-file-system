CC = g++ -std=c++11 -Werror
.DEFAULT_GOAL := all

# Compile targets for individual source files
cache: cache.cpp cache.h
	$(CC) -c client.cpp client.h 

constants: constants.cpp constants.h
	$(CC) -c constants.cpp constants.h 

connection: connection.cpp connection.h
	$(CC) -c connection.cpp connection.h 

utils: utils.cpp utils.h
	$(CC) -c utils.cpp utils.h 

exceptions: exceptions.cpp exceptions.h
	$(CC) -c exceptions.cpp exceptions.h 

service: service.cpp service.h
	$(CC) -c service.cpp service.h 

client: cache constants connection utils exceptions service client.cpp
	$(CC) -o cache.o constants.o connection.o utils.o exceptions.o service.o client.cpp  

# Main target to build all necessary executables
all: client
	
# Clean target to remove generated files
clean: 
	@rm client
	@rm *.o
	@rm *.h.gch