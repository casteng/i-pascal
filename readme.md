Intellij IDEA Object Pascal plugin
==================================

Description
-----------
Project homepage: http://www.siberika.com/ipascal.htm

A free Object Pascal language plugin for excellent IntelliJ IDEA

Supports integration with Delphi compiler (dcc32) and Free Pascal compilers.

Compatible with IDEA Community Edition as well as Ultimate Edition from version 13.xx and above.

Pascal-specific features currently implemented
----------------------------------------------

* code insight features
    * identifier declaration, references and usages search and highlight
    * context-aware completion
    * statements completion
    * compiler defines and directives completion
    * used units usage check with quick fix
* code analysis
    * undeclared identifiers highlighting with quick fix
    * syntax errors highlighting
    * missing method declaration/implementation highlight with quick fix
    * missing routine implementation highlight with quick fix
    * probably bad code warnings with quick fix
    * unused identifiers highlighting
    * error insight via compiler
    * inherited call in destructor
    * function result assignment check
* refactoring
    * rename identifier
    * introduce variable
    * override inherited methods
* code navigation
    * to super methods/classes
    * to implementations/descendants
    * between unit routine interface definition and implementation
    * between class method definition and implementation
* run and debug right in the IDE
    * GDB and LLDB debug backends
* compiled unit interface decompilation
    * PPU - FPC 2.7.x or above required
    * DCU - DCU32INT (http://hmelnov.icc.ru/DCU) required
* on-the-fly code formatting
* Lazarus and Delphi project files import
* routine parameters hint
* braces matcher
* code commenter
* Free Pascal compiler integration
* Delphi compiler integration
* full Object Pascal parser
* Object Pascal syntax highlighting
* conditional compilation support
* folding support

As well as other features provided by IDEA
------------------------------------------

* full-text find/replace with regular expressions
* version control systems integration (SVN, Git, Mercurial, ...)
* other languages support, such as XML and SQL
* spell checking
* TODO support

to name a few.

Planned in future releases
--------------------------

* more code analysis
* more code inspections
* more statements completion
* more refactorings
* extended includes support
* full error insight with quick fixes
* test frameworks support

Developer environment configuration
-----------------------------------
Instructions
https://www.jetbrains.com/help/idea/2016.3/plugin-development-guidelines.html

Lexer is generated using IDEA JFlex modification.

Parser classes are generated with [Grammar Kit plugin](https://plugins.jetbrains.com/plugin/6606-grammar-kit).

To generate _PascalLexer class open pascal.flex, right click and choose "Run JFlex generator".

To generate parser classes open pascal.bnf, right click and choose "Generate parser code".

**There are JUnit tests. Please ensure the tests passing before creating pull requests.**

Other information
-----------------
Information on how to install and use the plugin is available at [I-Pascal homepage](http://www.siberika.com/ipascal.htm).