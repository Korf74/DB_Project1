#include <string>

#include "Attribute.hpp"

using namespace std;

Attribute::Attribute() : name("") {
}

Attribute::Attribute(std::string s) {
	name = s.copy();
}

Attribute::~Attribute() {
}

// Order functions
bool Attribute::operator == (const Attribute& other) const {
}

bool Attribute::operator < (const Attribute& other) const {
}

bool Attribute::operator > (const Attribute& other) const {
}
