#ifndef MAIN_HPP
#define MAIN_HPP

#include "Attribute.hpp"

#include <set>
#include <map>


typedef std::set<Attribute> Attributes;
typedef std::multimap<Attributes, Attributes> Dependency;
typedef std::set<Dependency> Dependencies;

#endif
