
#include "main.hpp"
#include "Attribute.hpp"

#include <fstream>
#include <string>

using namespace std;

void read_to_stdout() {
}

void write_to_file(string filename) {
}

void read_from_stdin() {
}

void read_from_file(string filename) {
	
	ifstream in(filename);
	
	Dependencies dep;
	
	while(in) {
		Attributes from;
		Attributes to;
		char c;
		in >> c;
		
			
		while(c != '-') {
			while(c != ' ') {
				string s;
				s += c;
				in >> c;
			}
			Attribute att(s);
			from.insert(att);
			in >> c;
		}
		
		in >> c;
		
		if(c != '>') {
			cerr << "bad format";
			exit(1);
		}
		
		in >> c;
		
		if(c != ' ') {
			cerr << "bad format";
			exit(1);
		}
		
		in >> c;
		
		while(c != '\n') {
			while(c != ' ') {
				string s;
				s += c;
				in >> c;
			}
			Attribute att(s);
			to.insert(att);
			in >> c;
		}
		
		Dependency dep;
		
	}
	
	
	
}

int main(int argc, char* argv[]) {
	
	
	
}
