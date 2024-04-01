CC = g++  -Werror
.DEFAULT_GOAL := all

# Compile targets for individual source files
client: client.cpp
	$(CC) -o client client.cpp connection.cpp cache.cpp constants.cpp service.cpp utils.cpp exceptions.cpp

# Main target to build all necessary executables
all: client
	
# Clean target to remove generated files
clean: 
	@rm client