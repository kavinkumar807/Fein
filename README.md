# Fein
**A custom built programming language using Java.**

## Table of Contents
* Introduction
* Getting Started
  * Installation
  * Basic Usage
* Syntax Grammer
* Lexical Grammer



## Introduction
* Fein is a custom-built dynamically typed scripting language built as hobby project. It is basically a Tree walk interpreter
* As a basic interpreter Fein has implemented using java, with custom built lexer for scanning token 
recursive decent parsing, AST,state resolution and interpreter for evaluating and executing statements and expressions.
* It has all the features as basic scripting language which includes class, functions, methods, inheritance, etc

## Getting Started
### Installation
* Latest JDK installation in local machine is enough to run Fein.
* Run the AST generator tool beforehand for generating necessary AST classes ``java tool/GenerateAst "<Your Path where the class need to be generate>``
* Run the main Fein file to generate class file
``javac Fein.java``
* After generating the class file run the below command ``java Fein "<you code file path>"``

### Basic Usage
* A simple "Hello, World!" example.
``print "Hello World !"``
* Basic syntax and structure overview.

```
// program for declaring variable in Fein the looping
var a = 0;
var temp;
for (var b = 1; a < 10000; b = temp + b) {
  print a;
  temp = a;
  a = b;
}

// program for demonstrating block level scoping
{
  var i = 0;
  while (i < 10) {
    print i;
    i = i + 1;
  }
}

// program for function declaration and calling
fun sayHi(first, last) {
  print "Hi, " + first + " " + last + "!";
}

sayHi("Dear", "Reader");

// program to print function
fun add(a, b) {
  print a + b;
}

print add; // "<fn add>".


// simple fibonacci sequence program
fun fib(n) {
if (n <= 1) return n;
  return fib(n - 2) + fib(n - 1);
}

for (var i = 0; i < 20; i = i + 1) {
  print fib(i);
}


// program to print class
class DevonshireCream {
  serveOn() {
    return "Scones";
  }
}

print DevonshireCream; // Prints "DevonshireCream".

// program to print instance
class Bagel {}
var bagel = Bagel();
print bagel; // Prints "Bagel instance".


// program to call method inside class
class Bacon {
  eat() {
    print "Crunch crunch crunch!";
  }
}

Bacon().eat();

// program to set fields to class and usage of this keyword
class Cake {
  taste() {
    var adjective = "delicious";
    print "The " + this.flavor + " cake is " + adjective + "!";
  }
}

var cake = Cake();
cake.flavor = "German chocolate";
cake.taste(); // Prints "The German chocolate cake is delicious!".

// program to use super and inheritance
class A {
  method() {
    print "A method";
  }
}

class B < A {
  method() {
    print "B method";
  }

  test() {
    super.method();
  }
}

class C < B {}

C().test();
```

## Syntax Grammer
The syntactic grammar is used to parse the linear sequence of tokens into the nested syntax tree structure. It starts with the first rule that matches an entire Fein program (or a single REPL entry).

````
Statements:

       program        → declaration* EOF ;
       declaration    → classDecl
                      | funDecl
                      | varDecl
                      | statement ;
       classDecl      → "class" IDENTIFIER ("<" IDENTIFIER)? "{" function* "}" ;
       funDecl        → "fun" function ;
       function       → IDENTIFIER "(" parameters? ")" block ;
       parameters     → IDENTIFIER ( "," IDENTIFIER )* ;
       varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
       statement      → exprStmt
                      | ifStmt
                      | printStmt
                      | returnStmt
                      | whileStmt
                      | forStmt
                      | block ;
       returnStmt     → "return" expression? ";" ;
       ifStmt         → "if" "(" expression ")" statement
                      ( "else" statement )? ;
       whileStmt      → "while" "(" expression ")" statement ;
       forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
                        expression? ";"
                        expression? ")" statement ;
       block          → "{" declaration * "}" ;
       exprStmt       → expression ";" ;
       printStmt      → "print" expression ";" ;

Expressions:

       expression     → assignment ;
       assignment     → ( call "." )? IDENTIFIER "=" assignment
                      | logic_or ;
       logic_or       → logic_and ( "or" logic_and )* ;
       logic_and      → equality ( "and" equality )* ;
       equality       → comparison ( ( "!=" | "==" ) comparison )* ;
       comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
       term           → factor ( ( "-" | "+" ) factor )* ;
       factor         → unary ( ( "/" | "*" ) unary )* ;
       unary          → ( "!" | "-" ) unary | call ;
       call           → primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
       arguments      → expression ( "," expression )* ;
       primary        → "true" | "false" | "nil" | "this"
                        | NUMBER | STRING | IDENTIFIER | "(" expression ")"
                        | "super" "." IDENTIFIER ;

````

## Lexical Grammer
The lexical grammar is used by the scanner to group characters into tokens. Where the syntax is context free, the lexical grammar 
is regular—note that there are no recursive rules.
```
NUMBER         → DIGIT+ ( "." DIGIT+ )? ;
STRING         → "\"" <any char except "\"">* "\"" ;
IDENTIFIER     → ALPHA ( ALPHA | DIGIT )* ;
ALPHA          → "a" ... "z" | "A" ... "Z" | "_" ;
DIGIT          → "0" ... "9" ;
```



