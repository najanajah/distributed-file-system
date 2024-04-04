#ifndef EXCEPTIONS_H
#define EXCEPTIONS_H

#include <stdexcept>
#include "constants.h"

class ApplicationException : public std::exception {
public:
    explicit ApplicationException(const std::string& m) : message(m) {}
    const char* what() const noexcept override {
        return message.c_str();
    }
    void check_app_exception(int);
private:
    std::string message;
};

class BadPathnameException : public ApplicationException {
public:
    BadPathnameException() : ApplicationException("pathname does not exist") {}
};

class BadRangeException : public ApplicationException {
public:
    BadRangeException() : ApplicationException("bad range") {}
};

class CorruptMessageException : public ApplicationException {
public:
    CorruptMessageException() : ApplicationException("Corrupt message received") {}
};

class FileAlreadyExistsException : public ApplicationException {
public:
    FileAlreadyExistsException() : ApplicationException("file already exists") {}
};

class FileEmptyException : public ApplicationException {
public:
    FileEmptyException() : ApplicationException("file is empty") {}
};

class NotADirectoryException : public ApplicationException {
public:
    NotADirectoryException() : ApplicationException("Not a directory") {}
};

class NotAFileException : public ApplicationException {
public:
    NotAFileException() : ApplicationException("not a file") {}
};

class ServerBusyException : public ApplicationException {
public:
    ServerBusyException() : ApplicationException("server is busy; please try again later") {}
};

class CorrputMessageException : public ApplicationException{ 
public: 
    CorrputMessageException() : ApplicationException("Received Message is Corrupt"){}

};

class TimeoutException : public ApplicationException{ 
public: 
    TimeoutException() : ApplicationException("Connection Timeout"){}

};


#endif // EXCEPTIONS_H
