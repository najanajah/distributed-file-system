#include <iostream>
#include <sstream>
#include <stdexcept>
#include "utils.h"
#include "connection.h"
#include "service.h"
#include "constants.h"

using namespace std;

int main(int argc, char* argv[]) {
    // args: [ip address, port number, at most once, network failure rate, freshness interval]
    cout<<"Welcome to DFS client "; 
    // if (argc != 6) {
    //     cerr << "Bad parameters. Please provide command line arguments in the format:" << endl;
    //     cerr << "{ip address} {port number} {1 for at most once, 0 for at least once} "
    //          << "{network failure rate} {freshness interval in milliseconds}" << endl;
    //     return 1;
    // }
    string ip_address; 
    int port_number;
    int at_most_once; 
    double network_failure_rate; 
    int freshness_interval; 

    cout<<"please enter the following configurations: ";
    cout<<"ip_address: ";
    cin>>ip_address; 

    cout<<"port_number: ";
    cin>>port_number; 

    cout<<"at_most_once: ";
    cin>>at_most_once; 

    cout<<"network_failure_rate: ";
    cin>>network_failure_rate; 
    
    cout<<"freshness_interval: ";
    cin>>freshness_interval; 

    // string ip_address = argv[1];
    // int port_number = atoi(argv[2]);
    // int at_most_once = atoi(argv[3]);
    // double network_failure_rate = atof(argv[4]);
    // int freshness_interval = atoi(argv[5]);

    Connection connection(ip_address, port_number, at_most_once, network_failure_rate, freshness_interval);
    cout << "Welcome!" << endl;

    while (true) {
        cout << Constants::SERVICE_PROMPT << endl;
        int input;
        cin >> input;
        // cin.ignore(numeric_limits<streamsize>::max(), '\n');

        if (input == Constants::EXIT_ID) {
            break;
        }

        Service* requested_service = Service::generate_service(input, connection);
        if (requested_service != nullptr) {
            requested_service->act();
            delete requested_service;
        }

        cout << "entered successfully";
    }

    // connection.close_connection();
    cout << "Goodbye!" << endl;

    return 0;
}
