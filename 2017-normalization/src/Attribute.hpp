#ifndef ATTRIBUTE_HPP
#define ATTRIBUTE_HPP


#include <string>

class Attribute {
	
	private:
	std::string name;
	
	public:
	
	// Constructors and Destructor
	Attribute();
	Attribute(std::string s);
	~Attribute();
	
	// Order functions
	bool operator == (const Attribute& other) const;
	bool operator < (const Attribute& other) const;
	bool operator > (const Attribute& other) const;
};

#endif
